package fr.albotw.Engine.textures;

import java.util.HashMap;

/**
 * Il est conseillé d'utiliser une énumération avec les noms des textures -> string pour éviter les typos
 * et ne pas oublier les handles.
 */
public class TextureAtlas {
    private HashMap<String, Texture> atlas;
    private HashMap<String, String> texturesToLoad;

    public TextureAtlas() {
        this.atlas = new HashMap<String, Texture>();
        this.texturesToLoad = new HashMap<String, String>();
    }

    public void load() {
        this.texturesToLoad.forEach((key, value) -> {
            Texture texture = new Texture(value);
            this.atlas.put(key, texture);
        });
    }

    public void bind(String id) {
        this.atlas.get(id).bind();
    }

    public void addTexture(String handle, String dir) {
        this.texturesToLoad.put(handle, dir);
    }
}
