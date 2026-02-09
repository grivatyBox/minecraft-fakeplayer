package net.minecraft.network.protocol;

import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.ServerboundPacketListener;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public class ProtocolInfoBuilder<T extends PacketListener, B extends ByteBuf, C> {

    final EnumProtocol protocol;
    final EnumProtocolDirection flow;
    private final List<ProtocolInfoBuilder.a<T, ?, B, C>> codecs = new ArrayList();
    @Nullable
    private BundlerInfo bundlerInfo;

    public ProtocolInfoBuilder(EnumProtocol enumprotocol, EnumProtocolDirection enumprotocoldirection) {
        this.protocol = enumprotocol;
        this.flow = enumprotocoldirection;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> packettype, StreamCodec<? super B, P> streamcodec) {
        this.codecs.add(new ProtocolInfoBuilder.a(packettype, streamcodec, (CodecModifier) null));
        return this;
    }

    public <P extends Packet<? super T>> ProtocolInfoBuilder<T, B, C> addPacket(PacketType<P> packettype, StreamCodec<? super B, P> streamcodec, CodecModifier<B, P, C> codecmodifier) {
        this.codecs.add(new ProtocolInfoBuilder.a(packettype, streamcodec, codecmodifier));
        return this;
    }

    public <P extends BundlePacket<? super T>, D extends BundleDelimiterPacket<? super T>> ProtocolInfoBuilder<T, B, C> withBundlePacket(PacketType<P> packettype, Function<Iterable<Packet<? super T>>, P> function, D d0) {
        StreamCodec<ByteBuf, D> streamcodec = StreamCodec.<ByteBuf, D>unit(d0);
        PacketType<D> packettype1 = d0.type();

        this.codecs.add(new ProtocolInfoBuilder.a(packettype1, streamcodec, (CodecModifier) null));
        this.bundlerInfo = BundlerInfo.createForPacket(packettype, function, d0);
        return this;
    }

    StreamCodec<ByteBuf, Packet<? super T>> buildPacketCodec(Function<ByteBuf, B> function, List<ProtocolInfoBuilder.a<T, ?, B, C>> list, C c0) {
        ProtocolCodecBuilder<ByteBuf, T> protocolcodecbuilder = new ProtocolCodecBuilder<ByteBuf, T>(this.flow);

        for (ProtocolInfoBuilder.a<T, ?, B, C> protocolinfobuilder_a : list) {
            protocolinfobuilder_a.addToBuilder(protocolcodecbuilder, function, c0);
        }

        return protocolcodecbuilder.build();
    }

    private static ProtocolInfo.a buildDetails(final EnumProtocol enumprotocol, final EnumProtocolDirection enumprotocoldirection, final List<? extends ProtocolInfoBuilder.a<?, ?, ?, ?>> list) {
        return new ProtocolInfo.a() {
            @Override
            public EnumProtocol id() {
                return enumprotocol;
            }

            @Override
            public EnumProtocolDirection flow() {
                return enumprotocoldirection;
            }

            @Override
            public void listPackets(ProtocolInfo.a.a protocolinfo_a_a) {
                for (int i = 0; i < list.size(); ++i) {
                    ProtocolInfoBuilder.a<?, ?, ?, ?> protocolinfobuilder_a = (ProtocolInfoBuilder.a) list.get(i);

                    protocolinfo_a_a.accept(protocolinfobuilder_a.type, i);
                }

            }
        };
    }

    public SimpleUnboundProtocol<T, B> buildUnbound(final C c0) {
        final List<ProtocolInfoBuilder.a<T, ?, B, C>> list = List.copyOf(this.codecs);
        final BundlerInfo bundlerinfo = this.bundlerInfo;
        final ProtocolInfo.a protocolinfo_a = buildDetails(this.protocol, this.flow, list);

        return new SimpleUnboundProtocol<T, B>() {
            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function) {
                return new ProtocolInfoBuilder.b<T>(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list, c0), bundlerinfo);
            }

            @Override
            public ProtocolInfo.a details() {
                return protocolinfo_a;
            }
        };
    }

    public UnboundProtocol<T, B, C> buildUnbound() {
        final List<ProtocolInfoBuilder.a<T, ?, B, C>> list = List.copyOf(this.codecs);
        final BundlerInfo bundlerinfo = this.bundlerInfo;
        final ProtocolInfo.a protocolinfo_a = buildDetails(this.protocol, this.flow, list);

        return new UnboundProtocol<T, B, C>() {
            @Override
            public ProtocolInfo<T> bind(Function<ByteBuf, B> function, C c0) {
                return new ProtocolInfoBuilder.b<T>(ProtocolInfoBuilder.this.protocol, ProtocolInfoBuilder.this.flow, ProtocolInfoBuilder.this.buildPacketCodec(function, list, c0), bundlerinfo);
            }

            @Override
            public ProtocolInfo.a details() {
                return protocolinfo_a;
            }
        };
    }

    private static <L extends PacketListener, B extends ByteBuf> SimpleUnboundProtocol<L, B> protocol(EnumProtocol enumprotocol, EnumProtocolDirection enumprotocoldirection, Consumer<ProtocolInfoBuilder<L, B, Unit>> consumer) {
        ProtocolInfoBuilder<L, B, Unit> protocolinfobuilder = new ProtocolInfoBuilder<L, B, Unit>(enumprotocol, enumprotocoldirection);

        consumer.accept(protocolinfobuilder);
        return protocolinfobuilder.buildUnbound(Unit.INSTANCE);
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> serverboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B, Unit>> consumer) {
        return protocol(enumprotocol, EnumProtocolDirection.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf> SimpleUnboundProtocol<T, B> clientboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B, Unit>> consumer) {
        return protocol(enumprotocol, EnumProtocolDirection.CLIENTBOUND, consumer);
    }

    private static <L extends PacketListener, B extends ByteBuf, C> UnboundProtocol<L, B, C> contextProtocol(EnumProtocol enumprotocol, EnumProtocolDirection enumprotocoldirection, Consumer<ProtocolInfoBuilder<L, B, C>> consumer) {
        ProtocolInfoBuilder<L, B, C> protocolinfobuilder = new ProtocolInfoBuilder<L, B, C>(enumprotocol, enumprotocoldirection);

        consumer.accept(protocolinfobuilder);
        return protocolinfobuilder.buildUnbound();
    }

    public static <T extends ServerboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextServerboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B, C>> consumer) {
        return contextProtocol(enumprotocol, EnumProtocolDirection.SERVERBOUND, consumer);
    }

    public static <T extends ClientboundPacketListener, B extends ByteBuf, C> UnboundProtocol<T, B, C> contextClientboundProtocol(EnumProtocol enumprotocol, Consumer<ProtocolInfoBuilder<T, B, C>> consumer) {
        return contextProtocol(enumprotocol, EnumProtocolDirection.CLIENTBOUND, consumer);
    }

    private static record a<T extends PacketListener, P extends Packet<? super T>, B extends ByteBuf, C>(PacketType<P> type, StreamCodec<? super B, P> serializer, @Nullable CodecModifier<B, P, C> modifier) {

        public void addToBuilder(ProtocolCodecBuilder<ByteBuf, T> protocolcodecbuilder, Function<ByteBuf, B> function, C c0) {
            StreamCodec<? super B, P> streamcodec;

            if (this.modifier != null) {
                streamcodec = this.modifier.apply(this.serializer, c0);
            } else {
                streamcodec = this.serializer;
            }

            StreamCodec<ByteBuf, P> streamcodec1 = streamcodec.mapStream(function);

            protocolcodecbuilder.add(this.type, streamcodec1);
        }
    }

    private static record b<L extends PacketListener>(EnumProtocol id, EnumProtocolDirection flow, StreamCodec<ByteBuf, Packet<? super L>> codec, @Nullable BundlerInfo bundlerInfo) implements ProtocolInfo<L> {

    }
}
