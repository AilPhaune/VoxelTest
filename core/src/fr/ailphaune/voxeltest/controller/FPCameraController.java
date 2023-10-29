package fr.ailphaune.voxeltest.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g3d.utils.FirstPersonCameraController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.entities.PlayerEntity;
import fr.ailphaune.voxeltest.utils.RotationUtils;

public class FPCameraController extends FirstPersonCameraController {

	public boolean pauseController = false;

	public int fastKey = Input.Keys.CONTROL_LEFT;

	public float speedMultiplier = 1;

	public float yaw = 0, pitch = 0, roll = 0;

	public PlayerEntity player;
	public boolean updatedPosition = false;
	public boolean updatedDirection = false;

	private Vector3 tempVec3 = new Vector3();
	
	protected final PerspectiveCamera camera;
	
	public FPCameraController(PerspectiveCamera camera) {
		this(camera, null);
	}

	public FPCameraController(PerspectiveCamera camera, PlayerEntity player) {
		super(camera);
		this.upKey = Input.Keys.SPACE;
		this.downKey = Input.Keys.SHIFT_LEFT;

		this.player = player;
		this.camera = camera;
	}

	public boolean updateLook() {
		float deltaX = Gdx.input.getDeltaX() * degreesPerPixel;
		float deltaY = Gdx.input.getDeltaY() * degreesPerPixel;
		/*
		 * camera.direction.rotate(camera.up, deltaX);
		 * tmp.set(camera.direction).crs(camera.up).nor(); camera.direction.rotate(tmp,
		 * deltaY);
		 */
		yaw += deltaX;
		yaw %= 360f;
		pitch = MathUtils.clamp(pitch + deltaY, -90f, 90f);

		camera.direction.set(0, 0, -1).unrotate(RotationUtils.matrixFromYawPitchRollDegrees(yaw, pitch, roll, camera.rotation));

		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		if (!Gdx.input.isCursorCatched() || this.pauseController) {
			return false;
		}
		/*
		 * float deltaX = -Gdx.input.getDeltaX() * degreesPerPixel; float deltaY =
		 * -Gdx.input.getDeltaY() * degreesPerPixel; yaw += deltaX; pitch += deltaY;
		 * tmp.set(0, 0, 1).crs(camera.up).nor(); camera.direction.rotate(tmp, deltaY);
		 * camera.direction.rotate(camera.up, deltaX); return true;
		 */
		return updateLook();
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (!Gdx.input.isCursorCatched() || this.pauseController) {
			return false;
		}
		return updateLook();
	}

	@Override
	public void update() {
		if (!Gdx.input.isCursorCatched() || this.pauseController) {
			return;
		}
		super.update();
	}

	@Override
	public void update(float deltaTime) {
		float velocity = keys.containsKey(fastKey) ? 2 * this.velocity : this.velocity;
		velocity *= this.speedMultiplier;

		Vector3 positionToUpdate = player == null ? camera.position : player.getPosition(tempVec3);
		updatedPosition = false;

		if (keys.containsKey(forwardKey)) {
			tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if (keys.containsKey(backwardKey)) {
			tmp.set(camera.direction.x, 0, camera.direction.z).nor().scl(-deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if (keys.containsKey(strafeLeftKey)) {
			tmp.set(camera.direction.x, 0, camera.direction.z).crs(camera.up).nor().scl(-deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if (keys.containsKey(strafeRightKey)) {
			tmp.set(camera.direction.x, 0, camera.direction.z).crs(camera.up).nor().scl(deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if (keys.containsKey(upKey)) {
			tmp.set(0, 1, 0).scl(deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if (keys.containsKey(downKey)) {
			tmp.set(0, -1, 0).scl(deltaTime * velocity);
			positionToUpdate.add(tmp);
			updatedPosition = true;
		}
		if(updatedPosition && player != null) {
			player.setPosition(positionToUpdate);
		}
		if (autoUpdate)
			camera.update(true);
	}
}