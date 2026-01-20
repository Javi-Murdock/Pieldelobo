package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/**
 * Enemigo Bárbaro - Un guerrero poderoso con ataques fuertes.
 *  Carga correcta de frames con espacio en el nombre.
 *
 *   * @author Javier Gala
 *  * @version 2.0
 */
public class Barbaro extends Enemigos {

    private boolean cargandoAtaque = false;
    private float tiempoCarga = 0;
    private static final float TIEMPO_CARGA_MAXIMO = 0.8f;

    /**
     * Constructor del Bárbaro.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     */
    public Barbaro(float x, float y) {
        super(x, y);

        // ====================== CONFIGURACIÓN DEL BÁRBARO ======================
        this.vidaMaxima = 6;
        this.vidaActual = 6;
        this.velocidad = 55f;
        this.distanciaMaxima = 120f;
        this.danoAtaque = 3;
        this.rangoAtaque = 60f;
        this.duracionAtaque = 1.0f;
        this.cooldownAtaqueMaximo = 2.5f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 48f;
        this.offsetHitboxX = 8f;
        this.offsetHitboxY = 6f;
        this.anchoHitbox = 32f;
        this.altoHitbox = 42f;

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Barbaro", "Creado en posición: " + x + ", " + y);
    }

    /**
     * Carga las animaciones del bárbaro desde el atlas.
     * ¡IMPORTANTE! El atlas tiene "Combat Idle" con ESPACIO.
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Cargar atlas
            atlas = new TextureAtlas(Gdx.files.internal("Barbaro.atlas"));
            Gdx.app.log("Barbaro", "Atlas Barbaro cargado exitosamente");

            String prefix = "LightBandit_";

            // ====================== ANIMACIÓN IDLE (QUIETO) ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            // ¡IMPORTANTE! El atlas muestra "LightBandit_Combat Idle" (con ESPACIO)
            for (int i = 0; i < 4; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Combat Idle", i);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("Barbaro", "Frame idle encontrado: Combat Idle índice " + i);
                } else {
                    // Intentar sin espacio como fallback
                    frame = atlas.findRegion(prefix + "CombatIdle", i);
                    if (frame != null) {
                        idleFrames.add(frame);
                        idleCargado = true;
                        Gdx.app.debug("Barbaro", "Frame idle encontrado: CombatIdle índice " + i);
                    } else {
                        break;
                    }
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.2f, idleFrames);
                Gdx.app.log("Barbaro", "Animación Idle cargada: " + idleFrames.size + " frames");
            } else {
                Gdx.app.log("Barbaro", "No se encontraron frames para animación Idle");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 0; i < 8; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Run", i);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                    Gdx.app.debug("Barbaro", "Frame caminar encontrado: Run índice " + i);
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.1f, walkFrames);
                Gdx.app.log("Barbaro", "Animación Caminar cargada: " + walkFrames.size + " frames");
            } else {
                Gdx.app.log("Barbaro", "No se encontraron frames para animación Caminar");
            }

            // ====================== ANIMACIÓN ATACAR ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 0; i < 8; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Attack", i);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                    Gdx.app.debug("Barbaro", "Frame ataque encontrado: Attack índice " + i);
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Barbaro", "Animación Atacar cargada: " + attackFrames.size + " frames");
            } else {
                Gdx.app.log("Barbaro", "No se encontraron frames para animación Atacar");
            }

            // ====================== ANIMACIÓN DAÑO (GOLPE) ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 0; i < 2; i++) {
                TextureRegion frame = atlas.findRegion(prefix + "Hurt", i);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                    Gdx.app.debug("Barbaro", "Frame daño encontrado: Hurt índice " + i);
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.1f, hurtFrames);
                Gdx.app.log("Barbaro", "Animación Daño cargada: " + hurtFrames.size + " frames");
            } else {
                Gdx.app.log("Barbaro", "No se encontraron frames para animación Daño");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            TextureRegion deathFrame = atlas.findRegion(prefix + "Death", 0);
            if (deathFrame != null) {
                deathFrames.add(deathFrame);
                deathCargado = true;
                Gdx.app.debug("Barbaro", "Frame muerte encontrado: Death");
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.5f, deathFrames);
                Gdx.app.log("Barbaro", "Animación Muerte cargada");
            } else {
                Gdx.app.log("Barbaro", "No se encontraron frames para animación Muerte");
            }

            // ====================== FRAME INICIAL ======================
            if (idleFrames.size > 0) {
                frameActual = idleFrames.first();
                Gdx.app.log("Barbaro", "Frame inicial establecido desde animación Idle");
            } else if (walkFrames.size > 0) {
                frameActual = walkFrames.first();
                Gdx.app.log("Barbaro", "Frame inicial establecido desde animación Caminar");
            } else if (atlas.getRegions().size > 0) {
                // Usar el primer frame disponible
                frameActual = atlas.getRegions().first();
                Gdx.app.log("Barbaro", "Frame inicial establecido desde primera región del atlas");
            } else {
                Gdx.app.log("Barbaro", "ADVERTENCIA: No se pudo establecer frame inicial");
            }

            Gdx.app.log("Barbaro", "Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("Barbaro", "Error al cargar animaciones", e);
        }
    }

    /**
     * Actualiza la IA específica del bárbaro.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void actualizarIA(float delta) {
        if (objetivo == null || !objetivo.estaVivo()) {
            patrullarAgresivamente(delta);
            return;
        }

        float distancia = calcularDistanciaAlObjetivo();

        if (distancia <= rangoAtaque) {
            // Objetivo en rango de ataque
            direccionMovimiento.set(0, 0);
            mirarAlObjetivo();

            if (puedeAtacar && !estaAtacando) {
                prepararAtaqueCargado();
            }
        } else if (distancia > rangoAtaque * 1.5f) {
            // Objetivo muy lejos - cargar agresivamente
            cargarHaciaObjetivo(delta);
        } else {
            // Mantener distancia de combate
            mantenerDistanciaCombate(delta);
        }
    }

    /**
     * Patrulla de forma agresiva.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void patrullarAgresivamente(float delta) {
        // Patrulla más rápida y con cambios bruscos de dirección
        float velocidadPatrulla = velocidad * 1.2f;
        distanciaRecorrida += Math.abs(direccionMovimiento.x) * velocidadPatrulla * delta;

        if (distanciaRecorrida >= distanciaMaxima) {
            direccionMovimiento.x *= -1;
            mirandoDerecha = (direccionMovimiento.x > 0);
            distanciaRecorrida = 0;

            // A veces hace una pausa agresiva
            if (Math.random() < 0.3f) {
                direccionMovimiento.set(0, 0);
            }
        }

        moverHorizontalmente(direccionMovimiento.x * velocidadPatrulla * delta);
    }

    /**
     * Carga agresivamente hacia el objetivo.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void cargarHaciaObjetivo(float delta) {
        if (objetivo == null) return;

        float direccionX = Math.signum(objetivo.getX() - getX());
        direccionMovimiento.x = direccionX;
        mirandoDerecha = (direccionX > 0);

        // Cargar más rápido de lo normal
        moverHorizontalmente(direccionX * velocidad * 1.5f * delta);
    }

    /**
     * Mantiene una distancia óptima para el combate.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void mantenerDistanciaCombate(float delta) {
        if (objetivo == null) return;

        float distancia = calcularDistanciaAlObjetivo();
        float distanciaIdeal = rangoAtaque * 0.7f;

        if (distancia < distanciaIdeal) {
            // Retroceder un poco
            float direccionX = Math.signum(getX() - objetivo.getX());
            moverHorizontalmente(direccionX * velocidad * 0.5f * delta);
        } else if (distancia > distanciaIdeal * 1.3f) {
            // Acercarse un poco
            float direccionX = Math.signum(objetivo.getX() - getX());
            moverHorizontalmente(direccionX * velocidad * 0.8f * delta);
        }
        // Si está en la distancia ideal, no moverse
    }

    /**
     * Prepara un ataque cargado.
     */
    private void prepararAtaqueCargado() {
        if (puedeAtacar && !estaAtacando && !estaMuerto && !recibiendoDano) {
            cargandoAtaque = true;
            tiempoCarga = 0;
            estaAtacando = true; // Marcar como atacando durante la carga
            tiempoAtaque = 0;
            puedeAtacar = false;

            Gdx.app.log("Barbaro", "¡Preparando ataque cargado!");
        }
    }

