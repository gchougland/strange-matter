package com.hexvane.strangematter.item;

import net.minecraft.world.item.ArmorItem;

public class TinfoilHatItem extends ArmorItem {
    
    public TinfoilHatItem() {
        super(TinfoilHatArmorMaterial.TINFOIL_HAT, ArmorItem.Type.HELMET, new Properties().durability(TinfoilHatArmorMaterial.TINFOIL_HAT.getDurabilityForType(ArmorItem.Type.HELMET)));
    }
}

