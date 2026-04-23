# PatentCraft / 专利工艺

PatentCraft is a Fabric 1.21.1 gameplay mod that adds an item patent system for multiplayer servers.

When a protected item is crafted or upgraded for the first time, the first player becomes the patent owner and receives a patent book. Other players can still craft the item, but protected items cannot be used normally until they are authorized with the matching patent book.

Patent owners can use a lectern UI to create patent books, grant or revoke permission for other players to create patent books, and open-source patents so everyone can use those items. Creative-mode administrators can use the Patent Station to manage the protected item whitelist in-game.

## Features

- First-craft patent registration for protected items.
- Patent books bound to a patented item and its first owner.
- Unauthorized protected items are blocked from normal use.
- Patent book authorization recipe for protected items.
- Each patent book can authorize one item once.
- Lectern patent registry UI.
- Patent book creation permission grants and revokes.
- Open-source patents.
- Creative-only Patent Station whitelist management.
- Support for crafting table output and smithing table netherite upgrades.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.16.0 or newer
- Fabric API
- Java 21

The mod must be installed on both the client and the server.

## Build

```powershell
.\gradlew.bat build
```

The mod jar is produced under `build/libs/`.

## License

MIT
