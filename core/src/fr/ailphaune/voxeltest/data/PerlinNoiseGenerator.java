package fr.ailphaune.voxeltest.data;

import com.badlogic.gdx.math.RandomXS128;

public class PerlinNoiseGenerator {

	private static final RandomXS128 RNG = new RandomXS128();
	
	public PerlinNoiseGenerator() {
		this(0);
	}
	
	public PerlinNoiseGenerator(long seed) {
		RNG.setSeed(seed);
		seed(RNG);
	}
	
	public PerlinNoiseGenerator(long seed0, long seed1) {
		RNG.setState(seed0, seed1);
		seed(RNG);
	}
	
	public PerlinNoiseGenerator(RandomXS128 rng) {
		seed(rng);
	}
	
	private int[] permutation = new int[512];
	private int[] p = new int[512];
	
	private double autoY, autoZ;
	
	public void seed(RandomXS128 rng) {
		int i;
		for(i = 0; i < 256; i++) {
			permutation[i] = i;
		}
		for(i = 0; i < 256; i++) {
			int k = rng.nextInt(256 - i);
			int l = permutation[i];
			permutation[i] = permutation[k];
			permutation[k] = l;
			permutation[i + 256] = permutation[i];
		}
		for(i = 0; i < 256; i++) {
			p[256 + i] = (p[i] = permutation[i]);
		}
		autoY = rng.nextDouble(0.1, 0.9);
		autoZ = rng.nextDouble(0.1, 0.9);
	}
	
	public double noise(double x) {
		return noise(x, autoY, autoZ);
	}
	
	public double noise(double x, double y) {
		return noise(x, y, autoZ);
	}
	
	public double noise(double x, double y, double z) {
		int X = 255 & (int)Math.floor(x);
		int Y = 255 & (int)Math.floor(y);
		int Z = 255 & (int)Math.floor(z);

		x -= Math.floor(x);
		y -= Math.floor(y);
		z -= Math.floor(z);

		double u = fade(x);
		double v = fade(y);
		double w = fade(z);
		
		int A = this.p[X  ]+Y, AA = this.p[A]+Z, AB = this.p[A+1]+Z;
		int B = this.p[X+1]+Y, BA = this.p[B]+Z, BB = this.p[B+1]+Z;
		
		return (( 
				//and add blended results from 8 corners of cube
				this.lerp(w, this.lerp(v, this.lerp(u, this.grad(this.p[AA], x, y, z ), 
				this.grad(this.p[BA], x-1, y, z )),							
				this.lerp(u, this.grad(this.p[AB], x, y-1, z ),				
				this.grad(this.p[BB], x-1, y-1, z ))),
				this.lerp(v, this.lerp(u, this.grad(this.p[AA+1], x  , y  , z-1 ),
				this.grad(this.p[BA+1], x-1, y, z-1 )),
				this.lerp(u, this.grad(this.p[AB+1], x, y-1, z-1 ),
				this.grad(this.p[BB+1], x-1, y-1, z-1 ))))
			) + 0.747202767955651) * 0.6581541427273581;
	}
	
	private double fade(double t) {
		return t * t * t * (t * (t * 6 - 15) + 10);
	}
	
	private double lerp(double t, double a, double b) {
		return a + t * (b - a);
	}
	
	private double grad(int hash, double x, double y, double z) {
		int h = hash & 15;
		double u = h < 8 ? x : y;
		double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
		return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
	}
}