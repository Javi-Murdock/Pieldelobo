package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Proyectil Oscuro simplificado para el Necromancer.
 *
 *  * @author Javier Gala
 *  * @version 10.
 */
public class ProyectilOscuro extends Proyectil {

    public ProyectilOscuro(float x, float y, float velocidadX, float velocidadY, float dano,
                           MapaManager mapaManager, Protagonista objetivo) {
        super(x, y, velocidadX, velocidadY, dano, crearTexturaPorDefecto(), mapaManager, objetivo);

        // Tamaño
        setSize(24, 24);

        // Establecer origen en el centro para rotación
        setOrigin(getWidth() / 2, getHeight() / 2);

        Gdx.app.log("ProyectilOscuro", "¡PROYECTIL OSCURO CREADO! Pos: (" + x + "," + y + ")");
    }

    private static TextureRegion crearTexturaPorDefecto() {
        Pixmap pixmap = new Pixmap(24, 24, Pixmap.Format.RGBA8888);

        // Color púrpura oscuro
        pixmap.setColor(0.5f, 0.1f, 0.8f, 1);
        pixmap.fillCircle(12, 12, 10);

        // Borde brillante
        pixmap.setColor(0.7f, 0.3f, 1.0f, 1);
        pixmap.drawCircle(12, 12, 11);

        // Ojos
        pixmap.setColor(0.1f, 0.1f, 0.1f, 1);
        pixmap.fillCircle(8, 12, 2);
        pixmap.fillCircle(16, 12, 2);

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(tex);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Efecto visual: rotación
        if (velocidad.x != 0 || velocidad.y != 0) {
            float angulo = (float)Math.toDegrees(Math.atan2(velocidad.y, velocidad.x));
            setRotation(angulo);
        }
    }
}
