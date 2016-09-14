package com.creativemd.generationmanager.config;

import com.creativemd.creativecore.client.avatar.Avatar;
import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.generationmanager.GenerationDummyContainer;
import com.creativemd.igcm.api.common.branch.ConfigBranch;
import com.creativemd.igcm.api.common.branch.ConfigSegmentCollection;
import com.creativemd.igcm.api.common.segment.BooleanSegment;
import com.creativemd.igcm.api.common.segment.SelectSegment;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WorldGeneralBranch extends ConfigBranch{

	public WorldGeneralBranch(String name) {
		super(name);
	}
	
	@Override
	public void createConfigSegments() {
		segments.add(new BooleanSegment("active", "Active", GenerationDummyContainer.active));
		for (int i = 0; i < GenerationDummyContainer.worldGenerators.size(); i++) {
			if(GenerationDummyContainer.worldGenerators.get(i).canUngenerate())
				segments.add(new SelectSegment(GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).state, "Disabled", "Enabled", "Ungenerate"));
			else
				segments.add(new SelectSegment(GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).name, GenerationDummyContainer.worldGenerators.get(i).state, "Disabled", "Enabled"));
		}
	}

	@Override
	public boolean needPacket() {
		return true;
	}

	@Override
	public void onRecieveFrom(boolean isServer, ConfigSegmentCollection collection) {
		GenerationDummyContainer.active = (Boolean) collection.getSegmentValue("active");
		
		for (int i = 0; i < GenerationDummyContainer.worldGenerators.size(); i++) {
			GenerationDummyContainer.worldGenerators.get(i).state = ((SelectSegment)collection.getSegmentByID(GenerationDummyContainer.worldGenerators.get(i).name)).getIndex();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected Avatar getAvatar() {
		return new AvatarItemStack(new ItemStack(Blocks.COAL_ORE));
	}

	@Override
	public void loadCore() {
		
	}

}
