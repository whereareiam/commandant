## Summary

<!-- What changes for plugins that use Commandant, and why. -->

## Release notes

<!-- The title and the label decide what the release draft shows. See "Release notes" in AGENTS.md. -->

- [ ] The title follows `Area: Description` with a listed area
- [ ] The title describes the effect in the past tense, for example `Help: Fixed ...`
- [ ] Exactly one label: `feature`, `change`, `bug`, `dependencies`, or `skip-changelog`
- [ ] `bug` only for a bug that shipped in a release; a fix for a bug that only existed on `dev` is `skip-changelog`
- [ ] `major` added when plugins must change their code on upgrade
- [ ] Upgrade steps added to `.github/release-notes.md`, if plugins need them

## Checks

- [ ] Tests added or updated
- [ ] Javadocs updated for changed public API
