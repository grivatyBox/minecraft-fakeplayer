package net.minecraft.network.protocol.game;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.MinecraftKey;

public class PacketPlayOutCommands implements Packet<PacketListenerPlayOut> {

    public static final StreamCodec<PacketDataSerializer, PacketPlayOutCommands> STREAM_CODEC = Packet.<PacketDataSerializer, PacketPlayOutCommands>codec(PacketPlayOutCommands::write, PacketPlayOutCommands::new);
    private static final byte MASK_TYPE = 3;
    private static final byte FLAG_EXECUTABLE = 4;
    private static final byte FLAG_REDIRECT = 8;
    private static final byte FLAG_CUSTOM_SUGGESTIONS = 16;
    private static final byte FLAG_RESTRICTED = 32;
    private static final byte TYPE_ROOT = 0;
    private static final byte TYPE_LITERAL = 1;
    private static final byte TYPE_ARGUMENT = 2;
    private final int rootIndex;
    private final List<PacketPlayOutCommands.b> entries;

    public <S> PacketPlayOutCommands(RootCommandNode<S> rootcommandnode, PacketPlayOutCommands.e<S> packetplayoutcommands_e) {
        Object2IntMap<CommandNode<S>> object2intmap = enumerateNodes(rootcommandnode);

        this.entries = createEntries(object2intmap, packetplayoutcommands_e);
        this.rootIndex = object2intmap.getInt(rootcommandnode);
    }

    private PacketPlayOutCommands(PacketDataSerializer packetdataserializer) {
        this.entries = packetdataserializer.<PacketPlayOutCommands.b>readList(PacketPlayOutCommands::readNode);
        this.rootIndex = packetdataserializer.readVarInt();
        validateEntries(this.entries);
    }

    private void write(PacketDataSerializer packetdataserializer) {
        packetdataserializer.writeCollection(this.entries, (packetdataserializer1, packetplayoutcommands_b) -> {
            packetplayoutcommands_b.write(packetdataserializer1);
        });
        packetdataserializer.writeVarInt(this.rootIndex);
    }

    private static void validateEntries(List<PacketPlayOutCommands.b> list, BiPredicate<PacketPlayOutCommands.b, IntSet> bipredicate) {
        IntSet intset = new IntOpenHashSet(IntSets.fromTo(0, list.size()));

        while (!((IntSet) intset).isEmpty()) {
            boolean flag = intset.removeIf((i) -> {
                return bipredicate.test((PacketPlayOutCommands.b) list.get(i), intset);
            });

            if (!flag) {
                throw new IllegalStateException("Server sent an impossible command tree");
            }
        }

    }

    private static void validateEntries(List<PacketPlayOutCommands.b> list) {
        validateEntries(list, PacketPlayOutCommands.b::canBuild);
        validateEntries(list, PacketPlayOutCommands.b::canResolve);
    }

    private static <S> Object2IntMap<CommandNode<S>> enumerateNodes(RootCommandNode<S> rootcommandnode) {
        Object2IntMap<CommandNode<S>> object2intmap = new Object2IntOpenHashMap();
        Queue<CommandNode<S>> queue = new ArrayDeque();

        queue.add(rootcommandnode);

        CommandNode<S> commandnode;

        while ((commandnode = (CommandNode) ((Queue) queue).poll()) != null) {
            if (!object2intmap.containsKey(commandnode)) {
                int i = object2intmap.size();

                object2intmap.put(commandnode, i);
                queue.addAll(commandnode.getChildren());
                if (commandnode.getRedirect() != null) {
                    queue.add(commandnode.getRedirect());
                }
            }
        }

        return object2intmap;
    }

    private static <S> List<PacketPlayOutCommands.b> createEntries(Object2IntMap<CommandNode<S>> object2intmap, PacketPlayOutCommands.e<S> packetplayoutcommands_e) {
        ObjectArrayList<PacketPlayOutCommands.b> objectarraylist = new ObjectArrayList(object2intmap.size());

        objectarraylist.size(object2intmap.size());
        ObjectIterator objectiterator = Object2IntMaps.fastIterable(object2intmap).iterator();

        while (objectiterator.hasNext()) {
            Object2IntMap.Entry<CommandNode<S>> object2intmap_entry = (Entry) objectiterator.next();

            objectarraylist.set(object2intmap_entry.getIntValue(), createEntry((CommandNode) object2intmap_entry.getKey(), packetplayoutcommands_e, object2intmap));
        }

        return objectarraylist;
    }

    private static PacketPlayOutCommands.b readNode(PacketDataSerializer packetdataserializer) {
        byte b0 = packetdataserializer.readByte();
        int[] aint = packetdataserializer.readVarIntArray();
        int i = (b0 & 8) != 0 ? packetdataserializer.readVarInt() : 0;
        PacketPlayOutCommands.g packetplayoutcommands_g = read(packetdataserializer, b0);

        return new PacketPlayOutCommands.b(packetplayoutcommands_g, b0, i, aint);
    }

