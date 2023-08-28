package cow.plug;

import java.io.*;
import java.util.*;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;

import org.json.simple.*;
import org.json.simple.parser.*;

public class CowEvents implements Listener {
	private static JSONObject nodes;
	private static File file;

	public CowEvents(Cow main){
		 this.nodes = main.nodes;
		 this.file = main.file;
	}
	
	@EventHandler
	public static void onEntityBreed(EntityBreedEvent evt) throws IOException {
		System.out.println("bred");
		Ageable child = (Ageable) evt.getEntity();
		String childID = child.getUniqueId().toString();
		String fatherID = evt.getFather().getUniqueId().toString();
		String motherID = evt.getMother().getUniqueId().toString();
		Location location = evt.getFather().getLocation();
		World world = evt.getFather().getWorld();
		
		double COI = 0.00;
		JSONArray fatherTree = null;
		JSONArray motherTree = null;
		List<Integer> nums = new ArrayList<Integer>();
		List<Double> COIs = new ArrayList<Double>();
		
		JSONObject fatherObj = (JSONObject)nodes.get(fatherID);
		JSONObject motherObj = (JSONObject)nodes.get(motherID);
		
		
		if(fatherObj == null){
			JSONArray first = new JSONArray();
			first.add(fatherID);
			fatherTree = new JSONArray();
			fatherTree.add(first);
			
			fatherObj = new JSONObject();
			fatherObj.put("tree", fatherTree);
			fatherObj.put("COI", 0.00);
			nodes.put(fatherID, fatherObj);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}else fatherTree = (JSONArray)fatherObj.get("tree");
		
		if(motherObj == null){
			JSONArray first = new JSONArray();
			first.add(motherID);
			motherTree = new JSONArray();
			motherTree.add(first);
			
			motherObj = new JSONObject();
			motherObj.put("tree", motherTree);
			motherObj.put("COI", 0.00);
			nodes.put(motherID, motherObj);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}else motherTree = (JSONArray)motherObj.get("tree");

		for(int i = 0; i < fatherTree.size(); i++){
			
			for(int j = 0; j < motherTree.size(); j++){
				JSONArray genF = (JSONArray)fatherTree.get(i);
				JSONArray genM = (JSONArray)motherTree.get(j);
				
				for(int k = 0; k < genF.size(); k++){
					
					String entity = (String)genF.get(k);
					int count = 0;
					
					for(int m = 0; m < genM.size(); m++) if(entity == (String)genM.get(m)) count++;
					
					for(int n = 0; n < count; n++){
						nums.add(i+j+1);
						JSONObject entityObj = (JSONObject)nodes.get(entity);
						if(entityObj != null) COIs.add((Double)entityObj.get("COI"));
						else COIs.add(0.00);
					}
					
				}
			}
		}
		
		for(int i = 0; i < nums.size(); i++) COI += Math.pow(0.5, nums.get(i))*(1+COIs.get(i));

		double random = Math.random();
		System.out.println(random);
		if(random < COI){
			child.remove();
			switch(child.getType()){
				case PIG:
					Creeper creeper = (Creeper)world.spawnEntity(location, EntityType.CREEPER);
					creeper.ignite();
					break;
				default:
					LivingEntity ravager = (LivingEntity)world.spawnEntity(location, EntityType.RAVAGER);
					ravager.setCustomName(child.getType().toString());
					break;
			}
		}else{
			JSONArray childTree = new JSONArray();
			JSONArray first = new JSONArray();
			first.add(childID);
			childTree.add(first);
			
			for(int i = 0; i < Math.max(fatherTree.size(), motherTree.size()); i++){
				JSONArray jointArray = new JSONArray();
				if(fatherTree.size() > i) jointArray.addAll((JSONArray)fatherTree.get(i));
				if(motherTree.size() > i) jointArray.addAll((JSONArray)motherTree.get(i));
				childTree.add(jointArray);
			}
			
			JSONObject childObj = new JSONObject();
			childObj.put("tree", childTree);
			childObj.put("COI", COI);
			nodes.put(childID, childObj);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}
		System.out.println(COI);
	}
}