package me.whereareiam.commandant;

import me.whereareiam.commandant.builder.ArgumentFactory;
import me.whereareiam.commandant.builder.HelpBuilder;
import me.whereareiam.commandant.builder.PaginationBuilder;
import me.whereareiam.commandant.common.DefaultHelpBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Creates a {@link HelpBuilder}: a help made of translatable components, which the plugin's translator renders in
 * each reader's language. The plugin writes a translation of each {@link HelpBuilder.Text} under the prefix, and of
 * each {@link me.whereareiam.commandant.builder.PaginationBuilder.Text} under {@code <prefix>.pagination}.
 *
 * <pre>{@code
 * HelpBuilder<S> help = Help.<S>builder("arbor.help", TextArgument::of)
 *         .host("Arbor")
 *         .pageCommand(page -> "/arbor help " + page)
 *         .dedupeByDefinitionId(true)
 *         .build();
 *
 * sender.sendMessage(help.build(visibleCommands, page));
 * }</pre>
 */
@SuppressWarnings("unused")
public final class Help {
	/**
	 * Starts a help.
	 *
	 * @param prefix    the keys' prefix, such as {@code arbor.help}
	 * @param arguments makes the named arguments of the texts, such as semantica's {@code TextArgument::of}
	 * @param <S>       the sender type
	 * @return the builder
	 */
	@NotNull
	public static <S> Builder<S> builder(@NotNull String prefix, @NotNull ArgumentFactory arguments) {
		return new Builder<>(prefix, arguments);
	}

	/**
	 * Builder of a {@link HelpBuilder}. Without further settings it lists 7 commands per page in the order given,
	 * with a page line under {@code <prefix>.pagination} that is hidden when the commands fit on one page.
	 *
	 * @param <S> the sender type
	 */
	public static final class Builder<S> {
		private static final int DEFAULT_ITEMS_PER_PAGE = 7;

		private final String prefix;
		private final ArgumentFactory arguments;
		private String host;
		private Map<String, String> argumentNames = Map.of();
		private PaginationBuilder pagination;
		private IntFunction<String> pageCommand;
		private int itemsPerPage = DEFAULT_ITEMS_PER_PAGE;
		private boolean sortAlphabetically;
		private boolean dedupeByDefinitionId;

		private Builder(@NotNull String prefix, @NotNull ArgumentFactory arguments) {
			this.prefix = prefix;
			this.arguments = arguments;
		}

		/**
		 * Sets the name of the plugin whose help it is, the heading's {@code host} argument. Required.
		 *
		 * @param host the name, such as {@code Arbor}
		 * @return this builder
		 */
		@NotNull
		public Builder<S> host(@NotNull String host) {
			this.host = host;
			return this;
		}

		/**
		 * Sets names to show instead of arguments' own names.
		 *
		 * @param argumentNames the shown name by argument name
		 * @return this builder
		 */
		@NotNull
		public Builder<S> argumentNames(@NotNull Map<String, String> argumentNames) {
			this.argumentNames = argumentNames;
			return this;
		}

		/**
		 * Sets the command the page line's arrows run, for the help's own pagination.
		 *
		 * @param pageCommand gives the command that opens a page of help, with its slash, such as
		 *                    {@code page -> "/arbor help " + page}; {@code null} for arrows that run nothing
		 * @return this builder
		 */
		@NotNull
		public Builder<S> pageCommand(@Nullable IntFunction<String> pageCommand) {
			this.pageCommand = pageCommand;
			return this;
		}

		/**
		 * Replaces the help's own pagination, such as one made by {@link Pagination} with other keys or settings.
		 *
		 * @param pagination the pagination, or {@code null} for the help's own
		 * @return this builder
		 */
		@NotNull
		public Builder<S> pagination(@Nullable PaginationBuilder pagination) {
			this.pagination = pagination;
			return this;
		}

		/**
		 * Sets the number of commands on one page.
		 *
		 * @param itemsPerPage the number, at least {@code 1}
		 * @return this builder
		 */
		@NotNull
		public Builder<S> itemsPerPage(int itemsPerPage) {
			this.itemsPerPage = itemsPerPage;
			return this;
		}

		/**
		 * Sets whether commands are listed by their arguments in alphabetical order.
		 *
		 * @param sortAlphabetically {@code true} to sort them
		 * @return this builder
		 */
		@NotNull
		public Builder<S> sortAlphabetically(boolean sortAlphabetically) {
			this.sortAlphabetically = sortAlphabetically;
			return this;
		}

		/**
		 * Sets whether commands that share a {@link CommandantKeys#DEFINITION_ID}, such as a command and its
		 * aliases, are listed once.
		 *
		 * @param dedupeByDefinitionId {@code true} to list them once
		 * @return this builder
		 */
		@NotNull
		public Builder<S> dedupeByDefinitionId(boolean dedupeByDefinitionId) {
			this.dedupeByDefinitionId = dedupeByDefinitionId;
			return this;
		}

		/**
		 * Builds the help.
		 *
		 * @return the help
		 * @throws NullPointerException     when no host was set
		 * @throws IllegalArgumentException when fewer than one command fits on a page
		 */
		@NotNull
		public HelpBuilder<S> build() {
			Objects.requireNonNull(host, "host");

			return new DefaultHelpBuilder<>(
					prefix,
					arguments,
					host,
					argumentNames,
					paginationOrDefault(),
					itemsPerPage,
					sortAlphabetically,
					dedupeByDefinitionId
			);
		}

		@NotNull
		private PaginationBuilder paginationOrDefault() {
			if (pagination != null) return pagination;

			return Pagination.builder(prefix + '.' + HelpBuilder.PAGINATION, arguments)
					.pageCommand(pageCommand)
					.build();
		}
	}
}
