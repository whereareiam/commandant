package me.whereareiam.commandant.builder;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * A text a builder sends as a translatable component, with the named arguments it is given. A plugin writes a
 * translation for each under the builder's prefix.
 */
public interface TranslationKey {
	/**
	 * Returns the key under the builder's prefix.
	 *
	 * @return the key, such as {@code title}
	 */
	@NotNull
	String getKey();

	/**
	 * Returns the named arguments the text is given.
	 *
	 * @return the arguments
	 */
	@NotNull
	Set<? extends TranslationArgument> getArguments();

	/**
	 * Returns the full key of this text under a prefix.
	 *
	 * @param prefix the builder's prefix, such as {@code arbor.help}
	 * @return the key, such as {@code arbor.help.title}
	 */
	@NotNull
	default String under(@NotNull String prefix) {
		return prefix + '.' + getKey();
	}
}
