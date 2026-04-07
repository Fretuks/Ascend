# Changelog

This changelog covers all recorded changes after `1.0.0`, grouped by release version from the git history.

## 1.1.0

- Added Ascend admin commands for inspecting stats, setting attributes, granting XP, and resetting progression.
- Added configurable progression values and several balance adjustments across leveling and attribute scaling.
- Expanded attribute effects, including strength, agility, fortitude, intelligence, willpower, and charisma improvements.
- Added Iron's Spellbooks compatibility so Ascend stats can affect mana, spell power, cooldowns, and related spellcasting bonuses.
- Added key rebinding support and translation/localization support.
- Added data generation support for generated assets and loot data.
- Added the `Moonseye Tome` item for granting knowledge.
- Added the `Shrine of Remembrance`, including its screen flow, custom block/model assets, network handling, and world generation.
- Improved the stats UI and refreshed project documentation.
- Fixed multiple early issues, including command handling and shrine worldgen stability.

## 1.2.0

- Fixed command registration so Ascend commands are consistently available.
- Added Mind Motion shrine-choice support and the initial compatibility layer for Mind Motion integration.
- Updated the shrine interaction flow and packet handling to support compatibility-specific outcomes.

## 2.0.1

- Added `Remembrance Essence`, including its item behavior, client packet, texture, localization, and shrine-related integration.
- Improved shrine data generation and structure handling to support the new remembrance flow.
- Refined Iron's Spellbooks integration and minor player stat synchronization behavior.
- Fixed issues in remembrance essence handling after the `2.0.0` release.

## 2.0.2

- Updated compatibility for the newest Mind Motion version.
- Adjusted Iron's Spellbooks compatibility wiring and related dependency setup.

## 2.0.3

- Slightly adjusted Ascend XP scaling.

## 2.1.0

- Updated Mind Motion dependency support again.
- Added the `Shrine of Remembrance` as a creative tab item so it can be accessed directly in creative mode.
- Reworked melee damage scaling to use separate light, medium, and heavy weapon multipliers based on held weapon type.
- Rebalanced strength-derived scaling formulas and related combat stat calculations.

## 2.1.1

- Made Iron's Spellbooks compatibility optional at runtime instead of requiring the full mod as a hard implementation dependency.
- Refactored the compatibility code so Iron's Spellbooks-specific attribute access stays behind the optional compat layer.
- Adjusted the fortitude tick handling and villager trade XP calculation logic.
- Included a small combat-side cleanup in player damage handling.

## 2.2.0

- Added ranged weapon scaling so projectile attacks can benefit from Ascend stat scaling.
- Extended weapon magic scaling to handle projectiles more directly and correctly.
- Updated the Mind Motion dependency version again.

## 2.2.1

- Enhanced intelligence, willpower, and charisma bonuses with broader gameplay effects.
- Added intelligence-based anvil support.
- Improved charisma trade handling and merchant compatibility behavior.
- Hardened `PlayerStats` persistence and validation to clamp invalid saved values and respect config-driven limits.
- Added GitHub issue templates for bug reports, feature requests, and tasks.
- Included small infrastructure and polish updates across packet registration, worldgen wiring, Moonseye Tome behavior, and player event handling.
