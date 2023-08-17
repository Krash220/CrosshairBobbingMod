package krash220.xbob.game.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import krash220.xbob.loader.utils.FabricQuiltUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class Config {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, Boolean> DEFAULT = new LinkedHashMap<>();
    private static final Map<String, Boolean> CONFIG = new LinkedHashMap<>();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void load() {
        File config = new File(FabricQuiltUtils.getConfigDir(), "${MOD_ID}.json");

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

        try (FileOutputStream fis = new FileOutputStream(new File(FabricQuiltUtils.getConfigDir(), "${MOD_ID}.json"))) {
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

    public static void registerGui() {}

    public static class ModMenu implements ModMenuApi {

        @Override
        public ConfigScreenFactory<?> getModConfigScreenFactory() {
            return previous -> new ConfigScreen(previous);
        }
    }

    private static class ConfigScreen extends Screen {

        private static final Text VALUE_ENABLE = new TranslatableText("${MOD_ID}.config.value.enable").formatted(Formatting.GREEN);
        private static final Text VALUE_DISABLE = new TranslatableText("${MOD_ID}.config.value.disable").formatted(Formatting.DARK_RED);

        public static Text getValueText(String key) {
            boolean bool = CONFIG.get(key).booleanValue();

            return new TranslatableText("%s: %s", new TranslatableText("${MOD_ID}.config." + key), bool ? VALUE_ENABLE : VALUE_DISABLE);
        }

        private Screen previous;

        public ConfigScreen(Screen previous) {
            super(new TranslatableText("${MOD_ID}.config.title"));

            this.previous = previous;
        }

        @Override
        public void onClose() {
            save();

            MinecraftClient.getInstance().openScreen(this.previous);
        }

        @Override
        protected void init() {
            int i = this.height / 6 - 12;

            for (String key : DEFAULT.keySet()) {
                this.addButton(new ButtonWidget(this.width / 2 - 150, i, 300, 20, getValueText(key), btn -> {
                    set(key, !check(key));

                    btn.setMessage(getValueText(key));
                }));

                i += 24;
            }

            this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 27, 200, 20, ScreenTexts.DONE, btn -> this.onClose()));
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            this.renderBackground(matrices);
            drawCenteredText(matrices, textRenderer, this.title, this.width / 2, 15, 0xFFFFFF);
            super.render(matrices, mouseX, mouseY, delta);
        }
    }
}
