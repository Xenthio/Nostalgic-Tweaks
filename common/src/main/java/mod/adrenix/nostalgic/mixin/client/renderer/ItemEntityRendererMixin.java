package mod.adrenix.nostalgic.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.adrenix.nostalgic.common.config.ModConfig;
import mod.adrenix.nostalgic.util.client.ItemClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin
{
    /* Shadows */

    @Shadow @Final private ItemRenderer itemRenderer;

    /**
     * Forces the item entity's rotation to always face the player. Controlled by the old floating item tweak.
     */
    @Redirect(
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"
        )
    )
    private void NT$rotationProxy(PoseStack poseStack, Quaternionf quaternion, ItemEntity itemEntity, float entityYaw, float partialTicks)
    {
        if (ModConfig.Candy.oldFloatingItems())
        {
            BakedModel model = this.itemRenderer.getModel(itemEntity.getItem(), null, null, 0);

            if (ItemClientUtil.isModelFlat(model))
            {
                float degrees = 180F - Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
            }
            else
                poseStack.mulPose(Axis.YP.rotation(itemEntity.getSpin(partialTicks)));
        }
        else
            poseStack.mulPose(Axis.YP.rotation(itemEntity.getSpin(partialTicks)));
    }

    /**
     * Renders floating item entities as 2D. Controlled by the old floating items tweak.
     */
    @Inject(
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            shift = At.Shift.BEFORE,
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
        )
    )
    private void NT$onRender(ItemEntity itemEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo callback)
    {
        if (ModConfig.Candy.oldFloatingItems())
        {
            BakedModel model = itemRenderer.getModel(itemEntity.getItem(), null, null, 0);

            if (ItemClientUtil.isModelFlat(model))
            {
                ItemClientUtil.flatten(poseStack);
                ItemClientUtil.disableDiffusedLighting();
            }
        }
    }

    /**
     * Enables diffused lighting after it has been disabled before rendering the item entity. Not controlled by any
     * tweak.
     */
    @Inject(
        method = "render(Lnet/minecraft/world/entity/item/ItemEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(
            shift = At.Shift.AFTER,
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
        )
    )
    private void NT$onFinishRender(ItemEntity itemEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo callback)
    {
        ItemClientUtil.enableDiffusedLighting();
    }
}