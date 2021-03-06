/**
 *  Thaumic Augmentation
 *  Copyright (c) 2019 TheCodex6824.
 *
 *  This file is part of Thaumic Augmentation.
 *
 *  Thaumic Augmentation is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Thaumic Augmentation is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Thaumic Augmentation.  If not, see <https://www.gnu.org/licenses/>.
 */

package thecodex6824.thaumicaugmentation.common.network;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import thecodex6824.thaumicaugmentation.ThaumicAugmentation;

public class PacketFullWardSync implements IMessage {

    private NBTTagCompound tag;
    
    public PacketFullWardSync() {}
    
    public PacketFullWardSync(NBTTagCompound toSend) {
        tag = toSend;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            byte[] buffer = new byte[buf.readInt()];
            buf.readBytes(buffer);
            ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
            tag = CompressedStreamTools.readCompressed(stream);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to deserialize PacketFullWardSync: " + ex.getMessage());
        }
    }
    
    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            CompressedStreamTools.writeCompressed(tag, stream);
            byte[] data = stream.toByteArray();
            buf.writeInt(data.length);
            buf.writeBytes(data);
        }
        catch (IOException ex) {
            ThaumicAugmentation.getLogger().warn("Unable to serialize PacketFullWardSync: " + ex.getMessage());
        }
    }
    
    public NBTTagCompound getTag() {
        return tag;
    }
    
}
