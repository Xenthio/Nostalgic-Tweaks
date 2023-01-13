package mod.adrenix.nostalgic.client.screen;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mod.adrenix.nostalgic.common.config.ModConfig;
import mod.adrenix.nostalgic.common.config.tweak.TweakVersion;
import mod.adrenix.nostalgic.mixin.widen.ScreenAccessor;
import mod.adrenix.nostalgic.mixin.widen.TitleScreenAccessor;
import mod.adrenix.nostalgic.util.client.GuiUtil;
import mod.adrenix.nostalgic.util.common.LangUtil;
import mod.adrenix.nostalgic.util.common.TextureLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class overrides the vanilla title screen. The nostalgic title screen can be configured in a variety of ways.
 * The screen can have its button layout, Minecraft logo, and screen background changed. Various corner text options
 * are available as well.
 */

public class NostalgicTitleScreen extends TitleScreen
{
    /* Static Fields */

    /**
     * This field determines whether the game has finished its first loading cycle. When the game first loads, the
     * loading overlay screen slowly fades away to the title screen. This effect overlaps the falling logo animation.
     * Therefore, the nostalgic title screen will delay the animation until the fade away effect has finished.
     */
    public static boolean isGameReady = false;

    /* Fields */

    private static final String[] MINECRAFT = {
        " *   * * *   * *** *** *** *** *** ***",
        " ** ** * **  * *   *   * * * * *    * ",
        " * * * * * * * **  *   **  *** **   * ",
        " *   * * *  ** *   *   * * * * *    * ",
        " *   * * *   * *** *** * * * * *    * "
    };

    /**
     * This field delays the falling logo animation.
     * Once the loading overlay fade away effect has finished, the falling logo animation can begin.
     */
    private long updateScreenDelay;

    /**
     * This field, when <code>true</code>, will change the title to M I N C E R A F T. This Easter egg will be applied
     * to the vanilla logo and falling logo animation.
     */
    private final boolean isEasterEgged;

    /**
     * This two-dimensional array holds falling block data.
     * The array is set up in [x][y] format.
     */
    private LogoEffectRandomizer[][] logoEffects;

    /* Widget Data */

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final ResourceLocation OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");

    /**
     * This panorama is used when the user wishes to use a more modern title screen display.
     * The vanilla renderer is still used since that has not changed since its original debut.
     */
    private final PanoramaRenderer panorama = new PanoramaRenderer(TitleScreen.CUBE_MAP);

    /* Random Source & Button Layouts */

    private final RandomSource random = RandomSource.create();
    private final List<Widget> alpha = new ArrayList<>();
    private final List<Widget> beta = new ArrayList<>();
    private final List<Widget> release = new ArrayList<>();

    /* Constructor */

    /**
     * Create a new nostalgic title screen instance.
     * Easter egg creation is done here if the user has won the lottery.
     */
    public NostalgicTitleScreen()
    {
        this.isEasterEgged = random.nextFloat() < 1.0E-4;
        this.updateScreenDelay = 0L;

        if (this.isEasterEgged)
            MINECRAFT[2] = " * * * * * * * *   **  **  *** **   * ";
        else
            MINECRAFT[2] = " * * * * * * * **  *   **  *** **   * ";
    }

    /* Overrides */

    /**
     * Handler method for screen initialization.
     */
    @Override
    protected void init()
    {
        int x = this.width / 2 - 100;
        int y = this.height / 4 + 48;
        int rowHeight = 24;

        this.alpha.clear();
        this.beta.clear();
        this.release.clear();

        this.createAlphaOptions(x, y, rowHeight);
        this.createBetaOptions(x, y, rowHeight);
        this.createReleaseOptions(x, y, rowHeight);

        List<Widget> widgets = switch (ModConfig.Candy.getButtonLayout())
        {
            case ALPHA -> this.alpha;
            case BETA -> this.beta;
            default -> this.release;
        };

        if (ModConfig.Candy.getButtonLayout() != TweakVersion.TitleLayout.MODERN)
            widgets.forEach((widget) -> super.addRenderableWidget((AbstractWidget) widget));

        super.init();
    }