    @Nullable
    private static PacketPlayOutCommands.g read(PacketDataSerializer packetdataserializer, byte b0) {
        int i = b0 & 3;

        if (i == 2) {
            String s = packetdataserializer.readUtf();
            int j = packetdataserializer.readVarInt();
            ArgumentTypeInfo<?, ?> argumenttypeinfo = BuiltInRegistries.COMMAND_ARGUMENT_TYPE.byId(j);

            if (argumenttypeinfo == null) {
                return null;
            } else {
                ArgumentTypeInfo.a<?> argumenttypeinfo_a = argumenttypeinfo.deserializeFromNetwork(packetdataserializer);
                MinecraftKey minecraftkey = (b0 & 16) != 0 ? packetdataserializer.readResourceLocation() : null;

                return new PacketPlayOutCommands.a(s, argumenttypeinfo_a, minecraftkey);
            }
        } else if (i == 1) {
            String s1 = packetdataserializer.readUtf();

            return new PacketPlayOutCommands.c(s1);
        } else {
            return null;
        }
    }

    private static <S> PacketPlayOutCommands.b createEntry(CommandNode<S> commandnode, PacketPlayOutCommands.e<S> packetplayoutcommands_e, Object2IntMap<CommandNode<S>> object2intmap) {
        int i = 0;
        int j;

        if (commandnode.getRedirect() != null) {
            i |= 8;
            j = object2intmap.getInt(commandnode.getRedirect());
        } else {
            j = 0;
        }

        if (packetplayoutcommands_e.isExecutable(commandnode)) {
            i |= 4;
        }

        if (packetplayoutcommands_e.isRestricted(commandnode)) {
            i |= 32;
        }

        Objects.requireNonNull(commandnode);
        byte b0 = 0;
        PacketPlayOutCommands.g packetplayoutcommands_g;

        //$FF: b0->value
        //0->com/mojang/brigadier/tree/RootCommandNode
        //1->com/mojang/brigadier/tree/ArgumentCommandNode
        //2->com/mojang/brigadier/tree/LiteralCommandNode
        switch (commandnode.typeSwitch<invokedynamic>(commandnode, b0)) {
            case 0:
                RootCommandNode<S> rootcommandnode = (RootCommandNode)commandnode;

                i |= 0;
                packetplayoutcommands_g = null;
                break;
            case 1:
                ArgumentCommandNode<S, ?> argumentcommandnode = (ArgumentCommandNode)commandnode;
                MinecraftKey minecraftkey = packetplayoutcommands_e.suggestionId(argumentcommandnode);

                packetplayoutcommands_g = new PacketPlayOutCommands.a(argumentcommandnode.getName(), ArgumentTypeInfos.unpack(argumentcommandnode.getType()), minecraftkey);
                i |= 2;
                if (minecraftkey != null) {
                    i |= 16;
                }
                break;
            case 2:
                LiteralCommandNode<S> literalcommandnode = (LiteralCommandNode)commandnode;

                packetplayoutcommands_g = new PacketPlayOutCommands.c(literalcommandnode.getLiteral());
                i |= 1;
                break;
            default:
                throw new UnsupportedOperationException("Unknown node type " + String.valueOf(commandnode));
        }

        Stream stream = commandnode.getChildren().stream();

        Objects.requireNonNull(object2intmap);
        int[] aint = stream.mapToInt(object2intmap::getInt).toArray();

        return new PacketPlayOutCommands.b(packetplayoutcommands_g, i, j, aint);
    }

    @Override
    public PacketType<PacketPlayOutCommands> type() {
        return GamePacketTypes.CLIENTBOUND_COMMANDS;
    }

    public void handle(PacketListenerPlayOut packetlistenerplayout) {
        packetlistenerplayout.handleCommands(this);
    }

    public <S> RootCommandNode<S> getRoot(CommandBuildContext commandbuildcontext, PacketPlayOutCommands.d<S> packetplayoutcommands_d) {
        return (RootCommandNode) (new PacketPlayOutCommands.f<S>(commandbuildcontext, packetplayoutcommands_d, this.entries)).resolve(this.rootIndex);
    }

    private static record c(String id) implements PacketPlayOutCommands.g {

        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandbuildcontext, PacketPlayOutCommands.d<S> packetplayoutcommands_d) {
            return packetplayoutcommands_d.createLiteral(this.id);
        }

