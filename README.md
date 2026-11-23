# Celeste Create Hell

The Celeste Create Hell modpack (Minecraft 1.21.1, NeoForge 21.1.267). Players' installs **update themselves from this repo every time they launch**, so a fix pushed here reaches everyone on their next launch, with no new Modrinth upload and no reinstall.

## How auto-updating works

```
Modrinth App starts the game
  └─ Relauncher (mod) reads config/relauncher/config.cfg and restarts Java with -javaagent:unsup.jar
       ├─ celeste-early-fixes runs first: repairs the restart arguments and adds per-machine tweaks
       └─ restarted Java: unsup syncs the instance with pack/ on GitHub (usually a few seconds),
          then Minecraft loads the updated mods
```

| Piece | What it does |
|---|---|
| `pack/` | The modpack in [packwiz](https://packwiz.infra.link/) format. unsup reads `pack/pack.toml` from the `main` branch. |
| `launcher/unsup.jar`, `launcher/unsup.ini` | The updater ([unsup](https://modrinth.com/mod/unsup) 1.2.8, from Modrinth) and its settings: pack address, Celeste colours and icon, `behavior=semi`. They sit in each player's instance folder. They are **not** part of `pack/`, so a bad push can never break the updater's own settings. |
| `pack/config/relauncher/config.cfg` | Tells [Relauncher](https://modrinth.com/mod/relauncher) to restart Java with unsup attached. The Modrinth App can't set Java arguments from a modpack, so this is what makes unsup run. |
| `early-fixes/` | Source of `celeste-early-fixes`, our own small jar (see below). The built jar lives in `pack/mods/`. |
| `tools/build_mrpack.py` | Builds the `.mrpack` new players import. |

`behavior=semi` means updates install without asking. unsup only asks the player when an update would overwrite a file they edited themselves. Files that aren't part of the pack (a player's own extra mods, worlds, options.txt) are never touched. unsup only deletes files it installed.

## Making a quick fix

You need [packwiz](https://packwiz.infra.link/installation/). Run commands from `pack/`.

```sh
cd pack
packwiz modrinth add <slug-or-url>      # add a mod from Modrinth
packwiz curseforge add <slug-or-url>    # add a mod from CurseForge
packwiz update <mod>                    # update one mod (packwiz update --all for everything)
packwiz remove <mod>                    # remove a mod
# editing a config: just edit the file under pack/config/
packwiz refresh                         # ALWAYS run before committing: rebuilds index.toml hashes
```

Then bump `version` in `pack/pack.toml`, commit and push to `main`. Players get it on their next launch.

GitHub caches each raw file for up to 5 minutes. Someone who launches in the first few minutes after a push can get a mix of old and new files, and see an "Updater error". Pressing **OK** starts the game on the previous version, and the next launch picks up the update. So wait ~5 minutes after pushing before you test.

Rules that keep players safe:
- **Never push without `packwiz refresh`.** A stale `index.toml` makes unsup fail the update.
- **Be careful with `config/relauncher/config.cfg`, `relauncher.pw.toml` and `celeste-early-fixes-*.jar`.** They run *before* unsup. If a change there stops the restart, unsup never runs, so it can't download the fix. Test those on your own instance first.
- Mark mods that must not be on players' PCs with `side = "server"` in their `.pw.toml`, and client-only mods with `side = "client"`.

## celeste-early-fixes

A tiny jar in `pack/mods/`. It isn't a normal mod: it hooks into the launch before Minecraft loads.

1. **Sodium + NVIDIA restart fix.** On NVIDIA cards Sodium overwrites the process command line, so the graphics driver doesn't apply its Minecraft profile. Relauncher reads that same command line to restart the game, so the restarted game would get garbage arguments. The jar puts the real command line back after the early loading window has made its OpenGL context and before Relauncher reads it. The restarted game applies Sodium's workaround again by itself.
2. **Restarts the game through Relauncher itself**, with Relauncher's config arguments (the unsup javaagent) plus per-machine tweaks. It runs from ModLauncher's `onLoad`, which always comes before Relauncher's own restart. It deliberately doesn't register as a Relauncher SPI provider: Relauncher would load it through a `URLClassLoader`, which on Windows locks the jar for the whole session (the original process stays alive while the restarted game runs), so unsup could never update it. The per-machine tweaks:
   - Switches to **Generational ZGC** (less stutter) on machines with 8+ CPU threads, 16 GB+ RAM and 8 GB+ given to Minecraft. It never overrides a collector the player set; two collectors would stop Java from starting.
   - On **laptops with integrated + dedicated graphics**, sets Windows' per-app GPU choice for Minecraft's Java to "high performance". It only does this when both kinds of GPU are present and the player hasn't already chosen one in *Settings > Display > Graphics*.
   - Logs a warning when the memory slider is too high (over 75% of RAM) or too low (under 6 GB).
   - It doesn't change memory (`-Xmx`): Relauncher puts extra arguments before the launcher's, so the launcher's value always wins.

Players can turn each part off in `config/celeste-early-fixes.properties`. The jar catches every error and falls back to launching normally.

Rebuild after changing the source (needs JDK 17+), then refresh the pack:

```powershell
powershell -ExecutionPolicy Bypass -File early-fixes\build.ps1
cd pack; packwiz refresh
```

Bump `$Version` in `build.ps1` for each release, so the jar's file name changes and unsup swaps it.

## New players

1. Download the latest `Celeste-Create-Hell-*.mrpack` from [Releases](https://github.com/CelesteRed/Celeste-Create-Hell/releases).
2. Modrinth App → **Add an instance** → **Import from file** → pick the `.mrpack`.
3. Launch. From then on it updates itself.

To build a new `.mrpack`: `python3 tools/build_mrpack.py` → `dist/`. An old `.mrpack` still works, because unsup brings it up to date on first launch.

## Server

The pack also lists server-only mods (`side = "server"`). The server can sync from the same pack: put `unsup.jar` and an `unsup.ini` (same `source=`, but `force_env=server` and no `[branding]`) in the server folder, then add `-javaagent:unsup.jar` to the Pterodactyl startup command. That isn't set up yet.

## Official server

The live server runs on wings1 (Pterodactyl server `235d0353`). Its "hellish" balance lives in three places:

- `config/create-server.toml` and the other `*-server.toml` / `*-common.toml` files. `pack/config/` holds the same copies, so singleplayer matches the server.
- World datapacks in `world/datapacks/`; copies are in `server/datapacks/`:
  - `CCH4.2.zip`: Create Rebalanced
  - `CAR3.0.zip`: Create Aero Rebalanced
  - `CGunsmithCCHRebalance1.0.zip`: no mass bullet crafting
  - `LetsDoVineryRebalanced2.0.zip`, `NoMoreTemplateCrafting.zip`, `QuickFixes`, `HarkenOre`, `HigherHeights`
  - `ZZ-CCH-Reforged-Balance/`: fixes recipes in the packs above that broke on 1.21.1, and balances the new mods' content
- Server-only mods: antixray, reducedores, reducedloot, Xaero's map server utils, CPM, plus the `side = "server"` mods in `pack/mods/`.

## Notes

- Every mod, resource pack, shader and datapack in `pack/` is downloaded from Modrinth. The one exception is `celeste-early-fixes` (our own jar). The only other bundled files are configs, the emotes in `emotes/`, and the shader settings `.txt` files.
- `datapacks/` holds three world datapacks. `moonlight-global-datapacks/` holds No Void Structures, which Moonlight loads into every world. It replaces the "mod" version of No Void Structures, which only existed because the Modrinth App wraps datapacks as mods.
- Server-only mods that aren't on Modrinth (Duplicationless and Fluidium from CurseForge, plus Starsoul's own server mods) aren't in the pack. Add them to the server by hand.
