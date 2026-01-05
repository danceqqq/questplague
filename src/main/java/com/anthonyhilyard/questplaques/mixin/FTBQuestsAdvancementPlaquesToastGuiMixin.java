package com.anthonyhilyard.questplaques.mixin;

import com.anthonyhilyard.advancementplaques.ui.ToastComponentWrapper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;


@Mixin(ToastComponentWrapper.class)
public class FTBQuestsAdvancementPlaquesToastGuiMixin extends ToastComponent
{
	public FTBQuestsAdvancementPlaquesToastGuiMixin(Minecraft p_94918_) { super(p_94918_); }

	@Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
	public void cancelQuestToasts(Toast toastIn, CallbackInfo info)
	{
		// Check if this is an FTB Quests toast - class name may have changed in 1.20.1
		String className = toastIn.getClass().getName();
		if (className.contains("ftbquests") && (className.contains("Toast") || className.contains("Quest")))
		{
			info.cancel();
		}
	}
}
