package mod.adrenix.nostalgic.mixin.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.adrenix.nostalgic.common.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen
{
    /* Shadows */

    @Shadow protected EditBox input;

    /* Dummy Constructor */

    private ChatScreenMixin(Component ignored) { super(ignored); }

    /**
     * Moves the input position further to the right to account for the new '>' symbol.
     * Controlled by the old chat input tweak.
     */
    @Inject(method = "init", at = @At("RETURN"))
    private void NT$onInitInput(CallbackInfo callback)
    {
        this.input.setX(ModConfig.Candy.oldChatInput() ? 12 : 4);
        this.input.setWidth(ModConfig.Candy.oldChatInput() ? this.width - 21 : this.width - 4);
        this.input.setTextColor(0xFFFFFF);
    }

    /**
     * Adds a '>' to the beginning of the chat input box.
     * Controlled by the old chat input tweak.
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void NT$onRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick, CallbackInfo callback)
    {
        if (!ModConfig.Candy.oldChatInput())
            return;

        ChatScreen.drawString(poseStack, Minecraft.getInstance().font, ">", 4, this.height - 12, 0xFFFFFF);
    }

    /**
     * Prevents the colored chat signature rectangle from rendering.
     * Controlled by the disable chat signature boxes tweak.
     */
    @Redirect(method = "render", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/client/gui/screens/ChatScreen;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V"))
    private void NT$onRenderSigned(PoseStack poseStack, int minX, int minY, int maxX, int maxY, int color)
    {
        if (ModConfig.Candy.disableSignatureBoxes())
            return;

        ChatScreen.fill(poseStack, minX, minY, maxX, maxY, color);
    }
}
