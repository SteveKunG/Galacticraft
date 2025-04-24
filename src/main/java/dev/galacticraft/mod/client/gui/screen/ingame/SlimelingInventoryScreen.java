/*
 * Copyright (c) 2019-2025 Team Galacticraft
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.galacticraft.mod.client.gui.screen.ingame;

import dev.galacticraft.mod.content.entity.Slimeling;
import dev.galacticraft.mod.screen.SlimelingInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SlimelingInventoryScreen extends AbstractContainerScreen<SlimelingInventoryMenu> {
    private static final ResourceLocation CHEST_SLOTS_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/chest_slots");
    private static final ResourceLocation SADDLE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/saddle_slot");
    private static final ResourceLocation ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/horse/armor_slot");
    private static final ResourceLocation HORSE_INVENTORY_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/horse.png");
    private final Slimeling slimeling;
    private final int inventoryColumns;
    private float xMouse;
    private float yMouse;

    public SlimelingInventoryScreen(SlimelingInventoryMenu menu, Inventory inventory, Slimeling slimeling, int column) {
        super(menu, inventory, slimeling.getDisplayName());
        this.slimeling = slimeling;
        this.inventoryColumns = column;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        var i = (this.width - this.imageWidth) / 2;
        var j = (this.height - this.imageHeight) / 2;
        graphics.blit(HORSE_INVENTORY_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);

        if (this.inventoryColumns > 0) {
            graphics.blitSprite(CHEST_SLOTS_SPRITE, 90, 54, 0, 0, i + 79, j + 17, this.inventoryColumns * 18, 54);
        }

        graphics.blitSprite(SADDLE_SLOT_SPRITE, i + 7, j + 35 - 18, 18, 18);

        InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, i + 26, j + 18, i + 78, j + 70, 17, 0.25F, this.xMouse, this.yMouse, this.slimeling);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.xMouse = mouseX;
        this.yMouse = mouseY;
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}