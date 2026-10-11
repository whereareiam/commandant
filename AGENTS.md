# AGENTS.md

Commandant applies definition-driven configuration to Cloud commands. A plugin parses its commands with
its own parser and hands them to Commandant, which applies a definition's aliases, usage, permission,
description and cooldown, and registers them. It also builds help pages and pagination and renders
command exceptions through Keystone messages.

## Modules

- `commandant-api`: what plugins use. The `@Definition` annotation, the `DefinitionAdapter` contract,
  the help and pagination builders, message models, exception message keys and exception formatting.
  Public API: every change is compatibility-sensitive and needs complete Javadocs.
- `commandant-common`: definition parsing and the default help and pagination builders.
- `commandant`: the entry point plugins depend on (`Commandant`, `Help`, `Pagination`), the exception
  handlers, the built-in permission formatters and the syntax formatter.
- `build-logic`: the `commandant.java-common` convention every module applies (Java 17, the shared
  `compileOnly` dependencies, publishing, the version from `VERSION`).

Cloud, Adventure and Lombok stay `compileOnly`; the plugin that uses Commandant provides Cloud and
Adventure. Keystone is the only library Commandant brings along.

## Build and test

- `./gradlew test build` builds everything and runs the tests.
- `VERSION=<version> ./gradlew publishToMavenLocal` lets a plugin build against a local version.

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
  `Syntax` (the syntax formatter), `Messages` (message models and resolution). Add an area only for
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
