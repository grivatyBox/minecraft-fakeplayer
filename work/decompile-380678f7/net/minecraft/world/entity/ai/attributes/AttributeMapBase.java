package net.minecraft.world.entity.ai.attributes;

import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.resources.MinecraftKey;

public class AttributeMapBase {

    private final Map<Holder<AttributeBase>, AttributeModifiable> attributes = new Object2ObjectOpenHashMap();
    private final Set<AttributeModifiable> attributesToSync = new ObjectOpenHashSet();
    private final Set<AttributeModifiable> attributesToUpdate = new ObjectOpenHashSet();
    private final AttributeProvider supplier;

    public AttributeMapBase(AttributeProvider attributeprovider) {
        this.supplier = attributeprovider;
    }

    private void onAttributeModified(AttributeModifiable attributemodifiable) {
        this.attributesToUpdate.add(attributemodifiable);
        if (((AttributeBase) attributemodifiable.getAttribute().value()).isClientSyncable()) {
            this.attributesToSync.add(attributemodifiable);
        }

    }

    public Set<AttributeModifiable> getAttributesToSync() {
        return this.attributesToSync;
    }

    public Set<AttributeModifiable> getAttributesToUpdate() {
        return this.attributesToUpdate;
    }

    public Collection<AttributeModifiable> getSyncableAttributes() {
        return (Collection) this.attributes.values().stream().filter((attributemodifiable) -> {
            return ((AttributeBase) attributemodifiable.getAttribute().value()).isClientSyncable();
        }).collect(Collectors.toList());
    }

    @Nullable
    public AttributeModifiable getInstance(Holder<AttributeBase> holder) {
        return (AttributeModifiable) this.attributes.computeIfAbsent(holder, (holder1) -> {
            return this.supplier.createInstance(this::onAttributeModified, holder1);
        });
    }

    public boolean hasAttribute(Holder<AttributeBase> holder) {
        return this.attributes.get(holder) != null || this.supplier.hasAttribute(holder);
    }

    public boolean hasModifier(Holder<AttributeBase> holder, MinecraftKey minecraftkey) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

        return attributemodifiable != null ? attributemodifiable.getModifier(minecraftkey) != null : this.supplier.hasModifier(holder, minecraftkey);
    }

    public double getValue(Holder<AttributeBase> holder) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

        return attributemodifiable != null ? attributemodifiable.getValue() : this.supplier.getValue(holder);
    }

    public double getBaseValue(Holder<AttributeBase> holder) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

        return attributemodifiable != null ? attributemodifiable.getBaseValue() : this.supplier.getBaseValue(holder);
    }

    public double getModifierValue(Holder<AttributeBase> holder, MinecraftKey minecraftkey) {
        AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

        return attributemodifiable != null ? attributemodifiable.getModifier(minecraftkey).amount() : this.supplier.getModifierValue(holder, minecraftkey);
    }

    public void addTransientAttributeModifiers(Multimap<Holder<AttributeBase>, AttributeModifier> multimap) {
        multimap.forEach((holder, attributemodifier) -> {
            AttributeModifiable attributemodifiable = this.getInstance(holder);

            if (attributemodifiable != null) {
                attributemodifiable.removeModifier(attributemodifier.id());
                attributemodifiable.addTransientModifier(attributemodifier);
            }

        });
    }

    public void removeAttributeModifiers(Multimap<Holder<AttributeBase>, AttributeModifier> multimap) {
        multimap.asMap().forEach((holder, collection) -> {
            AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

            if (attributemodifiable != null) {
                collection.forEach((attributemodifier) -> {
                    attributemodifiable.removeModifier(attributemodifier.id());
                });
            }

        });
    }

    public void assignAllValues(AttributeMapBase attributemapbase) {
        attributemapbase.attributes.values().forEach((attributemodifiable) -> {
            AttributeModifiable attributemodifiable1 = this.getInstance(attributemodifiable.getAttribute());

            if (attributemodifiable1 != null) {
                attributemodifiable1.replaceFrom(attributemodifiable);
            }

        });
    }

    public void assignBaseValues(AttributeMapBase attributemapbase) {
        attributemapbase.attributes.values().forEach((attributemodifiable) -> {
            AttributeModifiable attributemodifiable1 = this.getInstance(attributemodifiable.getAttribute());

            if (attributemodifiable1 != null) {
                attributemodifiable1.setBaseValue(attributemodifiable.getBaseValue());
            }

        });
    }

    public void assignPermanentModifiers(AttributeMapBase attributemapbase) {
        attributemapbase.attributes.values().forEach((attributemodifiable) -> {
            AttributeModifiable attributemodifiable1 = this.getInstance(attributemodifiable.getAttribute());

            if (attributemodifiable1 != null) {
                attributemodifiable1.addPermanentModifiers(attributemodifiable.getPermanentModifiers());
            }

        });
    }

    public boolean resetBaseValue(Holder<AttributeBase> holder) {
        if (!this.supplier.hasAttribute(holder)) {
            return false;
        } else {
            AttributeModifiable attributemodifiable = (AttributeModifiable) this.attributes.get(holder);

            if (attributemodifiable != null) {
                attributemodifiable.setBaseValue(this.supplier.getBaseValue(holder));
            }

            return true;
        }
    }

    public List<AttributeModifiable.a> pack() {
        List<AttributeModifiable.a> list = new ArrayList(this.attributes.values().size());

        for (AttributeModifiable attributemodifiable : this.attributes.values()) {
            list.add(attributemodifiable.pack());
        }

        return list;
    }

    public void apply(List<AttributeModifiable.a> list) {
        for (AttributeModifiable.a attributemodifiable_a : list) {
            AttributeModifiable attributemodifiable = this.getInstance(attributemodifiable_a.attribute());

            if (attributemodifiable != null) {
                attributemodifiable.apply(attributemodifiable_a);
            }
        }

    }
}
