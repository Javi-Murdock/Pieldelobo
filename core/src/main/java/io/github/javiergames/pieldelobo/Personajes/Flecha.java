package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Proyectil Flecha lanzado por el Arquero.
 *
 *  * @author Javier Gala
 *  * @version 1.0
 */
public class Flecha extends Proyectil {

    private static TextureRegion texturaFlecha;

    static {
        // Cargar textura estática para todas las flechas

            Gdx.app.error("Flecha", "No se pudo cargar flecha.png, usando textura por defecto");
            // Crear textura por defecto
            texturaFlecha = crearTexturaPorDefecto(1, 0, 0); // Flecha amarilla
    }

    public Flecha(float x, float y, float velocidadX, float velocidadY, float dano,
                  MapaManager mapaManager, Protagonista objetivo) {
        super(x, y, velocidadX, velocidadY, dano, texturaFlecha, mapaManager, objetivo);

        // Velocidad más rápida para flechas
        this.velocidad.scl(1.5f);

        Gdx.app.log("Flecha", "Flecha creada con velocidad: " + velocidad);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Efecto visual: rotación según la velocidad
        if (velocidad.x != 0 || velocidad.y != 0) {
            float angulo = (float)Math.toDegrees(Math.atan2(velocidad.y, velocidad.x));
            setRotation(angulo);
        }
    }

    private static TextureRegion crearTexturaPorDefecto(float r, float g, float b) {
        // Crear una textura simple (triángulo para flecha)
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 8,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, 1);
        pixmap.fillTriangle(0, 4, 24, 0, 24, 8); // Punta de flecha
        pixmap.setColor(r * 0.7f, g * 0.7f, b * 0.7f, 1);
        pixmap.fillRectangle(24, 2, 8, 4); // Cuerpo de flecha

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(tex);
    }
}
