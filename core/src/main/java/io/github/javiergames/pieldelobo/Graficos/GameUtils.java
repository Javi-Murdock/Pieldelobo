package io.github.javiergames.pieldelobo.Graficos;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Utilidades gráficas básicas para el juego.
 * Proporciona recursos gráficos reutilizables.
 *
 * @author Javier Gala
 * @version 1.0
 */
public class GameUtils {
    /**
     * Obtiene un píxel blanco de 1x1 como textura.
     * Útil para dibujar formas básicas y fondos.
     *
     * @return Textura de un píxel blanco
     */
    private static Texture whitePixel;

    public static Texture getWhitePixel() {
        if (whitePixel == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 1, 1, 1);
            pixmap.fill();
            whitePixel = new Texture(pixmap);
            pixmap.dispose();
        }
        return whitePixel;
    }

    public static void dispose() {
        if (whitePixel != null) {
            whitePixel.dispose();
            whitePixel = null;
        }
    }
}
