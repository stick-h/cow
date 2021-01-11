package cow.plug;

import java.util.*;
import java.io.IOException;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.loot.*;
import org.bukkit.potion.*;
import org.bukkit.entity.*;

public class CowEvents implements Listener {
	private static Cow main;
	static Map<String, String[][]> trees;
	public CowEvents(Cow main){
		CowEvents.main = main;
		CowEvents.trees = Conversion.yamlToHashMap(main.treeConfig);;
	}
	
	@EventHandler
	public static void onEntityBreed(EntityBreedEvent evt){
		LivingEntity child = evt.getEntity();
		String childId = child.getUniqueId().toString();
		String fatherId = evt.getFather().getUniqueId().toString();
		String motherId = evt.getMother().getUniqueId().toString();
		Location location = evt.getFather().getLocation();
		World world = evt.getFather().getWorld();
		
		ArrayList<String[]> familyList = new ArrayList<String[]>();
		familyList.add(new String[] {fatherId, motherId});
		String[][] fatherBranch = trees.get(fatherId);
		String[][] motherBranch = trees.get(motherId);
		
		if(fatherBranch != null){
			for(int i = 0; i < fatherBranch.length; i++){
				ArrayList<String> subBranch = new ArrayList<String>();
	
				for(int j = 0; j < fatherBranch[i].length; j++) subBranch.add(fatherBranch[i][j]);
				
				if(motherBranch != null) if(motherBranch.length > i){
						for(int j = 0; j < motherBranch[i].length; j++) subBranch.add(motherBranch[i][j]);
				}
				familyList.add(subBranch.toArray(new String[0]));
			} 
		}
		
		if(motherBranch != null){
			int init;
			
			if(fatherBranch == null) init = 0;
			else if(fatherBranch.length < motherBranch.length) init = fatherBranch.length;
			else init = motherBranch.length;
			
			for(int i = init; i < motherBranch.length; i++) familyList.add(motherBranch[i]);
		}
		
		String[][] tree = familyList.toArray(new String[0][0]);
		trees.put(childId, tree);
		

		
		for(Map.Entry<String, String[][]> entry : trees.entrySet()){
	        if(main.treeConfig.getString(entry.getKey()) == null){
	        	main.treeConfig.set(entry.getKey(), entry.getValue());
	        }
	    }
		try{
			main.treeConfig.save(main.treeYml);
		}catch(IOException err){
			err.printStackTrace();
		}
		
		
		
		Map<String, Leaf> leaves = new HashMap<>();	
		for(int i = 0; i < tree.length; i++){
			for(int j = 0; j < tree[i].length; j++){
				double chance = Math.pow(2, -i-1);
				if(i == 0) chance = 0;
				
				String leafId = tree[i][j];
				if(!leaves.containsKey(leafId)) leaves.put(leafId, new Leaf(false, chance));
				else{
					Leaf leaf = leaves.get(leafId);
					leaves.put(leafId, new Leaf(true, leaf.chance + chance));
				}
			}
		}
		
		Leaf fatherLeaf = leaves.get(fatherId);
		Leaf motherLeaf = leaves.get(motherId);
		if(fatherLeaf.repeated) leaves.put(fatherId, new Leaf(true, fatherLeaf.chance*2));
		if(motherLeaf.repeated) leaves.put(motherId, new Leaf(true, motherLeaf.chance*2));
		
		Map<String, Fraction> fractions = new HashMap<>();
		
		fractions.put("inbred", new Fraction(0, 0));
		leaves.forEach((k,v) -> {
			Fraction inbredFrac = fractions.get("inbred");
			if(v.repeated) fractions.put("inbred", new Fraction(inbredFrac.numer + v.chance, inbredFrac.denom + 1));
		});
		
		Fraction inbredFrac = fractions.get("inbred");
		if(inbredFrac.denom != 0){
			double inbredChance = inbredFrac.numer/inbredFrac.denom;
			if(Math.random() < inbredChance){
				EntityType childType = child.getType();
				child.remove();
				if(Math.random() < 0.5){
						Ageable inbred = (Ageable) world.spawnEntity(location, childType);
						switch((int)Math.floor(Math.random()*4)){
							case 0:
								inbred.setLootTable((LootTable) LootTables.ZOMBIE.getLootTable());
								break;
							case 1:
								inbred.setAgeLock(true);
								break;
							case 2:
								inbred.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 1000000, 4));
								break;
							case 3:
								inbred.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1000000, 0));
								break;
						}
						inbred.setBaby();
				}else{
					if(childType != EntityType.PIG){
						LivingEntity inbred = (LivingEntity) world.spawnEntity(location, EntityType.RAVAGER);
						inbred.setCustomName(childType.toString());
					}
					else{
						if(Math.random() < 0.5){
							Ageable inbred = (Ageable) world.spawnEntity(location, EntityType.HOGLIN);
							inbred.setBaby();
						}else{
							Creeper inbred = (Creeper) world.spawnEntity(location, EntityType.CREEPER);
							inbred.ignite();
						}
					}
				}
			}
		}
	}
}