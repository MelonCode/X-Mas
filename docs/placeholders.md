# XMas Tree Placeholders

PlaceholderAPI is optional. When PlaceholderAPI is installed, XMas Tree registers the `onembxmastree` expansion.

Use an underscore after the expansion identifier:

```text
%onembxmastree_event.active%
```

The key after `onembxmastree_` can use dotted or underscore form. For example, both of these resolve:

```text
%onembxmastree_event.active%
%onembxmastree_event_active%
```

| Placeholder | Example Output | Meaning |
| --- | --- | --- |
| `%onembxmastree_event.active%` | `true` | Whether the event is currently active. |
| `%onembxmastree_event.active_text%` | `Active` | Human-readable active/inactive state. |
| `%onembxmastree_event.status%` | `In Progress` | Current event status text. |
| `%onembxmastree_event.starts_at%` | `manual` | Start mode. The plugin currently starts from `core.plugin-enabled`. |
| `%onembxmastree_event.ends_at%` | `10-01-2027 03-33-33` | Configured event end date. |
| `%onembxmastree_event.ends_in%` | `263d 7h` | Approximate time until the configured end date, or `disabled` when auto-end is off. |
| `%onembxmastree_event.ends_timestamp%` | `1799552013000` | Event end timestamp in milliseconds. |
| `%onembxmastree_event.auto_end%` | `true` | Whether automatic event ending is enabled. |
| `%onembxmastree_resource.back%` | `true` | Whether resource refunds are enabled. |
| `%onembxmastree_resource.back_text%` | `Yes` | Human-readable refund state. |
| `%onembxmastree_particles.enabled%` | `true` | Whether XMas Tree particles are globally enabled. |
| `%onembxmastree_luck.enabled%` | `false` | Whether gift luck chance is enabled. |
| `%onembxmastree_luck.chance%` | `75` | Gift luck chance as a percent. |
| `%onembxmastree_trees.total%` | `14` | Total loaded XMas trees. |
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

## Example CMI Hologram

```text
&aX-Mas Event: &f%onembxmastree_event.active_text%
&aEnds in: &f%onembxmastree_event.ends_in%
&aResource back: &f%onembxmastree_resource.back_text%
&aTrees planted: &f%onembxmastree_trees.total%
```

## Example Leaderboard Values

```text
%onembxmastree_trees.total%
%onembxmastree_trees.owners%
%onembxmastree_player.trees%
%onembxmastree_community.gifts_opened%
```
