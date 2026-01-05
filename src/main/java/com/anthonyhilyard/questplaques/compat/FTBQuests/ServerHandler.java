package com.anthonyhilyard.questplaques.compat.FTBQuests;

import com.anthonyhilyard.questplaques.network.Protocol;
import com.anthonyhilyard.questplaques.network.QuestCompletedMessage;
import com.anthonyhilyard.questplaques.network.QuestCompletedMessage.QuestMod;

import dev.ftb.mods.ftbquests.events.ObjectCompletedEvent;
import dev.architectury.event.EventResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class ServerHandler
{
	public static void initServer()
	{
		ObjectCompletedEvent.GENERIC.register(event -> 
		{
			// Send a message to the client when a quest is completed.
			for (ServerPlayer player : event.getNotifiedPlayers())
			{
				Protocol.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new QuestCompletedMessage(QuestMod.FTB_QUESTS, event.getObject().id));
			}
			return EventResult.pass();
		});
	}
}
