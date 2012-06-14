package ch.spacebase.openclassic.client.util;

import com.mojang.minecraft.Minecraft;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.client.ClassicClient;

public class GeneralUtils {

	public static Minecraft getMinecraft() {
		return ((ClassicClient) OpenClassic.getClient()).getMinecraft();
	}
	
}
