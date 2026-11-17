package me.whereareiam.commandant.builder;

import me.whereareiam.semantica.Message;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Builds the page line of a paginated list: the current and last page, and arrows to the pages around it, which run
 * the page command when clicked. The line is Commandant's translated message {@code commandant.pagination.line}.
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
	Optional<Message> build(int totalItems, int currentPage, int itemsPerPage);
}
