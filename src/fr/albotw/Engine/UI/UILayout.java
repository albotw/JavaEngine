package fr.albot.GameOfLife.Engine.UI;

import fr.albot.GameOfLife.App;
import fr.albot.GameOfLife.CONFIG.CONFIG;
import org.lwjgl.nuklear.NkRect;

import static org.lwjgl.nuklear.Nuklear.*;

public class UILayout {
    private NkRect panel = null;
    private int panelOptions = NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE;

    private String appStateLabel = "Start";

    private int[] envsize;
    private int[] alive;
    private int[] current;

    public UILayout() {
        envsize = new int[]{10};
        alive = new int[]{5};
        current = new int[]{4};
    }

    public void layout(int x, int y) {
        if (this.panel == null) {
            this.panel = NkRect.create();
            nk_rect(x, y, 300, 330, this.panel);
            nk_begin(UI.context, "Controls", this.panel, this.panelOptions);
            nk_end(UI.context);
        } else {
            if (nk_begin(UI.context, "Controls", this.panel, panelOptions)) {
                nk_layout_row_dynamic(UI.context, 30, 3);
                if (nk_button_label(UI.context, appStateLabel)) {
                    App.instance.loop = !App.instance.loop;
                    appStateLabel = "Pause";
                }
                if (nk_button_label(UI.context, "Reset")) {
                    App.instance.loop = false;
                    App.instance.purge();
                    App.instance.init();
                    appStateLabel = "Start";
                }
                if (nk_button_label(UI.context, "Step")) {
                    App.instance.loop = false;
                    App.instance.gol.step();
                }

                nk_layout_row_dynamic(UI.context, 30, 1);
                nk_label(UI.context, "step: " + App.instance.gol.getDuration() + " ms", NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);

                nk_layout_row_dynamic(UI.context, 30, 1);
                nk_label(UI.context, "alive: " + App.instance.gol.getAliveSize(), NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);

                nk_layout_row_dynamic(UI.context, 30, 1);
                nk_label(UI.context, "neighbours: " + App.instance.gol.getNeighboursSize(), NK_TEXT_ALIGN_LEFT | NK_TEXT_ALIGN_MIDDLE);

                //input ENV_SIZE
                nk_layout_row_dynamic(UI.context, 30, 1);
                int before_env = envsize[0];
                nk_property_int(UI.context, "ENV_SIZE", 3, envsize, 100, 1, 1);
                if (envsize[0] != before_env) {
                    CONFIG.setEnvSize(envsize[0]);
                }

                //input ALIVE_THRESHOLD
                nk_layout_row_dynamic(UI.context, 30, 1);
                int before_alive = alive[0];
                nk_property_int(UI.context, "ALIVE_THRESHOLD", 0, alive, 100, 1, 1);
                if (alive[0] != before_alive) {
                    CONFIG.setEnvSize(alive[0]);
                }

                //input CURRENT_THRESHOLD
                nk_layout_row_dynamic(UI.context, 30, 1);
                int before_current = current[0];
                nk_property_int(UI.context, "CURRENT_THRESHOLD", 0, current, 100, 1, 1);
                if (current[0] != before_current) {
                    CONFIG.setEnvSize(current[0]);
                }

                nk_layout_row_dynamic(UI.context, 30, 1);
                if (nk_button_label(UI.context, "EXIT")) {
                    App.instance.exit();
                }
            }
            nk_end(UI.context);
        }
    }
}