        @Override
        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUtf(this.id);
        }
    }

    private static record a(String id, ArgumentTypeInfo.a<?> argumentType, @Nullable MinecraftKey suggestionId) implements PacketPlayOutCommands.g {

        @Override
        public <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandbuildcontext, PacketPlayOutCommands.d<S> packetplayoutcommands_d) {
            ArgumentType<?> argumenttype = this.argumentType.instantiate(commandbuildcontext);

            return packetplayoutcommands_d.createArgument(this.id, argumenttype, this.suggestionId);
        }

        @Override
        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeUtf(this.id);
            serializeCap(packetdataserializer, this.argumentType);
            if (this.suggestionId != null) {
                packetdataserializer.writeResourceLocation(this.suggestionId);
            }

        }

        private static <A extends ArgumentType<?>> void serializeCap(PacketDataSerializer packetdataserializer, ArgumentTypeInfo.a<A> argumenttypeinfo_a) {
            serializeCap(packetdataserializer, argumenttypeinfo_a.type(), argumenttypeinfo_a);
        }

        private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.a<A>> void serializeCap(PacketDataSerializer packetdataserializer, ArgumentTypeInfo<A, T> argumenttypeinfo, ArgumentTypeInfo.a<A> argumenttypeinfo_a) {
            packetdataserializer.writeVarInt(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getId(argumenttypeinfo));
            argumenttypeinfo.serializeToNetwork(argumenttypeinfo_a, packetdataserializer);
        }
    }

    private static record b(@Nullable PacketPlayOutCommands.g stub, int flags, int redirect, int[] children) {

        public void write(PacketDataSerializer packetdataserializer) {
            packetdataserializer.writeByte(this.flags);
            packetdataserializer.writeVarIntArray(this.children);
            if ((this.flags & 8) != 0) {
                packetdataserializer.writeVarInt(this.redirect);
            }

            if (this.stub != null) {
                this.stub.write(packetdataserializer);
            }

        }

        public boolean canBuild(IntSet intset) {
            return (this.flags & 8) != 0 ? !intset.contains(this.redirect) : true;
        }

        public boolean canResolve(IntSet intset) {
            for (int i : this.children) {
                if (intset.contains(i)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static class f<S> {

        private final CommandBuildContext context;
        private final PacketPlayOutCommands.d<S> builder;
        private final List<PacketPlayOutCommands.b> entries;
        private final List<CommandNode<S>> nodes;

        f(CommandBuildContext commandbuildcontext, PacketPlayOutCommands.d<S> packetplayoutcommands_d, List<PacketPlayOutCommands.b> list) {
            this.context = commandbuildcontext;
            this.builder = packetplayoutcommands_d;
            this.entries = list;
            ObjectArrayList<CommandNode<S>> objectarraylist = new ObjectArrayList();

            objectarraylist.size(list.size());
            this.nodes = objectarraylist;
        }

        public CommandNode<S> resolve(int i) {
            CommandNode<S> commandnode = (CommandNode) this.nodes.get(i);

            if (commandnode != null) {
                return commandnode;
            } else {
                PacketPlayOutCommands.b packetplayoutcommands_b = (PacketPlayOutCommands.b) this.entries.get(i);
                CommandNode<S> commandnode1;

                if (packetplayoutcommands_b.stub == null) {
                    commandnode1 = new RootCommandNode();
                } else {
                    ArgumentBuilder<S, ?> argumentbuilder = packetplayoutcommands_b.stub.build(this.context, this.builder);

                    if ((packetplayoutcommands_b.flags & 8) != 0) {
                        argumentbuilder.redirect(this.resolve(packetplayoutcommands_b.redirect));
                    }

                    boolean flag = (packetplayoutcommands_b.flags & 4) != 0;
                    boolean flag1 = (packetplayoutcommands_b.flags & 32) != 0;

                    commandnode1 = this.builder.configure(argumentbuilder, flag, flag1).build();
                }

                this.nodes.set(i, commandnode1);

                for (int j : packetplayoutcommands_b.children) {
                    CommandNode<S> commandnode2 = this.resolve(j);

                    if (!(commandnode2 instanceof RootCommandNode)) {
                        commandnode1.addChild(commandnode2);
                    }
                }

                return commandnode1;
            }
        }
    }

    public interface d<S> {

        ArgumentBuilder<S, ?> createLiteral(String s);

        ArgumentBuilder<S, ?> createArgument(String s, ArgumentType<?> argumenttype, @Nullable MinecraftKey minecraftkey);

        ArgumentBuilder<S, ?> configure(ArgumentBuilder<S, ?> argumentbuilder, boolean flag, boolean flag1);
    }

    public interface e<S> {

        @Nullable
        MinecraftKey suggestionId(ArgumentCommandNode<S, ?> argumentcommandnode);

        boolean isExecutable(CommandNode<S> commandnode);

        boolean isRestricted(CommandNode<S> commandnode);
    }

    private interface g {

        <S> ArgumentBuilder<S, ?> build(CommandBuildContext commandbuildcontext, PacketPlayOutCommands.d<S> packetplayoutcommands_d);

        void write(PacketDataSerializer packetdataserializer);
    }
}
