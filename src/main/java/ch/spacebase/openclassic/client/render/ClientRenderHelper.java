package ch.spacebase.openclassic.client.render;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.block.Block;
import ch.spacebase.openclassic.api.block.BlockFace;
import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.CuboidModel;
import ch.spacebase.openclassic.api.block.model.LiquidModel;
import ch.spacebase.openclassic.api.block.model.Model;
import ch.spacebase.openclassic.api.block.model.Quad;
import ch.spacebase.openclassic.api.block.model.SubTexture;
import ch.spacebase.openclassic.api.block.model.Texture;
import ch.spacebase.openclassic.api.render.RenderHelper;
import ch.spacebase.openclassic.client.ClassicClient;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.TerrainParticle;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.ShapeRenderer;

/**
 * @author Steveice10 <Steveice10@gmail.com>
 */
public class ClientRenderHelper extends RenderHelper {
	
	private static final Random rand = new Random();
	
	public static ClientRenderHelper getHelper() {
		return (ClientRenderHelper) helper;
	}
	
	public void drawDirtBG() {
		this.bindTexture("/dirt.png", true);

		int width = Display.getWidth() * 240 / Display.getHeight();
		int height = Display.getHeight() * 240 / Display.getHeight();

		ShapeRenderer.instance.reset();
		ShapeRenderer.instance.addColor(4210752);
		ShapeRenderer.instance.addTexturedPoint(0, height, 0, 0, height / 32);
		ShapeRenderer.instance.addTexturedPoint(width, height, 0, width / 32, height / 32);
		ShapeRenderer.instance.addTexturedPoint(width, 0, 0, width / 32, 0);
		ShapeRenderer.instance.addTexturedPoint(0, 0, 0, 0, 0);
		ShapeRenderer.instance.draw();
	}
	
	public void renderText(String text, int x, int y) {
		this.renderText(text, x, y, true);
	}
	
	public void renderText(String text, int x, int y, boolean xCenter) {
		this.renderText(text, x, y, 16777215, xCenter);
	}
	
	public void renderText(String text, int x, int y, int color) {
		this.renderText(text, x, y, color, true);
	}
	
	public void renderText(String text, int x, int y, int color, boolean xCenter) {
		FontRenderer renderer = ((ClassicClient) OpenClassic.getClient()).getMinecraft().fontRenderer;
		
		if(xCenter) {
			renderer.renderWithShadow(text, x - renderer.getWidth(text) / 2, y, color);
		} else {
			renderer.renderWithShadow(text, x, y, color);
		}
	}
	
	public void drawBox(int x1, int y1, int x2, int y2, int color) {
		float alpha = (color >>> 24) / 255F;
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		this.glColor(red, green, blue, alpha);
		
		ShapeRenderer.instance.reset();
		ShapeRenderer.instance.addPoint(x1, y2, 0);
		ShapeRenderer.instance.addPoint(x2, y2, 0);
		ShapeRenderer.instance.addPoint(x2, y1, 0);
		ShapeRenderer.instance.addPoint(x1, y1, 0);
		ShapeRenderer.instance.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color) {
		this.color(x1, y1, x2, y2, color, color);
	}
	
	public void color(int x1, int y1, int x2, int y2, int color, int fadeTo) {
		float alpha = (color >>> 24) / 255F;
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		
		float alpha2 = (fadeTo >>> 24) / 255F;
		float red2 = (fadeTo >> 16 & 255) / 255F;
		float green2 = (fadeTo >> 8 & 255) / 255F;
		float blue2 = (fadeTo & 255) / 255F;
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glBegin(7);
		
		this.glColor(red, green, blue, alpha);
		GL11.glVertex2f(x2, y1);
		GL11.glVertex2f(x1, y1);
		this.glColor(red2, green2, blue2, alpha2);
		GL11.glVertex2f(x1, y2);
		GL11.glVertex2f(x2, y2);
		
		GL11.glEnd();
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}
	
