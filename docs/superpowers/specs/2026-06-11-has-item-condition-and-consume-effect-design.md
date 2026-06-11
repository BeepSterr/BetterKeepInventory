# Design: `has_item` condition + `consume` effect

**Date:** 2026-06-11
**Status:** Approved design, pending implementation
**Scope:** One PR. Independent of the separate `then`/`else` branching work.

## Goal

Let server operators branch death-handling rules on whether a player is carrying
a specific item (by type/name/lore), and optionally "use up" that item on death.
The motivating use case is a **Protection Charm**: an item a server hands out that
the holder can configure a rule around (e.g. keep inventory), consumed on death.

`has_item` only *checks*; `consume` *mutates*. They are deliberately split because
in BKI a condition can evaluate `true` without its rule firing (multi-condition
`allMatch`) and conditions are re-checked on respawn — so item removal must live in
an effect, which runs exactly once, only when the rule fires.

## Non-goals

- No per-item `keep` effect — redundant with `drop` + `!` negation (decided separately).
- No PvP detection.
- `has_item` does not consume; `consume` does not check/gate.

## Config shape

Both reuse the existing `filters` block already used by `drop`/`damage`
(`items`, `name`, `lore`), so `!` negation and `*` wildcards behave identically.

```yaml
conditions:
  has_item:
    amount: ">= 1"          # NumberRange over total matching item count; default ">= 1"
    filters:
      items: ["NETHER_STAR"]   # optional; matches Material
      name:  ["*Protection Charm*"]
      lore:  ["*Soulbound*"]
```

```yaml
effects:
  consume:
    amount: 1               # how many matching items to remove; default 1
    filters:
      items: ["NETHER_STAR"]
      name:  ["*Protection Charm*"]
      lore:  ["*Soulbound*"]
```

`amount` differs by context: a **range** for `has_item` (how many must exist), an
**int** for `consume` (how many to remove). An empty/absent `filters` block matches
any item (so `has_item` with no filters = "has any item at all", `amount` defaulting
to `>= 1`).

## Components

### Shared: `ItemFilter` (new, `Library/`)
A small value object encapsulating the `items`/`name`/`lore` matching currently
duplicated inline in `DropItemEffect`/`DamageItemEffect`:

- `ItemFilter(ConfigurationSection filtersSection)` — parses `items` (`MaterialList`),
  `name`, `lore` (`Utilities.ConfigList`).
- `boolean matches(ItemStack item)` — true iff the item passes every provided filter,
  using the existing semantics (empty filter list = no constraint;
  `Utilities.advancedStringCompare` for name/lore; `MaterialList.contains` for type).

This is the one piece of shared logic both new units need. `DropItemEffect`/
`DamageItemEffect` are **not** refactored to use it in this PR (keeps the diff
focused and low-risk); a follow-up can retrofit them. Noted so we don't forget.

### `HasItemCondition` (`Content/Conditions/HasItemCondition.java`)
- Constructor: parse optional `amount` as `NumberRange` (default `">= 1"`), and an
  `ItemFilter` from `filters`.
- `check(...)`: iterate `ply.getInventory()`, sum the `getAmount()` of every stack the
  filter matches, return `amountRange.contains(total)`.
- No mutation, no respawn behavior.

### `ConsumeItemEffect` (`Content/Effects/ConsumeItemEffect.java`)
- Constructor: parse `amount` (int, default 1) and an `ItemFilter` from `filters`.
- `onDeath(...)`: walk the inventory, removing matching items (decrementing stacks /
  clearing slots) until `amount` total have been removed or no matches remain. Removing
  fewer than `amount` when not enough exist is fine (no error). Sends `effects.consume`
  message if non-empty (consistent with other effects).
- `onRespawn(...)`: no-op.
- **Timing:** consumes on death. With a keep base the kept inventory carries the
  removal through to respawn; documented assumption is keep-base/charm usage.

### Registration (`BetterKeepInventory.java`)
- `registerConditions`: `api.conditionRegistry().register(this, "has_item", HasItemCondition::new);`
- `registerEffects`:  `api.effectRegistry().register(this, "consume", ConsumeItemEffect::new);`
- Add matching `nlb.log(...)` lines like the surrounding registrations.

### Messages (`messages.yml`)
- Add `effects.consume` default, e.g. `"&7&oYour {item} crumbles to dust."`
  (blank-disable convention applies automatically via `Config.sendMessage`).

## Data flow

Death → `OnPlayerDeath` builds rules → `ConfigRule.trigger`:
`has_item.check()` gates the rule (unchanged machinery) → if the rule fires,
`consume.onDeath()` removes the item. No engine changes; both plug into existing
registries and the existing trigger loop.

## Docs

- `docs/docs/Rules/Conditions/HasItem.md` — fields, filter semantics, `amount` range.
- `docs/docs/Rules/Effects/Consume.md` — fields, timing note.
- `docs/docs/Rules/Examples/ProtectionCharm.md` — full worked example:
  `has_item` gate + `consume` + (with current sibling-rule inversion, or the future
  `then`/`else`) drop-everything otherwise.

## Backwards compatibility

Purely additive — two new registry entries, one new message key (auto-filled by the
existing `LoadMessages` copy-missing-keys logic). No config migration.

## Testing / verification

The repo has no unit-test harness (no JUnit/MockBukkit). Verification for this PR:
1. `mvn -pl plugin -am compile` succeeds.
2. Manual server check: a charm item triggers the rule and is consumed exactly once;
   a non-charm death does not; `amount` range boundaries behave.

If we want real regression coverage, the `ItemFilter.matches` logic is the pure,
harness-friendly piece worth a unit test once a test harness exists — flagged, not
done here.

## Open questions (defaults chosen, change if desired)

- `has_item.amount` counts **total items** (sum of stack sizes), not number of stacks.
- `consume.amount` removes a **total count** across stacks.
- `consume` runs on **death**, not respawn.
