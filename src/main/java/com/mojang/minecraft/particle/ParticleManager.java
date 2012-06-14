package com.mojang.minecraft.particle;

import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.render.TextureManager;
import java.util.ArrayList;
import java.util.List;

public final class ParticleManager {

	@SuppressWarnings("unchecked")
	public List<Particle>[] particles = new List[2];
	public TextureManager textureManager;

	public ParticleManager(Level level, TextureManager textureManager) {
		if (level != null) {
			level.particleEngine = this;
		}

		this.textureManager = textureManager;

		for (int index = 0; index < particles.length; ++index) {
			this.particles[index] = new ArrayList<Particle>();
		}

	}

	public final void spawnParticle(Particle particle) {
		int texture = particle.getParticleTexture();
		this.particles[texture].add(particle);
	}

	public final void tickParticles() {
		for (int pIndex = 0; pIndex < 2; ++pIndex) {
			for (int index = 0; index < this.particles[pIndex].size(); ++index) {
				Particle particle = this.particles[pIndex].get(index);
				particle.tick();
				
				if (particle.removed) {
					this.particles[pIndex].remove(index--);
				}
			}
		}

	}
}
