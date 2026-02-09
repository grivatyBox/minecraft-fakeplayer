package net.minecraft.network;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketEncoderException extends EncoderException implements SkipEncodeException, IdDispatchCodec.b {

    public SkipPacketEncoderException(String s) {
        super(s);
    }

    public SkipPacketEncoderException(Throwable throwable) {
        super(throwable);
    }
}
