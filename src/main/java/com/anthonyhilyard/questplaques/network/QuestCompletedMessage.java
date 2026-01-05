package com.anthonyhilyard.questplaques.network;

import java.util.function.Supplier;

import com.anthonyhilyard.questplaques.Loader;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkEvent;


public class QuestCompletedMessage
{
	public enum QuestMod
	{
		FTB_QUESTS,
		CUSTOM_QUESTS
	}

	private final QuestMod sourceMod;
	private final long questID;

	public QuestCompletedMessage(final QuestMod sourceMod, final long questID)
	{
		this.sourceMod = sourceMod;
		this.questID = questID;
	}

	public static void encode(final QuestCompletedMessage msg, final FriendlyByteBuf packetBuffer)
	{
		packetBuffer.writeEnum(msg.sourceMod);
		packetBuffer.writeLong(msg.questID);
	}

	public static QuestCompletedMessage decode(final FriendlyByteBuf packetBuffer)
	{
		return new QuestCompletedMessage(packetBuffer.readEnum(QuestMod.class), packetBuffer.readLong());
	}

	public static void handle(final QuestCompletedMessage msg, final Supplier<NetworkEvent.Context> contextSupplier)
	{
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			try
			{
				switch (msg.sourceMod)
				{
					case FTB_QUESTS:
						if (ModList.get().isLoaded("ftbquests"))
						{
							Class.forName("com.anthonyhilyard.questplaques.compat.FTBQuests.Handler").getMethod("finishQuest", long.class).invoke(null, msg.questID);
						}
						break;
					case CUSTOM_QUESTS:
						if (ModList.get().isLoaded("customquests"))
						{
							Class.forName("com.anthonyhilyard.questplaques.compat.CustomQuests.Handler").getMethod("finishQuest", long.class).invoke(null, msg.questID);
						}
						break;
				}
			}
			catch (Exception e)
			{
				Loader.LOGGER.warn(ExceptionUtils.getStackTrace(e));
			}
		});
		context.setPacketHandled(true);
	}
}
