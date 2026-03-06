package com.aurora.profile;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Profile extends JavaPlugin {

    private ChanceLogic chanceLogic;

    private Database database;
    public Database getDatabase() {
        return database;
    }

    private ManhuntEvent manhuntEvent;
    public ManhuntEvent getManhuntEvent() {
        return manhuntEvent;
    }
    private ManhuntEventCommand manhuntEventCommand;
    public ManhuntEventCommand getManhuntEventCommand() {
        return manhuntEventCommand;
    }
    private double chance;
    public double getChance() {
        return chance;
    }
    private Discord discord;
    public Discord getDiscord() {
        return discord;
    }
    private Network network;
    public Network getNetwork() {
        return network;
    }
    private StatisticChanging statisticChanging;
    public StatisticChanging getStatisticChanging() {
        return statisticChanging;
    }
    private MineAndDiscConnecting mineAndDiscConnecting;
    public MineAndDiscConnecting getMineAndDiscConnecting() {
        return mineAndDiscConnecting;
    }
    private DonateLogic donateLogic;
    public DonateLogic getDonateLogic() {
        return donateLogic;
    }
    private BorderExpansion borderExpansion;
    public BorderExpansion getBorderExpansion() {
        return borderExpansion;
    }
    private MushroomEating mushroomEating;
    public MushroomEating getMushroomEating() {
        return mushroomEating;
    }
    private Oxygen oxygen;
    public Oxygen getOxygen() {
        return oxygen;
    }
    private DiscordDayLogin discordDayLogin;
    public DiscordDayLogin getDiscordDayLogin() {
        return discordDayLogin;
    }

    private Map<String, Double> coefsOfMining = new HashMap<>();
    public Map<String, Double> getCoefsOfMining() {
        return coefsOfMining;
    }
    public void addCoefsOfMining(String nickname, double coef) {
        coefsOfMining.put(nickname, coef);
    }

    private Map<String, Double> coefsOfWood = new HashMap<>();
    public Map<String, Double> getCoefsOfWood() {
        return coefsOfWood;
    }
    public void addCoefsOfWood(String nickname, double coef) {
        coefsOfWood.put(nickname, coef);
    }
    private Map<String, String> discordLinkMap = new HashMap<>();
    public Map<String, String> getDiscordLinkMap() {
        return discordLinkMap;
    }
    public void addDiscordLinkMap(String name, String discord) {
        discordLinkMap.put(name, discord);
    }

    private Map<String, String> minecraftLinkMap = new HashMap<>();;
    public Map<String, String> getMinecraftLinkMap() {
        return minecraftLinkMap;
    }
    public void addMinecraftLinkMap(String discord, String name) {
        minecraftLinkMap.put(discord, name);
    }

    private Map<String, String> profileGifs = new HashMap<>();
    public Map<String, String> getProfileGifs() {
        return profileGifs;
    }
    public void addProfileGifs(String discordId, String url) {
        profileGifs.put(discordId, url);
    }
    private Map<String, String> timePlayeds = new HashMap<>();
    public Map<String, String> getTimePlayeds() {
        return timePlayeds;
    }
    public void addTimePlayeds(String nickname, String time) {
        timePlayeds.put(nickname, time);
    }
    private Map<String, String> deaths = new HashMap<>();
    public Map<String, String> getDeaths() {
        return deaths;
    }
    public void addDeaths(String nickname, String deathsCount) {
        deaths.put(nickname, deathsCount);
    }
    private Map<String, String> eventsWon = new HashMap<>();
    public Map<String, String> getEventsWon() {
        return eventsWon;
    }
    public void addEventsWon(String nickname, String eventsCount) {
        deaths.put(nickname, eventsCount);
    }
    private Map<String, Integer> donateMap = new HashMap<>();
    public Map<String, Integer> getDonateMap() {
        return donateMap;
    }

    private Map<String, Boolean> eventMap = new HashMap<>();
    public Map<String, Boolean> getEventMap() {
        return eventMap;
    }
    public void addEventMap(String nickname, boolean isActive) {
        eventMap.put(nickname, isActive);
    }


    private Map<String, Set<String>> upVotes = new HashMap<>();
    public Map<String, Set<String>> getUpVotes() {
        return upVotes;
    }
    public void addUpVotes(String whoGetting, String whoTrying) {
        upVotes.computeIfAbsent(whoGetting, k -> new HashSet<>()).add(whoTrying);
    }
    public void removeUpVotes(String whoGetting, String whoTrying) {
        upVotes.getOrDefault(whoGetting, new HashSet<>()).remove(whoTrying);
    }
    public boolean hasUpVoted(String whoGetting, String whoTrying) {
        return upVotes.getOrDefault(whoGetting, new HashSet<>()).contains(whoTrying);
    }
    public int getUpVotedCount(String whoGetting, String whoTrying) {
        return upVotes.getOrDefault(whoGetting, new HashSet<>()).size();
    }

    private Map<String, Set<String>> downVotes = new HashMap<>();
    public Map<String, Set<String>> getDownVotes() {
        return downVotes;
    }
    public void addDownVotes(String whoGetting, String whoTrying) {
        downVotes.computeIfAbsent(whoGetting, k -> new HashSet<>()).add(whoTrying);
    }
    public void removeDownVotes(String whoGetting, String whoTrying) {
        downVotes.getOrDefault(whoGetting, new HashSet<>()).remove(whoTrying);
    }
    public boolean hasDownVoted(String whoGetting, String whoTrying) {
        return downVotes.getOrDefault(whoGetting, new HashSet<>()).contains(whoTrying);
    }
    public int getDownVotedCount(String whoGetting, String whoTrying) {
        return downVotes.getOrDefault(whoGetting, new HashSet<>()).size();
    }

    public boolean hasVoted(String whoGetting, String whoTrying) {
        boolean voted;
        if (downVotes.getOrDefault(whoGetting, new HashSet<>()).contains(whoTrying) || upVotes.getOrDefault(whoGetting, new HashSet<>()).contains(whoTrying)) return true;
        return false;
    }

    private Map<String, Integer> worldBorders = new HashMap<>();
    public Map<String, Integer> getWorldBroders() {
        return worldBorders;
    }
    public void addWorldBorder(String world, int radius) {
        worldBorders.put(world, radius);
    }

    private Location center;
    public Location getCenter() {
        return center;
    }




    private int howMuchItemsNow;
    public int getHowMuchItemsNow() {
        return howMuchItemsNow;
    }
    public void changeHowMuchItemsNow(int changing) {
        howMuchItemsNow += changing;
    }
    public void setHowMuchItemsNow(int set) {
        howMuchItemsNow = set;
    }
    private int howMuchLevelsNow;
    public int getHowMuchLevelsNow() {
        return howMuchLevelsNow;
    }
    public void changeHowMuchLevelsNow(int changing) {
        howMuchLevelsNow += changing;
    }
    public void setHowMuchLevelsNow(int set) {
        howMuchLevelsNow = set;
    }

    private int howMuchItemsNeedNow;
    public int getHowMuchItemsNeedNow() {
        return howMuchItemsNeedNow;
    }
    public void changeHowMuchItemsNeedNow(int changing) {
        howMuchItemsNeedNow += changing;
    }
    public void setHowMuchItemsNeedNow(int set) {
        howMuchItemsNeedNow = set;
    }
    private int howMuchLevelsNeedNow;
    public int getHowMuchLevelsNeedNow() {
        return howMuchLevelsNeedNow;
    }
    public void changeHowMuchLevelsNeedNow(int changing) {
        howMuchLevelsNeedNow += changing;
    }
    public void setHowMuchLevelsNeedNow(int set) {
        howMuchLevelsNeedNow = set;
    }


    private int borderOfWorld;
    public int getBorderOfWorld() {
        return borderOfWorld;
    }
    public void changeBorderOfWorld(int changing) {
        borderOfWorld += changing;
    }
    public void setBorderOfWorld(int set) {
        borderOfWorld = set;
    }
    private int borderOfWorldNether;
    public int getBorderOfWorldNether() {
        return borderOfWorldNether;
    }
    public void setBorderOfWorldNether(int set) {
        borderOfWorldNether = set;
    }
    public void changeBorderOfWorldNether(int changing) {
        borderOfWorldNether += changing;
    }
    private int borderOfWorldEnd;
    public int getBorderOfWorldEnd() {
        return borderOfWorldEnd;
    }

    public void setBorderOfWorldEnd(int set) {
        borderOfWorldEnd = set;
    }
    public void changeBorderOfWorldEnd(int changing) {
        borderOfWorldEnd += changing;
    }

    private boolean eventActive;
    public boolean isEventActive() {
        return eventActive;
    }
    public void eventActiveOn() {
        eventActive = true;
    }
    public void eventActiveOff() {
        eventActive = false;
    }

    private JDA jda;
    public JDA getJda() {
        return jda;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        network = new Network(getDataFolder());
        database = new Database(this);

        database.connect(getDataFolder().getAbsolutePath());
        database.loadEverything();

        statisticChanging = new StatisticChanging(this);
        mineAndDiscConnecting = new MineAndDiscConnecting(this);
        donateLogic = new DonateLogic(this);
        manhuntEvent = new ManhuntEvent(this);
        manhuntEventCommand = new ManhuntEventCommand(this);
        borderExpansion = new BorderExpansion(this);
        mushroomEating = new MushroomEating(this);
        oxygen = new Oxygen(this);
        discordDayLogin = new DiscordDayLogin(this);

        getCommand("startevent").setExecutor(manhuntEventCommand);
        getCommand("event").setExecutor(manhuntEventCommand);

        getServer().getPluginManager().registerEvents(mineAndDiscConnecting, this);
        getServer().getPluginManager().registerEvents(statisticChanging, this);
        getServer().getPluginManager().registerEvents(manhuntEvent, this);
        getServer().getPluginManager().registerEvents(borderExpansion, this);
        getServer().getPluginManager().registerEvents(mushroomEating, this);
        getServer().getPluginManager().registerEvents(oxygen, this);
        getServer().getPluginManager().registerEvents(discordDayLogin, this);


        manhuntEvent.scheduleDailyEvent();

        discord = new Discord(this);
        try {
            jda = JDABuilder.createDefault("-")
                    .addEventListeners(discord)
                    .addEventListeners(mineAndDiscConnecting)
                    .addEventListeners(donateLogic)
                    .addEventListeners(discordDayLogin)
                    .setActivity(Activity.playing("Aurora-admin"))
                    .build()
                    .awaitReady();


        } catch (Exception e) {
            e.printStackTrace();
        }

        long guildId = 1388842817027309639L;
        Guild guild = jda.getGuildById(guildId);

        guild.upsertCommand("profile", "Показать профиль игрока")
                .addOption(OptionType.STRING, "ник", "Ник игрока", true)
                .queue();

        guild.upsertCommand("tickets", "Создать кнопку для заявок")
                .queue();

        guild.upsertCommand("donate", "донатное меню")
                .queue();

        guild.upsertCommand("addmoney", "добавить деняк")
                .addOption(OptionType.STRING, "discordid", "кому деньгу", true)
                .addOption(OptionType.INTEGER, "скока", "скока деняк", true)
                .queue();
        guild.upsertCommand("removemoney", "отнять деняк")
                .addOption(OptionType.STRING, "discordid", "у кого отнять деньгу", true)
                .addOption(OptionType.INTEGER, "скока", "скока деняк", true)
                .queue();



        chanceLogic = new ChanceLogic(this);
        getServer().getPluginManager().registerEvents(chanceLogic, this);

        World world = Bukkit.getWorld("world");
        center = new Location(world, 0.5, 65, 0.5);

        borderExpansion.startRiftEffect();

        oxygen.cauldron();

    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.disconnect();
        }

        World world = Bukkit.getWorld("world");
        double radius = 10;

        for (TextDisplay td : world.getEntitiesByClass(TextDisplay.class)) {
            if (td.getLocation().distance(center) < radius) {
                td.remove();
            }
        }

    }
}
