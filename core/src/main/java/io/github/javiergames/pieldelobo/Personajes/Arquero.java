package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Enemigo Arquero - Enemigo a distancia que ataca con flechas.
 * Ahora lanza flechas visibles que el jugador puede ver y esquivar.
 *
 *  * @author Javier Gala
 *  * @version 2.1
 */
public class Arquero extends Enemigos {

    private boolean disparando = false;
    private float tiempoDisparo = 0;
    private static final float TIEMPO_PREPARACION = 0.3f;

    // Referencia al stage para añadir flechas
    private Stage stageReferencia;

    // Para controlar la frecuencia de disparo
    private float tiempoEntreDisparos = 0;
    private static final float TIEMPO_ENTRE_DISPAROS = 1.5f;

    /**
     * Constructor del Arquero.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     */
    public Arquero(float x, float y) {
        super(x, y);

        // ====================== CONFIGURACIÓN DEL ARQUERO ======================
        this.vidaMaxima = 2;
        this.vidaActual = 2;
        this.velocidad = 70f;
        this.distanciaMaxima = 100f;
        this.danoAtaque = 1;
        this.rangoAtaque = 200f;
        this.duracionAtaque = 1.0f;
        this.cooldownAtaqueMaximo = 3.0f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 64f;
        this.offsetHitboxX = 10f;
        this.offsetHitboxY = 20f;
        this.anchoHitbox = 44f;
        this.altoHitbox = 36f;

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Arquero", "Creado en posición: " + x + ", " + y);
        Gdx.app.log("Arquero", "Hitbox: offsetY=" + offsetHitboxY + ", alto=" + altoHitbox);
    }

    /**
     * Carga las animaciones del arquero desde el atlas.
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe
            if (!Gdx.files.internal("Arquero.atlas").exists()) {
                Gdx.app.log("Arquero", "Atlas 'Arquero.atlas' no encontrado");
                return;
            }

            atlas = new TextureAtlas(Gdx.files.internal("Arquero.atlas"));
            Gdx.app.log("Arquero", "Atlas Arquero cargado exitosamente");

            // ====================== ANIMACIÓN IDLE (QUIETO) - USANDO PRIMEROS FRAMES DE ANDAR ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            // Usar los primeros 4 frames de arqueraAndar para idle
            for (int i = 0; i < 4; i++) {
                String frameName = String.format("arqueraAndar%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("Arquero", "Frame idle encontrado: " + frameName);
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.2f, idleFrames);
                Gdx.app.log("Arquero", "Animación Idle cargada: " + idleFrames.size + " frames");
            } else {
                Gdx.app.log("Arquero", "No se encontraron frames para animación Idle");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            // Todos los frames de arqueraAndar
            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("arqueraAndar%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                    Gdx.app.debug("Arquero", "Frame caminar encontrado: " + frameName);
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.15f, walkFrames);
                Gdx.app.log("Arquero", "Animación Caminar cargada: " + walkFrames.size + " frames");
            } else {
                Gdx.app.log("Arquero", "No se encontraron frames para animación Caminar");
            }

            // ====================== ANIMACIÓN ATACAR (DISPARAR) ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            // Todos los frames de arqueraAtaque
            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("arqueraAtaque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                    Gdx.app.debug("Arquero", "Frame ataque encontrado: " + frameName);
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Arquero", "Animación Atacar cargada: " + attackFrames.size + " frames");
            } else {
                Gdx.app.log("Arquero", "No se encontraron frames para animación Atacar");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            // Todos los frames de arqueraMuerte
            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("arqueraMuerte%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    deathFrames.add(frame);
                    deathCargado = true;
                    Gdx.app.debug("Arquero", "Frame muerte encontrado: " + frameName);
                }
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.15f, deathFrames);
                Gdx.app.log("Arquero", "Animación Muerte cargada: " + deathFrames.size + " frames");
            } else {
                Gdx.app.log("Arquero", "No se encontraron frames para animación Muerte");
            }

            // ====================== ANIMACIÓN DAÑO (GOLPE) ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            // Usar los primeros 2 frames de ataque para daño
            for (int i = 0; i < 2; i++) {
                String frameName = String.format("arqueraAtaque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.1f, hurtFrames);
                Gdx.app.log("Arquero", "Animación Daño cargada: " + hurtFrames.size + " frames");
            } else {
                // Usar el primer frame de muerte como alternativa
                TextureRegion hurtFrame = atlas.findRegion("arqueraMuerte000");
                if (hurtFrame != null) {
                    hurtFrames.add(hurtFrame);
                    animacionDano = new Animation<>(0.1f, hurtFrames);
                    Gdx.app.log("Arquero", "Animación Daño cargada (usando frame de muerte)");
                }
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            } else if (animacionCaminar != null) {
                frameActual = animacionCaminar.getKeyFrame(0);
            } else if (animacionAtacar != null) {
                frameActual = animacionAtacar.getKeyFrame(0);
            }

            // Mostrar resumen de animaciones cargadas
            Gdx.app.log("Arquero", "=== RESUMEN DE ANIMACIONES ===");
            Gdx.app.log("Arquero", "Idle cargada: " + (animacionIdle != null) +
                " (" + (animacionIdle != null ? animacionIdle.getKeyFrames().length : 0) + " frames)");
            Gdx.app.log("Arquero", "Caminar cargada: " + (animacionCaminar != null) +
                " (" + (animacionCaminar != null ? animacionCaminar.getKeyFrames().length : 0) + " frames)");
            Gdx.app.log("Arquero", "Atacar cargada: " + (animacionAtacar != null) +
                " (" + (animacionAtacar != null ? animacionAtacar.getKeyFrames().length : 0) + " frames)");
            Gdx.app.log("Arquero", "Daño cargada: " + (animacionDano != null) +
                " (" + (animacionDano != null ? animacionDano.getKeyFrames().length : 0) + " frames)");
            Gdx.app.log("Arquero", "Muerte cargada: " + (animacionMuerte != null) +
                " (" + (animacionMuerte != null ? animacionMuerte.getKeyFrames().length : 0) + " frames)");

        } catch (Exception e) {
            Gdx.app.error("Arquero", "Error al cargar animaciones", e);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar tiempo entre disparos
        if (tiempoEntreDisparos > 0) {
            tiempoEntreDisparos -= delta;
        }
    }

    /**
     * Establece referencia al stage para añadir flechas.
     */
    public void setStage(Stage stage) {
        this.stageReferencia = stage;
    }