    /**
     * Handler method for when a key is pressed.
     * @param keyCode The pressed key code.
     * @param scanCode A key scancode.
     * @param modifiers Any held modifiers.
     * @return Whether this method handled the event.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (this.minecraft == null)
            return false;
        else if (keyCode == GLFW.GLFW_KEY_M)
            this.minecraft.setScreen(new NostalgicTitleScreen());

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /**
     * Handler method for when the mouse is clicked.
     * @param mouseX The current x-position of the mouse.
     * @param mouseY The current y-position of the mouse.
     * @param button The mouse button that was clicked.
     * @return Whether this method handled the event.
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        return switch (ModConfig.Candy.getButtonLayout())
        {
            case MODERN -> super.mouseClicked(mouseX, mouseY, button);
            case ALPHA -> this.getClicked(this.alpha, mouseX, mouseY, button);
            case BETA -> this.getClicked(this.beta, mouseX, mouseY, button);
            case RELEASE_TEXTURE_PACK, RELEASE_NO_TEXTURE_PACK -> this.getClicked(this.release, mouseX, mouseY, button);
        };
    }

    /**
     * Handler method that provides instructions for rendering this screen.
     * @param poseStack The current pose stack.
     * @param mouseX The current x-position of the mouse.
     * @param mouseY The current y-position of the mouse.
     * @param partialTick The change in game frame time.
     */
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        if (ModConfig.Candy.oldTitleBackground())
            this.renderDirtBackground(0);
        else
        {
            this.panorama.render(partialTick, 1.0F);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, OVERLAY);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            TitleScreen.blit(poseStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
        }

        if (this.updateScreenDelay == 0L)
            this.updateScreenDelay = Util.getMillis();

        boolean isModern = ModConfig.Candy.getLoadingOverlay() == TweakVersion.Overlay.MODERN;
        boolean isDelayed = !NostalgicTitleScreen.isGameReady && Util.getMillis() - this.updateScreenDelay < 1200;

        if (this.minecraft == null || (isModern && isDelayed))
            return;

        if (ModConfig.Candy.oldAlphaLogo())
            this.renderClassicLogo(partialTick);
        else
        {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, TextureLocation.MINECRAFT_LOGO);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            int width = this.width / 2 - 137;
            int height = 30;

