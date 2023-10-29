package fr.ailphaune.voxeltest.entities;

import java.util.UUID;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.DamageSource;
import fr.ailphaune.voxeltest.data.Hitbox;
import fr.ailphaune.voxeltest.data.world.World;

public class LivingEntity extends AbstractEntity {

	public long hp;
	public boolean dead = false;
	public float yaw, pitch;
	
	public LivingEntity(UUID uuid, Vector3 position) {
		super(uuid, position);
	}
	
	public LivingEntity(UUID uuid, Vector3 position, long hp) {
		super(uuid, position);
		this.hp = hp;
	}

	@Override
	public void tick() {
		
	}
	
	public void damage(long damage, DamageSource source, World world) {
		this.hp -= damage;
		if(this.hp <= 0) {
			this.hp = 0;
			this.dead = true;
		}
	}
	
	public Hitbox getHitbox() {
		return null;
	}
}