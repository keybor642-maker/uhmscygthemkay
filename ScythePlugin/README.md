# ✿ Bloom's Scythe — ScythePlugin

Cherry Blossom relic weapon for **mkaymc** on Paper 1.21.x

---

## What It Does

| Feature | Details |
|---|---|
| **Base item** | Mace (uses your scythe texture via custom model data) |
| **Attack style** | Sword cooldown speed (not mace timing) |
| **Enchants** | Sharpness V, Fire Aspect II, Sweeping Edge III, Looting III, Unbreaking III, Mending I |
| **Right-click ability** | **Blossom Burst** — knocks all enemies in 5 block radius back + ignites them + cherry petal explosion particles |
| **Ability cooldown** | 5 seconds |
| **Owner lock** | Only `mkaymc` can hold it — others get blocked with a spooky message |
| **Hit particles** | Purple cherry blossom petals spawn on every melee hit |

---

## Blossom Burst (Right-Click Ability)

- Pushes nearby mobs/players **outward** like Breach knockback — **no Windburst** upward launch
- Deals 6 damage to all targets in 5 block radius
- Sets them on fire for 3 seconds
- Expanding cherry petal ring particle effect
- Sound combo: cherry leaves + sweep attack + totem

---

## Build

```bash
mvn clean package
```
Outputs: `target/ScythePlugin.jar`

Drop into your Paper server's `/plugins/` folder.

---

## Resource Pack — Texture Setup

The scythe uses **Custom Model Data `100001`** on the **Mace** item.

In your resource pack:
```
assets/minecraft/models/item/mace.json
```

Add this override:
```json
{
  "parent": "minecraft:item/handheld",
  "textures": {
    "layer0": "minecraft:item/mace"
  },
  "overrides": [
    { "predicate": { "custom_model_data": 100001 }, "model": "minecraft:item/scythe" }
  ]
}
```

Then place your scythe model at:
```
assets/minecraft/models/item/scythe.json
assets/minecraft/textures/item/scythe.png   ← your SCYTHE.png texture here
```

---

## Commands

| Command | Description |
|---|---|
| `/givescythe` | Gives scythe to mkaymc (op only) |
| `/givescythe <player>` | Gives to specified player (op only) |

---

## Notes

- Built for **Paper 1.21.x** — will not work on Spigot/Bukkit (uses Paper API + Adventure)
- Java 21 required
- The `MACE` item was chosen because it visually accepts the scythe texture via resource pack while keeping sword-speed combat mechanics through the enchantment system
