package git.hashibutogarasu.playerhead.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import git.hashibutogarasu.playerhead.ListType;
import git.hashibutogarasu.playerhead.configs.Config;
import git.hashibutogarasu.playerhead.keybindings.Keybindings;
import git.hashibutogarasu.playerhead.screen.PlayerGiveScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;

public class PlayerheadClient implements ClientModInitializer {
    public static Logger clientlogger = LoggerFactory.getLogger("playerheadclient");
    public static Config config = new Config();
    private static final File config_directory = new File("config");

    private static final Path config_file_path = Path.of("config/playerhead.json");
    private static final File config_file = new File("config/playerhead.json");

    @Override
    public void onInitializeClient() {
        RegisterKeyBinds();
        SaveAndWriteConfigs();
    }

    public static String GetJsonFromClass(Config config){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(config);
    }

    public static void SaveConfig(Config config, StandardOpenOption standardOpenOption){
        try {
            if(!config_file.exists()){
                Files.createFile(config_file_path);
            }
            else{
                BufferedWriter bw = Files.newBufferedWriter(config_file_path, standardOpenOption);
                if(config != null) config.favorited_players = new ArrayList<>(new HashSet<>(config.favorited_players));
                bw.write(GetJsonFromClass(config));
                PlayerheadClient.clientlogger.info(GetJsonFromClass(config));
                bw.close();
            }
        } catch (IOException ignored) {

        }
    }

    public static Config LoadConfig(){
        if(!config_file.exists()) {
            try {
                writedummy();
            } catch (IOException ignored) {

            }
        }

        Gson gson = new Gson();
        String json = null;
        try {
            json = Files.readString(config_file_path);
        } catch (IOException ignored) {

        }

        if(json.isEmpty()){
            try {
                writedummy();
            } catch (IOException ignored) {

            }
        }

        PlayerheadClient.config = gson.fromJson(json, Config.class);
        if(PlayerheadClient.config != null) PlayerheadClient.config.favorited_players = new ArrayList<>(new HashSet<>(PlayerheadClient.config.favorited_players));
        return PlayerheadClient.config;
    }

    private static void writedummy() throws IOException {
        Config dummy = new Config();
        dummy.favorited_players = new ArrayList<>();
        SaveConfig(dummy, StandardOpenOption.WRITE);
    }

    private void RegisterKeyBinds(){
        Keybindings keybindings = new Keybindings();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keybindings.playergivescreen.wasPressed()) {
                MinecraftClient.getInstance().setScreen(new PlayerGiveScreen(MinecraftClient.getInstance().getSession().getProfile().getName()));
            }
        });
    }

    private void SaveAndWriteConfigs(){
        if(!config_directory.exists()) try {
            Files.createDirectory(config_directory.toPath());
        } catch (IOException ignored) {

        }
        PlayerheadClient.config = LoadConfig();
        PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
    }
}
