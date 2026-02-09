package net.minecraft.world.inventory;

import javax.annotation.Nullable;
import net.minecraft.network.HashedPatchMap;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;

public interface RemoteSlot {

    RemoteSlot PLACEHOLDER = new RemoteSlot() {
        @Override
        public void receive(HashedStack hashedstack) {}

        @Override
        public void force(ItemStack itemstack) {}

        @Override
        public boolean matches(ItemStack itemstack) {
            return true;
        }
    };

    void force(ItemStack itemstack);

    void receive(HashedStack hashedstack);

    boolean matches(ItemStack itemstack);

    public static class a implements RemoteSlot {

        private final HashedPatchMap.a hasher;
        @Nullable
        private ItemStack remoteStack = null;
        @Nullable
        private HashedStack remoteHash = null;

        public a(HashedPatchMap.a hashedpatchmap_a) {
            this.hasher = hashedpatchmap_a;
        }

        @Override
        public void force(ItemStack itemstack) {
            this.remoteStack = itemstack.copy();
            this.remoteHash = null;
        }

        @Override
        public void receive(HashedStack hashedstack) {
            this.remoteStack = null;
            this.remoteHash = hashedstack;
        }

        @Override
        public boolean matches(ItemStack itemstack) {
            if (this.remoteStack != null) {
                return ItemStack.matches(this.remoteStack, itemstack);
            } else if (this.remoteHash != null && this.remoteHash.matches(itemstack, this.hasher)) {
                this.remoteStack = itemstack.copy();
                return true;
            } else {
                return false;
            }
        }

        public void copyFrom(RemoteSlot.a remoteslot_a) {
            this.remoteStack = remoteslot_a.remoteStack;
            this.remoteHash = remoteslot_a.remoteHash;
        }
    }
}
