# XMas Tree Commands

Primary command: `/xmastree`

The old `/xmas` command is not registered by this plugin, leaving it free for a broader event command or another plugin.

## Player Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/xmastree` | `onembxmastree.command.status` | Shows plugin version, event status, auto-end, refund status, tree count, and owner count. |
| `/xmastree help` | `onembxmastree.command.help` | Shows the command list. |
| `/xmastree info` | `onembxmastree.command.info` | Shows a short player guide and the canonical docs link. |
| `/xmastree journal [player]` | `onembxmastree.command.journal` | Shows tree count, highest tree level, gift totals, streaks, claimable milestones, tree history, and claimed milestone history. Viewing another player requires `onembxmastree.tree.override`. |
| `/xmastree journal gui [player]` | `onembxmastree.command.journal` | Opens the journal GUI for yourself or, with override permission, another player. |
| `/xmastree milestones [player]` | `onembxmastree.command.milestones` | Shows personal milestone progress and claim state. Viewing another player requires `onembxmastree.tree.override`. |
| `/xmastree milestones claim <key>` | `onembxmastree.command.milestones.claim` | Claims a ready personal milestone reward for yourself. |
| `/xmastree community` | `onembxmastree.command.community` | Shows server-wide community milestone progress. |

## Gift Management

| Command | Permission | Description |
| --- | --- | --- |
| `/xmastree gifts` | `onembxmastree.command.gifts` | Spawns presents under every loaded XMas tree. |
| `/xmastree gifts spawn` | `onembxmastree.command.gifts` | Explicit form of `/xmastree gifts`. |
| `/xmastree gifts list [page]` | `onembxmastree.command.gifts` | Lists configured gift rewards by index, weight, and roll chance. |
| `/xmastree gifts roll` | `onembxmastree.command.gifts` | Previews one weighted random gift roll without giving it to anyone. |
| `/xmastree gifts remove <index>` | `onembxmastree.command.gifts` | Removes a configured gift reward by list index. |
| `/xmastree gifts weight <index> <weight>` | `onembxmastree.command.gifts` | Sets a positive whole-number roll weight for a gift reward. |
| `/xmastree gifts addhand` | `onembxmastree.command.addhand` | Adds the held item to the gift pool and saves it to `config.yml`. |
| `/xmastree addhand` | `onembxmastree.command.addhand` | Compatibility shortcut for `/xmastree gifts addhand`. |

## Staff and Admin Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/xmastree give <player>` | `onembxmastree.command.give` | Gives an online player a Christmas Crystal. |
| `/xmastree reload` | `onembxmastree.command.reload` | Reloads config, translations, theme, prefix, crystal text, present heads, gifts, luck settings, and tree level requirements. Prints a reload report. |
| `/xmastree inspect` | `onembxmastree.command.inspect` | Inspects the tree you are looking at, or the nearest tree if no tree block is targeted. |
| `/xmastree inspect nearest <player>` | `onembxmastree.command.inspect` | Inspects the nearest loaded tree to an online player. |
| `/xmastree inspect <tree-uuid>` | `onembxmastree.command.inspect` | Inspects a specific loaded tree by UUID. |
| `/xmastree test sound first [player]` | `onembxmastree.command.test` | Previews the configured first accepted ingredient sound. |
| `/xmastree test sound repeat [player]` | `onembxmastree.command.test` | Previews the configured repeat ingredient sound. |
| `/xmastree test particle <level> [all\|ambient\|swag\|body] [player]` | `onembxmastree.command.test` | Previews configured particles for a tree stage. |
| `/xmastree test refund [tree-uuid\|nearest [player]]` | `onembxmastree.command.test` | Dry-runs refund delivery without editing data or giving items. |
| `/xmastree data backup [trees\|progress\|all]` | `onembxmastree.command.data` | Creates timestamped data backups. Defaults to `trees`. |
| `/xmastree data validate` | `onembxmastree.command.data` | Validates `trees.yml` for invalid IDs, owners, levels, locations, worlds, requirements, and duplicate locations. |
| `/xmastree data report` | `onembxmastree.command.data` | Shows an event wrap-up report with tree, gift, milestone, and returning-player summary data. |
| `/xmastree data migrate-world <from> <to> [dry-run\|apply]` | `onembxmastree.command.data` | Reviews or rewrites saved tree world names. `apply` creates a backup first. |
| `/xmastree milestones test <key> [player]` | `onembxmastree.command.test` | Previews personal milestone reward commands without claiming or running them. |
| `/xmastree community test <key>` | `onembxmastree.command.test` | Previews community milestone reward commands without claiming or running them. |
| `/xmastree community claim <key> confirm` | `onembxmastree.command.community.claim` | Claims a ready community milestone reward. Requires explicit `confirm`. |
| `/xmastree debug` | `onembxmastree.command.debug` | Opens the status debug section. |
| `/xmastree debug [section\|page]` | `onembxmastree.command.debug` | Shows debug output for `status`, `commands`, `permissions`, `placeholders`, `config`, or `diagnostics`. Numeric pages `1-6` still work. |
| `/xmastree debug toggle <key> true\|false` | `onembxmastree.command.debug.toggle` | Toggles supported global boolean config keys and reloads the plugin config. |
| `/xmastree end` | `onembxmastree.command.end` | Ends the event and sets `core.plugin-enabled` to `false`. |

`onembxmastree.admin` includes all command permissions and tree override access.
