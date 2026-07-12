# XMas Tree Troubleshooting

## First Checks

Run these commands before editing data:

```text
/xmastree
/xmastree info
/xmastree reload
/xmastree debug diagnostics
/xmastree data validate
```

For larger maintenance, back up first:

```text
/xmastree data backup all
```

## The Tree Will Not Grow

Check:

- the event is active
- the player owns the tree
- the tree has enough empty space for the next stage
- the player is holding the required material
- the material name in config is a modern Paper/Bukkit material name

Use:

```text
/xmastree inspect
```

This reports next level, remaining requirements, spent resources, chunk/world state, and whether the next level has room.

## A Legacy Tree Does Not Load

Run:

```text
/xmastree data validate
```

Common causes:

- saved world name no longer exists
- coordinates now point to unloaded or changed terrain
- old material names are invalid on modern Paper
- duplicate saved tree locations

If the world was renamed, configure `migration.world-aliases` or use `/xmastree data migrate-world <from> <to> apply` after creating a backup.

## Presents Do Not Spawn

Check:

- `core.plugin-enabled`
- `xmas.tree-lvl.<stage>.gift-cooldown`
- tree stage
- present head diagnostics from `/xmastree debug diagnostics`
- whether the server was recently reloaded and existing loaded trees need a restart to pick up stage changes

## Present Rewards Look Wrong

Use:

```text
/xmastree gifts list
/xmastree gifts roll
/xmastree debug diagnostics
```

Invalid material names, unsafe present texture URLs, and malformed Base64 gift entries are skipped and reported in diagnostics.

## Refunds Do Not Return Items

Check:

- `core.holiday-ends.resource-back`
- `/xmastree inspect` refund preview
- whether the tree has actually consumed upgrade ingredients

Refund delivery order is:

1. chest
2. barrel
3. player inventory
4. floor drops

Preview without changing anything:

```text
/xmastree test refund
```

## Milestone Rewards Did Not Run

Use:

```text
/xmastree milestones test <key> [player]
/xmastree community test <key>
```

Reward commands run from console when claimed. They should not include a leading slash. Check server logs for whether each command was accepted.

Community milestone claims require:

```text
/xmastree community claim <key> confirm
```

## Placeholder Values Are Empty

Check:

- PlaceholderAPI is installed and enabled
- the placeholder starts with `%onembxmastree_`
- player-specific placeholders are being requested in a player context
- `/xmastree debug placeholders` lists the placeholder

## Config Changes Did Not Apply

Most file edits apply with:

```text
/xmastree reload
```

Restart is recommended for:

- `core.update-speed`
- `core.particles-delay`
- fully consistent tree-stage changes on already loaded trees

## Getting Help

Open an issue with:

- plugin jar version
- Paper version
- Java version
- relevant console errors
- `/xmastree debug diagnostics` output
- `/xmastree data validate` output, if tree data is involved

[github.com/mrfdev/XMasTree/issues](https://github.com/mrfdev/XMasTree/issues)
