package com.creativemd.generationmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class GenerationDummyContainer extends DummyModContainer {

	public GenerationDummyContainer() {

		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "generationmanager";
		meta.name = "Generation Manager";
		meta.version = "0.4.4";
		meta.credits = "CreativeMD";
		meta.authorList = Arrays.asList("CreativeMD");
		meta.description = "";
		meta.url = "http://www.minecraftforum.net/topic/1879772-";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}
	
	public static Configuration configuration;
	
	public static ArrayList<WorldGenerator> worldGenerators = new ArrayList<WorldGenerator>();
	
	public static boolean active = false;
	
	public static WorldGenerator getWorldGenerator(String mod)
	{
		for (int i = 0; i < worldGenerators.size(); i++) {
			if(worldGenerators.get(i).name.equals(mod))
				return worldGenerators.get(i);
		}
		return null;
	}
	
	public static void generateWorld(int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider)
    {
        if(getField("sortedGeneratorList") == null)
        {
            callFunction("computeSortedGeneratorList");
        }
        long worldSeed = world.getSeed();
        Random fmlRandom = new Random(worldSeed);
        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
        long chunkSeed = (xSeed * chunkX + zSeed * chunkZ) ^ worldSeed;
        for (int i = 0; i < worldGenerators.size(); i++) {
        	String[] mods2 = ChunkHandler.modsGenerated(world.getChunkFromChunkCoords(chunkX, chunkZ));
        	if(mods2 == null)mods2 = new String[0];
        	List<String> newmods = new ArrayList<String>();
        	for(int zahl = 0; zahl < mods2.length; zahl++)
        		if(!mods2[zahl].equals(""))
        			newmods.add(mods2[zahl]);
        	newmods.add(worldGenerators.get(i).name);
        	mods2 = new String[newmods.size()];
        	for(int zahl = 0; zahl < mods2.length; zahl++)
        		mods2[zahl] = newmods.get(zahl);
        	ChunkHandler.setMods(world.getChunkFromChunkCoords(chunkX, chunkZ), mods2.clone());
            fmlRandom.setSeed(chunkSeed);
            worldGenerators.get(i).generator.generate(fmlRandom, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
        }
    }
	
	public static Object getField(String name)
	{
		try {
			return GameRegistry.class.getField(name).get(null);
		} catch (Exception e){
			System.out.println("Field " + name + " was not found");
			return null;
		}
	}

	public static Object callFunction(String name, Object...args)
	{
		try {
			Class<?>[] objects = new Class<?>[args.length];
			for(int zahl = 0; zahl < args.length; zahl++)
				objects[zahl] = args[zahl].getClass();
			return GameRegistry.class.getMethod(name, objects).invoke(null, args);
		} catch (Exception e){
			System.out.println("Method " + name + " was not found");
			return null;
		}
	}
	
	public static void setField(String name, Object field)
	{
		try {
			GameRegistry.class.getField(name).set(null, field);
		} catch (Exception e){
			System.out.println("Field " + name + " was not found");
		}
	}
	
	/*public static boolean isEnabled(IWorldGenerator generator)
	{
		if(isEnabled(generator))
		{
			long worldSeed = 0;
	        Random fmlRandom = new Random(worldSeed);
	        long xSeed = fmlRandom.nextLong() >> 2 + 1L;
	        long zSeed = fmlRandom.nextLong() >> 2 + 1L;
	        int chunkX = 0;
			int chunkZ = 0;
			long chunkSeed = (xSeed * chunkX  + zSeed * chunkZ ) ^ worldSeed;
			fmlRandom.setSeed(chunkSeed);
			World world = null;
			IChunkProvider chunkGenerator = null;
			IChunkProvider chunkProvider = null;
			generator.generate(fmlRandom, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
		}
		return false;
	}*/
	
	public static String getName(IWorldGenerator generator)
	{
		try
		{
			String name = (String) generator.getClass().getMethod("getName").invoke(generator);
			if(!name.equals(""))
				return name;
		}catch(Exception e){
			
		}
		if(!getPrefix().equals("minecraft"))
		{
			return getPrefix();
		}
		return generator.getClass().getName();
	}
	
	public static String getPrefix()
    {
        ModContainer mc = Loader.instance().activeModContainer();

        if (mc != null)
        {
            return mc.getModId();
        }
        else // no mod container, assume minecraft
        {
            return "minecraft";
        }
    }
	
	public static boolean containsName(String name)
	{
		for (int i = 0; i < worldGenerators.size(); i++) {
			if(worldGenerators.get(i).name.equals(name))
				return true;
		}
		return false;
	}
	
	public static void onAdd(IWorldGenerator generator)
	{
		try
		{
			String name = getName(generator);
			while (containsName(name)) {
				name += "I";
			}
			WorldGenerator worldGenerator = new WorldGenerator(name, generator, getPrefix());
			worldGenerators.add(worldGenerator);
			configuration.load();
			worldGenerator.state = configuration.get("regeneration", worldGenerator.name, 1).getInt();
	    	configuration.save();
		}catch(Exception e){
			System.out.println("Error registering World Generator");
			e.printStackTrace();
		}
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller) {
		bus.register(this);
		return true;
	}

	@Subscribe
	public void modConstruction(FMLConstructionEvent evt){
	}

	@Subscribe
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new ChunkHandler());
		
		//GameRegistry.registerWorldGenerator(new WorldTestGenerator(), 0);
	}

	@Subscribe
	public void preInit(FMLPreInitializationEvent evt) {
		configuration = new Configuration(evt.getSuggestedConfigurationFile());
		configuration.load();
		configuration.addCustomCategoryComment("general", "0: nothing, 1: regenerate, 2: ungenerate");
		active = configuration.get("general", "active", false).getBoolean(false);
		configuration.save();
	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent evt) {

	}
}
