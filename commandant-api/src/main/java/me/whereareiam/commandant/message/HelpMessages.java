package me.whereareiam.commandant.message;

import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.Value;
import me.whereareiam.semantica.declaration.Default;

/**
 * The help, {@code commandant.help.*}: a heading, a line for each command and the page line of
 * {@link PaginationMessages}.
 */
public interface HelpMessages {
	/**
	 * The heading above the commands.
	 *
	 * @param host the name of the plugin whose help it is
	 * @return the message
	 */
	@Default("<gold><bold>{host}</bold> <gray>commands")
	Message title(String host);

	/**
	 * One command's line.
	 *
	 * @param command     the root command's name
	 * @param arguments   the command's arguments, each after a space and each as {@link #argument} or
	 *                    {@link #optionalArgument} says; empty for none
	 * @param description the command's description: a translated message or the text the command was given
	 * @return the message
	 */
	@Default("<yellow>/{command}<gray>{arguments}</gray> <dark_gray>-</dark_gray> <white>{description}")
	Message command(String command, Value arguments, Value description);

	/**
	 * A required argument inside a command's arguments.
	 *
	 * @param name the argument's name
	 * @return the message
	 */
	@Default("<{name}>")
	Message argument(String name);

	/**
	 * An optional argument inside a command's arguments.
	 *
	 * @param name the argument's name
	 * @return the message
	 */
	@Default("[{name}]")
	Message optionalArgument(String name);

	/**
	 * The line shown instead of commands when a page has none.
	 *
	 * @return the message
	 */
	@Default("<gray>There are no commands on this page.")
	Message empty();
}
