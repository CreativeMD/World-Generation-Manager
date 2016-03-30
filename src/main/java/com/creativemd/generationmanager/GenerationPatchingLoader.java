package com.creativemd.generationmanager;

import java.io.File;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;

@MCVersion(value = "1.9")
public class GenerationPatchingLoader implements IFMLLoadingPlugin {
	
	public static File location;

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{GenerationTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return GenerationDummyContainer.class.getName();
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {
		location = (File) data.get("coremodLocation");
	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
