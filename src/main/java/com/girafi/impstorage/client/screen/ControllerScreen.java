package com.girafi.impstorage.client.screen;

import com.girafi.impstorage.block.blockentity.ControllerBlockEntity;
import com.girafi.impstorage.client.screen.widget.ButtonArrowScreen;
import com.girafi.impstorage.lib.ImpracticalConfig;
import com.girafi.impstorage.lib.ModInfo;
import com.girafi.impstorage.lib.data.SortingType;
import com.girafi.impstorage.network.PacketHandler;
import com.girafi.impstorage.network.packet.SControllerConfig;
import com.google.common.base.Predicate;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import javax.annotation.Nonnull;

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

    private static final Component TEXT_SHOW_BOUNDS = Component.translatable("impstorage.gui.text.bounds.show");
    private static final Component TEXT_HIDE_BOUNDS = Component.translatable("impstorage.gui.text.bounds.hide");
    private static final String TEXT_SORTING_TYPE = "impstorage.gui.text.sort_type.";

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

    private EditBox boundX;
    private EditBox boundY;
    private EditBox boundZ;

    private EditBox offsetX;
    private EditBox offsetY;
    private EditBox offsetZ;

    private ControllerBlockEntity controllerBlockEntity;

    public ControllerScreen(ControllerBlockEntity controllerBlockEntity) {
        super(Component.translatable("impstorage.controllerScreen.title"));
        this.controllerBlockEntity = controllerBlockEntity;

        this.x = controllerBlockEntity.rawX;
        this.y = controllerBlockEntity.rawY;
        this.z = controllerBlockEntity.rawZ;
        this.offX = controllerBlockEntity.offset.getX();
        this.offY = controllerBlockEntity.offset.getY();
        this.offZ = controllerBlockEntity.offset.getZ();

        this.showBounds = controllerBlockEntity.showBounds;
        this.sortingType = controllerBlockEntity.sortingType;

        this.isInventoryEmpty = controllerBlockEntity.isEmpty;
    }

    @Override
    public void tick() {
        super.tick();
        this.boundX.tick();
        this.boundY.tick();
        this.boundZ.tick();
        this.offsetX.tick();
        this.offsetY.tick();
        this.offsetZ.tick();
    }

    @Override
    public void init() {
        super.init();

        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;

        addRenderableWidget(new ExtendedButton(guiLeft + 8, guiTop + 164, 101, 16,
                TEXT_SHOW_BOUNDS, (button) -> {

            System.out.println(this.controllerBlockEntity.showBounds);
            update(this.x, this.y, this.z, this.offX, this.offY, this.offZ, !this.controllerBlockEntity.showBounds, this.sortingType);
            button.setMessage(this.controllerBlockEntity.showBounds ? TEXT_HIDE_BOUNDS : TEXT_SHOW_BOUNDS);
        }));

        addRenderableWidget(new ExtendedButton(guiLeft + 8, guiTop + 184, 101, 16,
                Component.translatable(TEXT_SORTING_TYPE + sortingType.getUnlocalizedName()), (button) -> {
            SortingType sortingType = this.sortingType;
            int ord = sortingType.ordinal();
            if (ord + 1 >= SortingType.VALUES.length) {
                sortingType = SortingType.ROWS;
            } else {
                sortingType = SortingType.VALUES[ord + 1];
            }
            update(this.x, this.y, this.z, this.offX, this.offY, this.offZ, this.showBounds, sortingType);
            button.setMessage(Component.translatable(TEXT_SORTING_TYPE + sortingType.getUnlocalizedName()));
        }));

        addRenderableWidget(new ButtonArrowScreen(guiLeft + 8, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x + 1, this.y, this.z, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 8, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x - 1, this.y, this.z, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 43, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x, this.y + 1, this.z, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 43, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x, this.y - 1, this.z, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        ;
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 78, guiTop + 21, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x, this.y, this.z + 1, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 78, guiTop + 60, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x, this.y, this.z - 1, this.offX, this.offY, this.offZ, this.showBounds, this.sortingType)));
        ;
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 8, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x, this.y, this.z, this.offX + 1, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 8, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x, this.y, this.z, this.offX - 1, this.offY, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 43, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x, this.y, this.z, this.offX, this.offY + 1, this.offZ, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 43, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x, this.y, this.z, this.offX, this.offY - 1, this.offZ, this.showBounds, this.sortingType)));
        ;
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 78, guiTop + 92, 31, 15, ButtonArrowScreen.ARROW_UP, (button) -> update(this.x, this.y, this.z, this.offX, this.offY, this.offZ + 1, this.showBounds, this.sortingType)));
        addRenderableWidget(new ButtonArrowScreen(guiLeft + 78, guiTop + 131, 31, 15, ButtonArrowScreen.ARROW_DOWN, (button) -> update(this.x, this.y, this.z, this.offX, this.offY, this.offZ - 1, this.showBounds, this.sortingType)));

        this.renderables.stream().filter((b) -> b instanceof ButtonArrowScreen).forEach((b) -> ((ButtonArrowScreen) b).active = this.isInventoryEmpty);

        this.boundX = new EditBox(this.font, this.guiLeft + 9, this.guiTop + 40, 29, 15, Component.translatable("impstorage.boundX"));
        this.boundX.setValue(Integer.toString(x));
        this.boundX.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.boundX);

        this.boundY = new EditBox(this.font, this.guiLeft + 44, this.guiTop + 40, 29, 15, Component.translatable("impstorage.boundY"));
        this.boundY.setValue(Integer.toString(y));
        this.boundY.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.boundY);

        this.boundZ = new EditBox(this.font, this.guiLeft + 79, this.guiTop + 40, 29, 15, Component.translatable("impstorage.boundZ"));
        this.boundZ.setValue(Integer.toString(z));
        this.boundZ.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.boundZ);

        this.offsetX = new EditBox(this.font, this.guiLeft + 9, this.guiTop + 111, 29, 15, Component.translatable("impstorage.offsetX"));
        this.offsetX.setValue(Integer.toString(offX));
        this.offsetX.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.offsetX);

        this.offsetY = new EditBox(this.font, this.guiLeft + 44, this.guiTop + 111, 29, 15, Component.translatable("impstorage.offsetY"));
        this.offsetY.setValue(Integer.toString(offY));
        this.offsetY.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.offsetY);

        this.offsetZ = new EditBox(this.font, this.guiLeft + 79, this.guiTop + 111, 29, 15, Component.translatable("impstorage.offsetZ"));
        this.offsetZ.setValue(Integer.toString(offZ));
        this.offsetZ.setFilter(NUMBER_VALIDATOR);
        this.addWidget(this.offsetZ);
    }

    @Override
    public boolean charTyped(char key, int keyCode) {
        super.charTyped(key, keyCode);

        if (boundX.isFocused() || boundY.isFocused() || boundZ.isFocused() || offsetX.isFocused() || offsetY.isFocused() || offsetZ.isFocused()) {
            String sx = boundX.getValue();
            String sy = boundY.getValue();
            String sz = boundZ.getValue();
            String sox = offsetX.getValue();
            String soy = offsetY.getValue();
            String soz = offsetZ.getValue();

            int nx = sx.isEmpty() ? x : Integer.parseInt(sx);
            int ny = sy.isEmpty() ? y : Integer.parseInt(sy);
            int nz = sz.isEmpty() ? z : Integer.parseInt(sz);

            int ox = sox.isEmpty() ? offX : Integer.parseInt(sox);
            int oy = soy.isEmpty() ? offY : Integer.parseInt(soy);
            int oz = soz.isEmpty() ? offZ : Integer.parseInt(soz);

            update(nx, ny, nz, ox, oy, oz, showBounds, sortingType);

            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.boundX.mouseClicked(mouseX, mouseY, mouseButton);
        this.boundY.mouseClicked(mouseX, mouseY, mouseButton);
        this.boundZ.mouseClicked(mouseX, mouseY, mouseButton);
        this.offsetX.mouseClicked(mouseX, mouseY, mouseButton);
        this.offsetY.mouseClicked(mouseX, mouseY, mouseButton);
        this.offsetZ.mouseClicked(mouseX, mouseY, mouseButton);

        return true;
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

        if (this.controllerBlockEntity.getBlockPos().getY() + offY <= 0) offY = offY + 1;

        this.boundX.setValue(Integer.toString(nx));
        this.boundY.setValue(Integer.toString(ny));
        this.boundZ.setValue(Integer.toString(nz));
        this.offsetX.setValue(Integer.toString(offX));
        this.offsetY.setValue(Integer.toString(offY));
        this.offsetZ.setValue(Integer.toString(offZ));

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
            System.out.println("Dimensions set to true");
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

        SControllerConfig packet = new SControllerConfig(this.controllerBlockEntity.getBlockPos(), dimensions, boundX, boundY, boundZ, offset, offsetX, offsetY, offsetZ, sort, sortingType);
        PacketHandler.CHANNEL.sendToServer(packet);

        this.x = nx;
        this.y = ny;
        this.z = nz;
        this.offX = offX;
        this.offY = offY;
        this.offZ = offZ;
        this.showBounds = nshowBounds;
        this.sortingType = nsortingType;
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        guiGraphics.drawString(this.font, Component.translatable("impstorage.gui.label.bounds"), this.guiLeft + 8, this.guiTop + 10, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("impstorage.gui.label.offset"), this.guiLeft + 8, this.guiTop + 81, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("impstorage.gui.label.other"), this.guiLeft + 8, this.guiTop + 152, 4210752, false);

        this.boundX.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.boundY.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.boundZ.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.offsetX.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.offsetY.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.offsetZ.render(guiGraphics, mouseX, mouseY, partialTicks);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(@Nonnull GuiGraphics guiGraphics) {
        guiGraphics.blit(GUI_TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}