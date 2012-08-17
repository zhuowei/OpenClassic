package ch.spacebase.openclassic.client.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;

import com.mojang.minecraft.Minecraft;


public class Shader {
	
	private int programId;
	private String file;
	private int vert;
	private int frag;
	
	public Shader(String file) {
		this.file = file;
	}
	
	public void compileShader() {
		String v = getCode(this.file + ".vert");
		this.vert = ARBShaderObjects.glCreateShaderObjectARB(ARBVertexShader.GL_VERTEX_SHADER_ARB);
		ByteBuffer b = ByteBuffer.wrap(v.getBytes());
		b.rewind();
		
		ARBShaderObjects.glShaderSourceARB(this.vert, b);
		ARBShaderObjects.glCompileShaderARB(this.vert);
		
		String f = getCode(this.file + ".frag");
		this.frag = ARBShaderObjects.glCreateShaderObjectARB(ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		b = ByteBuffer.wrap(f.getBytes());
		b.rewind();
		
		ARBShaderObjects.glShaderSourceARB(this.frag, b);
		ARBShaderObjects.glCompileShaderARB(this.frag);
		
		this.programId = ARBShaderObjects.glCreateProgramObjectARB();
		ARBShaderObjects.glAttachObjectARB(this.programId, this.vert);
		ARBShaderObjects.glAttachObjectARB(this.programId, this.frag);
		ARBShaderObjects.glLinkProgramARB(this.programId);
		ARBShaderObjects.glValidateProgramARB(this.programId);
	}
	
	
	public void begin() {
		ARBShaderObjects.glUseProgramObjectARB(this.programId);
	}

	public void end() {
		ARBShaderObjects.glUseProgramObjectARB(0);
	}
	
	public void clean() {
		ARBShaderObjects.glDetachObjectARB(this.programId, this.vert);
		ARBShaderObjects.glDetachObjectARB(this.programId, this.frag);
		ARBShaderObjects.glDeleteObjectARB(this.vert);
		ARBShaderObjects.glDeleteObjectARB(this.frag);
		ARBShaderObjects.glDeleteObjectARB(this.programId);
	}
	
	public String getFile() {
		return this.file;
	}
	
	public int getProgram() {
		return this.programId;
	}
	
	private static String getCode(String name) {
		BufferedReader reader = null;
		String code = "";
		
		try {
			reader = new BufferedReader(new InputStreamReader(Minecraft.class.getResourceAsStream("/" + name)));
			String line = null;
			while((line = reader.readLine()) != null) {
				code += line + "\n";
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return code;
	}
	
}
