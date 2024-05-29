package org.desolate.health;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Health extends JavaPlugin {
    public static FileConfiguration config;
    EventListener eventListener = new EventListener();

    //插件加载
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(eventListener, this);
        loadConfig();
        getLogger().info("DESOLATE-Health has been enabled!");
        Timer timer=new Timer();
        timer.schedule(loadHealthTask(),2000L,10000L);
        //注册命令
        Objects.requireNonNull(this.getCommand("sethealth")).setExecutor(new HealthCommand());
    }

    @Override
    public void onDisable() {
        savePlayerHealth();
    }

    //加载配置文件
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

    //保存玩家血量
    private void savePlayerHealth() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            config.set(player.getUniqueId().toString(), player.getHealth());
        }
        try {
            config.save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().info("玩家血量保存失败: " + e.getMessage());
        }
    }

    //回血机制，在指定的世界关闭回血机制
    public TimerTask loadHealthTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Collection<? extends Player> onlinePlayer = getServer().getOnlinePlayers();
                for (Player player : onlinePlayer) {
                    boolean onPlayerWorld=checkPlayerWorld(player);
                    if (onPlayerWorld){
                        double health = player.getHealth();
                        config.set(player.getUniqueId().toString(), health);
                        PotionEffect heal = new PotionEffect(PotionEffectType.REGENERATION, 100, 2, false, false);
                        player.addPotionEffect(heal);
                    }
                }
            }
        };
    }

    //检查玩家所在世界
    public boolean checkPlayerWorld(Player player){
        List<String> worldName=config.getStringList("disabledWord");
        String playerWorld=player.getWorld().getName();
        for (String list:worldName){
            if (playerWorld.equals(list)){
                return false;
            }
        }
        return true;
    }
}