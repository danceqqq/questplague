package com.anthonyhilyard.questplaques;

import net.minecraft.advancements.FrameType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** Yes, this entire file is for one record. */
public record QuestDisplay(Component questName, Component title, ItemStack stack, FrameType frame)
{

}
