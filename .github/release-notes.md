---
version: 1.0.0
---

# Commandant 1.0.0 - Stable commands

Commandant 1.0.0 is the first stable release and builds on Keystone 1.0.0. Plugins parse their commands themselves and hand them to `Commandant.process`, which applies definitions, help and pagination; plugins written against 0.0.1 need the upgrade steps below.

## Highlights

* Commands are parsed by your own parser and handed to `Commandant.process(command, manager)`, which applies a definition's overrides through a `DefinitionAdapter` before registering them.
* Definitions can declare root aliases, and usage tokens that do not match the command's arguments fail fast.
* Help and pagination are configured through fluent builders; help can list a command once per definition.
* Exception messages format their placeholder values through `ExceptionFormatting`, with built-in formatters for Cloud permissions.
* `CommandantSyntaxFormatter` gives Cloud's syntax messages the argument format of Commandant's help.

## Upgrading

* `Command`, `CommandDefinition` and `CommandRegistrar` are removed; parse commands yourself and register them through `Commandant.process`.
