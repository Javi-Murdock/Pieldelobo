package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Clase base para todos los proyectiles (flechas, bolas de fuego, etc.)
 *
 *  * @author Javier Gala
 *  * @version 1.0
 */
public class Proyectil extends Actor {
    protected TextureRegion textura;
    protected Vector2 velocidad;
    protected float dano;
    protected boolean activo = true;
    protected MapaManager mapaManager;
    protected Protagonista objetivo;
    protected boolean esAmigable = false; // True si es del jugador, False si es de enemigos

    // Para efectos visuales
    protected float tiempoVida = 0;
    protected float duracionMaxima = 5f; // Desaparece después de 5 segundos

    public Proyectil(float x, float y, float velocidadX, float velocidadY, float dano,
                     TextureRegion textura, MapaManager mapaManager, Protagonista objetivo) {
        this.textura = textura;
        this.velocidad = new Vector2(velocidadX, velocidadY);
        this.dano = dano;
        this.mapaManager = mapaManager;
        this.objetivo = objetivo;

        setPosition(x, y);

        // Establecer tamaño basado en la textura
        if (textura != null) {
            setSize(textura.getRegionWidth(), textura.getRegionHeight());
        } else {
            setSize(16, 16); // Tamaño por defecto
        }

        Gdx.app.log("Proyectil", "Creado en: " + x + ", " + y +
            " Velocidad: " + velocidadX + ", " + velocidadY);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!activo) return;

        tiempoVida += delta;

        // Desaparecer si ha pasado mucho tiempo
        if (tiempoVida >= duracionMaxima) {
            eliminar();
            return;
        }

        // Movimiento
        float xAnterior = getX();
        float yAnterior = getY();

        setX(getX() + velocidad.x * delta);
        setY(getY() + velocidad.y * delta);

        // Verificar colisiones con el mapa
        if (mapaManager != null && mapaManager.hayColision(getHitbox())) {
            eliminar();
            Gdx.app.log("Proyectil", "Impactó contra el mapa");
            return;
        }

        // Verificar colisión con el objetivo
        if (objetivo != null && objetivo.estaVivo() && !objetivo.estaInvencible()) {
            if (getHitbox().overlaps(objetivo.getHitbox())) {
                objetivo.recibirDano();
                eliminar();
                Gdx.app.log("Proyectil", "¡Impacto al jugador! Daño: " + dano);
                return;
            }
        }

        // Rotación según dirección (opcional, para efecto visual)
        if (velocidad.x != 0) {
            setRotation((float)Math.toDegrees(Math.atan2(velocidad.y, velocidad.x)));
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!activo || textura == null) return;

        // Dibujar el proyectil
        batch.draw(textura,
            getX(), getY(),
            getOriginX(), getOriginY(),
            getWidth(), getHeight(),
            getScaleX(), getScaleY(),
            getRotation());
    }

    public Rectangle getHitbox() {
        // Hitbox más pequeño que el sprite para equilibrio
        float margen = 4f;
        return new Rectangle(
            getX() + margen,
            getY() + margen,
            getWidth() - margen * 2,
            getHeight() - margen * 2
        );
    }

    public void eliminar() {
        activo = false;
        remove(); // Remover del stage
    }

    public boolean estaActivo() {
        return activo;
    }

    public void setEsAmigable(boolean amigable) {
        this.esAmigable = amigable;
    }
}
