package org.examplemores.desolatehealth;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class DESOLATEHealth extends JavaPlugin implements Listener {
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        loadConfig();
        getLogger().info("DESOLATE-Health has been enabled!");
    }

    @Override
    public void onDisable() {
        savePlayerHealth();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        player.setMaxHealth(60);// 设置最大血量为60
        // 从配置文件读取玩家血量
        if (config.contains(player.getUniqueId().toString())) {
            double health = config.getDouble(player.getUniqueId().toString());
            player.setHealth(health);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 记录玩家血量到配置文件
        config.set(player.getUniqueId().toString(), player.getHealth());
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        config = getConfig();
        for (Player player:Bukkit.getOnlinePlayers()){
            if (!config.contains(player.getUniqueId().toString())){
                config.set(player.getUniqueId().toString(),player.getMaxHealth());
            }
        }
    }

    private void savePlayerHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            config.set(player.getUniqueId().toString(), player.getMaxHealth());
        }
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
