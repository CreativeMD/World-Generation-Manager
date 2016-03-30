package com.creativemd.generationmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.ChunkDataEvent.Save;
import net.minecraftforge.event.world.ChunkEvent.Load;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChunkHandler {
	
	public static int regenerated = 0;
	public static int maxregenerated = 300;
	public static int unregenated = 0;
	
	@SubscribeEvent
	public void onChunkSaveData(Save event)
	{
		String save = "";
        if(modsGenerated(event.getChunk()) == null)setMods(event.getChunk(), new String[0]);
        String[] temp = modsGenerated(event.getChunk());
        for(int zahl = 0; zahl < temp.length; zahl++)
        	save = save + temp[zahl] + ";";
        event.getData().setString("mods", save);
	}
	
	@SubscribeEvent
	public void onChunkLoadData(net.minecraftforge.event.world.ChunkDataEvent.Load event)
	{
		String save = event.getData().getString("mods");
        if(modsGenerated(event.getChunk()) == null)setMods(event.getChunk(), new String[0]);
        if(!save.equals(""))
        	setMods(event.getChunk(), save.split(";"));	
	}
	
	@SubscribeEvent
	public void onChunkLoad(Load event)
	{
		if(!event.getChunk().isTerrainPopulated())return ;
		if(GenerationDummyContainer.active)
		{
			String[] chunkmods = modsGenerated(event.getChunk());	
			if(chunkmods == null)
				chunkmods = new String[0];
			List<String> newmods = new ArrayList<String>();
			
			boolean hasregenerate = false;
			for(int zahl = 0; zahl < GenerationDummyContainer.worldGenerators.size(); zahl++)
			{
				if(!contains(chunkmods, GenerationDummyContainer.worldGenerators.get(zahl).name) && GenerationDummyContainer.worldGenerators.get(zahl).isActive())
				{
					//Regenerate
					long worldSeed = event.getWorld().getSeed();
			        Random fmlRandom = new Random(worldSeed);
			        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
			        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
			        fmlRandom.setSeed((xSeed * event.getChunk().xPosition + zSeed * event.getChunk().zPosition) ^ worldSeed);
			        
		        	if(event.getWorld() instanceof WorldServer)
		        	{
		        		ChunkProviderServer provider1 =  (ChunkProviderServer) event.getWorld().getChunkProvider();
		        		IChunkGenerator generator = provider1.chunkGenerator;
		        		IChunkProvider provider2 = event.getWorld().getChunkProvider();
		        		GenerationDummyContainer.worldGenerators.get(zahl).generator.generate(fmlRandom, event.getChunk().xPosition, event.getChunk().zPosition, event.getWorld(), generator, provider2);
		        	}
			        	
			        newmods.add(GenerationDummyContainer.worldGenerators.get(zahl).name);
			        hasregenerate = true;
				}else if(contains(chunkmods, GenerationDummyContainer.worldGenerators.get(zahl).name) && GenerationDummyContainer.worldGenerators.get(zahl).isUngenerating()){
					String modID = GenerationDummyContainer.worldGenerators.get(zahl).modid;
					for (int x = 0; x < 16; x++) {
						for (int y = 0; y < 256; y++) {
							for (int z = 0; z < 16; z++) {
								BlockPos pos = new BlockPos(x, y, z);
								String name = Block.blockRegistry.getNameForObject(event.getChunk().getBlockState(pos).getBlock()).toString();
								String[] names = name.split(":");
								if(names.length > 1 && names[0].equals(modID))
								{
							
									if(y < 50)
										event.getChunk().setBlockState(pos, Blocks.stone.getDefaultState());
									else if(y <= 64)
										event.getChunk().setBlockState(pos, Blocks.dirt.getDefaultState());
									else
										event.getChunk().setBlockState(pos, Blocks.air.getDefaultState());
								}
							}
						}
					}
					unregenated++;
				}else{
					if(contains(chunkmods, GenerationDummyContainer.worldGenerators.get(zahl).name))
						newmods.add(GenerationDummyContainer.worldGenerators.get(zahl).name);
				}
			}
			String[] mods = new String[newmods.size()];
			for(int zahl = 0; zahl < mods.length; zahl++)
				mods[zahl] = newmods.get(zahl);
			
			setMods(event.getChunk(), mods.clone());
			event.getChunk().setChunkModified();
			
			if(hasregenerate)
				regenerated++;
			if(maxregenerated <= unregenated)
			{
				System.out.println("ungenerated " + unregenated + " chunks!");
				unregenated = 0;
			}
			if(maxregenerated <= regenerated)
			{
				System.out.println("Regenerated " + maxregenerated + " chunks!");
				regenerated = 0;
			}
		}
	}
	
	public static String[] modsGenerated(Chunk chunk)
	{
		try {
			return (String[]) Chunk.class.getField("modsGenerated").get(chunk);
		} catch (Exception e){
			System.out.println("Field modsGenerated was not found");
			return new String[0];
		}
	}
	
	public static void setMods(Chunk chunk, String[] mods)
	{
		try{
			Chunk.class.getField("modsGenerated").set(chunk, mods);
		} catch (Exception e){
			System.out.println("Field modsGenerated was not found");
		}
	}
	
	public static boolean contains(Object[] array, Object jar)
	{
		if(array == null)
			return false;
		for(int zahl = 0; zahl < array.length; zahl++)
			if(array[zahl].equals(jar))return true;
		return false;
	}
	
	
}
