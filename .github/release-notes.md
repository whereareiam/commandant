---
version: 2.0.0
---

# Commandant 2.0.0 - Keystone 2 and every reader's language

Commandant 2.0.0 is built on Keystone 2: the help, its page line, the reasons a command failed and the cooldown message are Commandant's own Semantica messages, shipped in English in its jar, which the plugin's Keystone messenger shows to each sender in their language and which a plugin words anew in its own files. It needs Java 21.

## Highlights

* `CommandantMessages` declares every text of Commandant (`commandant.exception.*`, `commandant.help.*`, `commandant.pagination.*`) with its English default; a plugin adds them as Semantica's lowest layer with `ClasspathSource.of(CommandantMessages.class, configura)` and overrides any of them in any language.
* `ExceptionHandlerRegistrar.register(manager, messenger)` tells a missing permission, a wrong syntax, an argument that cannot be read, the wrong sender and a failing handler through the plugin's messenger, on Cloud's own exception controller.
* `HelpBuilder.build` returns the page's lines as messages: the heading, a line per command with each argument its own text, a translated description for a `TranslatableDescription`, and a page line whose arrows run the page command when clicked.
* `DefinitionAdapter.Cooldown` gives a definition's command a cooldown in seconds; definitions naming the same group share one, and a blank group gives a definition a cooldown of its own. `CommandCooldowns.register(manager, messenger)` enforces them per sender; the console and senders without an id never wait.

## Upgrading

* Build a Keystone 2 messenger over the senders of the command manager and pass it to `ExceptionHandlerRegistrar.register` and `CommandCooldowns.register`; `SerializerEngine`, `AudienceProvider`, `ExceptionMessages`, `ExceptionMessageKeys`, `MessageResolver`, `Console` and cloud-minecraft-extras are no longer used.
* Add Commandant's defaults to the plugin's Semantica: `.layer(Layer.of(ClasspathSource.of(CommandantMessages.class, configura), Layer.DEFAULTS))`. Texts written for 1.x move to the plugin's language files under their new keys, such as `commandant.exception.no-permission` with `{permission}` instead of `{content}`.
* Build help with `Help.<S>builder().host(name).pageCommand(page -> "/<command> help " + page).build()` and send each line: `help.build(commands, page).forEach(line -> messenger.send(sender, line))`. `Pagination.builder()` takes no prefix; its texts are `commandant.pagination.*`.
* `TranslatableDescription.of(message)` takes a declared Semantica message instead of a key.
* `CommandantSyntaxFormatter` is removed: the syntax in `commandant.exception.invalid-syntax` is Cloud's own, as text.
* A definition with a cooldown needs `org.incendo:cloud-processors-cooldown` at runtime.
* Java 21 is required.
