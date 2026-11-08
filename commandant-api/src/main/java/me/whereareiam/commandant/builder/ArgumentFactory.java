package me.whereareiam.commandant.builder;

import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.NotNull;

/**
 * Makes a named argument of a translatable component, so a translation refers to it by name, such as
 * {@code {host}}. The plugin's translator decides how a named argument is represented; with semantica it is
 * {@code TextArgument::of}.
 *
 * <p>The help and the pagination pass a value as a {@link ComponentLike}, a {@link String} or an {@link Integer},
 * so a translation can format numbers, for example in a plural.</p>
 *
 * <pre>{@code
 * HelpBuilder<S> help = Help.<S>builder("arbor.help", TextArgument::of)
 *         .host("Arbor")
 *         .build();
 * }</pre>
 */
@FunctionalInterface
public interface ArgumentFactory {
	/**
	 * Makes a named argument.
	 *
	 * @param name  the argument's name, as a translation refers to it
	 * @param value the argument's value: a {@link ComponentLike}, a {@link String} or an {@link Integer}
	 * @return the argument, to pass to a translatable component
	 */
	@NotNull
	ComponentLike create(@NotNull String name, @NotNull Object value);

	/**
	 * Makes a named argument of a number.
	 *
	 * @param argument the argument
	 * @param value    its value
	 * @return the argument, to pass to a translatable component
	 */
	@NotNull
	default ComponentLike create(@NotNull TranslationArgument argument, int value) {
		return create(argument.getId(), value);
	}

	/**
	 * Makes a named argument of a text.
	 *
	 * @param argument the argument
	 * @param value    its value
	 * @return the argument, to pass to a translatable component
	 */
	@NotNull
	default ComponentLike create(@NotNull TranslationArgument argument, @NotNull String value) {
		return create(argument.getId(), value);
	}

	/**
	 * Makes a named argument of a component.
	 *
	 * @param argument the argument
	 * @param value    its value
	 * @return the argument, to pass to a translatable component
	 */
	@NotNull
	default ComponentLike create(@NotNull TranslationArgument argument, @NotNull ComponentLike value) {
		return create(argument.getId(), value);
	}
}
