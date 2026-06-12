package mc.mkay.scythe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScytheListener implements Listener {

    private static final String OWNER = "mkaymc";
    private final ScythePlugin plugin;

    // Cooldown tracker for the right-click ability (5 seconds)
    private final Map<UUID, Long> abilityCooldowns = new HashMap<>();
    private static final long ABILITY_COOLDOWN_MS = 5000L;

    public ScytheListener(ScythePlugin plugin) {
        this.plugin = plugin;
    }

    // ── Right-click ability: Blossom Burst ─────────────────────────────────────
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.getName().equalsIgnoreCase(OWNER)) return;
        if (!ScytheItem.isScythe(player.getInventory().getItemInMainHand(), plugin)) return;

        // Check cooldown
        long now = System.currentTimeMillis();
        long lastUsed = abilityCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long remaining = (ABILITY_COOLDOWN_MS - (now - lastUsed)) / 1000;

        if (now - lastUsed < ABILITY_COOLDOWN_MS) {
            player.sendActionBar(
                Component.text("✿ Blossom Burst — " + remaining + "s cooldown", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
            );
            return;
        }

        abilityCooldowns.put(player.getUniqueId(), now);
        event.setCancelled(true);

        blossomBurst(player);
    }

    /**
     * BLOSSOM BURST — Cherry Blossom Right-Click Ability
     *
     * Releases a petal nova around the player:
     *  - Launches nearby enemies back (Breach-style knockback, no windburst)
     *  - Deals damage (scales with Sharpness level)
     *  - Spawns cherry blossom + cherry leaves particles
     *  - Ignites hit enemies (Fire Aspect synergy)
     *  - Plays a satisfying sound combo
     */
    private void blossomBurst(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        double radius = 5.0;
        double damage = 6.0; // base — equivalent to diamond sword hit
        double knockbackPower = 1.4;

        // ── Sound ──
        world.playSound(loc, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.5f, 0.8f);
        world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.2f, 0.7f);
        world.playSound(loc, Sound.ITEM_TOTEM_USE, 0.6f, 1.8f);

        // ── Hit nearby entities ──
        for (Entity entity : world.getNearbyEntities(loc, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (entity.equals(player)) continue;
            if (entity instanceof Player other && other.getGameMode() == GameMode.CREATIVE) continue;

            // Knockback vector — pushes away from player (Breach direction, no windburst upward)
            Vector knockback = entity.getLocation().toVector()
                .subtract(loc.toVector())
                .normalize()
                .multiply(knockbackPower);
            knockback.setY(0.35); // slight lift, not windburst

            ((LivingEntity) entity).damage(damage, player);
            entity.setVelocity(knockback);

            // Ignite hit targets (Fire Aspect)
            ((LivingEntity) entity).setFireTicks(60); // 3 seconds

            // Petal particles on each hit entity
            world.spawnParticle(Particle.CHERRY_LEAVES, entity.getLocation().add(0, 1, 0),
                25, 0.4, 0.4, 0.4, 0.05);
        }

        // ── Visual: expanding petal ring ──
        new BukkitRunnable() {
            int tick = 0;
            final double maxRadius = 5.5;

            @Override
            public void run() {
                if (tick > 15) {
                    cancel();
                    return;
                }
                double r = (maxRadius / 15.0) * tick;
                int points = 32;
                for (int i = 0; i < points; i++) {
                    double angle = (2 * Math.PI / points) * i;
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    Location particleLoc = loc.clone().add(x, 0.2, z);

                    world.spawnParticle(Particle.CHERRY_LEAVES, particleLoc,
                        3, 0.1, 0.2, 0.1, 0.02);
                    if (tick % 2 == 0) {
                        world.spawnParticle(Particle.WITCH, particleLoc,
                            1, 0.05, 0.1, 0.05, 0);
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // ── Player feedback ──
        player.sendActionBar(
            Component.text("✿ Blossom Burst!", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false)
        );

        // Ambient cherry particles around player for 1s
        new BukkitRunnable() {
            int t = 0;
            @Override
            public void run() {
                if (t++ > 20) { cancel(); return; }
                world.spawnParticle(Particle.CHERRY_LEAVES,
                    player.getLocation().add(0, 1, 0),
                    8, 0.6, 0.8, 0.6, 0.04);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // ── Sword attack cooldown — fires on every melee hit with scythe ──────────
    // Mace normally ignores attack speed; we enforce sword-speed cooldown feedback
    @EventHandler
    public void onScytheAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!ScytheItem.isScythe(player.getInventory().getItemInMainHand(), plugin)) return;

        // Cherry blossom hit particles on target
        if (event.getEntity() instanceof LivingEntity) {
            Location hitLoc = event.getEntity().getLocation().add(0, 1, 0);
            player.getWorld().spawnParticle(Particle.CHERRY_LEAVES, hitLoc,
                12, 0.3, 0.3, 0.3, 0.05);
            player.getWorld().spawnParticle(Particle.WITCH, hitLoc,
                5, 0.2, 0.2, 0.2, 0.02);
        }
    }

    // ── Owner lock — stop anyone else from using it ──────────────────────────
    @EventHandler
    public void onNonOwnerHold(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (player.getName().equalsIgnoreCase(OWNER)) return;

        // Check the item being switched to
        org.bukkit.inventory.ItemStack newItem =
            player.getInventory().getItem(event.getNewSlot());
        if (!ScytheItem.isScythe(newItem, plugin)) return;

        // Deny and notify
        event.setCancelled(true);
        player.sendMessage(
            Component.text("✿ ", NamedTextColor.LIGHT_PURPLE)
                .append(Component.text("This relic is not yours to wield.", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, true))
        );
        // Spooky sound
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.5f, 1.5f);
    }
}
