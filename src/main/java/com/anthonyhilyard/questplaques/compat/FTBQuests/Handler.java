package com.anthonyhilyard.questplaques.compat.FTBQuests;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.anthonyhilyard.questplaques.QuestDisplay;
import com.anthonyhilyard.questplaques.QuestPlaquesConfig;
import com.anthonyhilyard.questplaques.event.QuestCompletedEvent;
import com.anthonyhilyard.questplaques.event.QuestCompletedEvent.QuestType;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.architectury.event.EventResult;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class Handler
{
	private static final Map<QuestObjectType, FrameType> frameMap = new HashMap<>() {{
		put(QuestObjectType.TASK, FrameType.TASK);
		put(QuestObjectType.QUEST, FrameType.GOAL);
		put(QuestObjectType.CHAPTER, FrameType.CHALLENGE);
		put(QuestObjectType.FILE, FrameType.CHALLENGE);
	}};

	private static final Map<QuestObjectType, String> translationKeyMap = new HashMap<>() {{
		put(QuestObjectType.TASK, "ftbquests.task");
		put(QuestObjectType.QUEST, "ftbquests.quest");
		put(QuestObjectType.CHAPTER, "ftbquests.chapter");
		put(QuestObjectType.FILE, "ftbquests.file");
	}};

	public static void init()
	{
		ObjectCompletedEvent.GENERIC.register(event -> 
		{
			finishQuest(event.getObject().id);
			return EventResult.pass();
		});
	}

	public static QuestDisplay getQuestInfo(QuestObject quest)
	{
		FrameType frame = frameMap.getOrDefault(quest.getObjectType(), FrameType.TASK);
		String translationKey = translationKeyMap.getOrDefault(quest.getObjectType(), "ftbquests.quest");
		Component title = Component.translatable(translationKey + ".completed").withStyle(quest.getObjectType().getColor());
		Component questName = quest.getTitle();

		ItemStack itemStack = ItemStack.EMPTY;
		Icon icon = quest.getIcon();

		if (icon instanceof ItemIcon itemIcon)
		{
			itemStack = itemIcon.getStack();
		}

		if (itemStack.isEmpty())
		{
			Icon altIcon = quest.getAltIcon();
			if (altIcon instanceof ItemIcon itemIcon)
			{
				itemStack = itemIcon.getStack();
			}
		}

		QuestDisplay result = new QuestDisplay(questName, title, itemStack, frame);
		return result;
	}

	private static boolean isToastDisabled(QuestObject quest)
	{
		try
		{
			Field field = QuestObject.class.getDeclaredField("disableToast");
			field.setAccessible(true);
			return field.getBoolean(quest);
		}
		catch (Exception e)
		{
			// If field access fails, assume toast is enabled
			return false;
		}
	}

	private static int getQuestTaskCount(Task task)
	{
		try
		{
			Field questField = Task.class.getDeclaredField("quest");
			questField.setAccessible(true);
			Object questObj = questField.get(task);
			
			Field tasksField = questObj.getClass().getDeclaredField("tasks");
			tasksField.setAccessible(true);
			Object tasks = tasksField.get(questObj);
			
			if (tasks instanceof java.util.Collection)
			{
				return ((java.util.Collection<?>)tasks).size();
			}
		}
		catch (Exception e)
		{
			// If field access fails, return 0 to skip consolidation check
		}
		return 0;
	}

	public static void finishQuest(long questID)
	{
		final Minecraft minecraft = Minecraft.getInstance();
		QuestObject quest = ClientQuestFile.INSTANCE.get(questID);

		// Toast is disabled for this quest object, so bail.
		if (isToastDisabled(quest))
		{
			return;
		}

		QuestType questType;
		switch (quest.getObjectType())
		{
			case FILE:
			case CHAPTER:
				questType = QuestType.CHAPTER;
				break;
			case TASK:
				questType = QuestType.TASK;
				break;
			default:
			case QUEST:
				questType = QuestType.QUEST;
				break;
		}

		// Check if this is a task from a simple quest and skip it if configured so.
		if (QuestPlaquesConfig.INSTANCE.consolidateSimpleQuests.get() && quest instanceof Task task)
		{
			int taskCount = getQuestTaskCount(task);
			if (taskCount == 1)
			{
				return;
			}
		}

		MinecraftForge.EVENT_BUS.post(new QuestCompletedEvent(minecraft.player, getQuestInfo(quest), questID, questType));
	}
}
