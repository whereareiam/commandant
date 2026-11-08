package me.whereareiam.commandant.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A command description that is a translation key, so the help shows it in each reader's language. The help
 * renders it as a translatable component of its key; Cloud's text consumers, such as a platform's command
 * tree, see the key.
 *
 * <pre>{@code
 * manager.commandBuilder("arbor")
 *         .literal("status")
 *         .commandDescription(TranslatableDescription.of("arbor.command.status.description"))
 *         .handler(...);
 * }</pre>
 *
 * <p>The help renders any {@link Description} that is also a {@link ComponentLike} as that component; this
 * class is the one Commandant provides.</p>
 */
@Getter
@ToString
@EqualsAndHashCode
public final class TranslatableDescription implements Description, ComponentLike {
	private final String key;

	private TranslatableDescription(@NotNull String key) {
		this.key = Objects.requireNonNull(key, "key");
	}

	/**
	 * Creates a description of a translation key.
	 *
	 * @param key the key, such as {@code arbor.command.status.description}
	 * @return the description
	 */
	@NotNull
	public static TranslatableDescription of(@NotNull String key) {
		return new TranslatableDescription(key);
	}

	/**
	 * Returns the key, for consumers that only read text.
	 *
	 * @return the key
	 */
	@Override
	@NotNull
	public String textDescription() {
		return key;
	}

	/**
	 * A translatable description is never empty.
	 *
	 * @return {@code false}
	 */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/**
	 * Returns the translatable component of the key, which a translator renders for its reader.
	 *
	 * @return the component
	 */
	@Override
	@NotNull
	public Component asComponent() {
		return Component.translatable(key);
	}
}
