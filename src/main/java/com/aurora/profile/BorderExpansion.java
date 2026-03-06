package com.aurora.profile;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;

public class BorderExpansion   implements Listener {
    private final Profile plugin;
    public BorderExpansion(Profile plugin) {
        this.plugin = plugin;
    }
    private TextDisplay riftDisplay;
    private BukkitRunnable displayTask;


    public void startRiftEffect() {

        if (plugin.getHowMuchLevelsNeedNow() == 0) {
            plugin.changeHowMuchLevelsNeedNow(10);
        }
        if (plugin.getHowMuchItemsNeedNow() == 0) {
            plugin.changeHowMuchItemsNeedNow(100);
        }

        spawnDisplay();
        checkingInsideRift();
        new BukkitRunnable() {
            double rotation = 0;

            @Override
            public void run() {
                double radius = 2.0;
                int points = 30;


                if (Math.random() < 0.8) {

                    double radiusOfEnch = 5.0;

                    double angle = Math.random() * 2 * Math.PI;
                    double distance = Math.random() * radiusOfEnch;

                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;

                    Location randomLoc = plugin.getCenter().clone().add(x, 0.1, z);

                    for (int i = 0; i < 3; ++i) {
                        plugin.getCenter().getWorld().spawnParticle(
                                Particle.ENCHANT,
                                randomLoc,
                                5,
                                0.2, 0.2, 0.2,
                                0
                        );
                    }

                }

                plugin.getCenter().getWorld().spawnParticle(
                        Particle.END_ROD,
                        plugin.getCenter().clone().add(0, 0.2, 0),
                        3,
                        0.2, 0.3, 0.2,
                        0
                );

                rotation += 0.05;
            }

        }.runTaskTimer(plugin, 0L, 5L);
    }

    private void spawnDisplay() {

        Location center = plugin.getCenter();
        World world = center.getWorld();

        if (riftDisplay != null && !riftDisplay.isDead()) {
            riftDisplay.remove();
        }

        if (displayTask != null) {
            displayTask.cancel();
        }

        Location displayLoc = center.clone().add(0, 1.5, 0);
        riftDisplay = world.spawn(displayLoc, TextDisplay.class);

        riftDisplay.setBillboard(Display.Billboard.CENTER);
        riftDisplay.setSeeThrough(true);
        riftDisplay.setShadowed(true);
        riftDisplay.setGlowing(false);

        displayTask = new BukkitRunnable() {
            @Override
            public void run() {

                if (riftDisplay == null || riftDisplay.isDead()) {
                    cancel();
                    return;
                }

                riftDisplay.text(MiniMessage.miniMessage().deserialize(
                        "<light_purple>ᴀᴜʀᴏʀᴀ ʙᴀʀʀɪᴇʀ</light_purple>\n" +
                                "<gray>Нынешние границы:</gray>\n" +
                                "<green>Верхний мир:</green> <white>" + plugin.getBorderOfWorld() + "</white>\n" +
                                "<red>Нижний мир:</red> <white>" + plugin.getBorderOfWorldNether() + "</white>\n" +
                                "<light_purple>Энд:</light_purple> <white>" + plugin.getBorderOfWorldEnd() + "</white>\n" +
                                "<gray>До расширения:</gray>\n" +
                                "<aqua>Уровней:</aqua> <white>" + plugin.getHowMuchLevelsNow() + "</white>/" + plugin.getHowMuchLevelsNeedNow() + "\n" +
                                "<aqua>Ресурсов:</aqua> <white>" + plugin.getHowMuchItemsNow() + "</white>/" + plugin.getHowMuchItemsNeedNow()
                ));
            }
        };

        displayTask.runTaskTimer(plugin, 0L, 20L);
    }


