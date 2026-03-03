package org.desolate.health;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Health extends JavaPlugin {
    private static final long REGEN_DELAY_MS = 5000L;
    private static final double REGEN_AMOUNT = 1.0D;

    public static FileConfiguration config;
    EventListener eventListener = new EventListener(this);
    private final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    private BukkitTask healthTask;

    //插件加载
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(eventListener, this);
        loadConfig();
        getLogger().info("DESOLATE-Health has been enabled!");
        healthTask = Bukkit.getScheduler().runTaskTimer(this, this::processHealthRegen, 20L, 20L);
        //注册命令
        Objects.requireNonNull(this.getCommand("rehealth")).setExecutor(new HealthCommand());
    }

    @Override
    public void onDisable() {
        if (healthTask != null) {
            healthTask.cancel();
        }
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

    //回血机制：每个玩家单独判断，受伤后等待5秒再持续回血
    private void processHealthRegen() {
        Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
        long currentTime = System.currentTimeMillis();

        for (Player player : onlinePlayers) {
            if (!checkPlayerWorld(player) || player.isDead()) {
                continue;
            }

            double health = player.getHealth();
            config.set(player.getUniqueId().toString(), health);

            double maxHealth = getPlayerMaxHealth(player);
            if (health >= maxHealth) {
                continue;
            }

            long lastDamage = lastDamageTime.getOrDefault(player.getUniqueId(), 0L);
            if (currentTime - lastDamage < REGEN_DELAY_MS) {
                continue;
            }

            double newHealth = Math.min(maxHealth, health + REGEN_AMOUNT);
            player.setHealth(newHealth);
        }
    }

    public void resetRegenDelay(Player player) {
        lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void clearRegenState(Player player) {
        lastDamageTime.remove(player.getUniqueId());
    }

    private double getPlayerMaxHealth(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            return maxHealth.getBaseValue();
        }
        return player.getMaxHealth();
    }

    //检查玩家所在世界
    public boolean checkPlayerWorld(Player player) {
        List<String> worldName = config.getStringList("在以下世界不启用回血机制");
        String playerWorld = player.getWorld().getName();
        for (String list : worldName) {
            if (playerWorld.equals(list)) {
                return false;
            }
        }
        return true;
    }
}
