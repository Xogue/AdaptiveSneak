// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.xogue.adaptivesneak.config.AdaptiveSneakConfig;
// import com.xogue.adaptivesneak.client.AdaptiveSneakClient;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    // CONSTANTS
    @Unique
    private static final int ACTION_RELEASE = 0;
    @Unique
    private static final int ACTION_PRESS = 1;
    @Unique
    private static final int ACTION_REPEAT = 2;
    @Unique
    private static final long DOUBLE_PRESS_THRESHOLD = 250;

    // STATE VARIABLES
    @Unique
    private boolean adaptiveSneak$holdTracking;
    @Unique
    private boolean adaptiveSneak$doublePressTracking;
    @Unique
    private boolean adaptiveSneak$doublePressDetected;
    @Unique
    private boolean adaptiveSneak$downBeforePress;
    @Unique
    private boolean adaptiveSneak$sawRepeat;

    @Unique
    private boolean adaptiveSneak$restoreToggleAfterRelease;
    @Unique
    private long adaptiveSneak$releasedAt;
    @Unique
    private long adaptiveSneak$pressedAt;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void adaptiveSneak$beforeKeyPress(long window, int action, @NonNull KeyEvent event, CallbackInfo callback) {
        if (!minecraft.options.keyShift.matches(event)) {
            return;
        }

        if (action == ACTION_PRESS) {
            if (minecraft.player == null || minecraft.gui.screen() != null) {
                return;
            }

            if (adaptiveSneak$doublePressTracking) {
                long doublePressTime = System.currentTimeMillis() - adaptiveSneak$releasedAt;
                boolean doublePress = doublePressTime < DOUBLE_PRESS_THRESHOLD;

                if (doublePress) {
                    adaptiveSneak$doublePressDetected = true;
                }
            }

            adaptiveSneak$holdTracking = true;
            adaptiveSneak$downBeforePress = minecraft.options.keyShift.isDown();
            adaptiveSneak$sawRepeat = false;
            adaptiveSneak$doublePressTracking = true;
            adaptiveSneak$restoreToggleAfterRelease = false;
            adaptiveSneak$pressedAt = System.currentTimeMillis();

            // Hold mode from the first key-down makes OS repeat events harmless.
            minecraft.options.toggleCrouch().set(false);
            return;
        }

        if (!adaptiveSneak$holdTracking) {
            return;
        }

        if (action == ACTION_REPEAT) {
            adaptiveSneak$sawRepeat = true;
            return;
        }

        if (action == ACTION_RELEASE) {
            adaptiveSneak$releasedAt = System.currentTimeMillis();
            long heldFor = adaptiveSneak$releasedAt - adaptiveSneak$pressedAt;
            boolean quickTap = !adaptiveSneak$sawRepeat
                    && heldFor < AdaptiveSneakConfig.holdThresholdMillis();

            if (quickTap) {
                if (AdaptiveSneakConfig.doublePressRequired() && adaptiveSneak$doublePressDetected) {
                    minecraft.options.toggleCrouch().set(!adaptiveSneak$downBeforePress);
                    adaptiveSneak$restoreToggleAfterRelease = true;
                    adaptiveSneak$doublePressDetected = false;
                } else if (!AdaptiveSneakConfig.doublePressRequired()) {
                    minecraft.options.toggleCrouch().set(!adaptiveSneak$downBeforePress);
                    adaptiveSneak$restoreToggleAfterRelease = true;
                    adaptiveSneak$doublePressDetected = false;
                }
            } else {
                minecraft.options.toggleCrouch().set(false);
            }

            adaptiveSneak$holdTracking = false;
        }
    }

    @Inject(method = "keyPress", at = @At("RETURN"))
    private void adaptiveSneak$afterKeyPress(long window, int action, @NonNull KeyEvent event, CallbackInfo callback) {
        if (action == ACTION_RELEASE && adaptiveSneak$restoreToggleAfterRelease
                && minecraft.options.keyShift.matches(event)) {
            minecraft.options.toggleCrouch().set(true);
            adaptiveSneak$restoreToggleAfterRelease = false;
        }
    }
}
