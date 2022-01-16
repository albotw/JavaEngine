package fr.albotw.Engine;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.nuklear.NkMouse;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class Engine extends Thread {
    private fr.albot.GameOfLife.Engine.events.EventQueue eventQueue;

    private final fr.albot.GameOfLife.Engine.GL.Window window;
    private fr.albot.GameOfLife.Engine.GL.Shader shader;
    private final fr.albot.GameOfLife.Engine.GL.Camera camera;
    private SpriteManager spriteManager;
    private fr.albot.GameOfLife.Engine.textures.TextureAtlas textureAtlas;

    public Renderer() {
        this.eventQueue = new EventQueue(fr.albot.GameOfLife.Engine.events.ThreadID.Render);
        fr.albot.GameOfLife.Engine.UI.UI.createUI();
        this.window = new Window(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, VSYNC);
        this.textureAtlas = new fr.albot.GameOfLife.Engine.textures.TextureAtlas();
        this.spriteManager = SpriteManager.createSpriteManager();
        this.camera = new fr.albot.GameOfLife.Engine.GL.Camera(new Vector3f(0.0f, 0.0f, ENV_SIZE), ENV_SIZE);
    }

    public void init() throws Exception {
        this.window.init();
        this.textureAtlas.load();
        this.shader = new fr.albot.GameOfLife.Engine.GL.Shader();
        this.shader.createFragmentShader(fr.albot.GameOfLife.Engine.Util.loadResource("ressources/shaders/fragment.glsl"));
        this.shader.createVertexShader(fr.albot.GameOfLife.Engine.Util.loadResource("ressources/shaders/vertex.glsl"));
        this.shader.link();
    }


    public void run() {
        try {
            this.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean running = true;

        //translations[99] = new Vector3f(-1.0f, -1.0f, -1.0f);
        while (running && !window.windowShouldClose()) {
            // ! INPUT ---------------------------------------------------------

            nk_input_begin(fr.albot.GameOfLife.Engine.UI.UI.context);
            glfwPollEvents();
            NkMouse mouse = fr.albot.GameOfLife.Engine.UI.UI.context.input().mouse();
            if (mouse.grab()) {
                glfwSetInputMode(this.window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            } else if (mouse.grabbed()) {
                float prevX = mouse.prev().x();
                float prevY = mouse.prev().y();
                glfwSetCursorPos(this.window.getHandle(), prevX, prevY);
                mouse.pos().x(prevX);
                mouse.pos().y(prevY);
            } else if (mouse.ungrab()) {
                glfwSetInputMode(this.window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
            nk_input_end(fr.albot.GameOfLife.Engine.UI.UI.context);

            if (fr.albot.GameOfLife.Engine.input.Mouse.LMBPress && !fr.albot.GameOfLife.Engine.UI.UI.instance.isMouseOnUI()) {
                camera.rotate(fr.albot.GameOfLife.Engine.input.Mouse.Xoffset, fr.albot.GameOfLife.Engine.input.Mouse.Yoffset);
            }
            if (fr.albot.GameOfLife.Engine.input.Keyboard.UP_press || fr.albot.GameOfLife.Engine.input.Keyboard.Z_press) {
                camera.zoomIn();
            }
            if (fr.albot.GameOfLife.Engine.input.Keyboard.DOWN_press || fr.albot.GameOfLife.Engine.input.Keyboard.S_press) {
                camera.zoomOut();
            }

            // ! UPDATE --------------------------------------------------------
            if (!this.eventQueue.isEmpty()) {
                Event e = this.eventQueue.get();
                if (e instanceof fr.albot.GameOfLife.Engine.events.Events.PurgeEvent) {
                    this.spriteManager.purge();
                } else if (e instanceof fr.albot.GameOfLife.Engine.events.Events.SetBaseEnv) {
                    System.out.println("catched event");
                    this.spriteManager.init();
                    this.spriteManager.update(((fr.albot.GameOfLife.Engine.events.Events.SetBaseEnv) e).alive);
                }
            }

            // ! RENDER --------------------------------------------------------
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            this.shader.bind();
            try {
                this.shader.setUniform("proj", this.window.getProjectionMatrix());
                this.shader.setUniform("view", this.camera.getViewMatrix());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList<fr.albot.GameOfLife.Engine.geometry.Sprite> geometry = this.spriteManager.getGeometry();
            for (int i = 0; i < geometry.size(); i++) {
                fr.albot.GameOfLife.Engine.geometry.Sprite sprite = geometry.get(i);

                if (!sprite.hidden) {
                    Matrix4f model = sprite.getModelMatrix();
                    try {
                        this.shader.setUniform("model", model);
                        //this.shader.setUniform("color", sprite.mesh.getColor());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (sprite.wireframe) glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                    else glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

                    glBindVertexArray(sprite.mesh.getVaoID());
                    glEnableVertexAttribArray(0);

                    if (!sprite.solid) {
                        glEnableVertexAttribArray(1);
                        fr.albot.GameOfLife.Engine.textures.TextureID texture = ((fr.albot.GameOfLife.Engine.geometry.TexturedMesh) sprite.mesh).getTexture();
                        this.textureAtlas.bind(texture);
                    }

                    glDrawElements(GL_TRIANGLES, sprite.mesh.getVertexCount(), GL_UNSIGNED_INT, 0);
                    glBindVertexArray(0);
                }
            }
            shader.unbind();

            // ! DISPLAY -------------------------------------------------------
            fr.albot.GameOfLife.Engine.UI.UI.instance.render(NK_ANTI_ALIASING_ON, 512 * 1024, 128 * 1024);
            window.update();
        }
        System.out.println("done rendering");
    }
}