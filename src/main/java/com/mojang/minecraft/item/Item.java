package com.mojang.minecraft.item;

import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.model.Quad;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.item.ItemModel;
import com.mojang.minecraft.item.TakeEntityAnim;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.TextureManager;
import com.mojang.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class Item extends Entity {

	public static final long serialVersionUID = 0L;
	private static ItemModel[] models = new ItemModel[256];
	private float xd;
	private float yd;
	private float zd;
	private float rot;
	private int resource;
	private int tickCount;
	private int age = 0;

	public static void initModels() {
		for (int id = 0; id < 256; ++id) {
			if (Blocks.fromId(id) != null) {
				Quad quad = Blocks.fromId(id).getModel().getQuads().size() >= 3 ? Blocks.fromId(id).getModel().getQuad(2) : Blocks.fromId(id).getModel().getQuad(Blocks.fromId(id).getModel().getQuads().size() - 1);
				models[id] = new ItemModel(quad.getTexture().getId());
			}
		}

	}

	public Item(Level var1, float var2, float var3, float var4, int var5) {
		super(var1);
		this.setSize(0.25F, 0.25F);
		this.heightOffset = this.bbHeight / 2.0F;
		this.setPos(var2, var3, var4);
		this.resource = var5;
		this.rot = (float) (Math.random() * 360.0D);
		this.xd = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
		this.yd = 0.2F;
		this.zd = (float) (Math.random() * 0.20000000298023224D - 0.10000000149011612D);
		this.makeStepSound = false;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd -= 0.04F;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if (this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
			this.yd *= -0.5F;
		}

		++this.tickCount;
		++this.age;
		if (this.age >= 6000) {
			this.remove();
		}

	}

	public void render(TextureManager var1, float var2) {
		this.textureId = var1.bindTexture("/terrain.png");
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
		float var5 = this.level.getBrightness((int) this.x, (int) this.y, (int) this.z);
		float var3 = this.rot + (this.tickCount + var2) * 3.0F;
		GL11.glPushMatrix();
		GL11.glColor4f(var5, var5, var5, 1.0F);
		float var4 = (var5 = MathHelper.a(var3 / 10.0F)) * 0.1F + 0.1F;
		GL11.glTranslatef(this.xo + (this.x - this.xo) * var2, this.yo + (this.y - this.yo) * var2 + var4, this.zo + (this.z - this.zo) * var2);
		GL11.glRotatef(var3, 0.0F, 1.0F, 0.0F);
		
		if(models[this.resource] == null && Blocks.fromId(this.resource) != null) {
			Quad quad = Blocks.fromId(this.resource).getModel().getQuads().size() >= 3 ? Blocks.fromId(this.resource).getModel().getQuad(2) : Blocks.fromId(this.resource).getModel().getQuad(Blocks.fromId(this.resource).getModel().getQuads().size() - 1);
			models[this.resource] = new ItemModel(quad.getTexture().getId());
		}
		
		models[this.resource].a();
		var5 = (var5 = (var5 = var5 * 0.5F + 0.5F) * var5) * var5;
		GL11.glColor4f(1, 1, 1, var5 * 0.4F);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		models[this.resource].a();
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public void playerTouch(Entity var1) {
		Player var2;
		if ((var2 = (Player) var1).addResource(this.resource)) {
			this.level.addEntity(new TakeEntityAnim(this.level, this, var2));
			this.remove();
		}

	}

}
