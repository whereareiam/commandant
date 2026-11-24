package me.whereareiam.commandant.builder;

import me.whereareiam.semantica.Message;
import org.incendo.cloud.Command;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Builds a page of help: a heading, a line for each command with its arguments and description, and a page line. The
 * page is made of Commandant's translated messages ({@code commandant.help.*} and {@code commandant.pagination.*}),
 * so Keystone shows it in each reader's language.
 *
 * <pre>{@code
 * Collection<Command<S>> visible = manager.commands().stream()
 *         .filter(command -> manager.testPermission(sender, command.commandPermission()).allowed())
 *         .toList();
 * help.build(visible, page).forEach(line -> messenger.send(sender, line));
 * }</pre>
 *
 * @param <S> the sender type
 */
public interface HelpBuilder<S> {
	/**
	 * Builds one page of help for the given commands. A page past the last lists no command and shows the empty line.
	 *
	 * @param commands the commands to list, usually those the reader may use
	 * @param page     the page, from {@code 1}
	 * @return the page's lines, in order: the heading, the commands or the empty line, and the page line when shown
	 */
	@NotNull
	List<Message> build(@NotNull Collection<Command<S>> commands, int page);
}
