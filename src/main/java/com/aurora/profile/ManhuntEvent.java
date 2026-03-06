package com.aurora.profile;

import com.mojang.brigadier.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Strider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.css.RGBColor;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class ManhuntEvent   implements Listener {

    private String eventType;
    private Player playerInEvent;
    private MiniMessage mm = MiniMessage.miniMessage();
    private final Profile plugin;
    public ManhuntEvent(Profile plugin) {
        this.plugin = plugin;
    }
    private Material randomMaterial;
    private String nameOfMaterial;

    private int count = 0;

    private static final Set<Material> ALL_FLOWERS = EnumSet.of(
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.WITHER_ROSE,
            Material.TORCHFLOWER,

            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,

            Material.PITCHER_PLANT,
            Material.PINK_PETALS
    );

    private Set<Material> collectedFlowers = new HashSet<>();
    private Set<DyeColor> collectedDyeShips = new HashSet<>();
    private Set<UUID> uuidOfAlreayDyed = new HashSet<>();
    private List<Player> hunters = new ArrayList<>();

    Map<Material, String> materialNames = new HashMap<>();


    public void scheduleDailyEvent() {


        materialNames.put(Material.JUNGLE_LEAVES, "Большой твердолист");
        materialNames.put(Material.MINECART, "Вагонетка с динамитом");
        materialNames.put(Material.CRYING_OBSIDIAN, "Плачущий обсидиан");
        materialNames.put(Material.ENDER_EYE, "Око Эндера");
        materialNames.put(Material.COBWEB, "Блок паутины");
        materialNames.put(Material.JACK_O_LANTERN, "Светящаяся тыква");
        materialNames.put(Material.WARPED_ROOTS, "Искаженные корни");
        materialNames.put(Material.AZALEA, "Азалия");
        materialNames.put(Material.SWEET_BERRIES, "Светящиеся ягоды");
        materialNames.put(Material.MUSHROOM_STEW, "Грибная похлёбка");

        ZoneId zone = ZoneId.of("Europe/Moscow");
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDateTime nextRun = now.withHour(21).withMinute(20).withSecond(0);
        LocalDateTime preNextRun = now.withHour(20).withMinute(50).withSecond(0);
        LocalDateTime prePastNextRun = now.withHour(21).withMinute(5).withSecond(0);

        if (now.compareTo(nextRun) >= 0) {
            nextRun = nextRun.plusDays(1);
        }
        if (now.compareTo(preNextRun) >= 0) {
            preNextRun = preNextRun.plusDays(1);
        }
        if (now.compareTo(prePastNextRun) >= 0) {
            prePastNextRun = prePastNextRun.plusDays(1);
        }

        long delaySeconds = Duration.between(now, nextRun).getSeconds();
        long delayTicks = delaySeconds * 20;

        long delayForPreRun = Duration.between(now, preNextRun).getSeconds();
        long delayForPreRunTicks = delayForPreRun * 20;

        long delayForPrePastRun = Duration.between(now, prePastNextRun).getSeconds();
        long delayForPrePastRunTicks = delayForPrePastRun * 20;

        TextColor color = TextColor.fromCSSHexString("#7FFFD4");
        TextColor yellowAhh = TextColor.fromCSSHexString("#dbd395");

        Component component = mm.deserialize(plugin.getConfig().getString("event30min"));

        Component message1 = component
                                .append(
                                        Component.text("[?]")
                                                .color(NamedTextColor.DARK_GRAY)
                                                .hoverEvent(
                                                        HoverEvent.showText(
                                                                Component.text("Выживи и выполни задание или будь пойман!\n").color(color)
                                                                        .append(Component.text("Суть ивента: один игрок получает задание, которое он обязан выполнить до истечения времени, остальным игрокам нужно убить цель\n\n").color(NamedTextColor.WHITE))
                                                                        .append(Component.text("Во время ивента на сервере включен keepInventory, это означает, что все останутся при своих ресурсах\n").color(yellowAhh))
                                                        )
                                                ),
                                        Component.text(" "),
                                        Component.text("[!]")
                                                .color(NamedTextColor.RED)
                                                .hoverEvent(
                                                        HoverEvent.showText(
                                                                Component.text("Примечание!\n").color(NamedTextColor.RED)
                                                                        .append(Component.text("Если ваш друг стал целью - вам запрещено его убивать, в противном случае награда будет изъята\n\n").color(NamedTextColor.RED))
                                                                        .append(Component.text("Вы можете отключить участие в ивенте коммандой /event, однако награды за убийство цели вы также не получите").color(yellowAhh))
                                                        )
                                                )
                                );


        Component component2 = mm.deserialize(plugin.getConfig().getString("event15min"));

        Component message2 = component2
                .append(
                        Component.text("[?]")
                                .color(NamedTextColor.DARK_GRAY)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text("Выживи и выполни задание или будь пойман!\n").color(color)
                                                        .append(Component.text("Суть ивента: один игрок получает задание, которое он обязан выполнить до истечения времени, остальным игрокам нужно убить цель\n\n").color(NamedTextColor.WHITE))
                                                        .append(Component.text("Во время ивента на сервере включен keepInventory, это означает, что все останутся при своих ресурсах\n").color(yellowAhh))
                                        )
                                ),
                        Component.text(" "),
                        Component.text("[!]")
                                .color(NamedTextColor.RED)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text("Примечание!\n").color(NamedTextColor.RED)
                                                        .append(Component.text("Если ваш друг стал целью - вам запрещено его убивать, в противном случае награда будет изъята\n\n").color(NamedTextColor.RED))
                                                        .append(Component.text("Вы можете отключить участие в ивенте коммандой /event, однако награды за убийство цели вы также не получите").color(yellowAhh))
                                        )
                                )
                );

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcast(message1);
        }, delayForPrePastRunTicks);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcast(message2);
        }, delayForPreRunTicks);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            startEvent();
            scheduleDailyEvent();
        }, delayTicks);
    }

    public void startEvent() {
        if (plugin.isEventActive()) return;

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

        players.removeIf(player -> {
            return plugin.getEventMap().getOrDefault(player.getName(), true);
        });

        hunters = players;

        if (players.isEmpty() || players.size() == 1) {
            MiniMessage mm = MiniMessage.miniMessage();
            String message = plugin.getConfig().getString("nobodyIsActiveInEvent");
            Component component = mm.deserialize(message);
            Bukkit.broadcast(component);
            return;
        }

        Random random = new Random();
        Player randomOne = players.get(random.nextInt(players.size()));

        playerInEvent = randomOne;

        plugin.eventActiveOn();

        List<String> eventTypes = Arrays.asList("flowers", "strider", "diamondOre", "dyeSheep");

        eventType = eventTypes.get(random.nextInt(eventTypes.size()));

        switch (eventType) {
            case "flowers":
                String messageFlowers = plugin.getConfig().getString("flowers");
                Component componentFlowers = mm.deserialize(messageFlowers);
                playerInEvent.sendMessage(componentFlowers);
                break;
            case "strider":
                String messageStriders = plugin.getConfig().getString("striders");
                Component componentStriders = mm.deserialize(messageStriders);
                playerInEvent.sendMessage(componentStriders);
                break;
            case "diamondOre":
                String messageDiamondOres = plugin.getConfig().getString("diamonds");
                Component componentDiamonds = mm.deserialize(messageDiamondOres);
                playerInEvent.sendMessage(componentDiamonds);
                break;
            case "dyeSheep":
                String messageSheeps = plugin.getConfig().getString("sheeps");
                Component componentSheeps = mm.deserialize(messageSheeps);
                playerInEvent.sendMessage(componentSheeps);
                break;
        }


        ActionBarCoordinates();

        Material[] materials = materialNames.keySet().toArray(new Material[0]);

        randomMaterial = materials[random.nextInt(materials.length)];

        nameOfMaterial = materialNames.get(randomMaterial);

        System.out.println("-------------------------------");
        System.out.println(nameOfMaterial);
        System.out.println(playerInEvent.getName());
        System.out.println(eventType);
        System.out.println("-------------------------------");


        String offHandItemMessage = plugin.getConfig().getString("itemInLeftHand");
        offHandItemMessage = offHandItemMessage
                .replace("<offHand>", nameOfMaterial);
        Component componentOffHand = mm.deserialize(offHandItemMessage);
        playerInEvent.sendMessage(componentOffHand);

        long durationInTicks = 30 * 60 * 20;

        Bukkit.getScheduler().runTaskLater(plugin, this::stopEvent, durationInTicks);

        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
        }

        ZoneId zone = ZoneId.of("Europe/Moscow");
        LocalTime now = LocalTime.now(zone);
        LocalTime nowPlus30 = now.plusMinutes(30);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String nowPlus30Str = nowPlus30.format(formatter);

        TextColor color = TextColor.fromCSSHexString("#7FFFD4");
        TextColor yellowAhh = TextColor.fromCSSHexString("#dbd395");

        String messagePlus = plugin.getConfig().getString("eventNow");
        if (messagePlus != null) {
            messagePlus = messagePlus
                    .replace("<player>", randomOne.getName())
                    .replace("<timeForEvent>", nowPlus30Str);
        }

        Component component3 = mm.deserialize(messagePlus);

        Component message = component3
                .append(
                        Component.text("[?]")
                                .color(NamedTextColor.DARK_GRAY)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text("Выживи и выполни задание или будь пойман!\n\n").color(color)
                                                        .append(Component.text("Суть ивента: один игрок получает задание, которое он обязан выполнить до истечения времени, остальным игрокам нужно убить цель\n\n")).color(NamedTextColor.WHITE)
                                                        .append(Component.text("Во время ивента на сервере включен keepInventory, это означает, что все останутся при своих ресурсах").color(yellowAhh))
                                        )
                                ),
                        Component.text(" "),
                        Component.text("[!]")
                                .color(NamedTextColor.RED)
                                .hoverEvent(
                                        HoverEvent.showText(
                                                Component.text("Примечание!\n").color(NamedTextColor.RED)
                                                        .append(Component.text("Если ваш друг стал целью - вам запрещено его убивать, в противном случае награда будет изъята\n\n").color(NamedTextColor.RED))
                                                        .append(Component.text("Вы можете отключить участие в ивенте коммандой /event, однако награды за убийство цели вы также не получите").color(yellowAhh))
                                        )
                                )
                );



        Bukkit.broadcast(message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String nickname = event.getPlayer().getName();
        if (plugin.getEventMap().get(nickname) == null) {
            plugin.addEventMap(nickname, false);
        }
        if (plugin.getEventMap().get(nickname)) {
            hunters.add(event.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.isEventActive()) return;
        Player deadOne = event.getPlayer();
        if (deadOne.getKiller() == null) return;
        Player killerOne = deadOne.getKiller();
        if (deadOne.equals(playerInEvent)) {
            if (!plugin.getEventMap().get(killerOne.getName())) {
                String messageDeath = plugin.getConfig().getString("eventDeath");
                if (messageDeath != null) {
                    messageDeath = messageDeath
                            .replace("<player>", killerOne.getName());
                }
                Component componentDeath = mm.deserialize(messageDeath);
                Bukkit.broadcast(componentDeath);
                String discordId = plugin.getDiscordLinkMap().get(killerOne.getName());
                int current = plugin.getDonateMap().get(discordId);
                int newOne = current + 150;
                plugin.getDatabase().updateDonate(discordId, newOne);
                plugin.getDonateMap().put(discordId, newOne);
                ItemStack prize = new ItemStack(Material.DEEPSLATE_DIAMOND_ORE, 15);
                killerOne.getInventory().addItem(prize);
                plugin.eventActiveOff();
                stopEvent();
            }
            else return;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitOne = event.getPlayer();
        if (quitOne.equals(playerInEvent)) {
            String messageQuit = plugin.getConfig().getString("eventQuit");
            if (messageQuit != null) {
                messageQuit = messageQuit
                        .replace("<player>", quitOne.getName());
            }
            Component componentQuit = mm.deserialize(messageQuit);
            Bukkit.broadcast(componentQuit);
            stopEvent();
            startEvent();
        }
    }

    private void stopEvent() {
        if (!plugin.isEventActive()) return;
        String message = plugin.getConfig().getString("eventEnd");
        Component component = mm.deserialize(message);
        Bukkit.broadcast(component);
        playerInEvent = null;
        collectedFlowers = new HashSet<>();
        collectedDyeShips = new HashSet<>();
        uuidOfAlreayDyed = new HashSet<>();
        hunters = new ArrayList<>();

        nameOfMaterial = null;
        randomMaterial = null;
        eventType = null;
        collectedFlowers = HashSet.newHashSet(13);
        plugin.eventActiveOff();
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, false);
        }
        count = 0;
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.isEventActive()) return;
        if (event.getPlayer().equals(playerInEvent)) {
            if (eventType.equals("flowers") || eventType.equals("diamondOre")) {
                ItemStack itemStack = event.getPlayer().getInventory().getItemInOffHand();
                Material offHand = itemStack.getType();
                if (offHand.equals(randomMaterial)) {
                    Block block = event.getBlock();
                    Material blockMaterial = block.getType();
                    switch (eventType) {
                        case "flowers":
                            if (ALL_FLOWERS.contains(blockMaterial) && !collectedFlowers.contains(blockMaterial)) {
                                collectedFlowers.add(blockMaterial);
                                count++;
                                String message = plugin.getConfig().getString("flowersCollecting");
                                message = message.replace("<count>", String.valueOf(count));
                                Component component = mm.deserialize(message);
                                event.getPlayer().sendMessage(component);
                                if (count == 13) winningEvent();
                            }
                            break;
                        case "diamondOre":
                            if (!blockMaterial.equals(Material.DEEPSLATE_DIAMOND_ORE)) return;
                            count++;
                            String message = plugin.getConfig().getString("diamondCollecting");
                            message = message.replace("<count>", String.valueOf(count));
                            Component component = mm.deserialize(message);
                            event.getPlayer().sendMessage(component);
                            if (count == 20) winningEvent();
                            break;
                    }
                }

            }
        }
    }

    @EventHandler
    public void onRideStrider(VehicleEnterEvent event) {
        if (!plugin.isEventActive()) return;
        if (!(event.getVehicle() instanceof Strider)) return;
        if (!eventType.equals("strider")) return;

        if (!(event.getEntered() instanceof Player player)) return;
        if (!player.equals(playerInEvent)) return;

        ItemStack offHandStack = player.getInventory().getItemInOffHand();
        if (offHandStack == null || offHandStack.getType() != randomMaterial) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (playerInEvent.isInsideVehicle() && playerInEvent.getVehicle() instanceof Strider) {
                    String message = plugin.getConfig().getString("striderMessage");
                    Component component = mm.deserialize(message);
                    playerInEvent.sendMessage(component);
                    winningEvent();
                }
            }
        }.runTaskLater(plugin, 20 * 30);
    }


    @EventHandler
    public void onDyingSheep(SheepDyeWoolEvent event) {
        if (!plugin.isEventActive()) return;
        if (!event.getPlayer().equals(playerInEvent)) return;
        DyeColor color = event.getColor();
        if (collectedDyeShips.contains(color)) return;
        UUID buffer = event.getEntity().getUniqueId();
        if (uuidOfAlreayDyed.contains(buffer)) return;
        Player player = event.getPlayer();
        ItemStack offHandStack = player.getInventory().getItemInOffHand();
        Material offHand = offHandStack.getType();
        if (offHand.equals(randomMaterial)) {
            count++;
            String message = plugin.getConfig().getString("sheepMessage");
            message = message.replace("<count>", String.valueOf(count));
            Component component = mm.deserialize(message);
            player.sendMessage(component);
            uuidOfAlreayDyed.add(buffer);
            collectedDyeShips.add(color);
            if (count == 7) {
                winningEvent();
            }
        }
    }



    private void winningEvent() {
        String nickname = playerInEvent.getName();
        String discordId = plugin.getDiscordLinkMap().get(nickname);
        int current = plugin.getDonateMap().get(discordId);
        int newOne = current + 150;
        plugin.getDatabase().updateDonate(discordId, newOne);
        plugin.getDonateMap().put(discordId, newOne);
        ItemStack prize = new ItemStack(Material.DEEPSLATE_DIAMOND_ORE, 15);
        playerInEvent.getInventory().addItem(prize);

        String message = plugin.getConfig().getString("eventWonByTarget");
        Component component = mm.deserialize(message);
        playerInEvent.sendMessage(component);

        plugin.eventActiveOff();
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, false);
        }
        count = 0;
    }

    private void ActionBarCoordinates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isEventActive()) {
                    cancel();
                    return;
                }
                for (Player hunter : hunters) {
                    if (hunter.equals(playerInEvent)) continue;
                    Location location = playerInEvent.getLocation();
                    String actionBarShi = plugin.getConfig().getString("actionBar");
                    actionBarShi = actionBarShi
                            .replace("<x>", String.valueOf(location.getBlockX()))
                            .replace("<z>", String.valueOf(location.getBlockZ()));
                    Component component = mm.deserialize(actionBarShi);
                    hunter.sendActionBar(component);
                }
            }
        }.runTaskTimer(plugin, 10L, 20 * 10);
    }





}
