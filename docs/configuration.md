# XMas Tree Configuration

Main config file:

```text
plugins/X-Mas/config.yml
```

The plugin preserves existing admin-edited values while adding missing defaults and missing bundled comments when it loads config. Most settings apply with `/xmastree reload`; the exceptions are noted below.

## Core Settings

| Key | Default | Reload Behavior | Notes |
| --- | --- | --- | --- |
| `core.plugin-enabled` | `true` | `/xmastree reload`; also live-toggleable with `/xmastree debug toggle core.plugin-enabled true\|false` | Master event switch. Turning the event back on after an end state is safest with a restart. |
| `core.locale` | `en` | `/xmastree reload` | Loads `plugins/X-Mas/translations/locale_<locale>.yml`. |
| `core.tree-limit` | `3` | `/xmastree reload` | Applies to future placement checks. |
| `core.holiday-ends.enabled` | `true` | `/xmastree reload`; live-toggleable | Enables time-based event shutdown. |
| `core.holiday-ends.date` | `10-01-2027 03-33-33` | `/xmastree reload` | Format is `dd-MM-yyyy HH-mm-ss`. |
| `core.holiday-ends.resource-back` | `true` | `/xmastree reload`; live-toggleable | Returns actually spent upgrade materials when a tree is destroyed after confirmation. |
| `core.update-speed` | `7` ticks | Restart recommended | Main tree update task cadence. Reload reads the value, but a restart is recommended to recreate the scheduled task cadence. |
| `core.particles-delay` | `35` ticks | Restart recommended | Particle task cadence. |
| `core.particles-enabled` | `true` | `/xmastree reload`; live-toggleable | Global particle switch. |
| `core.sounds.grow.first-volume` | `0.5` | `/xmastree reload` | First accepted item sound volume. Use `0.0` for silent and `1.0` for full volume. |
| `core.sounds.grow.repeat-volume` | `0.2` | `/xmastree reload` | Repeated accepted item sound volume for the same material requirement. |

## Luck

| Key | Default | Reload Behavior | Notes |
| --- | --- | --- | --- |
| `xmas.luck.enabled` | `false` | `/xmastree reload`; live-toggleable | When disabled, presents always use the main gift pool. |
| `xmas.luck.chance` | `75` | `/xmastree reload` | Percent chance for the normal gift outcome when luck is enabled. |

## Presents and Gifts

`xmas.presents` controls the player-head textures used for present blocks. Modern entries should be `textures.minecraft.net` URLs. Old player-name entries still load for compatibility, but diagnostics warn about them.

`xmas.gifts` controls rewards from opened presents. Supported formats:

```yaml
xmas:
 gifts:
  - DIAMOND
  - EMERALD:3
  - item: GOLD_INGOT
    weight: 10
```

Supported gift entry forms:

- `MATERIAL`
- `MATERIAL:AMOUNT`
- Base64 item strings saved by `/xmastree gifts addhand`
- weighted entries with `item` and positive whole-number `weight`

Invalid modern material names, legacy material names, malformed texture URLs, and unsafe texture URL hosts are skipped and surfaced in reload/debug diagnostics.

## Milestones

Milestones live under:

```yaml
xmas:
 milestones:
  player:
  community:
```

Supported milestone fields:

| Field | Notes |
| --- | --- |
| `enabled` | `true` or `false`. |
| `type` | Player milestones support `daily_gifts`, `total_gifts`, and `streak_days`. Community milestones support `community_gifts`. |
| `target` | Whole number `1` or higher. |
| `display-name` | Name shown in milestone commands and journals. |
| `reward-commands` | Console commands run when the milestone is claimed. |

Reward command tokens:

- `{player}`
- `{claimer}`
- `{milestone}`
- `{display_name}`
- `{target}`
- `{progress}`

Example:

```yaml
reward-commands:
 - cmi kit christmas {player}
 - cmi money give {player} 250
 - cmi toast {player} -t:task -icon:EMERALD -text:"{display_name} complete!"
```

Milestone definition changes apply with `/xmastree reload`. Existing progress and claim state are stored in `progress.yml` and are not reset by changing config.

## Tree Levels

Tree stages live under `xmas.tree-lvl`:

- `sapling`
- `small_tree`
- `tree`
- `magic_tree`
- `super_tree`

Each stage can define:

- `gift-cooldown`
- `particles.ambient`
- `particles.swag`
- `particles.body`
- `lvlup` material requirements for the next stage

Use modern Paper/Bukkit material names for `lvlup`. Particle names should come from the Paper 26.2 `Particle` enum:

[jd.papermc.io/paper/26.2/org/bukkit/Particle.html](https://jd.papermc.io/paper/26.2/org/bukkit/Particle.html)

`/xmastree reload` is enough for new trees and newly loaded trees. A restart is recommended when every existing loaded tree should use refreshed stage settings consistently.

## World Aliases and Migration

Legacy world-name remapping lives under:

```yaml
migration:
 world-aliases:
  general: world
  wild: world
```

Aliases let old `trees.yml` records load in renamed worlds without rewriting data. To rewrite saved world names, use:

```text
/xmastree data migrate-world <from> <to>
/xmastree data migrate-world <from> <to> apply
```

The `apply` form creates a backup before writing and should be followed by a restart before testing migrated legacy trees.

## Translation and Theme

Player/admin messages, the chat prefix, and theme aliases live in:

```text
plugins/X-Mas/translations/locale_en.yml
```

MiniMessage is supported. The bundled locale uses semantic tags such as `<xm-text>`, `<xm-accent>`, `<xm-label>`, `<xm-command>`, `<xm-success>`, `<xm-warning>`, `<xm-error>`, and `<xm-info>`.
