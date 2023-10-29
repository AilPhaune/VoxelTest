package fr.ailphaune.voxeltest.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class RotationUtils {

	public static final float deg2rad = MathUtils.degreesToRadians;
	
	private static Matrix4 tempMat = new Matrix4();
	
	public static Vector3 directionFromYawPitch(float yaw, float pitch, Vector3 out) {
		float xzLen = MathUtils.cos(pitch);
		return out.set(xzLen * MathUtils.cos(yaw), MathUtils.sin(pitch), xzLen * MathUtils.sin(-yaw)).nor();
	}

	public static Vector3 directionFromYawPitchDegrees(float yaw, float pitch, Vector3 direction) {
		return directionFromYawPitch(yaw * deg2rad, pitch * deg2rad, direction);
	}

	public static Matrix4 matrixFromYawPitchRoll(float yaw, float pitch, float roll, Matrix4 rotation) {
		rotation.idt().rotateRad(1, 0, 0, pitch)
		.mul(tempMat.idt().rotateRad(0, 0, -1, roll))
		.mul(tempMat.idt().rotateRad(0, 1, 0, yaw));
		return rotation;
	}

	public static Matrix4 matrixFromYawPitchRollDegrees(float yaw, float pitch, float roll, Matrix4 rotation) {
		return matrixFromYawPitchRoll(yaw * deg2rad, pitch * deg2rad, roll * deg2rad, rotation);
	}
}