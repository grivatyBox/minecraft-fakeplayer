package net.minecraft.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketDecoderException extends DecoderException implements SkipEncodeException, IdDispatchCodec.b {

    public SkipPacketDecoderException(String s) {
        super(s);
    }

    public SkipPacketDecoderException(Throwable throwable) {
        super(throwable);
    }
}
