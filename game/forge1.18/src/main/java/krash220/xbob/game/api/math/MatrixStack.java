package krash220.xbob.game.api.math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

public class MatrixStack {

    public final PoseStack mat;
    
    public MatrixStack() {
        this.mat = new PoseStack();
    }

    public MatrixStack(PoseStack mat) {
        this.mat = mat;
    }

    public void push() {
        this.mat.pushPose();
    }

    public void pop() {
        this.mat.popPose();
    }

    public void translate(double x, double y, double z) {
        this.mat.translate(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        this.mat.mulPose(new Vector3f(x, y, z).rotationDegrees(angle));
    }

    public void scale(float x, float y, float z) {
        this.mat.scale(x, y, z);
    }

    public void identity() {
        this.mat.last().pose().setIdentity();
    }

    public float[] multiplyVector(float x, float y, float z, float w) {
        Vector4f vec = new Vector4f(x, y, z, w);

        vec.transform(this.mat.last().pose());

        return new float[] {vec.x(), vec.y(), vec.z(), vec.w()};
    }
}
