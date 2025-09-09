package com.hexvane.strangematter.client;

import com.hexvane.strangematter.research.ResearchType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResearchOverlay {
    private static final List<ResearchNotification> notifications = new ArrayList<>();
    private static final int NOTIFICATION_DURATION = 100; // 5 seconds at 20 TPS
    private static final int FADE_DURATION = 20; // 1 second fade
    private static long lastTickTime = 0;
    
    public static void showResearchGain(ResearchType type, int amount) {
        notifications.add(new ResearchNotification(type, amount, NOTIFICATION_DURATION));
    }
    
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (notifications.isEmpty()) return;
        
        // Only tick once per game tick (50ms = 20 TPS)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTickTime >= 50) {
            lastTickTime = currentTime;
            Iterator<ResearchNotification> iterator = notifications.iterator();
            while (iterator.hasNext()) {
                ResearchNotification notification = iterator.next();
                notification.tick();
                if (notification.isExpired()) {
                    iterator.remove();
                }
            }
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        
        // Position notifications in top-right corner
        int startX = screenWidth - 120;
        int startY = 20;
        
        Iterator<ResearchNotification> renderIterator = notifications.iterator();
        int index = 0;
        
        while (renderIterator.hasNext()) {
            ResearchNotification notification = renderIterator.next();
            
            int x = startX;
            int y = startY + (index * 25);
            
            float alpha = notification.getAlpha();
            if (alpha <= 0) {
                continue;
            }
            
            // No background - just the text and icon
            
            // Render research icon
            ResourceLocation icon = notification.getResearchType().getIconResourceLocation();
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
            guiGraphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);
            
            // Render text
            String text = "+" + notification.getAmount();
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
            guiGraphics.drawString(minecraft.font, text, x + 20, y + 4, 0xFFFFFF);
            
            index++;
        }
    }
    
    private static class ResearchNotification {
        private final ResearchType researchType;
        private final int amount;
        private int ticksRemaining;
        
        public ResearchNotification(ResearchType researchType, int amount, int duration) {
            this.researchType = researchType;
            this.amount = amount;
            this.ticksRemaining = duration;
        }
        
        public void tick() {
            ticksRemaining--;
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        public float getAlpha() {
            if (ticksRemaining > FADE_DURATION) {
                return 1.0f;
            }
            return (float) ticksRemaining / FADE_DURATION;
        }
        
        public ResearchType getResearchType() {
            return researchType;
        }
        
        public int getAmount() {
            return amount;
        }
    }
}
