package git.hashibutogarasu.playerhead.screen;

import git.hashibutogarasu.playerhead.ListType;
import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import git.hashibutogarasu.playerhead.keybindings.Keybindings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;

public class PlayerGiveScreen extends Screen {
    public TextFieldWidget searchBox;
    public String before_search_text;
    private ButtonWidget givebutton;
    public ButtonWidget changetypebutton;
    public PlayerGiveScreen playerGiveScreen;
    private ItemStack playerskull;
    private PlayerListWidget playerListWidget;
    private Text changebutton_text = Text.translatable("playerhead.widget.changetypebutton.server_players");

    private TexturedButtonWidget delete_all_button;
    private MinecraftServer server = null;

    public PlayerGiveScreen(String before_search_text){
        this(Text.translatable("playerhead.screen.playergivescreen"));
        server = MinecraftClient.getInstance().getServer();
        PlayerheadClient.config = PlayerheadClient.LoadConfig();
        this.before_search_text = before_search_text;
        updateSkull();

        if(PlayerheadClient.config != null){
            if(PlayerheadClient.config.last_listtype == null) PlayerheadClient.config.last_listtype = ListType.SERVER_PLAYERS;
        }
        PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
    }

    private PlayerGiveScreen(Text title) {
        super(title);
    }


    @Override
    protected void init() {
        this.searchBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 142, this.height - 50, 260, 20, Text.literal(before_search_text));
        this.searchBox.setText(before_search_text);
        this.searchBox.setChangedListener(search -> {
            if(!search.isEmpty()) before_search_text = search;
            updateSkull();
        });

        this.searchBox.setTooltip(Tooltip.of(Text.translatable("playerhead.tooltip.searchbox")));

        this.playerListWidget = new PlayerListWidget(this, this.client,this.width, this.height,32, this.height - 64,20);
        PlayerheadClient.config = PlayerheadClient.LoadConfig();

        this.givebutton = ButtonWidget.builder(Text.translatable("playerhead.widget.givebutton"),(button)->{
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            new Clipboard().setClipboard(window, "/give @s player_head{SkullOwner:" + this.searchBox.getText() + "}");
            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.message.copied.description"), null);
            MinecraftClient.getInstance().setScreen(null);
        }).position((this.width / 2) - 150, searchBox.getY() + 25).size(250,20).build();

