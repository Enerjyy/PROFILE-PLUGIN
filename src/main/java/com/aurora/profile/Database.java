package com.aurora.profile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.xml.crypto.Data;
import java.io.StringReader;
import java.net.ConnectException;
import java.sql.*;

public class Database {

    private Connection connection;
    public Connection getConnection() {
        return connection;
    }

    private final Profile plugin;
    public Database(Profile plugin) {
        this.plugin = plugin;
    }


    //-----------
    //   DB CONNECTION
    //-----------

    public void connect(String pluginFolder) {
        try {
            String url ="jdbc:sqlite:" + pluginFolder + "/constructions.db";
            connection = DriverManager.getConnection(url);

            System.out.println("[AuroraProfile] SQLite connected!");

            Statement stmt = connection.createStatement();
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS profile (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    nickname TEXT NOT NULL,
                    mining_coef DOUBLE,
                    killing_coef DOUBLE,
                    wood_coef DOUBLE,
                    discord_id TEXT,
                    donate INT,
                    profile_gif_url TEXT,
                    time_played TEXT,
                    deaths TEXT,
                    events_won INT,
                    event BOOLEAN
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_votes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    voter_id TEXT NOT NULL,
                    target_id TEXT NOT NULL,
                    vote_type TEXT NOT NULL,
                    UNIQUE(voter_id, target_id)
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS world_param (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    world_nether_radius INT,
                    world_end_radius INT,
                    world_radius INT,
                    resources INT,
                    levels INT,
                    resources_goal INT,
                    levels_goal INT
                    )
                    """);
            
            stmt.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try (Statement stmt = connection.createStatement()){
            stmt.executeUpdate("""
                    INSERT OR IGNORE INTO world_param
                    (id, world_nether_radius, world_end_radius, world_radius, resources, levels, resources_goal, levels_goal)
                    VALUES (1, 0, 0, 100, 0, 0, 500, 50)
                    """);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[AuroraProfile] SQLite disconnected!");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------
    //   DB LOGIC
    //-----------

    public void loadEverything() {
        if (connection == null) {
            System.out.println("[Profile] DB was not connected...");
            return;
        }
        try {
            String sql = "SELECT * FROM profile";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String nickname = rs.getString(("nickname"));
                Double mc = rs.getDouble("mining_coef");
                Double kc = rs.getDouble("killing_coef");
                Double wc = rs.getDouble("wood_coef");
                String discordId = rs.getString("discord_id");
                String profileGif = rs.getString("profile_gif_url");
                String timePlayed = rs.getString("time_played");
                String deaths = rs.getString("deaths");
                String eventsWon = rs.getString("events_won");
                int donate = rs.getInt("donate");
                boolean activityInEvent = rs.getBoolean("event");

                plugin.addEventMap(nickname, activityInEvent);

                plugin.addCoefsOfWood(nickname, wc);
                plugin.addCoefsOfMining(nickname, mc);

                if (profileGif != null) {
                    plugin.addProfileGifs(discordId, profileGif);
                }
                if (timePlayed != null) {
                    plugin.addTimePlayeds(nickname, timePlayed);
                }
                if (deaths != null) {
                    plugin.addDeaths(nickname, deaths);
                }
                if (eventsWon != null) {
                    plugin.addEventsWon(nickname, eventsWon);
                }

                plugin.addCoefsOfMining(nickname, mc);
                if (discordId != null && !discordId.isEmpty()) {
                    plugin.addDiscordLinkMap(nickname, discordId);
                    plugin.addMinecraftLinkMap(discordId, nickname);
                    plugin.getDonateMap().put(discordId, donate);
                    System.out.println(nickname + " БЫЛ ЗАГРУЖЕН КАК " + discordId + "С балансом " + donate);
                }

            }
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM player_votes";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String voter = rs.getString("voter_id");
                String target = rs.getString("target_id");
                String type = rs.getString("vote_type");

                if (type.equals("up")) {
                    plugin.addUpVotes(target, voter);
                }
                else if (type.equals("down")) {
                    plugin.addDownVotes(target, voter);
                }
            }
            rs.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM world_param";
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int borderWorld = rs.getInt("world_radius");
                int borderWorldEnd = rs.getInt("world_end_radius");
                int borderWorldNether = rs.getInt("world_nether_radius");
                int items = rs.getInt("resources");
                int itemsNeed = rs.getInt("resources_goal");
                int levels = rs.getInt("levels");
                int levelsNeed = rs.getInt("levels_goal");

                System.out.println("--------------------");
                plugin.setHowMuchItemsNeedNow(itemsNeed);
                System.out.println(plugin.getHowMuchItemsNeedNow());
                plugin.setHowMuchItemsNow(items);
                System.out.println(plugin.getHowMuchItemsNow());
                plugin.setHowMuchLevelsNeedNow(levelsNeed);
                System.out.println(plugin.getHowMuchLevelsNeedNow());
                plugin.setHowMuchLevelsNow(levels);
                System.out.println(plugin.getHowMuchLevelsNow());
                plugin.setBorderOfWorld(borderWorld);
                System.out.println(plugin.getBorderOfWorld());
                plugin.setBorderOfWorldEnd(borderWorldEnd);
                System.out.println(plugin.getBorderOfWorldEnd());
                plugin.setBorderOfWorldNether(borderWorldNether);
                System.out.println(plugin.getBorderOfWorldNether());
                System.out.println("--------------------");
            }
            rs.close();

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addOrUpdateWorldNether(int worldborder) {
        try {
            String sql = """
                    UPDATE world_param SET world_nether_radius = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, worldborder);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateWorldEnd(int worldborder) {
        try {
            String sql = """
                    UPDATE world_param SET world_end_radius = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, worldborder);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateWorld(int worldborder) {
        try {
            String sql = """
                    UPDATE world_param SET world_radius = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, worldborder);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateItemsNeed(int itemsneed) {
        try {
            String sql = """
                    UPDATE world_param SET resources_goal = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, itemsneed);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateItems(int items) {
        try {
            String sql = """
                    UPDATE world_param SET resources = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, items);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateLevelNeed(int levelneed) {
        try {
            String sql = """
                    UPDATE world_param SET levels_goal = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, levelneed);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {

        }
    }
    public void addOrUpdateLevel(int level) {
        try {
            String sql = """
                    UPDATE world_param SET levels = ? WHERE id = 1;
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, level);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addOrUpdateGif(String link, String discordId) {
        try {
            String sql = """
                    UPDATE profile SET profile_gif_url = ? WHERE discord_id = ?;
                    """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, link);
            ps.setString(2, discordId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addOrUpdateEventActivity(String nickname, boolean activityState) {
        try {
            String sql = """
                    UPDATE profile SET event = ? WHERE nickname = ?;
                    """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setBoolean(1, activityState);
            ps.setString(2, nickname);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void addOrUpdateVote(String voter, String target, String type) {
        try {
            String sql = """
                INSERT OR REPLACE INTO player_votes (voter_id, target_id, vote_type) VALUES (?, ?, ?);
                """;
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, voter);
            ps.setString(2, target);
            ps.setString(3, type);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDonate(String discordId, int donate) {
        try {
            String sql = "UPDATE profile SET donate = ? WHERE discord_id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, donate);
            ps.setString(2, discordId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean playerExists(String nickname) {
        try {
            String sql = "SELECT COUNT(*) FROM profile WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, nickname);
            ResultSet rs = ps.executeQuery();
            rs.next();
            boolean exists = rs.getInt(1) > 0;
            rs.close();
            ps.close();
            return exists;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public void addEverything(String uuid, String nickname) {
        try {
            String sql = "INSERT INTO profile (uuid, nickname, mining_coef, killing_coef, wood_coef, discord_id, profile_gif_url, donate, event) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, nickname);
            ps.setDouble(3, 0);
            ps.setDouble(4, 0);
            ps.setDouble(5, 0);
            ps.setString(6, null);
            ps.setString(7, null);
            ps.setInt(8, 0);
            ps.setBoolean(9, true);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void setDiscord(String nickname, String discordId) {
        try {
            String sql = "UPDATE profile SET discord_id = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, discordId);
            ps.setString(2, nickname);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setDeaths(String nickname, String deaths) {
        try {
            String sql = "UPDATE profile SET deaths = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, deaths);
            ps.setString(2, nickname);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void setTime(String nickname, String time) {
        try {
            String sql = "UPDATE profile SET time_played = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, time);
            ps.setString(2, nickname);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCoefMining(Player player, double coef) {
        try {
            String checkSql = "SELECT COUNT(*) FROM profile WHERE nickname = ?";
            PreparedStatement checkPs = connection.prepareStatement(checkSql);
            checkPs.setString(1, player.getName());
            ResultSet rs = checkPs.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) {
                addEverything(player.getUniqueId().toString(), player.getName());
            }
            rs.close();
            checkPs.close();

            String sql = "UPDATE profile SET mining_coef = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, coef);
            ps.setString(2, player.getName());
            ps.executeUpdate();
            ps.close();

            plugin.addCoefsOfMining(player.getName(), coef);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateCoefKilling(Player player, double coef) {
        try {
            String sql = "UPDATE profile SET killing_coef = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, coef);
            ps.setString(2, player.getName());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateCoefWood(Player player, double coef) {
        try {
            String checkSql = "SELECT COUNT(*) FROM profile WHERE nickname = ?";
            PreparedStatement checkPs = connection.prepareStatement(checkSql);
            checkPs.setString(1, player.getName());
            ResultSet rs = checkPs.executeQuery();
            rs.next();

            if (rs.getInt(1) == 0) {
                addEverything(player.getUniqueId().toString(), player.getName());
            }

            rs.close();
            checkPs.close();

            String sql = "UPDATE profile SET wood_coef = ? WHERE nickname = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setDouble(1, coef);
            ps.setString(2, player.getName());
            ps.executeUpdate();
            ps.close();

            plugin.getCoefsOfWood().put(player.getName(), coef);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
