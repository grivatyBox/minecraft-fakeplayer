package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockStateList;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.block.state.IBlockDataHolder;
import net.minecraft.world.level.block.state.properties.IBlockState;
import net.minecraft.world.level.material.Fluid;
import org.slf4j.Logger;

public final class GameProfileSerializer {

    private static final Comparator<NBTTagList> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt((nbttaglist) -> {
        return nbttaglist.getIntOr(1, 0);
    }).thenComparingInt((nbttaglist) -> {
        return nbttaglist.getIntOr(0, 0);
    }).thenComparingInt((nbttaglist) -> {
        return nbttaglist.getIntOr(2, 0);
    });
    private static final Comparator<NBTTagList> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble((nbttaglist) -> {
        return nbttaglist.getDoubleOr(1, 0.0D);
    }).thenComparingDouble((nbttaglist) -> {
        return nbttaglist.getDoubleOr(0, 0.0D);
    }).thenComparingDouble((nbttaglist) -> {
        return nbttaglist.getDoubleOr(2, 0.0D);
    });
    private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private GameProfileSerializer() {}

    @VisibleForTesting
    public static boolean compareNbt(@Nullable NBTBase nbtbase, @Nullable NBTBase nbtbase1, boolean flag) {
        if (nbtbase == nbtbase1) {
            return true;
        } else if (nbtbase == null) {
            return true;
        } else if (nbtbase1 == null) {
            return false;
        } else if (!nbtbase.getClass().equals(nbtbase1.getClass())) {
            return false;
        } else if (nbtbase instanceof NBTTagCompound) {
            NBTTagCompound nbttagcompound = (NBTTagCompound) nbtbase;
            NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbtbase1;

            if (nbttagcompound1.size() < nbttagcompound.size()) {
                return false;
            } else {
                for (Map.Entry<String, NBTBase> map_entry : nbttagcompound.entrySet()) {
                    NBTBase nbtbase2 = (NBTBase) map_entry.getValue();

                    if (!compareNbt(nbtbase2, nbttagcompound1.get((String) map_entry.getKey()), flag)) {
                        return false;
                    }
                }

                return true;
            }
        } else {
            if (nbtbase instanceof NBTTagList) {
                NBTTagList nbttaglist = (NBTTagList) nbtbase;

                if (flag) {
                    NBTTagList nbttaglist1 = (NBTTagList) nbtbase1;

                    if (nbttaglist.isEmpty()) {
                        return nbttaglist1.isEmpty();
                    }

                    if (nbttaglist1.size() < nbttaglist.size()) {
                        return false;
                    }

                    for (NBTBase nbtbase3 : nbttaglist) {
                        boolean flag1 = false;

                        for (NBTBase nbtbase4 : nbttaglist1) {
                            if (compareNbt(nbtbase3, nbtbase4, flag)) {
                                flag1 = true;
                                break;
                            }
                        }

                        if (!flag1) {
                            return false;
                        }
                    }

                    return true;
                }
            }

            return nbtbase.equals(nbtbase1);
        }
    }

    public static IBlockData readBlockState(HolderGetter<Block> holdergetter, NBTTagCompound nbttagcompound) {
        Optional optional = nbttagcompound.read("Name", GameProfileSerializer.BLOCK_NAME_CODEC);

        Objects.requireNonNull(holdergetter);
        Optional<? extends Holder<Block>> optional1 = optional.flatMap(holdergetter::get);

        if (optional1.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        } else {
            Block block = (Block) ((Holder) optional1.get()).value();
            IBlockData iblockdata = block.defaultBlockState();
            Optional<NBTTagCompound> optional2 = nbttagcompound.getCompound("Properties");

            if (optional2.isPresent()) {
                BlockStateList<Block, IBlockData> blockstatelist = block.getStateDefinition();

                for (String s : ((NBTTagCompound) optional2.get()).keySet()) {
                    IBlockState<?> iblockstate = blockstatelist.getProperty(s);

                    if (iblockstate != null) {
                        iblockdata = (IBlockData) setValueHelper(iblockdata, iblockstate, s, (NBTTagCompound) optional2.get(), nbttagcompound);
                    }
                }
            }

            return iblockdata;
        }
    }

