package com.mojang.minecraft.player;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.event.EventFactory;
import ch.spacebase.openclassic.api.event.player.PlayerMoveEvent;
import ch.spacebase.openclassic.client.player.ClientPlayer;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;
import com.mojang.minecraft.mob.ai.BasicAI;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.player.InputHandler;
import com.mojang.minecraft.player.Inventory;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class Player extends Mob {

	public static final long serialVersionUID = 0L;
	public static final int MAX_HEALTH = 20;
	public static final int MAX_ARROWS = 99;
	
	private static int newTextureId = -1;
	public static BufferedImage newTexture;
	
	public transient InputHandler input;
	public Inventory inventory = new Inventory();
	public byte userType = 0;
	public float oBob;
	public float bob;
	public int score = 0;
	public int arrows = 20;
	public transient boolean speedHack = false;
	
	public transient ClientPlayer openclassic = new ClientPlayer(this);
	
	public Player(Level level) {
		super(level);
		
		if (level != null) {
			level.player = this;
			level.removeEntity(this);
			level.addEntity(this);
		}

		this.heightOffset = 1.62F;
		this.health = 20;
		this.modelName = "humanoid";
		this.rotOffs = 180.0F;
		this.ai = new PlayerAI();
	}

	public void resetPos() {
		this.resetPos(null);
	}
	
	public void resetPos(Position pos) {
		this.heightOffset = 1.62F;
		this.setSize(0.6F, 1.8F);
		super.resetPos(pos);
		if (this.level != null) {
			this.level.player = this;
		}

		this.health = MAX_HEALTH;
		this.deathTime = 0;
	}

	public void aiStep() {
		this.inventory.tick();
		this.oBob = this.bob;
		this.input.updateMovement();
		super.aiStep();
		float bob = MathHelper.sqrt(this.xd * this.xd + this.zd * this.zd);
		float tilt = (float) Math.atan((-this.yd * 0.2F)) * 15.0F;
		if (bob > 0.1F) {
			bob = 0.1F;
		}

		if (!this.onGround || this.health <= 0) {
			bob = 0.0F;
		}

		if (this.onGround || this.health <= 0) {
			tilt = 0.0F;
		}

		this.bob += (bob - this.bob) * 0.4F;
		this.tilt += (tilt - this.tilt) * 0.8F;
		
		List<Entity> entities = this.level.findEntities(this, this.bb.grow(1, 0, 1));
		if (this.health > 0 && entities != null) {
			for (Entity entity : entities) {
				entity.playerTouch(this);
			}
		}
	}

	public void render(TextureManager textureManager, float var2) {
	}

	public void releaseAllKeys() {
		this.input.resetKeys();
	}

	public void setKey(int key, boolean pressed) {
		this.input.setKeyState(key, pressed);
	}

	public boolean addResource(int block) {
		return this.inventory.addResource(block);
	}

	public int getScore() {
		return this.score;
	}

	public HumanoidModel getModel() {
		return (HumanoidModel) modelCache.getModel(this.modelName);
	}

	public void die(Entity cause) {
		this.setSize(0.2F, 0.2F);
		this.setPos(this.x, this.y, this.z);
		this.yd = 0.1F;
		
		if (cause != null) {
			this.xd = -MathHelper.b((this.hurtDir + this.yRot) * 3.1415927F / 180.0F) * 0.1F;
			this.zd = -MathHelper.a((this.hurtDir + this.yRot) * 3.1415927F / 180.0F) * 0.1F;
		} else {
			this.xd = this.zd = 0.0F;
		}

		this.heightOffset = 0.1F;
	}

	public void remove() {
	}

	public void awardKillScore(Entity killed, int score) {
		this.score += score;
	}

	public boolean isShootable() {
		return true;
	}

	public void bindTexture(TextureManager textureManager) {
		if (newTexture != null) {
			newTextureId = textureManager.bindTexture(newTexture);
			newTexture = null;
		}

		if (newTextureId < 0) {
			int textureId = textureManager.bindTexture("/char.png");
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		} else {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, newTextureId);
		}
	}

	public void hurt(Entity entity, int damage) {
		if (!this.level.creativeMode) {
			super.hurt(entity, damage);
		}
	}

	public boolean isCreativeModeAllowed() {
		return true;
	}
	
	public void moveRelative(float relX, float relZ, float var3) {
		if(GeneralUtils.getMinecraft().settings.speed && this.speedHack &&  GeneralUtils.getMinecraft().hacks) {
			super.moveRelative(relX, relZ, var3, 2.5F);
		} else {
			super.moveRelative(relX, relZ, var3);
		}
	}
	
	@Override
	public void moveTo(float x, float y, float z, float yaw, float pitch) {
		Position from = new Position(this.level.openclassic, this.x, this.y, this.z, (byte) this.yRot, (byte) this.xRot);
		Position to = new Position(this.level.openclassic, x, y, z, (byte) yaw, (byte) pitch);
		PlayerMoveEvent event = EventFactory.callEvent(new PlayerMoveEvent(this.openclassic, from, to));
		if(event.isCancelled()) {
			return;
		}
		
		super.moveTo((float) event.getTo().getX(), (float) event.getTo().getY(), (float) event.getTo().getZ(), event.getTo().getYaw(), event.getTo().getPitch());
	}
	
	public class PlayerAI extends BasicAI implements Serializable {
		public static final long serialVersionUID = 0L;

		public void update() {
			this.jumping = Player.this.input.jumping;
			Player.this.speedHack = Player.this.input.speed;
			this.xxa = Player.this.input.xxa;
			this.yya = Player.this.input.yya;
		}
	}

}