    /**
     * Actualiza la IA específica del arquero.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void actualizarIA(float delta) {
        if (objetivo == null || !objetivo.estaVivo()) {
            patrullar(delta);
            return;
        }

        float distancia = calcularDistanciaAlObjetivo();

        if (distancia <= rangoAtaque) {
            // Objetivo en rango de disparo
            direccionMovimiento.set(0, 0);
            mirarAlObjetivo();

            if (puedeAtacar && !estaAtacando && tiempoEntreDisparos <= 0) {
                prepararDisparo();
            }
        } else if (distancia > rangoAtaque * 1.5f) {
            // Objetivo muy lejos - acercarse
            seguirObjetivo(delta);
        } else {
            // Mantener distancia óptima
            mantenerDistancia(delta);
        }
    }

    /**
     * Prepara el disparo del arquero.
     */
    private void prepararDisparo() {
        if (puedeAtacar && !estaAtacando && !estaMuerto && !recibiendoDano && tiempoEntreDisparos <= 0) {
            estaAtacando = true;
            tiempoAtaque = 0;
            puedeAtacar = false;
            disparando = true;
            tiempoDisparo = 0;

            Gdx.app.log("Arquero", "¡Preparando disparo!");
        }
    }

    /**
     * Mantiene una distancia segura del objetivo.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void mantenerDistancia(float delta) {
        if (objetivo == null) return;

        float distancia = calcularDistanciaAlObjetivo();
        float distanciaIdeal = rangoAtaque * 0.8f;

        if (distancia < distanciaIdeal) {
            // Alejarse del objetivo
            float direccionX = Math.signum(getX() - objetivo.getX());
            moverHorizontalmente(direccionX * velocidad * delta);
        } else if (distancia > distanciaIdeal * 1.2f) {
            // Acercarse al objetivo
            float direccionX = Math.signum(objetivo.getX() - getX());
            moverHorizontalmente(direccionX * velocidad * delta);
        }
    }

    /**
     * Actualiza el estado del disparo.
     * MODIFICADO: Ahora crea una flecha cuando dispara.
     */
    @Override
    protected void actualizarAtaque(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;

            if (disparando) {
                tiempoDisparo += delta;

                // Disparar en el frame específico (aproximadamente mitad de la animación)
                if (tiempoDisparo >= TIEMPO_PREPARACION &&
                    tiempoDisparo - delta < TIEMPO_PREPARACION) {
                    // Crear y lanzar flecha
                    lanzarFlecha();
                    Gdx.app.log("Arquero", "¡Flecha disparada!");
                }
            }

            if (tiempoAtaque >= duracionAtaque) {
                estaAtacando = false;
                tiempoAtaque = 0;
                tiempoDisparo = 0;
                disparando = false;
                tiempoEntreDisparos = TIEMPO_ENTRE_DISPAROS;
            }
        }
    }

    /**
     * Lanza una flecha hacia el objetivo.
     */
    private void lanzarFlecha() {
        if (stageReferencia == null || objetivo == null || !objetivo.estaVivo()) {
            return;
        }

        // Calcular posición de origen (del arco)
        float origenX = getX() + tamanioBase / 2;
        float origenY = getY() + tamanioBase * 0.6f; // A la altura del arco

        // Calcular dirección hacia el objetivo
        float objetivoX = objetivo.getX() + objetivo.getWidth() / 2;
        float objetivoY = objetivo.getY() + objetivo.getHeight() / 2;

        // Añadir un poco de aleatoriedad para que no sea perfecto
        float variacion = 0.1f; // 10% de variación
        objetivoX += (Math.random() - 0.5) * 40 * variacion;
        objetivoY += (Math.random() - 0.5) * 40 * variacion;

        float dirX = objetivoX - origenX;
        float dirY = objetivoY - origenY;

        // Normalizar y ajustar velocidad
        float distancia = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        float velocidadBase = 400f; // píxeles por segundo (más rápido para flechas)

        if (distancia > 0) {
            dirX = dirX / distancia * velocidadBase;
            dirY = dirY / distancia * velocidadBase;
        }

        // Ajustar para que la flecha salga desde la mano
        if (mirandoDerecha) {
            origenX += tamanioBase * 0.3f;
        } else {
            origenX -= tamanioBase * 0.3f;
        }

        // Crear la flecha
        Flecha flecha = new Flecha(origenX, origenY, dirX, dirY, danoAtaque,
            mapaManager, objetivo);

        // Añadir al stage
        stageReferencia.addActor(flecha);

        Gdx.app.log("Arquero", "Flecha lanzada desde (" + origenX + "," + origenY +
            ") hacia (" + objetivoX + "," + objetivoY + ") Velocidad: " + velocidadBase);
    }

    /**
     * Comportamiento específico al recibir daño.
     */
    @Override
    public void recibirDano() {
        super.recibirDano();

        // El arquero retrocede al recibir daño
        if (!estaMuerto) {
            float retroceso = mirandoDerecha ? -30f : 30f;
            setX(getX() + retroceso);

            // Interrumpir disparo si estaba disparando
            if (estaAtacando) {
                estaAtacando = false;
                disparando = false;
                tiempoEntreDisparos = TIEMPO_ENTRE_DISPAROS * 0.5f; // Menor tiempo de espera
                Gdx.app.log("Arquero", "¡Disparo interrumpido!");
            }

            Gdx.app.log("Arquero", "¡Arquero retrocede por el impacto!");
        }
    }

    /**
     * Comportamiento específico al morir.
     */
    @Override
    protected void morir() {
        super.morir();
        Gdx.app.log("Arquero", "¡Arquero eliminado!");
    }

    /**
     * Método específico para el arquero: verificar posición sobre el suelo.
     */
    @Override
    public void corregirPosicionInicial() {
        if (!aplicarGravedad || mapaManager == null) return;

        float yOriginal = getY();

        // Bajar hasta encontrar suelo
        for (int i = 0; i < 300; i++) {
            setY(getY() - 1);
            if (mapaManager.hayColision(getHitbox())) {
                setY(getY() + 1);
                enSuelo = true;
                velocidadY = 0;
                if (i > 0) {
                    Gdx.app.log("Arquero", "Arquero bajado " + i + " píxeles al suelo");
                }
                return;
            }
        }

        // Si no encontró suelo, volver a la posición original
        setY(yOriginal);
        Gdx.app.log("Arquero", "ADVERTENCIA: Arquero no encontró suelo después de bajar 300px");
    }
}
