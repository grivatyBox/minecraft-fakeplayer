package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class PacketPlayOutScoreboardObjective implements Packet<PacketListenerPlayOut> {

    public static final int METHOD_ADD = 0;
    public static final int METHOD_REMOVE = 1;
    public static final int METHOD_CHANGE = 2;
    private final String objectiveName;
    private final IChatBaseComponent displayName;
    private final IScoreboardCriteria.EnumScoreboardHealthDisplay renderType;
    @Nullable
    private final NumberFormat numberFormat;
    private final int method;

    public PacketPlayOutScoreboardObjective(ScoreboardObjective scoreboardobjective, int i) {
        this.objectiveName = scoreboardobjective.getName();
        this.displayName = scoreboardobjective.getDisplayName();
        this.renderType = scoreboardobjective.getRenderType();
        this.numberFormat = scoreboardobjective.numberFormat();
        this.method = i;
    }

    public PacketPlayOutScoreboardObjective(PacketDataSerializer packetdataserializer) {
        this.objectiveName = packetdataserializer.readUtf();
        this.method = packetdataserializer.readByte();
        if (this.method != 0 && this.method != 2) {
            this.displayName = CommonComponents.EMPTY;
            this.renderType = IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER;
            this.numberFormat = null;
        } else {
            this.displayName = packetdataserializer.readComponentTrusted();
            this.renderType = (IScoreboardCriteria.EnumScoreboardHealthDisplay) packetdataserializer.readEnum(IScoreboardCriteria.EnumScoreboardHealthDisplay.class);
            this.numberFormat = (NumberFormat) packetdataserializer.readNullable(NumberFormatTypes::readFromStream);
        }

    }

    @Override
    public void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeUtf(this.objectiveName);
        packetdataserializer.writeByte(this.method);
        if (this.method == 0 || this.method == 2) {
            packetdataserializer.writeComponent(this.displayName);
            packetdataserializer.writeEnum(this.renderType);
            packetdataserializer.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
        }

    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleAddObjective(this);
    }

    public String getObjectiveName() {
        return this.objectiveName;
    }

    public IChatBaseComponent getDisplayName() {
        return this.displayName;
    }

    public int getMethod() {
        return this.method;
    }

    public IScoreboardCriteria.EnumScoreboardHealthDisplay getRenderType() {
        return this.renderType;
    }

    @Nullable
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }
}
