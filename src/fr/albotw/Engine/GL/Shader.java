package fr.albotw.Engine.GL;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private final int programID;
    private int vertexShaderID;
    private int fragmentShaderID;

    public Shader() throws Exception {
        programID = glCreateProgram();

        if (programID == 0) {
            throw new Exception("impossible de créer le shader");
        }
    }

    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderID = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderID = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    public void setUniform(String uniformName, Matrix4f value) throws Exception {
        int uniformPos = glGetUniformLocation(this.programID, uniformName);
        if (uniformPos < 0) {
            throw new Exception("Uniform " + uniformName + " introuvable");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(uniformPos, false, value.get(stack.mallocFloat(16)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUniform(String uniformName, Vector3f value) throws Exception {
        int uniformPos = glGetUniformLocation(this.programID, uniformName);
        if (uniformPos < 0) {
            throw new Exception("Uniform " + uniformName + " introuvable: (" + uniformPos + ")");
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniform3fv(uniformPos, value.get(stack.mallocFloat(3)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUniform(String uniformName, int value) throws Exception {
        int uniformPos = glGetUniformLocation(this.programID, uniformName);
        if (uniformPos < 0) {
            throw new Exception("Uniform " + uniformName + " introuvable: (" + uniformPos + ")");
        }
        glUniform1i(uniformPos, value);

    }

    public int getProgramID() {
        return this.programID;
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) {
            throw new Exception("Erreur lors de la création du shader. Type: " + shaderType);
        }

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Erreur lors de la compilation du shader: " + glGetShaderInfoLog(shaderID, 1024));
        }

        glAttachShader(programID, shaderID);

        return shaderID;
    }

    public void link() throws Exception {
        glLinkProgram(programID);

        if (glGetProgrami(programID, GL_LINK_STATUS) != GL_TRUE) {
            throw new Exception("Erreur lors de la liaison du shader: " + glGetProgramInfoLog(programID, 1024));
        }

        if (vertexShaderID != 0) {
            glDetachShader(programID, vertexShaderID);
        }

        if (fragmentShaderID != 0) {
            glDetachShader(programID, fragmentShaderID);
        }

        glValidateProgram(programID);

        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning lors de la validation du shader: " + glGetProgramInfoLog(programID, 1024));
        }
    }

    public void bind() {
        glUseProgram(programID);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void flush() {
        unbind();
        if (programID != 0) {
            glDeleteProgram(programID);
        }
    }
}
