package net.minecraft.world.entity.ai.attributes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.resources.MinecraftKey;

public class AttributeModifiable {

    private static final String BASE_FIELD = "base";
    private static final String MODIFIERS_FIELD = "modifiers";
    public static final String ID_FIELD = "id";
    public static final Codec<Holder<AttributeBase>> TYPE_CODEC = BuiltInRegistries.ATTRIBUTE.holderByNameCodec();
    private final Holder<AttributeBase> attribute;
    private final Map<AttributeModifier.Operation, Map<MinecraftKey, AttributeModifier>> modifiersByOperation = Maps.newEnumMap(AttributeModifier.Operation.class);
    private final Map<MinecraftKey, AttributeModifier> modifierById = new Object2ObjectArrayMap();
    private final Map<MinecraftKey, AttributeModifier> permanentModifiers = new Object2ObjectArrayMap();
    private double baseValue;
    private boolean dirty = true;
    private double cachedValue;
    private final Consumer<AttributeModifiable> onDirty;

    public AttributeModifiable(Holder<AttributeBase> holder, Consumer<AttributeModifiable> consumer) {
        this.attribute = holder;
        this.onDirty = consumer;
        this.baseValue = ((AttributeBase) holder.value()).getDefaultValue();
    }

    public Holder<AttributeBase> getAttribute() {
        return this.attribute;
    }

    public double getBaseValue() {
        return this.baseValue;
    }

    public void setBaseValue(double d0) {
        if (d0 != this.baseValue) {
            this.baseValue = d0;
            this.setDirty();
        }
    }

    @VisibleForTesting
    Map<MinecraftKey, AttributeModifier> getModifiers(AttributeModifier.Operation attributemodifier_operation) {
        return (Map) this.modifiersByOperation.computeIfAbsent(attributemodifier_operation, (attributemodifier_operation1) -> {
            return new Object2ObjectOpenHashMap();
        });
    }

    public Set<AttributeModifier> getModifiers() {
        return ImmutableSet.copyOf(this.modifierById.values());
    }

    public Set<AttributeModifier> getPermanentModifiers() {
        return ImmutableSet.copyOf(this.permanentModifiers.values());
    }

    @Nullable
    public AttributeModifier getModifier(MinecraftKey minecraftkey) {
        return (AttributeModifier) this.modifierById.get(minecraftkey);
    }

    public boolean hasModifier(MinecraftKey minecraftkey) {
        return this.modifierById.get(minecraftkey) != null;
    }

    private void addModifier(AttributeModifier attributemodifier) {
        AttributeModifier attributemodifier1 = (AttributeModifier) this.modifierById.putIfAbsent(attributemodifier.id(), attributemodifier);

        if (attributemodifier1 != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        } else {
            this.getModifiers(attributemodifier.operation()).put(attributemodifier.id(), attributemodifier);
            this.setDirty();
        }
    }

    public void addOrUpdateTransientModifier(AttributeModifier attributemodifier) {
        AttributeModifier attributemodifier1 = (AttributeModifier) this.modifierById.put(attributemodifier.id(), attributemodifier);

        if (attributemodifier != attributemodifier1) {
            this.getModifiers(attributemodifier.operation()).put(attributemodifier.id(), attributemodifier);
            this.setDirty();
        }
    }

    public void addTransientModifier(AttributeModifier attributemodifier) {
        this.addModifier(attributemodifier);
    }

    public void addOrReplacePermanentModifier(AttributeModifier attributemodifier) {
        this.removeModifier(attributemodifier.id());
        this.addModifier(attributemodifier);
        this.permanentModifiers.put(attributemodifier.id(), attributemodifier);
    }

    public void addPermanentModifier(AttributeModifier attributemodifier) {
        this.addModifier(attributemodifier);
        this.permanentModifiers.put(attributemodifier.id(), attributemodifier);
    }

    public void addPermanentModifiers(Collection<AttributeModifier> collection) {
        for (AttributeModifier attributemodifier : collection) {
            this.addPermanentModifier(attributemodifier);
        }

    }

    protected void setDirty() {
        this.dirty = true;
        this.onDirty.accept(this);
    }

    public void removeModifier(AttributeModifier attributemodifier) {
        this.removeModifier(attributemodifier.id());
    }

    public boolean removeModifier(MinecraftKey minecraftkey) {
        AttributeModifier attributemodifier = (AttributeModifier) this.modifierById.remove(minecraftkey);

        if (attributemodifier == null) {
            return false;
        } else {
            this.getModifiers(attributemodifier.operation()).remove(minecraftkey);
            this.permanentModifiers.remove(minecraftkey);
            this.setDirty();
            return true;
        }
    }

    public void removeModifiers() {
        for (AttributeModifier attributemodifier : this.getModifiers()) {
            this.removeModifier(attributemodifier);
        }

    }

    public double getValue() {
        if (this.dirty) {
            this.cachedValue = this.calculateValue();
            this.dirty = false;
        }

        return this.cachedValue;
    }

    private double calculateValue() {
        double d0 = this.getBaseValue();

        for (AttributeModifier attributemodifier : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_VALUE)) {
            d0 += attributemodifier.amount();
        }

        double d1 = d0;

        for (AttributeModifier attributemodifier1 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
            d1 += d0 * attributemodifier1.amount();
        }

        for (AttributeModifier attributemodifier2 : this.getModifiersOrEmpty(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
            d1 *= 1.0D + attributemodifier2.amount();
        }

        return ((AttributeBase) this.attribute.value()).sanitizeValue(d1);
    }

    private Collection<AttributeModifier> getModifiersOrEmpty(AttributeModifier.Operation attributemodifier_operation) {
        return ((Map) this.modifiersByOperation.getOrDefault(attributemodifier_operation, Map.of())).values();
    }

    public void replaceFrom(AttributeModifiable attributemodifiable) {
        this.baseValue = attributemodifiable.baseValue;
        this.modifierById.clear();
        this.modifierById.putAll(attributemodifiable.modifierById);
        this.permanentModifiers.clear();
        this.permanentModifiers.putAll(attributemodifiable.permanentModifiers);
        this.modifiersByOperation.clear();
        attributemodifiable.modifiersByOperation.forEach((attributemodifier_operation, map) -> {
            this.getModifiers(attributemodifier_operation).putAll(map);
        });
        this.setDirty();
    }

    public NBTTagCompound save() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.store("id", AttributeModifiable.TYPE_CODEC, this.attribute);
        nbttagcompound.putDouble("base", this.baseValue);
        if (!this.permanentModifiers.isEmpty()) {
            nbttagcompound.store("modifiers", AttributeModifier.CODEC.listOf(), List.copyOf(this.permanentModifiers.values()));
        }

        return nbttagcompound;
    }

    public void load(NBTTagCompound nbttagcompound) {
        this.baseValue = nbttagcompound.getDoubleOr("base", 0.0D);

        for (AttributeModifier attributemodifier : (List) nbttagcompound.read("modifiers", AttributeModifier.CODEC.listOf()).orElse(List.of())) {
            this.modifierById.put(attributemodifier.id(), attributemodifier);
            this.getModifiers(attributemodifier.operation()).put(attributemodifier.id(), attributemodifier);
            this.permanentModifiers.put(attributemodifier.id(), attributemodifier);
        }

        this.setDirty();
    }
}
