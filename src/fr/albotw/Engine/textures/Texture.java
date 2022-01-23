package fr.albotw.Engine.textures;

import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static fr.albotw.Engine.Util.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {
    private final int handle;
    private final int width;
    private final int height;

    public Texture(String path) {
        ByteBuffer image = null;
        ByteBuffer imageBuffer = null;

        try {
            imageBuffer = ioResourceToByteBuffer(path, 8 * 1024);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);


            if (!stbi_info_from_memory(imageBuffer, w, h, comp)) {
                throw new RuntimeException("Failed to read image information: " + stbi_failure_reason());
            } else {
                System.out.println("OK with reason: " + stbi_failure_reason());
            }

            image = stbi_load_from_memory(imageBuffer, w, h, comp, 3);
            if (image == null) {
                throw new RuntimeException("Impossible de charger la texture " + stbi_failure_reason());
            }

            this.width = w.get(0);
            this.height = h.get(0);
            System.out.println("width: " + this.width + " height: " + this.height + " comp: " + comp.get(0));

            this.handle = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.handle);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
            glGenerateMipmap(GL_TEXTURE_2D);

            // insérer un glBindTexture ??
            //stbi_image_free(image);
        }
    }

    public void purge() {
        glDeleteTextures(this.handle);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.handle);
    }

    public int getHandle() {
        return this.handle;
    }
}
