package me.whereareiam.commandant.common;

import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.builder.HelpBuilder;
import me.whereareiam.commandant.fixture.TestMessenger;
import me.whereareiam.commandant.fixture.TestSender;
import me.whereareiam.commandant.message.CommandantMessages;
import me.whereareiam.commandant.message.GeneratedCommandantMessages;
import me.whereareiam.commandant.model.TranslatableDescription;
import me.whereareiam.keystone.Messenger;
import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.Value;
import me.whereareiam.semantica.testing.MemorySource;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.description.CommandDescription;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.stringParser;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultHelpBuilderTest {
	private static final CommandantMessages MESSAGES = new GeneratedCommandantMessages();

	private final TestSender reader = TestSender.player("Alice");
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
	void showsAPageWithEachArgumentAndATranslatedDescription() {
		Command<TestSender> status = commandManager.commandBuilder("arbor")
				.literal("status")
				.commandDescription(CommandDescription.commandDescription(TranslatableDescription.of(Message.of("arbor.status"))))
				.handler(context -> {})
				.build();
		Command<TestSender> module = commandManager.commandBuilder("arbor")
				.literal("module")
				.required("id", stringParser())
				.optional("page", integerParser())
				.commandDescription(CommandDescription.commandDescription("Shows a module"))
				.handler(context -> {})
				.build();
		Messenger<TestSender> messenger = TestMessenger.create(new MemorySource("arbor")
				.put(Locale.ENGLISH, "arbor.status", "Shows the status"));

		show(messenger, help(Map.of("id", "module"), null, 7).build(List.of(status, module), 1));

		assertEquals(List.of(
				"Arbor commands",
				"/arbor status - Shows the status",
				"/arbor module <module> [page] - Shows a module"
		), reader.lines());
	}

	@Test
	void takesThePluginsOwnWordingOfAnArgument() {
		Command<TestSender> warp = commandManager.commandBuilder("warp")
				.required("name", stringParser())
				.handler(context -> {})
				.build();
		Messenger<TestSender> messenger = TestMessenger.create(new MemorySource("plugin")
				.put(Locale.ENGLISH, "commandant.help.argument", "({name})"));

		show(messenger, help(Map.of(), null, 7).build(List.of(warp), 1));

		assertEquals("/warp (name) - ", reader.lines().get(1));
	}

	@Test
	void showsTheEmptyLineWhenAPageHasNoCommand() {
		HelpBuilder<TestSender> help = help(Map.of(), null, 7);

		assertEquals(List.of(MESSAGES.help().title("Arbor"), MESSAGES.help().empty()), help.build(List.of(), 1));
		assertEquals(List.of(MESSAGES.help().title("Arbor"), MESSAGES.help().empty()), help.build(List.of(command("alpha", "alpha")), 2));
	}

	@Test
	void addsThePageLineUnderTheCommands() {
		DefaultPaginationBuilder pagination = new DefaultPaginationBuilder(MESSAGES.pagination(), page -> "/arbor help " + page, false, false, false);

		show(TestMessenger.create(), help(Map.of(), pagination, 1).build(List.of(command("alpha", "alpha"), command("bravo", "bravo")), 2));

		assertEquals(List.of("Arbor commands", "/bravo - bravo", "Page 2 of 2 « Page 1 "), reader.lines());
		assertEquals(List.of("/arbor help 1"), reader.clickable());
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
				MESSAGES.help(),
				"Arbor",
				Map.of(),
				new DefaultPaginationBuilder(MESSAGES.pagination(), null, false, false, false),
				3,
				true,
				true
		);

		List<Message> secondPage = help.build(commands, 2);
		List<Message> thirdPage = help.build(commands, 3);

		assertEquals(Map.of("current", 2, "max", 3), numbers(secondPage.getLast()));
		assertEquals(Map.of("current", 3, "max", 3), numbers(thirdPage.getLast()));
		show(TestMessenger.create(), List.of(thirdPage.get(1)));
		assertEquals(List.of("/golf - golf"), reader.lines());
	}

	private void show(Messenger<TestSender> messenger, List<Message> lines) {
		lines.forEach(line -> messenger.send(reader, line));
	}

	private static Map<String, Integer> numbers(Message line) {
		return Map.of(
				"current", ((Value.Number) line.getValues().get("current")).value().intValue(),
				"max", ((Value.Number) line.getValues().get("max")).value().intValue()
		);
	}

	private HelpBuilder<TestSender> help(Map<String, String> argumentNames, DefaultPaginationBuilder pagination, int itemsPerPage) {
		return new DefaultHelpBuilder<>(MESSAGES.help(), "Arbor", argumentNames, pagination, itemsPerPage, false, false);
	}

	private Command<TestSender> command(String alias, String definitionId) {
		return commandManager.commandBuilder(alias)
				.meta(CommandantKeys.DEFINITION_ID, definitionId)
				.commandDescription(CommandDescription.commandDescription(definitionId))
				.handler(context -> {})
				.build();
	}
}
