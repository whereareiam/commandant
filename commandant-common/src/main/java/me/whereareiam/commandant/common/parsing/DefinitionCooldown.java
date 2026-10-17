package me.whereareiam.commandant.common.parsing;

import me.whereareiam.commandant.adapter.DefinitionAdapter;
import org.incendo.cloud.Command;
import org.incendo.cloud.processors.cooldown.Cooldown;
import org.incendo.cloud.processors.cooldown.CooldownGroup;
import org.incendo.cloud.processors.cooldown.DurationFunction;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

/**
 * Turns a definition's cooldown into Cloud's cooldown metadata. It is a class of its own so that Cloud's cooldown
 * processor is needed at runtime only by definitions that have a cooldown.
 */
final class DefinitionCooldown {
	static <S> Command.@NotNull Builder<S> apply(
			Command.@NotNull Builder<S> builder,
			@NotNull DefinitionAdapter.Cooldown cooldown,
			@NotNull String definitionId
	) {
		Duration duration = Duration.ofSeconds(cooldown.getDuration());
		String group = cooldown.getGroup();
		CooldownGroup cooldownGroup = group.isBlank()
				? new DefinitionGroup(definitionId)
				: CooldownGroup.named(group);

		return builder.apply(Cooldown.of(DurationFunction.constant(duration), cooldownGroup));
	}

	/**
	 * Cooldown group of one definition, shared by the commands built for its aliases.
	 *
	 * @param definitionId the definition's id
	 */
	private record DefinitionGroup(@NotNull String definitionId) implements CooldownGroup {}
}
