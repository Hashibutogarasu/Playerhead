package git.hashibutogarasu.playerhead.mixin;

import git.hashibutogarasu.playerhead.screen.HeadsSelectionScreen;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(at = @At("RETURN"), method = "processF3")
    private void processDebugKeys(int key, CallbackInfoReturnable<Boolean> cir) {
        if (key == 295) {
            if(client.player != null){
                if (!this.client.player.hasPermissionLevel(2)) {
                    this.client.inGameHud.getChatHud().addMessage(Text.empty().append(Text.translatable("debug.prefix").formatted(Formatting.YELLOW, Formatting.BOLD)).append(" ").append(Text.translatable("debug.playerheads.error").getString()));
                }
                else if (this.client.player != null && this.client.world != null) {
                    this.client.setScreen(new HeadsSelectionScreen(client.world.getPlayers()));
                }
            }
        }
    }
}
