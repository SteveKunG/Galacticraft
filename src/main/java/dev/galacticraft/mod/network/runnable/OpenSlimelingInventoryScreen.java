package dev.galacticraft.mod.network.runnable;

import dev.galacticraft.mod.client.gui.screen.ingame.SlimelingInventoryScreen;
import dev.galacticraft.mod.content.entity.Slimeling;
import dev.galacticraft.mod.network.s2c.OpenSlimelingInventoryScreenPayload;
import dev.galacticraft.mod.screen.SlimelingInventoryMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.SimpleContainer;

public class OpenSlimelingInventoryScreen implements Runnable {
    private final ClientPlayNetworking.Context context;
    private final OpenSlimelingInventoryScreenPayload payload;

    public OpenSlimelingInventoryScreen(ClientPlayNetworking.Context context, OpenSlimelingInventoryScreenPayload payload) {
        this.context = context;
        this.payload = payload;
    }

    @Override
    public void run() {
        if (this.context.client().level.getEntity(this.payload.entityId()) instanceof Slimeling slimeling) {
            var localPlayer = this.context.client().player;
            var columns = this.payload.inventoryColumns();
            var simpleContainer = new SimpleContainer(Slimeling.getInventorySize(columns));
            var inventoryMenu = new SlimelingInventoryMenu(this.payload.containerId(), localPlayer.getInventory(), simpleContainer, slimeling, columns);
            localPlayer.containerMenu = inventoryMenu;
            this.context.client().setScreen(new SlimelingInventoryScreen(inventoryMenu, localPlayer.getInventory(), slimeling, columns));
        }
    }
}