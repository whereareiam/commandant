package me.whereareiam.commandant.fixture;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.whereareiam.keystone.Recipient;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * A sender of commands in Commandant's tests: a player with an id and a language, the console, or a sender without an
 * id such as a command block. It keeps the plain text of every chat line it is shown.
 */
@Getter
@Accessors(fluent = true)
public final class TestSender {
	private final String name;
	private final Optional<UUID> uniqueId;
	private final Optional<Locale> language;
	private final boolean console;
	private final List<Component> shown = new ArrayList<>();

	private TestSender(String name, Optional<UUID> uniqueId, Optional<Locale> language, boolean console) {
		this.name = name;
		this.uniqueId = uniqueId;
		this.language = language;
		this.console = console;
	}

	/**
	 * Returns a player who reads English.
	 *
	 * @param name the player's name
	 * @return the player
	 */
	public static @NotNull TestSender player(@NotNull String name) {
		return new TestSender(name, Optional.of(UUID.randomUUID()), Optional.of(Locale.ENGLISH), false);
	}

	/**
	 * Returns the console.
	 *
	 * @return the console
	 */
	public static @NotNull TestSender console() {
		return new TestSender("CONSOLE", Optional.empty(), Optional.empty(), true);
	}

	/**
	 * Returns a sender without an id that is no console, such as a command block.
	 *
	 * @return the sender
	 */
	public static @NotNull TestSender block() {
		return new TestSender("@", Optional.empty(), Optional.empty(), false);
	}

	/**
	 * Returns the plain text of each chat line the sender was shown, in order.
	 *
	 * @return the lines
	 */
	public @NotNull List<String> lines() {
		return shown.stream().map(PlainTextComponentSerializer.plainText()::serialize).toList();
	}

	/**
	 * Returns the commands the chat lines it was shown run when clicked, in order.
	 *
	 * @return the commands
	 */
	public @NotNull List<String> clickable() {
		List<String> commands = new ArrayList<>();
		shown.forEach(line -> collect(line, commands));

		return commands;
	}

	private static void collect(Component component, List<String> commands) {
		ClickEvent click = component.clickEvent();
		if (click != null && click.action() == ClickEvent.Action.RUN_COMMAND && click.payload() instanceof ClickEvent.Payload.Text text)
			commands.add(text.value());
		component.children().forEach(child -> collect(child, commands));
	}

	/**
	 * Returns how Keystone sees the sender.
	 *
	 * @return the recipient
	 */
	@NotNull Recipient recipient() {
		TestSender sender = this;
		Audience audience = new Audience() {
			@Override
			public void sendMessage(@NotNull Component message) {
				shown.add(message);
			}
		};

		return new Recipient() {
			@Override
			public @NotNull String name() {
				return sender.name;
			}

			@Override
			public @NotNull Optional<UUID> uniqueId() {
				return sender.uniqueId;
			}

			@Override
			public @NotNull Optional<Locale> clientLanguage() {
				return sender.language;
			}

			@Override
			public @NotNull Audience audience() {
				return audience;
			}

			@Override
			public boolean isConsole() {
				return sender.console;
			}
		};
	}
}
