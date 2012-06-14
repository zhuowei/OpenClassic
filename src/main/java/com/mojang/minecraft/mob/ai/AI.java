package com.mojang.minecraft.mob.ai;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.mob.Mob;
import java.io.Serializable;

public abstract class AI implements Serializable {

	public static final long serialVersionUID = 0L;
	public int defaultLookAngle = 0;

	public abstract void tick(Level level, Mob mob);

	public abstract void beforeRemove();

	public abstract void hurt(Entity cause, int damage);
}
