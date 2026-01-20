package io.github.javiergames.pieldelobo.Videos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Utilidades para manejar diferentes tipos de viewports según la pantalla.
 * Proporciona métodos para crear y gestionar viewports adaptados a diferentes
 * estrategias de escalado de pantalla.
 *
 * @author JavierGames
 * @version 1.0
 */
public class ViewportManager {
    /**
     * Tipos de viewport disponibles para diferentes estrategias de escalado.
     */
    public enum ViewportType {
        /**
         * Mantiene el aspect ratio, extiende el mundo en una dirección
         * para llenar la pantalla sin recortar.
         */
        EXTEND,

        /**
         * Mantiene el aspect ratio exacto, mostrando letterbox o pillarbox
         * cuando sea necesario.
         */
        FIT,

        /**
         * Estira el contenido para llenar toda la pantalla, ignorando el aspect ratio.
         */
        STRETCH,

        /**
         * Usa coordenadas de píxeles reales de la pantalla.
         */
        SCREEN
    }

    /**
     * Ancho virtual base del juego en unidades del mundo.
     * Este es el tamaño de referencia para el diseño del juego.
     */
    public static final float VIRTUAL_WIDTH = 800f;
    /**
     * Alto virtual base del juego en unidades del mundo.
     * Este es el tamaño de referencia para el diseño del juego.
     */

    public static final float VIRTUAL_HEIGHT = 600f;

    /**
     * Crea un viewport según el tipo especificado.
     *
     * @param type El tipo de viewport a crear, según {@link ViewportType}
     * @param camera La cámara OrthographicCamera que usará el viewport
     * @return Un Viewport configurado según el tipo especificado
     */
    public static Viewport createViewport(ViewportType type, OrthographicCamera camera) {
        switch (type) {
            case EXTEND:
                return new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
            case FIT:
                return new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
            case STRETCH:
                return new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
            case SCREEN:
                return new ScreenViewport(camera);
            default:
                return new ExtendViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        }
    }

    /**
     * Obtiene el aspect ratio actual de la pantalla
     */
    public static float getScreenAspectRatio() {
        return (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
    }

    /**
     * Obtiene el aspect ratio virtual del juego
     */
    public static float getVirtualAspectRatio() {
        return VIRTUAL_WIDTH / VIRTUAL_HEIGHT;
    }

    /**
     * Verifica si la pantalla es más ancha o más alta que el aspect ratio virtual
     */
    public static boolean isScreenWiderThanVirtual() {
        return getScreenAspectRatio() > getVirtualAspectRatio();
    }

    /**
     * Log de información del viewport para debug
     */
    public static void logViewportInfo(Viewport viewport, String screenName) {
        Gdx.app.log("ViewportManager",
            screenName + " - " +
                "Virtual: " + VIRTUAL_WIDTH + "x" + VIRTUAL_HEIGHT + " (" + getVirtualAspectRatio() + ")" +
                " | Screen: " + Gdx.graphics.getWidth() + "x" + Gdx.graphics.getHeight() + " (" + getScreenAspectRatio() + ")" +
                " | World: " + viewport.getWorldWidth() + "x" + viewport.getWorldHeight() +
                " | ScreenViewport: " + viewport.getScreenWidth() + "x" + viewport.getScreenHeight());
    }
}
