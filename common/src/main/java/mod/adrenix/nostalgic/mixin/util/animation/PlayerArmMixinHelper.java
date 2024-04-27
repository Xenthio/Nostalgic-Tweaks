package mod.adrenix.nostalgic.mixin.util.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.adrenix.nostalgic.mixin.util.swing.SwingType;
import mod.adrenix.nostalgic.tweak.config.AnimationTweak;
import mod.adrenix.nostalgic.util.common.data.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/**
 * This utility class is used only by the client.
 */
public abstract class PlayerArmMixinHelper
{
    /* Fields */

    /**
     * Changes the swinging animation based on the current swing type. This is used by the old classic swing tweak since
     * different animations played depending on whether the mouse was left-clicked or right-clicked.
     */
    public static final Holder<SwingType> SWING_TYPE = Holder.create(SwingType.HIT);

    /* Methods */

    /**
     * Apply edits to the given matrix stack that simulate the old swinging animation.
     *
     * @param poseStack     The current {@link PoseStack}.
     * @param swingProgress The current arm swing progress.
     */
    public static void oldSwing(PoseStack poseStack, float swingProgress)
    {
        float progress = Mth.sin((float) Math.PI * swingProgress);
        float scale = 1.0F - (0.3F * progress);

        poseStack.translate(-0.12F * progress, 0.085F * progress, 0.0F);
        poseStack.scale(scale, scale, scale);
    }

    /**
     * Apply edits to the given matrix stack that simulate the old classic swinging animation.
     *
     * @param poseStack        The current {@link PoseStack}.
     * @param arm              The {@link HumanoidArm} value.
     * @param swingProgress    The current arm swing progress.
     * @param equippedProgress The current item equipping progress.
     * @return Whether a classic swing animation was applied.
     */
    public static boolean oldClassicSwing(PoseStack poseStack, HumanoidArm arm, float swingProgress, float equippedProgress)
    {
        boolean isHit = AnimationTweak.OLD_CLASSIC_HIT_SWING.get();
        boolean isPlace = AnimationTweak.OLD_CLASSIC_PLACE_SWING.get();

        if (!isHit && !isPlace)
            return false;

        float flip = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        SwingType swingType = SWING_TYPE.get();

        if (isHit && swingType == SwingType.HIT)
        {
            float x = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float y = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * ((float) Math.PI * 2));
            float z = -0.2F * Mth.sin(swingProgress * (float) Math.PI);

            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(flip * 0.56F, -0.52F + equippedProgress * -0.6F, -0.72F);
            poseStack.translate(flip * x, y, z);

            return true;
        }
        else if (isPlace && swingType == SwingType.PLACE)
        {
            float rotateProgress = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);

            poseStack.popPose();
            poseStack.pushPose();
            poseStack.translate(flip * 0.56F, -0.52F + equippedProgress * -0.6F, -0.72F);
            poseStack.translate(0.0F, rotateProgress * -0.15F, 0.0F);

            return true;
        }

        return false;
    }
}
