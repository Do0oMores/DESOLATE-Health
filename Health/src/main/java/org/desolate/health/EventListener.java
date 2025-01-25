package org.desolate.health;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class EventListener implements Listener {

    JavaPlugin plugin;

    public EventListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                HealthMade(player);
            }
        }.runTaskLater(plugin, 100L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 记录玩家血量到配置文件
        Health.config.set(player.getUniqueId().toString(), player.getHealth());
    }

    //玩家切换世界
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        HealthMade(player);
    }

    //玩家死亡
    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            @Override
            public void run() {
                HealthMade(player);
            }
        }.runTaskLater(plugin, 20L);
    }

    //新API:attribute
    private void HealthMade(Player player) {
        String PlayerWorldName = player.getWorld().getName();
        double DefaultHealth = Health.config.getDouble("默认最大玩家血量", 100.0);
        if (Health.config.contains("自定义世界血量." + PlayerWorldName)) {
            double health = Health.config.getDouble("自定义世界血量." + PlayerWorldName, DefaultHealth);
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(health);
                player.setHealth(health);
            }
        } else {
            AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(DefaultHealth);
                player.setHealth(DefaultHealth);
            }
        }
    }
}
