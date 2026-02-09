package net.minecraft.world.scores;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.network.chat.ChatHoverable;
import net.minecraft.network.chat.IChatBaseComponent;

public interface ScoreHolder {

    String WILDCARD_NAME = "*";
    ScoreHolder WILDCARD = new ScoreHolder() {
        @Override
        public String getScoreboardName() {
            return "*";
        }
    };

    String getScoreboardName();

    @Nullable
    default IChatBaseComponent getDisplayName() {
        return null;
    }

    default IChatBaseComponent getFeedbackDisplayName() {
        IChatBaseComponent ichatbasecomponent = this.getDisplayName();

        return ichatbasecomponent != null ? ichatbasecomponent.copy().withStyle((chatmodifier) -> {
            return chatmodifier.withHoverEvent(new ChatHoverable.e(IChatBaseComponent.literal(this.getScoreboardName())));
        }) : IChatBaseComponent.literal(this.getScoreboardName());
    }

    static ScoreHolder forNameOnly(final String s) {
        if (s.equals("*")) {
            return ScoreHolder.WILDCARD;
        } else {
            final IChatBaseComponent ichatbasecomponent = IChatBaseComponent.literal(s);

            return new ScoreHolder() {
                @Override
                public String getScoreboardName() {
                    return s;
                }

                @Override
                public IChatBaseComponent getFeedbackDisplayName() {
                    return ichatbasecomponent;
                }
            };
        }
    }

    static ScoreHolder fromGameProfile(GameProfile gameprofile) {
        final String s = gameprofile.getName();

        return new ScoreHolder() {
            @Override
            public String getScoreboardName() {
                return s;
            }
        };
    }
}
