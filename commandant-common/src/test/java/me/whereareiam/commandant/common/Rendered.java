package me.whereareiam.commandant.common;

import me.whereareiam.commandant.builder.ArgumentFactory;
import me.whereareiam.commandant.builder.TranslationArgument;
import me.whereareiam.commandant.builder.TranslationKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Writes out components as their keys and named arguments, such as {@code a.title(host=Arbor)}, so a test reads
 * what a translator is asked for.
 */
final class Rendered {
	/**
	 * Names an argument as {@code name=value}, as a translator's named argument would.
	 */
	static final ArgumentFactory ARGUMENTS = (name, value) -> Component.text(name + "=").append(value(value));

	/**
	 * Collects the translatable components in a tree by key, with the names of the arguments each was given.
	 */
	static Map<String, Set<String>> arguments(Component component) {
		Map<String, Set<String>> arguments = new HashMap<>();
		collect(component, arguments);

		return arguments;
	}

	/**
	 * Lists the texts by their full key under a prefix, with the names of the arguments each declares.
	 */
	static Map<String, Set<String>> declared(String prefix, TranslationKey... texts) {
		return Arrays.stream(texts).collect(Collectors.toMap(
				text -> text.under(prefix),
				text -> text.getArguments().stream().map(TranslationArgument::getId).collect(Collectors.toSet())
		));
	}

	private static void collect(Component component, Map<String, Set<String>> arguments) {
		if (component instanceof TranslatableComponent translatable) {
			arguments.put(translatable.key(), translatable.arguments().stream()
					.map(argument -> ((TextComponent) argument.asComponent()).content().replace("=", ""))
					.collect(Collectors.toSet()));
			translatable.arguments().forEach(argument -> collect(argument.asComponent(), arguments));
		}

		component.children().forEach(child -> collect(child, arguments));
	}

	private static Component value(Object value) {
		if (value instanceof ComponentLike component) return component.asComponent();

		return Component.text(String.valueOf(value));
	}

	static String of(Component component) {
		StringBuilder text = new StringBuilder();
		if (component instanceof TranslatableComponent translatable) {
			text.append(translatable.key()).append('(')
					.append(translatable.arguments().stream().map(argument -> of(argument.asComponent())).collect(Collectors.joining(", ")))
					.append(')');
		} else if (component instanceof TextComponent plain) {
			text.append(plain.content());
		}

		ClickEvent click = component.clickEvent();
		if (click != null) text.append("[click ").append(click.value()).append(']');

		component.children().forEach(child -> text.append(of(child)));

		return text.toString();
	}
}
