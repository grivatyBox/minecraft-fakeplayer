package net.minecraft.world.entity.monster.warden;

import java.util.Arrays;
import net.minecraft.SystemUtils;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.sounds.SoundEffects;

public enum AngerLevel {

    CALM(0, SoundEffects.WARDEN_AMBIENT, SoundEffects.WARDEN_LISTENING), AGITATED(40, SoundEffects.WARDEN_AGITATED, SoundEffects.WARDEN_LISTENING_ANGRY), ANGRY(80, SoundEffects.WARDEN_ANGRY, SoundEffects.WARDEN_LISTENING_ANGRY);

    private static final AngerLevel[] SORTED_LEVELS = (AngerLevel[]) SystemUtils.make(values(), (aangerlevel) -> {
        Arrays.sort(aangerlevel, (angerlevel, angerlevel1) -> {
            return Integer.compare(angerlevel1.minimumAnger, angerlevel.minimumAnger);
        });
    });
    private final int minimumAnger;
    private final SoundEffect ambientSound;
    private final SoundEffect listeningSound;

    private AngerLevel(final int i, final SoundEffect soundeffect, final SoundEffect soundeffect1) {
        this.minimumAnger = i;
        this.ambientSound = soundeffect;
        this.listeningSound = soundeffect1;
    }

    public int getMinimumAnger() {
        return this.minimumAnger;
    }

    public SoundEffect getAmbientSound() {
        return this.ambientSound;
    }

    public SoundEffect getListeningSound() {
        return this.listeningSound;
    }

    public static AngerLevel byAnger(int i) {
        for (AngerLevel angerlevel : AngerLevel.SORTED_LEVELS) {
            if (i >= angerlevel.minimumAnger) {
                return angerlevel;
            }
        }

        return AngerLevel.CALM;
    }

    public boolean isAngry() {
        return this == AngerLevel.ANGRY;
    }
}
