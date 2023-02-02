package git.hashibutogarasu.playerhead.keybindings;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public KeyBinding playergivescreen;

    public Keybindings(){
        playergivescreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerhead.show.playergivescreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "category.playerhead.keybinds"
        ));
    }
}
