// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SneakIndicator {
    public static final int SIZE = 24;

    private static final int ACTIVE_BACKGROUND = 0xC0225A36;
    private static final int INACTIVE_BACKGROUND = 0xA0222222;
    private static final int ACTIVE_COLOR = 0xFFFFFFFF;
    private static final int INACTIVE_COLOR = 0xFF9A9A9A;

    private SneakIndicator() {
    }

    public static void renderHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        int x = xForWidth(graphics.guiWidth());
        int y = yForHeight(graphics.guiHeight());
        draw(graphics, x, y, client.player.isCrouching(), false);
    }

    public static void draw(GuiGraphicsExtractor graphics, int x, int y, boolean sneaking, boolean selected) {
        int background = sneaking ? ACTIVE_BACKGROUND : INACTIVE_BACKGROUND;
        int color = sneaking ? ACTIVE_COLOR : INACTIVE_COLOR;

        graphics.fill(x, y, x + SIZE, y + SIZE, background);
        graphics.outline(x, y, SIZE, SIZE, selected ? 0xFFFFFFFF : 0xAA000000);

        // A tiny pixel-art player: upright when inactive and visibly crouched when active.
        if (sneaking) {
            graphics.fill(x + 6, y + 5, x + 11, y + 10, color);  // head
            graphics.fill(x + 9, y + 10, x + 17, y + 14, color); // bent body
            graphics.fill(x + 14, y + 14, x + 19, y + 17, color);
            graphics.fill(x + 7, y + 14, x + 14, y + 17, color); // bent leg
            graphics.fill(x + 5, y + 17, x + 10, y + 20, color);
            graphics.fill(x + 17, y + 17, x + 21, y + 20, color);
        } else {
            graphics.fill(x + 9, y + 4, x + 15, y + 10, color);  // head
            graphics.fill(x + 10, y + 10, x + 14, y + 17, color); // body
            graphics.fill(x + 6, y + 11, x + 10, y + 14, color);  // arms
            graphics.fill(x + 14, y + 11, x + 18, y + 14, color);
            graphics.fill(x + 8, y + 17, x + 11, y + 21, color);  // legs
            graphics.fill(x + 13, y + 17, x + 16, y + 21, color);
        }
    }

    public static int xForWidth(int width) {
        return Math.round(AdaptiveSneakConfig.indicatorX() * Math.max(0, width - SIZE));
    }

    public static int yForHeight(int height) {
        return Math.round(AdaptiveSneakConfig.indicatorY() * Math.max(0, height - SIZE));
    }
}
