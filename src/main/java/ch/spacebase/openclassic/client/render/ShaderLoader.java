package ch.spacebase.openclassic.client.render;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL20;

public class ShaderLoader {

	public static int load(InputStream in, int type) {
		int shader = GL20.glCreateShader(type);
		if(shader == 0) {
			return 0;
		}

		String source = "";
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while((line = reader.readLine()) != null) {
				source += line + "\n";
			}
		} catch(IOException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		GL20.glShaderSource(shader, source);
		GL20.glCompileShader(shader);

		return shader;
	}

}
