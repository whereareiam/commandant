package me.whereareiam.commandant;

import me.whereareiam.keystone.model.SerializerOptions;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.parser.aggregate.AggregateParser;
import org.incendo.cloud.syntax.StandardCommandSyntaxFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;

/**
 * Cloud syntax formatter that writes required and optional arguments with templates of their own, such as the
 * syntax shown by Commandant's exception messages. Each template holds the {@code argument} placeholder in the
 * given placeholder format, for example {@code <gray>[{argument}]</gray>}.
 *
 * @param <C> command sender type
 */
public class CommandantSyntaxFormatter<C> extends StandardCommandSyntaxFormatter<C> {
	private static final String ARGUMENT_PLACEHOLDER = "argument";

	private final Map<String, String> argumentNames;
	private final String requiredFormat;
	private final String optionalFormat;
	private final SerializerOptions.PlaceholderFormat placeholderFormat;

	/**
	 * Creates a new formatter.
	 *
	 * @param manager           command manager
	 * @param argumentNames     custom argument display names
	 * @param requiredFormat    template of a required argument, or {@code null} for its bare name
	 * @param optionalFormat    template of an optional argument, or {@code null} for its bare name
	 * @param placeholderFormat placeholder format used in the templates
	 */
	public CommandantSyntaxFormatter(
			@NotNull CommandManager<C> manager,
			@Nullable Map<String, String> argumentNames,
			@Nullable String requiredFormat,
			@Nullable String optionalFormat,
			@NotNull SerializerOptions.PlaceholderFormat placeholderFormat
	) {
		super(manager);
		this.argumentNames = argumentNames != null ? argumentNames : Map.of();
		this.requiredFormat = requiredFormat != null ? requiredFormat : placeholderFormat.format(ARGUMENT_PLACEHOLDER);
		this.optionalFormat = optionalFormat != null ? optionalFormat : placeholderFormat.format(ARGUMENT_PLACEHOLDER);
		this.placeholderFormat = placeholderFormat;
	}

	@Override
	protected @NotNull FormattingInstance createInstance() {
		return new CommandantFormattingInstance(argumentNames, requiredFormat, optionalFormat, placeholderFormat);
	}

	private static final class CommandantFormattingInstance extends FormattingInstance {
		private final Map<String, String> argumentNames;
		private final String requiredFormat;
		private final String optionalFormat;
		private final SerializerOptions.PlaceholderFormat placeholderFormat;

		private CommandantFormattingInstance(
				@NotNull Map<String, String> argumentNames,
				@NotNull String requiredFormat,
				@NotNull String optionalFormat,
				@NotNull SerializerOptions.PlaceholderFormat placeholderFormat
		) {
			this.argumentNames = argumentNames;
			this.requiredFormat = requiredFormat;
			this.optionalFormat = optionalFormat;
			this.placeholderFormat = placeholderFormat;
		}

		@Override
		public void appendRequired(@NotNull CommandComponent<?> argument) {
			super.appendName(format(argument, requiredFormat));
		}

		@Override
		public void appendOptional(@NotNull CommandComponent<?> argument) {
			super.appendName(format(argument, optionalFormat));
		}

		@Override
		public void appendAggregate(
				@NotNull CommandComponent<?> component,
				@NotNull AggregateParser<?, ?> parser
		) {
			String template = component.required() ? requiredFormat : optionalFormat;
			Iterator<? extends CommandComponent<?>> innerComponents = parser.components().iterator();
			while (innerComponents.hasNext()) {
				CommandComponent<?> innerComponent = innerComponents.next();
				super.appendName(format(innerComponent, template));
				if (innerComponents.hasNext()) appendBlankSpace();
			}
		}

		private @NotNull String format(@NotNull CommandComponent<?> argument, @NotNull String template) {
			return template.replace(placeholderFormat.format(ARGUMENT_PLACEHOLDER), argumentName(argument));
		}

		private @NotNull String argumentName(@NotNull CommandComponent<?> argument) {
			return argumentNames.getOrDefault(argument.name(), argument.name());
		}
	}
}
