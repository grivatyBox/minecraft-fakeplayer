package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import javax.annotation.Nullable;

public class LastSeenMessagesValidator {

    private final int lastSeenCount;
    private final ObjectList<LastSeenTrackedEntry> trackedMessages = new ObjectArrayList();
    @Nullable
    private MessageSignature lastPendingMessage;

    public LastSeenMessagesValidator(int i) {
        this.lastSeenCount = i;

        for (int j = 0; j < i; ++j) {
            this.trackedMessages.add((Object) null);
        }

    }

    public void addPending(MessageSignature messagesignature) {
        if (!messagesignature.equals(this.lastPendingMessage)) {
            this.trackedMessages.add(new LastSeenTrackedEntry(messagesignature, true));
            this.lastPendingMessage = messagesignature;
        }

    }

    public int trackedMessagesCount() {
        return this.trackedMessages.size();
    }

    public void applyOffset(int i) throws LastSeenMessagesValidator.a {
        int j = this.trackedMessages.size() - this.lastSeenCount;

        if (i >= 0 && i <= j) {
            this.trackedMessages.removeElements(0, i);
        } else {
            throw new LastSeenMessagesValidator.a("Advanced last seen window by " + i + " messages, but expected at most " + j);
        }
    }

    public LastSeenMessages applyUpdate(LastSeenMessages.b lastseenmessages_b) throws LastSeenMessagesValidator.a {
        this.applyOffset(lastseenmessages_b.offset());
        ObjectList<MessageSignature> objectlist = new ObjectArrayList(lastseenmessages_b.acknowledged().cardinality());

        if (lastseenmessages_b.acknowledged().length() > this.lastSeenCount) {
            int i = lastseenmessages_b.acknowledged().length();

            throw new LastSeenMessagesValidator.a("Last seen update contained " + i + " messages, but maximum window size is " + this.lastSeenCount);
        } else {
            for (int j = 0; j < this.lastSeenCount; ++j) {
                boolean flag = lastseenmessages_b.acknowledged().get(j);
                LastSeenTrackedEntry lastseentrackedentry = (LastSeenTrackedEntry) this.trackedMessages.get(j);

                if (flag) {
                    if (lastseentrackedentry == null) {
                        throw new LastSeenMessagesValidator.a("Last seen update acknowledged unknown or previously ignored message at index " + j);
                    }

                    this.trackedMessages.set(j, lastseentrackedentry.acknowledge());
                    objectlist.add(lastseentrackedentry.signature());
                } else {
                    if (lastseentrackedentry != null && !lastseentrackedentry.pending()) {
                        throw new LastSeenMessagesValidator.a("Last seen update ignored previously acknowledged message at index " + j + " and signature " + String.valueOf(lastseentrackedentry.signature()));
                    }

                    this.trackedMessages.set(j, (Object) null);
                }
            }

            LastSeenMessages lastseenmessages = new LastSeenMessages(objectlist);

            if (!lastseenmessages_b.verifyChecksum(lastseenmessages)) {
                throw new LastSeenMessagesValidator.a("Checksum mismatch on last seen update: the client and server must have desynced");
            } else {
                return lastseenmessages;
            }
        }
    }

    public static class a extends Exception {

        public a(String s) {
            super(s);
        }
    }
}
