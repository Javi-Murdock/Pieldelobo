package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Enemigo Golem - Enemigo tipo tanque con mucha vida y daño alto.
 * Animación de muerte mejor documentada.
 *  * @author Javier Gala
 *  * @version 1.0
 */
public class Golem extends Enemigos {

    /**
     * Constructor del Golem.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     */
    public Golem(float x, float y) {
        super(x, y);

        // ====================== CONFIGURACIÓN DEL GOLEM ======================
        this.vidaMaxima = 10;
        this.vidaActual = 10;
        this.velocidad = 30f;
        this.distanciaMaxima = 120f;
        this.danoAtaque = 3;
        this.rangoAtaque = 70f;
        this.duracionAtaque = 1.2f;
        this.cooldownAtaqueMaximo = 3.0f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 90f;
        this.offsetHitboxX = 15f;
        this.offsetHitboxY = 10f;
        this.anchoHitbox = 60f;
        this.altoHitbox = 44f;

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Golem", "Creado en posición: " + x + ", " + y);
    }

    /**
     * Carga las animaciones del golem desde el atlas.
     * NOTA: Los frames de muerte tienen prefijo diferente: "golem_derrotado"
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe
            if (!Gdx.files.internal("Golem.atlas").exists()) {
                Gdx.app.log("Golem", "Atlas 'Golem.atlas' no encontrado");
                return;
            }

            atlas = new TextureAtlas(Gdx.files.internal("Golem.atlas"));
            Gdx.app.log("Golem", "Atlas Golem cargado exitosamente");

            // ====================== ANIMACIÓN IDLE ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            for (int i = 0; i <= 3; i++) {
                String frameName = String.format("Golem_ataque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.3f, idleFrames);
                Gdx.app.log("Golem", "Animación Idle cargada: " + idleFrames.size + " frames");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 4; i <= 7; i++) {
                String frameName = String.format("Golem_ataque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.25f, walkFrames);
                Gdx.app.log("Golem", "Animación Caminar cargada: " + walkFrames.size + " frames");
            }

            // ====================== ANIMACIÓN ATACAR ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 0; i <= 10; i++) {
                String frameName = String.format("Golem_ataque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Golem", "Animación Atacar cargada: " + attackFrames.size + " frames");
            }

            // ====================== ANIMACIÓN MUERTE (DERROTADO) ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            // IMPORTANTE: Los frames de muerte tienen prefijo "golem_derrotado"
            for (int i = 0; i <= 11; i++) {
                String frameName = String.format("golem_derrotado%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    deathFrames.add(frame);
                    deathCargado = true;
                    Gdx.app.debug("Golem", "Frame muerte encontrado: " + frameName);
                }
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.15f, deathFrames);
                Gdx.app.log("Golem", "Animación Muerte cargada: " + deathFrames.size + " frames");
            } else {
                Gdx.app.log("Golem", "ADVERTENCIA: No se encontró animación de muerte específica");
            }

            // ====================== ANIMACIÓN DAÑO ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 8; i <= 10; i++) {
                String frameName = String.format("Golem_ataque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.1f, hurtFrames);
                Gdx.app.log("Golem", "Animación Daño cargada: " + hurtFrames.size + " frames");
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            }

            Gdx.app.log("Golem", "Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("Golem", "Error al cargar animaciones", e);
        }
    }

    /**
     * Comportamiento específico del golem al patrullar.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void patrullar(float delta) {
        // El golem patrulla muy lentamente
        float velocidadOriginal = velocidad;
        velocidad = 20f;
        super.patrullar(delta);
        velocidad = velocidadOriginal;
    }

    /**
     * Comportamiento específico al recibir daño.
     * El golem es resistente y reduce el daño recibido.
     */
    @Override
    public void recibirDano() {
        // El golem recibe menos daño
        vidaActual -= 1;
        recibiendoDano = true;
        tiempoDano = 0;

        Gdx.app.log("Golem", "¡Golem resiste el daño! Vida: " + vidaActual + "/" + vidaMaxima);

        if (vidaActual <= 0) {
            morir();
        }
    }

    /**
     * Comportamiento específico al morir.
     */
    @Override
    protected void morir() {
        super.morir();
        Gdx.app.log("Golem", "¡Golem destruido!");
    }
}
