package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Enemigo Bandido - Existen dos variantes: HeavyBandit (pesado) y LightBandit (ligero).
 *  Carga correcta de frames según el atlas.
 *
 *   * @author Javier Gala
 *  * @version 2.0
 */
public class Bandido extends Enemigos {

    private boolean esPesado; // True = HeavyBandit, False = LightBandit

    /**
     * Constructor del Bandido.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     * @param esPesado True para HeavyBandit, False para LightBandit
     */
    public Bandido(float x, float y, boolean esPesado) {
        super(x, y);
        this.esPesado = esPesado;

        // ====================== CONFIGURACIÓN SEGÚN TIPO ======================
        if (esPesado) {
            // HEAVY BANDIT (Bandido Pesado)
            this.vidaMaxima = 5;
            this.vidaActual = 5;
            this.velocidad = 50f;
            this.danoAtaque = 2;
            this.rangoAtaque = 55f;
            this.duracionAtaque = 0.7f;
        } else {
            // LIGHT BANDIT (Bandido Ligero)
            this.vidaMaxima = 2;
            this.vidaActual = 2;
            this.velocidad = 80f;
            this.danoAtaque = 1;
            this.rangoAtaque = 45f;
            this.duracionAtaque = 0.4f;
        }

        // ====================== CONFIGURACIONES COMUNES ======================
        this.distanciaMaxima = 200f;
        this.cooldownAtaqueMaximo = 2.0f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 48f;
        this.offsetHitboxX = 6f;
        this.offsetHitboxY = 3f;
        this.anchoHitbox = 36f;
        this.altoHitbox = 42f;

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Bandido", (esPesado ? "Heavy" : "Light") +
            " Bandido creado en: " + x + ", " + y);
    }

    /**
     * Carga las animaciones específicas del bandido desde el atlas.
     * CORREGIDO: Según la estructura exacta del atlas.
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Cargar atlas
            atlas = new TextureAtlas(Gdx.files.internal("Bandido.atlas"));
            Gdx.app.log("Bandido", "Atlas Bandido cargado exitosamente");

            // Prefijo según el tipo de bandido
            String prefix = esPesado ? "HeavyBandit_" : "LightBandit_";
            String tipoStr = esPesado ? "Heavy" : "Light";

            // ====================== ANIMACIÓN IDLE ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            // Para HeavyBandit usar "CombatIdle" (sin espacio)
            // Para LightBandit usar el que corresponda
            String idleName = esPesado ? "CombatIdle" : "Idle";

            for (int i = 0; i < 4; i++) {
                TextureRegion frame = atlas.findRegion(prefix + idleName, i);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("Bandido", "Frame idle " + i + " encontrado");
                } else {
                    break;
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.2f, idleFrames);
                Gdx.app.log("Bandido", tipoStr + " - Animación Idle cargada: " + idleFrames.size + " frames");
            } else {
                Gdx.app.log("Bandido", tipoStr + " - Usando idle por defecto");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 0; i < 8; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Run", i);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.1f, walkFrames);
                Gdx.app.log("Bandido", tipoStr + " - Animación Caminar cargada: " + walkFrames.size + " frames");
            }

            // ====================== ANIMACIÓN ATACAR ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 0; i < 8; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Attack", i);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Bandido", tipoStr + " - Animación Atacar cargada: " + attackFrames.size + " frames");
            }

            // ====================== ANIMACIÓN DAÑO ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 0; i < 2; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Hurt", i);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.1f, hurtFrames);
                Gdx.app.log("Bandido", tipoStr + " - Animación Daño cargada: " + hurtFrames.size + " frames");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            TextureRegion deathFrame = atlas.findRegion(prefix + "Death", 0);
            if (deathFrame != null) {
                deathFrames.add(deathFrame);
                deathCargado = true;
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.5f, deathFrames);
                Gdx.app.log("Bandido", tipoStr + " - Animación Muerte cargada");
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null && idleFrames.size > 0) {
                frameActual = idleFrames.first();
                Gdx.app.log("Bandido", "Frame inicial establecido desde animación Idle");
            } else if (walkFrames.size > 0) {
                frameActual = walkFrames.first();
                Gdx.app.log("Bandido", "Frame inicial establecido desde animación Caminar");
            } else if (atlas.getRegions().size > 0) {
                // Usar cualquier frame disponible
                frameActual = atlas.getRegions().first();
                Gdx.app.log("Bandido", "Frame inicial establecido desde primera región del atlas");
            }

            Gdx.app.log("Bandido", tipoStr + " Bandido - Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("Bandido", "Error al cargar animaciones", e);
        }
    }

    /**
     * Comportamiento específico del bandido al atacar.
     */
    @Override
    public void atacar() {
        super.atacar();

        // Los bandidos emiten sonido al atacar
        if (esPesado) {
            Gdx.app.log("Bandido", "¡Heavy Bandit ataca con fuerza!");
        } else {
            Gdx.app.log("Bandido", "¡Light Bandit ataca rápidamente!");
        }
    }
}
