package com.mojang.minecraft.net;

import com.mojang.minecraft.Minecraft;
import com.mojang.minecraft.mob.HumanoidMob;
import com.mojang.minecraft.net.SkinDownloadThread;
import com.mojang.minecraft.net.PositionUpdate;
import com.mojang.minecraft.render.FontRenderer;
import com.mojang.minecraft.render.TextureManager;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class NetworkPlayer extends HumanoidMob {

	public static final long serialVersionUID = 77479605454997290L;
	private transient List<PositionUpdate> moveQueue = new LinkedList<PositionUpdate>();
	private transient Minecraft minecraft;
	private int xp;
	private int yp;
	private int zp;
	private transient int newTextureId = -1;
	public transient BufferedImage newTexture = null;
	public String name;
	public String displayName;
	private transient TextureManager textures;

	public NetworkPlayer(Minecraft mc, int playerId, String name, int x, int y, int z, float yaw, float pitch) {
		super(mc.level, x, y, z);
		this.minecraft = mc;
		this.displayName = name;
		this.name = FontRenderer.removeBadCharacters(name);;
		this.xp = x;
		this.yp = y;
		this.zp = z;
		this.heightOffset = 0.0F;
		this.pushthrough = 0.8F;
		this.setPos(x / 32.0F, y / 32.0F, z / 32.0F);
		this.xRot = pitch;
		this.yRot = yaw;
		this.armor = this.helmet = false;
		this.renderOffset = 0.6875F;
		(new SkinDownloadThread(this)).start();
		this.allowAlpha = false;
	}

	public void aiStep() {
		int steps = 5;

		while (steps-- > 0 && this.moveQueue.size() > 10) {
			if (this.moveQueue.size() > 0) {
				this.setPos(this.moveQueue.remove(0));
			}
		}

		this.onGround = true;
	}

	public void bindTexture(TextureManager textures) {
		this.textures = textures;
		if (this.newTexture != null) {
			BufferedImage image = this.newTexture;
			int[] imageData = new int[512];
			image.getRGB(32, 0, 32, 16, imageData, 0, 32);
			int index = 0;

			boolean hair;
			while (true) {
				if (index >= imageData.length) {
					hair = false;
					break;
				}

				if (imageData[index] >>> 24 < 128) {
					hair = true;
					break;
				}

				index++;
			}

			this.hasHair = hair;
			this.newTextureId = textures.bindTexture(this.newTexture);
			this.newTexture = null;
		}

		if (this.newTextureId < 0) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textures.bindTexture("/char.png"));
		} else {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.newTextureId);
		}
	}

	public void renderHover(TextureManager textures, float var2) {
		FontRenderer fontRenderer = this.minecraft.fontRenderer;
		GL11.glPushMatrix();
		GL11.glTranslatef(this.xo + (this.x - this.xo) * var2, this.yo + (this.y - this.yo) * var2 + 0.8F + this.renderOffset, this.zo + (this.z - this.zo) * var2);
		GL11.glRotatef(-this.minecraft.player.yRot, 0.0F, 1.0F, 0.0F);
		var2 = 0.05F;
		GL11.glScalef(0.05F, -var2, var2);
		GL11.glTranslatef((-fontRenderer.getWidth(this.displayName)) / 2.0F, 0.0F, 0.0F);
		GL11.glNormal3f(1.0F, -1.0F, 1.0F);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_COLOR_BUFFER_BIT);
		if (this.name.equalsIgnoreCase("Notch")) {
			fontRenderer.renderNoShadow(this.displayName, 0, 0, 16776960);
		} else {
			fontRenderer.renderNoShadow(this.displayName, 0, 0, 16777215);
		}

		GL11.glDepthFunc(GL11.GL_GREATER);
		GL11.glDepthMask(false);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.8F);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		fontRenderer.renderNoShadow(this.displayName, 0, 0, 16777215);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glTranslatef(1.0F, 1.0F, -0.05F);
		fontRenderer.renderNoShadow(this.name, 0, 0, 5263440);
		GL11.glEnable(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public void queue(byte xChange, byte yChange, byte zChange, float yawChange, float pitchChange) {
		float yaw = yawChange - this.yRot;
		float pitch;
		
		for (pitch = pitchChange - this.xRot; yaw >= 180.0F; yaw -= 360.0F);

		while (yaw < -180.0F) {
			yaw += 360.0F;
		}

		while (pitch >= 180.0F) {
			pitch -= 360.0F;
		}

		while (pitch < -180.0F) {
			pitch += 360.0F;
		}

		yaw = this.yRot + yaw * 0.5F;
		pitch = this.xRot + pitch * 0.5F;
		this.moveQueue.add(new PositionUpdate((this.xp + xChange / 2.0F) / 32.0F, (this.yp + yChange / 2.0F) / 32.0F, (this.zp + zChange / 2.0F) / 32.0F, yaw, pitch));
		this.xp += xChange;
		this.yp += yChange;
		this.zp += zChange;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F, yawChange, pitchChange));
	}

	public void teleport(short x, short y, short z, float yaw, float pitch) {
		float newYaw = yaw - this.yRot;

		float newPitch;
		for (newPitch = pitch - this.xRot; newYaw >= 180.0F; newYaw -= 360.0F);

		while (newYaw < -180.0F) {
			newYaw += 360.0F;
		}

		while (newPitch >= 180.0F) {
			newPitch -= 360.0F;
		}

		while (newPitch < -180.0F) {
			newPitch += 360.0F;
		}

		newYaw = this.yRot + newYaw * 0.5F;
		newPitch = this.xRot + newPitch * 0.5F;
		this.moveQueue.add(new PositionUpdate((this.xp + x) / 64.0F, (this.yp + y) / 64.0F, (this.zp + z) / 64.0F, newYaw, newPitch));
		this.xp = x;
		this.yp = y;
		this.zp = z;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F, yaw, pitch));
	}

	public void queue(byte x, byte y, byte z) {
		this.moveQueue.add(new PositionUpdate((this.xp + x / 2.0F) / 32.0F, (this.yp + y / 2.0F) / 32.0F, (this.zp + z / 2.0F) / 32.0F));
		this.xp += x;
		this.yp += y;
		this.zp += z;
		this.moveQueue.add(new PositionUpdate(this.xp / 32.0F, this.yp / 32.0F, this.zp / 32.0F));
	}

	public void queue(float yaw, float pitch) {
		float newYaw = yaw - this.yRot;

		float newPitch;
		for (newPitch = pitch - this.xRot; newYaw >= 180.0F; newYaw -= 360.0F) {
			;
		}

		while (newYaw < -180.0F) {
			newYaw += 360.0F;
		}

		while (newPitch >= 180.0F) {
			newPitch -= 360.0F;
		}

		while (newPitch < -180.0F) {
			newPitch += 360.0F;
		}

		newYaw = this.yRot + newYaw * 0.5F;
		newPitch = this.xRot + newPitch * 0.5F;
		this.moveQueue.add(new PositionUpdate(newYaw, newPitch));
		this.moveQueue.add(new PositionUpdate(yaw, pitch));
	}

	public void clear() {
		if (this.newTextureId >= 0 && this.textures != null) {
			int texture = this.newTextureId;
			this.textures.textureImgs.remove(Integer.valueOf(texture));
			this.textures.textureBuffer.clear();
			this.textures.textureBuffer.put(texture);
			this.textures.textureBuffer.flip();
			GL11.glDeleteTextures(this.textures.textureBuffer);
		}

	}
}
