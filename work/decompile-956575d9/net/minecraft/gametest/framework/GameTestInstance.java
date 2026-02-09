package net.minecraft.gametest.framework;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.EnumChatFormat;
import net.minecraft.core.Holder;
import net.minecraft.core.IRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.EnumBlockRotation;

public abstract class GameTestInstance {

    public static final Codec<GameTestInstance> DIRECT_CODEC = BuiltInRegistries.TEST_INSTANCE_TYPE.byNameCodec().dispatch(GameTestInstance::codec, (mapcodec) -> {
        return mapcodec;
    });
    private final TestData<Holder<TestEnvironmentDefinition>> info;

    public static MapCodec<? extends GameTestInstance> bootstrap(IRegistry<MapCodec<? extends GameTestInstance>> iregistry) {
        register(iregistry, "block_based", BlockBasedTestInstance.CODEC);
        return register(iregistry, "function", FunctionGameTestInstance.CODEC);
    }

    private static MapCodec<? extends GameTestInstance> register(IRegistry<MapCodec<? extends GameTestInstance>> iregistry, String s, MapCodec<? extends GameTestInstance> mapcodec) {
        return (MapCodec) IRegistry.register(iregistry, ResourceKey.create(Registries.TEST_INSTANCE_TYPE, MinecraftKey.withDefaultNamespace(s)), mapcodec);
    }

    protected GameTestInstance(TestData<Holder<TestEnvironmentDefinition>> testdata) {
        this.info = testdata;
    }

    public abstract void run(GameTestHarnessHelper gametestharnesshelper);

    public abstract MapCodec<? extends GameTestInstance> codec();

    public Holder<TestEnvironmentDefinition> batch() {
        return this.info.environment();
    }

    public MinecraftKey structure() {
        return this.info.structure();
    }

    public int maxTicks() {
        return this.info.maxTicks();
    }

    public int setupTicks() {
        return this.info.setupTicks();
    }

    public boolean required() {
        return this.info.required();
    }

    public boolean manualOnly() {
        return this.info.manualOnly();
    }

    public int maxAttempts() {
        return this.info.maxAttempts();
    }

    public int requiredSuccesses() {
        return this.info.requiredSuccesses();
    }

    public boolean skyAccess() {
        return this.info.skyAccess();
    }

    public EnumBlockRotation rotation() {
        return this.info.rotation();
    }

    protected TestData<Holder<TestEnvironmentDefinition>> info() {
        return this.info;
    }

    protected abstract IChatMutableComponent typeDescription();

    public IChatBaseComponent describe() {
        return this.describeType().append(this.describeInfo());
    }

    protected IChatMutableComponent describeType() {
        return this.descriptionRow("test_instance.description.type", this.typeDescription());
    }

    protected IChatBaseComponent describeInfo() {
        return this.descriptionRow("test_instance.description.structure", this.info.structure().toString()).append((IChatBaseComponent) this.descriptionRow("test_instance.description.batch", ((Holder) this.info.environment()).getRegisteredName()));
    }

    protected IChatMutableComponent descriptionRow(String s, String s1) {
        return this.descriptionRow(s, IChatBaseComponent.literal(s1));
    }

    protected IChatMutableComponent descriptionRow(String s, IChatMutableComponent ichatmutablecomponent) {
        return IChatBaseComponent.translatable(s, ichatmutablecomponent.withStyle(EnumChatFormat.BLUE)).append((IChatBaseComponent) IChatBaseComponent.literal("\n"));
    }
}
