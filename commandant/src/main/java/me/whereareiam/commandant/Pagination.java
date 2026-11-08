package me.whereareiam.commandant;

import me.whereareiam.commandant.builder.ArgumentFactory;
import me.whereareiam.commandant.builder.PaginationBuilder;
import me.whereareiam.commandant.common.DefaultPaginationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

/**
 * Creates a {@link PaginationBuilder}, for a paginated list of a plugin's own. The help makes its own
 * pagination; see {@link Help}. The plugin writes a translation of each {@link PaginationBuilder.Text} under the
 * prefix.
 *
 * <pre>{@code
 * PaginationBuilder pagination = Pagination.builder("arbor.module.list.pagination", TextArgument::of)
 *         .pageCommand(page -> "/arbor module list " + page)
 *         .build();
 *
 * pagination.build(modules.size(), page, 10).ifPresent(sender::sendMessage);
 * }</pre>
 */
@SuppressWarnings("unused")
public final class Pagination {
	/**
	 * Starts a pagination.
	 *
	 * @param prefix    the keys' prefix, such as {@code arbor.module.list.pagination}
	 * @param arguments makes the named arguments of the texts, such as semantica's {@code TextArgument::of}
	 * @return the builder
	 */
	@NotNull
	public static Builder builder(@NotNull String prefix, @NotNull ArgumentFactory arguments) {
		return new Builder(prefix, arguments);
	}

	/**
	 * Builder of a {@link PaginationBuilder}. Without further settings the page line is hidden when the list
	 * fits on one page, arrows are shown only where there is a page to go to, and they run no command.
	 */
	public static final class Builder {
		private final String prefix;
		private final ArgumentFactory arguments;
		private IntFunction<String> pageCommand;
		private boolean showIfOnePage;
		private boolean showPreviousOnFirst;
		private boolean showNextOnLast;

		private Builder(@NotNull String prefix, @NotNull ArgumentFactory arguments) {
			this.prefix = prefix;
			this.arguments = arguments;
		}

		/**
		 * Sets the command an arrow runs.
		 *
		 * @param pageCommand gives the command that opens a page, with its slash, such as
		 *                    {@code page -> "/arbor help " + page}; {@code null} for arrows that run nothing
		 * @return this builder
		 */
		@NotNull
		public Builder pageCommand(@Nullable IntFunction<String> pageCommand) {
			this.pageCommand = pageCommand;
			return this;
		}

		/**
		 * Sets whether the page line is shown when the list fits on one page.
		 *
		 * @param showIfOnePage {@code true} to show it always
		 * @return this builder
		 */
		@NotNull
		public Builder showIfOnePage(boolean showIfOnePage) {
			this.showIfOnePage = showIfOnePage;
			return this;
		}

		/**
		 * Sets whether the previous arrow is shown on the first page, where it opens the first page again.
		 *
		 * @param showPreviousOnFirst {@code true} to show it
		 * @return this builder
		 */
		@NotNull
		public Builder showPreviousOnFirst(boolean showPreviousOnFirst) {
			this.showPreviousOnFirst = showPreviousOnFirst;
			return this;
		}

		/**
		 * Sets whether the next arrow is shown on the last page, where it opens the last page again.
		 *
		 * @param showNextOnLast {@code true} to show it
		 * @return this builder
		 */
		@NotNull
		public Builder showNextOnLast(boolean showNextOnLast) {
			this.showNextOnLast = showNextOnLast;
			return this;
		}

		/**
		 * Builds the pagination.
		 *
		 * @return the pagination
		 */
		@NotNull
		public PaginationBuilder build() {
			return new DefaultPaginationBuilder(
					prefix,
					arguments,
					pageCommand,
					showIfOnePage,
					showPreviousOnFirst,
					showNextOnLast
			);
		}
	}
}
