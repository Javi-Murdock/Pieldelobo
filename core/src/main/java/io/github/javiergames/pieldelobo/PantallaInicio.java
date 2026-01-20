package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Screen;

import io.github.javiergames.pieldelobo.GestorJuego.Main;
/**
 * Clase base para todas las pantallas del juego.
 * Implementa los métodos vacíos de Screen para simplificar herencia.
 * Todas las pantallas del juego deben extender esta clase.
 *
 * @author Javier Gala
 * @version 1.0
 */
public abstract class PantallaInicio implements Screen {

    protected final Main game;

    public PantallaInicio(Main game) {
        this.game = game;
    }

    @Override public void show() {}
    @Override public void render(float delta) {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {}
}
