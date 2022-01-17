package fr.albotw.Engine.input;

import fr.albotw.Engine.UI.UI;
import org.lwjgl.nuklear.NkVec2;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

import static fr.albotw.Engine.CONFIG.MOUSE_SENSITIVITY;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Mouse {
    public static double Xoffset;
    public static double Yoffset;

    public static double lastX = 400;
    public static double lastY = 300;

    public static boolean LMBPress;
    public static boolean RMBPress;

    public static void init(long window) {
        //glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(window, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        glfwSetCursorPosCallback(window, Mouse::processMousePosition);

        glfwSetMouseButtonCallback(window, Mouse::processMouseInput);

        glfwSetScrollCallback(window, Mouse::processScroll);
    }

    public static void processMousePosition(long window, double xpos, double ypos) {
        nk_input_motion(UI.context, (int) xpos, (int) ypos);
        Mouse.Xoffset = (Mouse.lastX - xpos) * MOUSE_SENSITIVITY;
        Mouse.Yoffset = (Mouse.lastY - ypos) * MOUSE_SENSITIVITY;

        Mouse.lastX = xpos;
        Mouse.lastY = ypos;
    }

    public static void processMouseInput(long window, int button, int action, int mods) {
        try (MemoryStack stack = stackPush()) {
            DoubleBuffer cx = stack.mallocDouble(1);
            DoubleBuffer cy = stack.mallocDouble(1);

            glfwGetCursorPos(window, cx, cy);

            int x = (int) cx.get(0);
            int y = (int) cy.get(0);

            int nkButton;
            switch (button) {
                case GLFW_MOUSE_BUTTON_RIGHT:
                    nkButton = NK_BUTTON_RIGHT;
                    Mouse.RMBPress = (action == GLFW_PRESS);
                    break;
                case GLFW_MOUSE_BUTTON_MIDDLE:
                    nkButton = NK_BUTTON_MIDDLE;
                    break;
                default:
                    nkButton = NK_BUTTON_LEFT;
                    Mouse.LMBPress = (action == GLFW_PRESS);
            }
            nk_input_button(UI.context, nkButton, x, y, action == GLFW_PRESS);
        }
    }

    public static void processScroll(long window, double xoffset, double yoffset) {
        try (MemoryStack stack = stackPush()) {
            NkVec2 scroll = NkVec2.malloc(stack)
                    .x((float) xoffset)
                    .y((float) yoffset);
            nk_input_scroll(UI.context, scroll);
        }
    }
}
