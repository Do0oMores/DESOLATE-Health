package org.desolate.health;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealthCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        //判断为玩家输入
        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("只有玩家可以使用该指令");
            return true;
        }
        Player player=(Player) commandSender;
        //从配置文件读取最大玩家血量
        double MaxHealth=Health.config.getDouble("最大玩家血量");
        player.setMaxHealth(MaxHealth);
        //设置血量
        player.setHealth(MaxHealth);
        return true;
    }
}
