package me.whereareiam.commandant;

import me.whereareiam.commandant.annotation.Definition;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.parser.ParserRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Reads a plugin's commands from Cloud's annotations ({@code @Command}, {@code @Argument}, ...) together with
 * Commandant's {@link Definition @Definition}, without registering them, so that each is registered once its
 * definition is applied:
 * <pre>{@code
 * AnnotatedCommands<CommandSender> annotated = AnnotatedCommands.of(manager, CommandSender.class);
 * for (Command<CommandSender> command : annotated.parse(new ReloadCommand(), new HelpCommand())) {
 *     String id = command.commandMeta().get(CommandantKeys.DEFINITION_ID);
 *     Commandant.process(command, manager).withDefinition(definitions.get(id), adapter).register();
 * }
 * }</pre>
 * Cloud's parser registers what it parses with its manager; this one parses with a manager of its own that keeps
 * nothing and shares the plugin manager's parsers and suggestions, so a command reaches the plugin's manager only
 * through {@link Commandant#process}. A command's definition id is the value of {@code @Definition} on its method, or on
 * its class where the method has none.
 *
 * @param <S> the sender type
 */
public final class AnnotatedCommands<S> {
	private final AnnotationParser<S> parser;

	private AnnotatedCommands(@NotNull AnnotationParser<S> parser) {
		this.parser = parser;
	}

	/**
	 * Starts reading commands for a command manager.
	 *
	 * @param commandManager the manager the commands will be registered with, whose parsers and suggestions they use
	 * @param senderType     the sender type
	 * @param <S>            the sender type
	 * @return the reader
	 */
	public static <S> @NotNull AnnotatedCommands<S> of(@NotNull CommandManager<S> commandManager, @NotNull Class<S> senderType) {
		AnnotationParser<S> parser = new AnnotationParser<>(new Parsing<>(Objects.requireNonNull(commandManager, "commandManager")), senderType);
		parser.registerBuilderModifier(Definition.class, (definition, builder) -> builder.meta(CommandantKeys.DEFINITION_ID, definition.value()));

		return new AnnotatedCommands<>(parser);
	}

	/**
	 * Returns Cloud's parser underneath, for what a plugin adds to it, such as annotation mappers or string processors.
	 *
	 * @return the parser
	 */
	public @NotNull AnnotationParser<S> parser() {
		return parser;
	}

	/**
	 * Reads the commands of annotated instances.
	 *
	 * @param containers the instances whose {@code @Command} methods are commands
	 * @return the commands, none of them registered
	 */
	public @NotNull List<Command<S>> parse(@NotNull Object @NotNull ... containers) {
		return List.copyOf(parser.parse(containers));
	}

	/** A manager that only lends the plugin manager's parsers: it registers nothing and keeps no command tree. */
	private static final class Parsing<S> extends CommandManager<S> {
		private final CommandManager<S> commandManager;

		private Parsing(@NotNull CommandManager<S> commandManager) {
			super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
			this.commandManager = commandManager;
		}

		@Override
		public @NotNull CommandManager<S> command(@NotNull Command<? extends S> command) {
			return this;
		}

		@Override
		public boolean hasPermission(@NotNull S sender, @NotNull String permission) {
			return commandManager.hasPermission(sender, permission);
		}

		@Override
		public @NotNull ParserRegistry<S> parserRegistry() {
			return commandManager.parserRegistry();
		}
	}
}
