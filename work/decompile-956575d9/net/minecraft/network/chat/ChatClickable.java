package net.minecraft.network.chat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.INamable;

public interface ChatClickable {

    Codec<ChatClickable> CODEC = ChatClickable.EnumClickAction.CODEC.dispatch("action", ChatClickable::action, (chatclickable_enumclickaction) -> {
        return chatclickable_enumclickaction.codec;
    });

    ChatClickable.EnumClickAction action();

    public static record OpenUrl(URI uri) implements ChatClickable {

        public static final MapCodec<ChatClickable.OpenUrl> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.UNTRUSTED_URI.fieldOf("url").forGetter(ChatClickable.OpenUrl::uri)).apply(instance, ChatClickable.OpenUrl::new);
        });

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.OPEN_URL;
        }
    }

    public static record OpenFile(String path) implements ChatClickable {

        public static final MapCodec<ChatClickable.OpenFile> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.fieldOf("path").forGetter(ChatClickable.OpenFile::path)).apply(instance, ChatClickable.OpenFile::new);
        });

        public OpenFile(File file) {
            this(file.toString());
        }

        public OpenFile(Path path) {
            this(path.toFile());
        }

        public File file() {
            return new File(this.path);
        }

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.OPEN_FILE;
        }
    }

    public static record RunCommand(String command) implements ChatClickable {

        public static final MapCodec<ChatClickable.RunCommand> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(ChatClickable.RunCommand::command)).apply(instance, ChatClickable.RunCommand::new);
        });

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.RUN_COMMAND;
        }
    }

    public static record SuggestCommand(String command) implements ChatClickable {

        public static final MapCodec<ChatClickable.SuggestCommand> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.CHAT_STRING.fieldOf("command").forGetter(ChatClickable.SuggestCommand::command)).apply(instance, ChatClickable.SuggestCommand::new);
        });

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.SUGGEST_COMMAND;
        }
    }

    public static record ChangePage(int page) implements ChatClickable {

        public static final MapCodec<ChatClickable.ChangePage> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(ExtraCodecs.POSITIVE_INT.fieldOf("page").forGetter(ChatClickable.ChangePage::page)).apply(instance, ChatClickable.ChangePage::new);
        });

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.CHANGE_PAGE;
        }
    }

    public static record CopyToClipboard(String value) implements ChatClickable {

        public static final MapCodec<ChatClickable.CopyToClipboard> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
            return instance.group(Codec.STRING.fieldOf("value").forGetter(ChatClickable.CopyToClipboard::value)).apply(instance, ChatClickable.CopyToClipboard::new);
        });

        @Override
        public ChatClickable.EnumClickAction action() {
            return ChatClickable.EnumClickAction.COPY_TO_CLIPBOARD;
        }
    }

    public static enum EnumClickAction implements INamable {

        OPEN_URL("open_url", true, ChatClickable.OpenUrl.CODEC), OPEN_FILE("open_file", false, ChatClickable.OpenFile.CODEC), RUN_COMMAND("run_command", true, ChatClickable.RunCommand.CODEC), SUGGEST_COMMAND("suggest_command", true, ChatClickable.SuggestCommand.CODEC), CHANGE_PAGE("change_page", true, ChatClickable.ChangePage.CODEC), COPY_TO_CLIPBOARD("copy_to_clipboard", true, ChatClickable.CopyToClipboard.CODEC);

        public static final Codec<ChatClickable.EnumClickAction> UNSAFE_CODEC = INamable.<ChatClickable.EnumClickAction>fromEnum(ChatClickable.EnumClickAction::values);
        public static final Codec<ChatClickable.EnumClickAction> CODEC = ChatClickable.EnumClickAction.UNSAFE_CODEC.validate(ChatClickable.EnumClickAction::filterForSerialization);
        private final boolean allowFromServer;
        private final String name;
        final MapCodec<? extends ChatClickable> codec;

        private EnumClickAction(final String s, final boolean flag, final MapCodec mapcodec) {
            this.name = s;
            this.allowFromServer = flag;
            this.codec = mapcodec;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public static DataResult<ChatClickable.EnumClickAction> filterForSerialization(ChatClickable.EnumClickAction chatclickable_enumclickaction) {
            return !chatclickable_enumclickaction.isAllowedFromServer() ? DataResult.error(() -> {
                return "Click event type not allowed: " + String.valueOf(chatclickable_enumclickaction);
            }) : DataResult.success(chatclickable_enumclickaction, Lifecycle.stable());
        }
    }
}
