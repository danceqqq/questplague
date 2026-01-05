package com.anthonyhilyard.questplaques;

import javax.annotation.Nonnull;

import com.anthonyhilyard.advancementplaques.AdvancementPlaques;
import com.anthonyhilyard.advancementplaques.config.AdvancementPlaquesConfig;
import com.anthonyhilyard.iceberg.renderer.CustomItemRenderer;
import com.anthonyhilyard.iceberg.util.GuiHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast.Visibility;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.locale.Language;


public class QuestPlaque
{
	private long animationTime = -1L;
	private long visibleTime = -1L;
	private boolean hasPlayedSound = false;
	private Visibility visibility = Visibility.SHOW;
	private final Minecraft minecraft;
	private final CustomItemRenderer itemRenderer;
	private final QuestDisplay display;

	public QuestPlaque(@Nonnull QuestDisplay display, Minecraft mcIn, CustomItemRenderer itemRendererIn)
	{
		minecraft = mcIn;
		itemRenderer = itemRendererIn;
		this.display = display;
	}

	public int width()
	{
		return 256;
	}

	public int height()
	{
		return 32;
	}

	private float getVisibility(long currentTime)
	{
		float f = Mth.clamp((float)(currentTime - animationTime) / 200.0f, 0.0f, 1.0f);
		f = f * f;
		return visibility == Visibility.HIDE ? 1.0f - f : f;
	}

