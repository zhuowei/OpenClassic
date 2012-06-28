package com.mojang.minecraft.level.generator;

import ch.spacebase.openclassic.api.Position;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.level.generator.Generator;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.ProgressBarDisplay;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.generator.algorithm.CombinedNoise;
import com.mojang.minecraft.level.generator.algorithm.OctaveNoise;
import com.mojang.util.MathHelper;
import java.util.ArrayList;
import java.util.Random;

public final class LevelGenerator extends Generator {

	private ProgressBarDisplay progress;
	private int width;
	private int depth;
	private int d;
	private Random rand = new Random();
	private byte[] data;
	private int g;
	private int[] h = new int[1048576];
	
	private String name;
	private String author;
	private int height;

	public LevelGenerator() {
		this.progress = GeneralUtils.getMinecraft().progressBar;
	}
	
	public void setInfo(String name, String author, int width, int height, int depth) {
		this.name = name;
		this.author = author;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}
	
	@Override
	public Position findSpawn(ch.spacebase.openclassic.api.level.Level level) {
		((ClientLevel) level).getHandle().findSpawn();
		return level.getSpawn();
	}

	@Override
	public void generate(ch.spacebase.openclassic.api.level.Level level) {
		level.setGenerating(true);
		
		this.progress.setTitle("Generating level");
		this.d = 64;
		this.g = 32;
		this.data = new byte[this.width * this.height * this.depth];
		this.progress.setText("Raising..");
		CombinedNoise alg1 = new CombinedNoise(new OctaveNoise(this.rand, 8), new OctaveNoise(this.rand, 8));
		CombinedNoise alg2 = new CombinedNoise(new OctaveNoise(this.rand, 8), new OctaveNoise(this.rand, 8));
		OctaveNoise var8 = new OctaveNoise(this.rand, 6);
		int[] var9 = new int[this.width * this.depth];
		float var10 = 1.3F;

		int x;
		int z;
		for (x = 0; x < this.width; x++) {
			this.setProgress(x * 100 / (this.width - 1));

			for (z = 0; z < this.depth; z++) {
				double var13 = alg1.compute((x * var10), (z * var10)) / 6.0D + -4;
				double var15 = alg2.compute((x * var10), (z * var10)) / 5.0D + 10.0D + -4;
				if (var8.compute(x, z) / 8.0D > 0.0D) {
					var15 = var13;
				}

				double var19;
				if ((var19 = Math.max(var13, var15) / 2.0D) < 0.0D) {
					var19 *= 0.8D;
				}

				var9[x + z * this.width] = (int) var19;
			}
		}

		this.progress.setText("Eroding..");
		int[] var42 = var9;
		alg2 = new CombinedNoise(new OctaveNoise(this.rand, 8), new OctaveNoise(this.rand, 8));
		CombinedNoise var49 = new CombinedNoise(new OctaveNoise(this.rand, 8), new OctaveNoise(this.rand, 8));

		int var23;
		int var51;
		int var54;
		for (var51 = 0; var51 < this.width; ++var51) {
			this.setProgress(var51 * 100 / (this.width - 1));

			for (var54 = 0; var54 < this.depth; ++var54) {
				double var21 = alg2.compute((var51 << 1), (var54 << 1)) / 8.0D;
				z = var49.compute((var51 << 1), (var54 << 1)) > 0.0D ? 1 : 0;
				if (var21 > 2.0D) {
					var23 = ((var42[var51 + var54 * this.width] - z) / 2 << 1) + z;
					var42[var51 + var54 * this.width] = var23;
				}
			}
		}

		this.progress.setText("Soiling..");
		var42 = var9;
		int var46 = this.width;
		int var48 = this.depth;
		var51 = this.d;
		OctaveNoise var53 = new OctaveNoise(this.rand, 8);

		int var25;
		int var24;
		int var27;
		int var26;
		int var28;
		for (var24 = 0; var24 < var46; ++var24) {
			this.setProgress(var24 * 100 / (this.width - 1));

			for (x = 0; x < var48; ++x) {
				z = (int) (var53.compute(var24, x) / 24.0D) - 4;
				var25 = (var23 = var42[var24 + x * var46] + this.g) + z;
				var42[var24 + x * var46] = Math.max(var23, var25);
				if (var42[var24 + x * var46] > var51 - 2) {
					var42[var24 + x * var46] = var51 - 2;
				}

				if (var42[var24 + x * var46] < 1) {
					var42[var24 + x * var46] = 1;
				}

				for (var26 = 0; var26 < var51; ++var26) {
					var27 = (var26 * this.depth + x) * this.width + var24;
					var28 = 0;
					if (var26 <= var23) {
						var28 = VanillaBlock.DIRT.getId();
					}

					if (var26 <= var25) {
						var28 = VanillaBlock.STONE.getId();
					}

					if (var26 == 0) {
						var28 = VanillaBlock.LAVA.getId();
					}

					this.data[var27] = (byte) var28;
				}
			}
		}

		this.progress.setText("Carving..");
		var48 = this.width;
		var51 = this.depth;
		var54 = this.d;
		var24 = var48 * var51 * var54 / 256 / 64 << 1;

		for (x = 0; x < var24; ++x) {
			this.setProgress(x * 100 / (var24 - 1) / 4);
			float var55 = this.rand.nextFloat() * var48;
			float var59 = this.rand.nextFloat() * var54;
			float var56 = this.rand.nextFloat() * var51;
			var26 = (int) ((this.rand.nextFloat() + this.rand.nextFloat()) * 200.0F);
			float var61 = this.rand.nextFloat() * 3.1415927F * 2.0F;
			float var64 = 0.0F;
			float var29 = this.rand.nextFloat() * 3.1415927F * 2.0F;
			float var30 = 0.0F;
			float var31 = this.rand.nextFloat() * this.rand.nextFloat();

			for (int var32 = 0; var32 < var26; ++var32) {
				var55 += MathHelper.sin(var61) * MathHelper.cos(var29);
				var56 += MathHelper.cos(var61) * MathHelper.cos(var29);
				var59 += MathHelper.sin(var29);
				var61 += var64 * 0.2F;
				var64 = (var64 *= 0.9F) + (this.rand.nextFloat() - this.rand.nextFloat());
				var29 = (var29 + var30 * 0.5F) * 0.5F;
				var30 = (var30 *= 0.75F) + (this.rand.nextFloat() - this.rand.nextFloat());
				if (this.rand.nextFloat() >= 0.25F) {
					float var43 = var55 + (this.rand.nextFloat() * 4.0F - 2.0F) * 0.2F;
					float var50 = var59 + (this.rand.nextFloat() * 4.0F - 2.0F) * 0.2F;
					float var33 = var56 + (this.rand.nextFloat() * 4.0F - 2.0F) * 0.2F;
					float var34 = (this.d - var50) / this.d;
					var34 = 1.2F + (var34 * 3.5F + 1.0F) * var31;
					var34 = MathHelper.sin(var32 * 3.1415927F / var26) * var34;

					for (int var35 = (int) (var43 - var34); var35 <= (int) (var43 + var34); ++var35) {
						for (int var36 = (int) (var50 - var34); var36 <= (int) (var50 + var34); ++var36) {
							for (int var37 = (int) (var33 - var34); var37 <= (int) (var33 + var34); ++var37) {
								float var38 = var35 - var43;
								float var39 = var36 - var50;
								float var40 = var37 - var33;
								if (var38 * var38 + var39 * var39 * 2.0F + var40 * var40 < var34 * var34 && var35 >= 1 && var36 >= 1 && var37 >= 1 && var35 < this.width - 1 && var36 < this.d - 1 && var37 < this.depth - 1) {
									int var66 = (var36 * this.depth + var37) * this.width + var35;
									if (this.data[var66] == VanillaBlock.STONE.getId()) {
										this.data[var66] = 0;
									}
								}
							}
						}
					}
				}
			}
		}

		this.a(VanillaBlock.COAL_ORE.getId(), 90, 1, 4);
		this.a(VanillaBlock.IRON_ORE.getId(), 70, 2, 4);
		this.a(VanillaBlock.GOLD_ORE.getId(), 50, 3, 4);
		this.progress.setText("Watering..");
		var51 = VanillaBlock.STATIONARY_WATER.getId();
		this.setProgress(0);

		for (var54 = 0; var54 < this.width; ++var54) {
			this.a(var54, this.d / 2 - 1, 0, 0, var51);
			this.a(var54, this.d / 2 - 1, this.depth - 1, 0, var51);
		}

		for (var54 = 0; var54 < this.depth; ++var54) {
			this.a(0, this.d / 2 - 1, var54, 0, var51);
			this.a(this.width - 1, this.d / 2 - 1, var54, 0, var51);
		}

		var54 = this.width * this.depth / 8000;

		for (var24 = 0; var24 < var54; ++var24) {
			if (var24 % 100 == 0) {
				this.setProgress(var24 * 100 / (var54 - 1));
			}

			x = this.rand.nextInt(this.width);
			z = this.g - 1 - this.rand.nextInt(2);
			var23 = this.rand.nextInt(this.depth);
			if (this.data[(z * this.depth + var23) * this.width + x] == 0) {
				this.a(x, z, var23, 0, var51);
			}
		}

		this.setProgress(100);
		this.progress.setText("Melting..");
		var46 = this.width * this.depth * this.d / 20000;

		for (var48 = 0; var48 < var46; ++var48) {
			if (var48 % 100 == 0) {
				this.setProgress(var48 * 100 / (var46 - 1));
			}

			var51 = this.rand.nextInt(this.width);
			var54 = (int) (this.rand.nextFloat() * this.rand.nextFloat() * (this.g - 3));
			var24 = this.rand.nextInt(this.depth);
			if (this.data[(var54 * this.depth + var24) * this.width + var51] == 0) {
				this.a(var51, var54, var24, 0, VanillaBlock.STATIONARY_LAVA.getId());
			}
		}

		this.setProgress(100);
		this.progress.setText("Growing..");
		var42 = var9;
		var46 = this.width;
		var48 = this.depth;
		var51 = this.d;
		var53 = new OctaveNoise(this.rand, 8);
		OctaveNoise var58 = new OctaveNoise(this.rand, 8);

		int var63;
		for (x = 0; x < var46; ++x) {
			this.setProgress(x * 100 / (this.width - 1));

			for (z = 0; z < var48; ++z) {
				boolean var60 = var53.compute(x, z) > 8.0D;
				boolean var57 = var58.compute(x, z) > 12.0D;
				var27 = ((var26 = var42[x + z * var46]) * this.depth + z) * this.width + x;
				if (((var28 = this.data[((var26 + 1) * this.depth + z) * this.width + x] & 255) == VanillaBlock.WATER.getId() || var28 == VanillaBlock.STATIONARY_WATER.getId()) && var26 <= var51 / 2 - 1 && var57) {
					this.data[var27] = VanillaBlock.GRAVEL.getId();
				}

				if (var28 == 0) {
					var63 = VanillaBlock.GRASS.getId();
					if (var26 <= var51 / 2 - 1 && var60) {
						var63 = VanillaBlock.SAND.getId();
					}

					this.data[var27] = (byte) var63;
				}
			}
		}

		this.progress.setText("Planting..");
		var42 = var9;
		var46 = this.width;
		var48 = this.width * this.depth / 3000;

		for (var51 = 0; var51 < var48; ++var51) {
			var54 = this.rand.nextInt(2);
			this.setProgress(var51 * 50 / (var48 - 1));
			var24 = this.rand.nextInt(this.width);
			x = this.rand.nextInt(this.depth);

			for (z = 0; z < 10; ++z) {
				var23 = var24;
				var25 = x;

				for (var26 = 0; var26 < 5; ++var26) {
					var23 += this.rand.nextInt(6) - this.rand.nextInt(6);
					var25 += this.rand.nextInt(6) - this.rand.nextInt(6);
					if ((var54 < 2 || this.rand.nextInt(4) == 0) && var23 >= 0 && var25 >= 0 && var23 < this.width && var25 < this.depth) {
						var27 = var42[var23 + var25 * var46] + 1;
						if ((this.data[(var27 * this.depth + var25) * this.width + var23] & 255) == 0) {
							var63 = (var27 * this.depth + var25) * this.width + var23;
							if ((this.data[((var27 - 1) * this.depth + var25) * this.width + var23] & 255) == VanillaBlock.GRASS.getId()) {
								if (var54 == 0) {
									this.data[var63] = VanillaBlock.DANDELION.getId();
								} else if (var54 == 1) {
									this.data[var63] = VanillaBlock.ROSE.getId();
								}
							}
						}
					}
				}
			}
		}

		var42 = var9;
		var46 = this.width;
		var51 = this.width * this.depth * this.d / 2000;

		for (var54 = 0; var54 < var51; ++var54) {
			var24 = this.rand.nextInt(2);
			this.setProgress(var54 * 50 / (var51 - 1) + 50);
			x = this.rand.nextInt(this.width);
			z = this.rand.nextInt(this.d);
			var23 = this.rand.nextInt(this.depth);

			for (var25 = 0; var25 < 20; ++var25) {
				var26 = x;
				var27 = z;
				var28 = var23;

				for (var63 = 0; var63 < 5; ++var63) {
					var26 += this.rand.nextInt(6) - this.rand.nextInt(6);
					var27 += this.rand.nextInt(2) - this.rand.nextInt(2);
					var28 += this.rand.nextInt(6) - this.rand.nextInt(6);
					if ((var24 < 2 || this.rand.nextInt(4) == 0) && var26 >= 0 && var28 >= 0 && var27 >= 1 && var26 < this.width && var28 < this.depth && var27 < var42[var26 + var28 * var46] - 1 && (this.data[(var27 * this.depth + var28) * this.width + var26] & 255) == 0) {
						int var62 = (var27 * this.depth + var28) * this.width + var26;
						if ((this.data[((var27 - 1) * this.depth + var28) * this.width + var26] & 255) == VanillaBlock.STONE.getId()) {
							if (var24 == 0) {
								this.data[var62] = VanillaBlock.BROWN_MUSHROOM.getId();
							} else if (var24 == 1) {
								this.data[var62] = VanillaBlock.RED_MUSHROOM.getId();
							}
						}
					}
				}
			}
		}

		Level handle = ((ClientLevel) level).getHandle();
		handle.waterLevel = this.g;
		handle.setData(width, height, depth, this.data);
		handle.createTime = System.currentTimeMillis();
		handle.creator = author;
		handle.name = name;
		int[] var52 = var9;
		var48 = this.width;
		var51 = this.width * this.depth / 4000;

		for (var54 = 0; var54 < var51; ++var54) {
			this.setProgress(var54 * 50 / (var51 - 1) + 50);
			var24 = this.rand.nextInt(this.width);
			x = this.rand.nextInt(this.depth);

			for (z = 0; z < 20; ++z) {
				var23 = var24;
				var25 = x;

				for (var26 = 0; var26 < 20; ++var26) {
					var23 += this.rand.nextInt(6) - this.rand.nextInt(6);
					var25 += this.rand.nextInt(6) - this.rand.nextInt(6);
					if (var23 >= 0 && var25 >= 0 && var23 < this.width && var25 < this.depth) {
						var27 = var52[var23 + var25 * var48] + 1;
						if (this.rand.nextInt(4) == 0) {
							handle.maybeGrowTree(var23, var27, var25);
						}
					}
				}
			}
		}
		
		level.setGenerating(false);
	}

