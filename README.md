# Ascend: SbA (Stats by Actions) — short overview

**Ascend** turns vanilla Minecraft into a lightweight, persistent RPG: train attributes by doing things in-game, gain Ascend levels, spend points to boost combat, movement, magic and social skills, and watch those choices change how your character performs.

## What it does (quick)

* Adds an **Ascend level & XP** system (max level **20**, **15 points per level**).
* Players earn XP from actions: killing mobs, mining certain blocks, taking damage, crafting/smelting, using potions, trading with villagers, using weapons, and periodic checks for risky situations (low health, debuffs, darkness).
* Spend earned points on 10+ attributes (strength, agility, fortitude, intelligence, willpower, charisma, and weapon/magic scaling stats).
* Attribute points apply **live** via attribute modifiers and event hooks (damage, health, movement speed, enchant/potion interactions, trade discounts, etc.).
* Client UI: press **V** (configurable keybind) to open a compact stats screen and spend points. Server-authoritative with packet sync.

## Key mechanics & examples

* **Strength**: +attack damage, +knockback, and armor-bypass bonus calculated on hit.
* **Agility**: small movement speed bonus; XP for agile actions (sprinting, climbing, jumping).
* **Fortitude**: increases max health, knockback resistance, chance to resist/cleanse harmful effects.
* **Intelligence**: increases mana (and integrates with Iron’s Spellbooks if present), lengthens beneficial potion effects, reduces enchantment costs/effects.
* **Willpower**: reduces sanity drain and increases tempo/stamina gains (API helpers available).
* **Charisma**: grants trade discounts by adjusting merchant offers when opening trades.
* **Scaling stats**: light/medium/heavy weapon scaling and magic scaling multiply damage based on attack speed or magic damage type.

## Compatibility & tech notes

* Designed for **Forge 1.20.1** (example build uses Forge 47.4.0) and **Java 17** toolchain.
* Optional compat with **Iron’s Spellbooks** (adds mana/spell-power hooks). Also prepared to play nicely with Geckolib, Caelus, Curios, Player Animator (as declared in the build).
* Uses a capability (`PlayerStats`) for persistent storage and network packets to sync server → client. Attributes are capped (per-mod caps and MAX_ATTRIBUTE_POINTS = 100).

## Playstyle & design goal

Ascend is built to be unobtrusive and organic: your playstyle trains the corresponding attributes. Want a heavy-hitter who tanks? Smash armor and spend in Strength/Fortitude. Prefer spellcasting? Farm XP and invest in Intelligence and Magic Scaling. It’s about emergent progression. No grindy menus, just meaningful choices.
