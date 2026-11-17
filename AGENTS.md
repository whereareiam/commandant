# AGENTS.md

Commandant applies definition-driven configuration to Cloud commands. A plugin reads its commands from Cloud's
annotations with `AnnotatedCommands` (or its own parser) and hands them to Commandant, which applies a definition's aliases, usage, permission,
description and cooldown, and registers them. It also builds help pages and pagination, tells senders why
a command failed and enforces cooldowns, all through the plugin's Keystone 2 `Messenger`, in each sender's
language.

## Modules

- `commandant-api`: what plugins use. The `@Definition` annotation, the `DefinitionAdapter` contract, the
  help and pagination builders, `TranslatableDescription`, exception formatting, and Commandant's own texts:
  the Semantica `@Messages` interface `message/CommandantMessages` with its groups `ExceptionMessages`,
  `HelpMessages` and `PaginationMessages`. The `semantica-processor` writes `GeneratedCommandantMessages` and
  the English defaults (`META-INF/semantica/en/commandant.json`) when it compiles; Semantica's Gradle plugin
  exports them into `translations/en/` and `check` fails while that file is behind. Its test fixtures
  (`fixture/TestMessenger`, `TestSender`) give every module's tests a real Keystone messenger whose Semantica
  reads Commandant's defaults. Public API: every change is compatibility-sensitive and needs complete Javadocs.
- `commandant-common`: definition parsing and the default help and pagination builders.
- `commandant`: the entry point plugins depend on (`Commandant`, `AnnotatedCommands`, `Help`, `Pagination`), the exception
  handlers (`ExceptionHandlerRegistrar`), the cooldowns (`CommandCooldowns`) and the built-in permission
  formatters.
- `build-logic`: the `commandant.java-common` convention every module applies (Java 21, the shared
  `compileOnly` dependencies, a module name per jar, publishing, the version from `VERSION`).

`AnnotatedCommands` exists because Cloud's `AnnotationParser` registers what it parses, while a command must be
registered only after its definition is applied.

Cloud and Lombok stay `compileOnly`; the plugin that uses Commandant provides Cloud. Commandant brings
Keystone's API (and Semantica's through it); the plugin builds its own messenger and Semantica, and adds
Commandant's defaults as a layer: `Layer.of(ClasspathSource.of(CommandantMessages.class, configura), Layer.DEFAULTS)`.
Texts are never key strings in code: a text of Commandant's is a method of `CommandantMessages`.

## Build and test

- `./gradlew test build` builds everything and runs the tests; Java 21.
- `./gradlew :commandant-api:translationsExport` rewrites `translations/en/` after a default changed.
- `VERSION=<version> ./gradlew publishToMavenLocal` lets a plugin build against a local version.
- Keystone 2 and Semantica come from `mavenLocal()` until they are published; `gradle/libs.versions.toml`
  names them.

## Pull requests and releases

Build each change in its own `feature/...` branch and merge it into `dev` through a pull request.
Every pull request is checked for its title and label and runs the tests. Publishing a release draft
tags `dev` (tags carry no `v` prefix, for example `0.0.2`), publishes the Maven artifacts and attaches
the jars. A manual run of `Development build` publishes the selected branch as
`<branch>-<short commit>`.

### Release notes

A merged pull request lands in the release draft as `* <title> ([#<number>](<url>))`, under the
category of its label. Readers are plugin developers.

- Titles use `Area: Description`, with a past-tense description of what changed for a plugin:
  `Introduced`, `Changed`, `Fixed`, `Removed`.
- Areas: `Definitions` (parsing and applying definitions: aliases, usage, permissions, descriptions,
  cooldowns), `Help`, `Pagination`, `Exceptions` (exception handling, messages and formatting),
  `Messages` (Commandant's texts and how they are sent). Add an area only for
  something plugins recognise by that name, and add it to this list in the same pull request.
- Never use module or class names such as `Common` or `Builder` as an area.

| Label | Use for |
| --- | --- |
| `feature` | something new for plugins |
| `change` | changed behavior or API |
| `bug` | a fix for a bug that shipped in a release |
| `dependencies` | a library update |
| `skip-changelog` | refactors, build, CI, tests and documentation, and fixes of bugs that never shipped |

Add `major` next to the category when plugins must change their code on upgrade.

Text written by hand, the subtitle, the summary and upgrade notes, lives in `.github/release-notes.md`;
it replaces the draft's placeholders while its `version` matches the drafted version. The release
workflow stops while a placeholder is left.