    private void checkingInsideRift() {
        Location center = plugin.getCenter();

        double radiusX = 1.5;
        double radiusY = 1.;
        double radiusZ = 1.5;

        new BukkitRunnable() {
            @Override
            public void run() {
                int itemsCount = 0;
                boolean wasItemPicked = false;
                for (Entity entity : center.getWorld().getNearbyEntities(center, radiusX, radiusY, radiusZ)) {
                    if (entity instanceof  Player player) {
                        int currentLevel = player.getLevel();
                        int newLevel = Math.max(0, currentLevel - 2);
                        if (currentLevel - 2 >= 0) {
                            player.setLevel(newLevel);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                            MiniMessage mm = MiniMessage.miniMessage();
                            String message = plugin.getConfig().getString("riftLevels");
                            Component component = mm.deserialize(message);
                            player.sendMessage(component);
                            plugin.changeHowMuchLevelsNow(2);

                        }
                    }
                    if (entity instanceof Item item) {
                        ItemStack stack = item.getItemStack();
                        plugin.changeHowMuchItemsNow(stack.getAmount());
                        item.remove();
                        World world = plugin.getCenter().getWorld();
                        if (!wasItemPicked) {
                            world.playSound(center, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        }
                        wasItemPicked = true;
                    }
                    if (plugin.getHowMuchLevelsNow() >= plugin.getHowMuchLevelsNeedNow()) {
                        changingBorder(150);
                        int difference = plugin.getHowMuchLevelsNeedNow() - plugin.getHowMuchLevelsNow();
                        plugin.changeHowMuchLevelsNow(-1 * plugin.getHowMuchLevelsNow());
                        plugin.changeHowMuchLevelsNow(-1 * difference);
                        plugin.changeHowMuchLevelsNeedNow(100);
                        plugin.getDatabase().addOrUpdateLevelNeed(plugin.getHowMuchLevelsNeedNow());
                        plugin.getDatabase().addOrUpdateLevel(plugin.getHowMuchLevelsNow());
                        plugin.getDatabase().addOrUpdateWorld(plugin.getBorderOfWorld());
                        plugin.getDatabase().addOrUpdateWorldEnd(plugin.getBorderOfWorldEnd());
                        plugin.getDatabase().addOrUpdateWorldNether(plugin.getBorderOfWorldNether());
                    }
                    if (plugin.getHowMuchItemsNow() >= plugin.getHowMuchItemsNeedNow()) {
                        changingBorder(150);
                        int difference = plugin.getHowMuchItemsNeedNow() - plugin.getHowMuchItemsNow();
                        plugin.changeHowMuchItemsNow(-1 * plugin.getHowMuchItemsNow());
                        plugin.changeHowMuchItemsNow(-1 * difference);
                        plugin.changeHowMuchItemsNeedNow(1000);
                        plugin.getDatabase().addOrUpdateItemsNeed(plugin.getHowMuchItemsNeedNow());
                        plugin.getDatabase().addOrUpdateItems(plugin.getHowMuchItemsNow());
                        plugin.getDatabase().addOrUpdateWorld(plugin.getBorderOfWorld());
                        plugin.getDatabase().addOrUpdateWorldEnd(plugin.getBorderOfWorldEnd());
                        plugin.getDatabase().addOrUpdateWorldNether(plugin.getBorderOfWorldNether());
                    }
                }
                plugin.getDatabase().addOrUpdateItems(plugin.getHowMuchItemsNow());
                plugin.getDatabase().addOrUpdateLevel(plugin.getHowMuchLevelsNow());
            }
        }.runTaskTimer(plugin, 0L, 20L);

    }

    private void changingBorder(int newRadius) {
        int nowBorder = plugin.getBorderOfWorld();
        plugin.changeBorderOfWorld(newRadius);
        plugin.changeBorderOfWorldNether(newRadius/8);
        plugin.changeBorderOfWorldEnd(newRadius+150);

        plugin.getDatabase().addOrUpdateWorld(newRadius);
        plugin.getDatabase().addOrUpdateWorldNether(newRadius/8);
        plugin.getDatabase().addOrUpdateWorldEnd(newRadius+150);

        World world = Bukkit.getWorld("world");
        World worldNether = Bukkit.getWorld("world_nether");
        World worldEnd = Bukkit.getWorld("world_the_end");

        WorldBorder borderWorld = world.getWorldBorder();
        WorldBorder borderWorldNether = worldNether.getWorldBorder();
        WorldBorder borderWorldEnd = worldEnd.getWorldBorder();


        borderWorld.setSize(plugin.getBorderOfWorld(), 10);
        borderWorldNether.setSize(plugin.getBorderOfWorldNether(), 10);
        borderWorldEnd.setSize(plugin.getBorderOfWorldEnd(), 10);
    }



}
