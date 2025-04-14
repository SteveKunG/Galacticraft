package dev.galacticraft.mod.content.entity.inventory;

import dev.galacticraft.mod.content.entity.Slimeling;
import net.minecraft.world.Container;

public interface SlimelingInventory {
    default void galacticraft$openSlimelingInventory(Slimeling slimeling, Container container) {
        throw new AssertionError("Implemented via mixin");
    }
}