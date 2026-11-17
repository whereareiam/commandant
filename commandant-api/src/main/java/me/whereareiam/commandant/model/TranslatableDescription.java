package me.whereareiam.commandant.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.whereareiam.semantica.Message;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A command description that is a translated message, so the help shows it in each reader's language. Cloud's text
 * consumers, such as a platform's command tree, see the message's key.
 *
 * <pre>{@code
 * manager.commandBuilder("arbor")
 *         .literal("status")
 *         .commandDescription(TranslatableDescription.of(messages.statusDescription()))
 *         .handler(...);
 * }</pre>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class TranslatableDescription implements Description {
	private final Message message;

	private TranslatableDescription(@NotNull Message message) {
		this.message = Objects.requireNonNull(message, "message");
	}

	/**
	 * Creates a description of a translated message.
	 *
	 * @param message the message, such as one of the plugin's declared messages
	 * @return the description
	 */
	@NotNull
	public static TranslatableDescription of(@NotNull Message message) {
		return new TranslatableDescription(message);
	}

	/**
	 * Returns the message's key, for consumers that only read text.
	 *
	 * @return the key
	 */
	@Override
	@NotNull
	public String textDescription() {
		return message.getKey();
	}

	/**
	 * A translated description is never empty.
	 *
	 * @return {@code false}
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}
}
