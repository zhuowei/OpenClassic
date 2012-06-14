package com.mojang.minecraft.item;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class TakeEntityAnim extends Entity {

	private static final long serialVersionUID = 1L;
	private int time = 0;
	private Entity item;
	private Entity player;
	private float xorg;
	private float yorg;
	private float zorg;

	public TakeEntityAnim(Level level, Entity item, Entity to) {
		super(level);
		this.item = item;
		this.player = to;
		this.setSize(1.0F, 1.0F);
		this.xorg = item.x;
		this.yorg = item.y;
		this.zorg = item.z;
	}

	public void tick() {
		this.time++;
		if (this.time >= 3) {
			this.remove();
		}

		float distance = (this.time / 3.0F) * (this.time / 3.0F);
		this.xo = this.item.xo = this.item.x;
		this.yo = this.item.yo = this.item.y;
		this.zo = this.item.zo = this.item.z;
		this.x = this.item.x = this.xorg + (this.player.x - this.xorg) * distance;
		this.y = this.item.y = this.yorg + (this.player.y - 1.0F - this.yorg) * distance;
		this.z = this.item.z = this.zorg + (this.player.z - this.zorg) * distance;
		this.setPos(this.x, this.y, this.z);
	}

	public void render(TextureManager textureManager, float var2) {
		this.item.render(textureManager, var2);
	}
}
