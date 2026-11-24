package me.whereareiam.commandant.message;

import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.Value;
import me.whereareiam.semantica.declaration.Default;

/**
 * The page line of a paginated list, such as the help, {@code commandant.pagination.*}.
 */
public interface PaginationMessages {
	/**
	 * The page line.
	 *
	 * @param current  the page shown
	 * @param max      the last page
	 * @param previous the arrow to the previous page, {@link #previous}, or empty where there is none
	 * @param next     the arrow to the next page, {@link #next}, or empty where there is none
	 * @return the message
	 */
	@Default("<gray>Page <white>{current}</white> of <white>{max}</white> {previous} {next}")
	Message line(int current, int max, Value previous, Value next);

	/**
	 * The arrow to the previous page. It needs no click tag: when the pagination has a page command, the whole arrow
	 * runs it.
	 *
	 * @param page the page it opens
	 * @return the message
	 */
	@Default("<gold>« Page {page}")
	Message previous(int page);

	/**
	 * The arrow to the next page. It needs no click tag: when the pagination has a page command, the whole arrow runs
	 * it.
	 *
	 * @param page the page it opens
	 * @return the message
	 */
	@Default("<gold>Page {page} »")
	Message next(int page);
}
