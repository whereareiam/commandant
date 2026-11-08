---
version: 2.0.0
---

# Commandant 2.0.0 - Help in every reader's language

Commandant 2.0.0 builds help and pagination from translatable components, so one help page is shown in each reader's language by the translator the plugin registers with Adventure. Plugins that build help need the upgrade steps below.

## Highlights

* `HelpBuilder.build` returns a `Component` made of translatable keys under a prefix the plugin chooses, with named arguments made by the plugin's `ArgumentFactory`, such as semantica's `TextArgument::of`.
* `HelpBuilder.Text` and `PaginationBuilder.Text` list every key with the named arguments it is given, so a plugin writes its translations in its own wording.
* `TranslatableDescription.of(key)` gives a command a description that the help shows in the reader's language.
* The page arrows run the page command themselves, so translations need no click tags.

## Upgrading

* Replace `HelpMessages` and `PaginationMessages` with translations of each `HelpBuilder.Text` under the help's prefix, for example `arbor.help.title`, and of each `PaginationBuilder.Text` under `<prefix>.pagination`. Send the `Component` that `HelpBuilder.build` returns instead of deserializing a string.
* Build help with `Help.builder(prefix, arguments).host(name).pageCommand(page -> "/<command> help " + page)`. `customArgumentNames` is now `argumentNames`, and `itemsPerPage` defaults to 7. Placeholders move from `{previousPage}`/`{nextPage}` to `{page}`, and from `{argument}` to `{name}`.
* A pagination of your own is built with `Pagination.builder(prefix, arguments)`, whose `showIfOnePage`, `showPreviousOnFirst` and `showNextOnLast` replace the flags of `PaginationMessages`. `PaginationBuilder.build` returns the page line, or nothing when it is hidden.
* `CommandantSyntaxFormatter` takes the required and optional argument templates as two strings instead of `HelpMessages.Format`.
