package com.girafi.impstorage.client.screen;

import com.girafi.impstorage.block.tile.ControllerBlockEntity;
import com.girafi.impstorage.client.screen.widget.ButtonArrowScreen;
import com.girafi.impstorage.lib.ImpracticalConfig;
import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.lib.data.SortingType;
import com.girafi.impstorage.network.PacketHandler;
import com.girafi.impstorage.network.packet.SControllerConfig;
import com.google.common.base.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.io.IOException;

public class ControllerScreen extends Screen {
    private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(ModInfo.ID, "textures/gui/controller.png");

    private static final Predicate<String> NUMBER_VALIDATOR = s -> {
        for (char c : s.toCharArray()) {
            if (!Character.isDigit(c) && c != '-') {
                return false;
            }
        }
        return true;
    };

    private static final String TEXT_SHOW_BOUNDS = "gui.text.bounds.show";
    private static final String TEXT_HIDE_BOUNDS = "gui.text.bounds.hide";
    private static final String TEXT_SORTING_TYPE = "gui.text.sort_type.";

    private static final int BUTTON_BOUND_X_UP = 0;
    private static final int BUTTON_BOUND_X_DOWN = 1;
    private static final int BUTTON_BOUND_Y_UP = 2;
    private static final int BUTTON_BOUND_Y_DOWN = 3;
    private static final int BUTTON_BOUND_Z_UP = 4;
    private static final int BUTTON_BOUND_Z_DOWN = 5;

    private static final int BUTTON_OFFSET_X_UP = 6;
    private static final int BUTTON_OFFSET_X_DOWN = 7;
    private static final int BUTTON_OFFSET_Y_UP = 8;
    private static final int BUTTON_OFFSET_Y_DOWN = 9;
    private static final int BUTTON_OFFSET_Z_UP = 10;
    private static final int BUTTON_OFFSET_Z_DOWN = 11;

    private static final int BUTTON_TOGGLE_BOUNDS = 12;
    private static final int BUTTON_SORTING_TYPE = 13;

    private static final int GUI_WIDTH = 117;
    private static final int GUI_HEIGHT = 209;

    private int guiLeft;
    private int guiTop;

    private int x;
    private int y;
    private int z;

    private int offX;
    private int offY;
    private int offZ;

    private boolean showBounds;
    private SortingType sortingType;

    private boolean isInventoryEmpty;

    private ExtendedButton buttonShowBounds;
    private ExtendedButton buttonSortType;

    private GuiTextField boundX;
    private GuiTextField boundY;
    private GuiTextField boundZ;

    private GuiTextField offsetX;
    private GuiTextField offsetY;
    private GuiTextField offsetZ;

    private ControllerBlockEntity tile;

    public ControllerScreen(ControllerBlockEntity tile) {
        super();
        this.tile = tile;

        this.x = tile.rawX;
        this.y = tile.rawY;
        this.z = tile.rawZ;
        this.offX = tile.offset.getX();
        this.offY = tile.offset.getY();
        this.offZ = tile.offset.getZ();

        this.showBounds = tile.showBounds;
        this.sortingType = tile.sortingType;

        this.isInventoryEmpty = tile.isEmpty;
    }