    private static <S extends IBlockDataHolder<?, S>, T extends Comparable<T>> S setValueHelper(S s0, IBlockState<T> iblockstate, String s, NBTTagCompound nbttagcompound, NBTTagCompound nbttagcompound1) {
        Optional optional = nbttagcompound.getString(s);

        Objects.requireNonNull(iblockstate);
        Optional<T> optional1 = optional.flatMap(iblockstate::getValue);

        if (optional1.isPresent()) {
            return (S) (((IBlockDataHolder) s0).setValue(iblockstate, (Comparable) optional1.get()));
        } else {
            GameProfileSerializer.LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{s, nbttagcompound.get(s), nbttagcompound1});
            return s0;
        }
    }

    public static NBTTagCompound writeBlockState(IBlockData iblockdata) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putString("Name", BuiltInRegistries.BLOCK.getKey(iblockdata.getBlock()).toString());
        Map<IBlockState<?>, Comparable<?>> map = iblockdata.getValues();

        if (!map.isEmpty()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            for (Map.Entry<IBlockState<?>, Comparable<?>> map_entry : map.entrySet()) {
                IBlockState<?> iblockstate = (IBlockState) map_entry.getKey();

                nbttagcompound1.putString(iblockstate.getName(), getName(iblockstate, (Comparable) map_entry.getValue()));
            }

            nbttagcompound.put("Properties", nbttagcompound1);
        }

        return nbttagcompound;
    }

    public static NBTTagCompound writeFluidState(Fluid fluid) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();

        nbttagcompound.putString("Name", BuiltInRegistries.FLUID.getKey(fluid.getType()).toString());
        Map<IBlockState<?>, Comparable<?>> map = fluid.getValues();

        if (!map.isEmpty()) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            for (Map.Entry<IBlockState<?>, Comparable<?>> map_entry : map.entrySet()) {
                IBlockState<?> iblockstate = (IBlockState) map_entry.getKey();

                nbttagcompound1.putString(iblockstate.getName(), getName(iblockstate, (Comparable) map_entry.getValue()));
            }

            nbttagcompound.put("Properties", nbttagcompound1);
        }

        return nbttagcompound;
    }

    private static <T extends Comparable<T>> String getName(IBlockState<T> iblockstate, Comparable<?> comparable) {
        return iblockstate.getName(comparable);
    }

    public static String prettyPrint(NBTBase nbtbase) {
        return prettyPrint(nbtbase, false);
    }

    public static String prettyPrint(NBTBase nbtbase, boolean flag) {
        return prettyPrint(new StringBuilder(), nbtbase, 0, flag).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder stringbuilder, NBTBase nbtbase, int i, boolean flag) {
        Objects.requireNonNull(nbtbase);
        byte b0 = 0;
        StringBuilder stringbuilder1;

        //$FF: b0->value
        //0->net/minecraft/nbt/PrimitiveTag
        //1->net/minecraft/nbt/NBTTagEnd
        //2->net/minecraft/nbt/NBTTagByteArray
        //3->net/minecraft/nbt/NBTTagList
        //4->net/minecraft/nbt/NBTTagIntArray
        //5->net/minecraft/nbt/NBTTagCompound
        //6->net/minecraft/nbt/NBTTagLongArray
        switch (nbtbase.typeSwitch<invokedynamic>(nbtbase, b0)) {
            case 0:
                PrimitiveTag primitivetag = (PrimitiveTag)nbtbase;

                stringbuilder1 = stringbuilder.append(primitivetag);
                break;
            case 1:
                NBTTagEnd nbttagend = (NBTTagEnd)nbtbase;

                stringbuilder1 = stringbuilder;
                break;
            case 2:
                NBTTagByteArray nbttagbytearray = (NBTTagByteArray)nbtbase;
                byte[] abyte = nbttagbytearray.getAsByteArray();
                int j = abyte.length;

                indent(i, stringbuilder).append("byte[").append(j).append("] {\n");
                if (!flag) {
                    indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(i + 1, stringbuilder);

                    for(int k = 0; k < abyte.length; ++k) {
                        if (k != 0) {
                            stringbuilder.append(',');
                        }

                        if (k % 16 == 0 && k / 16 > 0) {
                            stringbuilder.append('\n');
                            if (k < abyte.length) {
                                indent(i + 1, stringbuilder);
                            }
                        } else if (k != 0) {
                            stringbuilder.append(' ');
                        }

                        stringbuilder.append(String.format(Locale.ROOT, "0x%02X", abyte[k] & 255));
                    }
                }

                stringbuilder.append('\n');
                indent(i, stringbuilder).append('}');
                stringbuilder1 = stringbuilder;
                break;
            case 3:
                NBTTagList nbttaglist = (NBTTagList)nbtbase;
                int l = nbttaglist.size();

                indent(i, stringbuilder).append("list").append("[").append(l).append("] [");
                if (l != 0) {
                    stringbuilder.append('\n');
                }

                for(int i1 = 0; i1 < l; ++i1) {
                    if (i1 != 0) {
                        stringbuilder.append(",\n");
                    }

                    indent(i + 1, stringbuilder);
                    prettyPrint(stringbuilder, nbttaglist.get(i1), i + 1, flag);
                }

                if (l != 0) {
                    stringbuilder.append('\n');
                }

                indent(i, stringbuilder).append(']');
                stringbuilder1 = stringbuilder;
                break;
            case 4:
                NBTTagIntArray nbttagintarray = (NBTTagIntArray)nbtbase;
                int[] aint = nbttagintarray.getAsIntArray();
                int j1 = 0;

                for(int k1 : aint) {
                    j1 = Math.max(j1, String.format(Locale.ROOT, "%X", k1).length());
                }

                int l1 = aint.length;

                indent(i, stringbuilder).append("int[").append(l1).append("] {\n");
                if (!flag) {
                    indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(i + 1, stringbuilder);

                    for(int i2 = 0; i2 < aint.length; ++i2) {
                        if (i2 != 0) {
                            stringbuilder.append(',');
                        }

                        if (i2 % 16 == 0 && i2 / 16 > 0) {
                            stringbuilder.append('\n');
                            if (i2 < aint.length) {
                                indent(i + 1, stringbuilder);
                            }
                        } else if (i2 != 0) {
                            stringbuilder.append(' ');
                        }

                        stringbuilder.append(String.format(Locale.ROOT, "0x%0" + j1 + "X", aint[i2]));
                    }
                }

                stringbuilder.append('\n');
                indent(i, stringbuilder).append('}');
                stringbuilder1 = stringbuilder;
                break;
            case 5:
                NBTTagCompound nbttagcompound = (NBTTagCompound)nbtbase;
                List<String> list = Lists.newArrayList(nbttagcompound.keySet());

                Collections.sort(list);
                indent(i, stringbuilder).append('{');
                if (stringbuilder.length() - stringbuilder.lastIndexOf("\n") > 2 * (i + 1)) {
                    stringbuilder.append('\n');
                    indent(i + 1, stringbuilder);
                }

                int j2 = list.stream().mapToInt(String::length).max().orElse(0);
                String s = Strings.repeat(" ", j2);

                for(int k2 = 0; k2 < ((List)list).size(); ++k2) {
                    if (k2 != 0) {
                        stringbuilder.append(",\n");
                    }

                    String s1 = (String)list.get(k2);

                    indent(i + 1, stringbuilder).append('"').append(s1).append('"').append(s, 0, s.length() - s1.length()).append(": ");
                    prettyPrint(stringbuilder, nbttagcompound.get(s1), i + 1, flag);
                }

                if (!list.isEmpty()) {
                    stringbuilder.append('\n');
                }

                indent(i, stringbuilder).append('}');
                stringbuilder1 = stringbuilder;
                break;
            case 6:
                NBTTagLongArray nbttaglongarray = (NBTTagLongArray)nbtbase;
                long[] along = nbttaglongarray.getAsLongArray();
                long l2 = 0L;

                for(long i3 : along) {
                    l2 = Math.max(l2, (long)String.format(Locale.ROOT, "%X", i3).length());
                }

                long j3 = (long)along.length;

                indent(i, stringbuilder).append("long[").append(j3).append("] {\n");
                if (!flag) {
                    indent(i + 1, stringbuilder).append(" // Skipped, supply withBinaryBlobs true");
                } else {
                    indent(i + 1, stringbuilder);

                    for(int k3 = 0; k3 < along.length; ++k3) {
                        if (k3 != 0) {
                            stringbuilder.append(',');
                        }

                        if (k3 % 16 == 0 && k3 / 16 > 0) {
                            stringbuilder.append('\n');
                            if (k3 < along.length) {
                                indent(i + 1, stringbuilder);
                            }
                        } else if (k3 != 0) {
                            stringbuilder.append(' ');
                        }

                        stringbuilder.append(String.format(Locale.ROOT, "0x%0" + l2 + "X", along[k3]));
                    }
                }

                stringbuilder.append('\n');
                indent(i, stringbuilder).append('}');
                stringbuilder1 = stringbuilder;
                break;
            default:
                throw new MatchException((String)null, (Throwable)null);
        }

        return stringbuilder1;
    }

    private static StringBuilder indent(int i, StringBuilder stringbuilder) {
        int j = stringbuilder.lastIndexOf("\n") + 1;
        int k = stringbuilder.length() - j;

        for (int l = 0; l < 2 * i - k; ++l) {
            stringbuilder.append(' ');
        }

        return stringbuilder;
    }

    public static IChatBaseComponent toPrettyComponent(NBTBase nbtbase) {
        return (new TextComponentTagVisitor("")).visit(nbtbase);
    }

    public static String structureToSnbt(NBTTagCompound nbttagcompound) {
        return (new SnbtPrinterTagVisitor()).visit(packStructureTemplate(nbttagcompound));
    }

    public static NBTTagCompound snbtToStructure(String s) throws CommandSyntaxException {
        return unpackStructureTemplate(MojangsonParser.parseCompoundFully(s));
    }

    @VisibleForTesting
    static NBTTagCompound packStructureTemplate(NBTTagCompound nbttagcompound) {
        Optional<NBTTagList> optional = nbttagcompound.getList("palettes");
        NBTTagList nbttaglist;

        if (optional.isPresent()) {
            nbttaglist = ((NBTTagList) optional.get()).getListOrEmpty(0);
        } else {
            nbttaglist = nbttagcompound.getListOrEmpty("palette");
        }

        NBTTagList nbttaglist1 = (NBTTagList) nbttaglist.compoundStream().map(GameProfileSerializer::packBlockState).map(NBTTagString::valueOf).collect(Collectors.toCollection(NBTTagList::new));

        nbttagcompound.put("palette", nbttaglist1);
        if (optional.isPresent()) {
            NBTTagList nbttaglist2 = new NBTTagList();

            ((NBTTagList) optional.get()).stream().flatMap((nbtbase) -> {
                return nbtbase.asList().stream();
            }).forEach((nbttaglist3) -> {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();

                for (int i = 0; i < nbttaglist3.size(); ++i) {
                    nbttagcompound1.putString((String) nbttaglist1.getString(i).orElseThrow(), packBlockState((NBTTagCompound) nbttaglist3.getCompound(i).orElseThrow()));
                }

                nbttaglist2.add(nbttagcompound1);
            });
            nbttagcompound.put("palettes", nbttaglist2);
        }

        Optional<NBTTagList> optional1 = nbttagcompound.getList("entities");

        if (optional1.isPresent()) {
            NBTTagList nbttaglist3 = (NBTTagList) ((NBTTagList) optional1.get()).compoundStream().sorted(Comparator.comparing((nbttagcompound1) -> {
                return nbttagcompound1.getList("pos");
            }, Comparators.emptiesLast(GameProfileSerializer.YXZ_LISTTAG_DOUBLE_COMPARATOR))).collect(Collectors.toCollection(NBTTagList::new));

            nbttagcompound.put("entities", nbttaglist3);
        }

        NBTTagList nbttaglist4 = (NBTTagList) nbttagcompound.getList("blocks").stream().flatMap(NBTTagList::compoundStream).sorted(Comparator.comparing((nbttagcompound1) -> {
            return nbttagcompound1.getList("pos");
        }, Comparators.emptiesLast(GameProfileSerializer.YXZ_LISTTAG_INT_COMPARATOR))).peek((nbttagcompound1) -> {
            nbttagcompound1.putString("state", (String) nbttaglist1.getString(nbttagcompound1.getIntOr("state", 0)).orElseThrow());
        }).collect(Collectors.toCollection(NBTTagList::new));

        nbttagcompound.put("data", nbttaglist4);
        nbttagcompound.remove("blocks");
        return nbttagcompound;
    }

    @VisibleForTesting
    static NBTTagCompound unpackStructureTemplate(NBTTagCompound nbttagcompound) {
        NBTTagList nbttaglist = nbttagcompound.getListOrEmpty("palette");
        Map<String, NBTBase> map = (Map) nbttaglist.stream().flatMap((nbtbase) -> {
            return nbtbase.asString().stream();
        }).collect(ImmutableMap.toImmutableMap(Function.identity(), GameProfileSerializer::unpackBlockState));
        Optional<NBTTagList> optional = nbttagcompound.getList("palettes");

        if (optional.isPresent()) {
            nbttagcompound.put("palettes", (NBTBase) ((NBTTagList) optional.get()).compoundStream().map((nbttagcompound1) -> {
                return (NBTTagList) map.keySet().stream().map((s) -> {
                    return (String) nbttagcompound1.getString(s).orElseThrow();
                }).map(GameProfileSerializer::unpackBlockState).collect(Collectors.toCollection(NBTTagList::new));
            }).collect(Collectors.toCollection(NBTTagList::new)));
            nbttagcompound.remove("palette");
        } else {
            nbttagcompound.put("palette", (NBTBase) map.values().stream().collect(Collectors.toCollection(NBTTagList::new)));
        }

        Optional<NBTTagList> optional1 = nbttagcompound.getList("data");

        if (optional1.isPresent()) {
            Object2IntMap<String> object2intmap = new Object2IntOpenHashMap();

            object2intmap.defaultReturnValue(-1);

            for (int i = 0; i < nbttaglist.size(); ++i) {
                object2intmap.put((String) nbttaglist.getString(i).orElseThrow(), i);
            }

            NBTTagList nbttaglist1 = (NBTTagList) optional1.get();

            for (int j = 0; j < nbttaglist1.size(); ++j) {
                NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist1.getCompound(j).orElseThrow();
                String s = (String) nbttagcompound1.getString("state").orElseThrow();
                int k = object2intmap.getInt(s);

                if (k == -1) {
                    throw new IllegalStateException("Entry " + s + " missing from palette");
                }

                nbttagcompound1.putInt("state", k);
            }

            nbttagcompound.put("blocks", nbttaglist1);
            nbttagcompound.remove("data");
        }

        return nbttagcompound;
    }

    @VisibleForTesting
    static String packBlockState(NBTTagCompound nbttagcompound) {
        StringBuilder stringbuilder = new StringBuilder((String) nbttagcompound.getString("Name").orElseThrow());

        nbttagcompound.getCompound("Properties").ifPresent((nbttagcompound1) -> {
            String s = (String) nbttagcompound1.entrySet().stream().sorted(Entry.comparingByKey()).map((entry) -> {
                String s1 = (String) entry.getKey();

                return s1 + ":" + (String) ((NBTBase) entry.getValue()).asString().orElseThrow();
            }).collect(Collectors.joining(","));

            stringbuilder.append('{').append(s).append('}');
        });
        return stringbuilder.toString();
    }

    @VisibleForTesting
    static NBTTagCompound unpackBlockState(String s) {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        int i = s.indexOf(123);
        String s1;

        if (i >= 0) {
            s1 = s.substring(0, i);
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();

            if (i + 2 <= s.length()) {
                String s2 = s.substring(i + 1, s.indexOf(125, i));

                GameProfileSerializer.COMMA_SPLITTER.split(s2).forEach((s3) -> {
                    List<String> list = GameProfileSerializer.COLON_SPLITTER.splitToList(s3);

                    if (list.size() == 2) {
                        nbttagcompound1.putString((String) list.get(0), (String) list.get(1));
                    } else {
                        GameProfileSerializer.LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", s);
                    }

                });
                nbttagcompound.put("Properties", nbttagcompound1);
            }
        } else {
            s1 = s;
        }

        nbttagcompound.putString("Name", s1);
        return nbttagcompound;
    }

    public static NBTTagCompound addCurrentDataVersion(NBTTagCompound nbttagcompound) {
        int i = SharedConstants.getCurrentVersion().getDataVersion().getVersion();

        return addDataVersion(nbttagcompound, i);
    }

    public static NBTTagCompound addDataVersion(NBTTagCompound nbttagcompound, int i) {
        nbttagcompound.putInt("DataVersion", i);
        return nbttagcompound;
    }

    public static int getDataVersion(NBTTagCompound nbttagcompound, int i) {
        return nbttagcompound.getIntOr("DataVersion", i);
    }

    public static int getDataVersion(Dynamic<?> dynamic, int i) {
        return dynamic.get("DataVersion").asInt(i);
    }
}
