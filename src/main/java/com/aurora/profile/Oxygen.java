package com.aurora.profile;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.*;

public class Oxygen implements Listener {
    private final Profile plugin;
    private final NamespacedKey oxygenKey;
    public Oxygen(Profile plugin) {
        this.plugin = plugin;
        this.oxygenKey = new NamespacedKey(plugin, "oxygen_bottle");
    }
    Random random = new Random();

    Map<Location, Integer> cauldronTimes = new HashMap<>();

    @EventHandler
    public void onRedstoneChange(BlockRedstoneEvent event) {

        if (event.getNewCurrent() <= 0) return;

        Block redstoneBlock = event.getBlock();


        Location location = redstoneBlock.getLocation();

        Block[] nearby = new Block[] {
                location.clone().add(1, 0, 0).getBlock(),
                location.clone().add(-1, 0, 0).getBlock(),
                location.clone().add(0, 0, 1).getBlock(),
                location.clone().add(0, 0, -1).getBlock()
        };

        for (Block block : nearby) {
            if (block.getType() == Material.WATER_CAULDRON) {

                Location cauldronLoc = block.getLocation();

                if (!cauldronTimes.containsKey(cauldronLoc)) {
                    cauldronTimes.put(cauldronLoc, 60);
                    System.out.println("КОТЕЛ ДОБАВЛЕН: "
                            + cauldronLoc.getBlockX() + " "
                            + cauldronLoc.getBlockY() + " "
                            + cauldronLoc.getBlockZ());
                }
            }
        }
    }

    public void cauldron() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<Location, Integer>> iterator = cauldronTimes.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Location, Integer> entry = iterator.next();
                    Location loc = entry.getKey();
                    int time = entry.getValue();

                    if (!isCauldronOn(loc)) {
                        iterator.remove();
                        continue;
                    }

                    Block block = loc.getBlock();

                    time--;
                    entry.setValue(time);

                    boolean randomBool = random.nextBoolean();
                    double additionalShi;

                    if (randomBool) {
                        if (time > 40) additionalShi = 1;
                        else if (time > 20) additionalShi = 0.8;
                        else additionalShi = 0.5;
                    } else {
                        if (time > 40) additionalShi = 1;
                        else if (time > 20) additionalShi = 0.7;
                        else additionalShi = 0.5;
                    }

                    double random1 = 0.1 + (0.9 - 0.1) * Math.random();
                    double random2 = 0.1 + (0.9 - 0.1) * Math.random();

                    loc.getWorld().spawnParticle(
                            Particle.BUBBLE_POP,
                            loc.clone().add(random1, additionalShi, random2),
                            0
                    );

                    if (time == 40 || time == 20) {
                        if (block.getType() == Material.WATER_CAULDRON) {
                            Levelled data = (Levelled) block.getBlockData();
                            int level = data.getLevel();

                            if (level > 1) {
                                data.setLevel(level - 1);
                                block.setBlockData(data);
                            } else {
                                block.setType(Material.CAULDRON);
                                iterator.remove();
                            }

                            loc.getWorld().playSound(
                                    loc,
                                    Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED,
                                    1,
                                    1
                            );
                        }
                    }

                    if (time <= 0) {
                        block.setType(Material.CAULDRON);
                        loc.getWorld().playSound(
                                loc,
                                Sound.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED,
                                1,
                                1
                        );
                        iterator.remove();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isCauldronUnderYou(Location loc) {
        Location buffer = loc.clone();
        World world = buffer.getWorld();
        if (world == null) return false;

        for (int i = 0; i < 3; i++) {
            buffer = buffer.clone().subtract(0, i, 0);
            Location blockLoc = buffer.getBlock().getLocation();
            if (cauldronTimes.containsKey(blockLoc)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCauldronOn(Location loc) {

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        Location temp1 = loc.clone().add(1, 0, 0);
        Location temp2 = loc.clone().add(-1, 0, 0);
        Location temp3 = loc.clone().add(0, 0, -1);
        Location temp4 = loc.clone().add(0, 0, 1);

        Block block1 = temp1.getBlock();
        Block block2 = temp2.getBlock();
        Block block3 = temp3.getBlock();
        Block block4 = temp4.getBlock();

        if (block1.getBlockPower()>6) return true;
        else if (block2.getBlockPower()>6) return true;
        else if (block3.getBlockPower()>6) return true;
        else if (block4.getBlockPower()>6) return true;

        return false;
    }

    @EventHandler
    public void onBottleFilling(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.GLASS_BOTTLE) return;

        if (!isCauldronUnderYou(player.getLocation())) return;

        int amount = item.getAmount();
        if (amount > 1) {
            item.setAmount(amount - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(ChatColor.AQUA + "Бутылёк с кислородом");
        meta.getPersistentDataContainer().set(oxygenKey, PersistentDataType.BYTE, (byte) 1);
        bottle.setItemMeta(meta);

        player.getInventory().addItem(bottle);

        player.updateInventory();
        player.sendMessage(ChatColor.GREEN + "Ты наполнил бутылку кислородом!");
    }

    @EventHandler
    public void onPlayerUseOxygen(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK
                && action != Action.LEFT_CLICK_AIR && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.GLASS_BOTTLE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta.getPersistentDataContainer().has(oxygenKey, PersistentDataType.BYTE)) {
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        player.sendTitle("Õ", "", 10, 30, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:faahhhhhh",
                                1.0f,
                                1.0f
                        );
                    }
                    else {
                        player.sendTitle("Ò", "", 10, 30, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:meow",
                                1.0f,
                                1.0f
                        );
                    }
                }
                else {
                    if (random.nextBoolean()) {
                        player.sendTitle("Ô", "", 10, 40, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:oldspice",
                                1.0f,
                                1.0f
                        );
                    }
                    else {
                        player.sendTitle("Ñ", "", 10, 30, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:kolokol",
                                1.0f,
                                1.0f
                        );
                    }
                }
            }
            else {
                if (random.nextBoolean()) {
                    if (random.nextBoolean()) {
                        player.sendTitle("Ö", "", 10, 100, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:siren",
                                1.0f,
                                1.0f
                        );
                    }
                    else {
                        player.sendTitle("Ð", "", 10, 40, 20);
                        player.getWorld().playSound(
                                player.getLocation(),
                                "minecraft:goodboy",
                                1.0f,
                                1.0f
                        );
                    }
                }
                else {
                    player.sendTitle("Ó", "", 10, 320, 20);
                    player.getWorld().playSound(
                            player.getLocation(),
                            "minecraft:aurora",
                            1.0f,
                            1.0f
                    );
                }
            }
            item.setItemMeta(null);
        }
    }

}
