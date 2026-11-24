package me.whereareiam.commandant.cooldown;

import me.whereareiam.commandant.Commandant;
import me.whereareiam.commandant.CommandantKeys;
import me.whereareiam.commandant.adapter.DefinitionAdapter;
import me.whereareiam.commandant.model.Console;
import me.whereareiam.keystone.Actor;
import me.whereareiam.keystone.model.SerializerContent;
import me.whereareiam.keystone.serializer.SerializerEngine;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.minecraft.extras.AudienceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandCooldownsTest {
	private CommandManager<TestActor> commandManager;
	private MutableClock clock;
	private CommandCooldowns<TestActor> cooldowns;
	private Map<String, Integer> executions;

	@BeforeEach
	void setUp() {
		commandManager = new CommandManager<>(
				ExecutionCoordinator.simpleCoordinator(),
				CommandRegistrationHandler.nullCommandRegistrationHandler()
		) {
			@Override
			public boolean hasPermission(@NotNull TestActor sender, @NotNull String permission) {
				return true;
			}
		};
		clock = new MutableClock();
		cooldowns = CommandCooldowns.register(
				commandManager,
				actor -> "Wait {content}",
				new PlaceholderSerializer(),
				AudienceProvider.nativeAudience(),
				clock
		);
		executions = new HashMap<>();
	}

	@AfterEach
	void tearDown() {
		cooldowns.close();
	}

	@Test
	void refusesTheCommandUntilItsCooldownIsOver() {
		register("spawn", List.of("spawn"), new TestCooldown(true, 3, "default"));
		TestActor alice = new TestActor();

		run(alice, "spawn");
		run(alice, "spawn");
		assertEquals(1, executions.get("spawn"));
		assertEquals(List.of("Wait 3"), alice.messages);

		clock.advance(Duration.ofMillis(2_500));
		run(alice, "spawn");
		assertEquals(1, executions.get("spawn"));
		assertEquals("Wait 1", alice.messages.get(1));

		clock.advance(Duration.ofMillis(500));
		run(alice, "spawn");
		assertEquals(2, executions.get("spawn"));
	}

	@Test
	void keepsCooldownsPerSender() {
		register("spawn", List.of("spawn"), new TestCooldown(true, 3, "default"));
		TestActor alice = new TestActor();
		TestActor bob = new TestActor();

		run(alice, "spawn");
		run(bob, "spawn");

		assertEquals(2, executions.get("spawn"));
		assertTrue(bob.messages.isEmpty());
	}

	@Test
	void sharesTheCooldownOfAGroup() {
		register("spawn", List.of("spawn"), new TestCooldown(true, 3, "travel"));
		register("home", List.of("home"), new TestCooldown(true, 3, "travel"));
		register("help", List.of("help"), new TestCooldown(true, 3, "info"));
		TestActor alice = new TestActor();

		run(alice, "spawn");
		run(alice, "home");
		run(alice, "help");

		assertEquals(1, executions.get("spawn"));
		assertEquals(null, executions.get("home"));
		assertEquals(1, executions.get("help"));
	}

	@Test
	void sharesTheCooldownOfABlankGroupBetweenTheAliasesOfOneDefinitionOnly() {
		register("spawn", List.of("spawn", "s"), new TestCooldown(true, 3, ""));
		register("home", List.of("home"), new TestCooldown(true, 3, ""));
		TestActor alice = new TestActor();

		run(alice, "spawn");
		run(alice, "s");
		run(alice, "home");

		assertEquals(1, executions.get("spawn"));
		assertEquals(1, executions.get("home"));
	}

	@Test
	void ignoresADisabledOrEmptyCooldown() {
		register("spawn", List.of("spawn"), new TestCooldown(false, 3, "default"));
		register("home", List.of("home"), new TestCooldown(true, 0, "default"));
		register("help", List.of("help"), null);
		TestActor alice = new TestActor();

		for (int i = 0; i < 2; i++) {
			run(alice, "spawn");
			run(alice, "home");
			run(alice, "help");
		}

		assertEquals(2, executions.get("spawn"));
		assertEquals(2, executions.get("home"));
		assertEquals(2, executions.get("help"));
		assertTrue(alice.messages.isEmpty());
	}

	@Test
	void endsTheCooldownsOfAClearedSender() {
		register("spawn", List.of("spawn"), new TestCooldown(true, 3, "default"));
		TestActor alice = new TestActor();

		run(alice, "spawn");
		cooldowns.clear(alice.getUniqueId());
		run(alice, "spawn");

		assertEquals(2, executions.get("spawn"));
	}

	@Test
	void letsTheConsoleRunACommandOnCooldown() {
		register("spawn", List.of("spawn"), new TestCooldown(true, 3, "default"));
		TestActor console = new TestActor(Console.CONSOLE_UUID);

		run(console, "spawn");
		run(console, "spawn");

		assertEquals(2, executions.get("spawn"));
	}

	private void register(
			@NotNull String definitionId,
			@NotNull List<String> aliases,
			@Nullable DefinitionAdapter.Cooldown cooldown
	) {
		Command<TestActor> command = commandManager.commandBuilder("original-" + definitionId)
				.meta(CommandantKeys.DEFINITION_ID, definitionId)
				.handler(context -> executions.merge(definitionId, 1, Integer::sum))
				.build();

		Commandant.process(command, commandManager)
				.withDefinition(new TestDefinition(aliases, cooldown), new TestAdapter())
				.register();
	}

	private void run(@NotNull TestActor sender, @NotNull String input) {
		commandManager.commandExecutor().executeCommand(sender, input).join();
	}

	private record TestDefinition(@NotNull List<String> aliases, @Nullable DefinitionAdapter.Cooldown cooldown) {}

	private record TestCooldown(boolean enabled, int duration, @NotNull String group) implements DefinitionAdapter.Cooldown {
		@Override
		public boolean isEnabled() {
			return enabled;
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public @NotNull String getGroup() {
			return group;
		}
	}

	private static final class TestAdapter implements DefinitionAdapter<TestDefinition> {
		@Override
		public boolean isEnabled(@NotNull TestDefinition definition) {
			return true;
		}

		@Override
		public @NotNull List<String> getAliases(@NotNull TestDefinition definition) {
			return definition.aliases();
		}

		@Override
		public @Nullable String getPermission(@NotNull TestDefinition definition) {
			return null;
		}

		@Override
		public @Nullable String getDescription(@NotNull TestDefinition definition) {
			return null;
		}

		@Override
		public @Nullable String getUsage(@NotNull TestDefinition definition) {
			return null;
		}

		@Override
		public @Nullable Cooldown getCooldown(@NotNull TestDefinition definition) {
			return definition.cooldown();
		}

		@Override
		public @Nullable Map<String, String> getArguments(@NotNull TestDefinition definition) {
			return null;
		}
	}

	private static final class MutableClock extends Clock {
		private Instant now = Instant.parse("2026-10-17T12:00:00Z");

		private void advance(@NotNull Duration duration) {
			now = now.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	private static final class TestActor implements Actor, Audience {
		private final UUID uniqueId;
		private final List<String> messages = new ArrayList<>();

		private TestActor() {
			this(UUID.randomUUID());
		}

		private TestActor(@NotNull UUID uniqueId) {
			this.uniqueId = uniqueId;
		}

		@Override
		public @NotNull UUID getUniqueId() {
			return uniqueId;
		}

		@Override
		public @NotNull String getUsername() {
			return "tester";
		}

		@Override
		public void sendMessage(@NotNull Component message) {
			messages.add(((TextComponent) message).content());
		}

		@Override
		public boolean hasPermission(@NotNull String permission) {
			return true;
		}

		@Override
		public @NotNull Locale getLocale() {
			return Locale.ENGLISH;
		}

		@Override
		public @NotNull Audience getAudience() {
			return this;
		}
	}

	private static final class PlaceholderSerializer implements SerializerEngine {
		@Override
		public @NotNull String serialize(@NotNull Component component) {
			return component.toString();
		}

		@Override
		public @NotNull Component serialize(@NotNull SerializerContent content) {
			return Component.text(content.getMessage());
		}

		@Override
		public @NotNull String renderTemplate(@NotNull SerializerContent content) {
			return content.getMessage();
		}

		@Override
		public @NotNull Component serialize(
				@NotNull Actor receiver,
				@NotNull String message,
				@NotNull Consumer<SerializerContent.Builder> consumer
		) {
			SerializerContent.Builder builder = SerializerContent.builder()
					.receiver(receiver)
					.message(message);
			consumer.accept(builder);
			String rendered = message;
			for (var entry : builder.build().getPlaceholders().entrySet())
				rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());

			return Component.text(rendered);
		}
	}
}