    /**
     * Actualiza el estado del ataque cargado.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void actualizarAtaque(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;

            if (cargandoAtaque) {
                tiempoCarga += delta;

                // Lanzar ataque después del tiempo de carga
                if (tiempoCarga >= TIEMPO_CARGA_MAXIMO) {
                    ejecutarAtaqueCargado();
                    cargandoAtaque = false;
                }
            }

            if (tiempoAtaque >= duracionAtaque) {
                estaAtacando = false;
                tiempoAtaque = 0;
                tiempoCarga = 0;
            }
        }
    }

    /**
     * Ejecuta el ataque cargado.
     */
    private void ejecutarAtaqueCargado() {
        Gdx.app.log("Barbaro", "¡Ataque cargado ejecutado!");
        verificarGolpe();

        // El bárbaro se mueve hacia adelante durante el ataque
        if (objetivo != null && !estaMuerto) {
            float impulso = mirandoDerecha ? 30f : -30f;
            setX(getX() + impulso);
        }
    }

    /**
     * Comportamiento específico al recibir daño.
     */
    @Override
    public void recibirDano() {
        if (estaMuerto || recibiendoDano) return;

        vidaActual--;
        recibiendoDano = true;
        tiempoDano = 0;

        // Interrumpir ataque cargado si estaba en uno
        if (estaAtacando) {
            estaAtacando = false;
            cargandoAtaque = false;
            Gdx.app.log("Barbaro", "¡Ataque interrumpido!");
        }

        Gdx.app.log("Barbaro", "¡Bárbaro herido! Vida: " + vidaActual + "/" + vidaMaxima);

        if (vidaActual <= 0) {
            morir();
        } else {
            // El bárbaro se enfurece al recibir daño (más velocidad)
            velocidad *= 1.2f;
            Gdx.app.log("Barbaro", "¡Bárbaro enfurecido! Velocidad: " + velocidad);
        }
    }

    /**
     * Comportamiento específico al morir.
     */
    @Override
    protected void morir() {
        super.morir();
        Gdx.app.log("Barbaro", "¡Bárbaro caído en combate!");
    }
}
