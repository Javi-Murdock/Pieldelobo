package io.github.javiergames.pieldelobo.Puertas;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Stage personalizado que ordena actores por posición Y para vista cenital.
 * Los actores con mayor posición Y se dibujan primero, creando un efecto de profundidad.
 *
 * @author Javier Gala
 * @version 1.0
 */
public class StageOrdenado extends Stage {
    /**
     * Constructor que crea un Stage ordenado con el viewport especificado.
     *
     * @param viewport Viewport a usar para este Stage
     */
    public StageOrdenado(Viewport viewport) {
        super(viewport);
    }
    /**
     * Dibuja todos los actores en el Stage, ordenados por su posición Y descendente.
     * Esto crea un efecto de profundidad adecuado para juegos con vista cenital.
     */
    @Override
    public void draw() {
        // Ordenar actores antes de dibujar
        getActors().sort((actor1, actor2) -> {
            float y1 = actor1.getY() + actor1.getHeight();
            float y2 = actor2.getY() + actor2.getHeight();
            return Float.compare(y2, y1); // Orden descendente
        });

        super.draw();
    }
}
