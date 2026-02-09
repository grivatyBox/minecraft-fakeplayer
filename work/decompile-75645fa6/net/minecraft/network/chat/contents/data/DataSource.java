package net.minecraft.network.chat.contents.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.nbt.NBTTagCompound;

public interface DataSource {

    Stream<NBTTagCompound> getData(CommandListenerWrapper commandlistenerwrapper) throws CommandSyntaxException;

    MapCodec<? extends DataSource> codec();
}
