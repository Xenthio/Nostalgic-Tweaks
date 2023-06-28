package mod.adrenix.nostalgic.client.config.gui.widget.text;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.adrenix.nostalgic.client.config.gui.widget.element.ElementWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;

/**
 * This class provides text rendering in widget form. Flexible methods provide ease of use when needing to render text
 * to a screen.
 */

public class TextWidget extends ElementWidget
{
    /* Fields */

    private final TextAlign align;
    private final MultiLineLabel label;
    private final int lineHeight;

    /* Constructor */

    /**
     * Create a new text widget instance.
     * @param text The text to render.
     * @param align The alignment of the text.
     * @param startX Where the text starts on the x-axis.
     * @param startY Where the text starts on the y-axis.
     * @param maxWidth The maximum width allowed for the text.
     */
    public TextWidget(Component text, TextAlign align, int startX, int startY, int maxWidth)
    {
        super(startX, startY, 0, 0);

        // Fixes a color continuation on the next line issue when using a multi line label
        Component fixed = Component.literal(text.getString().replaceAll("§r", "§f"));
        Font font = Minecraft.getInstance().font;

        this.label = MultiLineLabel.create(font, fixed, maxWidth);
        this.lineHeight = font.lineHeight + 4;
        this.align = align;

        this.setWidth(maxWidth);
        this.setHeight(this.label.getLineCount() * this.lineHeight);
    }

    /* Getters */

    /**
     * Retrieve the bottom y-position of this text widget. This is done by adding the starting y-position to the height
     * generated by the multi line label.
     *
     * @return The bottom y-position of this widget.
     */
    public int getBottomY() { return this.getY() + this.getHeight(); }

    /* Methods */

    /**
     * Handler method for rendering a text widget.
     * @param poseStack The current pose stack.
     * @param mouseX The x-position of the mouse.
     * @param mouseY The y-position of the mouse.
     * @param partialTick The change in frame time.
     */
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick)
    {
        switch (this.align)
        {
            case LEFT -> this.label.renderLeftAligned(poseStack, this.getX(), this.getY(), this.lineHeight, 0xFFFFFF);
            case CENTER -> this.label.renderCentered(poseStack, this.getX() + (this.getWidth() / 2), this.getY());
        }
    }
}
