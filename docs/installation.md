# XMas Tree Installation and Building

## Compatibility

| Requirement | Value |
| --- | --- |
| Server | Paper 26.2 |
| Java runtime | Java 25 or newer compatible runtime |
| Build target | Java 25 bytecode |
| Compile API | Paper API `26.2.build.29-alpha` |
| Plugin API version | `26.2` |
| Optional dependency | PlaceholderAPI `2.12.3-DEV-266` or compatible build |

## Install

1. Stop the Paper server.
2. Back up `plugins/X-Mas` and relevant world folders.
3. Remove old XMas Tree jars from the top-level `plugins` folder.
4. Copy the current jar into `plugins`.
5. Start the server.
6. Check console for startup warnings.
7. Run `/xmastree`, `/xmastree info`, and `/xmastree debug diagnostics`.

Current jar name:

```text
1MB-XMas-2026-v2.1.0-051-v25-26.2.jar
```

## Updating From an Older Event

Preserve:

- `plugins/X-Mas/trees.yml`
- `plugins/X-Mas/config.yml`
- `plugins/X-Mas/progress.yml`, if an active event already has progress
- `plugins/X-Mas/translations/locale_en.yml`, if custom text was edited

Before testing live data:

```text
/xmastree data backup all
/xmastree data validate
/xmastree debug diagnostics
```

If saved world names changed between old and new servers, configure `migration.world-aliases` or use `/xmastree data migrate-world`.

## Building

Requirements:

- JDK 25
- Gradle
- Centralized Paper 26.2 cache at `/Users/floris/Projects/Codex/servers/cache/Paper-26.2`
- PlaceholderAPI build `266` in the centralized Paper 26.2 cache

Build and copy the release jar into `libs/`:

```bash
gradle clean buildAllJars
```

Build only the jar:

```bash
gradle jar
```

Print build metadata:

```bash
gradle printBuildConfig
```

Optional deprecated API lint pass:

```bash
gradle clean compileJava -PlintDeprecatedApi=true
```

End users do not need a local `servers/` folder. Local test server folders are ignored development data.

## Support

Report bugs and upgrade issues here:

[github.com/mrfdev/XMasTree/issues](https://github.com/mrfdev/XMasTree/issues)
