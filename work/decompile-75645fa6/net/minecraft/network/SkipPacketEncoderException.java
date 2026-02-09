package net.minecraft.network;

import io.netty.handler.codec.EncoderException;
import net.minecraft.network.codec.IdDispatchCodec;

public class SkipPacketEncoderException extends EncoderException implements IdDispatchCodec.b, SkipEncodeException {

    public SkipPacketEncoderException(String s) {
        super(s);
    }

    public SkipPacketEncoderException(Throwable throwable) {
        super(throwable);
    }
}
