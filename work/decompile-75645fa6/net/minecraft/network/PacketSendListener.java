package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import org.slf4j.Logger;

public class PacketSendListener {

    private static final Logger LOGGER = LogUtils.getLogger();

    public PacketSendListener() {}

    public static ChannelFutureListener thenRun(Runnable runnable) {
        return (channelfuture) -> {
            runnable.run();
            if (!channelfuture.isSuccess()) {
                channelfuture.channel().pipeline().fireExceptionCaught(channelfuture.cause());
            }

        };
    }

    public static ChannelFutureListener exceptionallySend(Supplier<Packet<?>> supplier) {
        return (channelfuture) -> {
            if (!channelfuture.isSuccess()) {
                Packet<?> packet = (Packet) supplier.get();

                if (packet != null) {
                    PacketSendListener.LOGGER.warn("Failed to deliver packet, sending fallback {}", packet.type(), channelfuture.cause());
                    channelfuture.channel().writeAndFlush(packet, channelfuture.channel().voidPromise());
                } else {
                    channelfuture.channel().pipeline().fireExceptionCaught(channelfuture.cause());
                }
            }

        };
    }
}
