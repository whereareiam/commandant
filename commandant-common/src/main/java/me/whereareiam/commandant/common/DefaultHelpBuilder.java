package me.whereareiam.commandant.common;

import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.builder.ArgumentFactory;
import me.whereareiam.commandant.builder.HelpBuilder;
import me.whereareiam.commandant.builder.PaginationBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
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
 * The standard help: the {@link Text}s under a prefix, as translatable components with named arguments. A
 * command's description is shown as a component when its {@link Description} is a {@link ComponentLike}, such as
 * a translatable description, and as text otherwise.
 *
 * @param <S> the sender type
 */
public class DefaultHelpBuilder<S> implements HelpBuilder<S> {
	private static final Component SPACE = Component.space();

	private final String prefix;
	private final ArgumentFactory arguments;
	private final String host;
	private final Map<String, String> argumentNames;
	private final PaginationBuilder pagination;
	private final int itemsPerPage;
	private final boolean sortAlphabetically;
	private final boolean dedupeByDefinitionId;

	/**
	 * Creates a help.
	 *
	 * @param prefix               the keys' prefix, such as {@code arbor.help}
	 * @param arguments            makes the named arguments of the texts
	 * @param host                 the name of the plugin whose help it is, the heading's {@link Argument#HOST}
	 * @param argumentNames        names to show instead of arguments' own names
	 * @param pagination           builds the page line, or {@code null} for none
	 * @param itemsPerPage         the number of commands on one page
	 * @param sortAlphabetically   whether commands are listed by their arguments in alphabetical order
	 * @param dedupeByDefinitionId whether commands of one definition are listed once
	 */
	public DefaultHelpBuilder(
			@NotNull String prefix,
			@NotNull ArgumentFactory arguments,
			@NotNull String host,
			@NotNull Map<String, String> argumentNames,
			@Nullable PaginationBuilder pagination,
			int itemsPerPage,
			boolean sortAlphabetically,
			boolean dedupeByDefinitionId
	) {
		if (itemsPerPage < 1) throw new IllegalArgumentException("itemsPerPage must be at least 1: " + itemsPerPage);

		this.prefix = prefix;
		this.arguments = arguments;
		this.host = host;
		this.argumentNames = Map.copyOf(argumentNames);
		this.pagination = pagination;
		this.itemsPerPage = itemsPerPage;
		this.sortAlphabetically = sortAlphabetically;
		this.dedupeByDefinitionId = dedupeByDefinitionId;
	}

	@Override
	@NotNull
	public Component build(@NotNull Collection<Command<S>> commands, int page) {
		List<Command<S>> listed = listed(commands);
		List<Component> lines = new ArrayList<>();
		lines.add(text(Text.TITLE, arguments.create(Argument.HOST, host)));

		List<Component> commandLines = listed.stream()
				.skip((long) (Math.max(1, page) - 1) * itemsPerPage)
				.limit(itemsPerPage)
				.map(this::line)
				.toList();
		lines.addAll(commandLines.isEmpty() ? List.of(text(Text.EMPTY)) : commandLines);

		pageLine(listed.size(), page).ifPresent(lines::add);

		return Component.join(JoinConfiguration.newlines(), lines);
	}

	@NotNull
	private Optional<Component> pageLine(int totalItems, int page) {
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
	private Component line(@NotNull Command<S> command) {
		return text(
				Text.COMMAND,
				arguments.create(Argument.COMMAND, command.rootComponent().name()),
				arguments.create(Argument.ARGUMENTS, arguments(command.nonFlagArguments())),
				arguments.create(Argument.DESCRIPTION, description(command.commandDescription().description()))
		);
	}

	// Every argument after the root command, each after a space.
	@NotNull
	private Component arguments(@NotNull List<? extends CommandComponent<?>> components) {
		List<Component> parts = new ArrayList<>();
		for (CommandComponent<?> component : components.subList(Math.min(1, components.size()), components.size())) {
			parts.add(SPACE);
			parts.add(argument(component));
		}

		return Component.join(JoinConfiguration.noSeparators(), parts);
	}

	@NotNull
	private Component argument(@NotNull CommandComponent<?> component) {
		String name = argumentNames.getOrDefault(component.name(), component.name());

		return switch (component.type()) {
			case REQUIRED_VARIABLE -> text(Text.ARGUMENT, arguments.create(Argument.NAME, name));
			case OPTIONAL_VARIABLE -> text(Text.OPTIONAL_ARGUMENT, arguments.create(Argument.NAME, name));
			default -> Component.text(component.name());
		};
	}

	@NotNull
	private static Component description(@NotNull Description description) {
		if (description instanceof ComponentLike component) return component.asComponent();

		return Component.text(description.textDescription());
	}

	@NotNull
	private Component text(@NotNull Text text, @NotNull ComponentLike... values) {
		return Component.translatable(text.under(prefix), values);
	}

	@NotNull
	private static String sortKey(@NotNull Command<?> command) {
		return command.nonFlagArguments().stream()
				.map(CommandComponent::name)
				.collect(Collectors.joining(" "));
	}
}
