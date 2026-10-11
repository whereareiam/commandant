---
version: 0.0.2
---

# Commandant 0.0.2 - [Short subtitle]

[1-2 sentence summary of the release.]

## Highlights

* Commands are parsed by your own parser and handed to `Commandant.process(command, manager)`, which applies a definition's overrides through a `DefinitionAdapter` before registering them.
* Definitions can declare root aliases, and usage tokens that do not match the command's arguments fail fast.
* Help and pagination are configured through fluent builders; help can list a command once per definition.
* Exception messages format their placeholder values through `ExceptionFormatting`, with built-in formatters for Cloud permissions.
* `CommandantSyntaxFormatter` gives Cloud's syntax messages the argument format of Commandant's help.

## Upgrading

* `Command`, `CommandDefinition` and `CommandRegistrar` are removed; parse commands yourself and register them through `Commandant.process`.
