// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AdaptiveSneakClient implements ClientModInitializer {
    public static final String MOD_ID = "adaptive_sneak";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        AdaptiveSneakConfig.load();
        LOGGER.info("Adaptive Sneak initialized");
    }
}
