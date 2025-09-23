package com.hexvane.strangematter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import com.hexvane.strangematter.world.ShadowLightProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
    
    @Inject(method = "getBrightness", at = @At("RETURN"), cancellable = true)
    private void onGetBrightness(LightLayer lightLayer, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        ShadowLightProvider provider = ShadowLightProvider.getInstance((ServerLevel) (Object) this);
        
        int originalLight = cir.getReturnValue();
        int modifiedLight = provider.getModifiedLightLevel(pos, lightLayer, originalLight);
        
        if (modifiedLight != originalLight) {
            cir.setReturnValue(modifiedLight);
            System.out.println("SERVER LEVEL BRIGHTNESS MODIFIED: " + originalLight + " -> " + modifiedLight + " (" + lightLayer + ") at " + pos);
        }
    }
}
