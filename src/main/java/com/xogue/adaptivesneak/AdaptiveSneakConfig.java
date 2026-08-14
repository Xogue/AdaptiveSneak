// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public final class AdaptiveSneakConfig {
    private static final long DEFAULT_HOLD_THRESHOLD_MS = 150;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(AdaptiveSneakClient.MOD_ID + ".json");

    private static long holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;

    private AdaptiveSneakConfig() {
    }

    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Values values = GSON.fromJson(reader, Values.class);
            if (values == null || values.holdThresholdMs < 0) {
                AdaptiveSneakClient.LOGGER.warn(
                        "Invalid holdThresholdMs in {}; using the default of {} ms",
                        CONFIG_PATH, DEFAULT_HOLD_THRESHOLD_MS);
                holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;
                return;
            }

            holdThresholdMs = values.holdThresholdMs;
        } catch (IOException | JsonParseException exception) {
            AdaptiveSneakClient.LOGGER.warn(
                    "Could not read {}; using the default hold threshold of {} ms",
                    CONFIG_PATH, DEFAULT_HOLD_THRESHOLD_MS, exception);
            holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;
        }
    }

    public static long holdThresholdNanos() {
        return TimeUnit.MILLISECONDS.toNanos(holdThresholdMs);
    }

    private static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new Values(), writer);
            }
        } catch (IOException exception) {
            AdaptiveSneakClient.LOGGER.warn("Could not create default config at {}", CONFIG_PATH, exception);
        }
    }

    private static final class Values {
        private long holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;
    }
}
