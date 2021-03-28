package com.ismaiiil.randomspawn;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 0){
            RandomSpawn rd = (RandomSpawn) Bukkit.getServer().getPluginManager().getPlugin("RandomSpawn");
            if (rd != null){
                rd.reloadConfig();
            }
        }else{
            sender.sendMessage("Please use the command as shown below");
            return false;
        }
        return true;
    }
}
