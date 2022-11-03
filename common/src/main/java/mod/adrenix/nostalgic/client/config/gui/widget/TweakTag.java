package mod.adrenix.nostalgic.client.config.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.adrenix.nostalgic.NostalgicTweaks;
import mod.adrenix.nostalgic.client.config.annotation.TweakClient;
import mod.adrenix.nostalgic.client.config.gui.widget.button.StatusButton;
import mod.adrenix.nostalgic.client.config.gui.widget.list.ConfigRowList;
import mod.adrenix.nostalgic.common.config.annotation.TweakSide;
import mod.adrenix.nostalgic.common.config.reflect.CommonReflect;
import mod.adrenix.nostalgic.common.config.tweak.GuiTweak;
import mod.adrenix.nostalgic.client.config.gui.screen.config.ConfigScreen;
import mod.adrenix.nostalgic.client.config.reflect.TweakClientCache;
import mod.adrenix.nostalgic.util.common.LangUtil;
import mod.adrenix.nostalgic.util.common.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * There are multiple tags that can be displayed next to a tweak's display name in a configuration row.
 * A row can have a variety of different tags and will always be visible on the screen.
 */

public class TweakTag extends AbstractWidget
{
    /* Horizontal Coordinate Offsets */

    public static final int U_NEW_OFFSET = 66;
    public static final int U_CLIENT_OFFSET = 69;
    public static final int U_SERVER_OFFSET = 72;
    public static final int U_DYNAMIC_OFFSET = 81;
    public static final int U_RELOAD_OFFSET = 75;
    public static final int U_RESTART_OFFSET = 78;
    public static final int U_KEY_OFFSET = 81;
    public static final int U_SYNC_OFFSET = 84;
    public static final int U_WARNING_OFFSET = 87;
    public static final int V_GLOBAL_OFFSET = 0;
    public static final int U_GLOBAL_WIDTH = 1;
    public static final int V_GLOBAL_HEIGHT = 11;
    public static final int TAG_MARGIN = 5;

    /* Widget Fields */

    private String title;
    private boolean render = true;
    private final TweakClientCache<?> cache;
    private final AbstractWidget anchor;
    private final boolean isTooltip;

    /* Constructor */

    public TweakTag(TweakClientCache<?> cache, AbstractWidget anchor, boolean isTooltip)
    {
        super(0, 0, 0, 0, Component.empty());

        this.cache = cache;
        this.anchor = anchor;
        this.isTooltip = isTooltip;
        this.title = Component.translatable(this.cache.getLangKey()).getString();
    }

    /* Helper Methods */

    /**
     * @return Get the title of this tag.
     */
    public String getTitle() { return this.title; }

    /**
     * Set the title of this tag.
     * @param title The new title of this tag.
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Control whether this tag should render.
     * @param state A flag that dictates tag rendering.
     */
    public void setRender(boolean state) { this.render = state; }

    /* Rendering Static Helpers */

    /**
     * Get the width of a tag.
     * @param tag The title of a tag.
     * @param startX Where the title of this tag is starting.
     * @return The ending x-position that includes the starting x-position and the text-width of the tag title.
     */
    private static int getTagWidth(Component tag, int startX)
    {
        return startX + U_GLOBAL_WIDTH + Minecraft.getInstance().font.width(tag) + TAG_MARGIN;
    }

    /**
     * Draws a tag to the screen.
     * @param screen The current screen.
     * @param poseStack The current pose stack.
     * @param x The x-position of where the tag should be drawn.
     * @param y The y-position of where the tag should be drawn.
     * @param uOffset The horizontal texture coordinate offset.
     * @param vOffset The vertical texture coordinate offset.
     * @param render Whether the tag should be rendered.
     */
    private static void draw(Screen screen, PoseStack poseStack, int x, int y, int uOffset, int vOffset, boolean render)
    {
        if (render)
            screen.blit(poseStack, x, y, uOffset, vOffset, U_GLOBAL_WIDTH, V_GLOBAL_HEIGHT);
    }

    /**
     * Renders a complete tag to the screen.
     * @param screen The current screen.
     * @param poseStack The current pose stack.
     * @param tag The tag to render.
     * @param startX The x-position of where the tag should be drawn.
     * @param startY The y-position of where the tag should be drawn.
     * @param uOffset The horizontal texture coordinate offset.
     * @param render Whether the tag should be rendered.
     * @return An x-position of where the next tag should start rendering. This includes the defined tag margin.
     */
    public static int renderTag(Screen screen, PoseStack poseStack, Component tag, int startX, int startY, int uOffset, boolean render)
    {
        RenderSystem.setShaderTexture(0, ModUtil.Resource.WIDGETS_LOCATION);
        Font font = Minecraft.getInstance().font;

        int tagWidth = font.width(tag);
        int endX = getTagWidth(tag, startX);

        TweakTag.draw(screen, poseStack, startX, startY, uOffset, V_GLOBAL_OFFSET, render);

        for (int i = 0; i < tagWidth + TAG_MARGIN; i++)
            TweakTag.draw(screen, poseStack, startX + U_GLOBAL_WIDTH + i, startY, uOffset + 1, 0, render);

        TweakTag.draw(screen, poseStack, endX, startY, uOffset, V_GLOBAL_OFFSET, render);

        font.draw(poseStack, tag, startX + 4, startY + 2, 0xFFFFFF);

        return endX + TAG_MARGIN;
    }

