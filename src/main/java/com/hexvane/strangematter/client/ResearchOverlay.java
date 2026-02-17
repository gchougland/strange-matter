package com.hexvane.strangematter.client;

import com.hexvane.strangematter.research.ResearchTypeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResearchOverlay {
    private static final List<ResearchNotification> notifications = new ArrayList<>();
    private static final int NOTIFICATION_DURATION = 100;
    private static final int FADE_DURATION = 20;
    private static long lastTickTime = 0;
    
    public static void showResearchGain(String typeId, int amount) {
        notifications.add(new ResearchNotification(typeId, amount, NOTIFICATION_DURATION));
    }
    
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (notifications.isEmpty()) return;
        
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
            
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
            if (ResearchTypeHelper.hasIconTexture(notification.getTypeId())) {
                ResourceLocation icon = ResearchTypeHelper.getIconResourceLocation(notification.getTypeId());
                if (icon != null) {
                    guiGraphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);
                }
            } else if (ResearchTypeHelper.hasIconItem(notification.getTypeId())) {
                guiGraphics.renderItem(ResearchTypeHelper.getIconItem(notification.getTypeId()), x, y);
            }
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);
            guiGraphics.drawString(minecraft.font, "+" + notification.getAmount(), x + 20, y + 4, 0xFFFFFF);
            index++;
        }
    }
    
    private static class ResearchNotification {
        private final String typeId;
        private final int amount;
        private int ticksRemaining;
        
        public ResearchNotification(String typeId, int amount, int duration) {
            this.typeId = typeId;
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
        
        public String getTypeId() {
            return typeId;
        }
        
        public int getAmount() {
            return amount;
        }
    }
}
