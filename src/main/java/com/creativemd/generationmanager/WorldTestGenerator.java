package com.creativemd.generationmanager;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;

public class WorldTestGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider){
		for(int zahl = 100; zahl < 120; zahl++)
			world.setBlockState(new BlockPos(chunkX*16, zahl, chunkZ*16), Blocks.diamond_block.getDefaultState(), 0);
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
