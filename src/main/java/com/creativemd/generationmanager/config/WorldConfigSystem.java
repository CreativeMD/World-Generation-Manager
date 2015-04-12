package com.creativemd.generationmanager.config;

import java.util.ArrayList;

import net.minecraftforge.common.config.Configuration;

import com.creativemd.craftingmanager.api.common.utils.entry.BooleanEntry;
import com.creativemd.craftingmanager.api.common.utils.entry.StateEntry;
import com.creativemd.craftingmanager.api.common.utils.packet.BooleanPacketEntry;
import com.creativemd.craftingmanager.api.common.utils.packet.IntegerPacketEntry;
import com.creativemd.craftingmanager.api.common.utils.packet.PacketEntry;
import com.creativemd.craftingmanager.api.common.utils.packet.StringPacketEntry;
import com.creativemd.craftingmanager.api.core.ConfigEntry;
import com.creativemd.craftingmanager.api.core.ConfigSystem;
import com.creativemd.craftingmanager.api.core.ConfigTab;
import com.creativemd.generationmanager.GenerationDummyContainer;

public class WorldConfigSystem extends ConfigSystem{

	public WorldConfigSystem() {
		super("Enable/Disable Regeneration", WorldConfig.worldTab);
	}

	@Override
	public void loadSystem() {
		
	}

	@Override
	public void loadConfig(Configuration config) {
		
	}

	@Override
	public void saveConfig(Configuration config) {
		GenerationDummyContainer.configuration.load();
		GenerationDummyContainer.configuration.get("general", "active", false).set(GenerationDummyContainer.active);
		for (int i = 0; i < GenerationDummyContainer.worldGenerators.size(); i++) {
			GenerationDummyContainer.configuration.get("regeneration", GenerationDummyContainer.worldGenerators.get(i).name, 0).set(GenerationDummyContainer.worldGenerators.get(i).state);
		}
		
		GenerationDummyContainer.configuration.save();
	}

	@Override
	public ArrayList<ConfigEntry> getEntries() {
		ArrayList<ConfigEntry> entries = new ArrayList<ConfigEntry>();
		entries.add(new BooleanEntry("Regeneration", GenerationDummyContainer.active));
		for (int i = 0; i < GenerationDummyContainer.worldGenerators.size(); i++) {
			if(GenerationDummyContainer.worldGenerators.get(i).canUngenerate())
				entries.add(new StateEntry(GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).state, "Disabled", "Enabled", "Ungenerate"));
			else
				entries.add(new StateEntry(GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).state, "Disabled", "Enabled"));
		}
		return entries;
	}

	@Override
	public ArrayList<PacketEntry> getPacketInformation() {
		ArrayList<PacketEntry> entries = new ArrayList<PacketEntry>();
		entries.add(new BooleanPacketEntry(GenerationDummyContainer.active));
		for (int i = 0; i < GenerationDummyContainer.worldGenerators.size(); i++) {
			entries.add(new StringPacketEntry(GenerationDummyContainer.worldGenerators.get(i).name));
			entries.add(new IntegerPacketEntry(GenerationDummyContainer.worldGenerators.get(i).state));
		}
		return entries;
	}
	
	public void UpdateInformation(ArrayList<PacketEntry> Packet)
	{
		GenerationDummyContainer.active = ((BooleanPacketEntry)Packet.get(0)).value;
		String[] mods = new String[(Packet.size()-1)/2];
		for (int i = 0; i < (Packet.size()-1)/2; i++) {
			mods[i] = ((StringPacketEntry)Packet.get(i*2+1)).value;
			GenerationDummyContainer.worldGenerators.get(i).state = ((IntegerPacketEntry)Packet.get(i*2+2)).value;
		}
	}

	@Override
	public String getRecieveInformation() {
		return "";
	}

	@Override
	public boolean needClientUpdate() {
		return false;
	}

	@Override
	public void onEntryChange(ConfigEntry entry) {
		if(entry instanceof BooleanEntry)
		{
			if(((BooleanEntry) entry).Title.equals("Regeneration"))
				GenerationDummyContainer.active = ((BooleanEntry) entry).value;
		}else{
			GenerationDummyContainer.getWorldGenerator(((StateEntry) entry).Title).state = ((StateEntry) entry).getState();
		}
	}

}
