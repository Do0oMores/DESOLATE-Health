package org.desolate.health;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public final class Health extends JavaPlugin implements Listener {
    private FileConfiguration config;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        getLogger().info("DESOLATE-Health has been enabled!");
        Timer timer=new Timer();
        timer.schedule(loadHealthTask(),2000L,10000L);
    }

    @Override
    public void onDisable() {
        savePlayerHealth();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        double health = config.getDouble("最大玩家血量");
//重置超出阈值的玩家血量 (更正--重复判断)
//        if (player.getHealth()>health){
//            player.setHealth(health);
//            player.sendMessage(ChatColor.RED+"你的血量已超出阈值，现已重置");
//        }
        if (config.contains("最大玩家血量")) {
            if (health > 0.0) {
                player.setMaxHealth(health);
            } else {
                config.set("最大玩家血量", 60.0);
                player.setMaxHealth(60);// 设置最大血量为60
                player.sendMessage(ChatColor.RED + "血量需为正数，已将血量重置为60");
            }
        }
        // 从配置文件读取玩家血量
        if (config.contains(player.getUniqueId().toString())) {
            double NowHealth = config.getDouble(player.getUniqueId().toString());
            //增加判断，如果配置中保存玩家血量大于最大血量
            //则直接设置玩家血量为最大血量，否则会出现血量不匹配报错
            if (NowHealth > health) {
                player.setHealth(health);
                //重置超出阈值的玩家血量
                player.sendMessage(ChatColor.RED + "你的血量已超出阈值，现已重置");
            } else
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
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!config.contains(player.getUniqueId().toString())) {
                config.set(player.getUniqueId().toString(), player.getHealth());
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

    //玩家切换世界
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Player player= event.getPlayer();
        World world= event.getFrom();
        double health = config.getDouble("最大玩家血量");
        if (config.contains("最大玩家血量")) {
            if (health > 0.0) {
                player.setMaxHealth(health);
            } else {
                config.set("最大玩家血量", 60.0);
                player.setMaxHealth(60);// 设置最大血量为60
                player.sendMessage(ChatColor.RED + "血量需为正数，已将血量重置为60");
            }
        }
        // 从配置文件读取玩家血量
        if (config.contains(player.getUniqueId().toString())) {
            double NowHealth = config.getDouble(player.getUniqueId().toString());
            //增加判断，如果配置中保存玩家血量大于最大血量
            //则直接设置玩家血量为最大血量，否则会出现血量不匹配报错
            if (NowHealth > health) {
                player.setHealth(health);
                //重置超出阈值的玩家血量
                player.sendMessage(ChatColor.RED + "你的血量已超出阈值，现已重置");
            } else
                player.setHealth(NowHealth);
        }
    }

    public TimerTask loadHealthTask() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayer = getServer().getOnlinePlayers();
                for (Player player : onlinePlayer) {
                    double health = player.getHealth();
                    config.set(player.getUniqueId().toString(), health);
                    PotionEffect heal = new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false);
                    player.addPotionEffect(heal);
                }
            }
        };
        return task;
    }
}