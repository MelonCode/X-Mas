# XMas Tree Player Guide

XMas Tree is the 1MoreBlock Christmas tree event plugin. You plant a magical spruce sapling, feed it ingredients, grow it through several stages, and collect presents that appear below the tree during the event.

Returning players can keep preserved trees from previous years where the server data still has them. Winter 2026 also adds a `super_tree` level so old trees can become useful again instead of being left behind.

## Quick Start

1. Get a Christmas Crystal during the event.
2. Place a spruce sapling where you want your tree.
3. Right-click the sapling while holding the Christmas Crystal.
4. Right-click your tree with the ingredients shown in chat.
5. When all ingredients are complete, right-click the tree again to grow it.
6. Check below grown trees for presents and right-click presents to claim rewards.
7. Use `/xmastree journal` and `/xmastree milestones` to track your event progress.

## Commands

| Command | What It Does |
| --- | --- |
| `/xmastree` | Shows the current plugin/event status. |
| `/xmastree help` | Shows the available command list. |
| `/xmastree info` | Shows a short in-game event guide and the canonical docs link. |
| `/xmastree journal` | Shows your trees, gift totals, streaks, milestone count, and recent tree/milestone history. |
| `/xmastree journal gui` | Opens the journal GUI. |
| `/xmastree milestones` | Shows your personal milestone progress and claim state. |
| `/xmastree milestones claim <key>` | Claims a ready personal milestone reward. |
| `/xmastree community` | Shows community-wide gift milestone progress. |

Most admin commands are hidden behind staff permissions and are documented in the technical command reference.

## Permissions and Rank Requirements

The player-facing commands are available by default unless the server changes permissions:

| Permission | Default | Allows |
| --- | --- | --- |
| `onembxmastree.command.status` | Everyone | `/xmastree` |
| `onembxmastree.command.help` | Everyone | `/xmastree help` |
| `onembxmastree.command.info` | Everyone | `/xmastree info` |
| `onembxmastree.command.journal` | Everyone | `/xmastree journal` and `/xmastree journal gui` |
| `onembxmastree.command.milestones` | Everyone | `/xmastree milestones` |
| `onembxmastree.command.milestones.claim` | Everyone | `/xmastree milestones claim <key>` |
| `onembxmastree.command.community` | Everyone | `/xmastree community` |

If a command does not work for your rank, ask staff whether the event is active and whether that command is enabled for your group.

## Rewards, Costs, Limits, and Cooldowns

Trees cost the ingredients shown in chat. Hold the listed ingredient and right-click the tree to feed it in. If more than one of an ingredient is needed, repeat until the counter is complete.

Each account can own a limited number of XMas trees. The default limit is `3`, but server staff can change it.

Presents can appear below grown trees while the event is active. Present rewards come from the server's configured gift pool, so exact rewards may change between years.

Milestones are one-time claims. You can build progress by opening presents:

- daily gift milestones reward activity on one server-local day
- streak milestones reward opening gifts on consecutive days
- total gift milestones reward event-wide gift openings
- community milestones reward the whole server when enough presents are opened by everyone

After the event, staff may allow trees to be broken down. If resource refunds are enabled, the ingredients you actually spent on that tree are returned by chest, barrel, inventory, or floor drops if needed.

## Important Notes

- Use a spruce sapling for new trees.
- Tree growth needs enough empty space around the tree.
- Do not break random leaves to remove a tree; cut the log when the event allows cleanup.
- If you had a tree from a previous year, staff may preserve it and may give you another crystal for the new event.
- `/xmastree info` is the best in-game reminder if you forget the flow.

## Placeholders

Players normally do not type placeholders directly, but the server may use them in holograms, scoreboards, or menus. Examples include:

- `%onembxmastree_event.active_text%`
- `%onembxmastree_event.ends_in%`
- `%onembxmastree_player.trees%`
- `%onembxmastree_player.gifts_today%`
- `%onembxmastree_player.streak_current%`
- `%onembxmastree_community.gifts_opened%`

## Related Features

XMas Tree can be shown in CMI holograms, scoreboards, PlaceholderAPI-aware menus, and community event displays. Staff may also connect milestone rewards to kits, money, toasts, broadcasts, or extra present spawns.

## Technical Documentation

Technical reference: [README](../README.md)

Canonical public docs: [docs.1moreblock.com/custom-server-plugins/xmas-tree/](https://docs.1moreblock.com/custom-server-plugins/xmas-tree/)
