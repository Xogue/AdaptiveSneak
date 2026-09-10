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
    private static final float DEFAULT_INDICATOR_X = 0.04F;
    private static final float DEFAULT_INDICATOR_Y = 0.72F;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir()
            .resolve(AdaptiveSneakClient.MOD_ID + ".json");

    private static long holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;
    private static float indicatorX = DEFAULT_INDICATOR_X;
    private static float indicatorY = DEFAULT_INDICATOR_Y;

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
            indicatorX = clampPosition(values.indicatorX, DEFAULT_INDICATOR_X);
            indicatorY = clampPosition(values.indicatorY, DEFAULT_INDICATOR_Y);
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

    public static float indicatorX() {
        return indicatorX;
    }

    public static float indicatorY() {
        return indicatorY;
    }

    public static void setIndicatorPosition(float x, float y) {
        indicatorX = clampPosition(x, DEFAULT_INDICATOR_X);
        indicatorY = clampPosition(y, DEFAULT_INDICATOR_Y);
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(new Values(holdThresholdMs, indicatorX, indicatorY), writer);
            }
        } catch (IOException exception) {
            AdaptiveSneakClient.LOGGER.warn("Could not create default config at {}", CONFIG_PATH, exception);
        }
    }

    private static float clampPosition(float value, float fallback) {
        if (!Float.isFinite(value)) {
            return fallback;
        }
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static final class Values {
        private long holdThresholdMs = DEFAULT_HOLD_THRESHOLD_MS;
        private float indicatorX = DEFAULT_INDICATOR_X;
        private float indicatorY = DEFAULT_INDICATOR_Y;

        private Values() {
        }

        private Values(long holdThresholdMs, float indicatorX, float indicatorY) {
            this.holdThresholdMs = holdThresholdMs;
            this.indicatorX = indicatorX;
            this.indicatorY = indicatorY;
        }
    }
}
