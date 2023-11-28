package git.hashibutogarasu.playerhead.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Environment(EnvType.CLIENT)
public class HeadsSelectionScreen extends Screen {
    static final Identifier SLOT_TEXTURE = new Identifier("gamemode_switcher/slot");
    static final Identifier SELECTION_TEXTURE = new Identifier("gamemode_switcher/selection");
    static final Identifier TEXTURE = new Identifier("textures/gui/container/gamemode_switcher.png");
    private static final int UI_WIDTH = HeadSelection.VALUES.toArray().length * 31 - 5;
    private static final Text SELECT_NEXT_TEXT;
    private final Optional<HeadSelection> currentHeadSelection = HeadSelection.of(this.getPreviousHeadSelection().isPresent() ? this.getPreviousHeadSelection().get().getPlayer().getUuid() : null);
    private Optional<HeadSelection> headSelection = Optional.empty();
    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseUsedForSelection;
    private final List<HeadsSelectionScreen.ButtonWidget> gameModeButtons = Lists.newArrayList();
    private final List<AbstractClientPlayerEntity> playerEntities;

    public HeadsSelectionScreen(List<AbstractClientPlayerEntity> playerEntities) {
        super(NarratorManager.EMPTY);
        this.playerEntities = playerEntities;
    }

    private @NotNull Optional<HeadSelection> getPreviousHeadSelection() {
        if (playerEntities != null) {
            if(!playerEntities.isEmpty()){
                return HeadSelection.of(playerEntities.get(0).getUuid());
            }
        }
        return Optional.empty();
    }

    protected void init() {
        super.init();
        HeadSelection.VALUES.add(MinecraftClient.getInstance().player);
        Optional<AbstractClientPlayerEntity> player = Optional.of(playerEntities.get(0));
        player.ifPresent(serverPlayerEntity -> this.headSelection = this.currentHeadSelection.isPresent() ? this.currentHeadSelection : HeadSelection.of(serverPlayerEntity.getUuid()));

        for(int i = 0; i < Arrays.stream(HeadSelection.VALUES.toArray()).count(); ++i) {
            var value = HeadSelection.of(HeadSelection.VALUES.get(i).getUuid());
            HeadSelection gameModeSelection = null;

            if (client != null) {
                if (client.player != null) {
                    if(value.isPresent()){
                        gameModeSelection = value.get();
                    }
                }
            }
            if(gameModeSelection != null){
                this.gameModeButtons.add(new HeadsSelectionScreen.ButtonWidget(gameModeSelection, this.width / 2 - UI_WIDTH / 2 + i * 31, this.height / 2 - 31));
            }
        }

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!this.checkForClose() && this.headSelection.isPresent()) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            context.getMatrices().push();
            RenderSystem.enableBlend();
            RenderSystem.setShaderTexture(0, TEXTURE);
            int i = this.width / 2 - 62;
            int j = this.height / 2 - 31 - 27;
            context.drawTexture(TEXTURE, i, j, 0.0F, 0.0F, 125, 75, 128, 128);
            context.getMatrices().pop();
            super.render(context, mouseX, mouseY, delta);

            if(headSelection.get().player != null){
                context.drawCenteredTextWithShadow(this.textRenderer, headSelection.get().player.getName(), this.width / 2, this.height / 2 - 31 - 20, -1);
            }

            context.drawCenteredTextWithShadow(this.textRenderer, SELECT_NEXT_TEXT, this.width / 2, this.height / 2 + 5, 16777215);
            if (!this.mouseUsedForSelection) {
                this.lastMouseX = mouseX;
                this.lastMouseY = mouseY;
                this.mouseUsedForSelection = true;
            }

            boolean bl = this.lastMouseX == mouseX && this.lastMouseY == mouseY;

