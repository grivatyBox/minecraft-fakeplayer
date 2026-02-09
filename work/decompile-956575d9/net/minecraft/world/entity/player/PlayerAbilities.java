package net.minecraft.world.entity.player;

import net.minecraft.nbt.NBTTagCompound;

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

    public void addSaveData(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        nbttagcompound1.putBoolean("invulnerable", this.invulnerable);
        nbttagcompound1.putBoolean("flying", this.flying);
        nbttagcompound1.putBoolean("mayfly", this.mayfly);
        nbttagcompound1.putBoolean("instabuild", this.instabuild);
        nbttagcompound1.putBoolean("mayBuild", this.mayBuild);
        nbttagcompound1.putFloat("flySpeed", this.flyingSpeed);
        nbttagcompound1.putFloat("walkSpeed", this.walkingSpeed);
        nbttagcompound.put("abilities", nbttagcompound1);
    }

    public void loadSaveData(NBTTagCompound nbttagcompound) {
        NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundOrEmpty("abilities");

        this.invulnerable = nbttagcompound1.getBooleanOr("invulnerable", false);
        this.flying = nbttagcompound1.getBooleanOr("flying", false);
        this.mayfly = nbttagcompound1.getBooleanOr("mayfly", false);
        this.instabuild = nbttagcompound1.getBooleanOr("instabuild", false);
        this.flyingSpeed = nbttagcompound1.getFloatOr("flySpeed", 0.05F);
        this.walkingSpeed = nbttagcompound1.getFloatOr("walkSpeed", 0.1F);
        this.mayBuild = nbttagcompound1.getBooleanOr("mayBuild", true);
    }

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
}
