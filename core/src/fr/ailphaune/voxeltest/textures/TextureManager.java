package fr.ailphaune.voxeltest.textures;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.badlogic.gdx.graphics.Pixmap;

public final class TextureManager {

	private TextureAtlas atlas;
	private int baseWidth, baseHeight, texturePadding;
	private HashMap<String, Pixmap> pixmaps;
	
	private boolean generated = false;
	
	public TextureManager(TextureAtlas atlas) {
		this(atlas, 128, 128, 0);
	}
	
	public TextureManager(TextureAtlas atlas, int baseWidth, int baseHeight, int texturePadding) {
		this.atlas = atlas;
		this.baseWidth = baseWidth;
		this.baseHeight = baseHeight;
		this.texturePadding = texturePadding;
		this.pixmaps = new HashMap<>();
	}
	
	public void generateAtlas() {
		if(generated) throw new IllegalStateException("Atlas already generated");
		List<Texture> textures = run(pixmaps, baseWidth, baseHeight, texturePadding);
		int atlasWidth = baseWidth * textures.size();
		int atlasHeight = baseHeight;
		atlas.resizeTo(atlasWidth, atlasHeight);
		for(int i = 0; i < textures.size(); i++) {
			Texture texture = textures.get(i);
			for(Entry<String, Rectangle> entry : texture.rectangleMap.entrySet()) {
				Rectangle rect = entry.getValue();
				atlas.addRegion(pixmaps.get(entry.getKey()), entry.getKey(), baseWidth*i + rect.x, rect.y);
			}
		}
		pixmaps.clear();
		atlas.manager = null;
		atlas.lock();
		atlas = null;
		generated = true;
	}
	
	public boolean addRegion(Pixmap pixmap, String name) {
		if(pixmaps.containsKey(name)) return false;
		pixmaps.put(name, pixmap);
		return true;
	}
	
	private List<Texture> run(Map<String, Pixmap> pixmaps, int width, int height, int padding) {
		Set<ImageInfo> imageInfoSet = new TreeSet<ImageInfo>(new ImageInfoComparator());

		for(Entry<String, Pixmap> entry : pixmaps.entrySet()) {
			imageInfoSet.add(new ImageInfo(entry.getValue(), entry.getKey()));
		}

		List<Texture> textures = new ArrayList<Texture>();

		textures.add(new Texture(width, height));

		for (ImageInfo imageInfo : imageInfoSet) {
			boolean added = false;

			for (Texture texture : textures) {
				if (texture.addImage(imageInfo.image, imageInfo.name, padding)) {
					added = true;
					break;
				}
			}

			if (!added) {
				Texture texture = new Texture(width, height);
				texture.addImage(imageInfo.image, imageInfo.name, padding);
				textures.add(texture);
			}
		}
		
		return textures;
	}

	private class ImageInfo {
		public Pixmap image;
		public String name;

		public ImageInfo(Pixmap image, String name) {
			this.image = image;
			this.name = name;
		}
	}

	private class ImageInfoComparator implements Comparator<ImageInfo> {
		public int compare(ImageInfo image1, ImageInfo image2) {
			int area1 = image1.image.getWidth() * image1.image.getHeight();
			int area2 = image2.image.getWidth() * image2.image.getHeight();

			if (area1 != area2) {
				return area2 - area1;
			} else {
				return image1.name.compareTo(image2.name);
			}
		}
	}
	
	public class Texture {
		private class Node {
			public Rectangle rect;
			public Node child[];
			public boolean occupied = false;

			public Node(int x, int y, int width, int height) {
				rect = new Rectangle(x, y, width, height);
				child = new Node[2];
				child[0] = null;
				child[1] = null;
			}

			public boolean isLeaf() {
				return child[0] == null && child[1] == null;
			}

			// Algorithm from http://www.blackpawn.com/texts/lightmaps/
			public Node insert(Pixmap image, int padding) {
				if (!isLeaf()) {
					Node newNode = child[0].insert(image, padding);

					if (newNode != null) {
						return newNode;
					}

					return child[1].insert(image, padding);
				} else {
					if (this.occupied) {
						return null; // occupied
					}

					if (image.getWidth() > rect.width || image.getHeight() > rect.height) {
						return null; // does not fit
					}

					if (image.getWidth() == rect.width && image.getHeight() == rect.height) {
						this.occupied = true; // perfect fit
						return this;
					}

					int dw = rect.width - image.getWidth();
					int dh = rect.height - image.getHeight();

					if (dw > dh) {
						child[0] = new Node(rect.x, rect.y, image.getWidth(), rect.height);
						child[1] = new Node(padding + rect.x + image.getWidth(), rect.y,
								rect.width - image.getWidth() - padding, rect.height);
					} else {
						child[0] = new Node(rect.x, rect.y, rect.width, image.getHeight());
						child[1] = new Node(rect.x, padding + rect.y + image.getHeight(), rect.width,
								rect.height - image.getHeight() - padding);
					}

					return child[0].insert(image, padding);
				}
			}
		}

		private Node root;
		private Map<String, Rectangle> rectangleMap;

		public Texture(int width, int height) {
			root = new Node(0, 0, width, height);
			rectangleMap = new TreeMap<String, Rectangle>();
		}

		public boolean addImage(Pixmap image, String name, int padding) {
			Node node = root.insert(image, padding);

			if (node == null) {
				return false;
			}

			rectangleMap.put(name, node.rect);

			return true;
		}
	}
}