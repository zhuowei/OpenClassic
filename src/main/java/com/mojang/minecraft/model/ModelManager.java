package com.mojang.minecraft.model;

import com.mojang.minecraft.model.AnimalModel;
import com.mojang.minecraft.model.CreeperModel;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.model.Model;
import com.mojang.minecraft.model.PigModel;
import com.mojang.minecraft.model.SheepFurModel;
import com.mojang.minecraft.model.SheepModel;
import com.mojang.minecraft.model.SkeletonModel;
import com.mojang.minecraft.model.SpiderModel;
import com.mojang.minecraft.model.ZombieModel;

public final class ModelManager {

	private HumanoidModel humanoid = new HumanoidModel(0.0F);
	private HumanoidModel humanoidWithArmor = new HumanoidModel(1.0F);
	private CreeperModel creeper = new CreeperModel();
	private SkeletonModel skeleton = new SkeletonModel();
	private ZombieModel zombie = new ZombieModel();
	private AnimalModel pig = new PigModel();
	private AnimalModel sheep = new SheepModel();
	private SpiderModel spider = new SpiderModel();
	private SheepFurModel sheepFur = new SheepFurModel();

	public final Model getModel(String name) {
		return (name.equals("humanoid") ? this.humanoid : (name.equals("humanoid.armor") ? this.humanoidWithArmor : (name.equals("creeper") ? this.creeper : (name.equals("skeleton") ? this.skeleton : (name.equals("zombie") ? this.zombie : (name.equals("pig") ? this.pig : (name.equals("sheep") ? this.sheep : (name.equals("spider") ? this.spider : (name.equals("sheep.fur") ? this.sheepFur : null)))))))));
	}
}
