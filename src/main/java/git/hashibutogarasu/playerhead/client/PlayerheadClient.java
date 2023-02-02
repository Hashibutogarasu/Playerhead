package git.hashibutogarasu.playerhead.client;

import git.hashibutogarasu.playerhead.keybindings.Keybindings;
import git.hashibutogarasu.playerhead.screen.PlayerGiveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerheadClient implements ClientModInitializer {
    public static Logger clientlogger = LoggerFactory.getLogger("playerheadclient");
    public static boolean tutorial_flag = false;

    @Override
    public void onInitializeClient() {
        try {
            Files.createDirectory(Paths.get("./skin_cache"));
        } catch (IOException ignored) {

        }

        Keybindings keybindings = new Keybindings();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybindings.playergivescreen.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new PlayerGiveScreen(MinecraftClient.getInstance().getSession().getProfile().getName()));
            }
        });
    }
}
