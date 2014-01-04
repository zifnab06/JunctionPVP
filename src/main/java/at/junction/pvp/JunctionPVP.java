package at.junction.pvp;


import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.io.File;
import java.util.HashMap;

class JunctionPVP  extends JavaPlugin{
    public Configuration config;
    public HashMap<String, Team> teams;
    private WorldGuardPlugin wg;

    @Override
    public void onEnable(){
        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ((plugin == null) || !(plugin instanceof WorldGuardPlugin)){
            getLogger().severe("Worldguard not detected. JunctionPVP not loaded");
            return;
        }
        wg = (WorldGuardPlugin) plugin;
        //Register Listener

        getServer().getPluginManager().registerEvents(new JunctionPVPListener(this), this);

        config = new Configuration(this);
        config.load();

        teams = new HashMap<String, Team>();
        for(String team : config.TEAM_NAMES){
            teams.put(team, new Team(this, team));
        }


    }

    @Override
    public void onDisable(){
        for (Team team : teams.values()){
            team.saveTeam();
        }
        config.save();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String name, String[] args) {
        if (name.equalsIgnoreCase("teams")){
            for (Team team : teams.values()){
                sender.sendMessage(team.getColor() + team.getName() + ChatColor.RESET + ":");
                sender.sendMessage(team.getFormattedPlayerList());
            }
        } else if (name.equalsIgnoreCase("forceteam")){
            if (args.length < 3){
                sender.sendMessage(ChatColor.RED + "Usage: /forceteam <player> <team name>");
                return true;
            }
            Player toSwitch = getServer().getPlayer(args[0]);
            if (toSwitch == null){
                sender.sendMessage(ChatColor.RED + "This player is not online");
                return true;
            }

            String teamName = "";
            for (int i=1; i<args.length; i++){
                teamName += args[i];
                if (i != args.length-1)
                    teamName += " ";
            }
        }
        return true;
    }

    public boolean hasRegion(Block block) {
        return wg.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).size() != 0;
    }
    /*
    * getTeam(String player)
    * returns the name of the team a player is on
    * If player is not on a team, returns null
     */
    public String getTeam(String player){
        for (String team : config.TEAM_NAMES){
            if (teams.get(team).containsPlayer(player)) return team;
        }
        return null;
    }

}
