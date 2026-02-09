package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.InventorySubcontainer;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BlockComposter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.IBlockData;

public class BehaviorWorkComposter extends BehaviorWork {

    private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

    public BehaviorWorkComposter() {}

    @Override
    protected void useWorkstation(WorldServer worldserver, EntityVillager entityvillager) {
        Optional<GlobalPos> optional = entityvillager.getBrain().<GlobalPos>getMemory(MemoryModuleType.JOB_SITE);

        if (!optional.isEmpty()) {
            GlobalPos globalpos = (GlobalPos) optional.get();
            IBlockData iblockdata = worldserver.getBlockState(globalpos.pos());

            if (iblockdata.is(Blocks.COMPOSTER)) {
                this.makeBread(worldserver, entityvillager);
                this.compostItems(worldserver, entityvillager, globalpos, iblockdata);
            }

        }
    }

    private void compostItems(WorldServer worldserver, EntityVillager entityvillager, GlobalPos globalpos, IBlockData iblockdata) {
        BlockPosition blockposition = globalpos.pos();

        if ((Integer) iblockdata.getValue(BlockComposter.LEVEL) == 8) {
            iblockdata = BlockComposter.extractProduce(entityvillager, iblockdata, worldserver, blockposition);
        }

        int i = 20;
        int j = 10;
        int[] aint = new int[BehaviorWorkComposter.COMPOSTABLE_ITEMS.size()];
        InventorySubcontainer inventorysubcontainer = entityvillager.getInventory();
        int k = inventorysubcontainer.getContainerSize();
        IBlockData iblockdata1 = iblockdata;

        for (int l = k - 1; l >= 0 && i > 0; --l) {
            ItemStack itemstack = inventorysubcontainer.getItem(l);
            int i1 = BehaviorWorkComposter.COMPOSTABLE_ITEMS.indexOf(itemstack.getItem());

            if (i1 != -1) {
                int j1 = itemstack.getCount();
                int k1 = aint[i1] + j1;

                aint[i1] = k1;
                int l1 = Math.min(Math.min(k1 - 10, i), j1);

                if (l1 > 0) {
                    i -= l1;

                    for (int i2 = 0; i2 < l1; ++i2) {
                        iblockdata1 = BlockComposter.insertItem(entityvillager, iblockdata1, worldserver, itemstack, blockposition);
                        if ((Integer) iblockdata1.getValue(BlockComposter.LEVEL) == 7) {
                            this.spawnComposterFillEffects(worldserver, iblockdata, blockposition, iblockdata1);
                            return;
                        }
                    }
                }
            }
        }

        this.spawnComposterFillEffects(worldserver, iblockdata, blockposition, iblockdata1);
    }

    private void spawnComposterFillEffects(WorldServer worldserver, IBlockData iblockdata, BlockPosition blockposition, IBlockData iblockdata1) {
        worldserver.levelEvent(1500, blockposition, iblockdata1 != iblockdata ? 1 : 0);
    }

    private void makeBread(WorldServer worldserver, EntityVillager entityvillager) {
        InventorySubcontainer inventorysubcontainer = entityvillager.getInventory();

        if (inventorysubcontainer.countItem(Items.BREAD) <= 36) {
            int i = inventorysubcontainer.countItem(Items.WHEAT);
            int j = 3;
            int k = 3;
            int l = Math.min(3, i / 3);

            if (l != 0) {
                int i1 = l * 3;

                inventorysubcontainer.removeItemType(Items.WHEAT, i1);
                ItemStack itemstack = inventorysubcontainer.addItem(new ItemStack(Items.BREAD, l));

                if (!itemstack.isEmpty()) {
                    entityvillager.spawnAtLocation(worldserver, itemstack, 0.5F);
                }

            }
        }
    }
}
