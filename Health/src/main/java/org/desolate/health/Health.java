package org.desolate.health;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Health extends JavaPlugin implements Listener {
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
        double health=config.getDouble("最大玩家血量");
        //重置超出阈值的玩家血量
        if (player.getHealth()>health){
            player.setHealth(health);
            player.sendMessage(ChatColor.RED+"你的血量已超出阈值，现已重置");
        }
        if (config.contains("最大玩家血量")){
            if (health>0.0){
                player.setMaxHealth(health);
            }else {
                config.set("最大玩家血量",60.0);
                player.setMaxHealth(60);// 设置最大血量为60
                player.sendMessage(ChatColor.RED+"血量需为正数，已将血量重置为60");
            }
        }
        // 从配置文件读取玩家血量
        if (config.contains(player.getUniqueId().toString())) {
            double NowHealth = config.getDouble(player.getUniqueId().toString());
            player.setHealth(NowHealth);
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
                config.set(player.getUniqueId().toString(),player.getHealth());
            }
        }
    }

    private void savePlayerHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            config.set(player.getUniqueId().toString(), player.getHealth());
        }
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

