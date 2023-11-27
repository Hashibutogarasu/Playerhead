package git.hashibutogarasu.playerhead.mixin;

import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class WorldMixin {
    @Inject(at = @At("RETURN"), method = "addPlayer")
    private void addPlayer(int id, AbstractClientPlayerEntity player, CallbackInfo ci) {
        PlayerheadClient.server = player.getServer();
    }
}
