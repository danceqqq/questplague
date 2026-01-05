package com.anthonyhilyard.questplaques.event;

import com.anthonyhilyard.questplaques.QuestDisplay;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * This event is fired when a player completes a quest, task, or collection of quests.
 * <br>
 * This event is not {@link net.minecraftforge.eventbus.api.Cancelable}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 */
public class QuestCompletedEvent extends PlayerEvent
{
	public enum QuestType
	{
		TASK,
		QUEST,
		CHAPTER
	}

	final QuestDisplay questInfo;
	final long questID;
	final QuestType type;

	public QuestCompletedEvent(Player player, QuestDisplay questInfo, long questID, QuestType type)
	{
		super(player);
		this.questInfo = questInfo;
		this.questID = questID;
		this.type = type;
	}

	public QuestDisplay getQuestInfo()
	{
		return questInfo;
	}

	public long getQuestID()
	{
		return questID;
	}

	public QuestType getType()
	{
		return type;
	}
}
