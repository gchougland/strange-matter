package com.hexvane.strangematter.client.screen;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.block.RealityForgeBlockEntity;
import com.hexvane.strangematter.menu.RealityForgeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealityForgeScreen extends BaseMachineScreen<RealityForgeMenu> {
    
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/gui/reality_forge.png");
    private static final ResourceLocation EJECT_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/ui/eject_button.png");
    
    // Shard colors
    private static final int SHADOW_COLOR = 0xFF8752d0;
    private static final int GRAVITY_COLOR = 0xFFd75335;
    private static final int ENERGY_COLOR = 0xFF3abde8;
    private static final int COGNITION_COLOR = 0xFF54c55b;
    private static final int TIME_COLOR = 0xFFe6b538;
    private static final int SPACE_COLOR = 0xFF3d88dd;
    
    private int animationTick = 0;
    private boolean ejectButtonHovered = false;
    private boolean ejectButtonPressed = false;
    
    public RealityForgeScreen(RealityForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, GUI_TEXTURE);
        this.imageWidth = 176;
        this.imageHeight = 148;
    }
    
    
    
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // Render spinning shards around output slot
        renderSpinningShards(guiGraphics, x + 130, y + 37, partialTick);
        
        
        // Render custom eject button (8x8 texture, positioned under shard slot and centered)
        renderEjectButton(guiGraphics, x + 41, y + 44, mouseX, mouseY);
    }
    
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        
        // Render machine-specific labels (implemented by subclasses)
        renderMachineLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderMachineLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // No machine-specific labels needed - title is handled by BaseMachineScreen
    }
    
    private void renderSpinningShards(GuiGraphics guiGraphics, int centerX, int centerY, float partialTick) {
        Map<String, Integer> storedShards = menu.getStoredShards();
        List<String> shardOrder = menu.getShardOrder();
        
        // Get synchronized shard count from ContainerData (like crafting progress)
        int synchronizedShardCount = 0;
        if (menu.getDataAccess() != null) {
            synchronizedShardCount = menu.getDataAccess().get(8); // Index 8 is total shard count
        }
        
        // Use synchronized data instead of block entity data
        if (synchronizedShardCount == 0) return;
        
        float time = (animationTick + partialTick) * 0.1f;
        int radius = 25;
        
        // Get crafting progress from synchronized data (like ResonantBurnerScreen)
        int craftTicks = 0;
        boolean isCrafting = false;
        if (menu.getDataAccess() != null) {
            isCrafting = menu.getDataAccess().get(9) == 1; // Index 9 is crafting state
            craftTicks = menu.getDataAccess().get(10); // Index 10 is craft progress
        }
        
        // Also don't render if crafting is complete (craftTicks >= 100)
        if (isCrafting && craftTicks >= 100) {
            return;
        }
        
        
        // Calculate coalescing progress (0-100)
        int coalesceProgress = isCrafting ? (craftTicks * 100) / 100 : 0; // CRAFT_TIME is 100
        boolean isCoalescing = isCrafting && craftTicks > 0;
        
        
        // Create a list of all individual shards to render
        List<String> allShards = new ArrayList<>();
        for (String shardType : shardOrder) {
            int count = storedShards.getOrDefault(shardType, 0);
            for (int i = 0; i < count; i++) {
                allShards.add(shardType);
            }
        }
        
        // Render each shard individually
        for (int i = 0; i < allShards.size(); i++) {
            String shardType = allShards.get(i);
            float angle = time + (i * 2.0f * (float) Math.PI / Math.max(allShards.size(), 1));
            
            int shardX, shardY;
            
            if (isCoalescing) {
                // Coalescing animation: shards move toward center
                float coalesceFactor = coalesceProgress / 100.0f;
                float currentRadius = radius * (1.0f - coalesceFactor);
                shardX = centerX + (int) (Math.cos(angle) * currentRadius);
                shardY = centerY + (int) (Math.sin(angle) * currentRadius);
            } else {
                // Normal spinning animation
                shardX = centerX + (int) (Math.cos(angle) * radius);
                shardY = centerY + (int) (Math.sin(angle) * radius);
            }
            
            // Render shard
            renderShard(guiGraphics, shardX, shardY, shardType);
            
            // Render particle trail
            renderParticleTrail(guiGraphics, shardX, shardY, shardType, angle, time);
        }
    }
    
    private void renderShard(GuiGraphics guiGraphics, int x, int y, String shardType) {
        ResourceLocation shardTexture = getShardTexture(shardType);
        guiGraphics.blit(shardTexture, x - 8, y - 8, 0, 0, 16, 16, 16, 16);
    }
    
    private void renderParticleTrail(GuiGraphics guiGraphics, int x, int y, String shardType, float angle, float time) {
        int color = getShardColor(shardType);
        
        // Create particle trail behind the shard
        for (int i = 1; i <= 5; i++) {
            float trailAngle = angle - (i * 0.2f);
            float trailRadius = 25 - (i * 3);
            int trailX = x + (int) (Math.cos(trailAngle) * trailRadius) - (int) (Math.cos(angle) * 25);
            int trailY = y + (int) (Math.sin(trailAngle) * trailRadius) - (int) (Math.sin(angle) * 25);
            
            // Render trail particle with alpha
            int alpha = 255 - (i * 40);
            guiGraphics.fill(trailX - 2, trailY - 2, trailX + 2, trailY + 2, (alpha << 24) | (color & 0xFFFFFF));
        }
    }
    
    private ResourceLocation getShardTexture(String shardType) {
        return switch (shardType) {
            case "energetic" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/energetic_shard.png");
            case "gravitic" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/gravitic_shard.png");
            case "chrono" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/chrono_shard.png");
            case "spatial" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/spatial_shard.png");
            case "shade" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/shade_shard.png");
            case "insight" -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/insight_shard.png");
            default -> ResourceLocation.fromNamespaceAndPath(StrangeMatterMod.MODID, "textures/item/energetic_shard.png");
        };
    }
    
    private int getShardColor(String shardType) {
        return switch (shardType) {
            case "energetic" -> ENERGY_COLOR;
            case "gravitic" -> GRAVITY_COLOR;
            case "chrono" -> TIME_COLOR;
            case "spatial" -> SPACE_COLOR;
            case "shade" -> SHADOW_COLOR;
            case "insight" -> COGNITION_COLOR;
            default -> ENERGY_COLOR;
        };
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
    
    private void renderEjectButton(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY) {
        // Check if mouse is hovering over the button (8x8 area)
        boolean hovered = mouseX >= x && mouseX < x + 8 && mouseY >= y && mouseY < y + 8;
        this.ejectButtonHovered = hovered;
        
        // Check if crafting is active
        boolean isCrafting = this.menu.isCrafting();
        
        // Render the 8x8 eject button texture
        guiGraphics.blit(EJECT_BUTTON_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);
        
        // Add visual feedback based on state
        if (isCrafting) {
            // Show locked state with gray overlay
            guiGraphics.fill(x, y, x + 8, y + 8, 0x80000000); // Semi-transparent black overlay
        } else if (hovered) {
            // Show hover effect when not crafting
            guiGraphics.fill(x, y, x + 8, y + 8, 0x80FFFFFF); // Semi-transparent white overlay
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        
        if (button == 0) { // Left click
            int guiX = (this.width - this.imageWidth) / 2;
            int guiY = (this.height - this.imageHeight) / 2;
            int buttonX = guiX + 41;
            int buttonY = guiY + 44;

            // Check if eject button was clicked
            if (mouseX >= buttonX && mouseX < buttonX + 8 &&
                mouseY >= buttonY && mouseY < buttonY + 8) {
                
                // Check if crafting is active - lock eject button during crafting
                if (this.menu.isCrafting()) {
                    return true; // Block the click
                }
                
                this.menu.ejectShards();
                this.ejectButtonPressed = true;
                
                // Immediately clear shards on client side for visual feedback
                if (this.menu.getBlockEntity() != null) {
                    this.menu.getBlockEntity().clearShardsForEject();
                }
                return true;
            }
        }
        
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        return result;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.ejectButtonPressed = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public void containerTick() {
        super.containerTick();
        animationTick++;
    }
}
