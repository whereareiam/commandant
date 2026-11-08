package me.whereareiam.commandant.builder;

import org.jetbrains.annotations.NotNull;

/**
 * A named argument of a {@link TranslationKey}.
 */
public interface TranslationArgument {
	/**
	 * Returns the name a translation refers to the argument by.
	 *
	 * @return the name, such as {@code host} in {@code {host}}
	 */
	@NotNull
	String getId();
}
