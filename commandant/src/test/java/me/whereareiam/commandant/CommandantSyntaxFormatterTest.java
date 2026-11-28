package me.whereareiam.commandant;

import me.whereareiam.keystone.model.SerializerOptions;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandantSyntaxFormatterTest {
	private final CommandManager<Object> manager = new CommandManager<>(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {
		@Override
		public boolean hasPermission(@NotNull Object sender, @NotNull String permission) {
			return true;
		}
	};

	@Test
	void writesEachArgumentWithItsTemplateAndItsShownName() {
		Command<Object> command = manager.commandBuilder("ban")
				.required("target", stringParser())
				.optional("days", integerParser())
				.build();
		CommandantSyntaxFormatter<Object> formatter = new CommandantSyntaxFormatter<>(
				manager,
				Map.of("target", "player"),
				"<{name}>",
				"[{name}]",
				SerializerOptions.PlaceholderFormat.CURLY_BRACES
		);

		assertEquals("ban <player> [days]", formatter.apply(null, command.components(), null));
	}
}
