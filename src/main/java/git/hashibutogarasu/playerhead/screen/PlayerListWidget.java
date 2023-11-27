package git.hashibutogarasu.playerhead.screen;

import com.google.common.collect.Lists;
import git.hashibutogarasu.playerhead.ListType;
import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import git.hashibutogarasu.playerhead.keybindings.Keybindings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

@Environment(value=EnvType.CLIENT)
public class PlayerListWidget
        extends AlwaysSelectedEntryListWidget<PlayerListWidget.Entry> {
    private final PlayerGiveScreen screen;
    private final List<PlayerEntry> players = Lists.newArrayList();
    private final Entry scanningEntry = new ScanningEntry();

    public PlayerListWidget(PlayerGiveScreen screen, MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
        super(client, width, height, top, bottom, entryHeight);
        this.screen = screen;

        if(PlayerheadClient.config != null){
            if(PlayerheadClient.config.last_listtype == ListType.SERVER_PLAYERS){
                MinecraftServer server = MinecraftClient.getInstance().getServer();
                if(server != null){
                    this.setPlayers(server.getPlayerNames());
                }
            }
            else if(PlayerheadClient.config.last_listtype == ListType.FAVORITED){
                this.setPlayers(PlayerheadClient.LoadConfig().favorited_players);
            }
        }
    }

    public void updateEntries() {
        this.clearEntries();
        this.players.forEach(this::addEntry);
        this.addEntry(this.scanningEntry);
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Entry entry = this.getSelectedOrNull();
        return entry != null && entry.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void moveSelection(EntryListWidget.MoveDirection direction) {
        this.moveSelectionIf(direction, entry -> !(entry instanceof ScanningEntry));
    }

    public void setPlayers(String[] players) {
        this.players.clear();
        for (int i = 0; i < Arrays.stream(players).count(); ++i) {
            this.players.add(new PlayerEntry(this.screen, players[i]));
        }
        this.updateEntries();
    }

    public void setPlayers(List<String> players) {
        this.players.clear();
        if(players != null){
            for (int i = 0; i < (long) players.size(); ++i) {
                this.players.add(new PlayerEntry(this.screen, players.get(i)));
            }
            this.updateEntries();
        }
    }

    public void clearPlayers(){
        this.players.clear();
        this.updateEntries();
    }

    @Override
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 30;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 85;
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    @Environment(value=EnvType.CLIENT)
    public static class ScanningEntry extends Entry {

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        }

        @Override
        public Text getNarration() {
            return ScreenTexts.EMPTY;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> {
    }

    @Environment(value=EnvType.CLIENT)
    public static class PlayerEntry extends Entry {
        private final PlayerGiveScreen screen;
        private final MinecraftClient client;
        public final String playername;

        private final ItemStack playerskull;
        private final ItemRenderer itemRenderer;

        protected PlayerEntry(PlayerGiveScreen screen, String playername) {
            this.itemRenderer = MinecraftClient.getInstance().getItemRenderer();
            this.screen = screen;
            this.playername = playername;
            this.client = MinecraftClient.getInstance();

            playerskull = new ItemStack(Items.PLAYER_HEAD);
            NbtCompound nbtCompound = new NbtCompound();
            nbtCompound.putString("SkullOwner",playername);
            playerskull.setNbt(nbtCompound);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            if(hovered){
                if (PlayerheadClient.config != null && PlayerheadClient.config.last_listtype == ListType.FAVORITED) {
                    this.screen.renderTooltip(matrices, Text.translatable("playerhead.tooltip.removefavorite", "delete"), mouseX, mouseY);
                }
            }
            this.client.textRenderer.draw(matrices, this.playername, (float)(x + 35), (float)(y + 2), 0xFFFFFF);
            this.itemRenderer.renderGuiItemIcon(playerskull, (x + 275), (y - 1));
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (Screen.hasShiftDown()) {
                PlayerGiveScreen playerGiveScreen = this.screen.playerGiveScreen;
                int i = playerGiveScreen.children().indexOf(this);
                if (i == -1) {
                    return true;
                }
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            this.screen.select(this);
            return false;
        }

        @Override
        public Text getNarration() {
            return Text.translatable("narrator.select", this.playername);
        }
    }
}