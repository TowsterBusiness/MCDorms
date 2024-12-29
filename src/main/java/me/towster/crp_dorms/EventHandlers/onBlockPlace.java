package me.towster.crp_dorms.EventHandlers;

import me.towster.crp_dorms.CRP_Dorms;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class onBlockPlace implements Listener {
    @EventHandler
    public void onBlockPlace (BlockPlaceEvent event) {
        Block blockPlaced = event.getBlock();
        CRP_Dorms.editingPlayers.forEach((player) -> {
            if (!(CRP_Dorms.dormList.get(CRP_Dorms.dormPlayerAllocation.get(player.getUniqueId())).isInDorm(blockPlaced.getLocation().toVector()))) {
                player.sendMessage("That is not part of your dorm!");
                event.setCancelled(true);
            }

            if (blockPlaced instanceof Container) {
                Container containerBlock = (Container) blockPlaced;
                containerBlock.setLock("87*(&^#+)_$%(FDO(#*hF)(DFP(G{ASFO:");
            }

        });
    }
}
