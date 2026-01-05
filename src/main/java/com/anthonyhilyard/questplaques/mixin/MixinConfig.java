package com.anthonyhilyard.questplaques.mixin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.minecraftforge.fml.loading.FMLLoader;

public class MixinConfig implements IMixinConfigPlugin
{
	private Set<String> loadedMods = null;

	@Override
	public void onLoad(String mixinPackage) { }

	@Override
	public String getRefMapperConfig() { return null; }

	/**
	 * A set of mod ids that can all be inclusively required for a given mixin.
	 */
	private static final Set<String> inclusiveMods = Sets.newHashSet("toastcontrol", "ftbquests");

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		// Get the set of loaded mod ids.
		if (loadedMods == null)
		{
			loadedMods = FMLLoader.getLoadingModList().getMods().stream().map(mod -> mod.getModId()).collect(Collectors.toSet());
		}

		// Get the set of modids that this mixin should apply to.
		Set<String> mixinAppliesToMods = inclusiveMods.stream().filter(modid -> mixinClassName.toLowerCase().contains(modid)).collect(Collectors.toSet());

		// Load the mixin if all mods required for this mixin are loaded.
		return loadedMods.containsAll(mixinAppliesToMods);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

	@Override
	public List<String> getMixins() { return null; }

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}
