package fr.ailphaune.voxeltest.textures;

import java.util.HashMap;
import java.util.Objects;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class TextureAtlas implements Disposable {
	
	public class AtlasRegion {
		
		public final int x, y, w, h, clockwise90degRot;
		public final int mirrorX, mirrorY;

		public AtlasRegion(int x, int y, int w, int h, int clockwise90degRot, boolean mirrorX, boolean mirrorY) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.clockwise90degRot = clockwise90degRot;
			this.mirrorX = mirrorX ? 2 : 0;
			this.mirrorY = mirrorY ? 1 : 0;
		}
		
		private AtlasRegion(int x, int y, int w, int h, int clockwise90degRot, int mirrorX, int mirrorY) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.clockwise90degRot = clockwise90degRot;
			this.mirrorX = mirrorX;
			this.mirrorY = mirrorY;
		}

		public float getU() {
			return x / (float)pixmap.getWidth();
		}
		
		public float getV() {
			return y / (float)pixmap.getHeight();
		}
		
		public float getU2() {
			return (x+w) / (float)pixmap.getWidth();
		}
		
		public float getV2() {
			return (y+h) / (float)pixmap.getHeight();
		}
		
		public AtlasRegion original() {
			return new AtlasRegion(x, y, w, h, 0, 0, 0);
		}
		
		public AtlasRegion rotated(int clockwise90degRot) {
			return new AtlasRegion(x, y, w, h, (this.clockwise90degRot + clockwise90degRot) & 3, mirrorX, mirrorY);
		}
		
		public AtlasRegion mirrored(boolean mirrorX, boolean mirrorY) {
			return new AtlasRegion(x, y, w, h, this.clockwise90degRot, (this.mirrorX == 1) == mirrorX, (this.mirrorY == 1) == mirrorY);
		}
		
		public AtlasRegion rotateMirror(int clockwise90degRot, boolean mirrorX, boolean mirrorY) {
			return new AtlasRegion(x, y, w, h, (this.clockwise90degRot + clockwise90degRot) & 3, (this.mirrorX == 1) == mirrorX, (this.mirrorY == 1) == mirrorY);
		}

		private float _getU(int vertex) {
			return (vertex & 2) == mirrorX ? getU() : getU2();
		}

		public float _getV(int vertex) {
			return (vertex & 1) == mirrorY ? getV() : getV2();
		}
		
		public float getU(int vertex) {
			if((clockwise90degRot & 3) == 0) return _getU(~vertex << 1);
			if((clockwise90degRot & 3) == 1) return _getU(vertex);
			if((clockwise90degRot & 3) == 2) return _getU(vertex << 1);
			if((clockwise90degRot & 3) == 3) return _getU(vertex ^ 2);
			return 0;
		}
		
		public float getV(int vertex) {
			if((clockwise90degRot & 3) == 0) return _getV(vertex >> 1);
			if((clockwise90degRot & 3) == 1) return _getV(vertex);
			if((clockwise90degRot & 3) == 2) return _getV(~vertex >> 1);
			if((clockwise90degRot & 3) == 3) return _getV(vertex ^ 1);
			return 0;
		}
		
		public TextureAtlas getAtlas() {
			return TextureAtlas.this;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(x, y, w, h, clockwise90degRot, mirrorX, mirrorY);
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj == null) return false;
			if(obj == this) return true;
			if(obj.getClass() != getClass()) return false;
			AtlasRegion o = (AtlasRegion) obj;
			return x == o.x
					&& y == o.y
					&& w == o.w
					&& h == o.h
					&& mirrorX == o.mirrorX
					&& mirrorY == o.mirrorY
					&& clockwise90degRot == o.clockwise90degRot
					&& getAtlas() == o.getAtlas();
		}
		
		@Override
		public String toString() {
			return String.format("AtlasRegion{x=%d,y=%d,w=%d,h=%d}", x, y, w, h);
		}

		public AtlasRegion subRegion(float u, float v, float u2, float v2) {
			return new AtlasRegion((int) (this.x + (w * u)), (int) (this.y + (h * v)), (int) (w * (u2 - u)), (int) (h * (v2 - v)), clockwise90degRot, mirrorX, mirrorY);
		}
	}
	
	Pixmap pixmap;
	HashMap<String, AtlasRegion> regions;
	TextureManager manager;
	
	private boolean locked = false;
	
	public TextureAtlas(int width, int height) {
		pixmap = new Pixmap(width, height, Format.RGBA8888);
		regions = new HashMap<String, AtlasRegion>();
		manager = new TextureManager(this);
	}
	
	public TextureAtlas(int width, int height, int managerBaseWidth, int managerBaseHeight, int managerPadding) {
		pixmap = new Pixmap(width, height, Format.RGBA8888);
		regions = new HashMap<String, AtlasRegion>();
		manager = new TextureManager(this, managerBaseWidth, managerBaseHeight, managerPadding);
	}
	
	public TextureManager getManager() {
		return manager;
	}
	
	public void resizeTo(int width, int height) {
		if(locked) throw new IllegalStateException("Can't modify atlas when locked");
		if(width < pixmap.getWidth() || height < pixmap.getHeight()) throw new IllegalArgumentException("Can only resize to a greater than or equals size");
		Pixmap updated = new Pixmap(width, height, Format.RGB888);
		updated.drawPixmap(pixmap, 0, 0);
		pixmap.dispose();
		pixmap = updated;
	}
	
	public void addRegion(Pixmap source, String regionName, int x, int y) {
		if(locked) throw new IllegalStateException("Can't modify atlas when locked");
		pixmap.drawPixmap(source, x, y);
		regions.put(regionName, new AtlasRegion(x, y, source.getWidth(), source.getHeight(), 0, true, true));
	}
	
	public AtlasRegion getRegion(String regionName) {
		return regions.get(regionName);
	}
	
	public void lock() {
		this.locked = true;
	}
	
	private Texture texture;
	public Texture getTexture() {
		if(!locked) throw new IllegalStateException("Can't generate texture atlas when not locked");
		if(texture != null) return texture;
		return texture = new Texture(pixmap);
	}
	
	public Pixmap getPixmap() {
		return pixmap;
	}
	
	@Override
	public void dispose() {
		pixmap.dispose();
	}
}