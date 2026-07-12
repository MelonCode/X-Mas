# 1MB X-Mas Tree

Upgradeable Christmas trees for Paper servers. Players plant a magic spruce sapling, feed it resources, grow it through several tree levels, and collect presents that appear under the finished tree.

This fork keeps the old X-Mas event data usable for winter 2026 while moving the plugin forward for modern Paper and Java runtimes.

![X-Mas tree preview](http://puu.sh/dKlK1/85c3dad454.jpg)

## Current target

The Gradle build creates the current Paper 26.2 target jar in `build/libs`:

| Jar | Purpose |
| --- | --- |
| `1MB-XMas-2026-v2.1.0-051-v25-26.2.jar` | Modern Paper 26.2 build, Java 25 bytecode. |

The checked-in source compiles against the Paper 26.2 API, targets Java 25, and declares `api-version: 26.2`. This fork now treats Paper 26.2 as the primary winter 2026 target, with future 26.x builds as the expected forward-compatibility path.

## Features

- Magic Christmas Crystal item for planting event trees.
- Upgradeable spruce tree levels with configurable material requirements.
- Random present spawning under grown trees.
- Configurable present head skins using Mojang texture URLs.
- Configurable gift table using modern material names or saved exact items.
- MiniMessage support for locale strings, crystal display text, and plugin messages.
- Existing `plugins/X-Mas/trees.yml` data remains the event data source.
- Optional resource refunds when a tree is destroyed or cleaned up after the event.
- Configurable per-stage particles using Paper 26.2 particle names.
- `/xmastree debug` sections for `status`, `commands`, `permissions`, `placeholders`, `config`, and `diagnostics`, plus live global boolean toggles.
- Single primary `/xmastree` command; `/xmas` is no longer registered by this plugin.
- Optional PlaceholderAPI placeholders for CMI holograms, ajLeaderboards, scoreboards, and menus.
- Legacy `trees.yml` world-name alias support for renamed destination worlds.
- Comment-preserving `config.yml` syncing that keeps admin values while safely adding missing defaults and missing template comments.
- Present heads are tagged with plugin PDC data, so gift handling no longer depends on deprecated skull profile reads.

## Documentation

- [Player Guide](docs/player-guide.md)
- [Commands](docs/commands.md)
- [Permissions](docs/permissions.md)
- [Placeholders](docs/placeholders.md)
- [Configuration](docs/configuration.md)
- [Installation and Building](docs/installation.md)
- [Integrations](docs/integrations.md)
- [Troubleshooting](docs/troubleshooting.md)
- [Importer Manifest](docs/plugin-docs.yml)

Canonical public docs URL:

[docs.1moreblock.com/custom-server-plugins/xmas-tree/](https://docs.1moreblock.com/custom-server-plugins/xmas-tree/)

## v2 changelog

- modernize the plugin from the legacy deployed build to an actively maintained Paper 26.2 / Java 25 Gradle build
- simplify the build around the active Paper 26.2 target and remove the retired 1.x local server dependency
- keep build output clean and predictable in `build/libs` with the 2026 versioned jar naming

- keep legacy tree data compatible by continuing to read `plugins/X-Mas/trees.yml`
- add world alias migration support so old saved trees can survive renamed worlds
- preserve old event data while modernizing the runtime and admin tooling

- make `/xmastree` the primary command
- remove the old `/xmas` command alias so this plugin keeps one clear command surface
- improve `/xmastree help` output and keep it aligned with the actual command surface
- add `/xmastree info` as a concise player-facing event guide
- add community gift-opening milestones at 250 and 500 opens, with larger global present-spawn rewards
- retune sapling, small-tree, and super-tree particles toward a quieter winter/XMas style
- expand milestone reward commands with console-run tokens for kits, economy, CMI messages, and announcements
- add milestone reward dry-runs, logged reward command results, community claim confirmation, progress backups, and debounced progress saves

- add granular permissions under `onembxmastree.*`
- replace the old `xmas.admin` permission with `onembxmastree.admin`
- add separate permissions for `status`, `help`, `give`, `gifts`, `addhand`, `reload`, `debug`, `debug.toggle`, `end`, and `tree.override`

- add a modern debug system with named categories: `status`, `commands`, `permissions`, `placeholders`, `config`, and `diagnostics`
- keep numeric debug pages working as a legacy shortcut
- improve debug output formatting with clearer key/value coloring
- make invalid debug page or section requests return a helpful response instead of silently falling back
- add `/xmastree debug toggle <key> true|false` for live boolean config changes
- keep tab completion focused on named debug categories instead of numeric page suggestions
- add `/xmastree inspect` for staff tree support checks, including owner, tree UUID, level, location, remaining requirements, refund preview, present timer, and scheduled presents
- expand `/xmastree inspect` with owner online state, owner tree count, world/chunk state, built block count, next level, next-level space check, gift cooldown, nearby present count, requirement progress, spent resources, and refund state
- make `/xmastree reload` print a reload report with locale, gift/head/tree counts, key toggles, sound volumes, and config/data warnings
- add `/xmastree test sound` and `/xmastree test particle` so admins can preview reloadable sound volume and particle settings without waiting for live tree interactions
- add `/xmastree test refund` for a safe dry run of refund delivery without editing blocks, inventories, tree data, or config
- add `/xmastree data backup` and `/xmastree data validate` for safe `trees.yml` support checks before event work or migration testing
- add `/xmastree data migrate-world <from> <to> [dry-run|apply]` so saved tree world names can be reviewed and migrated safely with an automatic backup
- add `/xmastree gifts list`, `/xmastree gifts roll`, and `/xmastree gifts remove <index>` for command-line gift pool management before a GUI exists
- add weighted gift pools with `/xmastree gifts weight <index> <weight>` while keeping old simple gift entries compatible as weight `1`
- add `/xmastree gifts addhand` as the grouped gift-management command, while keeping `/xmastree addhand` as a compatibility shortcut
- add present head diagnostics for valid texture URLs, legacy player-name heads, invalid head entries, invalid gift entries, and tree-data validation warnings

- add optional PlaceholderAPI support with the `onembxmastree` namespace
- add placeholders for event state, end time, end countdown, auto-end, resource-back, particles, luck, tree totals, owner totals, player tree count, and plugin version
- add player highest-tree placeholders for CMI holograms, scoreboards, and ajLeaderboards
- document placeholders in the README and show them in debug output

- modernize message handling with MiniMessage support while keeping legacy color compatibility
- improve player-facing text, prefixes, debug output, and help text
- change the visible plugin and chat identity toward `XMas Tree` for clearer user-facing output
- make the Christmas Crystal display name non-italic

- fix `resource-back` so destroying a tree returns only the materials actually spent on that tree
- fix the old refund dupe issue where the plugin could return more than the player had used
- improve refund delivery with fallback order: chest, barrel, player inventory, then floor drops

- reduce the loud grow and ingredient sound behavior
- make first-hit and repeat-hit grow sound volumes configurable and reloadable
- support silent, quiet, and loud tuning through config without server restarts

- modernize material and item handling for current Paper names
- use safer material matching and validation to avoid legacy enum failures
- improve displayed item names so materials such as Redstone Dust render properly in requirement output
- add configurable per-stage particle effects using modern Paper particle names

- harden config and item parsing by restricting present texture URLs to `textures.minecraft.net`
- cap Base64 gift payload handling
- skip invalid or legacy material names safely

- update config comments and improve documentation for installation, building, commands, permissions, placeholders, support, and credits
- point support to the GitHub issues page
- refresh `.gitignore` for local dev/test folders and obvious OS/build junk
- remove deprecated skull-profile reads from gameplay logic and add an opt-in Gradle deprecation lint switch for future work
- promote the fork to a Paper 26.2-first custom 1MB target with a minor version bump

## Installation

1. Stop the Paper server.
2. Back up the existing `plugins/X-Mas` folder and the world folders.
3. Remove the old X-Mas jar from `plugins` so Paper does not load two copies of the same plugin.
4. Copy the jar for your server target into the server `plugins` folder.
5. Start the server with Java 25.
6. Check the console for XMas Tree startup messages, then run `/xmastree` in game or console.

For the 2026 target, use the modern Paper 26.2 jar:

- Paper 26.2: `1MB-XMas-2026-v2.1.0-051-v25-26.2.jar`
- Future 26.x: use the same jar for forward-compatibility testing

## Building

Requirements:

- JDK 25
- Gradle
- Centralized Paper server cache at `/Users/floris/Projects/Codex/servers/cache/Paper-26.2`
- PlaceholderAPI build `266` in the centralized 26.2 cache, sourced from the matching 26.2 1MB CMI-API server setup

This repo no longer requires a local `servers/` folder for compilation. If an ignored local `servers/` folder exists here for ad-hoc compatibility testing, treat it as optional local data rather than part of the project.

Build the current Paper 26.2 jar:

```bash
gradle clean buildAllJars
```

Build only the Paper 26.2 jar:

```bash
gradle jar
```

The `paper262Jar` task is kept as an alias:

```bash
gradle paper262Jar
```

`buildAllJars` now:

- compiles against centralized Paper API `26.2.build.29-alpha`
- declares plugin `api-version: 26.2`
- keeps the Java release target at `25`
- writes the standard jar to `build/libs/`
- copies the same jar into `libs/` for the centralized test runner
- prints the active build config, compile target, declared plugin API version, and forward-compatibility target

You can also inspect the build metadata directly with:

```bash
gradle printBuildConfig
```

Run an explicit deprecated-API lint pass when doing future refactors:

```bash
gradle clean compileJava -PlintDeprecatedApi=true
```

End users do not need any `servers/` folder. The installable jars are written to `build/libs/`, and this project also keeps a local copy in `libs/` for shared test-runner use.

## Commands

The only command registered by this plugin is `/xmastree`. The old `/xmas` alias is no longer registered, leaving `/xmas` free for a broader event command or another plugin.

| Command | Description |
| --- | --- |
| `/xmastree` | Shows plugin version, event status, auto-end status, resource-back status, tree count, and owner count. |
| `/xmastree help` | Shows the command list. |
| `/xmastree info` | Explains how players start the event, grow trees, claim gifts, reclaim ingredients, and progress streak milestones. |
| `/xmastree give <player>` | Gives an online player a Christmas Crystal. |
| `/xmastree gifts` | Spawns a small batch of presents under every loaded Christmas tree. |
| `/xmastree gifts spawn` | Explicit form of `/xmastree gifts`; spawns presents under every loaded Christmas tree. |
| `/xmastree gifts list [page]` | Lists configured gift rewards by index, weight, and roll chance. |
| `/xmastree gifts roll` | Previews one weighted random gift roll without giving it to a player. |
| `/xmastree gifts remove <index>` | Removes a configured gift reward by the index shown in `/xmastree gifts list`. |
| `/xmastree gifts weight <index> <weight>` | Sets a configured gift reward's roll weight. Weight must be a positive whole number. |
| `/xmastree gifts addhand` | Adds the item in your main hand to the gift list and saves it to `config.yml`. |
| `/xmastree addhand` | Compatibility shortcut for `/xmastree gifts addhand`. |
| `/xmastree reload` | Reloads config, translations, theme, prefix, crystal item text, present heads, gifts, luck settings, and tree level requirements, then prints a reload report with warning diagnostics. |
| `/xmastree inspect` | Inspects the tree you are looking at, or the nearest tree if no tree block is targeted. |
| `/xmastree inspect nearest <player>` | Inspects the nearest loaded tree to an online player. |
| `/xmastree inspect <tree-uuid>` | Inspects a specific loaded tree by UUID. |
| `/xmastree test sound first [player]` | Plays the configured first ingredient sound volume for yourself or an online player. |
| `/xmastree test sound repeat [player]` | Plays the configured repeat ingredient sound volume for yourself or an online player. |
| `/xmastree test particle <level> [all\|ambient\|swag\|body] [player]` | Previews configured particle effects for `sapling`, `small_tree`, `tree`, `magic_tree`, or `super_tree`. |
| `/xmastree test refund [tree-uuid\|nearest [player]]` | Dry-runs refund delivery for a tree. This reports the target tree, refund items, and delivery order without editing blocks, inventories, tree data, or config. |
| `/xmastree data backup [trees\|progress\|all]` | Creates timestamped backups in `plugins/X-Mas/backups/`. Defaults to `trees`. |
| `/xmastree data validate` | Reads `trees.yml` and reports missing worlds, invalid IDs, owners, levels, coordinates, requirements, and duplicate locations. |
| `/xmastree data report` | Shows an event wrap-up report with saved/loaded tree counts, owners, level counts, gifts opened, milestone claim counts, legacy trees preserved, and returning-player reward candidates. |
| `/xmastree data migrate-world <from> <to> [dry-run\|apply]` | Counts or rewrites saved tree world names in `trees.yml`. `dry-run` is the default. `apply` creates a backup first, writes the change, and should be followed by a server restart before testing migrated trees. |
| `/xmastree journal [player]` | Shows tree count, highest tree level, gift-open totals, streaks, claimable milestone count, recent tree history, and claimed milestone history. Viewing another player requires `onembxmastree.tree.override`. |
| `/xmastree journal gui [player]` | Opens an informational journal GUI for yourself or, with override permission, another player. |
| `/xmastree milestones [player]` | Shows personal milestone progress and claim state. Viewing another player requires `onembxmastree.tree.override`. |
| `/xmastree milestones test <key> [player]` | Previews personal milestone reward commands without claiming or running them. |
| `/xmastree milestones claim <key>` | Claims a ready personal milestone reward for yourself. |
| `/xmastree community` | Shows server-wide community milestone progress. |
| `/xmastree community test <key>` | Previews community milestone reward commands without claiming or running them. |
| `/xmastree community claim <key> confirm` | Claims a ready community milestone reward. Defaults to staff/admin access and requires `confirm`. |
| `/xmastree debug` | Opens the `status` debug section by default. |
| `/xmastree debug [section\|page]` | Shows debug output for `status`, `commands`, `permissions`, `placeholders`, `config`, or `diagnostics`. Numeric pages `1-6` still work as a legacy shortcut. |
| `/xmastree debug toggle <key> true\|false` | Toggles supported global boolean config keys and reloads the plugin config. |
| `/xmastree end` | Ends the event and sets `core.plugin-enabled` to `false`. |

### Debug sections

The preferred debug syntax is category-based:

| Section | Example | Purpose |
| --- | --- | --- |
| `status` | `/xmastree debug status` | Event state, end date, auto-end, refund state, particles, loaded tree count, and owner count. |
| `commands` | `/xmastree debug commands` | Command list and debug syntax. |
| `permissions` | `/xmastree debug permissions` | All registered `onembxmastree.*` permissions and what they allow. |
| `placeholders` | `/xmastree debug placeholders` | All built-in PlaceholderAPI placeholders plus their descriptions. |
| `config` | `/xmastree debug config` | The current values of the toggleable global config keys. |
| `diagnostics` | `/xmastree debug diagnostics` | Present head, gift pool, and tree-data warning counts plus warning details. |

Numeric compatibility remains available for existing habits and old screenshots:

| Page | Section |
| --- | --- |
| `1` | `status` |
| `2` | `commands` |
| `3` | `permissions` |
| `4` | `placeholders` |
| `5` | `config` |
| `6` | `diagnostics` |

### Debug toggle keys

`/xmastree debug toggle <key> true|false` currently supports:

- `core.plugin-enabled`
- `core.holiday-ends.enabled`
- `core.holiday-ends.resource-back`
- `core.particles-enabled`
- `xmas.luck.enabled`

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `onembxmastree.admin` | `op` | Umbrella permission for all XMas Tree commands and override actions. |
| `onembxmastree.command.status` | `true` | Allows viewing `/xmastree` status output. |
| `onembxmastree.command.help` | `true` | Allows viewing `/xmastree help`. |
| `onembxmastree.command.info` | `true` | Allows viewing `/xmastree info`. |
| `onembxmastree.command.give` | `op` | Allows `/xmastree give <player>`. |
| `onembxmastree.command.gifts` | `op` | Allows `/xmastree gifts`, including list, roll, remove, weight, and spawn actions. |
| `onembxmastree.command.addhand` | `op` | Allows `/xmastree gifts addhand` and `/xmastree addhand`. |
| `onembxmastree.command.reload` | `op` | Allows `/xmastree reload`. |
| `onembxmastree.command.inspect` | `op` | Allows `/xmastree inspect`. |
| `onembxmastree.command.test` | `op` | Allows `/xmastree test sound`, `/xmastree test particle`, and `/xmastree test refund`. |
| `onembxmastree.command.data` | `op` | Allows `/xmastree data backup`, `/xmastree data validate`, `/xmastree data report`, and `/xmastree data migrate-world`. |
| `onembxmastree.command.journal` | `true` | Allows `/xmastree journal` and `/xmastree journal gui`. |
| `onembxmastree.command.milestones` | `true` | Allows `/xmastree milestones`. |
| `onembxmastree.command.milestones.claim` | `true` | Allows `/xmastree milestones claim <key>`. |
| `onembxmastree.command.community` | `true` | Allows `/xmastree community`. |
| `onembxmastree.command.community.claim` | `op` | Allows `/xmastree community claim <key> confirm`. |
| `onembxmastree.command.debug` | `op` | Allows `/xmastree debug [section\|page]`. |
| `onembxmastree.command.debug.toggle` | `op` | Allows `/xmastree debug toggle <key> true\|false`. |
| `onembxmastree.command.end` | `op` | Allows `/xmastree end`. |
| `onembxmastree.tree.override` | `op` | Allows managing other players' trees. |

## Player flow

Players can receive a Christmas Crystal from an admin, or craft one with diamonds around an emerald in a cross shape. Right-click a spruce sapling with the crystal to create a magic tree.

After planting, players right-click the tree with the configured level-up materials. The requirement header is short in chat and includes a hover hint that explains the ingredients must be fed into the tree by right-clicking while holding them. When all requirements for a level are complete, right-click the tree again to grow it. The 2026 fork adds a fifth hardcoded `super_tree` stage after `magic_tree`, giving returning players a new upgrade target without invalidating their preserved legacy trees. Presents spawn around grown trees while the event is enabled.

Opening presents records event progress in `progress.yml`. Players can use `/xmastree journal`, `/xmastree journal gui`, and `/xmastree milestones` to see daily gift claims, total gift claims, streaks, ready milestone rewards, recent tree history, and claimed milestone history. Community progress is visible with `/xmastree community`; staff can preview community milestone rewards with `/xmastree community test <key>` and claim ready community milestones with `/xmastree community claim <key> confirm`.

Personal and community milestone rewards are configured in `config.yml` under `xmas.milestones.*.<key>.reward-commands`. These commands always run from console when the milestone is claimed. Commands can omit a leading slash.

Supported reward command tokens:

- `{player}`: claiming player for personal milestones; staff/admin claimer for community milestones.
- `{claimer}`: same as the command sender who claimed the milestone.
- `{milestone}`: milestone config key, such as `streak_7` or `gifts_250`.
- `{display_name}`: milestone display name.
- `{target}`: configured milestone target.
- `{progress}`: progress value at claim time.

Example reward commands:

```yaml
reward-commands:
 - cmi kit christmas {player}
 - cmi money give {player} 250
 - cmi toast {player} -t:task -icon:EMERALD -text:"{display_name} complete!"
 - cmi broadcast ! <gold>{display_name}</gold> reached: {progress}/{target}
```

The bundled community milestones are:

- `gifts_100`: 100 community gift opens, runs `/xmastree gifts` once.
- `gifts_250`: 250 community gift opens, runs `/xmastree gifts` twice.
- `gifts_500`: 500 community gift opens, runs `/xmastree gifts` three times.

Progress writes are batched briefly during active play and flushed on reload, disable, and progress backup. Use `/xmastree data backup progress` before manual progress edits, `/xmastree data backup all` before larger event maintenance, and `/xmastree data report` for an end-of-event wrap-up summary.

If `core.holiday-ends.resource-back` is enabled, confirmed tree destruction returns the upgrade materials that were actually spent on the tree. The plugin tries to place a chest first, then a barrel, then the player's inventory, and finally drops any overflow at the tree location.

Ingredient accept sounds can be tuned live in `config.yml` under `core.sounds.grow`. Use `0.0` for silent, `0.1` for quiet, `0.5` for half volume, and `1.0` for full volume. `/xmastree reload` applies the new values without a server restart.

Admins can preview those sound values with `/xmastree test sound first` and `/xmastree test sound repeat`. Particle configuration can be previewed with `/xmastree test particle sapling`, `/xmastree test particle small_tree`, `/xmastree test particle tree`, `/xmastree test particle magic_tree`, or `/xmastree test particle super_tree`. Refund delivery can be safely reviewed with `/xmastree test refund`, `/xmastree test refund nearest <player>`, or `/xmastree test refund <tree-uuid>` without changing the tree or giving any items. Tree support checks should use `/xmastree inspect`, which reports owner, UUID, world/chunk state, progression, remaining requirements, refund preview, present timers, and nearby present state.

## Configuration

The plugin writes its files to `plugins/X-Mas`:

- `config.yml` controls event timing, locale, tree limits, gift cooldowns, present skins, gift items, and level-up requirements.
- `trees.yml` stores placed tree data and should be kept when upgrading an existing event.
- `progress.yml` stores present-open totals, daily streak data, and claimed milestone state.
- `translations/locale_en.yml` is the bundled English source of truth for player/admin text, theme colors, and prefixes.
- optional custom translations can be added as `translations/locale_<code>.yml`.

Use modern Paper/Bukkit material names such as `GOLD_INGOT`, `SPRUCE_LOG`, and `PLAYER_HEAD`. Legacy numeric IDs and old material names are skipped to avoid modern Paper exceptions.

Gift entries in `xmas.gifts` can be simple material names:

```yaml
- DIAMOND
- EMERALD:3
```

Gift entries can also be weighted. Existing simple entries automatically count as weight `1`.

```yaml
- item: DIAMOND
  weight: 10
- item: EMERALD:3
  weight: 5
```

Admins can also hold an item and run `/xmastree gifts addhand`. The older `/xmastree addhand` shortcut still works. This saves the exact item as Base64 so custom names, lore, enchantments, and metadata can be used as gifts.

Gift pool maintenance commands:

- `/xmastree gifts list [page]` shows the currently loaded gift rewards, weights, and roll chances.
- `/xmastree gifts roll` previews one weighted random gift selection without giving it to anyone.
- `/xmastree gifts remove <index>` removes the indexed gift from `xmas.gifts`, saves `config.yml`, and reloads the gift pool.
- `/xmastree gifts weight <index> <weight>` converts or updates the indexed gift entry to the weighted format, saves `config.yml`, and reloads the gift pool.

Legacy world-name remapping lives under `migration.world-aliases`. This is useful when an old `trees.yml` was saved in worlds like `general`, `wild`, or `santa`, but the new Paper 26.2 server uses different world names:

```yaml
migration:
 world-aliases:
  general: world
  wild: world
  santa: santa_event
```

The saved coordinates are preserved. If the destination world terrain or world border changed, some legacy tree locations may still need manual cleanup.

Admins can preview or apply a world-name rewrite directly against `trees.yml`:

```text
/xmastree data migrate-world general world
/xmastree data migrate-world general world apply
```

The first command is a dry run. The `apply` command creates a timestamped backup in `plugins/X-Mas/backups/`, rewrites matching saved world names, and should be followed by a server restart before testing migrated legacy trees.

Present head entries in `xmas.presents` should use `textures.minecraft.net` URLs. Old player-name entries are still accepted for compatibility, but `/xmastree reload` and `/xmastree debug diagnostics` warn about them because texture URLs are more predictable on Paper 26.x.

Invalid present head entries are skipped when config loads. This includes blank values, malformed URLs, and non-`textures.minecraft.net` URLs. Invalid gift entries are also skipped and reported in diagnostics.

Per-stage particles live under `xmas.tree-lvl.<stage>.particles`. Particle names should come from the Paper 26.2 `Particle` enum:

[jd.papermc.io/paper/26.2/org/bukkit/Particle.html](https://jd.papermc.io/paper/26.2/org/bukkit/Particle.html)

The config currently supports simple particles and `DUST`.

## MiniMessage

Translation messages, crystal names, crystal lore, command/debug text, and prefixes support MiniMessage:

```yaml
crystal:
 name: <xm-accent>Christmas Crystal</xm-accent>
 lore:
  - <xm-label>Concentrated Christmas Spirit</xm-label>
  - <xm-muted>Use it on a spruce sapling to fill it with magic.</xm-muted>
```

The fork also ships a small semantic pastel tag set for locale files and command output:

- `<xm-text>` main readable text
- `<xm-muted>` softer secondary text
- `<xm-accent>` mint accent text
- `<xm-accent-2>` rose accent text
- `<xm-label>` warm label text
- `<xm-command>` command/path accent text
- `<xm-success>`, `<xm-warning>`, `<xm-error>`, `<xm-info>` status tones

Legacy `&` color codes are still parsed for compatibility when a message does not contain MiniMessage tags.

The active translation path is:

```text
plugins/X-Mas/translations/locale_en.yml
```

`core.locale: en` maps to `locale_en.yml`. A custom file such as `locale_fr.yml` can be loaded by setting `core.locale: fr`.

## Placeholders

PlaceholderAPI is optional. If PlaceholderAPI is installed, X-Mas registers the `onembxmastree` expansion.

PlaceholderAPI requires an underscore after the expansion identifier, so use:

```text
%onembxmastree_event.active%
```

The dotted key after `onembxmastree_` is supported to keep the placeholders readable and namespaced. Underscore variants also work, for example `%onembxmastree_event_active%`.

| Placeholder | Example output | Description |
| --- | --- | --- |
| `%onembxmastree_event.active%` | `true` | Whether the event is currently active. |
| `%onembxmastree_event.active_text%` | `Active` | Human-readable active/inactive state. |
| `%onembxmastree_event.status%` | `In Progress` | Current event status text. |
| `%onembxmastree_event.starts_at%` | `manual` | Start mode. The plugin currently starts from `core.plugin-enabled`, not a scheduled start date. |
| `%onembxmastree_event.ends_at%` | `10-01-2027 03-33-33` | Configured event end date. |
| `%onembxmastree_event.ends_in%` | `263d 7h` | Approximate time until the configured end date, or `disabled` when auto-end is off. |
| `%onembxmastree_event.ends_timestamp%` | `1799552013000` | Event end timestamp in milliseconds. |
| `%onembxmastree_event.auto_end%` | `true` | Whether automatic event ending is enabled. |
| `%onembxmastree_resource.back%` | `true` | Whether resource refunds are enabled. |
| `%onembxmastree_resource.back_text%` | `Yes` | Human-readable refund state. |
| `%onembxmastree_particles.enabled%` | `true` | Whether X-Mas particles are globally enabled. |
| `%onembxmastree_luck.enabled%` | `false` | Whether gift luck chance is enabled. |
| `%onembxmastree_luck.chance%` | `75` | Gift luck chance as a percent. |
| `%onembxmastree_trees.total%` | `14` | Total loaded X-Mas trees. |
| `%onembxmastree_trees.owners%` | `6` | Number of unique loaded tree owners. |
| `%onembxmastree_player.trees%` | `2` | Number of loaded trees owned by the placeholder player. |
| `%onembxmastree_player.highest_tree_level%` | `5` | Highest loaded tree level owned by the placeholder player, as `0-5`. |
| `%onembxmastree_player.highest_tree_level_name%` | `super_tree` | Highest loaded tree level owned by the placeholder player, as a config key. |
| `%onembxmastree_player.highest_tree_level_display%` | `Super Tree` | Highest loaded tree level owned by the placeholder player, as display text. |
| `%onembxmastree_player.gifts_today%` | `6` | Present gifts opened by the placeholder player today. |
| `%onembxmastree_player.gifts_total%` | `25` | Present gifts opened by the placeholder player during the event. |
| `%onembxmastree_player.streak_current%` | `7` | Current daily present-opening streak for the placeholder player. |
| `%onembxmastree_player.streak_best%` | `12` | Best daily present-opening streak for the placeholder player. |
| `%onembxmastree_player.milestones_claimable%` | `1` | Claimable personal milestone count for the placeholder player. |
| `%onembxmastree_community.gifts_opened%` | `100` | Community-wide present gifts opened. |
| `%onembxmastree_version%` | `2.1.0-051` | Loaded plugin version. |

CMI hologram example:

```text
&aX-Mas Event: &f%onembxmastree_event.active_text%
&aEnds in: &f%onembxmastree_event.ends_in%
&aResource back: &f%onembxmastree_resource.back_text%
&aTrees planted: &f%onembxmastree_trees.total%
```

ajLeaderboards placeholder examples:

```text
%onembxmastree_trees.total%
%onembxmastree_trees.owners%
%onembxmastree_player.trees%
```

## Compatibility notes

- Back up `plugins/X-Mas/trees.yml` before upgrading a live server.
- Existing tree records are loaded from the same `trees.yml` format.
- When saved world names no longer match the current server world names, `migration.world-aliases` can remap them without rewriting `trees.yml`.
- Existing present head player-name entries are still accepted, but new configs should prefer Mojang texture URLs.
- The modern jars are compiled with Java 25 bytecode and should be run on Java 25.
- The Paper 26.2 jar is the intended winter 2026 target, and the same jar should be used for forward-compatibility testing on newer 26.x builds.

## Security notes

- Admin and staff access is gated by `onembxmastree.*` permissions.
- Present texture URLs are restricted to `textures.minecraft.net`.
- Gift item Base64 entries are capped before deserialization.
- Config material names are resolved with modern `Material.matchMaterial` and invalid or legacy values are skipped.
- Treat `config.yml` and translation files as trusted admin-controlled files, especially when using MiniMessage click or hover tags.

## Support

Please report bugs, compatibility problems, and upgrade questions in the GitHub issues section:

[github.com/mrfdev/XMasTree/issues](https://github.com/mrfdev/XMasTree/issues)

## Credits

- **MelonCode** - Original developer - [MelonCode](https://github.com/MelonCode)
- **Ghost_chu** - NMS fixes - [Ghost-chu](https://github.com/Ghost-chu)
- **LoneDev6** - Optimization patches - [LoneDev6](https://github.com/LoneDev6)
- **montlikadani** - Hungarian translation - [montlikadani](https://github.com/montlikadani)
- **mrfloris** - 2026 Paper modernization, Java 25 builds, and XMasTree maintenance - [mrfloris](https://github.com/mrfloris)

Original SpigotMC listing:

[spigotmc.org/resources/x-mas-upgradeable-christmas-tree-event.2672](https://www.spigotmc.org/resources/x-mas-upgradeable-christmas-tree-event.2672/)