    /**
     * An override method of {@link TweakTag#renderTag(Screen, PoseStack, Component, int, int, int, boolean)} that does
     * not require a rendering state.
     *
     * @param screen The current screen.
     * @param poseStack The current pose stack.
     * @param tag The tag to render.
     * @param startX The x-position of where the tag should be drawn.
     * @param startY The y-position of where the tag should be drawn.
     * @param uOffset The horizontal texture coordinate offset.
     * @return An x-position of where the next tag should start rendering. This includes the defined tag margin.
     */
    public static int renderTag(Screen screen, PoseStack poseStack, Component tag, int startX, int startY, int uOffset)
    {
        return renderTag(screen, poseStack, tag, startX, startY, uOffset, true);
    }

    /**
     * Render a tooltip on the screen if the mouse is over a specific position.
     * @param screen The current screen.
     * @param poseStack The current pose stack.
     * @param title The title component of a tag.
     * @param tooltip The tooltip component to display.
     * @param startX Where the tooltip box starts rendering on the x-axis.
     * @param startY Where the tooltip box starts rendering on the y-axis.
     * @param mouseX Where the mouse currently sits on the x-axis.
     * @param mouseY Where the mouse currently sits on the y-axis.
     */
    public static void renderTooltip(Screen screen, PoseStack poseStack, Component title, Component tooltip, int startX, int startY, int mouseX, int mouseY)
    {
        int endX = getTagWidth(title, startX);
        boolean isMouseOver = (mouseX >= startX && mouseX <= endX) && (mouseY >= startY && mouseY <= startY + V_GLOBAL_HEIGHT);

        if (isMouseOver && screen instanceof ConfigScreen)
            ((ConfigScreen) screen).renderLast.add(() ->
                screen.renderComponentTooltip(poseStack, ModUtil.Wrap.tooltip(tooltip, 38), mouseX, mouseY));
    }

    /**
     * An override method that instructs the screen renderer what to show.
     * @param poseStack The current pose stack.
     * @param mouseX The x-position of the mouse.
     * @param mouseY The y-position of the mouse.
     * @param partialTick The change in frame time.
     */
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;

        if (screen == null) return;

