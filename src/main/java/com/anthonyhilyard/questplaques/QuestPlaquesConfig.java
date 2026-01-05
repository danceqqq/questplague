package com.anthonyhilyard.questplaques;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.electronwill.nightconfig.core.Config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.advancements.FrameType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.LongValue;

public class QuestPlaquesConfig
{
	public static final ForgeConfigSpec SPEC;
	public static final QuestPlaquesConfig INSTANCE;
	static
	{
		Config.setInsertionOrderPreserved(true);
		Pair<QuestPlaquesConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(QuestPlaquesConfig::new);
		SPEC = specPair.getRight();
		INSTANCE = specPair.getLeft();
	}

	public final BooleanValue tasks;
	public final BooleanValue goals;
	public final BooleanValue challenges;

	public final LongValue titleColor;
	public final LongValue nameColor;

	public final BooleanValue consolidateSimpleQuests;
	private final ConfigValue<List<? extends String>> whitelist;

	public QuestPlaquesConfig(ForgeConfigSpec.Builder build)
	{
		build.comment(" Quest Plaques Configuration Options\n" + 
					  " The options set here will apply only to plaques shown for quests, not advancements.\n" +
					  " If you would like to change display options, like plaque position or duration, these settings can be found in the Advancement Plaques configuration file.").push("client").push("visual_options");

		tasks = build.comment(" If plaques should show for \"task\"-level quests (quest tasks).").define("tasks", true);
		goals = build.comment(" If plaques should show for \"goal\"-level quests (standard quests).").define("goals", true);
		challenges = build.comment(" If plaques should show for \"challenge\"-level quests (quest chapters).").define("challenges", true);

		titleColor = build.comment(" Text color to use for plaque titles (like \"Quest Complete!\"). Can be entered as an 8-digit hex color code 0xAARRGGBB for convenience.").defineInRange("title_color", 0xFF332200L, 0x00000000L, 0xFFFFFFFFL);
		nameColor = build.comment(" Text color to use for quest names on plaques. Can be entered as an 8-digit hex color code 0xAARRGGBB for convenience.").defineInRange("name_color", 0xFFFFFFFFL, 0x00000000L, 0xFFFFFFFFL);

		build.pop().push("functionality_options");

		consolidateSimpleQuests = build.comment(" Whether to skip the task plaque for quests that only have a single task.").define("consolidate_simple_quests", true);
		whitelist = build.comment(" Whitelist of quests to show plaques for.  Leave empty to display for all.  Specify as numerical quest ID for FTB Quests and Custom Quests.").defineListAllowEmpty(Arrays.asList("whitelist"), () -> new ArrayList<String>(), QuestPlaquesConfig::validateWhitelist );

		build.pop().pop();
	}

	public boolean shouldShowPlaque(FrameType frameType, Object questID)
	{
		if (frameType == FrameType.TASK && !tasks.get() ||
			frameType == FrameType.GOAL && !goals.get() ||
			frameType == FrameType.CHALLENGE && !challenges.get())
		{
			return false;
		}

		if (whitelist.get().isEmpty())
		{
			return true;
		}
		else
		{
			if (whitelist.get().contains(questID))
			{
				return true;
			}

			if (questID instanceof Number number)
			{
				String decimalString = Long.toString(number.longValue());
				String hexString = Long.toHexString(number.longValue());
				
				// Check if this value is available as a string in decimal or hex.
				if (whitelist.get().contains(decimalString) ||
					whitelist.get().contains(hexString) ||
					whitelist.get().contains(hexString.toUpperCase()))
				{
					return true;
				}
			}
		}
		return false;
	}

	private static boolean validateWhitelist(Object value)
	{
		if (value instanceof Number)
		{
			return true;
		}
		else if (value instanceof String string)
		{
			try
			{
				Long.parseLong(string);
				return true;
			}
			catch (Exception e)
			{
				try
				{
					// Try again in hex just in case.
					Long.parseLong(string, 16);
				}
				catch (Exception e2)
				{
					return false;
				}
				return true;
			}
		}
		return false;
	}
}
