# XMas Tree Integrations

## PlaceholderAPI

PlaceholderAPI is optional. When installed, XMas Tree registers the `onembxmastree` expansion for holograms, scoreboards, menus, and leaderboard displays.

See [placeholders.md](placeholders.md) for the complete placeholder table.

## CMI

CMI is not required, but it is useful for milestone rewards, holograms, toasts, and economy commands.

Milestone reward commands run from console and can include CMI commands:

```yaml
reward-commands:
 - cmi kit christmas {player}
 - cmi money give {player} 250
 - cmi toast {player} -t:task -icon:EMERALD -text:"{display_name} complete!"
```

CMI holograms can display PlaceholderAPI values:

```text
&aX-Mas Event: &f%onembxmastree_event.active_text%
&aEnds in: &f%onembxmastree_event.ends_in%
&aTrees planted: &f%onembxmastree_trees.total%
```

## LuckPerms

Use LuckPerms or another permissions plugin to assign granular permissions:

- player groups usually receive the default `true` permissions
- staff groups can receive command-specific admin permissions
- full admins can receive `onembxmastree.admin`

The modern namespace is `onembxmastree.*`.

## ajLeaderboards and Scoreboards

The plugin exposes totals through PlaceholderAPI, which can be used by leaderboard or scoreboard plugins that support PlaceholderAPI:

```text
%onembxmastree_trees.total%
%onembxmastree_trees.owners%
%onembxmastree_player.trees%
%onembxmastree_community.gifts_opened%
```

## Multiverse and World Managers

XMas Tree can load legacy tree records across renamed worlds with `migration.world-aliases`. If a world manager changes world names between event years, use `/xmastree data validate` and `/xmastree data migrate-world` before opening the event.

## Paper API

This fork targets Paper 26.2 and Java 25. Particle names, material names, and plugin metadata are documented for that environment.
