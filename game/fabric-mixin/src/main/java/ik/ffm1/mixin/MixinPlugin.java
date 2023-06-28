package ik.ffm1.mixin;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.loader.impl.FabricLoaderImpl;

public class MixinPlugin implements IMixinConfigPlugin {

    private String version;
    private Map<String, List<String>> plugins;

    @SuppressWarnings("unchecked")
    @Override
    public void onLoad(String mixinPackage) {
        String[] parts = FabricLoaderImpl.INSTANCE.getGameProvider().getRawGameVersion().split("\\.");

        this.version = String.join(".", parts[0], parts[1]);

        try {
            this.plugins = (Map<String, List<String>>) new Gson().fromJson(new InputStreamReader(MixinPlugin.class.getResourceAsStream("/plugins.json"), "UTF-8"), TypeToken.getParameterized(Map.class, String.class, TypeToken.getParameterized(List.class, String.class).getType()).getType());
        } catch (JsonIOException | JsonSyntaxException | UnsupportedEncodingException e) {}
    }

    @Override
    public String getRefMapperConfig() {
        return "mixin.${MOD_ID}.refmap." + this.version + ".json";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return this.plugins.get(this.version);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
