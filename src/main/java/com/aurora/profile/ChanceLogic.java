package com.aurora.profile;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import javax.xml.crypto.Data;
import java.awt.*;
import java.util.*;
import java.util.List;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;

public class ChanceLogic implements Listener {


    private double chanceOfGettingMoreMining;
    private double chanceOfGettingMoreWood;
    Random random = new Random();
    private final Map<Material, Double> blockCoefs = new HashMap<>();
    MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Material, Material> blockEquals = new HashMap<>();
    private final Set<Material> woodTypes = new HashSet<>();
    private final Map<String, Double> chanceIncreaseMining = new HashMap<>();
    private final Map<String, Double> chanceIncreaseWood = new HashMap<>();
    private final Set<Location> placedWood = new HashSet<>();

    private final Profile plugin;
    public ChanceLogic(Profile plugin) {
        this.plugin = plugin;
        blockCoefs.put(Material.STONE, 0.0005);
        blockCoefs.put(Material.TUFF, 0.0005);;
        blockCoefs.put(Material.ANDESITE, 0.0005);
        blockCoefs.put(Material.DEEPSLATE, 0.0005);
        blockCoefs.put(Material.DEEPSLATE_DIAMOND_ORE, 0.5);
        blockCoefs.put(Material.DEEPSLATE_IRON_ORE, 0.1);
        blockCoefs.put(Material.DEEPSLATE_GOLD_ORE, 0.15);
        blockCoefs.put(Material.DEEPSLATE_COPPER_ORE, 0.1);
        blockCoefs.put(Material.DEEPSLATE_COAL_ORE, 0.1);
        blockCoefs.put(Material.DEEPSLATE_EMERALD_ORE, 0.5);
        blockCoefs.put(Material.DEEPSLATE_LAPIS_ORE, 0.1);
        blockCoefs.put(Material.DEEPSLATE_REDSTONE_ORE, 0.01);
        blockCoefs.put(Material.DIAMOND_ORE, 0.5);
        blockCoefs.put(Material.IRON_ORE, 0.1);
        blockCoefs.put(Material.GOLD_ORE, 0.15);
        blockCoefs.put(Material.COPPER_ORE, 0.1);
        blockCoefs.put(Material.COAL_ORE, 0.05);
        blockCoefs.put(Material.EMERALD_ORE, 0.5);
        blockCoefs.put(Material.LAPIS_ORE, 0.01);
        blockCoefs.put(Material.REDSTONE_ORE, 0.01);
        blockCoefs.put(Material.NETHER_GOLD_ORE, 0.01);
        blockCoefs.put(Material.NETHER_QUARTZ_ORE, 0.01);

        blockEquals.put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND);
        blockEquals.put(Material.DIAMOND_ORE, Material.DIAMOND);
        blockEquals.put(Material.DEEPSLATE_IRON_ORE, Material.RAW_IRON);
        blockEquals.put(Material.IRON_ORE, Material.RAW_IRON);
        blockEquals.put(Material.DEEPSLATE_GOLD_ORE, Material.RAW_GOLD);
        blockEquals.put(Material.GOLD_ORE, Material.RAW_GOLD);
        blockEquals.put(Material.DEEPSLATE_COPPER_ORE, Material.RAW_COPPER);
        blockEquals.put(Material.COPPER_ORE, Material.RAW_COPPER);
        blockEquals.put(Material.DEEPSLATE_COAL_ORE, Material.COAL);
        blockEquals.put(Material.COAL_ORE, Material.COAL);
        blockEquals.put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD);
        blockEquals.put(Material.EMERALD_ORE, Material.EMERALD);
        blockEquals.put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_LAZULI);
        blockEquals.put(Material.LAPIS_ORE, Material.LAPIS_LAZULI);
        blockEquals.put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE);
        blockEquals.put(Material.REDSTONE_ORE, Material.REDSTONE);

        woodTypes.add(Material.ACACIA_LOG);
        woodTypes.add(Material.BIRCH_LOG);
        woodTypes.add(Material.CHERRY_LOG);
        woodTypes.add(Material.DARK_OAK_LOG);
        woodTypes.add(Material.JUNGLE_LOG);
        woodTypes.add(Material.MANGROVE_LOG);
        woodTypes.add(Material.OAK_LOG);
        woodTypes.add(Material.PALE_OAK_LOG);
        woodTypes.add(Material.SPRUCE_LOG);
        woodTypes.add(Material.WARPED_STEM);
        woodTypes.add(Material.CRIMSON_STEM);
    }


    private final Map<String, Double> playerMiningIncreasingChance = new HashMap<>();
    private final Map<String, Double> playerWoodIncreasingChance = new HashMap<>();

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (woodTypes.contains(block.getType())) {
            placedWood.add(event.getBlockPlaced().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        String name = player.getName();
        ItemStack itemWithWhatBlockWasBroken = event.getPlayer().getInventory().getItemInMainHand();

        if (blockCoefs.containsKey(block.getType())) {
            if (itemWithWhatBlockWasBroken.containsEnchantment(Enchantment.SILK_TOUCH)) return;
            double coef = blockCoefs.get(block.getType());


            double increasingChance = playerMiningIncreasingChance.getOrDefault(name, 0.0);
            increasingChance += coef;

            double currentCoef = plugin.getCoefsOfMining().getOrDefault(name, 0.0);

            if (random.nextDouble() < increasingChance / 100 && currentCoef < 100) {
                double newCoef = currentCoef + 0.5;
                plugin.addCoefsOfMining(name, newCoef);

                String message = plugin.getConfig().getString("newOreChance");
                message = message
                        .replace("<newCoef>", Double.toString(newCoef));
                Component component = mm.deserialize(message);

                player.sendActionBar(component);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 15f, 1);

                plugin.getDatabase().updateCoefMining(player, newCoef);

                increasingChance = 0;
            }

            playerMiningIncreasingChance.put(name, increasingChance);

            if (random.nextDouble() < currentCoef / 100) {
                event.setDropItems(false);
                ItemStack tool = player.getInventory().getItemInMainHand();
                for (ItemStack drop : block.getDrops(tool)) {
                    drop.setAmount(drop.getAmount() * 2);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }

        if (woodTypes.contains(block.getType())) {
            if (placedWood.contains(block.getLocation())) return;
            double increasingChance = playerWoodIncreasingChance.getOrDefault(name, 0.0);
            increasingChance += 0.15;

            double currentCoef = plugin.getCoefsOfWood().getOrDefault(name, 0.0);

            if (random.nextDouble() < increasingChance / 100 && currentCoef < 100) {
                double newCoef = currentCoef + 0.5;
                plugin.addCoefsOfWood(name, newCoef);

                String message = plugin.getConfig().getString("newWoodChance");
                message = message
                        .replace("<newCoef>", Double.toString(newCoef));
                Component component = mm.deserialize(message);

                player.sendActionBar(component);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 15f, 1);

                plugin.getDatabase().updateCoefWood(player, newCoef);

                increasingChance = 0;
            }

            playerWoodIncreasingChance.put(name, increasingChance);

            if (random.nextDouble() < currentCoef / 100) {
                event.setDropItems(false);
                ItemStack tool = player.getInventory().getItemInMainHand();
                for (ItemStack drop : block.getDrops(tool)) {
                    drop.setAmount(drop.getAmount() * 2);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);
                }
            }
        }
    }





}
