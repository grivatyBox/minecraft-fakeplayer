package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.util.SignatureUpdater;

public record LastSeenMessages(List<MessageSignature> entries) {

    public static final Codec<LastSeenMessages> CODEC = MessageSignature.CODEC.listOf().xmap(LastSeenMessages::new, LastSeenMessages::entries);
    public static LastSeenMessages EMPTY = new LastSeenMessages(List.of());
    public static final int LAST_SEEN_MESSAGES_MAX_LENGTH = 20;

    public void updateSignature(SignatureUpdater.a signatureupdater_a) throws SignatureException {
        signatureupdater_a.update(Ints.toByteArray(this.entries.size()));

        for (MessageSignature messagesignature : this.entries) {
            signatureupdater_a.update(messagesignature.bytes());
        }

    }

    public LastSeenMessages.a pack(MessageSignatureCache messagesignaturecache) {
        return new LastSeenMessages.a(this.entries.stream().map((messagesignature) -> {
            return messagesignature.pack(messagesignaturecache);
        }).toList());
    }

    public byte computeChecksum() {
        int i = 1;

        for (MessageSignature messagesignature : this.entries) {
            i = 31 * i + messagesignature.checksum();
        }

        byte b0 = (byte) i;

        return b0 == 0 ? 1 : b0;
    }

    public static record a(List<MessageSignature.a> entries) {

        public static final LastSeenMessages.a EMPTY = new LastSeenMessages.a(List.of());

        public a(PacketDataSerializer packetdataserializer) {
            this((List) packetdataserializer.readCollection(PacketDataSerializer.limitValue(ArrayList::new, 20), MessageSignature.a::read));
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeCollection(this.entries, MessageSignature.a::write);
        }

        public Optional<LastSeenMessages> unpack(MessageSignatureCache messagesignaturecache) {
            List<MessageSignature> list = new ArrayList(this.entries.size());

            for (MessageSignature.a messagesignature_a : this.entries) {
                Optional<MessageSignature> optional = messagesignature_a.unpack(messagesignaturecache);

                if (optional.isEmpty()) {
                    return Optional.empty();
                }

                list.add((MessageSignature) optional.get());
            }

            return Optional.of(new LastSeenMessages(list));
        }
    }

    public static record b(int offset, BitSet acknowledged, byte checksum) {

        public static final byte IGNORE_CHECKSUM = 0;

        public b(PacketDataSerializer packetdataserializer) {
            this(packetdataserializer.readVarInt(), packetdataserializer.readFixedBitSet(20), packetdataserializer.readByte());
        }

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeVarInt(this.offset);
            packetdataserializer.writeFixedBitSet(this.acknowledged, 20);
            packetdataserializer.writeByte(this.checksum);
        }

        public boolean verifyChecksum(LastSeenMessages lastseenmessages) {
            return this.checksum == 0 || this.checksum == lastseenmessages.computeChecksum();
        }
    }
}
