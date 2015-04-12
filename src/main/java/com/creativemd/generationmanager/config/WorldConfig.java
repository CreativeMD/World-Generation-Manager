package com.creativemd.generationmanager.config;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.creativemd.craftingmanager.api.core.ConfigRegistry;
import com.creativemd.craftingmanager.api.core.ConfigTab;

public class WorldConfig {
	
	public static ConfigTab worldTab = new ConfigTab("WorldGenerationManager", new ItemStack(Blocks.coal_ore));
	
	public static WorldConfigSystem system = new WorldConfigSystem();
	
	public static void startConfig()
	{
		ConfigRegistry.registerConfig(system);
	}
	
}
