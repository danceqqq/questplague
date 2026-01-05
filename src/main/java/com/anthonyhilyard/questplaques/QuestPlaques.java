package com.anthonyhilyard.questplaques;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;

import java.util.List;
import java.util.Optional;

import com.anthonyhilyard.questplaques.event.QuestCompletedEvent;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.exception.ExceptionUtils;

@Mod.EventBusSubscriber(modid = Loader.MODID, bus = Bus.FORGE)
public class QuestPlaques
{
	private static final int QUEST_DELAY = 2;
	private static final List<QuestDisplay> completedQuests = Lists.newArrayList();
	private static int questTimer = 0;
	private static QuestPlaque currentActivePlaque = null; // Only one active plaque at a time

	public static void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(QuestPlaques::onClientSetup);
	}

	public static boolean hasCompletedQuests()
	{
		if (questTimer > 0)
		{
			return false;
		}

		return !completedQuests.isEmpty();
	}

	public static boolean hasActivePlaque()
	{
		return currentActivePlaque != null;
	}

	public static QuestDisplay getNextCompletedQuest()
	{
		// Get the lowest-priority quest first.
		Optional<QuestDisplay> nextQuest = completedQuests.stream().min((a, b) -> Integer.compare(b.frame().getTexture(), a.frame().getTexture()));
		if (nextQuest.isPresent())
		{
			QuestDisplay result = nextQuest.get();
			completedQuests.remove(result);
			return result;
		}
		return null;
	}

	public static void addCompletedQuest(QuestDisplay quest)
	{
		questTimer = QUEST_DELAY;
		completedQuests.add(quest);
	}

	public static QuestPlaque getCurrentActivePlaque()
	{
		return currentActivePlaque;
	}

	public static void setCurrentActivePlaque(QuestPlaque plaque)
	{
		currentActivePlaque = plaque;
	}

	public static void clearCurrentActivePlaque()
	{
		currentActivePlaque = null;
	}

	@SubscribeEvent
	public static void onRenderTick(RenderTickEvent event)
	{
		if (questTimer > 0)
		{
			questTimer--;
		}
	}

	@SubscribeEvent
	public static void onQuestCompleted(QuestCompletedEvent event)
	{
		if (QuestPlaquesConfig.INSTANCE.shouldShowPlaque(event.getQuestInfo().frame(), event.getQuestID()))
		{
			addCompletedQuest(event.getQuestInfo());
		}
	}

	public static void onClientSetup(FMLClientSetupEvent event)
	{
		event.enqueueWork(() -> {
			try
			{
				// Set up FTB Quests compatibility.
				if (ModList.get().isLoaded("ftbquests"))
				{
					Class.forName("com.anthonyhilyard.questplaques.compat.FTBQuests.Handler").getMethod("init").invoke(null);
				}

				// Set up Custom Quests compatibility.
				if (ModList.get().isLoaded("customquests"))
				{
					Class.forName("com.anthonyhilyard.questplaques.compat.CustomQuests.Handler").getMethod("init").invoke(null);
				}
			}
			catch (Exception e)
			{
				Loader.LOGGER.warn(ExceptionUtils.getStackTrace(e));
			}
		});
	}
}
