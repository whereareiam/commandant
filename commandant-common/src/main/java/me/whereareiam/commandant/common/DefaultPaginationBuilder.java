package me.whereareiam.commandant.common;

import me.whereareiam.commandant.builder.PaginationBuilder;
import me.whereareiam.commandant.message.PaginationMessages;
import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;

/**
 * The standard pagination: Commandant's page line, with its arrows as values. An arrow runs the page command, when
 * there is one, for the page it opens.
 */
public class DefaultPaginationBuilder implements PaginationBuilder {
	private static final Value NONE = Value.joined(List.of());

	private final PaginationMessages messages;
	private final IntFunction<String> pageCommand;
	private final boolean showIfOnePage;
	private final boolean showPreviousOnFirst;
	private final boolean showNextOnLast;

	/**
	 * Creates a pagination.
	 *
	 * @param messages            the page line's texts
	 * @param pageCommand         the command that opens a page, such as {@code /arbor help 2}, or {@code null} for
	 *                            arrows that run nothing
	 * @param showIfOnePage       whether the line is shown when the list fits on one page
	 * @param showPreviousOnFirst whether the previous arrow is shown on the first page, opening it again
	 * @param showNextOnLast      whether the next arrow is shown on the last page, opening it again
	 */
	public DefaultPaginationBuilder(
			@NotNull PaginationMessages messages,
			@Nullable IntFunction<String> pageCommand,
			boolean showIfOnePage,
			boolean showPreviousOnFirst,
			boolean showNextOnLast
	) {
		this.messages = messages;
		this.pageCommand = pageCommand;
		this.showIfOnePage = showIfOnePage;
		this.showPreviousOnFirst = showPreviousOnFirst;
		this.showNextOnLast = showNextOnLast;
	}

	@Override
	@NotNull
	public Optional<Message> build(int totalItems, int currentPage, int itemsPerPage) {
		int perPage = Math.max(1, itemsPerPage);
		int totalPages = Math.max(1, (totalItems + perPage - 1) / perPage);
		if (totalPages == 1 && !showIfOnePage) return Optional.empty();

		return Optional.of(messages.line(currentPage, totalPages, previous(currentPage, totalPages), next(currentPage, totalPages)));
	}

	@NotNull
	private Value previous(int currentPage, int totalPages) {
		if (currentPage > 1) return arrow(messages::previous, Math.min(currentPage - 1, totalPages));

		return showPreviousOnFirst ? arrow(messages::previous, 1) : NONE;
	}

	@NotNull
	private Value next(int currentPage, int totalPages) {
		if (currentPage < totalPages) return arrow(messages::next, currentPage + 1);

		return showNextOnLast ? arrow(messages::next, totalPages) : NONE;
	}

	@NotNull
	private Value arrow(@NotNull IntFunction<Message> text, int page) {
		Value shown = Value.nested(text.apply(page));
		if (pageCommand == null) return shown;

		return Value.linked(shown, Value.Link.runCommand(pageCommand.apply(page)));
	}
}