	private void a(int var1, int var2, int var3, int var4) {
		byte var25 = (byte) var1;
		var4 = this.width;
		int var5 = this.depth;
		int var6 = this.d;
		int var7 = var4 * var5 * var6 / 256 / 64 * var2 / 100;

		for (int var8 = 0; var8 < var7; ++var8) {
			this.setProgress(var8 * 100 / (var7 - 1) / 4 + var3 * 100 / 4);
			float var9 = this.rand.nextFloat() * var4;
			float var10 = this.rand.nextFloat() * var6;
			float var11 = this.rand.nextFloat() * var5;
			int var12 = (int) ((this.rand.nextFloat() + this.rand.nextFloat()) * 75.0F * var2 / 100.0F);
			float var13 = this.rand.nextFloat() * 3.1415927F * 2.0F;
			float var14 = 0.0F;
			float var15 = this.rand.nextFloat() * 3.1415927F * 2.0F;
			float var16 = 0.0F;

			for (int var17 = 0; var17 < var12; ++var17) {
				var9 += MathHelper.sin(var13) * MathHelper.cos(var15);
				var11 += MathHelper.cos(var13) * MathHelper.cos(var15);
				var10 += MathHelper.sin(var15);
				var13 += var14 * 0.2F;
				var14 = (var14 *= 0.9F) + (this.rand.nextFloat() - this.rand.nextFloat());
				var15 = (var15 + var16 * 0.5F) * 0.5F;
				var16 = (var16 *= 0.9F) + (this.rand.nextFloat() - this.rand.nextFloat());
				float var18 = MathHelper.sin(var17 * 3.1415927F / var12) * var2 / 100.0F + 1.0F;

				for (int var19 = (int) (var9 - var18); var19 <= (int) (var9 + var18); ++var19) {
					for (int var20 = (int) (var10 - var18); var20 <= (int) (var10 + var18); ++var20) {
						for (int var21 = (int) (var11 - var18); var21 <= (int) (var11 + var18); ++var21) {
							float var22 = var19 - var9;
							float var23 = var20 - var10;
							float var24 = var21 - var11;
							if (var22 * var22 + var23 * var23 * 2.0F + var24 * var24 < var18 * var18 && var19 >= 1 && var20 >= 1 && var21 >= 1 && var19 < this.width - 1 && var20 < this.d - 1 && var21 < this.depth - 1) {
								int var26 = (var20 * this.depth + var21) * this.width + var19;
								if (this.data[var26] == VanillaBlock.STONE.getId()) {
									this.data[var26] = var25;
								}
							}
						}
					}
				}
			}
		}

	}

