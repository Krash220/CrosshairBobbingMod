package krash220.xbob.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Camera;

@Mixin(Camera.class)
public interface CameraAccessor {

    @Accessor("cameraY")
    float getCameraY();

    @Accessor("lastCameraY")
    float getLastCameraY();
}
