package me.whereareiam.commandant.message;

import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.declaration.Default;

/**
 * What a sender is told when a command fails, {@code commandant.exception.*}.
 */
public interface ExceptionMessages {
	/**
	 * The sender lacks the command's permission.
	 *
	 * @param permission the permission, as the exception formatting writes it
	 * @return the message
	 */
	@Default("<white>You don't have \"<gray>{permission}</gray>\" permission to use this command.")
	Message noPermission(String permission);

	/**
	 * The command's handler failed.
	 *
	 * @param error what went wrong
	 * @return the message
	 */
	@Default("<white>An error occurred while executing the command:</white> <gray>{error}")
	Message executionError(String error);

	/**
	 * The command was written wrongly.
	 *
	 * @param syntax the command as it should be written, as Cloud writes it, such as {@code warp <name> [page]}
	 * @return the message
	 */
	@Default("<white>Invalid syntax, please use:</white> <gray>/{syntax}")
	Message invalidSyntax(String syntax);

	/**
	 * An argument is no boolean.
	 *
	 * @param input what the sender wrote
	 * @return the message
	 */
	@Default("<white>You tried to use <gray>{input}</gray> as a boolean, but it's not a valid value, please use <green>true</green> or <red>false</red>.")
	Message invalidBoolean(String input);

	/**
	 * An argument is no number, or a number out of range.
	 *
	 * @param input what the sender wrote
	 * @return the message
	 */
	@Default("<white>You tried to use <gray>{input}</gray> as a number, but it's not a valid value, please use a valid number.")
	Message invalidNumber(String input);

	/**
	 * An argument is no valid text.
	 *
	 * @param input what the sender wrote
	 * @return the message
	 */
	@Default("<white>You tried to use <gray>{input}</gray> as a string, but it's not a valid value, please use a valid string.")
	Message invalidString(String input);

	/**
	 * Another argument could not be read.
	 *
	 * @param error what the argument's parser says
	 * @return the message
	 */
	@Default("<white>An argument is not valid:</white> <gray>{error}")
	Message invalidArgument(String error);

	/**
	 * The command is not for this kind of sender, such as a player's command run from the console.
	 *
	 * @return the message
	 */
	@Default("<white>You cannot use this command.")
	Message invalidSender();

	/**
	 * The sender ran the command again before its cooldown was over.
	 *
	 * @param seconds the seconds left, rounded up
	 * @return the message
	 */
	@Default("<white>You must wait <gray>{seconds, plural, one {# second} other {# seconds}}</gray> before using this command again.")
	Message cooldown(long seconds);
}
