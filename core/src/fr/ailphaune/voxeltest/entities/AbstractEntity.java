package fr.ailphaune.voxeltest.entities;

import java.util.Objects;
import java.util.UUID;

import com.badlogic.gdx.math.Vector3;

public abstract class AbstractEntity {

	protected Vector3 position;
	protected final UUID uuid;

	/**
	 * Creates an instance of an entity
	 * 
	 * @paral uuid The unique id of this entity
	 * @param position A {@link Vector3} representing this entity's position
	 */
	public AbstractEntity(UUID uuid, Vector3 position) {
		Objects.requireNonNull(uuid, "A non null UUID is mandatory for the creation of an instance if AbstractEntity");
		this.position = position;
		this.uuid = uuid;
	}

	public abstract void tick();

	protected Vector3 getPosition() {
		return position;
	}

	public Vector3 getPosition(Vector3 out) {
		return out.set(position);
	}

	public Vector3 setPosition(Vector3 newPosition) {
		return position.set(newPosition);
	}

	public Vector3 addPosition(Vector3 add) {
		return position.add(add);
	}

	public Vector3 addPosition(float x, float y, float z) {
		return position.add(x, y, z);
	}

	public UUID getUUID() {
		return uuid;
	}
}