        TweakClient.Gui.New newTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.New.class);
        TweakSide.Client clientTag = CommonReflect.getAnnotation(this.cache, TweakSide.Client.class);
        TweakSide.Server serverTag = CommonReflect.getAnnotation(this.cache, TweakSide.Server.class);
        TweakSide.Dynamic dynamicTag = CommonReflect.getAnnotation(this.cache, TweakSide.Dynamic.class);
        TweakClient.Gui.Alert alertTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.Alert.class);
        TweakClient.Gui.Sodium sodiumTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.Sodium.class);
        TweakClient.Gui.Restart restartTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.Restart.class);
        TweakClient.Gui.Warning warningTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.Warning.class);
        TweakClient.Gui.Optifine optifineTag = CommonReflect.getAnnotation(this.cache, TweakClient.Gui.Optifine.class);
        TweakClient.Run.ReloadResources reloadTag = CommonReflect.getAnnotation(this.cache, TweakClient.Run.ReloadResources.class);

        Component optifineTitle = Component.literal("Optifine");
        Component sodiumTitle = Component.literal("Sodium");

        ChatFormatting flashColor = StatusButton.getFlipState() ? ChatFormatting.GRAY : ChatFormatting.RED;

        Component title = Component.literal(this.title);
        Component newTitle = Component.translatable(LangUtil.Gui.TAG_NEW);
        Component clientTitle = Component.translatable(LangUtil.Gui.TAG_CLIENT);
        Component serverTitle = Component.translatable(LangUtil.Gui.TAG_SERVER);
        Component dynamicTitle = Component.translatable(LangUtil.Gui.TAG_DYNAMIC);
        Component reloadTitle = Component.translatable(LangUtil.Gui.TAG_RELOAD).withStyle(ChatFormatting.ITALIC);
        Component restartTitle = Component.translatable(LangUtil.Gui.TAG_RESTART).withStyle(ChatFormatting.ITALIC);
        Component warningTitle = Component.translatable(LangUtil.Gui.TAG_WARNING).withStyle(flashColor);
        Component alertTitle = Component.translatable(LangUtil.Gui.TAG_ALERT).withStyle(flashColor);

        Component newTooltip = Component.translatable(LangUtil.Gui.TAG_NEW_TOOLTIP);
        Component clientTooltip = Component.translatable(LangUtil.Gui.TAG_CLIENT_TOOLTIP);
        Component serverTooltip = Component.translatable(LangUtil.Gui.TAG_SERVER_TOOLTIP);
        Component dynamicTooltip = Component.translatable(LangUtil.Gui.TAG_DYNAMIC_TOOLTIP);
        Component reloadTooltip = Component.translatable(LangUtil.Gui.TAG_RELOAD_TOOLTIP);
        Component restartTooltip = Component.translatable(LangUtil.Gui.TAG_RESTART_TOOLTIP);
        Component sodiumTooltip = Component.translatable(this.cache.getSodiumKey());
        Component optifineTooltip = Component.translatable(this.cache.getOptifineKey());
        Component warningTooltip = Component.translatable(this.cache.getWarningKey());

        boolean isNewRenderable = (Boolean) TweakClientCache.get(GuiTweak.DISPLAY_NEW_TAGS).getCurrent();
        boolean isSidedRenderable = (Boolean) TweakClientCache.get(GuiTweak.DISPLAY_SIDED_TAGS).getCurrent();
        boolean isTooltipRenderable = (Boolean) TweakClientCache.get(GuiTweak.DISPLAY_TAG_TOOLTIPS).getCurrent();

        int startX = ConfigRowList.getStartX() + minecraft.font.width(title) + (isTooltip ? 20 : 4);
        int startY = this.anchor.y + 4;
        int lastX = startX;

        if (newTag != null && isNewRenderable)
        {
            if (isTooltipRenderable)
                renderTooltip(screen, poseStack, newTitle, newTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, newTitle, lastX, startY, U_NEW_OFFSET, this.render);
        }

        if (clientTag != null && isSidedRenderable)
        {
            if (isTooltipRenderable)
                renderTooltip(screen, poseStack, clientTitle, clientTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, clientTitle, lastX, startY, U_CLIENT_OFFSET, this.render);
        }

        if (serverTag != null && isSidedRenderable)
        {
            if (isTooltipRenderable)
                renderTooltip(screen, poseStack, serverTitle, serverTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, serverTitle, lastX, startY, U_SERVER_OFFSET, this.render);
        }

        if (dynamicTag != null && isSidedRenderable)
        {
            if (isTooltipRenderable)
                renderTooltip(screen, poseStack, dynamicTitle, dynamicTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, dynamicTitle, lastX, startY, U_DYNAMIC_OFFSET, this.render);
        }

        if (reloadTag != null)
        {
            renderTooltip(screen, poseStack, reloadTitle, reloadTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, reloadTitle, lastX, startY, U_RELOAD_OFFSET, this.render);
        }

        if (restartTag != null)
        {
            renderTooltip(screen, poseStack, restartTitle, restartTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, restartTitle, lastX, startY, U_RESTART_OFFSET, this.render);
        }

        if (alertTag != null && alertTag.alert().active())
        {
            Component tooltip = Component.translatable(alertTag.langKey());
            renderTooltip(screen, poseStack, alertTitle, tooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, alertTitle, lastX, startY, U_WARNING_OFFSET, this.render);
        }

        if (warningTag != null)
        {
            renderTooltip(screen, poseStack, warningTitle, warningTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, warningTitle, lastX, startY, U_WARNING_OFFSET, this.render);
        }

        if (sodiumTag != null && NostalgicTweaks.isSodiumInstalled)
        {
            if (sodiumTag.incompatible())
                sodiumTooltip = Component.translatable(LangUtil.Gui.TAG_SODIUM_TOOLTIP);

            renderTooltip(screen, poseStack, sodiumTitle, sodiumTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, sodiumTitle, lastX, startY, U_RESTART_OFFSET, this.render);
        }

        if (optifineTag != null && NostalgicTweaks.OPTIFINE.get())
        {
            if (optifineTag.incompatible())
                optifineTooltip = Component.translatable(LangUtil.Gui.TAG_OPTIFINE_TOOLTIP);

            renderTooltip(screen, poseStack, optifineTitle, optifineTooltip, lastX, startY, mouseX, mouseY);
            lastX = renderTag(screen, poseStack, optifineTitle, lastX, startY, U_RESTART_OFFSET, this.render);
        }

        this.x = startX;
        this.setWidth(lastX - startX);
    }

    /* Required Overrides */

    @Override public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
