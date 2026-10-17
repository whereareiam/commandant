package me.whereareiam.commandant.cooldown;

import me.whereareiam.commandant.exception.ExceptionMessageKeys;
import me.whereareiam.commandant.model.Console;
import me.whereareiam.commandant.model.message.ExceptionMessages;
import me.whereareiam.commandant.model.message.MessageResolver;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.incendo.cloud.processors.cooldown.CooldownConfiguration;
import org.incendo.cloud.processors.cooldown.CooldownManager;
import org.incendo.cloud.processors.cooldown.CooldownRepository;
import org.incendo.cloud.processors.cooldown.listener.ScheduledCleanupCreationListener;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Enforces the cooldowns that definitions give their commands.
 * <p>
 * {@link me.whereareiam.commandant.Commandant#process Commandant} marks a command with the cooldown of its definition;
 * this class registers the postprocessor on the command manager that keeps a sender from running such a command
 * again until the cooldown is over, and tells the sender how long to wait. Cooldowns are kept per sender, identified
 * by {@link Actor#getUniqueId()}, in memory only; they do not survive a restart. The console, identified by
 * {@link Console#CONSOLE_UUID}, never waits. A command is put on cooldown as soon as it passes parsing and permission
 * checks, whatever its handler does afterwards. A blank message refuses the sender without telling them.
 * <p>
 * Example usage:
 * <pre>{@code
 * CommandCooldowns<Player> cooldowns = CommandCooldowns.register(
 *     commandManager,
 *     config.getExceptionMessages(),
 *     serializer,
 *     Player::getAudience
 * );
 *
 * // when the plugin stops
 * cooldowns.close();
 * }</pre>
 * <p>
 * <b>Placeholders:</b> the message receives the seconds left, rounded up, as {@code content}; its format, such as
 * {@code {content}}, is configured in the SerializerEngine.
 *
 * @param <S> the sender type, which must implement {@link Actor} at runtime
 */
public final class CommandCooldowns<S> implements AutoCloseable {
	private final ScheduledExecutorService cleanup;

	private CommandCooldowns(@NotNull ScheduledExecutorService cleanup) {
		this.cleanup = cleanup;
	}

	/**
	 * Enforces cooldowns with the message of a static message model.
	 *
	 * @param commandManager   the command manager whose commands are enforced
	 * @param messages         the messages; {@link ExceptionMessages#getCooldown()} is read whenever a sender is refused
	 * @param serializer       the serializer engine that formats the message
	 * @param audienceProvider converts a sender into the audience that receives the message
	 * @param <S>              the sender type, which must implement {@link Actor} at runtime
	 * @return the registration, to be closed when the command manager is no longer used
	 */
	public static <S> @NotNull CommandCooldowns<S> register(
			@NotNull CommandManager<S> commandManager,
			@NotNull ExceptionMessages messages,
			@NotNull SerializerEngine serializer,
			@NotNull AudienceProvider<S> audienceProvider
	) {
		return register(commandManager, () -> messages, serializer, audienceProvider);
	}

	/**
	 * Enforces cooldowns with the message of a static message model that may be replaced, such as by a reload.
	 *
	 * @param commandManager   the command manager whose commands are enforced
	 * @param messages         supplies the current messages whenever a sender is refused
	 * @param serializer       the serializer engine that formats the message
	 * @param audienceProvider converts a sender into the audience that receives the message
	 * @param <S>              the sender type, which must implement {@link Actor} at runtime
	 * @return the registration, to be closed when the command manager is no longer used
	 */
	public static <S> @NotNull CommandCooldowns<S> register(
			@NotNull CommandManager<S> commandManager,
			@NotNull Supplier<ExceptionMessages> messages,
			@NotNull SerializerEngine serializer,
			@NotNull AudienceProvider<S> audienceProvider
	) {
		return register(
				commandManager,
				actor -> messages.get().getCooldown(),
				serializer,
				audienceProvider,
				Clock.systemUTC()
		);
	}

	/**
	 * Enforces cooldowns with a message resolved by key, such as from a translation system.
	 *
	 * @param commandManager   the command manager whose commands are enforced
	 * @param keys             the message keys; {@link ExceptionMessageKeys#getCooldown()} is resolved
	 * @param resolver         resolves the message for the refused sender
	 * @param serializer       the serializer engine that formats the message
	 * @param audienceProvider converts a sender into the audience that receives the message
	 * @param <S>              the sender type, which must implement {@link Actor} at runtime
	 * @return the registration, to be closed when the command manager is no longer used
	 */
	public static <S> @NotNull CommandCooldowns<S> register(
			@NotNull CommandManager<S> commandManager,
			@NotNull ExceptionMessageKeys keys,
			@NotNull MessageResolver resolver,
			@NotNull SerializerEngine serializer,
			@NotNull AudienceProvider<S> audienceProvider
	) {
		return register(
				commandManager,
				actor -> resolver.resolve(actor, keys.getCooldown()),
				serializer,
				audienceProvider,
				Clock.systemUTC()
		);
	}

	static <S> @NotNull CommandCooldowns<S> register(
			@NotNull CommandManager<S> commandManager,
			@NotNull Function<Actor, String> message,
			@NotNull SerializerEngine serializer,
			@NotNull AudienceProvider<S> audienceProvider,
			@NotNull Clock clock
	) {
		ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor(task -> {
			Thread thread = new Thread(task, "commandant-cooldowns");
			thread.setDaemon(true);
			return thread;
		});
		CooldownRepository<S> repository = CooldownRepository.mapping(
				sender -> ((Actor) sender).getUniqueId(),
				CooldownRepository.<UUID>forMap(new HashMap<>())
		);

		CooldownConfiguration<S> configuration = CooldownConfiguration.<S>builder()
				.repository(repository)
				.addActiveCooldownListener((sender, command, cooldown, remaining) ->
						refuse(sender, remaining, message, serializer, audienceProvider))
				.addCreationListener(new ScheduledCleanupCreationListener<>(cleanup, repository))
				.bypassCooldown(context -> isConsole(context.sender()))
				.clock(clock)
				.build();
		commandManager.registerCommandPostProcessor(CooldownManager.cooldownManager(configuration).createPostprocessor());

		return new CommandCooldowns<>(cleanup);
	}

	/**
	 * Stops removing expired cooldowns in the background. Cooldowns are still enforced afterwards, and expired ones
	 * are dropped when their sender runs a command again.
	 */
	@Override
	public void close() {
		cleanup.shutdownNow();
	}

	private static <S> void refuse(
			@NotNull S sender,
			@NotNull Duration remaining,
			@NotNull Function<Actor, String> message,
			@NotNull SerializerEngine serializer,
			@NotNull AudienceProvider<S> audienceProvider
	) {
		Actor actor = (Actor) sender;
		String template = message.apply(actor);
		if (template == null || template.isBlank()) return;

		String seconds = String.valueOf(Math.max(1, (remaining.toMillis() + 999) / 1000));
		Component component = serializer.serialize(actor, template, builder -> builder.placeholder("content", seconds));
		audienceProvider.apply(sender).sendMessage(component);
	}

	private static boolean isConsole(@NotNull Object sender) {
		return ((Actor) sender).getUniqueId().equals(Console.CONSOLE_UUID);
	}
}
