package com.creativemd.generationmanager;

import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldGenerator {
	
	public String name;
	
	public IWorldGenerator generator;
	
	/**0: disabled, 1: regenerate, 2: ungenerate**/
	public int state;
	
	public String modid;
	
	public WorldGenerator(String name, IWorldGenerator generator, String modid)
	{
		this.name = name;
		this.generator = generator;
		this.modid = modid;
	}
	
	public boolean canUngenerate()
	{
		return !modid.equals("minecraft");
	}
	
	public boolean isUngenerating()
	{
		return canUngenerate() && state == 2;
	}

	public boolean isActive() {
		return state == 1;
	}
	
}
