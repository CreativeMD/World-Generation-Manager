package com.creativemd.generationmanager.config;

import com.creativemd.ingameconfigmanager.api.core.TabRegistry;
import com.creativemd.ingameconfigmanager.api.tab.ModTab;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class WorldConfig {
	
	public static ModTab WGMTab = new ModTab("World Generation Manager", new ItemStack(Blocks.COAL_ORE));
	
	public static void startConfig()
	{
		TabRegistry.registerModTab(WGMTab);
		WGMTab.addBranch(new WorldGeneralBranch("general"));
	}
	
}
