package com.mojang.minecraft.render;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.GameSettings;
import com.mojang.minecraft.render.animation.AnimatedTexture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public class TextureManager {

	public HashMap<String, Integer> textures = new HashMap<String, Integer>();
	public HashMap<String, Boolean> jarTexture = new HashMap<String, Boolean>();
	public HashMap<Integer, BufferedImage> textureImgs = new HashMap<Integer, BufferedImage>();
	public IntBuffer textureBuffer = BufferUtils.createIntBuffer(1);
	public List<AnimatedTexture> animations = new ArrayList<AnimatedTexture>();
	public GameSettings settings;

	public TextureManager(GameSettings settings) {
		this.settings = settings;
	}

	public final int bindTexture(String file) {
		return this.bindTexture(file, true);
	}

	public final int bindTexture(String file, boolean jar) {
		if (this.textures.get(file) != null) {
			return this.textures.get(file);
		} else {
			try {
				this.textureBuffer.clear();
				GL11.glGenTextures(this.textureBuffer);
				int textureId = this.textureBuffer.get(0);

				BufferedImage img = null;
				if(!jar) {
					img = ImageIO.read(new FileInputStream(file));
				} else {
					if(this.settings.texturePack.equals("none")) {
						img = ImageIO.read(TextureManager.class.getResourceAsStream(file));
					} else {
						ZipFile zip = new ZipFile(new File(OpenClassic.getClient().getDirectory(), "texturepacks/" + this.settings.texturePack));
						if(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file) != null) {
							img = ImageIO.read(zip.getInputStream(zip.getEntry(file.startsWith("/") ? file.substring(1, file.length()) : file)));
						} else {
							img = ImageIO.read(TextureManager.class.getResourceAsStream(file));
						}
						
						zip.close();
					}
				}
				
				this.bindTexture(img, textureId);
				this.textures.put(file, textureId);
				this.jarTexture.put(file, jar);
				return textureId;
			} catch (IOException e) {
				throw new RuntimeException("Failed to bind texture!", e);
			}
		}
	}

	// TODO: Add texture packs to post
	public final int bindTexture(BufferedImage image) {
		this.textureBuffer.clear();
		GL11.glGenTextures(this.textureBuffer);
		int textureId = this.textureBuffer.get(0);
		this.bindTexture(image, textureId);
		this.textureImgs.put(textureId, image);
		return textureId;
	}

	public void bindTexture(BufferedImage image, int textureId) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		if(this.settings.smoothing && GeneralUtils.getMinecraft().mipmapMode > 0) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 2);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST_MIPMAP_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}

		int[] pixels = new int[image.getWidth() * image.getHeight()];
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

		for(int y = 0; y < image.getHeight(); y++){
			for(int x = 0; x < image.getWidth(); x++){
				int pixel = pixels[y * image.getWidth() + x];
				int red = (pixel >> 16) & 0xFF;
				int blue = pixel & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int alpha = (pixel >> 24) & 0xFF;

				if (this.settings.anaglyph) {
					green = (red * 30 + green * 70) / 100;
					blue = (red * 30 + blue * 70) / 100;
					red = (red * 30 + green * 59 + blue * 11) / 100;
				}

				buffer.put((byte) red);
				buffer.put((byte) green);
				buffer.put((byte) blue);
				buffer.put((byte) alpha);
			}
		}

		buffer.flip();
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
	
		if(this.settings.smoothing) {
			switch(GeneralUtils.getMinecraft().mipmapMode) {
			case 1:
				GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
				break;
			case 2:
				EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
				break;
			}
		}
	}

	public final void addAnimatedTexture(AnimatedTexture animation) {
		this.animations.add(animation);
		animation.animate();
	}

	public void clear() {
		this.textures.clear();
	}
}
