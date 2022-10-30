package net.ccbluex.liquidbounce.injection.forge.mixins.client;

import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GameSettings.class)
public class MixinGameSettings {
    /**
     * @author asbyth
     * @reason Resolve Chat Key bound to a unicode char causing crashes while creative inventory is opened (MC-102867)
     */
    @Overwrite
    public static boolean isKeyDown(KeyBinding key) {
        int keyCode = key.getKeyCode();
        if (keyCode != 0 && keyCode < 256) {
            return keyCode < 0 ? Mouse.isButtonDown(keyCode + 100) : Keyboard.isKeyDown(keyCode);
        } else {
            return false;
        }
    }
}
