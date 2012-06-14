package ch.spacebase.openclassic.client.util;

import java.util.Random;

import com.mojang.minecraft.MovingObjectPosition;
import com.mojang.minecraft.item.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.model.ModelPoint;
import com.mojang.minecraft.phys.AABB;

import ch.spacebase.openclassic.api.block.BlockType;
import ch.spacebase.openclassic.api.block.Blocks;
import ch.spacebase.openclassic.api.block.VanillaBlock;
import ch.spacebase.openclassic.api.block.custom.CustomBlock;
import ch.spacebase.openclassic.api.block.model.Model;

public class BlockUtils {

	private static Random rand = new Random();
	
	public static final MovingObjectPosition clipSelection(int id, int x, int y, int z, ModelPoint point, ModelPoint other) {
		Model model = Blocks.fromId(id).getModel();
		
		point = point.add((-x), (-y), (-z));
		other = other.add((-x), (-y), (-z));
		ModelPoint x1 = point.getXIntersection(other, model.getSelectionBox().getX1());
		ModelPoint x2 = point.getXIntersection(other, model.getSelectionBox().getX2());
		ModelPoint y1 = point.getYIntersection(other, model.getSelectionBox().getY1());
		ModelPoint y2 = point.getYIntersection(other, model.getSelectionBox().getY2());
		ModelPoint z1 = point.getZIntersection(other, model.getSelectionBox().getZ1());
		ModelPoint z2 = point.getZIntersection(other, model.getSelectionBox().getZ2());
		if (!xIntersectsSelection(id, x1)) {
			x1 = null;
		}

		if (!xIntersectsSelection(id, x2)) {
			x2 = null;
		}

		if (!yIntersectsSelection(id, y1)) {
			y1 = null;
		}

		if (!yIntersectsSelection(id, y2)) {
			y2 = null;
		}

		if (!zIntersectsSelection(id, z1)) {
			z1 = null;
		}

		if (!zIntersectsSelection(id, z2)) {
			z2 = null;
		}

		ModelPoint result = null;
		if (x1 != null) {
			result = x1;
		}

		if (x2 != null && (result == null || point.distance(x2) < point.distance(result))) {
			result = x2;
		}

		if (y1 != null && (result == null || point.distance(y1) < point.distance(result))) {
			result = y1;
		}

		if (y2 != null && (result == null || point.distance(y2) < point.distance(result))) {
			result = y2;
		}

		if (z1 != null && (result == null || point.distance(z1) < point.distance(result))) {
			result = z1;
		}

		if (z2 != null && (result == null || point.distance(z2) < point.distance(result))) {
			result = z2;
		}

		if (result == null) {
			return null;
		} else {
			byte side = -1;
			if (result == x1) {
				side = 4;
			}

			if (result == x2) {
				side = 5;
			}

			if (result == y1) {
				side = 0;
			}

			if (result == y2) {
				side = 1;
			}

			if (result == z1) {
				side = 2;
			}

			if (result == z2) {
				side = 3;
			}

			return new MovingObjectPosition(x, y, z, side, result.add(x, y, z));
		}
	}
	
