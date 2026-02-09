package net.minecraft.world.entity.animal.coppergolem;

import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundEffect;

public record CopperGolemOxidationLevel(SoundEffect spinHeadSound, SoundEffect hurtSound, SoundEffect deathSound, SoundEffect stepSound, MinecraftKey texture, MinecraftKey eyeTexture) {

}
