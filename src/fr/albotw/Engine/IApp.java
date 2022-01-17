package fr.albotw.Engine;

public interface IApp {
    void init() throws Exception;

    void input();

    void update();

    void render();

    void purge();
}
