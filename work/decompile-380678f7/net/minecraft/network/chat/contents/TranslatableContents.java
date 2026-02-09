package net.minecraft.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.locale.LocaleLanguage;
import net.minecraft.network.chat.ChatComponentUtils;
import net.minecraft.network.chat.ChatModifier;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.chat.IChatFormatted;
import net.minecraft.network.chat.IChatMutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;

public class TranslatableContents implements ComponentContents {

    public static final Object[] NO_ARGS = new Object[0];
    private static final Codec<Object> PRIMITIVE_ARG_CODEC = ExtraCodecs.JAVA.validate(TranslatableContents::filterAllowedArguments);
    private static final Codec<Object> ARG_CODEC = Codec.either(TranslatableContents.PRIMITIVE_ARG_CODEC, ComponentSerialization.CODEC).xmap((either) -> {
        return either.map((object) -> {
            return object;
        }, (ichatbasecomponent) -> {
            return Objects.requireNonNullElse(ichatbasecomponent.tryCollapseToString(), ichatbasecomponent);
        });
    }, (object) -> {
        Either either;

        if (object instanceof IChatBaseComponent ichatbasecomponent) {
            either = Either.right(ichatbasecomponent);
        } else {
            either = Either.left(object);
        }

        return either;
    });
    public static final MapCodec<TranslatableContents> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(Codec.STRING.fieldOf("translate").forGetter((translatablecontents) -> {
            return translatablecontents.key;
        }), Codec.STRING.lenientOptionalFieldOf("fallback").forGetter((translatablecontents) -> {
            return Optional.ofNullable(translatablecontents.fallback);
        }), TranslatableContents.ARG_CODEC.listOf().optionalFieldOf("with").forGetter((translatablecontents) -> {
            return adjustArgs(translatablecontents.args);
        })).apply(instance, TranslatableContents::create);
    });
    public static final ComponentContents.a<TranslatableContents> TYPE = new ComponentContents.a<TranslatableContents>(TranslatableContents.CODEC, "translatable");
    private static final IChatFormatted TEXT_PERCENT = IChatFormatted.of("%");
    private static final IChatFormatted TEXT_NULL = IChatFormatted.of("null");
    private final String key;
    @Nullable
    private final String fallback;
    private final Object[] args;
    @Nullable
    private LocaleLanguage decomposedWith;
    private List<IChatFormatted> decomposedParts = ImmutableList.of();
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    private static DataResult<Object> filterAllowedArguments(@Nullable Object object) {
        return !isAllowedPrimitiveArgument(object) ? DataResult.error(() -> {
            return "This value needs to be parsed as component";
        }) : DataResult.success(object);
    }

    public static boolean isAllowedPrimitiveArgument(@Nullable Object object) {
        return object instanceof Number || object instanceof Boolean || object instanceof String;
    }

    private static Optional<List<Object>> adjustArgs(Object[] aobject) {
        return aobject.length == 0 ? Optional.empty() : Optional.of(Arrays.asList(aobject));
    }

    private static Object[] adjustArgs(Optional<List<Object>> optional) {
        return optional.map((list) -> {
            return list.isEmpty() ? TranslatableContents.NO_ARGS : list.toArray();
        }).orElse(TranslatableContents.NO_ARGS);
    }

    private static TranslatableContents create(String s, Optional<String> optional, Optional<List<Object>> optional1) {
        return new TranslatableContents(s, (String) optional.orElse((Object) null), adjustArgs(optional1));
    }

    public TranslatableContents(String s, @Nullable String s1, Object[] aobject) {
        this.key = s;
        this.fallback = s1;
        this.args = aobject;
    }

    @Override
    public ComponentContents.a<?> type() {
        return TranslatableContents.TYPE;
    }

    private void decompose() {
        LocaleLanguage localelanguage = LocaleLanguage.getInstance();

        if (localelanguage != this.decomposedWith) {
            this.decomposedWith = localelanguage;
            String s = this.fallback != null ? localelanguage.getOrDefault(this.key, this.fallback) : localelanguage.getOrDefault(this.key);

            try {
                ImmutableList.Builder<IChatFormatted> immutablelist_builder = ImmutableList.builder();

                Objects.requireNonNull(immutablelist_builder);
                this.decomposeTemplate(s, immutablelist_builder::add);
                this.decomposedParts = immutablelist_builder.build();
            } catch (TranslatableFormatException translatableformatexception) {
                this.decomposedParts = ImmutableList.of(IChatFormatted.of(s));
            }

        }
    }

    private void decomposeTemplate(String s, Consumer<IChatFormatted> consumer) {
        Matcher matcher = TranslatableContents.FORMAT_PATTERN.matcher(s);

        try {
            int i = 0;

            int j;
            int k;

            for (j = 0; matcher.find(j); j = k) {
                int l = matcher.start();

                k = matcher.end();
                if (l > j) {
                    String s1 = s.substring(j, l);

                    if (s1.indexOf(37) != -1) {
                        throw new IllegalArgumentException();
                    }

                    consumer.accept(IChatFormatted.of(s1));
                }

                String s2 = matcher.group(2);
                String s3 = s.substring(l, k);

                if ("%".equals(s2) && "%%".equals(s3)) {
                    consumer.accept(TranslatableContents.TEXT_PERCENT);
                } else {
                    if (!"s".equals(s2)) {
                        throw new TranslatableFormatException(this, "Unsupported format: '" + s3 + "'");
                    }

                    String s4 = matcher.group(1);
                    int i1 = s4 != null ? Integer.parseInt(s4) - 1 : i++;

                    consumer.accept(this.getArgument(i1));
                }
            }

            if (j < s.length()) {
                String s5 = s.substring(j);

                if (s5.indexOf(37) != -1) {
                    throw new IllegalArgumentException();
                }

                consumer.accept(IChatFormatted.of(s5));
            }

        } catch (IllegalArgumentException illegalargumentexception) {
            throw new TranslatableFormatException(this, illegalargumentexception);
        }
    }

    private IChatFormatted getArgument(int i) {
        if (i >= 0 && i < this.args.length) {
            Object object = this.args[i];

            if (object instanceof IChatBaseComponent) {
                IChatBaseComponent ichatbasecomponent = (IChatBaseComponent) object;

                return ichatbasecomponent;
            } else {
                return object == null ? TranslatableContents.TEXT_NULL : IChatFormatted.of(object.toString());
            }
        } else {
            throw new TranslatableFormatException(this, i);
        }
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.b<T> ichatformatted_b, ChatModifier chatmodifier) {
        this.decompose();

        for (IChatFormatted ichatformatted : this.decomposedParts) {
            Optional<T> optional = ichatformatted.<T>visit(ichatformatted_b, chatmodifier);

            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    @Override
    public <T> Optional<T> visit(IChatFormatted.a<T> ichatformatted_a) {
        this.decompose();

        for (IChatFormatted ichatformatted : this.decomposedParts) {
            Optional<T> optional = ichatformatted.<T>visit(ichatformatted_a);

            if (optional.isPresent()) {
                return optional;
            }
        }

        return Optional.empty();
    }

    @Override
    public IChatMutableComponent resolve(@Nullable CommandListenerWrapper commandlistenerwrapper, @Nullable Entity entity, int i) throws CommandSyntaxException {
        Object[] aobject = new Object[this.args.length];

        for (int j = 0; j < aobject.length; ++j) {
            Object object = this.args[j];

            if (object instanceof IChatBaseComponent ichatbasecomponent) {
                aobject[j] = ChatComponentUtils.updateForEntity(commandlistenerwrapper, ichatbasecomponent, entity, i);
            } else {
                aobject[j] = object;
            }
        }

        return IChatMutableComponent.create(new TranslatableContents(this.key, this.fallback, aobject));
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else {
            boolean flag;

            if (object instanceof TranslatableContents) {
                TranslatableContents translatablecontents = (TranslatableContents) object;

                if (Objects.equals(this.key, translatablecontents.key) && Objects.equals(this.fallback, translatablecontents.fallback) && Arrays.equals(this.args, translatablecontents.args)) {
                    flag = true;
                    return flag;
                }
            }

            flag = false;
            return flag;
        }
    }

    public int hashCode() {
        int i = Objects.hashCode(this.key);

        i = 31 * i + Objects.hashCode(this.fallback);
        i = 31 * i + Arrays.hashCode(this.args);
        return i;
    }

    public String toString() {
        return "translation{key='" + this.key + "'" + (this.fallback != null ? ", fallback='" + this.fallback + "'" : "") + ", args=" + Arrays.toString(this.args) + "}";
    }

    public String getKey() {
        return this.key;
    }

    @Nullable
    public String getFallback() {
        return this.fallback;
    }

    public Object[] getArgs() {
        return this.args;
    }
}
