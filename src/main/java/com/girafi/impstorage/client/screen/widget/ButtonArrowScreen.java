package com.girafi.impstorage.client.screen.widget;

import com.girafi.impstorage.lib.ModInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class ButtonArrowScreen extends ExtendedButton {
    private static final ResourceLocation BUTTON_ARROWS = new ResourceLocation(ModInfo.ID, "textures/gui/arrows.png");

    public static final int ARROW_DOWN = 0;
    public static final int ARROW_UP = 1;
    public static final int ARROW_LEFT = 2;
    public static final int ARROW_RIGHT = 3;

    private static final int ARROW_WIDTH = 11;
    private static final int ARROW_HEIGHT = 7;

    private final int arrowType;

    public ButtonArrowScreen(int xPos, int yPos, int width, int height, int arrowType, OnPress onPress) {
        super(xPos, yPos, width, height, Component.empty(), onPress);

        this.arrowType = arrowType;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int k = !this.active ? 0 : (this.isHoveredOrFocused() ? 2 : 1);
        guiGraphics.blitWithBorder(BUTTON_ARROWS, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int halfW = ARROW_WIDTH / 2;
        int halfH = ARROW_HEIGHT / 2;

        int drawX;
        int drawY;
        int drawW;
        int drawH;

        int drawU;
        int drawV;

        switch (arrowType) {
            case ARROW_UP: {
                drawX = centerX - halfW;
                drawY = centerY - halfH;
                drawW = ARROW_WIDTH;
                drawH = ARROW_HEIGHT;
                drawU = 22 + (isHovered() ? 11 : 0);
                drawV = 0;
                break;
            }

            case ARROW_DOWN: {
                drawX = centerX - halfW;
                drawY = centerY - halfH;
                drawW = ARROW_WIDTH;
                drawH = ARROW_HEIGHT;
                drawU = (isHovered() ? 11 : 0);
                drawV = 0;
                break;
            }

            case ARROW_LEFT: {
                drawX = centerX - halfH;
                drawY = centerY - halfW;
                drawW = ARROW_HEIGHT;
                drawH = ARROW_WIDTH;
                drawU = 0;
                drawV = (isHoveredOrFocused() ? 7 : 0);
                break;
            }

            case ARROW_RIGHT:
            default: {
                drawX = centerX - halfH;
                drawY = centerY - halfW;
                drawW = ARROW_HEIGHT;
                drawH = ARROW_WIDTH;
                drawU = 0;
                drawV = 14 + (isHovered() ? 7 : 0);
                break;
            }
        }

        guiGraphics.blit(new ResourceLocation("textures/gui/widgets.png"), this.getX() + drawX, this.getY() + drawY, drawU, drawV, drawW, drawH); //TODO Test if resourcelocation renders correctly. Wasn't set before.
    }
}