            if (ModConfig.Candy.oldLogoOutline())
            {
                if (this.isEasterEgged)
                {
                    this.blit(poseStack, width, height, 0, 0, 99, 44);
                    this.blit(poseStack, width + 99, height, 129, 0, 27, 44);
                    this.blit(poseStack, width + 99 + 26, height, 126, 0, 3, 44);
                    this.blit(poseStack, width + 99 + 26 + 3, height, 99, 0, 26, 44);
                    this.blit(poseStack, width + 155, height, 0, 45, 155, 44);
                }
                else
                {
                    this.blit(poseStack, width, height, 0, 0, 155, 44);
                    this.blit(poseStack, width + 155, height, 0, 45, 155, 44);
                }
            }
            else
            {
                if (this.isEasterEgged)
                {
                    this.blitOutlineBlack(width, height, (x, y) -> {
                        this.blit(poseStack, x, y, 0, 0, 99, 44);
                        this.blit(poseStack, x + 99, y, 129, 0, 27, 44);
                        this.blit(poseStack, x + 99 + 26, y, 126, 0, 3, 44);
                        this.blit(poseStack, x + 99 + 26 + 3, y, 99, 0, 26, 44);
                        this.blit(poseStack, x + 155, y, 0, 45, 155, 44);
                    });
                }
                else
                {
                    this.blitOutlineBlack(width, height, (x, y) -> {
                        this.blit(poseStack, x, y, 0, 0, 155, 44);
                        this.blit(poseStack, x + 155, y, 0, 45, 155, 44);
                    });
                }
            }
        }

        NostalgicTitleScreen.isGameReady = true;

        TweakVersion.TitleLayout layout = ModConfig.Candy.getButtonLayout();
        TitleScreenAccessor titleAccessor = (TitleScreenAccessor) this;
        ScreenAccessor screenAccessor = (ScreenAccessor) this;

        int color = Mth.ceil(255.0F) << 24;

        if (titleAccessor.NT$getSplash() != null)
        {
            poseStack.pushPose();
            poseStack.translate((float) this.width / 2 + 90, 70.0, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));

            float scale = 1.8F - Mth.abs(Mth.sin((float) (Util.getMillis() % 1000L) / 1000.0F * ((float) Math.PI * 2)) * 0.1F);
            scale = scale * 100.0F / (float) (this.font.width(titleAccessor.NT$getSplash()) + 32);

            poseStack.scale(scale, scale, scale);
            TitleScreen.drawCenteredString(poseStack, this.font, titleAccessor.NT$getSplash(), 0, -8, 0xFFFF00 | color);
            poseStack.popPose();
        }

        String minecraft = ModConfig.Candy.getVersionText();

        Component copyright = switch (layout)
        {
            case ALPHA -> Component.translatable(LangUtil.Gui.CANDY_TITLE_COPYRIGHT_ALPHA);
            case BETA -> Component.translatable(LangUtil.Gui.CANDY_TITLE_COPYRIGHT_BETA);
            default -> COPYRIGHT_TEXT;
        };

        if (Minecraft.checkModStatus().shouldReportAsModified() && !ModConfig.Candy.removeTitleModLoaderText())
            minecraft = minecraft + "/" + this.minecraft.getVersionType() + I18n.get("menu.modded");

        int versionColor = ModConfig.Candy.oldTitleBackground() && !minecraft.contains("§") ? 5263440 : 0xFFFFFF;
        int height = ModConfig.Candy.titleBottomLeftText() ? this.height - 10 : 2;

        TitleScreen.drawString(poseStack, this.font, minecraft, 2, height, versionColor);
        TitleScreen.drawString(poseStack, this.font, copyright, this.width - this.font.width(copyright) - 2, this.height - 10, 0xFFFFFF);

        boolean isRelease = layout == TweakVersion.TitleLayout.RELEASE_TEXTURE_PACK || layout == TweakVersion.TitleLayout.RELEASE_NO_TEXTURE_PACK;

        this.setLayoutVisibility(screenAccessor.NT$getRenderables(), layout == TweakVersion.TitleLayout.MODERN);
        this.setLayoutVisibility(this.alpha, layout == TweakVersion.TitleLayout.ALPHA);
        this.setLayoutVisibility(this.beta, layout == TweakVersion.TitleLayout.BETA);
        this.setLayoutVisibility(this.release, isRelease);

        switch (layout)
        {
            case MODERN ->
            {
                for (GuiEventListener child : this.children())
                {
                    if (child instanceof AbstractWidget)
                        ((AbstractWidget) child).setAlpha(1.0F);
                }

                this.setButtonVisibility();

                for (Widget widget : screenAccessor.NT$getRenderables())
                    widget.render(poseStack, mouseX, mouseY, partialTick);

                if (titleAccessor.NT$getRealmsNotificationsEnabled())
                    titleAccessor.NT$getRealmsNotificationsScreen().render(poseStack, mouseX, mouseY, partialTick);
            }

            case ALPHA -> this.alpha.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
            case BETA -> this.beta.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));

            default -> this.release.forEach(widget -> widget.render(poseStack, mouseX, mouseY, partialTick));
        }
    }

    /* Methods */

    /*
       Nostalgic Buttons

       The following methods define the widget layout for the title screen.
       Helper methods are also defined for widget visibility and on-press instructions.
     */

    /**
     * Change the visibility of buttons. Some tweaks will remove vanilla buttons from the modern title screen.
     * This acts as a compatibility layer for mods that change the modern title screen.
     */
    private void setButtonVisibility()
    {
        ScreenAccessor screen = (ScreenAccessor) this;

        for (Widget widget : screen.NT$getRenderables())
        {
            if (widget instanceof ImageButton && ((ImageButton) widget).x == this.width / 2 - 124)
                ((ImageButton) widget).visible = !ModConfig.Candy.removeLanguageButton();
            else if (widget instanceof ImageButton && ((ImageButton) widget).x == this.width / 2 + 104)
                ((ImageButton) widget).visible = !ModConfig.Candy.removeAccessibilityButton();
            else if (widget instanceof Button button)
            {
                boolean isRealms = button.getMessage().getString().equals(Component.translatable("menu.online").getString());
                boolean isRemovable = ModConfig.Candy.removeRealmsButton();
                ((Button) widget).visible = !isRealms || !isRemovable;
            }
        }
    }

    /**
     * Change the visibility of widgets.
     * @param widgets A list of widgets.
     * @param visible A visibility boolean flag.
     */
    private void setLayoutVisibility(List<Widget> widgets, boolean visible)
    {
        for (Widget widget : widgets)
        {
            if (widget instanceof AbstractWidget)
                ((AbstractWidget) widget).visible = visible;
        }
    }

    /**
     * Check if a widget was clicked.
     * @param widgets A list of widgets to check.
     * @param mouseX The current x-position of the mouse.
     * @param mouseY The current x-position of the mouse.
     * @param button The mouse button that was clicked.
     * @return Whether a widget was clicked.
     */
    private boolean getClicked(List<Widget> widgets, double mouseX, double mouseY, int button)
    {
        boolean isClicked = false;

        for (Widget widget : widgets)
        {
            if (widget instanceof AbstractWidget)
                isClicked = ((AbstractWidget) widget).mouseClicked(mouseX, mouseY, button);

            if (isClicked)
                break;
        }

        return isClicked;
    }

    /* Button Press Instructions */

    /**
     * Instructions that goes to the vanilla select world screen.
     * @param ignored The button instance is not used.
     */
    private void onSingleplayer(Button ignored)
    {
        if (this.minecraft != null)
            this.minecraft.setScreen(new SelectWorldScreen(this));
    }

    /**
     * Instructions that goes to the vanilla join multiplayer screen.
     * @param ignored The button instance is not used.
     */
    private void onMultiplayer(Button ignored)
    {
        if (this.minecraft != null)
            this.minecraft.setScreen(new JoinMultiplayerScreen(this));
    }

    /**
     * Instructions that goes to the vanilla options screen.
     * @param ignored The button instance is not used.
     */
    private void onOptions(Button ignored)
    {
        if (this.minecraft != null)
            this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
    }

    /**
     * Instructions that opens a mod loader "mods" screen.
     * @param ignored The button instance is not used.
     */
    private void onMods(Button ignored)
    {
        if (this.minecraft != null && GuiUtil.modScreen != null)
            this.minecraft.setScreen(GuiUtil.modScreen.apply(this.minecraft.screen));
    }

    /**
     * Updates the resource pack repository list.
     * @param repository A pack repository instance.
     */
    private void updatePackList(PackRepository repository)
    {
        if (this.minecraft == null)
            return;

        Options options = this.minecraft.options;
        ImmutableList<String> before = ImmutableList.copyOf(options.resourcePacks);

        options.resourcePacks.clear();
        options.incompatibleResourcePacks.clear();

        for (Pack pack : repository.getSelectedPacks())
        {
            if (pack.isFixedPosition())
                continue;

            options.resourcePacks.add(pack.getId());

            if (pack.getCompatibility().isCompatible())
                continue;

            options.incompatibleResourcePacks.add(pack.getId());
        }

        options.save();

        ImmutableList<String> after = ImmutableList.copyOf(options.resourcePacks);

        if (!after.equals(before))
            this.minecraft.reloadResourcePacks();
    }

    /**
     * Instructions that opens the vanilla resource pack selection screen.
     * @param ignored The button instance is not used.
     */
    private void onResources(Button ignored)
    {
        if (this.minecraft != null)
            this.minecraft.setScreen(new PackSelectionScreen(this, this.minecraft.getResourcePackRepository(), this::updatePackList, this.minecraft.getResourcePackDirectory(), Component.translatable("resourcePack.title")));
    }

    /* Button Layouts */

    private void createAlphaOptions(int x, int y, int rowHeight)
    {
        int row = 1;

        // Singleplayer
        this.alpha.add(new Button(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_SINGLEPLAYER), this::onSingleplayer));

        // Multiplayer
        this.alpha.add(new Button(x, y + rowHeight, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_MULTIPLAYER), this::onMultiplayer));

        // Mods
        if (ModConfig.Candy.includeModsOnTitle() && GuiUtil.modScreen != null)
            this.alpha.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Gui.CANDY_TITLE_MODS), this::onMods));

        // Tutorial
        Button tutorial = new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), (button) -> {});
        tutorial.active = false;
        tutorial.setMessage(Component.translatable(LangUtil.Gui.CANDY_TITLE_TUTORIAL).withStyle(ChatFormatting.GRAY));

        this.alpha.add(tutorial);

        // Options
        this.alpha.add(new Button(x, y + rowHeight * (++row + 1) - 12, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_OPTIONS), this::onOptions));
    }

    private void createBetaOptions(int x, int y, int rowHeight)
    {
        int row = 1;

        // Singleplayer
        this.beta.add(new Button(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_SINGLEPLAYER), this::onSingleplayer));

        // Multiplayer
        this.beta.add(new Button(x, y + rowHeight, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_MULTIPLAYER), this::onMultiplayer));

        // Mods & Texture Packs
        boolean isMods = ModConfig.Candy.includeModsOnTitle() && GuiUtil.modScreen != null;
        if (isMods)
            this.beta.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Gui.CANDY_TITLE_MODS), this::onMods));

        this.beta.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(isMods ? LangUtil.Gui.CANDY_TITLE_TEXTURE_PACK : LangUtil.Gui.CANDY_TITLE_MODS_TEXTURE), this::onResources));

        // Options
        this.beta.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_OPTIONS), this::onOptions));
    }

    private void createReleaseOptions(int x, int y, int rowHeight)
    {
        int row = 1;

        // Singleplayer
        this.release.add(new Button(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_SINGLEPLAYER), this::onSingleplayer));

        // Multiplayer
        this.release.add(new Button(x, y + rowHeight, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Vanilla.MENU_MULTIPLAYER), this::onMultiplayer));

        // Mods
        boolean isMods = ModConfig.Candy.includeModsOnTitle() && GuiUtil.modScreen != null;
        if (isMods)
            this.release.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Gui.CANDY_TITLE_MODS), this::onMods));

        // Texture Packs
        if (ModConfig.Candy.getButtonLayout() == TweakVersion.TitleLayout.RELEASE_TEXTURE_PACK)
            this.release.add(new Button(x, y + rowHeight * ++row, BUTTON_WIDTH, BUTTON_HEIGHT, Component.translatable(LangUtil.Gui.CANDY_TITLE_TEXTURE_PACK), this::onResources));

        int lastRow = (this.height / 4 + 48) + 72 + 12;
        if (this.release.size() == 4)
            lastRow += 24;

        // Language
        if (this.minecraft != null && !ModConfig.Candy.removeLanguageButton())
            this.release.add(new ImageButton(this.width / 2 - 124, lastRow, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), Component.translatable("narrator.button.language")));

        // Options
        this.release.add(new Button(this.width / 2 - 100, lastRow, 98, 20, Component.translatable(LangUtil.Vanilla.MENU_OPTIONS), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));

        // Quit
        this.release.add(new Button(this.width / 2 + 2, lastRow, 98, 20, Component.translatable(LangUtil.Vanilla.MENU_QUIT), button -> this.minecraft.stop()));
    }

    /* Classic Logo */

    /**
     * Instructions for rendering the classic logo and the introduction falling animation.
     * @param partialTick The change in game frame time.
     */
    private void renderClassicLogo(float partialTick)
    {
        if (this.minecraft == null)
            return;

        if (this.logoEffects == null)
        {
            this.logoEffects = new LogoEffectRandomizer[MINECRAFT[0].length()][MINECRAFT.length];

            for (int x = 0; x < this.logoEffects.length; x++)
                for (int y = 0; y < this.logoEffects[x].length; y++)
                    logoEffects[x][y] = new LogoEffectRandomizer(x, y);
        }

        for (LogoEffectRandomizer[] logoEffect : this.logoEffects)
            for (LogoEffectRandomizer logoEffectRandomizer : logoEffect)
                logoEffectRandomizer.update(partialTick);

        Window window = this.minecraft.getWindow();
        int scaleHeight = (int) (120 * window.getGuiScale());

        RenderSystem.setProjectionMatrix(Matrix4f.perspective(70.0D, window.getWidth() / (float) scaleHeight, 0.05F, 100.0F));
        RenderSystem.viewport(0, window.getHeight() - scaleHeight, window.getWidth(), scaleHeight);

        PoseStack model = RenderSystem.getModelViewStack();
        model.translate(-0.05F, 1.0F, 1987.0F);
        model.scale(1.59F, 1.59F, 1.59F);

        BakedModel stone = this.itemRenderer.getItemModelShaper().getItemModel(Blocks.STONE.asItem());

        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);

        for (int pass = 0; pass < 3; pass++)
        {
            model.pushPose();

            if (pass == 0)
            {
                RenderSystem.clear(256, Minecraft.ON_OSX);
                model.translate(0.0F, -0.4F, 0.0F);
                model.scale(0.98F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
            }

            if (pass == 1)
            {
                RenderSystem.disableBlend();
                RenderSystem.clear(256, Minecraft.ON_OSX);
            }

            if (pass == 2)
            {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(768, 1);
            }

            model.scale(1.0F, -1.0F, 1.0F);
            model.mulPose(Vector3f.XP.rotationDegrees(15.0F));
            model.scale(0.89F, 1.0F, 0.4F);
            model.translate((float) (-MINECRAFT[0].length()) * 0.5F, (float) (-MINECRAFT.length) * 0.5F, 0.0F);

            if (pass == 0)
            {
                RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
                RenderSystem.setShaderTexture(0, TextureLocation.BLOCK_SHADOW);
            }
            else
            {
                RenderSystem.setShader(GameRenderer::getBlockShader);
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
            }

            for (int y = 0; y < MINECRAFT.length; y++)
            {
                for (int x = 0; x < MINECRAFT[y].length(); x++)
                {
                    if (MINECRAFT[y].charAt(x) == ' ')
                        continue;

                    model.pushPose();

                    float z = logoEffects[x][y].position;
                    float scale = 1.0F;
                    float alpha = 1.0F;

                    if (pass == 0)
                    {
                        scale = z * 0.04F + 1.0F;
                        alpha = 1.0F / scale;
                        z = 0.0F;
                    }

                    model.translate(x, y, z);
                    model.scale(scale, scale, scale);
                    renderBlock(model, stone, pass, alpha);
                    model.popPose();
                }
            }

            model.popPose();
        }

        RenderSystem.disableBlend();
        RenderSystem.setProjectionMatrix(Matrix4f.orthographic(0.0F, (float) window.getGuiScaledWidth(), 0.0F, (float) window.getGuiScaledHeight(), 1000.0F, 3000.0F));
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());
        model.setIdentity();
        model.translate(0, 0, -2000);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.enableCull();
    }

    /**
     * Get a packed RGBA integer.
     * @param red R
     * @param green G
     * @param blue B
     * @param alpha A
     * @return Packed RGBA integer.
     */
    private int getColorFromRGBA(float red, float green, float blue, float alpha)
    {
        return (int) (alpha * 255.0F) << 24 | (int) (red * 255.0F) << 16 | (int) (green * 255.0F) << 8 | (int) (blue * 255.0F);
    }

    /**
     * Get a grayscale packed RGBA integer from a brightness and alpha value.
     * @param brightness Brightness
     * @param alpha Transparency
     * @return A packed grayscale RGBA integer.
     */
    private int getColorFromBrightness(float brightness, float alpha)
    {
        return this.getColorFromRGBA(brightness, brightness, brightness, alpha);
    }

    /**
     * Quad rendering instructions that allow for transparency.
     * @param modelPose Model position matrix.
     * @param builder Buffer builder instance.
     * @param quad A baked quad.
     * @param brightness The brightness of the quad.
     * @param alpha The transparency of the quad.
     */
    private void renderQuad(PoseStack.Pose modelPose, BufferBuilder builder, BakedQuad quad, float brightness, float alpha)
    {
        int combinedLight = this.getColorFromBrightness(brightness, alpha);
        int[] vertices = quad.getVertices();
        Vec3i vec = quad.getDirection().getNormal();
        Vector3f vec3f = new Vector3f(vec.getX(), vec.getY(), vec.getZ());
        Matrix4f matrix = modelPose.pose();
        vec3f.transform(modelPose.normal());

        try (MemoryStack memoryStack = MemoryStack.stackPush())
        {
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for (int i = 0; i < vertices.length / 8; i++)
            {
                intBuffer.clear();
                intBuffer.put(vertices, i * 8, 8);
                float x = byteBuffer.getFloat(0);
                float y = byteBuffer.getFloat(4);
                float z = byteBuffer.getFloat(8);

                Vector4f vec4f = new Vector4f(x, y, z, 1.0F);
                vec4f.transform(matrix);

                builder.vertex(vec4f.x(), vec4f.y(), vec4f.z(), 1.0F, 1.0F, 1.0F, alpha, byteBuffer.getFloat(16), byteBuffer.getFloat(20), OverlayTexture.NO_OVERLAY, combinedLight, vec3f.x(), vec3f.y(), vec3f.z());
            }
        }
    }

    /**
     * Render a block to the classic title screen.
     * @param modelView Model view matrix.
     * @param stone A stone block model.
     * @param pass The rendering pass index.
     * @param alpha A transparency value.
     */
    private void renderBlock(PoseStack modelView, BakedModel stone, int pass, float alpha)
    {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        for (Direction direction : Direction.values())
        {
            float brightness = switch (direction)
            {
                case DOWN -> 1.0F;
                case UP -> 0.5F;
                case NORTH -> 0.0F;
                case SOUTH -> 0.8F;
                case WEST, EAST -> 0.6F;
            };

            int color = this.getColorFromBrightness(brightness, alpha);

            for (BakedQuad quad : stone.getQuads(null, direction, random))
            {
                if (pass == 0)
                    renderQuad(modelView.last(), builder, quad, brightness, alpha);
                else
                    builder.putBulkData(modelView.last(), quad, brightness, brightness, brightness, color, OverlayTexture.NO_OVERLAY);
            }
        }

        tesselator.end();
    }

    /* Logo Effect Randomizer */

    /**
     * This class tracks individual blocks for the falling animation.
     * Updates of position values are handled by the screen renderer.
     */

    private static class LogoEffectRandomizer
    {
        /* Fields */

        public float position;
        public float speed;

        /* Constructor */

        /**
         * Create a new logo effect randomizer instance.
         * @param x The starting x-position.
         * @param y The starting y-position.
         */
        public LogoEffectRandomizer(int x, int y)
        {
            this.position = (10 + y) + RandomSource.create().nextFloat() * 32.0F + x;
        }

        /**
         * Update the position of this randomizer instance.
         * @param partialTick The change in game frame time.
         */
        public void update(float partialTick)
        {
            if (this.position > 0.0F)
                this.speed -= 0.4F;

            this.position += this.speed * partialTick;
            this.speed *= 0.9F;

            if (this.position < 0.0F)
            {
                this.position = 0.0F;
                this.speed = 0.0F;
            }
        }
    }
}
