package git.hashibutogarasu.playerhead.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import git.hashibutogarasu.playerhead.client.PlayerheadClient;
import git.hashibutogarasu.playerhead.skin.SkinUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.util.Clipboard;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

import io.github.hashibutogarasu.mojangapiutil.MojangAPIUtil;

public class PlayerGiveScreen extends Screen {
    private TextFieldWidget searchBox;
    private File skinfile;

    private static int identify = 0;

    public Identifier rawSkin;
    public boolean oldSkin;
    public File skin_file;
    private String before_search_text;
    private ButtonWidget givebutton;

    public PlayerGiveScreen(String before_search_text){
        this(Text.translatable("playerhead.screen.playergivescreen"));
        deleteFilesRecursively(new File("skin_cache"));
        this.before_search_text = before_search_text;

        this.skinfile = new File("skin_cache/skin.png");
        this.rawSkin = new Identifier("skinswapper_raw:" + identify); //raw skin file uploaded to minecraft.net

        try {
            new MojangAPIUtil(before_search_text).getSkinfromName("./skin_cache/","skin","png");
        } catch (IOException | InterruptedException ignored) {

        }

        if(this.skinfile.exists()){
            NativeImageBackedTexture rawImageBackedTexture = new NativeImageBackedTexture(getNativeImage(skinfile));
            MinecraftClient.getInstance().getTextureManager().registerTexture(rawSkin, rawImageBackedTexture);
        }
    }

    private boolean deleteFilesRecursively(File rootFile) {
        File[] allFiles = rootFile.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                deleteFilesRecursively(file);
            }
        }
        System.out.println("Remove file: " + rootFile.getPath());
        return rootFile.delete();
    }

    private PlayerGiveScreen(Text title) {
        super(title);
    }


    @Override
    protected void init() {
        this.searchBox = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, (this.width / 2) - 150, 30, 300, 20, Text.literal(before_search_text));
        this.searchBox.setText(before_search_text);
        this.searchBox.setChangedListener(search -> {
            if(!search.isEmpty()) before_search_text = search;
        });

        givebutton = ButtonWidget.builder(Text.translatable("playerhead.widget.givebutton"),(button)->{
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            new Clipboard().setClipboard(window, "/give @s player_head{SkullOwner:" + before_search_text + "} 1");

            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.message.copied.description"), null);
            MinecraftClient.getInstance().setScreen(new PlayerGiveScreen(before_search_text));
        }).position((this.width / 2) - 150, 110).size(300,20).build();

        this.addDrawable(searchBox);
        this.addSelectableChild(givebutton);
        this.setInitialFocus(this.searchBox);

        if(!PlayerheadClient.tutorial_flag){
            SystemToast.show(MinecraftClient.getInstance().getToastManager(), SystemToast.Type.TUTORIAL_HINT, Text.translatable("playerhead.tutorial.howtouse.description"), null);
            PlayerheadClient.tutorial_flag = true;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == GLFW.GLFW_KEY_ENTER && this.searchBox.isActive() && !this.searchBox.getText().equals("")){
            PlayerheadClient.clientlogger.info(before_search_text);
            MinecraftClient.getInstance().setScreen(new PlayerGiveScreen(before_search_text));
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(0);
        this.searchBox.render(matrices, mouseX, mouseY, delta);

        if(skinfile != null && !before_search_text.isEmpty()) renderIcon((this.width / 2) - 16, 55, matrices);

        SelectWorldScreen.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);

        givebutton.render(matrices, mouseX, mouseY, delta);
    }

    private void renderIcon(int k, int j, MatrixStack matrices) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, rawSkin);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if(oldSkin) {
            //head icon
            DrawableHelper.drawTexture(matrices, k, j, 32.0F, 32.0F, 32, 32, 256, 128);

            //Notch's skin uses black instead of transparency
            if(!skin_file.getName().equals("Notch.png")) {
                DrawableHelper.drawTexture(matrices, k, j, 160.0F, 32.0F, 32, 32, 256, 128);
            }
        }
        else {
            //head icon
            DrawableHelper.drawTexture(matrices, k, j, 32.0F, 32.0F, 32, 32, 256, 256);
            DrawableHelper.drawTexture(matrices, k, j, 160.0F, 32.0F, 32, 32, 256, 256);
        }
    }

    public NativeImage getNativeImage(final File folder) {
        NativeImage nativeImage;
        boolean oldSkin;

        nativeImage = SkinUtils.toNativeImage(folder);

        if (nativeImage != null) {

            //checks dimensions of file
            if (nativeImage.getWidth() == 64 && nativeImage.getHeight() == 32) {
                oldSkin = true;
            } else if (nativeImage.getWidth() == 64 && nativeImage.getHeight() == 64) {
                oldSkin = false;
            }
            return nativeImage;
        }

        return null;
    }

    @Override
    public void close() {
        before_search_text = MinecraftClient.getInstance().getSession().getProfile().getName();
        super.close();
    }
}
