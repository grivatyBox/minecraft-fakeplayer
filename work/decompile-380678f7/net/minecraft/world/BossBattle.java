package net.minecraft.world;

import com.mojang.serialization.Codec;
import java.util.UUID;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.util.INamable;

public abstract class BossBattle {

    private final UUID id;
    public IChatBaseComponent name;
    protected float progress;
    public BossBattle.BarColor color;
    public BossBattle.BarStyle overlay;
    protected boolean darkenScreen;
    protected boolean playBossMusic;
    protected boolean createWorldFog;

    public BossBattle(UUID uuid, IChatBaseComponent ichatbasecomponent, BossBattle.BarColor bossbattle_barcolor, BossBattle.BarStyle bossbattle_barstyle) {
        this.id = uuid;
        this.name = ichatbasecomponent;
        this.color = bossbattle_barcolor;
        this.overlay = bossbattle_barstyle;
        this.progress = 1.0F;
    }

    public UUID getId() {
        return this.id;
    }

    public IChatBaseComponent getName() {
        return this.name;
    }

    public void setName(IChatBaseComponent ichatbasecomponent) {
        this.name = ichatbasecomponent;
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float f) {
        this.progress = f;
    }

    public BossBattle.BarColor getColor() {
        return this.color;
    }

    public void setColor(BossBattle.BarColor bossbattle_barcolor) {
        this.color = bossbattle_barcolor;
    }

    public BossBattle.BarStyle getOverlay() {
        return this.overlay;
    }

    public void setOverlay(BossBattle.BarStyle bossbattle_barstyle) {
        this.overlay = bossbattle_barstyle;
    }

    public boolean shouldDarkenScreen() {
        return this.darkenScreen;
    }

    public BossBattle setDarkenScreen(boolean flag) {
        this.darkenScreen = flag;
        return this;
    }

    public boolean shouldPlayBossMusic() {
        return this.playBossMusic;
    }

    public BossBattle setPlayBossMusic(boolean flag) {
        this.playBossMusic = flag;
        return this;
    }

    public BossBattle setCreateWorldFog(boolean flag) {
        this.createWorldFog = flag;
        return this;
    }

    public boolean shouldCreateWorldFog() {
        return this.createWorldFog;
    }

    public static enum BarColor implements INamable {

        PINK("pink", EnumChatFormat.RED), BLUE("blue", EnumChatFormat.BLUE), RED("red", EnumChatFormat.DARK_RED), GREEN("green", EnumChatFormat.GREEN), YELLOW("yellow", EnumChatFormat.YELLOW), PURPLE("purple", EnumChatFormat.DARK_BLUE), WHITE("white", EnumChatFormat.WHITE);

        public static final Codec<BossBattle.BarColor> CODEC = INamable.<BossBattle.BarColor>fromEnum(BossBattle.BarColor::values);
        private final String name;
        private final EnumChatFormat formatting;

        private BarColor(final String s, final EnumChatFormat enumchatformat) {
            this.name = s;
            this.formatting = enumchatformat;
        }

        public EnumChatFormat getFormatting() {
            return this.formatting;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static enum BarStyle implements INamable {

        PROGRESS("progress"), NOTCHED_6("notched_6"), NOTCHED_10("notched_10"), NOTCHED_12("notched_12"), NOTCHED_20("notched_20");

        public static final Codec<BossBattle.BarStyle> CODEC = INamable.<BossBattle.BarStyle>fromEnum(BossBattle.BarStyle::values);
        private final String name;

        private BarStyle(final String s) {
            this.name = s;
        }

        public String getName() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
