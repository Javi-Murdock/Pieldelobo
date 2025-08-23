package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainScreen extends PantallaInicio {
    private Skin skin;
    private Stage stage;
    private Protagonista jugador;
    private Texture fondo;
    private Procesador procesador;

    public MainScreen(Main game) {
        super(game);
    }

    @Override
    public void show() {
        // Usar FitViewport para mantener la relación de aspecto
        stage = new Stage(new FitViewport(800, 450)); // 16:9 ratio

        fondo = new Texture("fondo.jpg");
        jugador = new Protagonista();

        // Centrar al jugador en la pantalla
        jugador.setPosition(
            stage.getViewport().getWorldWidth() / 2 - jugador.getWidth() / 2,
            stage.getViewport().getWorldHeight() / 2 - jugador.getHeight() / 2
        );

        stage.addActor(jugador);
        procesador = new Procesador(jugador);
        Gdx.input.setInputProcessor(procesador);
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar viewport
        stage.getViewport().apply();

        // Dibujar fondo
        stage.getBatch().begin();
        stage.getBatch().draw(fondo, 0, 0,
            stage.getViewport().getWorldWidth(),
            stage.getViewport().getWorldHeight());
        stage.getBatch().end();

        // Actualizar movimiento
        procesador.actualizar(delta);

        // Dibujar escenario
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        super.dispose();
        fondo.dispose();
        jugador.dispose();
        stage.dispose();
    }
}
