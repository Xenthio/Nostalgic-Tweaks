package mod.adrenix.nostalgic.fabric.mixin.sodium;

import me.jellysquid.mods.sodium.client.world.WorldSlice;
import mod.adrenix.nostalgic.common.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSlice.class)
public abstract class WorldSliceMixin implements BlockAndTintGetter
{
    /* Shadows */

    @Shadow @Final private Level world;
    @Shadow public abstract LevelLightEngine getLightEngine();

    /* Injections */

    /**
     * Bypasses the section light getter so our custom light renderer can send the correct values to the world slice.
     * Controlled by the old light rendering tweak.
     */
    @Inject(method = "getBrightness", at = @At("HEAD"), cancellable = true)
    private void NT$onGetBrightness(LightLayer layer, BlockPos pos, CallbackInfoReturnable<Integer> callback)
    {
        if (ModConfig.Candy.oldLightRendering())
            callback.setReturnValue(this.world.getBrightness(layer, pos));
    }
}
