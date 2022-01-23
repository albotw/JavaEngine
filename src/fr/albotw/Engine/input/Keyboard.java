package fr.albotw.Engine.input;

import fr.albotw.Engine.UI.UI;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;

public class Keyboard {
    public static boolean UP_press;
    public static boolean DOWN_press;
    public static boolean LEFT_press;
    public static boolean RIGHT_press;
    public static boolean Z_press;
    public static boolean S_press;
    public static boolean Q_press;
    public static boolean D_press;
    public static boolean SPACE_press;
    public static boolean SHIFT_press;

    public static void init(long window) {
        glfwSetKeyCallback(window, Keyboard::processKeyboardEvents);
        glfwSetCharCallback(window, Keyboard::processChar);
    }

    public static void processChar(long window, int codepoint) {
        nk_input_unicode(UI.context, codepoint);
    }

    public static void processKeyboardEvents(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(window, true);
        }
        boolean press = action == GLFW_PRESS;
        switch (key) {
            case GLFW_KEY_Z:
                Z_press = press;
                break;
            case GLFW_KEY_S:
                S_press = press;
                break;
            case GLFW_KEY_Q:
                Q_press = press;
                break;
            case GLFW_KEY_D:
                D_press = press;
                break;
            case GLFW_KEY_SPACE:
                SPACE_press = press;
                break;
            case GLFW_KEY_DELETE:
                nk_input_key(UI.context, NK_KEY_DEL, press);
                break;
            case GLFW_KEY_ENTER:
                nk_input_key(UI.context, NK_KEY_ENTER, press);
                break;
            case GLFW_KEY_TAB:
                nk_input_key(UI.context, NK_KEY_TAB, press);
                break;
            case GLFW_KEY_BACKSPACE:
                nk_input_key(UI.context, NK_KEY_BACKSPACE, press);
                break;
            case GLFW_KEY_UP:
                nk_input_key(UI.context, NK_KEY_UP, press);
                UP_press = press;
                break;
            case GLFW_KEY_DOWN:
                nk_input_key(UI.context, NK_KEY_DOWN, press);
                DOWN_press = press;
                break;
            case GLFW_KEY_LEFT:
                LEFT_press = press;
                break;
            case GLFW_KEY_RIGHT:
                RIGHT_press = press;
                break;
            case GLFW_KEY_HOME:
                nk_input_key(UI.context, NK_KEY_TEXT_START, press);
                nk_input_key(UI.context, NK_KEY_SCROLL_START, press);
                break;
            case GLFW_KEY_END:
                nk_input_key(UI.context, NK_KEY_TEXT_END, press);
                nk_input_key(UI.context, NK_KEY_SCROLL_END, press);
                break;
            case GLFW_KEY_PAGE_DOWN:
                nk_input_key(UI.context, NK_KEY_SCROLL_DOWN, press);
                break;
            case GLFW_KEY_PAGE_UP:
                nk_input_key(UI.context, NK_KEY_SCROLL_UP, press);
                break;
            case GLFW_KEY_LEFT_SHIFT:
            case GLFW_KEY_RIGHT_SHIFT:
                nk_input_key(UI.context, NK_KEY_SHIFT, press);
                SHIFT_press = press;
                break;
            case GLFW_KEY_LEFT_CONTROL:
            case GLFW_KEY_RIGHT_CONTROL:
                if (press) {
                    nk_input_key(UI.context, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
                } else {
                    nk_input_key(UI.context, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
                    nk_input_key(UI.context, NK_KEY_COPY, false);
                    nk_input_key(UI.context, NK_KEY_PASTE, false);
                    nk_input_key(UI.context, NK_KEY_CUT, false);
                    nk_input_key(UI.context, NK_KEY_SHIFT, false);
                }
                break;
        }
    }
}
