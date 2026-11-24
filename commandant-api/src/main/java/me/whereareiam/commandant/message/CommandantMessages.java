package me.whereareiam.commandant.message;

import me.whereareiam.semantica.declaration.Messages;

/**
 * Commandant's own texts: what a sender is told when a command fails, and the help with its page line. Their English
 * defaults ship in Commandant's jar; a plugin adds them as Semantica's lowest layer and overrides any of them, in any
 * language, in its own files.
 * <pre>{@code
 * Semantica semantica = Semantica.builder()
 *         .syntax(new IcuSyntax())
 *         .layer(Layer.of(ClasspathSource.of(CommandantMessages.class, configura), Layer.DEFAULTS))
 *         .layer(Layer.of(FileSource.of(dataFolder.resolve("languages"), configura), Layer.OVERRIDES))
 *         .build();
 * }</pre>
 * Their keys start with {@code commandant.}, such as {@code commandant.exception.no-permission}.
 */
@Messages("commandant")
public interface CommandantMessages {
	/**
	 * Returns the texts of a command that fails.
	 *
	 * @return the texts, {@code commandant.exception.*}
	 */
	ExceptionMessages exception();

	/**
	 * Returns the texts of the help.
	 *
	 * @return the texts, {@code commandant.help.*}
	 */
	HelpMessages help();

	/**
	 * Returns the texts of a page line.
	 *
	 * @return the texts, {@code commandant.pagination.*}
	 */
	PaginationMessages pagination();
}
