package me.wooskie.colosseum;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Colosseum extends JavaPlugin{
    // Permissions handler.
    static PermissionHandler perm;

    // Easy references for plugin name and version.
    private String pluginName = "";
    private String pluginVers = "";

        // Sets logger for outputting to minecraft server console
	Logger log = Logger.getLogger("Minecraft");
	
	// Defines plugin classes:
	static ColosseumEntityListener eListener;
	static ColosseumPlayerListener pListener;
	static ColosseumCommands colosseumCommands;
	static ColosseumData colosseumData;
	static ColosseumMonitor colosseumMonitor;
	
	// Hashmaps for keeping track of player information:
	public HashMap<Player, Boolean> inPlay = new HashMap<Player, Boolean>();
	public HashMap<Player, String> roster = new HashMap<Player, String>();
	public HashMap<Player, ItemStack[]> initialInvItemStack = new HashMap<Player, ItemStack[]>();
	public HashMap<Player, ItemStack[]> initialArmItemStack = new HashMap<Player, ItemStack[]>();
	
	// Hashmaps for storing arena objects (i.e. teleport locations, signs, and gates):
	public HashMap<String, Location> colosseumLocs = new HashMap<String, Location>();
	public HashMap<Sign, String> colosseumSigns = new HashMap<Sign, String>();
	public HashMap<Location, String> colosseumGates = new HashMap<Location, String>();
	
	// Booleans for identifying state of the arena:
	public Boolean redReady = false;
	public Boolean blueReady = false;
	public Boolean battleInProgress = false;
	public Boolean colosseumInUse = false;
	
	// Commonly used strings:
	public ChatColor chatColor = ChatColor.YELLOW;
	public String redString = ChatColor.RED + "RED" + chatColor;
	public String blueString = ChatColor.BLUE + "BLUE" + chatColor;
	
	// ID tag for scheduled monitor task. Used for turning off monitor when not in use:
	public int monitorID;
	
	
	// Handles administrator console commands used to save and clear arena objects:
	// Permissions needs to be implemented.
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if (commandLabel.equalsIgnoreCase("pvp")) {
                    Player player = (Player) sender;
                    if (this.perm.has(player, "colosseum.admin")) {
			if (args.length < 2) {
                            player.sendMessage("Syntax: /pvp <param>");
			    return false;
			}
			if (args[0].equalsIgnoreCase("set")) {
				if (args.length > 2) {
					if (args[1].equalsIgnoreCase("location")) {
						return colosseumData.SetLocation(player, args[2]);
					} else if (args[1].equalsIgnoreCase("sign")) {
						return colosseumData.SetSign(player, args[2]);
					} else if (args[1].equalsIgnoreCase("gate")) {
						return colosseumData.SetGate(player, args[2]);
					} 
				}
			} else if (args[0].equalsIgnoreCase("clear")) {
				if (args[1].equalsIgnoreCase("allsigns")) {
					return colosseumData.ClearAllSigns(player);
				} else if (args[1].equalsIgnoreCase("sign")) {
					return colosseumData.ClearSign(player);
				} else if (args[1].equalsIgnoreCase("locations")) {
					colosseumLocs.clear();
					colosseumData.SaveLocations();
					player.sendMessage("Locations cleared.");
					return true;
				} else if (args[1].equalsIgnoreCase("gates"))
					colosseumCommands.OpenGates("inner");
					colosseumCommands.OpenGates("outer");
					colosseumGates.clear();
					colosseumData.SaveGates();
					player.sendMessage("Gates cleared.");
					return true;
			} else
                            player.sendMessage("Invalid operation!");
                    } else
                        player.sendMessage("You do not have access to this command.");
		}
		return false;
	}
	
	// Runs when plugin is loaded. Nothing interesting, just initialization.
	public void onEnable() {
		
            this.pluginName = this.getDescription().getName();
            this.pluginVers = this.getDescription().getVersion();
                eListener = new ColosseumEntityListener( this );
		pListener = new ColosseumPlayerListener( this );
		colosseumCommands = new ColosseumCommands( this );
		colosseumData = new ColosseumData( this );
		colosseumMonitor = new ColosseumMonitor( this );
		
		colosseumCommands.UpdateTeamSigns();
		
		log.info(this.pluginName + " v" + this.pluginVers + " enabled.");
		PluginManager pm = getServer().getPluginManager();	
		pm.registerEvent(Event.Type.ENTITY_DEATH, eListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, eListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, pListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, pListener, Priority.Normal, this);
	}
	
	// Runs when plugin is disabled.
	public void onDisable() {
		log.info(this.pluginName + " v" + this.pluginVers + " disabled.");
	}
	
    // Initial Permissions loading and setup.
    private void setupPermissions()
    {
      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

      if (this.perm == null) {
          if (permissionsPlugin != null) {
              this.perm = ((Permissions) permissionsPlugin).getHandler();
          } else {
              this.log.log(Level.SEVERE, "Permission system not detected.");
          }
      }
    }
	
}
