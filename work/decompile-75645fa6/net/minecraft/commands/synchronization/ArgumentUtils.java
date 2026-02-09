package net.minecraft.commands.synchronization;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.commands.PermissionCheck;
import org.slf4j.Logger;

public class ArgumentUtils {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte NUMBER_FLAG_MIN = 1;
    private static final byte NUMBER_FLAG_MAX = 2;

    public ArgumentUtils() {}

    public static int createNumberFlags(boolean flag, boolean flag1) {
        int i = 0;

        if (flag) {
            i |= 1;
        }

        if (flag1) {
            i |= 2;
        }

        return i;
    }

    public static boolean numberHasMin(byte b0) {
        return (b0 & 1) != 0;
    }

    public static boolean numberHasMax(byte b0) {
        return (b0 & 2) != 0;
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.a<A>> void serializeArgumentCap(JsonObject jsonobject, ArgumentTypeInfo<A, T> argumenttypeinfo, ArgumentTypeInfo.a<A> argumenttypeinfo_a) {
        argumenttypeinfo.serializeToJson(argumenttypeinfo_a, jsonobject);
    }

    private static <T extends ArgumentType<?>> void serializeArgumentToJson(JsonObject jsonobject, T t0) {
        ArgumentTypeInfo.a<T> argumenttypeinfo_a = ArgumentTypeInfos.<T>unpack(t0);

        jsonobject.addProperty("type", "argument");
        jsonobject.addProperty("parser", String.valueOf(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(argumenttypeinfo_a.type())));
        JsonObject jsonobject1 = new JsonObject();

        serializeArgumentCap(jsonobject1, argumenttypeinfo_a.type(), argumenttypeinfo_a);
        if (!jsonobject1.isEmpty()) {
            jsonobject.add("properties", jsonobject1);
        }

    }

    public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> commanddispatcher, CommandNode<S> commandnode) {
        JsonObject jsonobject = new JsonObject();

        Objects.requireNonNull(commandnode);
        byte b0 = 0;

        //$FF: b0->value
        //0->com/mojang/brigadier/tree/RootCommandNode
        //1->com/mojang/brigadier/tree/LiteralCommandNode
        //2->com/mojang/brigadier/tree/ArgumentCommandNode
        switch (commandnode.typeSwitch<invokedynamic>(commandnode, b0)) {
            case 0:
                RootCommandNode<S> rootcommandnode = (RootCommandNode)commandnode;

                jsonobject.addProperty("type", "root");
                break;
            case 1:
                LiteralCommandNode<S> literalcommandnode = (LiteralCommandNode)commandnode;

                jsonobject.addProperty("type", "literal");
                break;
            case 2:
                ArgumentCommandNode<S, ?> argumentcommandnode = (ArgumentCommandNode)commandnode;

                serializeArgumentToJson(jsonobject, argumentcommandnode.getType());
                break;
            default:
                ArgumentUtils.LOGGER.error("Could not serialize node {} ({})!", commandnode, commandnode.getClass());
                jsonobject.addProperty("type", "unknown");
        }

        Collection<CommandNode<S>> collection = commandnode.getChildren();

        if (!collection.isEmpty()) {
            JsonObject jsonobject1 = new JsonObject();

            for(CommandNode<S> commandnode1 : collection) {
                jsonobject1.add(commandnode1.getName(), serializeNodeToJson(commanddispatcher, commandnode1));
            }

            jsonobject.add("children", jsonobject1);
        }

        if (commandnode.getCommand() != null) {
            jsonobject.addProperty("executable", true);
        }

        Predicate predicate = commandnode.getRequirement();

        if (predicate instanceof PermissionCheck<?> permissioncheck) {
            jsonobject.addProperty("required_level", permissioncheck.requiredLevel());
        }

        if (commandnode.getRedirect() != null) {
            Collection<String> collection1 = commanddispatcher.getPath(commandnode.getRedirect());

            if (!collection1.isEmpty()) {
                JsonArray jsonarray = new JsonArray();

                for(String s : collection1) {
                    jsonarray.add(s);
                }

                jsonobject.add("redirect", jsonarray);
            }
        }

        return jsonobject;
    }

    public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> commandnode) {
        Set<CommandNode<T>> set = new ReferenceOpenHashSet();
        Set<ArgumentType<?>> set1 = new HashSet();

        findUsedArgumentTypes(commandnode, set1, set);
        return set1;
    }

    private static <T> void findUsedArgumentTypes(CommandNode<T> commandnode, Set<ArgumentType<?>> set, Set<CommandNode<T>> set1) {
        if (set1.add(commandnode)) {
            if (commandnode instanceof ArgumentCommandNode) {
                ArgumentCommandNode<T, ?> argumentcommandnode = (ArgumentCommandNode) commandnode;

                set.add(argumentcommandnode.getType());
            }

            commandnode.getChildren().forEach((commandnode1) -> {
                findUsedArgumentTypes(commandnode1, set, set1);
            });
            CommandNode<T> commandnode1 = commandnode.getRedirect();

            if (commandnode1 != null) {
                findUsedArgumentTypes(commandnode1, set, set1);
            }

        }
    }
}
