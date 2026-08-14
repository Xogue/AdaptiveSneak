// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak.mixin;

import com.xogue.adaptivesneak.AdaptiveSneakConfig;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Unique
    private static final int ACTION_RELEASE = 0;

    @Unique
    private static final int ACTION_PRESS = 1;

    @Unique
    private static final int ACTION_REPEAT = 2;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private boolean adaptiveSneak$tracking;

    @Unique
    private boolean adaptiveSneak$downBeforePress;

    @Unique
    private boolean adaptiveSneak$sawRepeat;

    @Unique
    private boolean adaptiveSneak$restoreToggleAfterRelease;

    @Unique
    private long adaptiveSneak$pressedAt;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void adaptiveSneak$beforeKeyPress(long window, int action, KeyEvent event, CallbackInfo callback) {
        if (!minecraft.options.keyShift.matches(event)) {
            return;
        }

        if (action == ACTION_PRESS) {
            if (minecraft.player == null || minecraft.gui.screen() != null) {
                return;
            }

            adaptiveSneak$tracking = true;
            adaptiveSneak$downBeforePress = minecraft.options.keyShift.isDown();
            adaptiveSneak$sawRepeat = false;
            adaptiveSneak$restoreToggleAfterRelease = false;
            adaptiveSneak$pressedAt = System.nanoTime();

            // Hold mode from the first key-down makes OS repeat events harmless.
            minecraft.options.toggleCrouch().set(false);
            return;
        }

        if (!adaptiveSneak$tracking) {
            return;
        }

        if (action == ACTION_REPEAT) {
            adaptiveSneak$sawRepeat = true;
            return;
        }

        if (action == ACTION_RELEASE) {
            long heldFor = System.nanoTime() - adaptiveSneak$pressedAt;
            boolean quickTap = !adaptiveSneak$sawRepeat
                    && heldFor < AdaptiveSneakConfig.holdThresholdNanos();

            if (quickTap) {
                // A tap toggles the state that existed before this press. Toggle mode
                // must be delayed when turning crouch off so vanilla processes release.
                minecraft.options.toggleCrouch().set(!adaptiveSneak$downBeforePress);
                adaptiveSneak$restoreToggleAfterRelease = true;
            } else {
                minecraft.options.toggleCrouch().set(false);
            }

            adaptiveSneak$tracking = false;
        }
    }

    @Inject(method = "keyPress", at = @At("RETURN"))
    private void adaptiveSneak$afterKeyPress(long window, int action, KeyEvent event, CallbackInfo callback) {
        if (action == ACTION_RELEASE && adaptiveSneak$restoreToggleAfterRelease
                && minecraft.options.keyShift.matches(event)) {
            minecraft.options.toggleCrouch().set(true);
            adaptiveSneak$restoreToggleAfterRelease = false;
        }
    }
}
