// SPDX-License-Identifier: LGPL-3.0-or-later
// Copyright (c) 2026 Xogue

package com.xogue.adaptivesneak;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class SneakIndicatorConfigScreen extends Screen {
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;

    public SneakIndicatorConfigScreen() {
        super(Component.translatable("screen.adaptive_sneak.indicator.title"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, width, height, 0x88000000);
        graphics.centeredText(font, title, width / 2, 18, 0xFFFFFFFF);
        graphics.centeredText(font, Component.translatable("screen.adaptive_sneak.indicator.help"),
                width / 2, 32, 0xFFBDBDBD);

        int x = SneakIndicator.xForWidth(width);
        int y = SneakIndicator.yForHeight(height);
        SneakIndicator.draw(graphics, x, y, true, dragging || isInside(mouseX, mouseY, x, y));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int x = SneakIndicator.xForWidth(width);
        int y = SneakIndicator.yForHeight(height);
        if (event.button() == 0 && isInside(event.x(), event.y(), x, y)) {
            dragging = true;
            dragOffsetX = event.x() - x;
            dragOffsetY = event.y() - y;
            setDragging(true);
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging && event.button() == 0) {
            updatePosition(event.x() - dragOffsetX, event.y() - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging && event.button() == 0) {
            updatePosition(event.x() - dragOffsetX, event.y() - dragOffsetY);
            dragging = false;
            setDragging(false);
            AdaptiveSneakConfig.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        AdaptiveSneakConfig.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updatePosition(double x, double y) {
        int availableWidth = Math.max(1, width - SneakIndicator.SIZE);
        int availableHeight = Math.max(1, height - SneakIndicator.SIZE);
        float normalizedX = (float) (Math.max(0.0, Math.min(availableWidth, x)) / availableWidth);
        float normalizedY = (float) (Math.max(0.0, Math.min(availableHeight, y)) / availableHeight);
        AdaptiveSneakConfig.setIndicatorPosition(normalizedX, normalizedY);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + SneakIndicator.SIZE
                && mouseY >= y && mouseY < y + SneakIndicator.SIZE;
    }
}
