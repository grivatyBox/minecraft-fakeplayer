package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.SystemUtils;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.MinecraftKey;

public class MacroFunction<T extends ExecutionCommandSource<T>> implements CommandFunction<T> {

    private static final DecimalFormat DECIMAL_FORMAT = (DecimalFormat) SystemUtils.make(new DecimalFormat("#"), (decimalformat) -> {
        decimalformat.setMaximumFractionDigits(15);
        decimalformat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    });
    private static final int MAX_CACHE_ENTRIES = 8;
    private final List<String> parameters;
    private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap(8, 0.25F);
    private final MinecraftKey id;
    private final List<MacroFunction.a<T>> entries;

    public MacroFunction(MinecraftKey minecraftkey, List<MacroFunction.a<T>> list, List<String> list1) {
        this.id = minecraftkey;
        this.entries = list;
        this.parameters = list1;
    }

    @Override
    public MinecraftKey id() {
        return this.id;
    }

    @Override
    public InstantiatedFunction<T> instantiate(@Nullable NBTTagCompound nbttagcompound, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException {
        if (nbttagcompound == null) {
            throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_arguments", IChatBaseComponent.translationArg(this.id())));
        } else {
            List<String> list = new ArrayList(this.parameters.size());

            for (String s : this.parameters) {
                NBTBase nbtbase = nbttagcompound.get(s);

                if (nbtbase == null) {
                    throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.missing_argument", IChatBaseComponent.translationArg(this.id()), s));
                }

                list.add(stringify(nbtbase));
            }

            InstantiatedFunction<T> instantiatedfunction = (InstantiatedFunction) this.cache.getAndMoveToLast(list);

            if (instantiatedfunction != null) {
                return instantiatedfunction;
            } else {
                if (this.cache.size() >= 8) {
                    this.cache.removeFirst();
                }

                InstantiatedFunction<T> instantiatedfunction1 = this.substituteAndParse(this.parameters, list, commanddispatcher);

                this.cache.put(list, instantiatedfunction1);
                return instantiatedfunction1;
            }
        }
    }

    private static String stringify(NBTBase nbtbase) {
        Objects.requireNonNull(nbtbase);
        byte b0 = 0;
        String s;

        //$FF: b0->value
        //0->net/minecraft/nbt/NBTTagFloat
        //1->net/minecraft/nbt/NBTTagDouble
        //2->net/minecraft/nbt/NBTTagByte
        //3->net/minecraft/nbt/NBTTagShort
        //4->net/minecraft/nbt/NBTTagLong
        //5->net/minecraft/nbt/NBTTagString
        switch (nbtbase.typeSwitch<invokedynamic>(nbtbase, b0)) {
            case 0:
                NBTTagFloat nbttagfloat = (NBTTagFloat)nbtbase;
                NBTTagFloat nbttagfloat1 = nbttagfloat;

                try {
                    f = nbttagfloat1.value();
                } catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }

                float f1 = f;

                s = MacroFunction.DECIMAL_FORMAT.format((double)f1);
                break;
            case 1:
                NBTTagDouble nbttagdouble = (NBTTagDouble)nbtbase;
                NBTTagDouble nbttagdouble1 = nbttagdouble;

                try {
                    d0 = nbttagdouble1.value();
                } catch (Throwable throwable1) {
                    throw new MatchException(throwable1.toString(), throwable1);
                }

                double d1 = d0;

                s = MacroFunction.DECIMAL_FORMAT.format(d1);
                break;
            case 2:
                NBTTagByte nbttagbyte = (NBTTagByte)nbtbase;
                NBTTagByte nbttagbyte1 = nbttagbyte;

                try {
                    b1 = nbttagbyte1.value();
                } catch (Throwable throwable2) {
                    throw new MatchException(throwable2.toString(), throwable2);
                }

                byte b2 = b1;

                s = String.valueOf(b2);
                break;
            case 3:
                NBTTagShort nbttagshort = (NBTTagShort)nbtbase;
                NBTTagShort nbttagshort1 = nbttagshort;

                try {
                    short0 = nbttagshort1.value();
                } catch (Throwable throwable3) {
                    throw new MatchException(throwable3.toString(), throwable3);
                }

                short short1 = short0;

                s = String.valueOf(short1);
                break;
            case 4:
                NBTTagLong nbttaglong = (NBTTagLong)nbtbase;
                NBTTagLong nbttaglong1 = nbttaglong;

                try {
                    i = nbttaglong1.value();
                } catch (Throwable throwable4) {
                    throw new MatchException(throwable4.toString(), throwable4);
                }

                long j = i;

                s = String.valueOf(j);
                break;
            case 5:
                NBTTagString nbttagstring = (NBTTagString)nbtbase;
                NBTTagString nbttagstring1 = nbttagstring;

                try {
                    s1 = nbttagstring1.value();
                } catch (Throwable throwable5) {
                    throw new MatchException(throwable5.toString(), throwable5);
                }

                String s2 = s1;

                s = s2;
                break;
            default:
                s = nbtbase.toString();
        }

        return s;
    }

    private static void lookupValues(List<String> list, IntList intlist, List<String> list1) {
        list1.clear();
        intlist.forEach((i) -> {
            list1.add((String) list.get(i));
        });
    }

    private InstantiatedFunction<T> substituteAndParse(List<String> list, List<String> list1, CommandDispatcher<T> commanddispatcher) throws FunctionInstantiationException {
        List<UnboundEntryAction<T>> list2 = new ArrayList(this.entries.size());
        List<String> list3 = new ArrayList(list1.size());

        for (MacroFunction.a<T> macrofunction_a : this.entries) {
            lookupValues(list1, macrofunction_a.parameters(), list3);
            list2.add(macrofunction_a.instantiate(list3, commanddispatcher, this.id));
        }

        return new PlainTextFunction<T>(this.id().withPath((s) -> {
            return s + "/" + list.hashCode();
        }), list2);
    }

    static class c<T> implements MacroFunction.a<T> {

        private final UnboundEntryAction<T> compiledAction;

        public c(UnboundEntryAction<T> unboundentryaction) {
            this.compiledAction = unboundentryaction;
        }

        @Override
        public IntList parameters() {
            return IntLists.emptyList();
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) {
            return this.compiledAction;
        }
    }

    static class b<T extends ExecutionCommandSource<T>> implements MacroFunction.a<T> {

        private final StringTemplate template;
        private final IntList parameters;
        private final T compilationContext;

        public b(StringTemplate stringtemplate, IntList intlist, T t0) {
            this.template = stringtemplate;
            this.parameters = intlist;
            this.compilationContext = t0;
        }

        @Override
        public IntList parameters() {
            return this.parameters;
        }

        @Override
        public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) throws FunctionInstantiationException {
            String s = this.template.substitute(list);

            try {
                return CommandFunction.<T>parseCommand(commanddispatcher, this.compilationContext, new StringReader(s));
            } catch (CommandSyntaxException commandsyntaxexception) {
                throw new FunctionInstantiationException(IChatBaseComponent.translatable("commands.function.error.parse", IChatBaseComponent.translationArg(minecraftkey), s, commandsyntaxexception.getMessage()));
            }
        }
    }

    interface a<T> {

        IntList parameters();

        UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commanddispatcher, MinecraftKey minecraftkey) throws FunctionInstantiationException;
    }
}
