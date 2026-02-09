package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PlayerAbilities {

    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULY_FLYING = false;
    private static final boolean DEFAULT_MAY_FLY = false;
    private static final boolean DEFAULT_INSTABUILD = false;
    private static final boolean DEFAULT_MAY_BUILD = true;
    private static final float DEFAULT_FLYING_SPEED = 0.05F;
    private static final float DEFAULT_WALKING_SPEED = 0.1F;
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    public float flyingSpeed = 0.05F;
    public float walkingSpeed = 0.1F;

    public PlayerAbilities() {}

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float f) {
        this.flyingSpeed = f;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float f) {
        this.walkingSpeed = f;
    }

    public PlayerAbilities.a pack() {
        return new PlayerAbilities.a(this.invulnerable, this.flying, this.mayfly, this.instabuild, this.mayBuild, this.flyingSpeed, this.walkingSpeed);
    }

    public void apply(PlayerAbilities.a playerabilities_a) {
        this.invulnerable = playerabilities_a.invulnerable;
        this.flying = playerabilities_a.flying;
        this.mayfly = playerabilities_a.mayFly;
        this.instabuild = playerabilities_a.instabuild;
        this.mayBuild = playerabilities_a.mayBuild;
        this.flyingSpeed = playerabilities_a.flyingSpeed;
        this.walkingSpeed = playerabilities_a.walkingSpeed;
    }

    public static record a(boolean invulnerable, boolean flying, boolean mayFly, boolean instabuild, boolean mayBuild, float flyingSpeed, float walkingSpeed) {

        public static final Codec<PlayerAbilities.a> CODEC = RecordCodecBuilder.create((instance) -> {
            return instance.group(Codec.BOOL.fieldOf("invulnerable").orElse(false).forGetter(PlayerAbilities.a::invulnerable), Codec.BOOL.fieldOf("flying").orElse(false).forGetter(PlayerAbilities.a::flying), Codec.BOOL.fieldOf("mayfly").orElse(false).forGetter(PlayerAbilities.a::mayFly), Codec.BOOL.fieldOf("instabuild").orElse(false).forGetter(PlayerAbilities.a::instabuild), Codec.BOOL.fieldOf("mayBuild").orElse(true).forGetter(PlayerAbilities.a::mayBuild), Codec.FLOAT.fieldOf("flySpeed").orElse(0.05F).forGetter(PlayerAbilities.a::flyingSpeed), Codec.FLOAT.fieldOf("walkSpeed").orElse(0.1F).forGetter(PlayerAbilities.a::walkingSpeed)).apply(instance, PlayerAbilities.a::new);
        });
    }
}
