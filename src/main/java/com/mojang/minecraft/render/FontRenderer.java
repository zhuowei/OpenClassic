package com.mojang.minecraft.render;

import com.mojang.minecraft.GameSettings;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

public final class FontRenderer {

	private int[] font = new int[256];
	private int fontId = 0;
	private GameSettings settings;

	public FontRenderer(GameSettings settings, String fontImage, TextureManager textures) {
		this.settings = settings;

		BufferedImage font;
		
		try {
			font = ImageIO.read(TextureManager.class.getResourceAsStream(fontImage));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		int width = font.getWidth();
		int height = font.getHeight();
		int[] fontData = new int[width * height];
		font.getRGB(0, 0, width, height, fontData, 0, width);

		for (int pixel = 0; pixel < 128; ++pixel) {
			int var6 = pixel % 16;
			int var7 = pixel / 16;
			int var8 = 0;

			for (boolean var9 = false; var8 < 8 && !var9; ++var8) {
				int var10 = (var6 << 3) + var8;
				var9 = true;

				for (int var11 = 0; var11 < 8 && var9; ++var11) {
					int var12 = ((var7 << 3) + var11) * width;
					if ((fontData[var10 + var12] & 255) > 128) {
						var9 = false;
					}
				}
			}

			if (pixel == 32) {
				var8 = 4;
			}

			this.font[pixel] = var8;
		}

		this.fontId = textures.bindTexture(fontImage);
	}

	public final void renderWithShadow(String text, int x, int y, int color) {
		this.render(text, x + 1, y + 1, color, true);
		this.renderNoShadow(text, x, y, color);
	}

	public final void renderNoShadow(String text, int x, int y, int color) {
		this.render(text, x, y, color, false);
	}

	private void render(String text, int x, int y, int color, boolean shadow) {
		if (text != null) {
			char[] chars = text.toCharArray();
			if (shadow) {
				color = (color & 16579836) >> 2;
			}

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.fontId);
			ShapeRenderer.instance.reset();
			ShapeRenderer.instance.addColor(color);
			int var7 = 0;

			for (int count = 0; count < chars.length; ++count) {
				if (chars[count] == '&' && chars.length > count + 1) {
					int code = "0123456789abcdef".indexOf(chars[count + 1]);
					if (code < 0) {
						code = 15;
					}

					int var9 = (code & 8) << 3;
					int var10 = (code & 1) * 191 + var9;
					int var11 = ((code & 2) >> 1) * 191 + var9;
					int blue = ((code & 4) >> 2) * 191 + var9;
					if (this.settings.anaglyph) {
						var9 = (code * 30 + var11 * 59 + var10 * 11) / 100;
						var11 = (code * 30 + var11 * 70) / 100;
						var10 = (code * 30 + var10 * 70) / 100;
						blue = var9;
					}

					int c = blue << 16 | var11 << 8 | var10;
					if (shadow) {
						c = (c & 16579836) >> 2;
					}		

					ShapeRenderer.instance.addColor(c);
					count += 2;
				}

				color = chars[count] % 16 << 3;
				int var9 = chars[count] / 16 << 3;
				float var13 = 7.99F;
				ShapeRenderer.instance.addTexturedPoint((x + var7), y + var13, 0.0F, color / 128.0F, (var9 + var13) / 128.0F);
				ShapeRenderer.instance.addTexturedPoint((x + var7) + var13, y + var13, 0.0F, (color + var13) / 128.0F, (var9 + var13) / 128.0F);
				ShapeRenderer.instance.addTexturedPoint((x + var7) + var13, y, 0.0F, (color + var13) / 128.0F, var9 / 128.0F);
				ShapeRenderer.instance.addTexturedPoint((x + var7), y, 0.0F, color / 128.0F, var9 / 128.0F);
				var7 += this.font[chars[count]];
			}

			ShapeRenderer.instance.draw();
		}
	}

	public final int getWidth(String string) {
		if (string == null) {
			return 0;
		} else {
			char[] chars = string.toCharArray();
			int width = 0;

			for (int index = 0; index < chars.length; ++index) {
				if (chars[index] == 38) {
					index++;
				} else {
					width += this.font[chars[index]];
				}
			}

			return width;
		}
	}

	public static String removeBadCharacters(String string) {
		char[] chars = string.toCharArray();
		String result = "";

		for (int index = 0; index < chars.length; ++index) {
			if (chars[index] == 38) {
				index++;
			} else {
				result = result + chars[index];
			}
		}

		return result;
	}
}
