package me.whereareiam.commandant.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

/**
 * Builds a page of help: a heading, a line for each command with its arguments and description, and a page
 * line. The page is made of translatable components with named arguments, so the plugin's translator renders it
 * in each reader's language. Each component's key is the help's prefix, a dot and the key of a {@link Text}, such
 * as {@code arbor.help.title}; the page line's keys are those of {@link PaginationBuilder.Text} under
 * {@code <prefix>.}{@value #PAGINATION}.
 *
 * <pre>{@code
 * Collection<Command<S>> visible = manager.commands().stream()
 *         .filter(command -> manager.testPermission(sender, command.commandPermission()).allowed())
 *         .toList();
 * sender.sendMessage(help.build(visible, page));
 * }</pre>
 *
 * @param <S> the sender type
 */
public interface HelpBuilder<S> {
	/**
	 * The key under the help's prefix that holds the page line's texts, those of {@link PaginationBuilder.Text}.
	 */
	String PAGINATION = "pagination";

	/**
	 * Builds one page of help for the given commands. A page past the last lists no command and shows the
	 * {@link Text#EMPTY} line.
	 *
	 * @param commands the commands to list, usually those the reader may use
	 * @param page     the page, from {@code 1}
	 * @return the page, its lines joined by line breaks
	 */
	@NotNull
	Component build(@NotNull Collection<Command<S>> commands, int page);

	/**
	 * A text of the help, with the named arguments it is given. A plugin writes a translation for each under the
	 * help's prefix.
	 */
	@Getter
	enum Text implements TranslationKey {
		/**
		 * The heading above the commands.
		 */
		TITLE("title", Argument.HOST),

		/**
		 * One command's line. Its {@link Argument#ARGUMENTS} is empty or starts with a space.
		 */
		COMMAND("command", Argument.COMMAND, Argument.ARGUMENTS, Argument.DESCRIPTION),

		/**
		 * A required argument inside a command's {@link Argument#ARGUMENTS}.
		 */
		ARGUMENT("argument", Argument.NAME),

		/**
		 * An optional argument inside a command's {@link Argument#ARGUMENTS}.
		 */
		OPTIONAL_ARGUMENT("optionalArgument", Argument.NAME),

		/**
		 * The line shown instead of commands when a page has none.
		 */
		EMPTY("empty");

		private final String key;
		private final Set<Argument> arguments;

		Text(@NotNull String key, @NotNull Argument... arguments) {
			this.key = key;
			this.arguments = Set.of(arguments);
		}
	}

	/**
	 * A named argument of a {@link Text}.
	 */
	@Getter
	@RequiredArgsConstructor
	enum Argument implements TranslationArgument {
		/**
		 * The name of the plugin whose help it is.
		 */
		HOST("host"),

		/**
		 * The root command's name.
		 */
		COMMAND("command"),

		/**
		 * The command's arguments, each after a space.
		 */
		ARGUMENTS("arguments"),

		/**
		 * The command's description.
		 */
		DESCRIPTION("description"),

		/**
		 * An argument's name.
		 */
		NAME("name");

		private final String id;
	}
}