	private void setProgress(int progress) {
		this.progress.setProgress(progress);
	}

	private long a(int var1, int var2, int var3, int var4, int var5) {
		byte var20 = (byte) var5;
		ArrayList<int[]> var21 = new ArrayList<int[]>();
		byte var6 = 0;
		int var7 = 1;

		int var8;
		for (var8 = 1; 1 << var7 < this.width; ++var7) {
			;
		}

		while (1 << var8 < this.depth) {
			++var8;
		}

		int var9 = this.depth - 1;
		int var10 = this.width - 1;
		int var22 = var6 + 1;
		this.h[0] = ((var2 << var8) + var3 << var7) + var1;
		long var11 = 0L;
		var1 = this.width * this.depth;

		while (var22 > 0) {
			--var22;
			var2 = this.h[var22];
			if (var22 == 0 && var21.size() > 0) {
				this.h = var21.remove(var21.size() - 1);
				var22 = this.h.length;
			}

			var3 = var2 >> var7 & var9;
			int var13 = var2 >> var7 + var8;

			int var14;
			int var15;
			for (var15 = var14 = var2 & var10; var14 > 0 && this.data[var2 - 1] == 0; --var2) {
				--var14;
			}

			while (var15 < this.width && this.data[var2 + var15 - var14] == 0) {
				++var15;
			}

			int var16 = var2 >> var7 & var9;
			int var17 = var2 >> var7 + var8;
			if (var16 != var3 || var17 != var13) {
				System.out.println("Diagonal flood!?");
			}

			boolean var23 = false;
			boolean var24 = false;
			boolean var18 = false;
			var11 += (var15 - var14);

			for (; var14 < var15; ++var14) {
				this.data[var2] = var20;
				boolean var19;
				if (var3 > 0) {
					if ((var19 = this.data[var2 - this.width] == 0) && !var23) {
						if (var22 == this.h.length) {
							var21.add(this.h);
							this.h = new int[1048576];
							var22 = 0;
						}

						this.h[var22++] = var2 - this.width;
					}

					var23 = var19;
				}

				if (var3 < this.depth - 1) {
					if ((var19 = this.data[var2 + this.width] == 0) && !var24) {
						if (var22 == this.h.length) {
							var21.add(this.h);
							this.h = new int[1048576];
							var22 = 0;
						}

						this.h[var22++] = var2 + this.width;
					}

					var24 = var19;
				}

				if (var13 > 0) {
					byte var25 = this.data[var2 - var1];
					if ((var20 == VanillaBlock.LAVA.getId() || var20 == VanillaBlock.STATIONARY_LAVA.getId()) && (var25 == VanillaBlock.WATER.getId() || var25 == VanillaBlock.STATIONARY_WATER.getId())) {
						this.data[var2 - var1] = VanillaBlock.STONE.getId();
					}

					if ((var19 = var25 == 0) && !var18) {
						if (var22 == this.h.length) {
							var21.add(this.h);
							this.h = new int[1048576];
							var22 = 0;
						}

						this.h[var22++] = var2 - var1;
					}

					var18 = var19;
				}

				++var2;
			}
		}

		return var11;
	}
}
