package com.anthonyhilyard.questplaques;

import org.apache.commons.lang3.exception.ExceptionUtils;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class QuestPlaquesServer
{
	public static void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(QuestPlaquesServer::onServerSetup);
	}

	public static void onServerSetup(FMLDedicatedServerSetupEvent event)
	{
		event.enqueueWork(() -> {
			try
			{
				// Set up FTB Quests compatibility.
				if (ModList.get().isLoaded("ftbquests"))
				{
					Class.forName("com.anthonyhilyard.questplaques.compat.FTBQuests.ServerHandler").getMethod("initServer").invoke(null);
				}

				// Set up Custom Quests compatibility.
				if (ModList.get().isLoaded("customquests"))
				{
					Class.forName("com.anthonyhilyard.questplaques.compat.CustomQuests.ServerHandler").getMethod("initServer").invoke(null);
				}
			}
			catch (Exception e)
			{
				Loader.LOGGER.warn(ExceptionUtils.getStackTrace(e));
			}
		});
	}
}
