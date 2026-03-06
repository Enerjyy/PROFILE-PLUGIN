package com.aurora.profile;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MushroomEating implements Listener {
    private final Profile plugin;
    public MushroomEating(Profile plugin) {
        this.plugin = plugin;
    }
    Random random = new Random();
    @EventHandler
    public void onMushroomEating(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = player.getInventory().getItemInMainHand();



        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (item.getType().equals(Material.RED_MUSHROOM)) {
                if (random.nextInt(4) == 0 && player.getAttribute(Attribute.SCALE).getValue() < 2) {
                    player.getAttribute(Attribute.SCALE).setBaseValue(player.getAttribute(Attribute.SCALE).getValue() + 0.2);
                    item.setAmount(item.getAmount() - 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_BIG_DRIPLEAF_BREAK, 1f, 1f);
                    item.setAmount(item.getAmount() - 1);
                }
            } else if (item.getType().equals(Material.BROWN_MUSHROOM)) {
                if (random.nextInt(4) == 0 && player.getAttribute(Attribute.SCALE).getValue() > 0.5) {
                    if (player.getAttribute(Attribute.SCALE).getValue() == 0.6) player.getAttribute(Attribute.SCALE).setBaseValue(player.getAttribute(Attribute.SCALE).getValue() - 0.1);
                    else {
                        player.getAttribute(Attribute.SCALE).setBaseValue(player.getAttribute(Attribute.SCALE).getValue() - 0.2);
                    }
                    item.setAmount(item.getAmount() - 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_CHARGE, 1f, 1f);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_BIG_DRIPLEAF_BREAK, 1f, 1f);
                    item.setAmount(item.getAmount() - 1);
                }
            }
        }

    }


}
