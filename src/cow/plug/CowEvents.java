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
	//childID: [parents IDs][grandparents IDs][great grandparents IDs]...
	
	public CowEvents(Cow main){
		CowEvents.main = main;
		CowEvents.trees = Conversion.yamlToHashMap(main.treeConfig);
	}
	
	@EventHandler
	public static void onEntityBreed(EntityBreedEvent evt){
		Ageable child = (Ageable) evt.getEntity();
		String childId = child.getUniqueId().toString();
		String fatherId = evt.getFather().getUniqueId().toString();
		String motherId = evt.getMother().getUniqueId().toString();
		Location location = evt.getFather().getLocation();
		World world = evt.getFather().getWorld();
		
		
		ArrayList<String[]> familyList = new ArrayList<String[]>();
		familyList.add(new String[]{fatherId, motherId});
		String[][] fatherBranch = trees.containsKey(fatherId) ? trees.get(fatherId) : new String[0][0];
		String[][] motherBranch = trees.containsKey(motherId) ? trees.get(motherId) : new String[0][0];
		
		for(int i = 0; i < fatherBranch.length || i < motherBranch.length; i++){
			if(i < fatherBranch.length){
				if(i >= motherBranch.length) familyList.add(fatherBranch[i]);
				else{
					ArrayList<String> list = new ArrayList<String>(Arrays.asList(fatherBranch[i]));
					list.addAll(Arrays.asList(motherBranch[i]));
					familyList.add(list.toArray(new String[0]));
				}
			}else familyList.add(motherBranch[i]);
		}
		
		String[][] tree = familyList.toArray(new String[0][0]);
		trees.put(childId, tree);
		main.treeConfig.set(childId, tree);
		try{ main.treeConfig.save(main.treeYml); }catch(IOException err){ err.printStackTrace(); }
		
		
		Map<String, Leaf> leaves = new HashMap<>();
		for(int i = 0; i < tree.length; i++){
			for(int j = 0; j < tree[i].length; j++){
				String leafId = tree[i][j];
				double chance = Math.pow(2, -i-1);
				if(!leaves.containsKey(leafId)) leaves.put(leafId, new Leaf(false, chance));
				else{
					Leaf leaf = leaves.get(leafId);
					leaf.repeated = true;
					leaf.chance += chance;
				}
			}
		}
		
		leaves.get(fatherId).chance = (leaves.get(fatherId).chance - 0.5) * 2;
		leaves.get(motherId).chance = (leaves.get(motherId).chance - 0.5) * 2;
		
		Fraction fraction = new Fraction(0, 0);
		leaves.forEach((key, value) -> {
			if(value.repeated){
				fraction.numer += value.chance;
				fraction.denom += 1;
			}
		});
		
		if(fraction.denom != 0){
			double chance = fraction.numer/fraction.denom;
			if(Math.random() < chance){
				child.remove();
				switch(child.getType()){
					case PIG:
						Creeper creeper = (Creeper) world.spawnEntity(location, EntityType.CREEPER);
						creeper.ignite();
						break;
					default:
						LivingEntity ravager = (LivingEntity) world.spawnEntity(location, EntityType.RAVAGER);
						ravager.setCustomName(child.getType().toString());
						break;
				}
			}
		}
	}
}