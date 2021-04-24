/*
 * Copyright (c) 2019-2021 Team Galacticraft
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

import dev.galacticraft.mod.block.entity.CircuitFabricatorBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.Property;

/**
 * @author <a href="https://github.com/TeamGalacticraft">TeamGalacticraft</a>
 */
public class CircuitFabricatorScreenHandler extends MachineScreenHandler<CircuitFabricatorBlockEntity> {
    public final Property progress = new Property() {
        @Override
        public int get() {
            return CircuitFabricatorScreenHandler.this.machine.progress;
        }

        @Override
        public void set(int value) {
            CircuitFabricatorScreenHandler.this.machine.progress = value;
        }
    };

    public CircuitFabricatorScreenHandler(int syncId, PlayerEntity player, CircuitFabricatorBlockEntity machine) {
        super(syncId, player, machine, GalacticraftScreenHandlerType.CIRCUIT_FABRICATOR_HANDLER);
        this.addProperty(this.progress);
        this.addPlayerInventorySlots(0, 94);
    }

    public CircuitFabricatorScreenHandler(int syncId, PlayerInventory inv, PacketByteBuf buf) {
        this(syncId, inv.player, (CircuitFabricatorBlockEntity) inv.player.world.getBlockEntity(buf.readBlockPos()));
    }
}
