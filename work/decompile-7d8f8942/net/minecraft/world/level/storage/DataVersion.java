package net.minecraft.world.level.storage;

public record DataVersion(int version, String series) {

    public static final String MAIN_SERIES = "main";

    public boolean isSideSeries() {
        return !this.series.equals("main");
    }

    public boolean isCompatible(DataVersion dataversion) {
        return this.series().equals(dataversion.series());
    }
}
