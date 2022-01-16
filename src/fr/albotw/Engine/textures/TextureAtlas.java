package fr.albotw.Engine.textures;

import java.util.HashMap;

public class TextureAtlas {
    private HashMap<TextureID, Texture> atlas;

    public TextureAtlas() {
        this.atlas = new HashMap<TextureID, Texture>();
    }

    public void load() {
        Texture cube = new Texture("ressources/texture.jpg");
        this.atlas.put(TextureID.CUBE, cube);

        //ajouter toutes les textures nécessaires au programme ici.
    }

    public void bind(TextureID id) {
        this.atlas.get(id).bind();
    }


}