	public void drawImage(String file, int x, int y) {
		try {
			Image img = ImageIO.read(new File(file));
			drawImage(x, y, 0, 0, 0, img.getWidth(null), img.getHeight(null));
		} catch(IOException e) {
			System.out.println("Failed to load img width/height for image " + file + ".");
			e.printStackTrace();
		}
	}
	
	public void drawImage(int x, int y, int width, int height) {
		drawImage(x, y, 0, 0, width, height);
	}
	
	public void drawImage(int x, int y, int imgX, int imgY, int imgWidth, int imgHeight) {
		drawImage(x, y, 0, imgX, imgY, imgWidth, imgHeight);
	}
	
	public void drawImage(int x, int y, int z, int imgX, int imgY, int imgWidth, int imgHeight) {
		ShapeRenderer.instance.reset();
		ShapeRenderer.instance.addTexturedPoint(x, (y + imgHeight), z, imgX * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint((x + imgWidth), (y + imgHeight), z, (imgX + imgWidth) * 0.00390625F, (imgY + imgHeight) * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint((x + imgWidth), y, z, (imgX + imgWidth) * 0.00390625F, imgY * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x, y, z, imgX * 0.00390625F, imgY * 0.00390625F);
		ShapeRenderer.instance.draw();
	}

	@Override
	public void bindTexture(String file, boolean jar) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, GeneralUtils.getMinecraft().textureManager.bindTexture(file, jar));
	}

	@Override
	public void glColor(float red, float green, float blue, float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}
	
	@Override
	public int getDisplayWidth() {
		return Display.getWidth();
	}
	
	@Override
	public int getDisplayHeight() {
		return Display.getHeight();
	}

	@Override
	public void drawQuad(Quad quad, int x, int y, int z) {
		this.drawQuad(quad, x, y, z, 1);
	}
	