    @Override
    public void init() {
        super.init();

        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        addButton(buttonShowBounds = new ExtendedButton(BUTTON_TOGGLE_BOUNDS, guiLeft + 8, guiTop + 164, 101, 16,
                I18n.translateToLocal(showBounds ? TEXT_HIDE_BOUNDS : TEXT_SHOW_BOUNDS)));

        addButton(buttonSortType = new ExtendedButton(BUTTON_SORTING_TYPE, guiLeft + 8, guiTop + 184, 101, 16,
                I18n.translateToLocal(TEXT_SORTING_TYPE + sortingType.getUnlocalizedName())));

        addButton(new ButtonArrowScreen(BUTTON_BOUND_X_UP, guiLeft + 8, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_BOUND_X_DOWN, guiLeft + 8, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN));
        addButton(new ButtonArrowScreen(BUTTON_BOUND_Y_UP, guiLeft + 43, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_BOUND_Y_DOWN, guiLeft + 43, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN));
        addButton(new ButtonArrowScreen(BUTTON_BOUND_Z_UP, guiLeft + 78, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_BOUND_Z_DOWN, guiLeft + 78, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_X_UP, guiLeft + 8, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_X_DOWN, guiLeft + 8, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_Y_UP, guiLeft + 43, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_Y_DOWN, guiLeft + 43, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_Z_UP, guiLeft + 78, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP));
        addButton(new ButtonArrowScreen(BUTTON_OFFSET_Z_DOWN, guiLeft + 78, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN));

        buttonList.stream().filter((b) -> b instanceof ButtonArrowScreen).forEach((b) -> b.enabled = this.isInventoryEmpty);

        boundX = new GuiTextField(0, fontRenderer, guiLeft + 9, guiTop + 40, 29, 15);
        boundX.setText(Integer.toString(x));
        boundX.setValidator(NUMBER_VALIDATOR);

        boundY = new GuiTextField(1, fontRenderer, guiLeft + 44, guiTop + 40, 29, 15);
        boundY.setText(Integer.toString(y));
        boundY.setValidator(NUMBER_VALIDATOR);

        boundZ = new GuiTextField(2, fontRenderer, guiLeft + 79, guiTop + 40, 29, 15);
        boundZ.setText(Integer.toString(z));
        boundZ.setValidator(NUMBER_VALIDATOR);

        offsetX = new GuiTextField(3, fontRenderer, guiLeft + 9, guiTop + 111, 29, 15);
        offsetX.setText(Integer.toString(offX));
        offsetX.setValidator(NUMBER_VALIDATOR);

        offsetY = new GuiTextField(4, fontRenderer, guiLeft + 44, guiTop + 111, 29, 15);
        offsetY.setText(Integer.toString(offY));
        offsetY.setValidator(NUMBER_VALIDATOR);

        offsetZ = new GuiTextField(5, fontRenderer, guiLeft + 79, guiTop + 111, 29, 15);
        offsetZ.setText(Integer.toString(offZ));
        offsetZ.setValidator(NUMBER_VALIDATOR);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_RETURN) {
            if (boundX.isFocused() || boundY.isFocused() || boundZ.isFocused() || offsetX.isFocused() || offsetY.isFocused() || offsetZ.isFocused()) {
                String sx = boundX.getText();
                String sy = boundY.getText();
                String sz = boundZ.getText();
                String sox = offsetX.getText();
                String soy = offsetY.getText();
                String soz = offsetZ.getText();

                int nx = sx.isEmpty() ? x : Integer.parseInt(sx);
                int ny = sy.isEmpty() ? y : Integer.parseInt(sy);
                int nz = sz.isEmpty() ? z : Integer.parseInt(sz);

                int ox = sox.isEmpty() ? offX : Integer.parseInt(sox);
                int oy = soy.isEmpty() ? offY : Integer.parseInt(soy);
                int oz = soz.isEmpty() ? offZ : Integer.parseInt(soz);

                boundX.setFocused(false);
                boundY.setFocused(false);
                boundZ.setFocused(false);
                offsetX.setFocused(false);
                offsetY.setFocused(false);
                offsetZ.setFocused(false);

                update(nx, ny, nz, ox, oy, oz, showBounds, sortingType);

                return;
            }
        }

        if (boundX.textboxKeyTyped(typedChar, keyCode)) return;
        if (boundY.textboxKeyTyped(typedChar, keyCode)) return;
        if (boundZ.textboxKeyTyped(typedChar, keyCode)) return;
        if (offsetX.textboxKeyTyped(typedChar, keyCode)) return;
        if (offsetY.textboxKeyTyped(typedChar, keyCode)) return;
        if (offsetZ.textboxKeyTyped(typedChar, keyCode)) return;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        boundX.mouseClicked(mouseX, mouseY, mouseButton);
        boundY.mouseClicked(mouseX, mouseY, mouseButton);
        boundZ.mouseClicked(mouseX, mouseY, mouseButton);
        offsetX.mouseClicked(mouseX, mouseY, mouseButton);
        offsetY.mouseClicked(mouseX, mouseY, mouseButton);
        offsetZ.mouseClicked(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        int nx = x;
        int ny = y;
        int nz = z;
        int ox = offX;
        int oy = offY;
        int oz = offZ;

        boolean nshowBounds = showBounds;
        SortingType nsortingType = sortingType;

        switch (button.id) {
            case BUTTON_BOUND_X_UP:
                nx += 1;
                break;
            case BUTTON_BOUND_X_DOWN:
                nx -= 1;
                break;
            case BUTTON_BOUND_Y_UP:
                ny += 1;
                break;
            case BUTTON_BOUND_Y_DOWN:
                ny -= 1;
                break;
            case BUTTON_BOUND_Z_UP:
                nz += 1;
                break;
            case BUTTON_BOUND_Z_DOWN:
                nz -= 1;
                break;
            case BUTTON_OFFSET_X_UP:
                ox += 1;
                break;
            case BUTTON_OFFSET_X_DOWN:
                ox -= 1;
                break;
            case BUTTON_OFFSET_Y_UP:
                oy += 1;
                break;
            case BUTTON_OFFSET_Y_DOWN:
                oy -= 1;
                break;
            case BUTTON_OFFSET_Z_UP:
                oz += 1;
                break;
            case BUTTON_OFFSET_Z_DOWN:
                oz -= 1;
                break;

            case BUTTON_TOGGLE_BOUNDS: {
                this.tile.showBounds = !this.tile.showBounds;
                buttonShowBounds.displayString = I18n.translateToLocal(this.tile.showBounds ? TEXT_HIDE_BOUNDS : TEXT_SHOW_BOUNDS);
                break;
            }

            case BUTTON_SORTING_TYPE: {
                int ord = nsortingType.ordinal();
                if (ord + 1 >= SortingType.VALUES.length) {
                    nsortingType = SortingType.ROWS;
                } else {
                    nsortingType = SortingType.VALUES[ord + 1];
                }

                buttonSortType.displayString = I18n.translateToLocal(TEXT_SORTING_TYPE + nsortingType.getUnlocalizedName());
            }

            default:
                break;
        }

        update(nx, ny, nz, ox, oy, oz, nshowBounds, nsortingType);
    }

