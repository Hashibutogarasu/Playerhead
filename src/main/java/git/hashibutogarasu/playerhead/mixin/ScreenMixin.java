package git.hashibutogarasu.playerhead.mixin;


import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import git.hashibutogarasu.playerhead.keybindings.Keybindings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.StandardOpenOption;
import java.util.List;

@Mixin(CreativeInventoryScreen.class)
public class ScreenMixin {
    @Unique
    private ItemStack rendereditem;
    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private void renderTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        rendereditem = stack;
    }

    @Inject(method = "keyPressed", at = @At("RETURN"))
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if(keyCode == KeyBindingHelper.getBoundKeyOf(Keybindings.addtofavorite).getCode()){
            PlayerheadClient.clientlogger.info(rendereditem.getTranslationKey());

            if (rendereditem.getNbt() != null) {
                NbtCompound subnbt = rendereditem.getSubNbt("SkullOwner");
                if(subnbt != null){
                    NbtElement Name = subnbt.get("Name");
                    if(Name != null){
                        String playername = Name.asString();
                        if (PlayerheadClient.config != null) {
                            PlayerheadClient.config.favorited_players.add(playername);
                        }
                    }
                }
                PlayerheadClient.SaveConfig(PlayerheadClient.config, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }
}
