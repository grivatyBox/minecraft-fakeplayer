package net.minecraft.nbt;

public sealed interface PrimitiveTag extends NBTBase permits NBTNumber, NBTTagString {

    @Override
    default NBTBase copy() {
        return this;
    }
}
