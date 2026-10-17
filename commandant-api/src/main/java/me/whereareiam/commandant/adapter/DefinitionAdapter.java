package me.whereareiam.commandant.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface DefinitionAdapter<T> {
	boolean isEnabled(@NotNull T definition);

	@Nullable List<String> getAliases(@NotNull T definition);

	@Nullable String getPermission(@NotNull T definition);

	@Nullable String getDescription(@NotNull T definition);

	@Nullable String getUsage(@NotNull T definition);

	@Nullable Cooldown getCooldown(@NotNull T definition);

	@Nullable Map<String, String> getArguments(@NotNull T definition);

	/**
	 * Cooldown of a definition's command.
	 * <p>
	 * A command on cooldown refuses a sender who ran it less than {@link #getDuration()} seconds ago. Commands
	 * whose definitions name the same {@link #getGroup() group} share one cooldown: running any of them puts all
	 * of them on cooldown for that sender. Cooldowns take effect once {@code CommandCooldowns} is registered on
	 * the command manager, which also tells a refused sender how long to wait.
	 */
	interface Cooldown {
		/**
		 * Returns whether the command has a cooldown.
		 *
		 * @return {@code true} if the cooldown applies; a cooldown with a duration of zero or less never applies
		 */
		boolean isEnabled();

		/**
		 * Returns how long the command stays on cooldown after a sender ran it.
		 *
		 * @return the duration in seconds
		 */
		int getDuration();

		/**
		 * Returns the group whose commands share this cooldown.
		 *
		 * @return the group name; a blank name gives the definition a cooldown of its own, shared only by its aliases
		 */
		@NotNull String getGroup();
	}
}
