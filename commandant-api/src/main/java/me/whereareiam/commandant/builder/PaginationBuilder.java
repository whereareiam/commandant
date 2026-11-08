package me.whereareiam.commandant.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * Builds the page line of a paginated list: the current and last page, and arrows to the pages around it. The
 * line is a translatable component with named arguments, whose keys are the pagination's prefix, a dot and the key
 * of a {@link Text}, such as {@code arbor.help.pagination.line}.
 */
public interface PaginationBuilder {
	/**
	 * Builds the page line for a list.
	 *
	 * @param totalItems   the number of items in the whole list
	 * @param currentPage  the page shown, from {@code 1}
	 * @param itemsPerPage the number of items on one page
	 * @return the page line, or empty when the list fits on one page and the pagination hides it then
	 */
	@NotNull
	Optional<Component> build(int totalItems, int currentPage, int itemsPerPage);

	/**
	 * A text of the page line, with the named arguments it is given. A plugin writes a translation for each under
	 * the pagination's prefix.
	 */
	@Getter
	enum Text implements TranslationKey {
		/**
		 * The page line. Its {@link Argument#PREVIOUS} and {@link Argument#NEXT} are the arrows, or empty where
		 * there is no page to go to.
		 */
		LINE("line", Argument.CURRENT, Argument.MAX, Argument.PREVIOUS, Argument.NEXT),

		/**
		 * The arrow to the previous page. It needs no click tag: when the pagination has a page command, the whole
		 * arrow runs it.
		 */
		PREVIOUS("previous", Argument.PAGE),

		/**
		 * The arrow to the next page. It needs no click tag: when the pagination has a page command, the whole
		 * arrow runs it.
		 */
		NEXT("next", Argument.PAGE);

		private final String key;
		private final Set<Argument> arguments;

		Text(@NotNull String key, @NotNull Argument... arguments) {
			this.key = key;
			this.arguments = Set.of(arguments);
		}
	}

	/**
	 * A named argument of a {@link Text}.
	 */
	@Getter
	@RequiredArgsConstructor
	enum Argument implements TranslationArgument {
		/**
		 * The page shown, a number.
		 */
		CURRENT("current"),

		/**
		 * The last page, a number.
		 */
		MAX("max"),

		/**
		 * The arrow to the previous page, or empty.
		 */
		PREVIOUS("previous"),

		/**
		 * The arrow to the next page, or empty.
		 */
		NEXT("next"),

		/**
		 * The page an arrow opens, a number.
		 */
		PAGE("page");

		private final String id;
	}
}
