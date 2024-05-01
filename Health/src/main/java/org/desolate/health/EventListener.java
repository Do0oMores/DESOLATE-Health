package org.desolate.health;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setPlayerHealth(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // 记录玩家血量到配置文件
        Health.config.set(player.getUniqueId().toString(), player.getHealth());
    }

    //玩家切换世界
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        Player player=event.getPlayer();
        double health=Health.config.getDouble("最大玩家血量");
        if (Health.config.contains("最大玩家血量")){
            if (health>0.0){
                player.setMaxHealth(health);
                player.setHealth(health);
            }else {
                Health.config.set("最大玩家血量",100.0);
                player.setMaxHealth(100.0);
                player.sendMessage(ChatColor.RED+"血量需为正数，已将血量重置为100");
            }
        }
    }

    //玩家死亡
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerDeath(PlayerSpawnLocationEvent event){
        Player player= event.getPlayer();
        double MaxHealth=Health.config.getDouble("最大玩家血量");
        player.setMaxHealth(MaxHealth);
        //设置血量
        player.setHealth(MaxHealth);
    }

    @SuppressWarnings("deprecation")
    public void setPlayerHealth(PlayerEvent event) {
        Player player = event.getPlayer();
        double health = Health.config.getDouble("最大玩家血量");
        //重置超出阈值的玩家血量 (更正--重复判断)
        if (Health.config.contains("最大玩家血量")) {
            if (health > 0.0) {
                player.setMaxHealth(health);
            } else {
                Health.config.set("最大玩家血量", 100.0);
                player.setMaxHealth(100);// 设置最大血量为60
                player.sendMessage(ChatColor.RED + "血量需为正数，已将血量重置为100");
            }
        }
        // 从配置文件读取玩家血量
        if (Health.config.contains(player.getUniqueId().toString())) {
            double NowHealth = Health.config.getDouble(player.getUniqueId().toString());
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
}
