package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class AdvancementDisplay {

    public static final Codec<AdvancementDisplay> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(ItemStack.ADVANCEMENT_ICON_CODEC.fieldOf("icon").forGetter(AdvancementDisplay::getIcon), ComponentSerialization.CODEC.fieldOf("title").forGetter(AdvancementDisplay::getTitle), ComponentSerialization.CODEC.fieldOf("description").forGetter(AdvancementDisplay::getDescription), ExtraCodecs.strictOptionalField(MinecraftKey.CODEC, "background").forGetter(AdvancementDisplay::getBackground), ExtraCodecs.strictOptionalField(AdvancementFrameType.CODEC, "frame", AdvancementFrameType.TASK).forGetter(AdvancementDisplay::getType), ExtraCodecs.strictOptionalField(Codec.BOOL, "show_toast", true).forGetter(AdvancementDisplay::shouldShowToast), ExtraCodecs.strictOptionalField(Codec.BOOL, "announce_to_chat", true).forGetter(AdvancementDisplay::shouldAnnounceChat), ExtraCodecs.strictOptionalField(Codec.BOOL, "hidden", false).forGetter(AdvancementDisplay::isHidden)).apply(instance, AdvancementDisplay::new);
    });
    private final IChatBaseComponent title;
    private final IChatBaseComponent description;
    private final ItemStack icon;
    private final Optional<MinecraftKey> background;
    private final AdvancementFrameType type;
    private final boolean showToast;
    private final boolean announceChat;
    private final boolean hidden;
    private float x;
    private float y;

    public AdvancementDisplay(ItemStack itemstack, IChatBaseComponent ichatbasecomponent, IChatBaseComponent ichatbasecomponent1, Optional<MinecraftKey> optional, AdvancementFrameType advancementframetype, boolean flag, boolean flag1, boolean flag2) {
        this.title = ichatbasecomponent;
        this.description = ichatbasecomponent1;
        this.icon = itemstack;
        this.background = optional;
        this.type = advancementframetype;
        this.showToast = flag;
        this.announceChat = flag1;
        this.hidden = flag2;
    }

    public void setLocation(float f, float f1) {
        this.x = f;
        this.y = f1;
    }

    public IChatBaseComponent getTitle() {
        return this.title;
    }

    public IChatBaseComponent getDescription() {
        return this.description;
    }

    public ItemStack getIcon() {
        return this.icon;
    }

    public Optional<MinecraftKey> getBackground() {
        return this.background;
    }

    public AdvancementFrameType getType() {
        return this.type;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public boolean shouldShowToast() {
        return this.showToast;
    }

    public boolean shouldAnnounceChat() {
        return this.announceChat;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void serializeToNetwork(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeComponent(this.title);
        packetdataserializer.writeComponent(this.description);
        packetdataserializer.writeItem(this.icon);
        packetdataserializer.writeEnum(this.type);
        int i = 0;

        if (this.background.isPresent()) {
            i |= 1;
        }

        if (this.showToast) {
            i |= 2;
        }

        if (this.hidden) {
            i |= 4;
        }

        packetdataserializer.writeInt(i);
        Optional optional = this.background;

        Objects.requireNonNull(packetdataserializer);
        optional.ifPresent(packetdataserializer::writeResourceLocation);
        packetdataserializer.writeFloat(this.x);
        packetdataserializer.writeFloat(this.y);
    }

    public static AdvancementDisplay fromNetwork(PacketDataSerializer packetdataserializer) {
        IChatBaseComponent ichatbasecomponent = packetdataserializer.readComponentTrusted();
        IChatBaseComponent ichatbasecomponent1 = packetdataserializer.readComponentTrusted();
        ItemStack itemstack = packetdataserializer.readItem();
        AdvancementFrameType advancementframetype = (AdvancementFrameType) packetdataserializer.readEnum(AdvancementFrameType.class);
        int i = packetdataserializer.readInt();
        Optional<MinecraftKey> optional = (i & 1) != 0 ? Optional.of(packetdataserializer.readResourceLocation()) : Optional.empty();
        boolean flag = (i & 2) != 0;
        boolean flag1 = (i & 4) != 0;
        AdvancementDisplay advancementdisplay = new AdvancementDisplay(itemstack, ichatbasecomponent, ichatbasecomponent1, optional, advancementframetype, flag, false, flag1);

        advancementdisplay.setLocation(packetdataserializer.readFloat(), packetdataserializer.readFloat());
        return advancementdisplay;
    }
}