	@Override
	public void drawQuad(Quad quad, int x, int y, int z, float brightness) {
		ShapeRenderer.instance.reset();
		this.bindTexture(quad.getTexture().getParent().getTexture(), quad.getTexture().getParent().isInJar());
		ShapeRenderer.instance.addColor(brightness, brightness, brightness);
		
		int y1 = quad.getTexture().getY1();
		int y2 = quad.getTexture().getY2();
		
		if(quad.getParent() instanceof CuboidModel && !(quad.getParent() instanceof LiquidModel) && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = (int) (y1 + quad.getVertex(0).getY() * quad.getTexture().getParent().getSubTextureHeight());
			y2 = (int) (y1 + quad.getVertex(1).getY() * quad.getTexture().getParent().getSubTextureHeight());
		}
		
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), quad.getTexture().getX2() * 0.00390625F, y2 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), quad.getTexture().getX2() * 0.00390625F, y1 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), quad.getTexture().getX1() * 0.00390625F, y1 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), quad.getTexture().getX1() * 0.00390625F, y2 * 0.00390625F);
		
		ShapeRenderer.instance.draw();
	}
	
	public void drawCracks(Quad quad, int x, int y, int z, int crackTexture) {
		ShapeRenderer.instance.reset();
		this.bindTexture(VanillaBlock.TERRAIN.getTexture(), true);
		
		SubTexture texture = quad.getTexture().getParent().getSubTexture(crackTexture);
		int y1 = texture.getY1();
		int y2 = texture.getY2();
		
		if(quad.getParent() instanceof CuboidModel && quad.getId() > 1 && (quad.getVertex(0).getY() > 0 || quad.getVertex(1).getY() < 1)) {
			y1 = (int) (y1 + quad.getVertex(0).getY() * texture.getParent().getSubTextureHeight());
			y2 = (int) (y1 + quad.getVertex(1).getY() * texture.getParent().getSubTextureHeight());
		}
		
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(0).getX(), y + quad.getVertex(0).getY(), z + quad.getVertex(0).getZ(), texture.getX2() * 0.00390625F, y2 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(1).getX(), y + quad.getVertex(1).getY(), z + quad.getVertex(1).getZ(), texture.getX2() * 0.00390625F, y1 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(2).getX(), y + quad.getVertex(2).getY(), z + quad.getVertex(2).getZ(), texture.getX1() * 0.00390625F, y1 * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + quad.getVertex(3).getX(), y + quad.getVertex(3).getY(), z + quad.getVertex(3).getZ(), texture.getX1() * 0.00390625F, y2 * 0.00390625F);
		
		ShapeRenderer.instance.draw();
	}
	
	@Override
	public void drawTexture(Texture texture, int x, int y) {
		this.drawTexture(texture, x, y, 0);
	}
	
	@Override
	public void drawTexture(Texture texture, int x, int y, int z) {
		ShapeRenderer.instance.reset();
		this.bindTexture(texture.getTexture(), texture.isInJar());
		
		this.glColor(1, 1, 1, 1);
		ShapeRenderer.instance.addTexturedPoint(x, y, z, 0, 0);
		ShapeRenderer.instance.addTexturedPoint(x, y + texture.getHeight(), z, 0, texture.getHeight() * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + texture.getWidth(), y + texture.getHeight(), z, texture.getWidth() * 0.00390625F, texture.getHeight() * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + texture.getWidth(), y, z, texture.getWidth() * 0.00390625F, 0);
		
		ShapeRenderer.instance.draw();
	}
	
	@Override
	public void drawSubTex(SubTexture texture, int x, int y) {
		this.drawSubTex(texture, x, y, 0);
	}
	
	@Override
	public void drawSubTex(SubTexture texture, int x, int y, int z) {
		ShapeRenderer.instance.reset();
		this.bindTexture(texture.getParent().getTexture(), texture.getParent().isInJar());
		
		this.glColor(1, 1, 1, 1);
		ShapeRenderer.instance.addTexturedPoint(x, y, z, texture.getX1() * 0.00390625F, texture.getY1() * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x, y + (texture.getY2() - texture.getY1()), z, texture.getX1() * 0.00390625F, texture.getY2() * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + (texture.getX2() - texture.getX1()), y + (texture.getY2() - texture.getY1()), z, texture.getX2() * 0.00390625F, texture.getY2() * 0.00390625F);
		ShapeRenderer.instance.addTexturedPoint(x + (texture.getX2() - texture.getX1()), y, z, texture.getX2() * 0.00390625F, texture.getY1() * 0.00390625F);
		
		ShapeRenderer.instance.draw();
	}
	
	@Override
	public boolean canRenderSide(Block block, BlockFace face) {
		if(block == null) return false;
		BlockType type = block.getType();
		
		if(type instanceof CustomBlock) {
			type = ((CustomBlock) type).getFallback();
		}
		
		if(type instanceof VanillaBlock) {
			switch((VanillaBlock) type) {
			case GLASS: {
				return block.getRelative(face) == null || (block.getRelative(face).getType() != type && !this.isSolidTile(block.getRelative(face)));
			}
			case WATER:
			case LAVA:
			case STATIONARY_WATER:
			case STATIONARY_LAVA: {
				Block relative = block.getRelative(face);
				if(relative == null) {
					return false;
				}
				
				if(Level.toMoving(relative.getType()) == Level.toMoving(type)) {
					return false;
				}
				
				//int x = relative.getPosition().getBlockX();
				//int y = relative.getPosition().getBlockY();
				//int z = relative.getPosition().getBlockZ();
				//return x >= 0 && y >= 0 && z >= 0 && x < block.getLevel().getWidth() && z < block.getLevel().getDepth() && Level.toMoving(relative.getType()) != Level.toMoving(block.getType()) && (face == BlockFace.UP && (block.getLevel().getBlockTypeAt(x - 1, y, z) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x + 1, y, z) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x, y, z - 1) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x, y, z + 1) == VanillaBlock.AIR) || !this.isSolidTile(relative));
				return !this.isSolidTile(relative);
				//return face == BlockFace.UP && (block.getLevel().getBlockTypeAt(x - 1, y, z) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x + 1, y, z) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x, y, z - 1) == VanillaBlock.AIR || block.getLevel().getBlockTypeAt(x, y, z + 1) == VanillaBlock.AIR) || !this.isSolidTile(relative);
			}
			case SLAB: {
				return block.getRelative(face) == null || face == BlockFace.UP || (!this.isSolidTile(block.getRelative(face)) && (face == BlockFace.DOWN || block.getRelative(face).getType() != VanillaBlock.SLAB));
			}
			default:
				return block.getRelative(face) == null || !this.isSolidTile(block.getRelative(face));
			}
		}
		
		return true;
	}
	
	private boolean isSolidTile(Block block) {
		if(block == null) return true;
		return ((ClientLevel) block.getLevel()).getHandle().isSolidTile(block.getPosition().getBlockX(), block.getPosition().getBlockY(), block.getPosition().getBlockZ());
	}
	
	@Override
	public float getBrightness(BlockType main, int x, int y, int z) {
		return main == VanillaBlock.LAVA || main == VanillaBlock.STATIONARY_LAVA ? 100 : ((ClientLevel) OpenClassic.getClient().getLevel()).getHandle().getBrightness(x, y, z);
	}
	
	@Override
	public void renderPreview(Model model) {
		ShapeRenderer.instance.reset();

		for (int side = 0; side < model.getQuads().size(); ++side) {
			if (side == 0) {
				ShapeRenderer.instance.glNormal3f(0, 1, 0);
			}

			if (side == 1) {
				ShapeRenderer.instance.glNormal3f(0, -1, 0);
			}

			if (side == 2) {
				ShapeRenderer.instance.glNormal3f(0, 0, 1);
			}

			if (side == 3) {
				ShapeRenderer.instance.glNormal3f(0, 0, -1);
			}

			if (side == 4) {
				ShapeRenderer.instance.glNormal3f(1, 0, 0);
			}

			if (side == 5) {
				ShapeRenderer.instance.glNormal3f(-1, 0, 0);
			}

			model.getQuad(side).render(0, 0, 0, 1);
		}

		ShapeRenderer.instance.draw();
	}
	
	public void spawnDestructionParticles(BlockType block, Level level, int x, int y, int z, ParticleManager particles) {
		for (int xMod = 0; xMod < 4; ++xMod) {
			for (int yMod = 0; yMod < 4; ++yMod) {
				for (int zMod = 0; zMod < 4; ++zMod) {
					float particleX = x + (xMod + 0.5F) / 4;
					float particleY = y + (yMod + 0.5F) / 4;
					float particleZ = z + (zMod + 0.5F) / 4;
					particles.spawnParticle(new TerrainParticle(level, particleX, particleY, particleZ, particleX - x - 0.5F, particleY - y - 0.5F, particleZ - z - 0.5F, block));
				}
			}
		}
	}

	public final void spawnBlockParticles(Level level, int x, int y, int z, int side, ParticleManager particles) {
		Model model = Blocks.fromId(level.getTile(x, y, z)).getModel();
		
		float particleX = x + rand.nextFloat() * (model.getSelectionBox().getX2() - model.getSelectionBox().getX1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox().getX1();
		float particleY = y + rand.nextFloat() * (model.getSelectionBox().getY2() - model.getSelectionBox().getY1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox().getY1();
		float particleZ = z + rand.nextFloat() * (model.getSelectionBox().getZ2() - model.getSelectionBox().getZ1() - 0.1F * 2.0F) + 0.1F + model.getSelectionBox().getZ1();
		if (side == 0) {
			particleY = y + model.getSelectionBox().getY1() - 0.1F;
		}

		if (side == 1) {
			particleY = y + model.getSelectionBox().getY2() + 0.1F;
		}

		if (side == 2) {
			particleZ = z + model.getSelectionBox().getZ1() - 0.1F;
		}

		if (side == 3) {
			particleZ = z + model.getSelectionBox().getZ2() + 0.1F;
		}

		if (side == 4) {
			particleX = x + model.getSelectionBox().getX1() - 0.1F;
		}

		if (side == 5) {
			particleX = x + model.getSelectionBox().getX2() + 0.1F;
		}

		particles.spawnParticle((new TerrainParticle(level, particleX, particleY, particleZ, 0.0F, 0.0F, 0.0F, Blocks.fromId(level.getTile(x, y, z)))).setPower(0.2F).scale(0.6F));
	}
	
}
