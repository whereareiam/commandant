package me.whereareiam.commandant.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.whereareiam.commandant.message.CommandantMessages;
import me.whereareiam.configura.Configura;
import me.whereareiam.keystone.Messenger;
import me.whereareiam.keystone.Recipient;
import me.whereareiam.keystone.common.DefaultMessenger;
import me.whereareiam.keystone.common.MessengerOptions;
import me.whereareiam.keystone.common.Platform;
import me.whereareiam.keystone.platform.format.AdventureV5Format;
import me.whereareiam.semantica.catalog.Layer;
import me.whereareiam.semantica.common.Semantica;
import me.whereareiam.semantica.format.adventure.TagPolicy;
import me.whereareiam.semantica.source.file.ClasspathSource;
import me.whereareiam.semantica.syntax.icu.IcuSyntax;
import me.whereareiam.semantica.testing.MemorySource;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Locale;

/**
 * Keystone's messenger as a plugin builds it, over {@link TestSender}s: Semantica reads Commandant's defaults from its
 * jar, under a plugin's own texts, and the console's lines go nowhere.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestMessenger {
	/**
	 * Creates a messenger with Commandant's texts alone.
	 *
	 * @return the messenger
	 */
	public static @NotNull Messenger<TestSender> create() {
		return create(new MemorySource("plugin"));
	}

	/**
	 * Creates a messenger with a plugin's own texts above Commandant's.
	 *
	 * @param plugin the plugin's texts, such as its commands' descriptions or its own wording of Commandant's texts
	 * @return the messenger
	 */
	public static @NotNull Messenger<TestSender> create(@NotNull MemorySource plugin) {
		Semantica semantica = Semantica.builder()
				.syntax(new IcuSyntax())
				.defaultLocale(Locale.ENGLISH)
				.layer(Layer.of(ClasspathSource.of(CommandantMessages.class, Configura.builder().build()), Layer.DEFAULTS))
				.layer(Layer.of(plugin, Layer.OVERRIDES))
				.watch(false)
				.build();
		MessengerOptions options = MessengerOptions.builder()
				.semantica(semantica)
				.format(new AdventureV5Format())
				.policy(TagPolicy.defaults())
				.fallback(Locale.ENGLISH)
				.build();

		return new DefaultMessenger<>(new SilentPlatform(), TestSender::recipient, options);
	}

	private static final class SilentPlatform implements Platform {
		@Override
		public @NotNull Recipient console() {
			return TestSender.console().recipient();
		}

		@Override
		public void log(@NotNull String line) {
		}

		@Override
		public void later(@NotNull Duration delay, @NotNull Runnable task) {
			task.run();
		}
	}
}
