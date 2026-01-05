package com.anthonyhilyard.questplaques.mixin;

import java.util.Iterator;

import com.anthonyhilyard.advancementplaques.ui.render.AdvancementPlaque;
import com.anthonyhilyard.advancementplaques.ui.ToastComponentWrapper;
import com.anthonyhilyard.questplaques.QuestPlaque;
import com.anthonyhilyard.questplaques.QuestPlaques;
import com.anthonyhilyard.questplaques.QuestDisplay;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastComponent;


@Mixin(ToastComponentWrapper.class)
public class AdvancementPlaquesToastGuiMixin extends ToastComponent
{
	public AdvancementPlaquesToastGuiMixin(Minecraft p_94918_) { super(p_94918_); }

	@Shadow(remap = false)
	@Final
	private AdvancementPlaque[] plaques;

	@Shadow(remap = false)
	@Final
	private Minecraft mc;

	@Inject(method = "render", at = @At("TAIL"))
	private void renderQuestPlaques(GuiGraphics graphics, CallbackInfo ci)
	{
		// Render the current active quest plaque (only one at a time)
		QuestPlaque currentPlaque = QuestPlaques.getCurrentActivePlaque();
		if (currentPlaque != null)
		{
			// Count active advancement plaques to position quest plaque correctly
			int advancementPlaqueCount = 0;
			for (int i = 0; i < plaques.length; i++)
			{
				if (plaques[i] != null)
				{
					advancementPlaqueCount++;
				}
			}
			
			// Render the current quest plaque
			if (currentPlaque.render(graphics.guiWidth(), advancementPlaqueCount, graphics))
			{
				// Quest plaque finished rendering completely, clear it so next one can show
				QuestPlaques.clearCurrentActivePlaque();
			}
		}

		// Only start a new quest plaque if there's no active one and we have completed quests waiting
		if (!QuestPlaques.hasActivePlaque() && QuestPlaques.hasCompletedQuests())
		{
			QuestDisplay quest = QuestPlaques.getNextCompletedQuest();
			if (quest != null)
			{
				com.anthonyhilyard.iceberg.renderer.CustomItemRenderer itemRenderer = new com.anthonyhilyard.iceberg.renderer.CustomItemRenderer(mc.getTextureManager(), mc.getModelManager(), mc.getItemColors(), mc.getItemRenderer().getBlockEntityRenderer(), mc);
				QuestPlaque questPlaque = new QuestPlaque(quest, mc, itemRenderer);
				QuestPlaques.setCurrentActivePlaque(questPlaque);
			}
		}
	}
}
