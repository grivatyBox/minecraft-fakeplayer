package net.minecraft.network;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;

public interface PacketListener {

    EnumProtocolDirection flow();

    EnumProtocol protocol();

    void onDisconnect(IChatBaseComponent ichatbasecomponent);

    boolean isAcceptingMessages();

    default boolean shouldHandleMessage(Packet<?> packet) {
        return this.isAcceptingMessages();
    }

    default boolean shouldPropagateHandlingExceptions() {
        return true;
    }

    default void fillCrashReport(CrashReport crashreport) {
        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Connection");

        crashreportsystemdetails.setDetail("Protocol", () -> {
            return this.protocol().id();
        });
        crashreportsystemdetails.setDetail("Flow", () -> {
            return this.flow().toString();
        });
        this.fillListenerSpecificCrashDetails(crashreportsystemdetails);
    }

    default void fillListenerSpecificCrashDetails(CrashReportSystemDetails crashreportsystemdetails) {}
}
