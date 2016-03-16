package container.mosaic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads a JSON file containing named arrays which act as a model
 * describing layout dimensions for a given rectangle. Each array
 * is the model for a single layout, and each element in that array
 * is a comma separated String containing either 4 or optionally 5
 * comma separated values - the 5 value String containing an "id"
 * in the zeroth slot. The other 4 slots are specifications for 
 * x, y, width, height values expressed as percentages of the overall
 * surface dimensions. see {@link MosaicEngineImpl#addAll} for more details.
 * 
 * This loader will load the specified file {@link ModelLoader#ModelLoader(String)}
 * {@link ModelLoader#setPath(String)} and store the definitions according
 * to the names given in the JSON file. The "models" may later be retrieved
 * individually via the {@link ModelLoader#getModel(String)} method or 
 * iteratively via the {@link ModelLoader#getModelNames()} method.
 * 
 * @author David Ray
 *
 */
public class ModelLoader {
	String filePath;
	String homeFilePath;
	
	private Map<String, String[]> namedModels = new TreeMap<String, String[]>();
	
	public ModelLoader(String filepath) {
		this.filePath = filepath;
		loadModels();
	}
	
	public void setPath(String pathToFile) {
		this.filePath = pathToFile;
	}
	
	public String getCurrentPath() {
		return filePath;
	}
	
	public void reload(String filePath) {
		this.filePath = filePath;
		loadModels();
	}
	
	public String[] getModel(String name) {
		return namedModels.get(name);
	}
	
	public List<String> getModelNames() {
		return new ArrayList<String>(namedModels.keySet());
	}
	
	private void loadModels() {
	    if(filePath.indexOf(File.separator) != -1) {
	        loadFromAbsolute(filePath);
	    }else{
    		try {
    			InputStream fis = getClass().getClassLoader().getResourceAsStream(filePath);
    			load(fis);
    		}catch(Exception e) {
    			e.printStackTrace();
    		}
	    }
	}
	
	private void loadFromAbsolute(String path) {
	    URL url = null;
	    try {
	        url = new File(path).toURI().toURL();
	        InputStream stream = url.openStream();
	        load(stream);
	    }catch(Exception e) { e.printStackTrace(); }
	}
	
	private void load(InputStream stream) throws JsonParseException, JsonMappingException, IOException {
	    ObjectMapper om = new ObjectMapper();
	    JsonNode root = om.readValue(stream, JsonNode.class);
        Iterator<String> it = root.fieldNames();
        for(;it.hasNext();) {
            String modelName = it.next();
            JsonNode node = root.get(modelName);
            namedModels.put(modelName, nodeToModel(node));
        }
	}
	
	private String[] nodeToModel(JsonNode node) {
		String[] model = new String[node.size()];
    	Iterator<JsonNode> iter = node.elements();
    	int j = 0;
		while(iter.hasNext()) {
			model[j++] = iter.next().toString().replaceAll("\\\"", "");
		}
		return model;
	}
}
