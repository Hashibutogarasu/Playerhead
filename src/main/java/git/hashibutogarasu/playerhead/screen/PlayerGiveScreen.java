package git.hashibutogarasu.playerhead.screen;

import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class PlayerGiveScreen extends Screen {
    public TextFieldWidget searchBox;
    public String before_search_text;
    private ButtonWidget givebutton;
    public PlayerGiveScreen playerGiveScreen;
    private ItemStack playerskull;
    private PlayerListWidget playerListWidget;

    public PlayerGiveScreen(String before_search_text){
        this(Text.translatable("playerhead.screen.playergivescreen"));
        this.before_search_text = before_search_text;
        updateSkull();
    }

    private PlayerGiveScreen(Text title) {
        super(title);
    }


    @Override
    protected void init() {
        this.searchBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 150, this.height - 50, 270, 20, Text.literal(before_search_text));
        this.searchBox.setText(before_search_text);
        this.searchBox.setChangedListener(search -> {
            if(!search.isEmpty()) before_search_text = search;
        });

        this.addSelectableChild(searchBox);

        givebutton = ButtonWidget.builder(Text.translatable("playerhead.widget.givebutton"),(button)->{
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            new Clipboard().setClipboard(window, "/give @s player_head{SkullOwner:" + before_search_text + "}");

            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.message.copied.description"), null);
            MinecraftClient.getInstance().setScreen(null);
        }).position((this.width / 2) - 150, searchBox.getY() + 25).size(300,20).build();

        this.addDrawable(searchBox);
        this.addSelectableChild(givebutton);
        this.setInitialFocus(this.searchBox);

        if(!PlayerheadClient.tutorial_flag){
            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.tutorial.howtouse.description"), null);
            PlayerheadClient.tutorial_flag = true;
        }

        playerListWidget = this.addSelectableChild(new PlayerListWidget(this,client,this.width, this.height,32, this.height - 64,20));
        playerListWidget.setPlayers(client.getServer().getPlayerNames());
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
        if(keyCode == GLFW.GLFW_KEY_ENTER && this.searchBox.isActive() && !this.searchBox.getText().equals("")){
            MinecraftClient.getInstance().setScreen(new PlayerGiveScreen(before_search_text));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(0);
        playerListWidget.render(matrices, mouseX, mouseY, delta);
        givebutton.render(matrices, mouseX, mouseY, delta);
        searchBox.render(matrices, mouseX, mouseY, delta);

        if(!before_search_text.isEmpty()) {
            itemRenderer.renderGuiItemIcon(playerskull,this.searchBox.getX() + this.searchBox.getWidth() + 10, this.searchBox.getY());
        }

        drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void close() {
        before_search_text = MinecraftClient.getInstance().getSession().getProfile().getName();
        super.close();
    }

    private void updateSkull(){
        playerskull = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("SkullOwner",before_search_text);
        playerskull.setNbt(nbtCompound);
    }
}
