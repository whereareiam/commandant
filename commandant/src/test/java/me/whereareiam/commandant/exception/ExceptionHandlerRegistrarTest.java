package me.whereareiam.commandant.exception;

import me.whereareiam.commandant.exception.format.CloudPermissionFormatters;
import me.whereareiam.commandant.exception.format.ExceptionFormatting;
import me.whereareiam.commandant.fixture.TestMessenger;
import me.whereareiam.commandant.fixture.TestSender;
import me.whereareiam.keystone.Messenger;
import me.whereareiam.semantica.testing.MemorySource;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletionException;

import static org.incendo.cloud.parser.standard.BooleanParser.booleanParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionHandlerRegistrarTest {
	private final TestSender alice = TestSender.player("Alice");
	private CommandManager<TestSender> commandManager;

	@BeforeEach
	void setUp() {
		commandManager = new CommandManager<>(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler()) {
			@Override
			public boolean hasPermission(@NotNull TestSender sender, @NotNull String permission) {
				return permission.isEmpty();
			}
		};
	}

	@Test
	void tellsAMissingPermissionAsCloudWritesIt() {
		ExceptionHandlerRegistrar.register(commandManager, TestMessenger.create());
		commandManager.command(commandManager.commandBuilder("admin").permission("identica.admin").handler(context -> {}));

		run("admin");

		// Cloud hands over the command's permission inside the sender's requirement, written in parentheses.
		assertEquals(List.of("You don't have \"(identica.admin)\" permission to use this command."), alice.lines());
	}

	@Test
	void writesAMissingPermissionAsTheFormattingSays() {
		ExceptionHandlerRegistrar.register(commandManager, TestMessenger.create(), ExceptionFormatting.builder()
				.format(Permission.class, CloudPermissionFormatters.minimal())
				.build());
		commandManager.command(commandManager.commandBuilder("audit")
				.permission(Permission.anyOf(
						Permission.permission("identica.admin"),
						Permission.allOf(Permission.permission("identica.staff"), Permission.permission("identica.audit"))))
				.handler(context -> {}));

		run("audit");

		assertEquals(List.of("You don't have \"identica.admin | (identica.audit & identica.staff)\" permission to use this command."),
				alice.lines());
	}

	@Test
	void takesThePluginsOwnWording() {
		Messenger<TestSender> messenger = TestMessenger.create(new MemorySource("plugin")
				.put(Locale.ENGLISH, "commandant.exception.no-permission", "Denied {permission}"));
		ExceptionHandlerRegistrar.register(commandManager, messenger);
		commandManager.command(commandManager.commandBuilder("admin").permission("identica.admin").handler(context -> {}));

		run("admin");

		assertEquals(List.of("Denied (identica.admin)"), alice.lines());
	}

	@Test
	void tellsTheSyntaxAndArgumentsThatCannotBeRead() {
		ExceptionHandlerRegistrar.register(commandManager, TestMessenger.create());
		commandManager.command(commandManager.commandBuilder("warp").required("name", stringParser()).optional("page", integerParser()).handler(context -> {}));
		commandManager.command(commandManager.commandBuilder("pages").required("page", integerParser()).handler(context -> {}));
		commandManager.command(commandManager.commandBuilder("toggle").required("on", booleanParser()).handler(context -> {}));

		run("warp");
		run("pages ten");
		run("toggle maybe");

		assertEquals(List.of(
				"Invalid syntax, please use: /warp <name> [page]",
				"You tried to use ten as a number, but it's not a valid value, please use a valid number.",
				"You tried to use maybe as a boolean, but it's not a valid value, please use true or false."
		), alice.lines());
	}

	@Test
	void tellsAFailingHandler() {
		ExceptionHandlerRegistrar.register(commandManager, TestMessenger.create());
		commandManager.command(commandManager.commandBuilder("broken").handler(context -> {
			throw new IllegalStateException("the vault is closed");
		}));

		run("broken");

		assertEquals(List.of("An error occurred while executing the command: java.lang.IllegalStateException: the vault is closed"),
				alice.lines());
	}

	private void run(String input) {
		try {
			commandManager.commandExecutor().executeCommand(alice, input).join();
		} catch (CompletionException ignored) {
			// The exception controller has told the sender; Cloud still completes the execution exceptionally.
		}
	}
}
