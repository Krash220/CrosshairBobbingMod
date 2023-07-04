package krash220.xbob.game.api.math;

import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

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
    }

    public void pop() {
        this.mat.pop();
    }

    public void translate(double x, double y, double z) {
        this.mat.translate(x, y, z);
    }

    public void rotate(float angle, float x, float y, float z) {
        this.mat.multiply(new Vec3f(x, y, z).getDegreesQuaternion(angle));
    }

    public void scale(float x, float y, float z) {
        this.mat.scale(x, y, z);
    }

    public void identity() {
        this.mat.peek().getModel().loadIdentity();
    }

    public float[] multiplyVector(float x, float y, float z, float w) {
        Vector4f vec = new Vector4f(x, y, z, w);

        vec.transform(this.mat.peek().getModel());

        return new float[] {vec.getX(), vec.getY(), vec.getZ(), vec.getW()};
    }
}