	public static final MovingObjectPosition clip(int id, int x, int y, int z, ModelPoint point, ModelPoint other) {
		Model model = Blocks.fromId(id).getModel();
		
		point = point.add((-x), (-y), (-z));
		other = other.add((-x), (-y), (-z));
		ModelPoint x1 = point.getXIntersection(other, model.getCollisionBox().getX1());
		ModelPoint x2 = point.getXIntersection(other, model.getCollisionBox().getX2());
		ModelPoint y1 = point.getYIntersection(other, model.getCollisionBox().getY1());
		ModelPoint y2 = point.getYIntersection(other, model.getCollisionBox().getY2());
		ModelPoint z1 = point.getZIntersection(other, model.getCollisionBox().getZ1());
		ModelPoint z2 = point.getZIntersection(other, model.getCollisionBox().getZ2());
		if (!xIntersects(id, x1)) {
			x1 = null;
		}

		if (!xIntersects(id, x2)) {
			x2 = null;
		}

		if (!yIntersects(id, y1)) {
			y1 = null;
		}

		if (!yIntersects(id, y2)) {
			y2 = null;
		}

		if (!zIntersects(id, z1)) {
			z1 = null;
		}

		if (!zIntersects(id, z2)) {
			z2 = null;
		}

		ModelPoint result = null;
		if (x1 != null) {
			result = x1;
		}

		if (x2 != null && (result == null || point.distance(x2) < point.distance(result))) {
			result = x2;
		}

		if (y1 != null && (result == null || point.distance(y1) < point.distance(result))) {
			result = y1;
		}

		if (y2 != null && (result == null || point.distance(y2) < point.distance(result))) {
			result = y2;
		}

		if (z1 != null && (result == null || point.distance(z1) < point.distance(result))) {
			result = z1;
		}

		if (z2 != null && (result == null || point.distance(z2) < point.distance(result))) {
			result = z2;
		}

		if (result == null) {
			return null;
		} else {
			byte side = -1;
			if (result == x1) {
				side = 4;
			}

			if (result == x2) {
				side = 5;
			}

			if (result == y1) {
				side = 0;
			}

			if (result == y2) {
				side = 1;
			}

			if (result == z1) {
				side = 2;
			}

			if (result == z2) {
				side = 3;
			}

			return new MovingObjectPosition(x, y, z, side, result.add(x, y, z));
		}
	}

	private static boolean xIntersectsSelection(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.y >= model.getSelectionBox().getY1() && point.y <= model.getSelectionBox().getY2() && point.z >= model.getSelectionBox().getZ1() && point.z <= model.getSelectionBox().getZ2();
	}

	private static boolean yIntersectsSelection(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getSelectionBox().getX1() && point.x <= model.getSelectionBox().getX2() && point.z >= model.getSelectionBox().getZ1() && point.z <= model.getSelectionBox().getZ2();
	}

	private static boolean zIntersectsSelection(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getSelectionBox().getX1() && point.x <= model.getSelectionBox().getX2() && point.y >= model.getSelectionBox().getY1() && point.y <= model.getSelectionBox().getY2();
	}
	
	private static boolean xIntersects(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.y >= model.getCollisionBox().getY1() && point.y <= model.getCollisionBox().getY2() && point.z >= model.getCollisionBox().getZ1() && point.z <= model.getCollisionBox().getZ2();
	}

	private static boolean yIntersects(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getCollisionBox().getX1() && point.x <= model.getCollisionBox().getX2() && point.z >= model.getCollisionBox().getZ1() && point.z <= model.getCollisionBox().getZ2();
	}

	private static boolean zIntersects(int id, ModelPoint point) {
		Model model = Blocks.fromId(id).getModel();
		return point != null && point.x >= model.getCollisionBox().getX1() && point.x <= model.getCollisionBox().getX2() && point.y >= model.getCollisionBox().getY1() && point.y <= model.getCollisionBox().getY2();
	}
	
	public static AABB getSelectionBox(int id, int x, int y, int z) {
		Model model = Blocks.fromId(id).getModel();
		if(model.getSelectionBox() == null) return null;
		return new AABB(x + model.getSelectionBox().getX1(), y + model.getSelectionBox().getY1(), z + model.getSelectionBox().getZ1(), x + model.getSelectionBox().getX2(), y + model.getSelectionBox().getY2(), z + model.getSelectionBox().getZ2());
	}

	public static AABB getCollisionBox(int id, int x, int y, int z) {
		Model model = Blocks.fromId(id).getModel();
		if(model.getCollisionBox() == null) return null;
		return new AABB(x + model.getCollisionBox().getX1(), y + model.getCollisionBox().getY1(), z + model.getCollisionBox().getZ1(), x + model.getCollisionBox().getX2(), y + model.getCollisionBox().getY2(), z + model.getCollisionBox().getZ2());
	}
	
	public static boolean canExplode(BlockType type) {
		if(type instanceof CustomBlock) type = ((CustomBlock) type).getFallback();
		return type != VanillaBlock.STONE && type != VanillaBlock.COBBLESTONE && type != VanillaBlock.BEDROCK && type != VanillaBlock.COAL_ORE && type != VanillaBlock.IRON_ORE && type != VanillaBlock.GOLD_ORE && type != VanillaBlock.GOLD_BLOCK && type != VanillaBlock.IRON_BLOCK && type != VanillaBlock.SLAB && type != VanillaBlock.DOUBLE_SLAB && type != VanillaBlock.BRICK_BLOCK && type != VanillaBlock.MOSSY_COBBLESTONE && type != VanillaBlock.OBSIDIAN;
	}
	
