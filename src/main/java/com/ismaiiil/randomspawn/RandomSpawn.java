package com.ismaiiil.randomspawn;

import javafx.util.Pair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class RandomSpawn extends JavaPlugin implements CommandExecutor, Listener, TabCompleter {
    int radius;
    ArrayList<String> playerNames = new ArrayList<>();
    ArrayList<String> empty = new ArrayList<>();
    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().broadcastMessage("Random spawn enabled");
        getServer().getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        getCommand("randomspawn").setExecutor(this);
        getCommand("reloadconfig").setExecutor(new ReloadSpawn());
        getCommand("randomspawn").setTabCompleter(this);

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()){
            RandomizeSpawn(p);
        }
    }

    private void RandomizeSpawn(Player p) {
        radius = getConfig().getInt("radius");
        Location spawn = p.getWorld().getSpawnLocation();
        Location destination = generateRandomSpawn(radius, spawn);
        teleportPlayer(destination, p, radius, spawn);
        p.setBedSpawnLocation(new Location(p.getWorld(),p.getLocation().getX(),p.getLocation().getY(),p.getLocation().getZ()), true);

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1){
            //change the player spawn by randomizing it within the radius and saving his bed loacation
            Player p = getServer().getPlayer(args[0]);
            if( p == null){
                sender.sendMessage("Failed to get the player you specified");
                return true;
            }
            RandomizeSpawn(p);
            return true;
        }
        if (args.length == 2){
            Player p = getServer().getPlayer(args[0]);
            if( p == null){
                sender.sendMessage("Failed to get the player you specified");
                return true;
            }
            if (args[1].equals("print")){
                //return the current spawn point
                sender.sendMessage(Objects.requireNonNull(p.getBedSpawnLocation()).toString());
                return true;
            }

        }
        if (args.length == 4){
            //change player spawn to the new spawn location
            Player p = getServer().getPlayer(args[0]);
            int x;
            int y = 0;
            int z;
            boolean isYSupplied = false;
            if( p == null){
                sender.sendMessage("Failed to get the player you specified");
                return true;
            }
            try{
                x = Integer.parseInt(args[1]);
                if(!args[2].equals("noy")){
                    y = Integer.parseInt(args[2]);
                    isYSupplied = true;
                }
                z = Integer.parseInt(args[3]);

                Pair<String,Boolean> returns = teleportPlayer(x,y,z,p,isYSupplied);

                if(returns.getValue()){
                    //save spawn location
                    p.setBedSpawnLocation(p.getLocation(), true);
                }
                sender.sendMessage(returns.getKey());

            }catch (NumberFormatException e){
                sender.sendMessage("Error parsing the coordinates");
                return false;
            }

        }
        return true;
    }


    public Location generateRandomSpawn(int radius, Location spawn){

        int x = spawn.getBlockX();
        int z = spawn.getBlockZ();
        int maxX= x + radius;
        int minX= x - radius;
        int maxZ= z + radius;
        int minZ= z - radius;
        Random Xrand = new Random();
        x = Xrand.nextInt(maxX - minX) + minX;
        Random Zrand = new Random();
        z = Zrand.nextInt(maxZ - minZ) + minZ;

        World world = Bukkit.getServer().getWorld("world");
        int y = world.getHighestBlockYAt(x,z);

        Location destination = new Location(world, x, y+1, z);

        return destination;

    }

    public void teleportPlayer(Location destination, Player player, int radius, Location spawn){
        try{
            World world;
            if ((world = player.getLocation().getWorld()) == null){
                return;
            }
            Location blockBelow = new Location( world, destination.getX(), destination.getY()-1, destination.getZ());

            Block block = world.getBlockAt(blockBelow);
            Block block1 = world.getBlockAt(destination);
            if (block.getType() == Material.LAVA || block.getType() == Material.WATER ||
                block1.getType() == Material.LAVA || block1.getType() == Material.WATER
            ){
                Location location = generateRandomSpawn(radius, spawn);
                teleportPlayer(location, player, radius, spawn);
                System.out.println(location);
                System.out.println(block.getType());
                System.out.println(block1.getType());
                return;
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "tp " + player.getName() + " " + destination.getX()  + " " + destination.getY() + " " + destination.getZ();
        Bukkit.dispatchCommand(console, command);
    }

    public Pair<String, Boolean> teleportPlayer(int x, int y, int z, Player player, boolean isYSupplied){
        try{
            Location blockBelow;
            World world;
            if ((world = player.getLocation().getWorld()) == null){
                return new Pair<>( "Could not get the world instance for player: " + player.toString() , false);
            }
            if(isYSupplied){
                blockBelow = new Location( player.getLocation().getWorld(), x,y-1, z);
            }else{
                y = world.getHighestBlockYAt(x,z);
                y += 1;
                blockBelow = new Location( player.getLocation().getWorld(), x, y-1, z);
            }

            Block block = world.getBlockAt(blockBelow);
            Block block1 = world.getBlockAt(new Location(world, x,y,z));
            if (block.getType() == Material.LAVA || block.getType() == Material.WATER ||
                    block1.getType() == Material.LAVA || block1.getType() == Material.WATER
            ){
                return new Pair<>("LAVA or WATER at destination specified" , false);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return  new Pair<>("Error occurred while getting the blocks at that location (see server trace)", false);
        }
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String command = "tp " + player.getName() + " " + x  + " " + y  + " " + z;
        if(Bukkit.dispatchCommand(console, command)){
            return new Pair<>( "Success", true);
        }
        return new Pair<>( "Error while teleporting player", false);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        Player p = (Player) sender;
        switch (args.length){
            case 1:
                loadPlayerNames(sender);
                return playerNames;
            case 2:

                ArrayList<String> xtabs = new ArrayList<>();
                xtabs.add(String.valueOf(p.getLocation().getBlockX()));
                xtabs.add("print");
                return xtabs;
            case 3:
                if(args[1].equals("print") ){
                    return empty;
                }else{
                    ArrayList<String> ytabs = new ArrayList<>();
                    ytabs.add(String.valueOf(p.getLocation().getBlockY()));
                    ytabs.add("noy");
                    return ytabs;
                }
            case 4:
                ArrayList<String> ztabs = new ArrayList<>();
                ztabs.add(String.valueOf(p.getLocation().getBlockZ()));
                return ztabs;
            case 5:
                return empty;
            default:
                break;
        }
        return empty;
    }

    private void loadPlayerNames(CommandSender sender) {
        playerNames = new ArrayList<>();
        sender.getServer().getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