	private Visibility drawPlaque(GuiGraphics graphics, long displayTime)
	{
		final float fadeInTime;
		final float fadeOutTime;
		final float duration;
		
		switch (display.frame())
		{
			default:
			case TASK:
				fadeInTime = (float)(AdvancementPlaquesConfig.INSTANCE.taskEffectFadeInTime.get() * 1000.0);
				fadeOutTime = (float)(AdvancementPlaquesConfig.INSTANCE.taskEffectFadeOutTime.get() * 1000.0);
				duration = (float)(AdvancementPlaquesConfig.INSTANCE.taskDuration.get() * 1000.0);
				break;
			case GOAL:
				fadeInTime = (float)(AdvancementPlaquesConfig.INSTANCE.goalEffectFadeInTime.get() * 1000.0);
				fadeOutTime = (float)(AdvancementPlaquesConfig.INSTANCE.goalEffectFadeOutTime.get() * 1000.0);
				duration = (float)(AdvancementPlaquesConfig.INSTANCE.goalDuration.get() * 1000.0);
				break;
			case CHALLENGE:
				fadeInTime = (float)(AdvancementPlaquesConfig.INSTANCE.challengeEffectFadeInTime.get() * 1000.0);
				fadeOutTime = (float)(AdvancementPlaquesConfig.INSTANCE.challengeEffectFadeOutTime.get() * 1000.0);
				duration = (float)(AdvancementPlaquesConfig.INSTANCE.challengeDuration.get() * 1000.0);
				break;
		}

		graphics.drawManaged(() ->
		{
			if (displayTime >= fadeInTime)
			{
				float alpha = 1.0f;
				if (displayTime > duration)
				{
					alpha = Math.max(0.0f, Math.min(1.0f, 1.0f - ((float)displayTime - duration) / 1000.0f));
				}

				// Grab the title and name colors using the same methods as AdvancementPlaques.
				int titleColor = AdvancementPlaquesConfig.INSTANCE.getTitleColor(alpha).getValue();
				int nameColor = AdvancementPlaquesConfig.INSTANCE.getNameColor(alpha).getValue();

				RenderSystem.enableBlend();
				RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
				RenderSystem.setShaderTexture(0, AdvancementPlaques.TEXTURE_PLAQUES);
				int frameOffset = 0;
				if (display.frame() == FrameType.GOAL)
				{
					frameOffset = 1;
				}
				else if (display.frame() == FrameType.CHALLENGE)
				{
					frameOffset = 2;
				}
				GuiHelper.blit(graphics.pose(), -1, -1, width(), height(), 0, height() * frameOffset, width(), height(), 256, 256);

				// Only bother drawing text if alpha is greater than 0.1.
				if (alpha > 0.1f)
				{
					// Text like "Challenge Complete!" at the top of the plaque.
					int typeWidth = minecraft.font.width(display.title());
					
					// GuiGraphics.drawString doesn't support alpha properly, so draw the string manually.
					minecraft.font.drawInBatch(Language.getInstance().getVisualOrder(display.title()), (int)((width() - typeWidth) / 2.0f + 15.0f), 5, titleColor, false, graphics.pose().last().pose(), graphics.bufferSource(), DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);

					int titleWidth = minecraft.font.width(display.questName());

					// If the width of the advancement title is less than the full available width, display it normally.
					if (titleWidth <= (220 / 1.5f))
					{
						PoseStack poseStack = graphics.pose();
						poseStack.pushPose();
						poseStack.scale(1.5f, 1.5f, 1.0f);

						// GuiGraphics.drawString doesn't support alpha properly, so draw the string manually.
						minecraft.font.drawInBatch(Language.getInstance().getVisualOrder(display.questName()), (int)(((width() / 1.5f) - titleWidth) / 2.0f + (15.0f / 1.5f)), 9, nameColor, false, graphics.pose().last().pose(), graphics.bufferSource(), DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);

						poseStack.popPose();
					}
					// Otherwise, display it with a smaller (default) font.
					else
					{
						// GuiGraphics.drawString doesn't support alpha properly, so draw the string manually.
						minecraft.font.drawInBatch(Language.getInstance().getVisualOrder(display.questName()), (int)((width() - titleWidth) / 2.0f + 15.0f), 15, nameColor, false, graphics.pose().last().pose(), graphics.bufferSource(), DisplayMode.SEE_THROUGH, 0, LightTexture.FULL_BRIGHT);
					}

					graphics.flush();
				}

				PoseStack poseStack = graphics.pose();
				poseStack.pushPose();
				poseStack.translate(1.0f, 1.0f, 0.0f);
				poseStack.scale(1.5f, 1.5f, 1.0f);

				itemRenderer.renderItemModelIntoGUIWithAlpha(poseStack, display.stack(), 1, 1, alpha);

				poseStack.popPose();

				if (!hasPlayedSound)
				{
					hasPlayedSound = true;

					final FrameType frameType = display.frame();
					try
					{
						switch (frameType)
						{
							case TASK:
								final double taskVolume = AdvancementPlaquesConfig.INSTANCE.taskVolume.get();
								if (taskVolume > 0.0)
								{
									minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AdvancementPlaques.TASK_COMPLETE.get(), 1.0f, (float)taskVolume));
								}
								break;
							case GOAL:
								final double goalVolume = AdvancementPlaquesConfig.INSTANCE.goalVolume.get();
								if (goalVolume > 0.0)
								{
									minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AdvancementPlaques.GOAL_COMPLETE.get(), 1.0f, (float)goalVolume));
								}
								break;
							default:
							case CHALLENGE:
								final double challengeVolume = AdvancementPlaquesConfig.INSTANCE.challengeVolume.get();
								if (challengeVolume > 0.0)
								{
									minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, (float)challengeVolume));
								}
								break;
						}
					}
					catch (NullPointerException e)
					{
						Loader.LOGGER.warn("Tried to play a custom sound for an advancement, but that sound was not registered! Install Quest Plaques on the server or mute tasks and goals in the config file.");
					}
				}
			}

			if (displayTime < fadeInTime + fadeOutTime)
			{
				float alpha = 1.0f - ((float)(displayTime - fadeInTime) / fadeOutTime);
				if (displayTime < fadeInTime)
				{
					alpha = (float)displayTime / fadeInTime;
				}

				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
				PoseStack poseStack = graphics.pose();
				poseStack.pushPose();
				poseStack.translate(0.0f, 0.0f, 95.0f);
				RenderSystem.setShaderTexture(0, AdvancementPlaques.TEXTURE_PLAQUE_EFFECTS);

				if (display.frame() == FrameType.CHALLENGE)
				{
					GuiHelper.blit(poseStack, -16, -16, width() + 32, height() + 32, 0, height() + 32, width() + 32, height() + 32, 512, 512);
				}
				else
				{
					GuiHelper.blit(poseStack, -16, -16, width() + 32, height() + 32, 0, 0, width() + 32, height() + 32, 512, 512);
				}
				poseStack.popPose();
			}

			graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		});

		return displayTime >= fadeInTime + fadeOutTime + duration ? Visibility.HIDE : Visibility.SHOW;
	}

	public boolean render(int screenWidth, int index, GuiGraphics graphics)
	{
		long currentTime = Util.getMillis();
		if (animationTime == -1L)
		{
			animationTime = currentTime;
			visibility.playSound(minecraft.getSoundManager());
		}

		if (visibility == Visibility.SHOW && currentTime - animationTime <= 200L)
		{
			visibleTime = currentTime;
		}
		
		RenderSystem.disableDepthTest();
		PoseStack poseStack = graphics.pose();
		poseStack.pushPose();

		if (AdvancementPlaquesConfig.INSTANCE.onTop.get())
		{
			poseStack.translate((float)(graphics.guiWidth() - width()) / 2.0f,
									 AdvancementPlaquesConfig.INSTANCE.distance.get(),
									 800.0f + index);
		}
		else
		{
			poseStack.translate((float)(graphics.guiWidth() - width()) / 2.0f,
									 (float)(minecraft.getWindow().getGuiScaledHeight() - (height() + AdvancementPlaquesConfig.INSTANCE.distance.get())),
									 800.0f + index);
		}
		Visibility newVisibility = drawPlaque(graphics, currentTime - visibleTime);

		poseStack.popPose();
		RenderSystem.enableDepthTest();

		if (newVisibility != visibility)
		{
			animationTime = currentTime - (long)((int)((1.0f - getVisibility(currentTime)) * 200.0f));
			visibility = newVisibility;
		}

		return visibility == Visibility.HIDE && currentTime - animationTime > 200L;
	}
}
