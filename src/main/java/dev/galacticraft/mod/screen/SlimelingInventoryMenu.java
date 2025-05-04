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

package dev.galacticraft.mod.screen;

import dev.galacticraft.mod.content.entity.Slimeling;
import dev.galacticraft.mod.content.item.GCItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlimelingInventoryMenu extends AbstractContainerMenu {
    private final Container container;
    private final Slimeling slimeling;

    public SlimelingInventoryMenu(int containerId, Inventory inventory, Container container, Slimeling slimeling, int column) {
        super(null, containerId);
        this.container = container;
        this.slimeling = slimeling;
        container.startOpen(inventory.player);

        // Bag Slot
        this.addSlot(new Slot(container, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(GCItems.SLIMELING_INVENTORY_BAG) && !this.hasItem();
            }
        });

        // Slimeling Inventory
        if (column > 0) {
            for (var m = 0; m < 3; m++) {
                for (var n = 0; n < column; n++) {
                    this.addSlot(new Slot(container, 1 + n + m * column, 80 + n * 18, 18 + m * 18) {
                        @Override
                        public boolean isActive() {
                            return slimeling.hasBag();
                        }
                    });
                }
            }
        }

        // Player Inventory
        for (var m = 0; m < 3; m++) {
            for (var n = 0; n < 9; n++) {
                this.addSlot(new Slot(inventory, n + m * 9 + 9, 8 + n * 18, 102 + m * 18 - 18));
            }
        }

        // Hotbar
        for (var m = 0; m < 9; m++) {
            this.addSlot(new Slot(inventory, m, 8 + m * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.slimeling.hasInventoryChanged(this.container) && this.container.stillValid(player) && this.slimeling.isAlive() && player.canInteractWithEntity(this.slimeling, 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        var itemStack = ItemStack.EMPTY;
        var slot2 = this.slots.get(slot);

        if (slot2.hasItem()) {
            var itemStack2 = slot2.getItem();
            itemStack = itemStack2.copy();
            var i = this.container.getContainerSize();
            if (slot < i) {
                if (!this.moveItemStackTo(itemStack2, i, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).mayPlace(itemStack2)) {
                if (!this.moveItemStackTo(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 1, i, false)) {
                var k = i + 27;
                var m = k + 9;
                if (slot >= k && slot < m) {
                    if (!this.moveItemStackTo(itemStack2, i, k, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slot < k) {
                    if (!this.moveItemStackTo(itemStack2, k, m, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemStack2, k, k, false)) {
                    return ItemStack.EMPTY;
                }

                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot2.setByPlayer(ItemStack.EMPTY);
            } else {
                slot2.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }
}