package io.github.javiergames.pieldelobo.Videos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

import io.github.javiergames.pieldelobo.GestorJuego.Main;
import io.github.javiergames.pieldelobo.LobbyScreen;
import io.github.javiergames.pieldelobo.VideoScreen;

/**
 * Utilidades para manejo de videos en el juego.
 * Proporciona métodos para reproducir videos y gestionar transiciones entre pantallas.
 *
 * @author JavierGames
 * @version 1.0
 */
public class VideoUtils {

    /**
     * Reproduce un video y luego vuelve a una pantalla de destino específica.
     *
     * @param game Instancia principal del juego ({@link Main})
     * @param videoPath Ruta del archivo de video a reproducir
     * @param pantallaDestino Pantalla a la que volver después de reproducir el video
     * @throws IllegalArgumentException Si la ruta del video es nula o vacía
     */
    public static void reproducirVideoYVolver(Main game, String videoPath, Screen pantallaDestino) {
        if (videoPath == null || videoPath.isEmpty()) {
            Gdx.app.error("VideoUtils", "Ruta de video inválida");
            return;
        }

        try {
            VideoScreen videoScreen = new VideoScreen(game, videoPath, pantallaDestino);
            game.setScreen(videoScreen);
            Gdx.app.log("VideoUtils", "Reproduciendo video: " + videoPath);
        } catch (Exception e) {
            Gdx.app.error("VideoUtils", "Error creando VideoScreen", e);
        }
    }

    /**
     * Reproduce un video y vuelve al lobby por defecto
     */
    public static void reproducirVideoYVolverALobby(Main game, String videoPath) {
        reproducirVideoYVolver(game, videoPath, new LobbyScreen(game));
    }
}
