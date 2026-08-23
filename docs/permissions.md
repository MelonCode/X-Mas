# XMas Tree Permissions

All current permissions use the `onembxmastree` namespace.

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
| `onembxmastree.command.test` | `op` | Allows `/xmastree test sound`, `/xmastree test particle`, `/xmastree test refund`, and reward preview tests. |
| `onembxmastree.command.data` | `op` | Allows `/xmastree data backup`, `/xmastree data validate`, `/xmastree data report`, and `/xmastree data migrate-world`. |
| `onembxmastree.command.journal` | `true` | Allows `/xmastree journal` and `/xmastree journal gui`. |
| `onembxmastree.command.milestones` | `true` | Allows `/xmastree milestones`. |
| `onembxmastree.command.milestones.claim` | `true` | Allows `/xmastree milestones claim <key>`. |
| `onembxmastree.command.community` | `true` | Allows `/xmastree community`. |
| `onembxmastree.command.community.claim` | `op` | Allows `/xmastree community claim <key> confirm`. |
| `onembxmastree.command.debug` | `op` | Allows `/xmastree debug [section\|page]`. |
| `onembxmastree.command.debug.toggle` | `op` | Allows `/xmastree debug toggle <key> true\|false`. |
| `onembxmastree.command.end` | `op` | Allows `/xmastree end`. |
| `onembxmastree.tree.override` | `op` | Allows viewing and managing other players' trees where supported. |

## Migration Note

The old legacy `xmas.admin` permission is not the active admin permission for this fork. Use `onembxmastree.admin` and the granular `onembxmastree.command.*` permissions for the 2026 event.
