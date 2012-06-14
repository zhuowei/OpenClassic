package com.mojang.minecraft.level;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashSet;
import java.util.Set;

public final class LevelObjectStream extends ObjectInputStream {

	private Set<String> classes = new HashSet<String>();

	public LevelObjectStream(InputStream in) throws IOException {
		super(in);
		this.classes.add("com.mojang.minecraft.player.Player$1");
		this.classes.add("com.mojang.minecraft.player.Player$PlayerAI");
		this.classes.add("com.mojang.minecraft.mob.Creeper$1");
		this.classes.add("com.mojang.minecraft.mob.Creeper$CreeperAI");
		this.classes.add("com.mojang.minecraft.mob.Skeleton$1");
		this.classes.add("com.mojang.minecraft.mob.Skeleton$SkeletonAI");
	}

	protected final ObjectStreamClass readClassDescriptor() {
		try {
			ObjectStreamClass streamClass = super.readClassDescriptor();
			return this.classes.contains(streamClass.getName()) ? ObjectStreamClass.lookup(Class.forName(streamClass.getName())) : streamClass;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}