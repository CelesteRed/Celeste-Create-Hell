# Modrinth listing: Celeste Early Fixes

Use this when creating the Modrinth project for `celeste-early-fixes`. After the first version is approved, switch the pack to it with:

```sh
cd pack
rm mods/celeste-early-fixes-*.jar
packwiz modrinth add <project-slug>
packwiz refresh
```

## Project settings
- **Type:** Mod
- **Name:** Celeste Early Fixes
- **Summary:** Launch fixes for Relauncher-based packs: repairs restarts on NVIDIA + Sodium and picks per-PC Java settings.
- **Loaders:** NeoForge (it is a ModLauncher service jar; no mods.toml)
- **Minecraft:** 1.21.1
- **Environment:** Client required, server unsupported
- **License:** pick one before publishing (MIT is the usual choice for a small utility)
- **Source:** https://github.com/CelesteRed/Celeste-Create-Hell/tree/main/early-fixes
- **Dependencies:** Relauncher (required)
- **Content disclosure:** tick **"Contains AI-generated content"**. The code was written with an AI assistant, and Modrinth's Content Rules §6.1 require the disclosure.

## Description

Celeste Early Fixes runs before Minecraft loads, in packs that use [Relauncher](https://modrinth.com/mod/relauncher) to restart Java with extra settings.

**What it does**
- **Fixes restarts on NVIDIA cards with Sodium.** Sodium replaces the game's command line so the NVIDIA driver doesn't apply its Minecraft profile. Relauncher reads that same command line to restart the game, so the restart would get broken arguments. This puts the real command line back first; the restarted game applies Sodium's workaround again.
- **Chooses Java settings per PC.** On PCs with 8+ CPU threads, 16 GB+ RAM and 8 GB+ given to Minecraft, it switches to Generational ZGC (less stutter). It never overrides a garbage collector you set yourself.
- **Uses the dedicated GPU on laptops.** On laptops with both built-in and dedicated graphics, it sets Windows' graphics preference for Minecraft's Java to "High performance". It does this only if you haven't already chosen one in Settings > Display > Graphics.
- **Warns about memory.** It logs a warning if the memory slider is set too high or too low.

**Why you'd want it:** in Relauncher packs on Windows, NVIDIA + Sodium players otherwise fail to restart, and laptops often run the game on the weak GPU.

**Good to know**
- Everything can be switched off in `config/celeste-early-fixes.properties`.
- Every error is caught. If something goes wrong, the game launches normally.
- It changes the Windows graphics preference only for Minecraft's own Java executable, and only on laptops with two GPUs.
- No data is sent anywhere.