	public static int getHardness(BlockType type) {
		if(type instanceof CustomBlock) type = ((CustomBlock) type).getFallback();
		if(type instanceof VanillaBlock) {
			switch((VanillaBlock) type) {
				case SAPLING:
				case DANDELION:
				case ROSE:
				case BROWN_MUSHROOM:
				case RED_MUSHROOM:
				case TNT:
					return 0;
				case RED_CLOTH:
				case ORANGE_CLOTH:
				case YELLOW_CLOTH:
				case LIME_CLOTH:
				case GREEN_CLOTH:
				case AQUA_GREEN_CLOTH:
				case CYAN_CLOTH:
				case BLUE_CLOTH:
				case PURPLE_CLOTH:
				case INDIGO_CLOTH:
				case VIOLET_CLOTH:
				case MAGENTA_CLOTH:
				case PINK_CLOTH:
				case BLACK_CLOTH:
				case GRAY_CLOTH:
				case WHITE_CLOTH:
					return 16;
				case GRASS:
				case DIRT:
				case GRAVEL:
				case SPONGE:
				case SAND:
					return 12;
				case STONE:
				case MOSSY_COBBLESTONE:
					return 20;
				case COBBLESTONE:
				case WOOD:
				case BOOKSHELF:
					return 30;
				case BEDROCK:
					return 19980;
				case WATER:
				case STATIONARY_WATER:
				case LAVA:
				case STATIONARY_LAVA:
					return 2000;
				case GOLD_ORE:
				case IRON_ORE:
				case COAL_ORE:
				case GOLD_BLOCK:
					return 60;
				case LOG:
					return 50;
				case LEAVES:
					return 4;
				case GLASS:
					return 6;
				case IRON_BLOCK:
					return 100;
				case OBSIDIAN:
					return 200;
			}
		}
		
		return 0;
	}
	
	public static int getDrop(BlockType type) {
		if(type instanceof VanillaBlock) {
			switch((VanillaBlock) type) {
				case STONE:
				case OBSIDIAN: return VanillaBlock.COBBLESTONE.getId();
				case GRASS: return VanillaBlock.DIRT.getId();
				case GOLD_ORE: return VanillaBlock.GOLD_BLOCK.getId();
				case IRON_ORE: return VanillaBlock.IRON_BLOCK.getId();
				case COAL_ORE:
				case DOUBLE_SLAB: return VanillaBlock.SLAB.getId();
				case LOG: return VanillaBlock.WOOD.getId();
				case LEAVES: return VanillaBlock.SAPLING.getId();
			}
		}
		
		return type.getId();
	}
	
	public static int getDropCount(BlockType type) {
		if(type instanceof VanillaBlock) {
			switch((VanillaBlock) type) {
			case WATER:
			case STATIONARY_WATER:
			case LAVA:
			case STATIONARY_LAVA:
			case TNT: 
			case BOOKSHELF: return 0;
			case DOUBLE_SLAB: return 2;
			case GOLD_ORE:
			case COAL_ORE:
			case IRON_ORE: return rand.nextInt(3) + 1;
			case LOG: return rand.nextInt(3) + 3;
			case LEAVES: return rand.nextInt(10) == 0 ? 1 : 0;
			}
		}
		
		return 1;
	}
	
	public static void dropItems(int block, Level level, int x, int y, int z) {
		dropItems(block, level, x, y, z, 1);
	}

	public static void dropItems(int block, Level level, int x, int y, int z, float chance) {
		if (!level.creativeMode) {
			int dropCount = getDropCount(Blocks.fromId(block));

			for (int count = 0; count < dropCount; ++count) {
				if (rand.nextFloat() <= chance) {
					float xOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float yOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					float zOffset = rand.nextFloat() * 0.7F + (1.0F - 0.7F) * 0.5F;
					level.addEntity(new Item(level, x + xOffset, y + yOffset, z + zOffset, getDrop(Blocks.fromId(block))));
				}
			}

		}
	}
	
}
