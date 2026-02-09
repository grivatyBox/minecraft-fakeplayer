package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.stats.StatisticList;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.level.World;
import net.minecraft.world.level.gameevent.GameEvent;

public class InstrumentItem extends Item {

    public InstrumentItem(Item.Info item_info) {
        super(item_info);
    }

    public static ItemStack create(Item item, Holder<Instrument> holder) {
        ItemStack itemstack = new ItemStack(item);

        itemstack.set(DataComponents.INSTRUMENT, new InstrumentComponent(holder));
        return itemstack;
    }

    @Override
    public EnumInteractionResult use(World world, EntityHuman entityhuman, EnumHand enumhand) {
        ItemStack itemstack = entityhuman.getItemInHand(enumhand);
        Optional<? extends Holder<Instrument>> optional = this.getInstrument(itemstack, entityhuman.registryAccess());

        if (optional.isPresent()) {
            Instrument instrument = (Instrument) ((Holder) optional.get()).value();

            entityhuman.startUsingItem(enumhand);
            play(world, entityhuman, instrument);
            entityhuman.getCooldowns().addCooldown(itemstack, MathHelper.floor(instrument.useDuration() * 20.0F));
            entityhuman.awardStat(StatisticList.ITEM_USED.get(this));
            return EnumInteractionResult.CONSUME;
        } else {
            return EnumInteractionResult.FAIL;
        }
    }

    @Override
    public int getUseDuration(ItemStack itemstack, EntityLiving entityliving) {
        Optional<Holder<Instrument>> optional = this.getInstrument(itemstack, entityliving.registryAccess());

        return (Integer) optional.map((holder) -> {
            return MathHelper.floor(((Instrument) holder.value()).useDuration() * 20.0F);
        }).orElse(0);
    }

    private Optional<Holder<Instrument>> getInstrument(ItemStack itemstack, HolderLookup.a holderlookup_a) {
        InstrumentComponent instrumentcomponent = (InstrumentComponent) itemstack.get(DataComponents.INSTRUMENT);

        return instrumentcomponent != null ? instrumentcomponent.unwrap(holderlookup_a) : Optional.empty();
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
        return ItemUseAnimation.TOOT_HORN;
    }

    private static void play(World world, EntityHuman entityhuman, Instrument instrument) {
        SoundEffect soundeffect = (SoundEffect) instrument.soundEvent().value();
        float f = instrument.range() / 16.0F;

        world.playSound(entityhuman, (Entity) entityhuman, soundeffect, SoundCategory.RECORDS, f, 1.0F);
        world.gameEvent(GameEvent.INSTRUMENT_PLAY, entityhuman.position(), GameEvent.a.of((Entity) entityhuman));
    }
}
