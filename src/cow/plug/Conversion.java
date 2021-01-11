package cow.plug;

import java.util.*;
import org.bukkit.configuration.file.FileConfiguration;

public class Conversion {
	public static String[][] listTo2dArray(List<?> list){
		if(list == null) return null;
		ArrayList<String[]> arrList = new ArrayList<String[]>();
		for(int i = 0; i < list.size(); i++){
			ArrayList<String> subArr = new ArrayList<String>();
			List<?> subList = ((List<?>) list.get(i));
			for(int j = 0; j < subList.size(); j++) subArr.add((String) subList.get(j));
			arrList.add(subArr.toArray(new String[0]));
		}
		return arrList.toArray(new String[0][0]);
	}
	public static Map<String, String[][]> yamlToHashMap(FileConfiguration fileConfig){
		Map<String, String[][]> map = new HashMap<>();
		fileConfig.getKeys(true).size();
		String[] keys = fileConfig.getKeys(false).toArray(new String[0]);
		for(int i = 0; i < keys.length; i++){
			map.put(keys[i], listTo2dArray(fileConfig.getList(keys[i])));
		}
		return map;
	}
}
