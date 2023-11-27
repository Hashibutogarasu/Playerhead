package git.hashibutogarasu.playerhead.keybindings;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybindings {
    public static KeyBinding playergivescreen;
    public static KeyBinding addtofavorite;
    public static KeyBinding openheadsselection;

    public Keybindings(){

    }

    public static void register(){
        playergivescreen = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerhead.show.playergivescreen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_COMMA,
                "category.playerhead.keybinds"
        ));

        addtofavorite = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerhead.show.addtofavorite",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.playerhead.keybinds"
        ));

        openheadsselection = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerhead.show.openheadsselection",
                InputUtil.Type.SCANCODE,
                GLFW.GLFW_KEY_H,
                "category.playerhead.keybinds"
        ));
    }
}
