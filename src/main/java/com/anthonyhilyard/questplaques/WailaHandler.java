package com.anthonyhilyard.questplaques;

import snownee.jade.Jade;

public class WailaHandler
{
	private static boolean previousState = true;
	private static boolean disabled = false;

	public static void disableWaila()
	{
		boolean currentState = Jade.CONFIG.get().getGeneral().shouldDisplayTooltip();
		if (!disabled || currentState)
		{
			previousState = currentState;
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(false);
			disabled = true;
		}
	}

	public static void enableWaila()
	{
		if (disabled)
		{
			Jade.CONFIG.get().getGeneral().setDisplayTooltip(previousState);
			disabled = false;
		}
	}
	
}