        if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.FAVORITED)
            changebutton_text = Text.translatable("playerhead.widget.changetypebutton.server_players");



        this.changetypebutton = ButtonWidget.builder(changebutton_text,(button)->{
            if(PlayerheadClient.config != null){
                if(PlayerheadClient.config.last_listtype == ListType.SERVER_PLAYERS){
                    PlayerheadClient.config.last_listtype = ListType.FAVORITED;
                    button.setMessage(Text.translatable("playerhead.widget.changetypebutton.server_players"));
                    this.playerListWidget.setPlayers(PlayerheadClient.config.favorited_players);
                } else if (PlayerheadClient.config.last_listtype == ListType.FAVORITED) {
                    PlayerheadClient.config.last_listtype = ListType.SERVER_PLAYERS;
                    button.setMessage(Text.translatable("playerhead.widget.changetypebutton.favorited"));
                    if(server != null){
                        this.playerListWidget.setPlayers(server.getPlayerNames());
                    }
                }
            }
        }).position(this.givebutton.getWidth() + this.givebutton.getX() + 5, this.givebutton.getY()).size(80,20).build();

        this.delete_all_button = this.addDrawableChild(new TexturedButtonWidget(this.givebutton.getX() - 25, this.givebutton.getY(), 20, 20,0, 0, 20, new Identifier("playerhead:textures/icons/deleteall.png"), 20, 40, (button -> {
            PlayerheadClient.config.favorited_players = new ArrayList<>();
            if(PlayerheadClient.config.last_listtype == ListType.FAVORITED){
                this.playerListWidget.clearPlayers();
            }
            PlayerheadClient.SaveConfig(PlayerheadClient.config, StandardOpenOption.TRUNCATE_EXISTING);
            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.widget.delete_all_button.delete"), null);
        })));
        this.setVisible();
        this.delete_all_button.setTooltip(Tooltip.of(Text.translatable("playerhead.widget.delete_all_button.tooltip")));

        this.addSelectableChild(searchBox);
        this.addSelectableChild(changetypebutton);
        this.addSelectableChild(givebutton);
        this.addSelectableChild(playerListWidget);

        if(PlayerheadClient.config != null){
            if(!PlayerheadClient.config.tutorial_flag){
                SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.tutorial.howtouse.description"), null);
                PlayerheadClient.config.tutorial_flag = true;

                PlayerheadClient.SaveConfig(PlayerheadClient.config, StandardOpenOption.TRUNCATE_EXISTING);
            }
        }
    }

    public void select(PlayerListWidget.Entry entry) {
        this.playerListWidget.setSelected(entry);

        PlayerListWidget.Entry selected = this.playerListWidget.getSelectedOrNull();
        String playername = selected != null ? ((PlayerListWidget.PlayerEntry) selected).playername : "";

        if(playername != null){
            this.searchBox.setText(playername);
            this.before_search_text = playername;
            updateSkull();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ENTER && this.searchBox.isActive() && !this.searchBox.getText().isEmpty()){
            updateSkull();
            if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.FAVORITED) {
                this.playerListWidget.setPlayers(PlayerheadClient.config.favorited_players);
                this.playerListWidget.updateEntries();
            }
        }
        else if(keyCode == KeyBindingHelper.getBoundKeyOf(Keybindings.addtofavorite).getCode()){
            PlayerListWidget.Entry selected = this.playerListWidget.getSelectedOrNull();
            if(selected != null){
                String playername = ((PlayerListWidget.PlayerEntry) selected).playername;
                if(playername != null) {
                    if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.SERVER_PLAYERS) {
                        PlayerheadClient.config.favorited_players.add(playername);
                    }

                    PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
                }
            }

            if(!this.searchBox.isActive()){
                if (PlayerheadClient.config != null) {
                    PlayerheadClient.config.favorited_players.add(this.searchBox.getText());
                }
                PlayerheadClient.config.favorited_players = new ArrayList<>(new HashSet<>(PlayerheadClient.config.favorited_players));

                if(PlayerheadClient.config.last_listtype == ListType.FAVORITED){
                    this.playerListWidget.setPlayers(PlayerheadClient.config.favorited_players);
                    this.playerListWidget.updateEntries();
                }

                PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (PlayerheadClient.config != null) {
                PlayerheadClient.config.favorited_players.remove(this.searchBox.getText());
            }
            this.playerListWidget.setPlayers(PlayerheadClient.config.favorited_players);
            PlayerheadClient.config.favorited_players = new ArrayList<>(new HashSet<>(PlayerheadClient.config.favorited_players));
            this.playerListWidget.updateEntries();

            PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(0);
        this.playerListWidget.render(matrices, mouseX, mouseY, delta);
        this.givebutton.render(matrices, mouseX, mouseY, delta);
        this.changetypebutton.render(matrices, mouseX, mouseY, delta);
        this.searchBox.render(matrices, mouseX, mouseY, delta);

        if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.FAVORITED) {
            this.delete_all_button.active = true;
            this.delete_all_button.visible = true;
            this.delete_all_button.render(matrices, mouseX, mouseY, delta);
        }

        if(!this.searchBox.getText().isEmpty()) {
            this.itemRenderer.renderGuiItemIcon(playerskull,this.searchBox.getX() + this.searchBox.getWidth() + 10, this.searchBox.getY());
        }

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void close() {
        this. before_search_text = MinecraftClient.getInstance().getSession().getProfile().getName();
        PlayerheadClient.SaveConfig(PlayerheadClient.config,StandardOpenOption.TRUNCATE_EXISTING);
        super.close();
    }

    private void updateSkull(){
        this.playerskull = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("SkullOwner", this.searchBox != null ? this.searchBox.getText() : MinecraftClient.getInstance().getSession().getProfile().getName());
        this.playerskull.setNbt(nbtCompound);
    }

    private void setVisible(){
        if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.SERVER_PLAYERS) {
            this.delete_all_button.visible = false;
            this.delete_all_button.active = false;
        }
    }
}
