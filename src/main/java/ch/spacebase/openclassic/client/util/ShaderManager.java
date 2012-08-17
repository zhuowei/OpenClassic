package ch.spacebase.openclassic.client.util;

import java.util.HashMap;
import java.util.Map;

public class ShaderManager {

	private static final Map<String, Shader> shaders = new HashMap<String, Shader>();
	
	public static void setup() {
		// TODO: Register shaders when had.
	}
	
	public static void register(String name, String file) {
		Shader shad = new Shader(file);
		shad.compileShader();
		
		shaders.put(name, shad);
	}
	
	public static void use(String name) {
		shaders.get(name).begin();
	}
	
	public static void stop(String name) {
		shaders.get(name).end();
	}
	
	public static void cleanup() {
		for(Shader shader : shaders.values()) {
			shader.clean();
		}
		
		shaders.clear();
	}
	
}
