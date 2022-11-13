package up.visulog.config;

import java.util.HashMap;
import java.util.Map;

public interface PluginConfig {
	HashMap<String,String> map = initialise();
	
	private static HashMap<String,String> initialise(){
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("countCommits","This plugin counts the number of commits in a branch for each user.");
		map.put("countLines","This plugin counts the number of added and deleted lines in a branch for each user.");
		map.put("countModifiedLines","This plugin counts the number of modified Lines in a branch");
		return map;
	}
	
	default Map<String, String> getConfig() {
		return new HashMap<String,String>();
	}

}