    private void update(int nx, int ny, int nz, int offX, int offY, int offZ, boolean nshowBounds, SortingType nsortingType) {
        int ox = x;
        int oy = y;
        int oz = z;
        int noffX = this.offX;
        int noffY = this.offY;
        int noffZ = this.offZ;

        SortingType osortingType = sortingType;

        if (nx <= 0) nx = 1;
        else if (nx >= ImpracticalConfig.BOUNDS_OPTIONS.maxX.get())
            nx = ImpracticalConfig.BOUNDS_OPTIONS.maxX.get() - 1;
        if (ny <= 0) ny = 1;
        else if (ny >= ImpracticalConfig.BOUNDS_OPTIONS.maxY.get())
            ny = ImpracticalConfig.BOUNDS_OPTIONS.maxY.get() - 1;
        if (nz <= 0) nz = 1;
        else if (nz >= ImpracticalConfig.BOUNDS_OPTIONS.maxZ.get())
            nz = ImpracticalConfig.BOUNDS_OPTIONS.maxZ.get() - 1;

        if (tile.getBlockPos().getY() + offY <= 0) offY = offY + 1;

        boundX.setText(Integer.toString(nx));
        boundY.setText(Integer.toString(ny));
        boundZ.setText(Integer.toString(nz));
        offsetX.setText(Integer.toString(offX));
        offsetY.setText(Integer.toString(offY));
        offsetZ.setText(Integer.toString(offZ));

        boolean dimensions = false;
        int boundX = 0;
        int boundY = 0;
        int boundZ = 0;
        boolean offset = false;
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        boolean sort = false;
        SortingType sortingType = SortingType.ROWS;

        if (ox != nx || oy != ny || oz != nz) {
            dimensions = true;
            boundX = nx;
            boundY = ny;
            boundZ = nz;
        }

        if (noffX != offX || noffY != offY || noffZ != offZ) {
            offset = true;
            offsetX = offX;
            offsetY = offY;
            offsetZ = offZ;
        }

        if (osortingType != nsortingType) {
            sort = true;
            sortingType = nsortingType;
        }

        SControllerConfig packet = new SControllerConfig(tile.getBlockPos(), dimensions, boundX, boundY, boundZ, offset, offsetX, offsetY, offsetZ, sort, sortingType);
        PacketHandler.CHANNEL.sendToServer(packet);

        x = nx;
        y = ny;
        z = nz;
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
        showBounds = nshowBounds;
        sortingType = nsortingType;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(GUI_TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        fontRenderer.drawString(I18n.translateToLocal("gui.label.bounds"), guiLeft + 8, guiTop + 10, 4210752, false);
        fontRenderer.drawString(I18n.translateToLocal("gui.label.offset"), guiLeft + 8, guiTop + 81, 4210752, false);
        fontRenderer.drawString(I18n.translateToLocal("gui.label.other"), guiLeft + 8, guiTop + 152, 4210752, false);

        super.drawScreen(mouseX, mouseY, partialTicks);

        boundX.drawTextBox();
        boundY.drawTextBox();
        boundZ.drawTextBox();

        offsetX.drawTextBox();
        offsetY.drawTextBox();
        offsetZ.drawTextBox();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
