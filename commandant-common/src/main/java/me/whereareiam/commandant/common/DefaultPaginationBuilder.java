package me.whereareiam.commandant.common;

import me.whereareiam.commandant.builder.ArgumentFactory;
import me.whereareiam.commandant.builder.PaginationBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.IntFunction;

/**
 * The standard pagination: the {@link Text#LINE} text under a prefix, with the {@link Text#PREVIOUS} and
 * {@link Text#NEXT} arrows as its arguments. An arrow runs the page command, when there is one, for the page it
 * opens.
 */
public class DefaultPaginationBuilder implements PaginationBuilder {
	private final String prefix;
	private final ArgumentFactory arguments;
	private final IntFunction<String> pageCommand;
	private final boolean showIfOnePage;
	private final boolean showPreviousOnFirst;
	private final boolean showNextOnLast;

	/**
	 * Creates a pagination.
	 *
	 * @param prefix              the keys' prefix, such as {@code arbor.help.pagination}
	 * @param arguments           makes the named arguments of the texts
	 * @param pageCommand         the command that opens a page, such as {@code /arbor help 2}, or {@code null}
	 *                            for arrows that run nothing
	 * @param showIfOnePage       whether the line is shown when the list fits on one page
	 * @param showPreviousOnFirst whether the previous arrow is shown on the first page, opening it again
	 * @param showNextOnLast      whether the next arrow is shown on the last page, opening it again
	 */
	public DefaultPaginationBuilder(
			@NotNull String prefix,
			@NotNull ArgumentFactory arguments,
			@Nullable IntFunction<String> pageCommand,
			boolean showIfOnePage,
			boolean showPreviousOnFirst,
			boolean showNextOnLast
	) {
		this.prefix = prefix;
		this.arguments = arguments;
		this.pageCommand = pageCommand;
		this.showIfOnePage = showIfOnePage;
		this.showPreviousOnFirst = showPreviousOnFirst;
		this.showNextOnLast = showNextOnLast;
	}

	@Override
	@NotNull
	public Optional<Component> build(int totalItems, int currentPage, int itemsPerPage) {
		int perPage = Math.max(1, itemsPerPage);
		int totalPages = Math.max(1, (totalItems + perPage - 1) / perPage);
		if (totalPages == 1 && !showIfOnePage) return Optional.empty();

		return Optional.of(Component.translatable(
				Text.LINE.under(prefix),
				arguments.create(Argument.CURRENT, currentPage),
				arguments.create(Argument.MAX, totalPages),
				arguments.create(Argument.PREVIOUS, previous(currentPage, totalPages)),
				arguments.create(Argument.NEXT, next(currentPage, totalPages))
		));
	}

	@NotNull
	private Component previous(int currentPage, int totalPages) {
		if (currentPage > 1) return arrow(Text.PREVIOUS, Math.min(currentPage - 1, totalPages));
		if (showPreviousOnFirst) return arrow(Text.PREVIOUS, 1);

		return Component.empty();
	}

	@NotNull
	private Component next(int currentPage, int totalPages) {
		if (currentPage < totalPages) return arrow(Text.NEXT, currentPage + 1);
		if (showNextOnLast) return arrow(Text.NEXT, totalPages);

		return Component.empty();
	}

	@NotNull
	private Component arrow(@NotNull Text text, int page) {
		Component arrow = Component.translatable(text.under(prefix), arguments.create(Argument.PAGE, page));
		if (pageCommand == null) return arrow;

		return arrow.clickEvent(ClickEvent.runCommand(pageCommand.apply(page)));
	}
}
