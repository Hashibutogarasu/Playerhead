package git.hashibutogarasu.playerhead.mixin;

import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class WorldMixin {
    @Inject(at = @At("RETURN"), method = "addEntity")
    private void addPlayer(Entity entity, CallbackInfo ci) {
        if(entity.isPlayer()){
            PlayerheadClient.server = entity.getServer();
        }
    }
}
