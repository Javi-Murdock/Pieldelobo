package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Proyectil Bola de Fuego lanzado por el Brujo.
 *
 *  * @author Javier Gala
 *  * @version 1.1
 */
public class BolaFuego extends Proyectil {

    private static Animation<TextureRegion> animacionBolaFuego;
    private float tiempoAnimacion = 0;

    static {
        // Cargar animación estática
        cargarAnimacion();
    }

    private static void cargarAnimacion() {
        try {
            Texture tex = new Texture(Gdx.files.internal("bola_fuego.png"));
            // Suponiendo que la textura es un sprite sheet
            TextureRegion[][] frames = TextureRegion.split(tex, 32, 32);

            Array<TextureRegion> animFrames = new Array<>();
            for (int i = 0; i < 4; i++) { // 4 frames de animación
                animFrames.add(frames[0][i]);
            }

            animacionBolaFuego = new Animation<>(0.1f, animFrames);
            Gdx.app.log("BolaFuego", "Animación de bola de fuego cargada: " + animFrames.size + " frames");

        } catch (Exception e) {
            Gdx.app.error("BolaFuego", "No se pudo cargar bola_fuego.png, creando animación por defecto", e);
            animacionBolaFuego = crearAnimacionPorDefecto();
        }
    }

    public BolaFuego(float x, float y, float velocidadX, float velocidadY, float dano,
                     MapaManager mapaManager, Protagonista objetivo) {
        super(x, y, velocidadX, velocidadY, dano,
            animacionBolaFuego != null ? animacionBolaFuego.getKeyFrame(0) : null,
            mapaManager, objetivo);

        // Tamaño más grande para bola de fuego
        setSize(32, 32);

        // Velocidad más lenta pero con daño de área
        this.velocidad.scl(0.8f);

        Gdx.app.log("BolaFuego", "Bola de fuego creada");
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        tiempoAnimacion += delta;

        // Actualizar animación
        if (animacionBolaFuego != null) {
            textura = animacionBolaFuego.getKeyFrame(tiempoAnimacion, true);
        }

        // Efecto de parpadeo antes de desaparecer
        if (tiempoVida > duracionMaxima - 1f) {
            float alpha = (float)Math.sin(tiempoVida * 10) * 0.5f + 0.5f;
            setColor(1, 1, 1, alpha);
        }
    }


    public void draw(Batch batch, float parentAlpha) {
        if (!activo || textura == null) return;

        // Efecto de brillo para bola de fuego
        batch.setColor(getColor());
        batch.draw(textura,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation());
        batch.setColor(1, 1, 1, 1);
    }

    private static Animation<TextureRegion> crearAnimacionPorDefecto() {
        // Crear animación por defecto (círculo que cambia de tamaño)
        Array<TextureRegion> frames = new Array<>();

        for (int i = 0; i < 4; i++) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32,
                com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

            // Color que cambia del amarillo al rojo
            float t = i / 3f;
            float r = 1.0f;
            float g = 1.0f - t * 0.5f;
            float b = 0.2f;

            pixmap.setColor(r, g, b, 1);
            int radio = 12 + i * 2; // Crece un poco cada frame
            pixmap.fillCircle(16, 16, radio);

            // Borde brillante
            pixmap.setColor(1, 1, 0.8f, 1);
            pixmap.drawCircle(16, 16, radio + 1);

            Texture tex = new Texture(pixmap);
            frames.add(new TextureRegion(tex));
            pixmap.dispose();
        }

        return new Animation<>(0.1f, frames);
    }
}
