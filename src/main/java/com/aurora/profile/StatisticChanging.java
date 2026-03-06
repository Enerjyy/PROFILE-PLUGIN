package com.aurora.profile;

import org.bukkit.Statistic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StatisticChanging  implements org.bukkit.event.Listener{
    private final Profile plugin;
    public StatisticChanging(Profile plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        int deaths = event.getPlayer().getStatistic(Statistic.DEATHS);
        plugin.addDeaths(event.getPlayer().getName(), String.valueOf(deaths));
        plugin.getDatabase().setDeaths(event.getPlayer().getName(), String.valueOf(deaths));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        String nickname = event.getPlayer().getName();
        double time = event.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE);
        double timeInHours = time / 20 / 3600;
        String timeWeWantToSee = String.format("%.0f ч", timeInHours);
        plugin.addTimePlayeds(nickname, timeWeWantToSee);
        plugin.getDatabase().setTime(nickname, timeWeWantToSee);

        int deaths = event.getPlayer().getStatistic(Statistic.DEATHS);
        plugin.addDeaths(event.getPlayer().getName(), String.valueOf(deaths));
        plugin.getDatabase().setDeaths(event.getPlayer().getName(), String.valueOf(deaths));
    }
}
