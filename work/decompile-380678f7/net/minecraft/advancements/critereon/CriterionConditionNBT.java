package net.minecraft.advancements.critereon;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public record CriterionConditionNBT(NBTTagCompound tag) {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<CriterionConditionNBT> CODEC = MojangsonParser.LENIENT_CODEC.xmap(CriterionConditionNBT::new, CriterionConditionNBT::tag);
    public static final StreamCodec<ByteBuf, CriterionConditionNBT> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(CriterionConditionNBT::new, CriterionConditionNBT::tag);
    public static final String SELECTED_ITEM_TAG = "SelectedItem";

    public boolean matches(DataComponentGetter datacomponentgetter) {
        CustomData customdata = (CustomData) datacomponentgetter.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        return customdata.matchedBy(this.tag);
    }

    public boolean matches(Entity entity) {
        return this.matches((NBTBase) getEntityTagToCompare(entity));
    }

    public boolean matches(@Nullable NBTBase nbtbase) {
        return nbtbase != null && GameProfileSerializer.compareNbt(this.tag, nbtbase, true);
    }

    public static NBTTagCompound getEntityTagToCompare(Entity entity) {
        try (ProblemReporter.j problemreporter_j = new ProblemReporter.j(entity.problemPath(), CriterionConditionNBT.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter_j, entity.registryAccess());

            entity.saveWithoutId(tagvalueoutput);
            if (entity instanceof EntityHuman entityhuman) {
                ItemStack itemstack = entityhuman.getInventory().getSelectedItem();

                if (!itemstack.isEmpty()) {
                    tagvalueoutput.store("SelectedItem", ItemStack.CODEC, itemstack);
                }
            }

            return tagvalueoutput.buildResult();
        }
    }
}
