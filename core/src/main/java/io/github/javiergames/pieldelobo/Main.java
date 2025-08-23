package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Clase principal del juego que extiende de Game (patrón pantallas de LibGDX).
 * Controla el flujo principal del juego y la gestión de pantallas.
 */
public class Main extends Game {

    /**
     * Método llamado cuando se crea la aplicación.
     * Configura la pantalla inicial del juego.
     */
    @Override
    public void create() {
        // Establece la pantalla principal como la primera pantalla del juego
        setScreen(new MainScreen(this));
    }

}
