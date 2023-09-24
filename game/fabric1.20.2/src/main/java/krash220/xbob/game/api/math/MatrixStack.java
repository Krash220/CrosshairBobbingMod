package krash220.xbob.game.api.math;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.util.math.RotationAxis;

public class MatrixStack {

    public final net.minecraft.client.util.math.MatrixStack mat;

    public MatrixStack() {
        this.mat = new net.minecraft.client.util.math.MatrixStack();
    }

    public MatrixStack(net.minecraft.client.util.math.MatrixStack mat) {
        this.mat = mat;
    }

    public void push() {
        this.mat.push();
        RenderSystem.applyModelViewMatrix();
    }

    public void pop() {
        this.mat.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public void translate(double x, double y, double z) {
        this.mat.translate(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        this.mat.multiply(RotationAxis.of(new Vector3f(x, y, z)).rotationDegrees(angle));
    }

    public void scale(float x, float y, float z) {
        this.mat.scale(x, y, z);
    }

    public void identity() {
        this.mat.peek().getPositionMatrix().identity();
    }

    public float[] multiplyVector(float x, float y, float z, float w) {
        Vector4f vec = new Vector4f(x, y, z, w);

        vec.mul(this.mat.peek().getPositionMatrix());

        return new float[] {vec.x, vec.y, vec.z, vec.w};
    }
}
