package net.minecraft.world.entity;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.INamable;

public enum EquipmentSlotGroup implements INamable, Iterable<EnumItemSlot> {

    ANY(0, "any", (enumitemslot) -> {
        return true;
    }), MAINHAND(1, "mainhand", EnumItemSlot.MAINHAND), OFFHAND(2, "offhand", EnumItemSlot.OFFHAND), HAND(3, "hand", (enumitemslot) -> {
        return enumitemslot.getType() == EnumItemSlot.Function.HAND;
    }), FEET(4, "feet", EnumItemSlot.FEET), LEGS(5, "legs", EnumItemSlot.LEGS), CHEST(6, "chest", EnumItemSlot.CHEST), HEAD(7, "head", EnumItemSlot.HEAD), ARMOR(8, "armor", EnumItemSlot::isArmor), BODY(9, "body", EnumItemSlot.BODY), SADDLE(10, "saddle", EnumItemSlot.SADDLE);

    public static final IntFunction<EquipmentSlotGroup> BY_ID = ByIdMap.<EquipmentSlotGroup>continuous((equipmentslotgroup) -> {
        return equipmentslotgroup.id;
    }, values(), ByIdMap.a.ZERO);
    public static final Codec<EquipmentSlotGroup> CODEC = INamable.<EquipmentSlotGroup>fromEnum(EquipmentSlotGroup::values);
    public static final StreamCodec<ByteBuf, EquipmentSlotGroup> STREAM_CODEC = ByteBufCodecs.idMapper(EquipmentSlotGroup.BY_ID, (equipmentslotgroup) -> {
        return equipmentslotgroup.id;
    });
    private final int id;
    private final String key;
    private final Predicate<EnumItemSlot> predicate;
    private final List<EnumItemSlot> slots;

    private EquipmentSlotGroup(final int i, final String s, final Predicate predicate) {
        this.id = i;
        this.key = s;
        this.predicate = predicate;
        this.slots = EnumItemSlot.VALUES.stream().filter(predicate).toList();
    }

    private EquipmentSlotGroup(final int i, final String s, final EnumItemSlot enumitemslot) {
        this(i, s, (enumitemslot1) -> {
            return enumitemslot1 == enumitemslot;
        });
    }

    public static EquipmentSlotGroup bySlot(EnumItemSlot enumitemslot) {
        EquipmentSlotGroup equipmentslotgroup;

        switch (enumitemslot) {
            case MAINHAND:
                equipmentslotgroup = EquipmentSlotGroup.MAINHAND;
                break;
            case OFFHAND:
                equipmentslotgroup = EquipmentSlotGroup.OFFHAND;
                break;
            case FEET:
                equipmentslotgroup = EquipmentSlotGroup.FEET;
                break;
            case LEGS:
                equipmentslotgroup = EquipmentSlotGroup.LEGS;
                break;
            case CHEST:
                equipmentslotgroup = EquipmentSlotGroup.CHEST;
                break;
            case HEAD:
                equipmentslotgroup = EquipmentSlotGroup.HEAD;
                break;
            case BODY:
                equipmentslotgroup = EquipmentSlotGroup.BODY;
                break;
            case SADDLE:
                equipmentslotgroup = EquipmentSlotGroup.SADDLE;
                break;
            default:
                throw new MatchException((String) null, (Throwable) null);
        }

        return equipmentslotgroup;
    }

    @Override
    public String getSerializedName() {
        return this.key;
    }

    public boolean test(EnumItemSlot enumitemslot) {
        return this.predicate.test(enumitemslot);
    }

    public List<EnumItemSlot> slots() {
        return this.slots;
    }

    public Iterator<EnumItemSlot> iterator() {
        return this.slots.iterator();
    }
}
