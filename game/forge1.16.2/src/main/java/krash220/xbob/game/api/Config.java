package krash220.xbob.game.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Boolean> DEFAULT = new LinkedHashMap<>();
    private static final Map<String, Boolean> CONFIG = new HashMap<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void load() {
        File config = new File(FMLPaths.CONFIGDIR.get().toFile(), "${MOD_ID}.json");

        if (!config.exists()) {
            save();
        }

        try (FileInputStream fis = new FileInputStream(config)) {
            Map map = GSON.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8), Map.class);

            CONFIG.putAll(map);
        } catch (IOException e) {}
    }

    public static void save() {
        String cfg = GSON.toJson(CONFIG);

        try (FileOutputStream fis = new FileOutputStream(new File(FMLPaths.CONFIGDIR.get().toFile(), "${MOD_ID}.json"))) {
            fis.write(cfg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {}
    }

    public static void define(String key, Boolean defaultValue) {
        DEFAULT.put(key, defaultValue);
        CONFIG.put(key, defaultValue);
    }

    public static void set(String key, Boolean value) {
        CONFIG.put(key, value);
    }

    public static boolean check(String key) {
        Boolean val = CONFIG.get(key);

        if (val != null) {
            return val.booleanValue();
        } else {
            return DEFAULT.get(key).booleanValue();
        }
    }

    public static void registerGui() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> (mc, previous) -> new ConfigScreen(mc, previous));
    }

    private static class ConfigScreen extends Screen {

        private static final ITextComponent VALUE_ENABLE = new TranslationTextComponent("${MOD_ID}.config.value.enable").withStyle(TextFormatting.GREEN);
        private static final ITextComponent VALUE_DISABLE = new TranslationTextComponent("${MOD_ID}.config.value.disable").withStyle(TextFormatting.DARK_RED);

        public static ITextComponent getValueText(String key) {
            boolean bool = CONFIG.get(key).booleanValue();

            return new TranslationTextComponent("%s: %s", new TranslationTextComponent("${MOD_ID}.config." + key), bool ? VALUE_ENABLE : VALUE_DISABLE);
        }

        private Minecraft mc;
        private Screen previous;

        public ConfigScreen(Minecraft mc, Screen previous) {
            super(new TranslationTextComponent("${MOD_ID}.config.title"));

            this.mc = mc;
            this.previous = previous;
        }

        @Override
        public void onClose() {
            save();

            this.mc.setScreen(this.previous);
        }

        @Override
        protected void init() {
            int i = this.height / 6 - 12;

            for (String key : DEFAULT.keySet()) {
                this.addButton(new Button(this.width / 2 - 150, i, 300, 20, getValueText(key), btn -> {
                    set(key, !check(key));

                    btn.setMessage(getValueText(key));
                }));

                i += 24;
            }

            this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, btn -> this.onClose()));
        }

        @Override
        public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            this.renderBackground(matrix);
            drawCenteredString(matrix, this.font, this.title, this.width / 2, 15, 0xFFFFFF);
            super.render(matrix, mouseX, mouseY, partialTicks);
        }
    }
}
