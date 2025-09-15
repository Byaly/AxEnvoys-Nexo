package com.artillexstudios.axenvoy.integrations.blocks.impl;

import com.artillexstudios.axenvoy.integrations.blocks.BlockIntegration;
import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public class NexoBlockIntegration implements BlockIntegration {

    @Override
    public void place(String id, Location location) {
        //"nexo:<itemId>"
        String itemId = id.substring("nexo:".length());

        // CustomBlock
        if (NexoBlocks.isCustomBlock(itemId)) {
            NexoBlocks.place(itemId, location);
            return;
        }

        // Furniture
        if (NexoFurniture.isFurniture(itemId)) {
            // Domyślnie: yaw z lokalizacji, osadzenie "na górze" bloku poniżej
            NexoFurniture.place(itemId, location, location.getYaw(), BlockFace.UP);
        }
    }

    @Override
    public void remove(Location location) {
        // Remove first furniture next block
        if (NexoFurniture.remove(location)) return;
        NexoBlocks.remove(location);
    }
}