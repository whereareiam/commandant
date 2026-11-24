package me.whereareiam.commandant.common;

import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.builder.HelpBuilder;
import me.whereareiam.commandant.builder.PaginationBuilder;
import me.whereareiam.commandant.message.HelpMessages;
import me.whereareiam.commandant.model.TranslatableDescription;
import me.whereareiam.semantica.Message;
import me.whereareiam.semantica.Value;
import org.incendo.cloud.Command;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The standard help: Commandant's help texts, a command's arguments each its own text and its description a translated
 * message when it is a {@link TranslatableDescription}, and the text it was given otherwise.
 *
 * @param <S> the sender type
 */
public class DefaultHelpBuilder<S> implements HelpBuilder<S> {
	private static final Value SPACE = Value.text(" ");

	private final HelpMessages messages;
	private final String host;
	private final Map<String, String> argumentNames;
	private final PaginationBuilder pagination;
	private final int itemsPerPage;
	private final boolean sortAlphabetically;
	private final boolean dedupeByDefinitionId;

	/**
	 * Creates a help.
	 *
	 * @param messages             the help's texts
	 * @param host                 the name of the plugin whose help it is, the heading's {@code host}
	 * @param argumentNames        names to show instead of arguments' own names
	 * @param pagination           builds the page line, or {@code null} for none
	 * @param itemsPerPage         the number of commands on one page
	 * @param sortAlphabetically   whether commands are listed by their arguments in alphabetical order
	 * @param dedupeByDefinitionId whether commands of one definition are listed once
	 */
	public DefaultHelpBuilder(
			@NotNull HelpMessages messages,
			@NotNull String host,
			@NotNull Map<String, String> argumentNames,
			@Nullable PaginationBuilder pagination,
			int itemsPerPage,
			boolean sortAlphabetically,
			boolean dedupeByDefinitionId
	) {
		if (itemsPerPage < 1) throw new IllegalArgumentException("itemsPerPage must be at least 1: " + itemsPerPage);

		this.messages = messages;
		this.host = host;
		this.argumentNames = Map.copyOf(argumentNames);
		this.pagination = pagination;
		this.itemsPerPage = itemsPerPage;
		this.sortAlphabetically = sortAlphabetically;
		this.dedupeByDefinitionId = dedupeByDefinitionId;
	}

	@Override
	@NotNull
	public List<Message> build(@NotNull Collection<Command<S>> commands, int page) {
		List<Command<S>> listed = listed(commands);
		List<Message> lines = new ArrayList<>();
		lines.add(messages.title(host));

		List<Message> commandLines = listed.stream()
				.skip((long) (Math.max(1, page) - 1) * itemsPerPage)
				.limit(itemsPerPage)
				.map(this::line)
				.toList();
		lines.addAll(commandLines.isEmpty() ? List.of(messages.empty()) : commandLines);

		pageLine(listed.size(), page).ifPresent(lines::add);

		return lines;
	}

	@NotNull
	private Optional<Message> pageLine(int totalItems, int page) {
		if (pagination == null) return Optional.empty();

		return pagination.build(totalItems, page, itemsPerPage);
	}

	@NotNull
	private List<Command<S>> listed(@NotNull Collection<Command<S>> commands) {
		List<Command<S>> listed = dedupeByDefinitionId ? dedupe(commands) : new ArrayList<>(commands);
		if (sortAlphabetically) listed.sort(Comparator.comparing(DefaultHelpBuilder::sortKey, String.CASE_INSENSITIVE_ORDER));

		return listed;
	}

	@NotNull
	private List<Command<S>> dedupe(@NotNull Collection<Command<S>> commands) {
		List<Command<S>> result = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (Command<S> command : commands) {
			Optional<String> definitionId = command.commandMeta().optional(CommandantKeys.DEFINITION_ID);
			if (definitionId.isEmpty() || seen.add(definitionId.get())) result.add(command);
		}

		return result;
	}

	@NotNull
	private Message line(@NotNull Command<S> command) {
		return messages.command(
				command.rootComponent().name(),
				arguments(command.nonFlagArguments()),
				description(command.commandDescription().description())
		);
	}

	// Every argument after the root command, each after a space.
	@NotNull
	private Value arguments(@NotNull List<? extends CommandComponent<?>> components) {
		List<Value> parts = new ArrayList<>();
		for (CommandComponent<?> component : components.subList(Math.min(1, components.size()), components.size())) {
			parts.add(SPACE);
			parts.add(argument(component));
		}

		return Value.joined(parts);
	}

	@NotNull
	private Value argument(@NotNull CommandComponent<?> component) {
		String name = argumentNames.getOrDefault(component.name(), component.name());

		return switch (component.type()) {
			case REQUIRED_VARIABLE -> Value.nested(messages.argument(name));
			case OPTIONAL_VARIABLE -> Value.nested(messages.optionalArgument(name));
			default -> Value.text(component.name());
		};
	}

	@NotNull
	private static Value description(@NotNull Description description) {
		if (description instanceof TranslatableDescription translatable) return Value.nested(translatable.getMessage());

		return Value.text(description.textDescription());
	}

	@NotNull
	private static String sortKey(@NotNull Command<?> command) {
		return command.nonFlagArguments().stream()
				.map(CommandComponent::name)
				.collect(Collectors.joining(" "));
	}
}
