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
		Ageable childEntity = (Ageable) evt.getEntity();
		String childID = childEntity.getUniqueId().toString();
		String fatherID = evt.getFather().getUniqueId().toString();
		String motherID = evt.getMother().getUniqueId().toString();
		Location location = evt.getFather().getLocation();
		World world = evt.getFather().getWorld();
		
		JSONObject father = (JSONObject)nodes.get(fatherID);
		JSONObject mother = (JSONObject)nodes.get(motherID);
		JSONArray fatherTree = null;
		JSONArray motherTree = null;
		
		double COI = 0.00;
		List<Integer> nums = new ArrayList<Integer>();
		List<Double> COIs = new ArrayList<Double>();
		
		//[[gen0], [gen1], [gen2],...]
		//[[{"id": parentID, child: childID}], [{}, {}],...]
		if(father == null){
			JSONObject fatherObj = new JSONObject();
			JSONArray array = new JSONArray();
			
			fatherObj.put("id", fatherID);
			fatherObj.put("child", null);
			
			array.add(fatherObj);
			fatherTree = new JSONArray();
			fatherTree.add(array);
			
			father = new JSONObject();
			father.put("tree", fatherTree);
			father.put("COI", 0.00);
			nodes.put(fatherID, father);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}else fatherTree = (JSONArray)father.get("tree");
		
		if(mother == null){
			JSONObject motherObj = new JSONObject();
			motherObj.put("id", motherID);
			motherObj.put("child", null);
			
			JSONArray array = new JSONArray();
			array.add(motherObj);
			motherTree = new JSONArray();
			motherTree.add(array);
			
			mother = new JSONObject();
			mother.put("tree", motherTree);
			mother.put("COI", 0.00);
			nodes.put(motherID, mother);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}else motherTree = (JSONArray)mother.get("tree");
		
		/*
		 * finding possible matches between fatherTree and motherTree
		 * if same entity found with different child (i.e. different initial and return paths), indicates loop
		 * multiple loops can exist per entity
		 * 
		 * COI = sum of COI contribution of each loop
		 * COI contribution = (0.5^(n-1))(1+f)
		 * n = number of members in loop (determined by index in father and mother trees)
		 * f = COI of ancestor
		 */
		for(int i = 0; i < fatherTree.size(); i++){
			
			for(int j = 0; j < motherTree.size(); j++){
				JSONArray genF = (JSONArray)fatherTree.get(i);
				JSONArray genM = (JSONArray)motherTree.get(j);
				
				for(int k = 0; k < genF.size(); k++){
					
					JSONObject entityObjF = (JSONObject)genF.get(k);
					int count = 0;
					
					for(int m = 0; m < genM.size(); m++){
						JSONObject entityObjM = (JSONObject)genM.get(m);
						if(entityObjF.get("id").equals(entityObjM.get("id")) && !entityObjF.get("child").equals(entityObjM.get("child"))) count++;
					}
					
					for(int n = 0; n < count; n++){
						nums.add(i+j+1);
						JSONObject entity = (JSONObject)nodes.get(entityObjF.get("id"));
						if(entity != null) COIs.add((Double)entity.get("COI"));
						else COIs.add(0.00);
					}
					
				}
			}
		}

		for(int i = 0; i < nums.size(); i++) COI += Math.pow(0.5, nums.get(i))*(1+COIs.get(i));
		double random = Math.random();
		
		if(random < COI){
			childEntity.remove();
			switch(childEntity.getType()){
				case PIG:
					Creeper creeper = (Creeper)world.spawnEntity(location, EntityType.CREEPER);
					creeper.ignite();
					break;
				default:
					LivingEntity ravager = (LivingEntity)world.spawnEntity(location, EntityType.RAVAGER);
					ravager.setCustomName(childEntity.getType().toString());
					break;
			}
		}else{
			JSONObject child = new JSONObject();
			JSONArray childTree = new JSONArray();
			
			JSONObject childObj = new JSONObject();
			JSONArray array0 = new JSONArray();
			
			JSONObject fatherObj = new JSONObject();
			JSONObject motherObj = new JSONObject();
			JSONArray array1 = new JSONArray();
			
			childObj.put("id", childID);
			childObj.put("child", null);
			
			array0.add(childObj);
			childTree.add(array0);
			
			fatherObj.put("id", fatherID);
			fatherObj.put("child", childID);
			motherObj.put("id", motherID);
			motherObj.put("child", childID);
			
			array1.add(fatherObj);
			array1.add(motherObj);
			childTree.add(array1);
			
			for(int i = 1; i < Math.max(fatherTree.size(), motherTree.size()); i++){
				JSONArray jointArray = new JSONArray();
				if(fatherTree.size() > i) jointArray.addAll((JSONArray)fatherTree.get(i));
				if(motherTree.size() > i) jointArray.addAll((JSONArray)motherTree.get(i));
				childTree.add(jointArray);
			}
			
			child.put("tree", childTree);
			child.put("COI", COI);
			nodes.put(childID, child);
			
			FileWriter filewriter = new FileWriter(file);
			filewriter.write(nodes.toJSONString());
			filewriter.close();
		}
	}
}