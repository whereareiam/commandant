package me.whereareiam.commandant;

import me.whereareiam.commandant.annotation.Definition;
import me.whereareiam.commandant.fixture.TestSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotatedCommandsTest {
	private CommandManager<TestSender> commandManager;

	@BeforeEach
	void setUp() {
		commandManager = new CommandManager<>(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {
			@Override
			public boolean hasPermission(@NotNull TestSender sender, @NotNull String permission) {
				return true;
			}
		};
	}

	@Test
	void parsesWithoutRegistering() {
		List<Command<TestSender>> commands = AnnotatedCommands.of(commandManager, TestSender.class).parse(new Methods());

		assertEquals(2, commands.size());
		assertTrue(commandManager.commands().isEmpty());
	}

	@Test
	void givesEachCommandItsDefinition() {
		Map<String, Optional<String>> definitions = definitions(new Methods());

		assertEquals(Optional.of("reload"), definitions.get("plugin reload"));
		assertEquals(Optional.of("grant"), definitions.get("plugin grant"));
	}

	@Test
	void takesTheClassDefinitionWhereTheMethodHasNone() {
		Map<String, Optional<String>> definitions = definitions(new Container());

		assertEquals(Optional.of("container"), definitions.get("plugin open"));
		assertEquals(Optional.of("close"), definitions.get("plugin close"));
	}

	@Test
	void registersOnceProcessed() {
		AnnotatedCommands<TestSender> annotated = AnnotatedCommands.of(commandManager, TestSender.class);
		annotated.parse(new Methods()).forEach(command -> Commandant.process(command, commandManager).withoutDefinition());

		assertEquals(2, commandManager.commands().size());
	}

	private Map<String, Optional<String>> definitions(Object container) {
		return AnnotatedCommands.of(commandManager, TestSender.class).parse(container).stream().collect(Collectors.toMap(
				command -> command.rootComponent().name() + " " + command.components().get(1).name(),
				command -> command.commandMeta().optional(CommandantKeys.DEFINITION_ID)));
	}

	static final class Methods {
		@Definition("reload")
		@org.incendo.cloud.annotations.Command("plugin reload")
		public void reload(TestSender sender) {
		}

		@Definition("grant")
		@org.incendo.cloud.annotations.Command("plugin grant <stage>")
		public void grant(TestSender sender, @Argument("stage") String stage) {
		}
	}

	@Definition("container")
	static final class Container {
		@org.incendo.cloud.annotations.Command("plugin open")
		public void open(TestSender sender) {
		}

		@Definition("close")
		@org.incendo.cloud.annotations.Command("plugin close")
		public void close(TestSender sender) {
		}
	}
}
