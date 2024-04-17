package de.hysky.skyblocker.mixin;

import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import de.hysky.skyblocker.skyblock.entity.MobBoundingBoxes;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

	@Inject(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;regularEntityCount:I", opcode = Opcodes.PUTFIELD))
	private void skyblocker$beforeEntityIsRendered(CallbackInfo ci, @Local Entity entity, @Share("renderedBoundingBox") LocalBooleanRef renderedBoundingBox) {
		boolean shouldShowBoundingBox = MobBoundingBoxes.shouldDrawMobBoundingBox(entity);

		if (shouldShowBoundingBox) {
			renderedBoundingBox.set(true);
			MobBoundingBoxes.submitBox2BeRendered(entity.getBoundingBox(), MobBoundingBoxes.getBoxColor(entity));
		}
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"))
	private boolean skyblocker$shouldMobGlow(boolean original, @Local Entity entity, @Share("renderedBoundingBox") LocalBooleanRef renderedBoundingBox, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
        boolean allowGlow = LividColor.allowGlow();
        boolean shouldGlow = !renderedBoundingBox.get() && MobGlow.shouldMobGlow(entity);
		hasCustomGlow.set(shouldGlow);
		return allowGlow && original || shouldGlow;
	}

	@ModifyVariable(method = "render",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;hasOutline(Lnet/minecraft/entity/Entity;)Z"), to = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/OutlineVertexConsumerProvider;setColor(IIII)V")),
			at = @At("STORE"), ordinal = 0
	)
	private int skyblocker$modifyGlowColor(int color, @Local Entity entity, @Share("hasCustomGlow") LocalBooleanRef hasCustomGlow) {
		return hasCustomGlow.get() ? MobGlow.getGlowColor(entity) : color;
	}
}
