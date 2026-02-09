package net.minecraft.world.entity.decoration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityPose;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumMainHand;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.World;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Mannequin extends Avatar {

    protected static final DataWatcherObject<ResolvableProfile> DATA_PROFILE = DataWatcher.<ResolvableProfile>defineId(Mannequin.class, DataWatcherRegistry.RESOLVABLE_PROFILE);
    private static final DataWatcherObject<Boolean> DATA_IMMOVABLE = DataWatcher.<Boolean>defineId(Mannequin.class, DataWatcherRegistry.BOOLEAN);
    private static final DataWatcherObject<Optional<IChatBaseComponent>> DATA_DESCRIPTION = DataWatcher.<Optional<IChatBaseComponent>>defineId(Mannequin.class, DataWatcherRegistry.OPTIONAL_COMPONENT);
    private static final byte ALL_LAYERS = (byte) Arrays.stream(PlayerModelPart.values()).mapToInt(PlayerModelPart::getMask).reduce(0, (i, j) -> {
        return i | j;
    });
    public static final Set<EntityPose> VALID_POSES = Set.of(EntityPose.STANDING, EntityPose.CROUCHING, EntityPose.SWIMMING, EntityPose.FALL_FLYING, EntityPose.SLEEPING);
    public static final Codec<EntityPose> POSE_CODEC = EntityPose.CODEC.validate((entitypose) -> {
        return Mannequin.VALID_POSES.contains(entitypose) ? DataResult.success(entitypose) : DataResult.error(() -> {
            return "Invalid pose: " + entitypose.getSerializedName();
        });
    });
    private static final Codec<Byte> LAYERS_CODEC = PlayerModelPart.CODEC.listOf().xmap((list) -> {
        return (byte) list.stream().mapToInt(PlayerModelPart::getMask).reduce(Mannequin.ALL_LAYERS, (i, j) -> {
            return i & ~j;
        });
    }, (obyte) -> {
        return Arrays.stream(PlayerModelPart.values()).filter((playermodelpart) -> {
            return (obyte & playermodelpart.getMask()) == 0;
        }).toList();
    });
    public static final ResolvableProfile DEFAULT_PROFILE = ResolvableProfile.Static.EMPTY;
    public static final IChatBaseComponent DEFAULT_DESCRIPTION = IChatBaseComponent.translatable("entity.minecraft.mannequin.label");
    protected static EntityTypes.b<Mannequin> constructor = Mannequin::new;
    private static final String PROFILE_FIELD = "profile";
    private static final String HIDDEN_LAYERS_FIELD = "hidden_layers";
    private static final String MAIN_HAND_FIELD = "main_hand";
    private static final String POSE_FIELD = "pose";
    private static final String IMMOVABLE_FIELD = "immovable";
    private static final String DESCRIPTION_FIELD = "description";
    private static final String HIDE_DESCRIPTION_FIELD = "hide_description";
    public IChatBaseComponent description;
    public boolean hideDescription;

    public Mannequin(EntityTypes<Mannequin> entitytypes, World world) {
        super(entitytypes, world);
        this.description = Mannequin.DEFAULT_DESCRIPTION;
        this.hideDescription = false;
        this.entityData.set(Mannequin.DATA_PLAYER_MODE_CUSTOMISATION, Mannequin.ALL_LAYERS);
    }

    protected Mannequin(World world) {
        this(EntityTypes.MANNEQUIN, world);
    }

    @Nullable
    public static Mannequin create(EntityTypes<Mannequin> entitytypes, World world) {
        return Mannequin.constructor.create(entitytypes, world);
    }

    @Override
    protected void defineSynchedData(DataWatcher.a datawatcher_a) {
        super.defineSynchedData(datawatcher_a);
        datawatcher_a.define(Mannequin.DATA_PROFILE, Mannequin.DEFAULT_PROFILE);
        datawatcher_a.define(Mannequin.DATA_IMMOVABLE, false);
        datawatcher_a.define(Mannequin.DATA_DESCRIPTION, Optional.of(Mannequin.DEFAULT_DESCRIPTION));
    }

    public ResolvableProfile getProfile() {
        return (ResolvableProfile) this.entityData.get(Mannequin.DATA_PROFILE);
    }

    public void setProfile(ResolvableProfile resolvableprofile) {
        this.entityData.set(Mannequin.DATA_PROFILE, resolvableprofile);
    }

    public boolean getImmovable() {
        return (Boolean) this.entityData.get(Mannequin.DATA_IMMOVABLE);
    }

    public void setImmovable(boolean flag) {
        this.entityData.set(Mannequin.DATA_IMMOVABLE, flag);
    }

    @Nullable
    public IChatBaseComponent getDescription() {
        return (IChatBaseComponent) ((Optional) this.entityData.get(Mannequin.DATA_DESCRIPTION)).orElse((Object) null);
    }

    public void setDescription(IChatBaseComponent ichatbasecomponent) {
        this.description = ichatbasecomponent;
        this.updateDescription();
    }

    public void setHideDescription(boolean flag) {
        this.hideDescription = flag;
        this.updateDescription();
    }

    private void updateDescription() {
        this.entityData.set(Mannequin.DATA_DESCRIPTION, this.hideDescription ? Optional.empty() : Optional.of(this.description));
    }

    @Override
    protected boolean isImmobile() {
        return this.getImmovable() || super.isImmobile();
    }

    @Override
    public boolean isEffectiveAi() {
        return !this.getImmovable() && super.isEffectiveAi();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueoutput) {
        super.addAdditionalSaveData(valueoutput);
        valueoutput.store("profile", ResolvableProfile.CODEC, this.getProfile());
        valueoutput.store("hidden_layers", Mannequin.LAYERS_CODEC, (Byte) this.entityData.get(Mannequin.DATA_PLAYER_MODE_CUSTOMISATION));
        valueoutput.store("main_hand", EnumMainHand.CODEC, this.getMainArm());
        valueoutput.store("pose", Mannequin.POSE_CODEC, this.getPose());
        valueoutput.putBoolean("immovable", this.getImmovable());
        IChatBaseComponent ichatbasecomponent = this.getDescription();

        if (ichatbasecomponent != null) {
            if (!ichatbasecomponent.equals(Mannequin.DEFAULT_DESCRIPTION)) {
                valueoutput.store("description", ComponentSerialization.CODEC, ichatbasecomponent);
            }
        } else {
            valueoutput.putBoolean("hide_description", true);
        }

    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueinput) {
        super.readAdditionalSaveData(valueinput);
        valueinput.read("profile", ResolvableProfile.CODEC).ifPresent(this::setProfile);
        this.entityData.set(Mannequin.DATA_PLAYER_MODE_CUSTOMISATION, (Byte) valueinput.read("hidden_layers", Mannequin.LAYERS_CODEC).orElse(Mannequin.ALL_LAYERS));
        this.setMainArm((EnumMainHand) valueinput.read("main_hand", EnumMainHand.CODEC).orElse(Mannequin.DEFAULT_MAIN_HAND));
        this.setPose((EntityPose) valueinput.read("pose", Mannequin.POSE_CODEC).orElse(EntityPose.STANDING));
        this.setImmovable(valueinput.getBooleanOr("immovable", false));
        this.setHideDescription(valueinput.getBooleanOr("hide_description", false));
        this.setDescription((IChatBaseComponent) valueinput.read("description", ComponentSerialization.CODEC).orElse(Mannequin.DEFAULT_DESCRIPTION));
    }

    @Nullable
    @Override
    public <T> T get(DataComponentType<? extends T> datacomponenttype) {
        return (T) (datacomponenttype == DataComponents.PROFILE ? castComponentValue(datacomponenttype, this.getProfile()) : super.get(datacomponenttype));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter datacomponentgetter) {
        this.applyImplicitComponentIfPresent(datacomponentgetter, DataComponents.PROFILE);
        super.applyImplicitComponents(datacomponentgetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> datacomponenttype, T t0) {
        if (datacomponenttype == DataComponents.PROFILE) {
            this.setProfile((ResolvableProfile) castComponentValue(DataComponents.PROFILE, t0));
            return true;
        } else {
            return super.applyImplicitComponent(datacomponenttype, t0);
        }
    }
}
