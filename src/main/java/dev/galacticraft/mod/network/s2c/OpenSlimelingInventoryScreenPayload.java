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

package dev.galacticraft.mod.network.s2c;

import org.jetbrains.annotations.NotNull;
import dev.galacticraft.impl.network.s2c.S2CPayload;
import dev.galacticraft.mod.Constant;
import dev.galacticraft.mod.client.gui.screen.ingame.SlimelingInventoryScreen;
import dev.galacticraft.mod.content.entity.Slimeling;
import dev.galacticraft.mod.screen.SlimelingInventoryMenu;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;

public record OpenSlimelingInventoryScreenPayload(int containerId, int inventoryColumns, int entityId) implements S2CPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSlimelingInventoryScreenPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            p -> p.containerId,
            ByteBufCodecs.INT,
            p -> p.inventoryColumns,
            ByteBufCodecs.INT,
            p -> p.entityId,
            OpenSlimelingInventoryScreenPayload::new
    );

    public static final ResourceLocation ID = Constant.id("open_slimeling_inventory");
    public static final Type<OpenSlimelingInventoryScreenPayload> TYPE = new Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @SuppressWarnings("Convert2Lambda")
    @Override
    public Runnable handle(@NotNull ClientPlayNetworking.Context context) {
        return new Runnable() {
            @Override
            public void run() {
                if (context.client().level.getEntity(OpenSlimelingInventoryScreenPayload.this.entityId()) instanceof Slimeling slimeling) {
                    var localPlayer = context.client().player;
                    var columns = OpenSlimelingInventoryScreenPayload.this.inventoryColumns();
                    var simpleContainer = new SimpleContainer(Slimeling.getInventorySize(columns));
                    var inventoryMenu = new SlimelingInventoryMenu(OpenSlimelingInventoryScreenPayload.this.containerId(), localPlayer.getInventory(), simpleContainer, slimeling, columns);
                    localPlayer.containerMenu = inventoryMenu;
                    context.client().setScreen(new SlimelingInventoryScreen(inventoryMenu, localPlayer.getInventory(), slimeling, columns));
                }
            }
        };
    }
}