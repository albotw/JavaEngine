package fr.albotw.Engine.geometry;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    protected final int vaoID;
    protected final int pos_vboID;
    protected final int idx_vboID;

    protected final int vertexCount;

    private Vector3f color;

    public Mesh(float[] positions, Vector3f color, int[] indices) {
        this.color = color;
        FloatBuffer posBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
            this.vertexCount = indices.length;
            this.vaoID = glGenVertexArrays();
            glBindVertexArray(this.vaoID);

            // Position VBO
            this.pos_vboID = glGenBuffers();
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, this.pos_vboID);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Index VBO
            this.idx_vboID = glGenBuffers();
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.idx_vboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public Vector3f getColor() {
        return this.color;
    }

    public void purge() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(pos_vboID);
        glDeleteBuffers(idx_vboID);

        glBindVertexArray(0);
        glDeleteVertexArrays(vaoID);
        this.color = null;
    }
}
