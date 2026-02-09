package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;

public interface UnboundProtocol<T extends PacketListener, B extends ByteBuf, C> extends ProtocolInfo.b {

    ProtocolInfo<T> bind(Function<ByteBuf, B> function, C c0);
}
