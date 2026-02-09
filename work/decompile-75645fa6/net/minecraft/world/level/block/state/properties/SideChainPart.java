package net.minecraft.world.level.block.state.properties;

import net.minecraft.util.INamable;

public enum SideChainPart implements INamable {

    UNCONNECTED("unconnected"), RIGHT("right"), CENTER("center"), LEFT("left");

    private final String name;

    private SideChainPart(final String s) {
        this.name = s;
    }

    public String toString() {
        return this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this != SideChainPart.UNCONNECTED;
    }

    public boolean isConnectionTowards(SideChainPart sidechainpart) {
        return this == SideChainPart.CENTER || this == sidechainpart;
    }

    public boolean isChainEnd() {
        return this != SideChainPart.CENTER;
    }

    public SideChainPart whenConnectedToTheRight() {
        SideChainPart sidechainpart;

        switch (this.ordinal()) {
            case 0:
            case 3:
                sidechainpart = SideChainPart.LEFT;
                break;
            case 1:
            case 2:
                sidechainpart = SideChainPart.CENTER;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return sidechainpart;
    }

    public SideChainPart whenConnectedToTheLeft() {
        SideChainPart sidechainpart;

        switch (this.ordinal()) {
            case 0:
            case 1:
                sidechainpart = SideChainPart.RIGHT;
                break;
            case 2:
            case 3:
                sidechainpart = SideChainPart.CENTER;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return sidechainpart;
    }

    public SideChainPart whenDisconnectedFromTheRight() {
        SideChainPart sidechainpart;

        switch (this.ordinal()) {
            case 0:
            case 3:
                sidechainpart = SideChainPart.UNCONNECTED;
                break;
            case 1:
            case 2:
                sidechainpart = SideChainPart.RIGHT;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return sidechainpart;
    }

    public SideChainPart whenDisconnectedFromTheLeft() {
        SideChainPart sidechainpart;

        switch (this.ordinal()) {
            case 0:
            case 1:
                sidechainpart = SideChainPart.UNCONNECTED;
                break;
            case 2:
            case 3:
                sidechainpart = SideChainPart.LEFT;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return sidechainpart;
    }
}
