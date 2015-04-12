package com.creativemd.generationmanager;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import cpw.mods.fml.common.IWorldGenerator;

public class WorldTestGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world,
			IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
		Block block = Block.getBlockFromName("diamond_block");
		for(int zahl = 100; zahl < 120; zahl++)
			world.setBlock(chunkX*16, zahl, chunkZ*16, block, 0, 2);
	}
	
	public String getName()
	{
		return "Test World Generator";
	}
	
	public String getModID()
	{
		return "generationmanager";
	}
	
}
