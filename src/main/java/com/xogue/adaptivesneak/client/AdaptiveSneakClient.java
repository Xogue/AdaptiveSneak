// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.xogue.adaptivesneak.SneakIndicator;
import com.xogue.adaptivesneak.config.AdaptiveSneakConfig;
import com.xogue.adaptivesneak.config.SneakIndicatorConfigScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdaptiveSneakClient implements ClientModInitializer {
    public static final String MOD_ID = "adaptive_sneak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        AdaptiveSneakConfig.load();

        KeyMapping configureIndicator = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.adaptive_sneak.configure_indicator",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_K,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configureIndicator.consumeClick()) {
                client.setScreenAndShow(new SneakIndicatorConfigScreen());
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.SUBTITLES,
                Identifier.fromNamespaceAndPath(MOD_ID, "sneak_indicator"),
                SneakIndicator::renderHud);
        LOGGER.info("Adaptive Sneak initialized");
    }
}
