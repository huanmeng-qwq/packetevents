/*
 * This file is part of ViaVersion - https://github.com/ViaVersion/ViaVersion
 * Copyright (C) 2016-2021 ViaVersion and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents.injector.legacy;

import io.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.processor.PacketProcessorInternal;
import net.minecraft.util.io.netty.channel.ChannelDuplexHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import org.bukkit.entity.Player;

public class PlayerChannelHandlerLegacy extends ChannelDuplexHandler {
    /**
     * Associated player.
     * This is null until we inject the player.
     */
    public volatile Player player;

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object packet) throws Exception {
        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().read(player, ctx.channel(), packet);
        if (data.packet != null) {
            super.channelRead(ctx, data.packet);
            PacketEvents.get().getInternalPacketProcessor().postRead(player, ctx.channel(), data.packet);
        }
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object packet, final ChannelPromise promise) throws Exception {
        PacketProcessorInternal.PacketData data = PacketEvents.get().getInternalPacketProcessor().write(player, ctx.channel(), packet);
        if (data.postAction != null) {
            promise.addListener(f -> {
                data.postAction.run();
            });
        }
        if (data.packet != null) {
            super.write(ctx, data.packet, promise);
            PacketEvents.get().getInternalPacketProcessor().postWrite(player, ctx.channel(), data.packet);
        }
    }
}
