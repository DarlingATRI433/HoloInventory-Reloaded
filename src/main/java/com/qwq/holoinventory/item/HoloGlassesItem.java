package com.qwq.holoinventory.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Equipable;
import org.jetbrains.annotations.NotNull;

public class HoloGlassesItem extends Item implements Equipable {
    public HoloGlassesItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }
}
