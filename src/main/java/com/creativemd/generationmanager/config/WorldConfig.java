package com.creativemd.generationmanager.config;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.generationmanager.GenerationDummyContainer;
import com.creativemd.ingameconfigmanager.api.core.TabRegistry;
import com.creativemd.ingameconfigmanager.api.tab.ModTab;
import com.creativemd.ingameconfigmanager.api.tab.SubTab;

public class WorldConfig {
	
	public static ModTab WGMTab = new ModTab("World Generation Manager", new ItemStack(Blocks.coal_ore));
	
	public static void startConfig()
	{
		TabRegistry.registerModTab(WGMTab);
		WGMTab.addBranch(new WorldGeneralBranch("general"));
	}
	
}
