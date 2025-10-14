package com.hexvane.strangematter.menu;

import com.hexvane.strangematter.block.ResonanceCondenserBlockEntity;
import com.hexvane.strangematter.block.AnomalyMachineBlockEntity;
import com.hexvane.strangematter.menu.slots.OutputSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerData;

public class ResonanceCondenserMenu extends BaseMachineMenu {
    private final ResonanceCondenserBlockEntity blockEntity;
    private final ContainerData dataAccess;

    public ResonanceCondenserMenu(int windowId, net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, Inventory inventory, net.minecraft.world.entity.player.Player player) {
        super(com.hexvane.strangematter.StrangeMatterMod.RESONANCE_CONDENSER_MENU.get(), windowId, inventory,
            world.getBlockEntity(pos) instanceof ResonanceCondenserBlockEntity be ? be : null, 1);
        this.blockEntity = world.getBlockEntity(pos) instanceof ResonanceCondenserBlockEntity be ? be : null;
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;

        // Add the ContainerData to the menu for synchronization
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
        } else {
            System.out.println("Server-side: dataAccess is null!");
        }
    }

    public ResonanceCondenserMenu(int windowId, Inventory inventory, net.minecraft.network.FriendlyByteBuf buf) {
        this(windowId, inventory, buf.readBlockPos());
    }

    public ResonanceCondenserMenu(int windowId, Inventory inventory, net.minecraft.core.BlockPos pos) {
        this(windowId, inventory.player.level(), pos, inventory, inventory.player);
    }
    
    // Client-side constructor that properly gets the block entity
    public ResonanceCondenserMenu(int windowId, Inventory inventory, ResonanceCondenserBlockEntity blockEntity) {
        super(com.hexvane.strangematter.StrangeMatterMod.RESONANCE_CONDENSER_MENU.get(), windowId, inventory,
            blockEntity, 1);
        this.blockEntity = blockEntity;
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;

        // Add the ContainerData to the menu for synchronization
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
            System.out.println("Client-side: Added ContainerData with " + this.dataAccess.getCount() + " slots");
        } else {
            System.out.println("Client-side: dataAccess is null!");
        }
    }
    
    
    @Override
    protected void addMachineSlots() {
        // Add single output slot (machine slot 0) - positioned to match your custom GUI texture
        // Use the actual block entity if available, otherwise use the machine inventory from BaseMachineMenu
        if (blockEntity != null) {
            this.addSlot(new OutputSlot(blockEntity, 0, 80, 30)); // Adjust these coordinates to match your texture
        } else if (machineInventory != null) {
            this.addSlot(new OutputSlot(machineInventory, 0, 80, 30)); // Adjust these coordinates to match your texture
        } else {
            // Fallback: create a dummy container for the slot
            net.minecraft.world.Container dummyContainer = new net.minecraft.world.Container() {
                private final net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> items = net.minecraft.core.NonNullList.withSize(1, net.minecraft.world.item.ItemStack.EMPTY);
                
                @Override
                public int getContainerSize() { return 1; }
                
                @Override
                public boolean isEmpty() { return items.get(0).isEmpty(); }
                
                @Override
                public net.minecraft.world.item.ItemStack getItem(int index) { return items.get(index); }
                
                @Override
                public net.minecraft.world.item.ItemStack removeItem(int index, int count) { return net.minecraft.world.ContainerHelper.removeItem(items, index, count); }
                
                @Override
                public net.minecraft.world.item.ItemStack removeItemNoUpdate(int index) { return net.minecraft.world.ContainerHelper.takeItem(items, index); }
                
                @Override
                public void setItem(int index, net.minecraft.world.item.ItemStack stack) { items.set(index, stack); }
                
                @Override
                public void setChanged() {}
                
                @Override
                public boolean stillValid(net.minecraft.world.entity.player.Player player) { return false; }
                
                @Override
                public void clearContent() { items.clear(); }
            };
            this.addSlot(new OutputSlot(dummyContainer, 0, 80, 30)); // Adjust these coordinates to match your texture
        }
    }

    public ResonanceCondenserBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
    
    public ContainerData getDataAccess() {
        return this.dataAccess;
    }
    
    @Override
    protected void addPlayerSlots(Inventory playerInventory) {
        // Add player inventory (slots after machine slots) - positioned to match your custom GUI texture
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 9 + col * 18, 68 + row * 17)); // Adjust these coordinates to match your texture
            }
        }
        
        // Add player hotbar - positioned to match your custom GUI texture
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 9 + col * 18, 124)); // Adjust these coordinates to match your texture
        }
    }
}


