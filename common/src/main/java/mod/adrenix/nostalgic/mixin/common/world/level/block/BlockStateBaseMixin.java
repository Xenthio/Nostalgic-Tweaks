package mod.adrenix.nostalgic.mixin.common.world.level.block;

import mod.adrenix.nostalgic.NostalgicTweaks;
import mod.adrenix.nostalgic.common.config.ModConfig;
import mod.adrenix.nostalgic.util.common.BlockCommonUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin
{
    /* Shadows */

    @Shadow public abstract Block getBlock();

    /* Injections */

    /**
     * Client:
     *
     * The following injections only impact the rendering aspects of chests. Therefore, there is no need to restrict
     * these injections to server-side only. The purpose of these injections is to improve performance when chests are
     * shaped like blocks.
     *
     * Controlled by various old chest tweaks.
     */

    @Inject(method = "canOcclude", at = @At("HEAD"), cancellable = true)
    private void NT$onCanOcclude(CallbackInfoReturnable<Boolean> callback)
    {
        if (NostalgicTweaks.isClient() && BlockCommonUtil.isOldChest(this.getBlock()))
            callback.setReturnValue(false);
    }

    @Inject(method = "getFaceOcclusionShape", at = @At("HEAD") , cancellable = true)
    private void NT$onGetFaceOcclusionShape(BlockGetter level, BlockPos pos, Direction direction, CallbackInfoReturnable<VoxelShape> callback)
    {
        if (NostalgicTweaks.isClient() && BlockCommonUtil.isOldChest(this.getBlock()))
            callback.setReturnValue(Shapes.block());
    }

    /**
     * Client:
     *
     * Disables the random offset positions for blocks such as flowers and tall-grass.
     * This will only be applied client side since this is a preference rendering option.
     *
     * Controlled by the disable offset tweak.
     */
    @Inject(method = "getOffset", at = @At("HEAD"), cancellable = true)
    private void NT$onGetOffset(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Vec3> callback)
    {
        if (NostalgicTweaks.isClient() && ModConfig.Candy.disableOffset())
            callback.setReturnValue(Vec3.ZERO);
    }

    /**
     * Multiplayer:
     *
     * Setting the shape to a full block prevents light from coming through chests. Because chests are being changed to
     * a full-block voxel shape, this should be restricted by a server-only tweak to prevent vanilla multiplayer issues.
     *
     * Controlled by the old chest voxel tweak and various old chest tweaks.
     */

    @Inject(method = "isSolidRender", at = @At("HEAD"), cancellable = true)
    private void NT$onIsSolidRender(CallbackInfoReturnable<Boolean> callback)
    {
        if (NostalgicTweaks.isClient() && ModConfig.Candy.oldChestVoxel() && BlockCommonUtil.isOldChest(this.getBlock()))
            callback.setReturnValue(true);
    }

    @Inject
    (
        cancellable = true,
        method = "getShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
        at = @At("HEAD")
    )
    private void NT$onGetShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> callback)
    {
        if (NostalgicTweaks.isClient() && ModConfig.Candy.oldChestVoxel() && BlockCommonUtil.isOldChest(this.getBlock()))
            callback.setReturnValue(Shapes.block());
    }
}
