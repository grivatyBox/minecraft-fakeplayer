package net.minecraft.network.protocol;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketProcessor;
import net.minecraft.server.CancelledPacketHandleException;
import net.minecraft.server.level.WorldServer;
import org.slf4j.Logger;

public class PlayerConnectionUtils {

    private static final Logger LOGGER = LogUtils.getLogger();

    public PlayerConnectionUtils() {}

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t0, WorldServer worldserver) throws CancelledPacketHandleException {
        ensureRunningOnSameThread(packet, t0, worldserver.getServer().packetProcessor());
    }

    public static <T extends PacketListener> void ensureRunningOnSameThread(Packet<T> packet, T t0, PacketProcessor packetprocessor) throws CancelledPacketHandleException {
        if (!packetprocessor.isSameThread()) {
            packetprocessor.scheduleIfPossible(t0, packet);
            throw CancelledPacketHandleException.RUNNING_ON_DIFFERENT_THREAD;
        }
    }

    public static <T extends PacketListener> ReportedException makeReportedException(Exception exception, Packet<T> packet, T t0) {
        if (exception instanceof ReportedException reportedexception) {
            fillCrashReport(reportedexception.getReport(), t0, packet);
            return reportedexception;
        } else {
            CrashReport crashreport = CrashReport.forThrowable(exception, "Main thread packet handler");

            fillCrashReport(crashreport, t0, packet);
            return new ReportedException(crashreport);
        }
    }

    public static <T extends PacketListener> void fillCrashReport(CrashReport crashreport, T t0, @Nullable Packet<T> packet) {
        if (packet != null) {
            CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Incoming Packet");

            crashreportsystemdetails.setDetail("Type", () -> {
                return packet.type().toString();
            });
            crashreportsystemdetails.setDetail("Is Terminal", () -> {
                return Boolean.toString(packet.isTerminal());
            });
            crashreportsystemdetails.setDetail("Is Skippable", () -> {
                return Boolean.toString(packet.isSkippable());
            });
        }

        t0.fillCrashReport(crashreport);
    }
}
