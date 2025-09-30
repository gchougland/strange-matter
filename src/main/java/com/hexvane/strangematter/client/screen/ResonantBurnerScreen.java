package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.menu.ResonantBurnerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ResonantBurnerScreen extends BaseMachineScreen<ResonantBurnerMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/gui/resonant_burner.png");
    private static final ResourceLocation PROGRESS_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/flame_progress.png");
    
    // Energy bar position (at the top of the screen)
    private static final int ENERGY_BAR_X = 8;
    private static final int ENERGY_BAR_Y = -18;
    
    // Flame progress bar position (below the fuel slot)
    private static final int FLAME_PROGRESS_X = 80;
    private static final int FLAME_PROGRESS_Y = 42;

    public ResonantBurnerScreen(ResonantBurnerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
        this.imageWidth = 176;
        this.imageHeight = 148;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        
        // Render energy bar and flame progress bar using the reusable components
        if (this.menu.getDataAccess() != null) {
            int energyStored = this.menu.getDataAccess().get(0);
            int maxEnergyStored = this.menu.getDataAccess().get(1);
            int burnTime = this.menu.getDataAccess().get(5);
            int burnDuration = this.menu.getDataAccess().get(6);
            
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            
            renderEnergyBar(guiGraphics, x + ENERGY_BAR_X, y + ENERGY_BAR_Y, energyStored, maxEnergyStored);
            renderProgressBar(guiGraphics, x + FLAME_PROGRESS_X, y + FLAME_PROGRESS_Y, burnTime, burnDuration, PROGRESS_TEXTURE);
        }
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Render title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x26bdba, false);
        
        // Render machine-specific labels (implemented by subclasses)
        renderMachineLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderMachineLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // No machine-specific labels needed - progress is shown as a visual bar
    }
    
    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        
        // Check for energy bar and flame progress bar tooltips
        if (this.menu.getDataAccess() != null) {
            int energyStored = this.menu.getDataAccess().get(0);
            int maxEnergyStored = this.menu.getDataAccess().get(1);
            int burnTime = this.menu.getDataAccess().get(5);
            int burnDuration = this.menu.getDataAccess().get(6);
            
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            
            renderEnergyBarTooltip(guiGraphics, mouseX, mouseY, x + ENERGY_BAR_X, y + ENERGY_BAR_Y, energyStored, maxEnergyStored);
            renderProgressBarTooltip(guiGraphics, mouseX, mouseY, x + FLAME_PROGRESS_X, y + FLAME_PROGRESS_Y, burnTime, burnDuration, 
                Component.translatable("gui.strangematter.burn_progress", burnTime, burnDuration));
        }
    }
}
