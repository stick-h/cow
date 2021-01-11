package cow.plug;

import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

public class Cow extends JavaPlugin {
	public File treeYml = new File(getDataFolder(), "tree.yml");
	public FileConfiguration treeConfig = YamlConfiguration.loadConfiguration(treeYml);
	
	@Override
	public void onEnable(){
		if(!treeYml.exists()) saveResource("tree.yml", false);
		getServer().getPluginManager().registerEvents(new CowEvents(this), this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "plugin enabled");
	}
	@Override
	public void onDisable(){
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "plugin disabled");
    }
}