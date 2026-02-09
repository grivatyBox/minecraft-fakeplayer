package net.minecraft.world.level;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportSystemDetails;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.UtilColor;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3D;

public abstract class CommandBlockListenerAbstract implements ICommandListener {

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final IChatBaseComponent DEFAULT_NAME = IChatBaseComponent.literal("@");
    private static final int NO_LAST_EXECUTION = -1;
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    @Nullable
    private IChatBaseComponent lastOutput;
    private String command = "";
    @Nullable
    private IChatBaseComponent customName;

    public CommandBlockListenerAbstract() {}

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int i) {
        this.successCount = i;
    }

    public IChatBaseComponent getLastOutput() {
        return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
    }

    public void save(ValueOutput valueoutput) {
        valueoutput.putString("Command", this.command);
        valueoutput.putInt("SuccessCount", this.successCount);
        valueoutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        valueoutput.putBoolean("TrackOutput", this.trackOutput);
        if (this.trackOutput) {
            valueoutput.storeNullable("LastOutput", ComponentSerialization.CODEC, this.lastOutput);
        }

        valueoutput.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution != -1L) {
            valueoutput.putLong("LastExecution", this.lastExecution);
        }

    }

    public void load(ValueInput valueinput) {
        this.command = valueinput.getStringOr("Command", "");
        this.successCount = valueinput.getIntOr("SuccessCount", 0);
        this.setCustomName(TileEntity.parseCustomNameSafe(valueinput, "CustomName"));
        this.trackOutput = valueinput.getBooleanOr("TrackOutput", true);
        if (this.trackOutput) {
            this.lastOutput = TileEntity.parseCustomNameSafe(valueinput, "LastOutput");
        } else {
            this.lastOutput = null;
        }

        this.updateLastExecution = valueinput.getBooleanOr("UpdateLastExecution", true);
        if (this.updateLastExecution) {
            this.lastExecution = valueinput.getLongOr("LastExecution", -1L);
        } else {
            this.lastExecution = -1L;
        }

    }

    public void setCommand(String s) {
        this.command = s;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(World world) {
        if (!world.isClientSide && world.getGameTime() != this.lastExecution) {
            if ("Searge".equalsIgnoreCase(this.command)) {
                this.lastOutput = IChatBaseComponent.literal("#itzlipofutzli");
                this.successCount = 1;
                return true;
            } else {
                this.successCount = 0;
                MinecraftServer minecraftserver = this.getLevel().getServer();

                if (minecraftserver.isCommandBlockEnabled() && !UtilColor.isNullOrEmpty(this.command)) {
                    try {
                        this.lastOutput = null;
                        CommandListenerWrapper commandlistenerwrapper = this.createCommandSourceStack().withCallback((flag, i) -> {
                            if (flag) {
                                ++this.successCount;
                            }

                        });

                        minecraftserver.getCommands().performPrefixedCommand(commandlistenerwrapper, this.command);
                    } catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Executing command block");
                        CrashReportSystemDetails crashreportsystemdetails = crashreport.addCategory("Command to be executed");

                        crashreportsystemdetails.setDetail("Command", this::getCommand);
                        crashreportsystemdetails.setDetail("Name", () -> {
                            return this.getName().getString();
                        });
                        throw new ReportedException(crashreport);
                    }
                }

                if (this.updateLastExecution) {
                    this.lastExecution = world.getGameTime();
                } else {
                    this.lastExecution = -1L;
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public IChatBaseComponent getName() {
        return this.customName != null ? this.customName : CommandBlockListenerAbstract.DEFAULT_NAME;
    }

    @Nullable
    public IChatBaseComponent getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.customName = ichatbasecomponent;
    }

    @Override
    public void sendSystemMessage(IChatBaseComponent ichatbasecomponent) {
        if (this.trackOutput) {
            SimpleDateFormat simpledateformat = CommandBlockListenerAbstract.TIME_FORMAT;
            Date date = new Date();

            this.lastOutput = IChatBaseComponent.literal("[" + simpledateformat.format(date) + "] ").append(ichatbasecomponent);
            this.onUpdated();
        }

    }

    public abstract WorldServer getLevel();

    public abstract void onUpdated();

    public void setLastOutput(@Nullable IChatBaseComponent ichatbasecomponent) {
        this.lastOutput = ichatbasecomponent;
    }

    public void setTrackOutput(boolean flag) {
        this.trackOutput = flag;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public EnumInteractionResult usedBy(EntityHuman entityhuman) {
        if (!entityhuman.canUseGameMasterBlocks()) {
            return EnumInteractionResult.PASS;
        } else {
            if (entityhuman.level().isClientSide) {
                entityhuman.openMinecartCommandBlock(this);
            }

            return EnumInteractionResult.SUCCESS;
        }
    }

    public abstract Vec3D getPosition();

    public abstract CommandListenerWrapper createCommandSourceStack();

    @Override
    public boolean acceptsSuccess() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
    }

    @Override
    public boolean acceptsFailure() {
        return this.trackOutput;
    }

    @Override
    public boolean shouldInformAdmins() {
        return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
    }

    public abstract boolean isValid();
}
