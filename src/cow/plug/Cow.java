package cow.plug;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.ParseException;
import org.bukkit.ChatColor;

import org.json.simple.*;
import org.json.simple.parser.*;

public class Cow extends JavaPlugin {
	public JSONObject nodes = null;
	public File file = new File("plugins/cow/nodes.json");
	
	@Override
	public void onEnable(){
		JSONParser parser = new JSONParser();
		Object obj = null;
		
		new File("plugins/cow").mkdirs();
		try{
			if(file.createNewFile()){
				FileWriter filewriter = new FileWriter(file);
				filewriter.write(new JSONObject().toJSONString());
				filewriter.close();
			}
		}
		catch(IOException e){ e.printStackTrace(); }

		try{ obj = parser.parse(new FileReader(file)); } 
		catch(IOException | ParseException e){ e.printStackTrace(); }
		
		nodes = (JSONObject)obj;
		getServer().getPluginManager().registerEvents(new CowEvents(this), this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "plugin enabled");
	}
	
	@Override
	public void onDisable(){
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "plugin disabled");
    }
}