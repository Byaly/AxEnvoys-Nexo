package com.artillexstudios.axenvoy.listeners;

import com.artillexstudios.axenvoy.envoy.Envoy;
import com.artillexstudios.axenvoy.envoy.Envoys;
import com.artillexstudios.axenvoy.envoy.SpawnedCrate;
import com.artillexstudios.axenvoy.user.User;
import com.nexomc.nexo.api.events.custom_block.NexoBlockBreakEvent;
import com.nexomc.nexo.api.events.custom_block.NexoBlockInteractEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureBreakEvent;
import com.nexomc.nexo.api.events.furniture.NexoFurnitureInteractEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class CollectionListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onNexoBlockInteract(NexoBlockInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        var action = e.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) return;

        var loc = e.getBlock().getLocation();
        handleCrateHit(e.getPlayer(), loc, () -> e.setCancelled(true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onNexoFurnitureInteract(NexoFurnitureInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        var loc = e.getInteractionPoint().getBlock().getLocation();
        handleCrateHit(e.getPlayer(), loc, () -> {
            e.setUseFurniture(Event.Result.DENY);
            e.setUseItemInHand(Event.Result.DENY);
            e.setCancelled(true);
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onNexoBlockBreak(NexoBlockBreakEvent e) {
        var loc = e.getBlock().getLocation();
        handleCrateHit(e.getPlayer(), loc, () -> e.setCancelled(true));
    }

    @EventHandler(ignoreCancelled = true)
    public void onNexoFurnitureBreak(NexoFurnitureBreakEvent e) {
        var base = e.getBaseEntity();
        if (base == null) return;
        var loc = base.getLocation().getBlock().getLocation();
        handleCrateHit(e.getPlayer(), loc, () -> e.setCancelled(true));
    }

    @EventHandler
    public void onPlayerInteractEvent(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() == Material.AIR) return;

        var loc = event.getClickedBlock().getLocation();
        handleCrateHit(event.getPlayer(), loc, () -> {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
        });
    }

    private boolean handleCrateHit(@NotNull Player player, @NotNull Location loc, @NotNull Runnable cancelEvent) {
        for (Envoy envoy : Envoys.getTypes().values()) {
            if (!envoy.isActive()) continue;

            for (SpawnedCrate crate : envoy.getSpawnedCrates()) {
                if (!crate.getFinishLocation().equals(loc)) continue;

                cancelEvent.run();

                var user = User.USER_MAP.get(player.getUniqueId());
                if (user != null && user.canDamage(envoy, crate.getHandle())) {
                    crate.damage(user, envoy);
                    user.addDamageCooldown(
                            crate.getHandle(),
                            crate.getHandle().getConfig().REQUIRED_INTERACTION_COOLDOWN,
                            envoy
                    );
                }
                return true;
            }
        }
        return false;
    }
}
