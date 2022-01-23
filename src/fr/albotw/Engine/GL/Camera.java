package fr.albotw.Engine.GL;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static fr.albotw.Engine.CONFIG.EPSILON;

public class Camera {
    private final Vector3f position;
    private final Vector3f target;
    private final Vector3f up;

    private double azimuth;
    private double polar;

    private double rotationRadius;

    public Camera(Vector3f position, double rotationRadius) {
        this.position = position;
        this.target = new Vector3f(0.0f, 0.0f, 0.0f);
        this.up = new Vector3f(0.0f, 1.0f, 0.0f);

        this.rotationRadius = rotationRadius;
        this.azimuth = 0.0f;
        this.polar = 0.0f;
        this.updatePosition();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(this.position, this.target, this.up);
    }

    public void rotateAzimuth(double angle) {
        this.azimuth += angle;
        this.azimuth = this.azimuth % (2 * Math.PI);
        if (this.azimuth < 0.0f) {
            this.azimuth = (2 * Math.PI) + this.azimuth;
        }
    }

    public void rotatePolar(double angle) {
        this.polar += angle;

        double cap = (float) (Math.PI / 2.0f - 0.001f);
        if (this.polar > cap) {
            this.polar = cap;
        }
        if (this.polar < -cap) {
            this.polar = -cap;
        }
    }

    public void rotate(double angleX, double angleY) {
        if (Math.abs(angleX) > EPSILON && Math.abs(angleY) > EPSILON) {
            angleX = -angleX;
            angleY = -angleY;

            rotateAzimuth(angleX);
            rotatePolar(angleY);
            updatePosition();
        }
    }

    public void updatePosition() {
        this.position.x = (float) (this.rotationRadius * Math.cos(this.polar) * Math.cos(this.azimuth));
        this.position.y = (float) (this.rotationRadius * Math.sin(this.polar));
        this.position.z = (float) (this.rotationRadius * Math.cos(this.polar) * Math.sin(this.azimuth));
    }

    public void zoomIn() {
        this.rotationRadius -= 0.1f;
        this.updatePosition();
    }

    public void zoomOut() {
        this.rotationRadius += 0.1f;
        this.updatePosition();
    }
}
