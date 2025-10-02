package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.menu.BaseMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Base class for machine screens that provides common functionality for GUI rendering.
 * This provides reusable GUI logic for all Strange Matter machines.
 */
public abstract class BaseMachineScreen<T extends BaseMachineMenu> extends AbstractContainerScreen<T> {
    
    protected final T menu;
    protected final ResourceLocation texture;
    
    public BaseMachineScreen(T menu, Inventory playerInventory, Component title, ResourceLocation texture) {
        super(menu, playerInventory, title);
        this.menu = menu;
        this.texture = texture;
    }
    
    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelX = (this.imageWidth - this.font.width(this.playerInventoryTitle)) / 2;
    }
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // Render texture at 1:1 scale to match the actual texture dimensions
        guiGraphics.blit(texture, x, y, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x26bdba, false);
        
        // Render machine-specific labels (implemented by subclasses)
        renderMachineLabels(guiGraphics, mouseX, mouseY);
    }
    
    /**
     * Override this to add machine-specific labels and information.
     */
    protected abstract void renderMachineLabels(GuiGraphics guiGraphics, int mouseX, int mouseY);
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    /**
     * Get the machine inventory for subclasses to access.
     */
    protected net.minecraft.world.Container getMachineInventory() {
        return this.menu.getMachineInventory();
    }
    
    /**
     * Render energy bar at the specified position using the reusable component.
     * This method can be called by subclasses to easily add energy bars to their screens.
     */
    protected void renderEnergyBar(GuiGraphics guiGraphics, int x, int y, int energyStored, int maxEnergyStored) {
        EnergyBarRenderer.renderEnergyBar(guiGraphics, x, y, energyStored, maxEnergyStored);
    }
    
    /**
     * Check if mouse is over energy bar and render tooltip if so.
     * This method can be called by subclasses to handle energy bar tooltips.
     */
    protected boolean renderEnergyBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int energyStored, int maxEnergyStored) {
        if (EnergyBarRenderer.isMouseOverEnergyBar(mouseX, mouseY, x, y)) {
            Component tooltip = EnergyBarRenderer.getEnergyBarTooltip(energyStored, maxEnergyStored);
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            return true;
        }
        return false;
    }

    /**
     * Render progress bar at the specified position using the reusable component.
     * This method can be called by subclasses to easily add progress bars to their screens.
     */
    protected void renderProgressBar(GuiGraphics guiGraphics, int x, int y, int progressLevel, int maxProgressLevel, ResourceLocation texture) {
        ProgressBarRenderer.renderProgressBar(guiGraphics, x, y, progressLevel, maxProgressLevel, texture);
    }

    /**
     * Check if mouse is over progress bar and render tooltip if so.
     * This method can be called by subclasses to handle progress bar tooltips.
     */
    protected boolean renderProgressBarTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int progressLevel, int maxProgressLevel, Component tooltipOverride) {
        if (ProgressBarRenderer.isMouseOverProgressBar(mouseX, mouseY, x, y)) {
            Component tooltip = tooltipOverride != null ? tooltipOverride : ProgressBarRenderer.getProgressBarTooltip(progressLevel, maxProgressLevel);
            guiGraphics.renderTooltip(this.font, tooltip, mouseX, mouseY);
            return true;
        }
        return false;
    }
}
