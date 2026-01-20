package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Enemigo Esqueleto - Enemigo básico con movimientos lentos pero persistentes.
 *  Nombres de frames y manejo de animaciones de muerte.
 *
 *  * @author Javier Gala
 *  * @version 1.1
 */
public class Esqueleto extends Enemigos {

    /**
     * Constructor del Esqueleto.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     */
    public Esqueleto(float x, float y) {
        super(x, y);

        // ====================== CONFIGURACIÓN DEL ESQUELETO ======================
        this.vidaMaxima = 3;
        this.vidaActual = 3;
        this.velocidad = 60f;
        this.distanciaMaxima = 150f;
        this.danoAtaque = 1;
        this.rangoAtaque = 50f;
        this.duracionAtaque = 0.5f;
        this.cooldownAtaqueMaximo = 1.5f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 40f;
        this.offsetHitboxX = 8f;
        this.offsetHitboxY = 4f;
        this.anchoHitbox = 24f;
        this.altoHitbox = 32f;

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Esqueleto", "Creado en posición: " + x + ", " + y +
            " - Atlas disponible: " + (atlas != null));
    }

    /**
     * Carga las animaciones específicas del esqueleto desde el atlas.
     * CORREGIDO: Verificación de frames y nombres correctos.
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe antes de intentar cargarlo
            if (!Gdx.files.internal("Esqueleto.atlas").exists()) {
                Gdx.app.log("Esqueleto", "Atlas 'Esqueleto.atlas' no encontrado en assets/");
                return; // Usará las animaciones por defecto de la clase base
            }

            // Intentar cargar el atlas
            atlas = new TextureAtlas(Gdx.files.internal("Esqueleto.atlas"));
            Gdx.app.log("Esqueleto", "Atlas cargado exitosamente");

            // ====================== ANIMACIÓN IDLE (QUIETO) ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            // NOTA: En el atlas está escrito "eaqueleto" (con 'a' extra)
            for (int i = 0; i <= 10; i++) {
                String frameName = String.format("eaqueleto_quieto%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("Esqueleto", "Frame idle encontrado: " + frameName);
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.15f, idleFrames);
                Gdx.app.log("Esqueleto", "Animación Idle cargada: " + idleFrames.size + " frames");
            } else {
                Gdx.app.log("Esqueleto", "No se encontraron frames para animación Idle");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 0; i <= 12; i++) {
                String frameName = String.format("esqueleto_andando%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                    Gdx.app.debug("Esqueleto", "Frame caminar encontrado: " + frameName);
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.1f, walkFrames);
                Gdx.app.log("Esqueleto", "Animación Caminar cargada: " + walkFrames.size + " frames");
            } else {
                Gdx.app.log("Esqueleto", "No se encontraron frames para animación Caminar");
            }

            // ====================== ANIMACIÓN ATACAR ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 0; i <= 17; i++) {
                String frameName = String.format("esqueleto_ataca%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                    Gdx.app.debug("Esqueleto", "Frame ataque encontrado: " + frameName);
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Esqueleto", "Animación Atacar cargada: " + attackFrames.size + " frames");
            } else {
                Gdx.app.log("Esqueleto", "No se encontraron frames para animación Atacar");
            }

            // ====================== ANIMACIÓN DAÑO (GOLPEADO) ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("esqueleto_golpeado%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                    Gdx.app.debug("Esqueleto", "Frame daño encontrado: " + frameName);
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.05f, hurtFrames);
                Gdx.app.log("Esqueleto", "Animación Daño cargada: " + hurtFrames.size + " frames");
            } else {
                Gdx.app.log("Esqueleto", "No se encontraron frames para animación Daño");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            // IMPORTANTE: Los frames de muerte van del 000 al 014
            for (int i = 0; i <= 14; i++) {
                String frameName = String.format("esqueleto_muerte%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    deathFrames.add(frame);
                    deathCargado = true;
                    Gdx.app.debug("Esqueleto", "Frame muerte encontrado: " + frameName);
                }
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.1f, deathFrames);
                Gdx.app.log("Esqueleto", "Animación Muerte cargada: " + deathFrames.size + " frames");
            } else {
                Gdx.app.log("Esqueleto", "ADVERTENCIA: No se encontraron frames para animación Muerte");
                Gdx.app.log("Esqueleto", "Se usará animación de muerte por defecto");
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            } else if (animacionCaminar != null) {
                frameActual = animacionCaminar.getKeyFrame(0);
            } else if (animacionAtacar != null) {
                frameActual = animacionAtacar.getKeyFrame(0);
            }

            Gdx.app.log("Esqueleto", "Carga de animaciones completada");

        } catch (Exception e) {
            Gdx.app.error("Esqueleto", "Error al cargar animaciones específicas", e);
            // No relanzar la excepción - se mantendrán las animaciones por defecto
        }
    }

    /**
     * Comportamiento específico del esqueleto al patrullar.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void patrullar(float delta) {
        // El esqueleto patrulla lentamente
        float velocidadOriginal = velocidad;
        velocidad = 40f; // Reducir velocidad en patrulla
        super.patrullar(delta);
        velocidad = velocidadOriginal; // Restaurar velocidad normal
    }

    /**
     * Comportamiento específico al morir.
     * CORREGIDO: Usa .length para arrays nativos de TextureRegion.
     */
    @Override
    protected void morir() {
        super.morir();

        Gdx.app.log("Esqueleto", "¡Esqueleto destruido!");
    }
}