            for (ButtonWidget buttonWidget : this.gameModeButtons) {
                buttonWidget.render(context, mouseX, mouseY, delta);
                this.headSelection.ifPresent((headSelection) -> buttonWidget.setSelected(headSelection == buttonWidget.headselection));
                if (!bl && buttonWidget.isHovered()) {
                    this.headSelection = Optional.of(buttonWidget.headselection);
                }
            }

        }
    }

    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void apply() {
        if (this.client != null) {
            apply(this.client, this.headSelection);
        }
    }

    private static void apply(MinecraftClient client, Optional<HeadSelection> headselection) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (client.interactionManager != null && client.player != null && headselection.isPresent() && server != null) {
            Optional<HeadSelection> optional = HeadSelection.of(server.getPlayerManager().getPlayerList().get(0).getUuid());
            HeadSelection headSelection = headselection.get();
            if (optional.isPresent() && client.player.hasPermissionLevel(2) && headSelection != optional.get()) {
                String command = headSelection.getCommand();
                if(command != null){
                    client.player.networkHandler.sendCommand(command);
                }
            }
        }
    }

    private boolean checkForClose() {
        if(client != null){
            if (!InputUtil.isKeyPressed(this.client.getWindow().getHandle(), 295)) {
                this.apply();
                this.client.setScreen(null);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 295 && this.headSelection.isPresent()) {
            this.mouseUsedForSelection = false;
            this.headSelection = this.headSelection.get().next();
            return true;
        } else {
            return false;
        }
    }

    public boolean shouldPause() {
        return false;
    }

    static {
        SELECT_NEXT_TEXT = Text.translatable("debug.gamemodes.select_next", Text.translatable("debug.gamemodes.press_f6").formatted(Formatting.AQUA));
    }

    @Environment(EnvType.CLIENT)
    private static class HeadSelection {
        ServerPlayerEntity player;
        final UUID uuid;
        final ItemStack icon;
        public static List<PlayerEntity> VALUES = new ArrayList<>();
        private HeadSelection(UUID uuid, ItemStack icon) {
            VALUES = new ArrayList<>();
            MinecraftServer server = MinecraftClient.getInstance().getServer();
            this.uuid = uuid;
            if (server != null) {
                player = server.getPlayerManager().getPlayer(uuid);
            }
            this.icon = icon;

        }

        @Nullable
        public String getCommand(){
            if(player != null){
                return "give @s minecraft:player_head{SkullOwner:\"" + player.getName().getString() + "\"}";
            }
            return null;
        }

        void renderIcon(DrawContext context, int x, int y) {
            context.drawItem(icon, x, y);
        }

        ServerPlayerEntity getPlayer() {
            return this.player;
        }

        Optional<HeadSelection> next() {
            return Optional.empty();
        }

        static Optional<HeadSelection> of(UUID uuid) {
            ItemStack item = Items.PLAYER_HEAD.getDefaultStack();
            ClientWorld world = MinecraftClient.getInstance().world;
            if (world != null) {
                String playername = null;
                for(PlayerEntity player : world.getPlayers()){
                    if(player.getUuid() == uuid){
                        playername = player.getName().getString();
                    }
                }
                if(playername != null){
                    NbtCompound nbtCompound = new NbtCompound();
                    nbtCompound.putString("SkullOwner", playername);
                    item.setNbt(nbtCompound);
                }
            }

            return Optional.of(new HeadSelection(uuid, item));
        }
    }

    @Environment(EnvType.CLIENT)
    public class ButtonWidget extends ClickableWidget {
        final HeadSelection headselection;
        private boolean selected;

        public ButtonWidget(HeadSelection headselection, int x, int y) {
            super(x, y, 26, 26, headselection.player.getName());
            this.headselection = headselection;
        }

        public void appendClickableNarrations(NarrationMessageBuilder builder) {
            this.appendDefaultNarrations(builder);
        }

        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            this.drawBackground(context);
            if (HeadsSelectionScreen.this.client != null) {
                this.headselection.renderIcon(context, this.getX() + 5, this.getY() + 5);
            }

            if (this.selected) {
                context.drawGuiTexture(HeadsSelectionScreen.SELECTION_TEXTURE, this.getX(), this.getY(), 26, 26);
                this.drawSelectionBox(context);
            }
            else{
                context.drawGuiTexture(HeadsSelectionScreen.SLOT_TEXTURE, this.getX(), this.getY(), 26, 26);
            }
        }

        public boolean isHovered() {
            return super.isHovered() || this.selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        private void drawBackground(DrawContext context) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderTexture(0, HeadsSelectionScreen.TEXTURE);
            context.getMatrices().push();
            context.getMatrices().translate((float)this.getX(), (float)this.getY(), 0.0F);
            context.drawTexture(TEXTURE, 0, 0, 0.0F, 75.0F, 26, 26, 128, 128);
            context.getMatrices().pop();
        }

        private void drawSelectionBox(DrawContext context){
            RenderSystem.setShaderTexture(0, HeadsSelectionScreen.TEXTURE);
            context.getMatrices().push();
            context.getMatrices().translate((float)this.getX(), (float)this.getY(), 0.0F);
            context.drawTexture(TEXTURE, 0, 0, 26.0F, 75.0F, 26, 26, 128, 128);
            context.getMatrices().pop();

        }
    }
}