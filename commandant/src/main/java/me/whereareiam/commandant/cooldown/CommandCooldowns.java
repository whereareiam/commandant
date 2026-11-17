package me.whereareiam.commandant.cooldown;

import me.whereareiam.commandant.Commandant;
import me.whereareiam.keystone.Messenger;
import me.whereareiam.keystone.Recipient;
import org.incendo.cloud.CommandManager;
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

/**
 * Enforces the cooldowns that definitions give their commands.
 * <p>
 * {@link Commandant#process Commandant} marks a command with the cooldown of its definition; this class registers the
 * postprocessor on the command manager that keeps a sender from running such a command again until the cooldown is
 * over, and tells the sender how long to wait, in their language, with Commandant's
 * {@code commandant.exception.cooldown}. Cooldowns are kept per sender, by the unique id the plugin's Keystone
 * messenger sees, in memory only: they do not survive a restart, and {@link #clear(UUID)} ends a sender's cooldowns
 * early. The console and senders without an id, such as command blocks, never wait. A command is put on cooldown as
 * soon as it passes parsing and permission checks, whatever its handler does afterwards.
 * <pre>{@code
 * CommandCooldowns<CommandSender> cooldowns = CommandCooldowns.register(commandManager, messenger);
 *
 * // when the plugin stops
 * cooldowns.close();
 * }</pre>
 *
 * @param <S> the sender type
 */
public final class CommandCooldowns<S> implements AutoCloseable {
	private final CooldownRepository<UUID> profiles;
	private final ScheduledExecutorService cleanup;

	private CommandCooldowns(@NotNull CooldownRepository<UUID> profiles, @NotNull ScheduledExecutorService cleanup) {
		this.profiles = profiles;
		this.cleanup = cleanup;
	}

	/**
	 * Enforces cooldowns.
	 *
	 * @param commandManager the command manager whose commands are enforced
	 * @param messenger      the plugin's messenger over the same senders, which tells who a sender is and tells them
	 *                       to wait
	 * @param <S>            the sender type
	 * @return the registration, to be closed when the command manager is no longer used
	 */
	public static <S> @NotNull CommandCooldowns<S> register(@NotNull CommandManager<S> commandManager, @NotNull Messenger<S> messenger) {
		return register(commandManager, messenger, Clock.systemUTC());
	}

	static <S> @NotNull CommandCooldowns<S> register(
			@NotNull CommandManager<S> commandManager,
			@NotNull Messenger<S> messenger,
			@NotNull Clock clock
	) {
		ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor(task -> {
			Thread thread = new Thread(task, "commandant-cooldowns");
			thread.setDaemon(true);
			return thread;
		});
		CooldownRepository<UUID> profiles = CooldownRepository.forMap(new HashMap<>());
		CooldownRepository<S> repository = CooldownRepository.mapping(sender -> messenger.recipient(sender).uniqueId().orElseThrow(), profiles);

		CooldownConfiguration<S> configuration = CooldownConfiguration.<S>builder()
				.repository(repository)
				.addActiveCooldownListener((sender, command, cooldown, remaining) ->
						messenger.send(sender, Commandant.messages().exception().cooldown(seconds(remaining))))
				.addCreationListener(new ScheduledCleanupCreationListener<>(cleanup, repository))
				.bypassCooldown(context -> unlimited(messenger.recipient(context.sender())))
				.clock(clock)
				.build();
		commandManager.registerCommandPostProcessor(CooldownManager.cooldownManager(configuration).createPostprocessor());

		return new CommandCooldowns<>(profiles, cleanup);
	}

	/**
	 * Ends every cooldown of a sender, such as when a player leaves, so the sender can run any command at once.
	 *
	 * @param uniqueId the sender's unique id, as their Keystone recipient has it
	 */
	public void clear(@NotNull UUID uniqueId) {
		profiles.deleteProfile(uniqueId);
	}

	/**
	 * Stops removing expired cooldowns in the background. Cooldowns are still enforced afterwards, and expired ones are
	 * dropped when their sender runs a command again.
	 */
	@Override
	public void close() {
		cleanup.shutdownNow();
	}

	// The seconds left, rounded up.
	private static long seconds(@NotNull Duration remaining) {
		return Math.max(1, (remaining.toMillis() + 999) / 1000);
	}

	private static boolean unlimited(@NotNull Recipient recipient) {
		return recipient.isConsole() || recipient.uniqueId().isEmpty();
	}
}
