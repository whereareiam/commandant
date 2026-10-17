---
version: 1.1.0
---

# Commandant 1.1.0 - Command cooldowns

Commandant 1.1.0 enforces the cooldowns that definitions give their commands: register `CommandCooldowns` on the command manager, and a sender who runs a command again too soon is told how long to wait.

## Highlights

* `DefinitionAdapter.Cooldown` gives a definition's command a cooldown in seconds; definitions naming the same group share one, and a blank group gives a definition a cooldown of its own.
* `CommandCooldowns.register` enforces the cooldowns per sender with Cloud's cooldown processor and sends the new `cooldown` message of `ExceptionMessages`, or `ExceptionMessageKeys.getCooldown()`, with the seconds left as `content`. The console is never limited.

## Upgrading

* A definition with a cooldown needs `org.incendo:cloud-processors-cooldown` at runtime.
* `ExceptionMessageKeys` has a new required `cooldown` key; `ExceptionMessageKeys.defaults()` sets it.
