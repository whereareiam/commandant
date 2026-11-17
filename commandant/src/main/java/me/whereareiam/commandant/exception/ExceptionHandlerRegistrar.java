package me.whereareiam.commandant.exception;

import me.whereareiam.commandant.Commandant;
import me.whereareiam.commandant.exception.format.ExceptionFormatting;
import me.whereareiam.commandant.message.ExceptionMessages;
import me.whereareiam.keystone.Messenger;
import me.whereareiam.semantica.Message;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.exception.handling.ExceptionContext;
import org.incendo.cloud.exception.handling.ExceptionHandler;
import org.incendo.cloud.exception.parsing.NumberParseException;
import org.incendo.cloud.parser.standard.BooleanParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * Tells a sender why their command failed, in their language, through the plugin's Keystone messenger: a missing
 * permission, a wrong syntax, an argument that could not be read, the wrong kind of sender, or a failing handler. The
 * texts are Commandant's {@code commandant.exception.*} (see {@link ExceptionMessages}), which the plugin may word
 * anew in its own files.
 * <pre>{@code
 * ExceptionHandlerRegistrar.register(commandManager, messenger);
 * }</pre>
 */
@SuppressWarnings("unused")
public final class ExceptionHandlerRegistrar {
	private ExceptionHandlerRegistrar() {
	}

	/**
	 * Registers the handlers, writing a missing permission as Cloud does.
	 *
	 * @param commandManager the command manager whose failures are told
	 * @param messenger      the plugin's messenger over the same senders
	 * @param <S>            the sender type
	 */
	public static <S> void register(@NotNull CommandManager<S> commandManager, @NotNull Messenger<S> messenger) {
		register(commandManager, messenger, ExceptionFormatting.defaults());
	}

	/**
	 * Registers the handlers, writing a missing permission as the formatting says.
	 *
	 * @param commandManager the command manager whose failures are told
	 * @param messenger      the plugin's messenger over the same senders
	 * @param formatting     how values in the texts are written, such as a permission
	 * @param <S>            the sender type
	 */
	public static <S> void register(
			@NotNull CommandManager<S> commandManager,
			@NotNull Messenger<S> messenger,
			@NotNull ExceptionFormatting formatting
	) {
		Objects.requireNonNull(messenger, "messenger");
		Objects.requireNonNull(formatting, "formatting");
		ExceptionMessages messages = Commandant.messages().exception();

		commandManager.exceptionController()
				.registerHandler(ArgumentParseException.class, tell(messenger, exception -> parse(messages, exception.getCause())))
				.registerHandler(InvalidCommandSenderException.class, tell(messenger, exception -> messages.invalidSender()))
				.registerHandler(NoPermissionException.class, tell(messenger, exception ->
						messages.noPermission(permission(exception.missingPermission(), formatting))))
				.registerHandler(InvalidSyntaxException.class, tell(messenger, exception ->
						messages.invalidSyntax(exception.correctSyntax().replace("|", "/"))))
				.registerHandler(CommandExecutionException.class, tell(messenger, exception ->
						messages.executionError(String.valueOf(exception.getMessage()))));
	}

	private static <S, T extends Throwable> ExceptionHandler<S, T> tell(
			@NotNull Messenger<S> messenger,
			@NotNull Function<T, Message> message
	) {
		return (ExceptionContext<S, T> context) -> messenger.send(context.context().sender(), message.apply(context.exception()));
	}

	@NotNull
	private static Message parse(@NotNull ExceptionMessages messages, @NotNull Throwable cause) {
		return switch (cause) {
			case BooleanParser.BooleanParseException exception -> messages.invalidBoolean(exception.input());
			case NumberParseException exception -> messages.invalidNumber(exception.input());
			case StringParser.StringParseException exception -> messages.invalidString(exception.input());
			default -> messages.invalidArgument(String.valueOf(cause.getMessage()));
		};
	}

	@NotNull
	private static String permission(@NotNull Permission permission, @NotNull ExceptionFormatting formatting) {
		String formatted = formatting.format(permission);
		if (formatted != null) return formatted;

		return permission.permissionString();
	}
}
