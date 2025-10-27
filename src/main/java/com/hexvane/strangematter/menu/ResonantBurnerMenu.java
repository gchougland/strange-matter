package com.hexvane.strangematter.menu;

import com.hexvane.strangematter.block.ResonantBurnerBlockEntity;
import com.hexvane.strangematter.menu.slots.FuelSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ContainerData;

public class ResonantBurnerMenu extends BaseMachineMenu {
    private final ResonantBurnerBlockEntity blockEntity;
    private final ContainerData dataAccess;

    public ResonantBurnerMenu(int windowId, net.minecraft.world.level.Level world, net.minecraft.core.BlockPos pos, Inventory inventory, net.minecraft.world.entity.player.Player player) {
        super(com.hexvane.strangematter.StrangeMatterMod.RESONANT_BURNER_MENU.get(), windowId, inventory,
            world.getBlockEntity(pos) instanceof ResonantBurnerBlockEntity be ? be : null, 1);
        this.blockEntity = world.getBlockEntity(pos) instanceof ResonantBurnerBlockEntity be ? be : null;
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;

        // Add the ContainerData to the menu for synchronization
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
        }
    }

    public ResonantBurnerMenu(int windowId, Inventory inventory, net.minecraft.network.FriendlyByteBuf buf) {
        this(windowId, inventory, buf.readBlockPos());
    }

    public ResonantBurnerMenu(int windowId, Inventory inventory, net.minecraft.core.BlockPos pos) {
        this(windowId, inventory.player.level(), pos, inventory, inventory.player);
    }
    
    // Client-side constructor that properly gets the block entity
    public ResonantBurnerMenu(int windowId, Inventory inventory, ResonantBurnerBlockEntity blockEntity) {
        super(com.hexvane.strangematter.StrangeMatterMod.RESONANT_BURNER_MENU.get(), windowId, inventory,
            blockEntity, 1);
        this.blockEntity = blockEntity;
        this.dataAccess = this.blockEntity != null ? this.blockEntity.getDataAccess() : null;

        // Add the ContainerData to the menu for synchronization
        if (this.dataAccess != null) {
            this.addDataSlots(this.dataAccess);
        }
    }
    
    // Client-side constructor for when block entity is not available
    public ResonantBurnerMenu(int windowId, Inventory inventory) {
        super(com.hexvane.strangematter.StrangeMatterMod.RESONANT_BURNER_MENU.get(), windowId, inventory, 1);
        this.blockEntity = null;
        
        // Create a dummy ContainerData for client-side synchronization
        this.dataAccess = new ContainerData() {
            private final int[] data = new int[7]; // 7 elements to match BaseMachineBlockEntity
            
            @Override
            public int get(int index) {
                return index >= 0 && index < data.length ? data[index] : 0;
            }
            
            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < data.length) {
                    data[index] = value;
                }
            }
            
            @Override
            public int getCount() {
                return data.length;
            }
        };
        
        // Add the ContainerData to the menu for synchronization
        this.addDataSlots(this.dataAccess);
    }
    
    @Override
    protected void addMachineSlots() {
        // Add single fuel slot (machine slot 0) - positioned to match the GUI texture
        if (blockEntity != null) {
            this.addSlot(new FuelSlot(blockEntity, 0, 80, 24)); // Fuel slot in the center
        } else if (machineInventory != null) {
            this.addSlot(new FuelSlot(machineInventory, 0, 80, 24)); // Fuel slot in the center
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
            this.addSlot(new FuelSlot(dummyContainer, 0, 80, 24)); // Fuel slot in the center
        }
    }

    public ResonantBurnerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
    
    public ContainerData getDataAccess() {
        return this.dataAccess;
    }
    
    @Override
    protected void addPlayerSlots(Inventory playerInventory) {
        // Add player inventory (slots after machine slots) - positioned to match the GUI texture
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 9 + col * 18, 68 + row * 17));
            }
        }
        
        // Add player hotbar - positioned to match the GUI texture
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 9 + col * 18, 124));
        }
    }
}
