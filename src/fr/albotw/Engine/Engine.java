package fr.albotw.Engine;

import fr.albotw.Engine.GL.Window;
import fr.albotw.Engine.UI.UI;
import fr.albotw.Engine.events.EventDispatcher;
import fr.albotw.Engine.textures.TextureAtlas;
import org.lwjgl.nuklear.NkMouse;

import javax.swing.plaf.TextUI;

import static fr.albotw.Engine.CONFIG.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_NORMAL;
import static org.lwjgl.nuklear.Nuklear.nk_input_begin;
import static org.lwjgl.nuklear.Nuklear.nk_input_end;

public class Engine implements Runnable{
    private final Thread appThread;
    private Window window;
    private TextureAtlas textureAtlas;
    private SpriteManager spriteManager;
    private IApp app;

    private boolean run = true;

    public Engine(IApp app) {
        appThread = new Thread(this, "APP_THREAD");
        window = new Window(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_TITLE, VSYNC);
        textureAtlas = new TextureAtlas();
        spriteManager = new SpriteManager();
        this.app = app;
    }

    public void start() {
        appThread.start();
    }

    public void stop() {
        this.run = false;
    }

    public void run() {
        try {
            init();
            loop();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init() throws Exception{
        EventDispatcher.createEventDispatcher();
        window.init();
        textureAtlas.load();
        app.init();
    }

    public void loop() {
        while (run && !window.windowShouldClose()){

            nk_input_begin(UI.context);
            glfwPollEvents();
            NkMouse mouse = UI.context.input().mouse();
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
            nk_input_end(UI.context);

            app.input();
            app.update();
            app.render();
            window.update();
        }
    }
}
