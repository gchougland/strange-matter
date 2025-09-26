package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.menu.ResonanceCondenserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ResonanceCondenserScreen extends BaseMachineScreen<ResonanceCondenserMenu> {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/gui/resonance_condenser.png");

    public ResonanceCondenserScreen(ResonanceCondenserMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, TEXTURE);
        this.imageWidth = 176;
        this.imageHeight = 148;
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
        // Render resonance level (energy absorption progress) - positioned to not overlap with slots
        if (this.menu.getDataAccess() != null) {
            int energyLevel = this.menu.getDataAccess().get(0);
            int maxEnergy = this.menu.getDataAccess().get(1);
            Component energyText = Component.literal("Energy: " + energyLevel + "/" + maxEnergy);
            guiGraphics.drawString(this.font, energyText, 8, 55, 0x26bdba, false);
        } else {
            Component energyText = Component.literal("Energy: 0/100");
            guiGraphics.drawString(this.font, energyText, 8, 55, 0x26bdba, false);
        }
    }
}
