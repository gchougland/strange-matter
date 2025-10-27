package com.hexvane.strangematter.event;

import com.hexvane.strangematter.StrangeMatterMod;
import com.hexvane.strangematter.entity.BaseAnomalyEntity;
import com.hexvane.strangematter.item.FieldScannerItem;
import com.hexvane.strangematter.research.ResearchData;
import com.hexvane.strangematter.research.ScannableObject;
import com.hexvane.strangematter.research.ScannableObjectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

public class FieldScannerEventHandler {
    
    // Track when "already scanned" messages were last sent to prevent spam
    private static final java.util.Map<String, Long> lastAlreadyScannedMessage = new java.util.HashMap<>();
    
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof BaseAnomalyEntity anomaly) {
            Player player = event.getEntity();
            
            // Check both hands for the field scanner
            ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
            
            FieldScannerItem scanner = null;
            ItemStack scannerStack = null;
            
            if (mainHand.getItem() instanceof FieldScannerItem) {
                scanner = (FieldScannerItem) mainHand.getItem();
                scannerStack = mainHand;
            } else if (offHand.getItem() instanceof FieldScannerItem) {
                scanner = (FieldScannerItem) offHand.getItem();
                scannerStack = offHand;
            }
            
            if (scanner != null && scannerStack != null) {
                // Only handle if not already scanning and not on cooldown to prevent spam
                if (!scanner.isScanning(scannerStack) && !scanner.isOnCooldown(scannerStack)) {
                    InteractionHand hand = (mainHand.getItem() instanceof FieldScannerItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    scanner.onInteractWithEntity(scannerStack, player, anomaly, hand);
                }
                event.setCanceled(true);
            }
        }
    }
    
    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        BlockState blockState = event.getLevel().getBlockState(pos);
        
        // Check both hands for the field scanner
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        
        FieldScannerItem scanner = null;
        ItemStack scannerStack = null;
        
        if (mainHand.getItem() instanceof FieldScannerItem) {
            scanner = (FieldScannerItem) mainHand.getItem();
            scannerStack = mainHand;
        } else if (offHand.getItem() instanceof FieldScannerItem) {
            scanner = (FieldScannerItem) offHand.getItem();
            scannerStack = offHand;
        }
        
        if (scanner != null && scannerStack != null) {
            // Check if the block is scannable
            if (ScannableObjectRegistry.isBlockScannable(blockState)) {
                Optional<ScannableObject> scannable = ScannableObjectRegistry.getScannableForBlock(blockState);
                if (scannable.isPresent()) {
                    String objectId = scannable.get().generateObjectId(pos);
                    ResearchData researchData = ResearchData.get(player);
                    
                    if (!researchData.hasScanned(objectId)) {
                        // Only start scanning if not already scanning and not on cooldown to prevent spam
                        if (!scanner.isScanning(scannerStack) && !scanner.isOnCooldown(scannerStack)) {
                            InteractionHand hand = (mainHand.getItem() instanceof FieldScannerItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                            scanner.onInteractWithBlock(scannerStack, player, pos, blockState, scannable.get(), hand);
                        }
                        event.setCanceled(true);
                    } else {
                        // Let the FieldScannerItem handle the "already scanned" message with proper cooldown
                        if (!scanner.isScanning(scannerStack) && !scanner.isOnCooldown(scannerStack)) {
                            InteractionHand hand = (mainHand.getItem() instanceof FieldScannerItem) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                            scanner.onInteractWithBlock(scannerStack, player, pos, blockState, scannable.get(), hand);
                        }
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}
