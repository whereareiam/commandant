package me.whereareiam.commandant.common;

import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.builder.HelpBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.whereareiam.commandant.builder.HelpBuilder.Text;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultHelpBuilderTest {
	private static final String PREFIX = "arbor.help";

	private CommandManager<TestSender> commandManager;

	@BeforeEach
	void setUp() {
		commandManager = new CommandManager<>(
				ExecutionCoordinator.simpleCoordinator(),
				CommandRegistrationHandler.nullCommandRegistrationHandler()
		) {
			@Override
			public boolean hasPermission(@NotNull TestSender sender, @NotNull String permission) {
				return true;
			}
		};
	}

	@Test
	void buildsAPageOfTranslatableTexts() {
		Command<TestSender> status = commandManager.commandBuilder("arbor")
				.literal("status")
				.commandDescription(CommandDescription.commandDescription(new KeyDescription("arbor.command.status.description")))
				.handler(context -> {})
				.build();
		Command<TestSender> module = commandManager.commandBuilder("arbor")
				.literal("module")
				.required("id", stringParser())
				.optional("page", integerParser())
				.commandDescription(CommandDescription.commandDescription("Shows a module"))
				.handler(context -> {})
				.build();

		HelpBuilder<TestSender> help = help(Map.of("id", "module"), null, 7);

		assertEquals(
				String.join("\n",
						"arbor.help.title(host=Arbor)",
						"arbor.help.command(command=arbor, arguments= status, description=arbor.command.status.description())",
						"arbor.help.command(command=arbor, arguments= module arbor.help.argument(name=module) "
								+ "arbor.help.optionalArgument(name=page), description=Shows a module)"
				),
				Rendered.of(help.build(List.of(status, module), 1))
		);
	}

	@Test
	void everyTextGetsTheArgumentsItDeclares() {
		Command<TestSender> module = commandManager.commandBuilder("arbor")
				.literal("module")
				.required("id", stringParser())
				.optional("page", integerParser())
				.handler(context -> {})
				.build();
		HelpBuilder<TestSender> help = help(Map.of(), null, 7);

		Map<String, Set<String>> built = new HashMap<>(Rendered.arguments(help.build(List.of(module), 1)));
		built.putAll(Rendered.arguments(help.build(List.of(), 1)));

		Map<String, Set<String>> declared = Rendered.declared(PREFIX, Text.values());
		assertEquals(declared, built);
	}

	@Test
	void showsTheEmptyLineWhenAPageHasNoCommand() {
		HelpBuilder<TestSender> help = help(Map.of(), null, 7);

		assertEquals(
				"arbor.help.title(host=Arbor)\narbor.help.empty()",
				Rendered.of(help.build(List.of(), 1))
		);
		assertEquals(
				"arbor.help.title(host=Arbor)\narbor.help.empty()",
				Rendered.of(help.build(List.of(command("alpha", "alpha")), 2))
		);
	}

	@Test
	void addsThePageLineUnderTheCommands() {
		DefaultPaginationBuilder pagination = new DefaultPaginationBuilder(
				PREFIX + ".pagination",
				Rendered.ARGUMENTS,
				page -> "/arbor help " + page,
				false,
				false,
				false
		);
		HelpBuilder<TestSender> help = help(Map.of(), pagination, 1);

		assertEquals(
				String.join("\n",
						"arbor.help.title(host=Arbor)",
						"arbor.help.command(command=bravo, arguments=, description=bravo)",
						"arbor.help.pagination.line(current=2, max=2, "
								+ "previous=arbor.help.pagination.previous(page=1)[click /arbor help 1], next=)"
				),
				Rendered.of(help.build(List.of(command("alpha", "alpha"), command("bravo", "bravo")), 2))
		);
	}

	@Test
	void dedupedCommandsDrivePaginationTotals() {
		Collection<Command<TestSender>> commands = List.of(
				command("alpha", "alpha"),
				command("alphaalias", "alpha"),
				command("bravo", "bravo"),
				command("bravoalias", "bravo"),
				command("charlie", "charlie"),
				command("charliealias", "charlie"),
				command("delta", "delta"),
				command("deltaalias", "delta"),
				command("echo", "echo"),
				command("echoalias", "echo"),
				command("foxtrot", "foxtrot"),
				command("foxtrotalias", "foxtrot"),
				command("golf", "golf"),
				command("golfalias", "golf")
		);
		HelpBuilder<TestSender> help = new DefaultHelpBuilder<>(
				PREFIX,
				Rendered.ARGUMENTS,
				"Arbor",
				Map.of(),
				new DefaultPaginationBuilder(PREFIX + ".pagination", Rendered.ARGUMENTS, null, false, false, false),
				3,
				true,
				true
		);

		String secondPage = Rendered.of(help.build(commands, 2));
		String thirdPage = Rendered.of(help.build(commands, 3));

		assertTrue(secondPage.contains("current=2, max=3"));
		assertTrue(thirdPage.contains("current=3, max=3"));
		assertTrue(thirdPage.contains("command=golf"));
	}

	private HelpBuilder<TestSender> help(
			Map<String, String> argumentNames,
			DefaultPaginationBuilder pagination,
			int itemsPerPage
	) {
		return new DefaultHelpBuilder<>(PREFIX, Rendered.ARGUMENTS, "Arbor", argumentNames, pagination, itemsPerPage, false, false);
	}

	private Command<TestSender> command(String alias, String definitionId) {
		return commandManager.commandBuilder(alias)
				.meta(CommandantKeys.DEFINITION_ID, definitionId)
				.commandDescription(CommandDescription.commandDescription(definitionId))
				.handler(context -> {})
				.build();
	}

	// A description that is a component, as a translatable description is.
	private record KeyDescription(String key) implements Description, ComponentLike {
		@Override
		public @NotNull String textDescription() {
			return key;
		}

		@Override
		public @NotNull Component asComponent() {
			return Component.translatable(key);
		}
	}

	private static final class TestSender {
	}
}
