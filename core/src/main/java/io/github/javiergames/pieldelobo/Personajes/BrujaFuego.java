package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Enemigo Bruja de Fuego - Enemigo mágico que ataca con hechizos de fuego.
 * COMPLETO: Ahora lanza bolas de fuego visibles que el jugador puede ver y esquivar.
 *
 *  * @author Javier Gala
 *  * @version 2.1
 */
public class BrujaFuego extends Enemigos {

    private boolean lanzandoHechizo = false;
    private float tiempoPreparacion = 0;
    private static final float TIEMPO_PREPARACION_HECHIZO = 0.5f;

    // Referencia al stage para añadir bolas de fuego
    private Stage stageReferencia;

    // Para efectos de flotación
    private float tiempoFlotacion = 0;
    private float alturaFlotacion = 0;

    /**
     * Constructor de la Bruja de Fuego.
     * @param x Posición horizontal inicial
     * @param y Posición vertical inicial
     */
    public BrujaFuego(float x, float y) {
        super(x, y);

        // ====================== CONFIGURACIÓN DE LA BRUJA DE FUEGO ======================
        this.vidaMaxima = 4;
        this.vidaActual = 4;
        this.velocidad = 60f;
        this.distanciaMaxima = 80f;
        this.danoAtaque = 2;
        this.rangoAtaque = 180f;
        this.duracionAtaque = 1.5f;
        this.cooldownAtaqueMaximo = 4.0f;

        // ====================== CONFIGURACIÓN DE TAMAÑO Y HITBOX ======================
        this.tamanioBase = 150f;
        this.offsetHitboxX = 30f;
        this.offsetHitboxY = 20f;
        this.anchoHitbox = 90f;
        this.altoHitbox = 110f;

        // LA BRUJA DE FUEGO FLOTA - NO TIENE GRAVEDAD
        this.aplicarGravedad = false;
        this.enSuelo = true; // Considerarla "en suelo" aunque flote

        setSize(tamanioBase, tamanioBase);

        // Cargar animaciones con manejo de errores
        cargarAnimacionesConSeguridad();

        Gdx.app.log("BrujaFuego", "Creada en posición: " + x + ", " + y);
    }

    /**
     * Carga las animaciones de la bruja desde el atlas.
     */
    @Override
    protected void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe
            if (!Gdx.files.internal("Bruja_Fuego.atlas").exists()) {
                Gdx.app.log("BrujaFuego", "Atlas 'Bruja_Fuego.atlas' no encontrado");
                return;
            }

            atlas = new TextureAtlas(Gdx.files.internal("Bruja_Fuego.atlas"));
            Gdx.app.log("BrujaFuego", "Atlas Bruja_Fuego cargado exitosamente");

            // ====================== ANIMACIÓN IDLE (QUIETO) ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("mago_quieto%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("BrujaFuego", "Frame idle encontrado: " + frameName);
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.2f, idleFrames);
                Gdx.app.log("BrujaFuego", "Animación Idle cargada: " + idleFrames.size + " frames");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("mago_move%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                    Gdx.app.debug("BrujaFuego", "Frame caminar encontrado: " + frameName);
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.15f, walkFrames);
                Gdx.app.log("BrujaFuego", "Animación Caminar cargada: " + walkFrames.size + " frames");
            }

            // ====================== ANIMACIÓN ATACAR (HECHIZO DE FUEGO) ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("mago_ataque%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                    Gdx.app.debug("BrujaFuego", "Frame ataque encontrado: " + frameName);
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("BrujaFuego", "Animación Atacar cargada: " + attackFrames.size + " frames");
            }

            // ====================== ANIMACIÓN DAÑO (GOLPE) ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 0; i <= 3; i++) {
                String frameName = String.format("mago_golpe%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                    Gdx.app.debug("BrujaFuego", "Frame daño encontrado: " + frameName);
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.1f, hurtFrames);
                Gdx.app.log("BrujaFuego", "Animación Daño cargada: " + hurtFrames.size + " frames");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            for (int i = 0; i <= 4; i++) {
                String frameName = String.format("mago_muerte%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    deathFrames.add(frame);
                    deathCargado = true;
                    Gdx.app.debug("BrujaFuego", "Frame muerte encontrado: " + frameName);
                }
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.2f, deathFrames);
                Gdx.app.log("BrujaFuego", "Animación Muerte cargada: " + deathFrames.size + " frames");
            } else {
                Gdx.app.log("BrujaFuego", "ADVERTENCIA: No se encontró animación de muerte específica");
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            }

            Gdx.app.log("BrujaFuego", "Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("BrujaFuego", "Error al cargar animaciones", e);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar flotación si no está muerta
        if (!estaMuerto) {
            tiempoFlotacion += delta;
            alturaFlotacion = (float)Math.sin(tiempoFlotacion * 2) * 3f;
        }
    }

    /**
     * Establece referencia al stage para añadir bolas de fuego.
     */
    public void setStage(Stage stage) {
        this.stageReferencia = stage;
    }

    /**
     * Actualiza la IA específica de la bruja.
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    protected void actualizarIA(float delta) {
        if (objetivo == null || !objetivo.estaVivo()) {
            patrullarMagicamente(delta);
            return;
        }

        float distancia = calcularDistanciaAlObjetivo();

        if (distancia <= rangoAtaque) {
            // Objetivo en rango de hechizo
            direccionMovimiento.set(0, 0);
            mirarAlObjetivo();

            if (puedeAtacar && !estaAtacando) {
                prepararHechizo();
            }
        } else if (distancia > rangoAtaque * 1.3f) {
            // Objetivo muy lejos - acercarse flotando
            seguirObjetivoMagicamente(delta);
        } else {
            // Mantener distancia óptima para hechizos
            mantenerDistanciaMagica(delta);
        }
    }

    /**
     * Patrulla mágica de la bruja.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void patrullarMagicamente(float delta) {
        super.patrullar(delta);

        // Flotación vertical ligera
        setY(getY() + alturaFlotacion * delta);
    }

    /**
     * Sigue al objetivo con movimiento flotante.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void seguirObjetivoMagicamente(float delta) {
        if (objetivo == null) return;

        float direccionX = Math.signum(objetivo.getX() - getX());
        direccionMovimiento.x = direccionX;
        mirandoDerecha = (direccionX > 0);

        moverHorizontalmente(direccionX * velocidad * delta);

        // Flotación vertical durante el movimiento
        setY(getY() + alturaFlotacion * delta * 1.5f);
    }

    /**
     * Mantiene una distancia segura del objetivo.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void mantenerDistanciaMagica(float delta) {
        if (objetivo == null) return;

        float distancia = calcularDistanciaAlObjetivo();
        float distanciaIdeal = rangoAtaque * 0.7f;

        if (distancia < distanciaIdeal) {
            // Alejarse flotando del objetivo
            float direccionX = Math.signum(getX() - objetivo.getX());
            moverHorizontalmente(direccionX * velocidad * delta);
        }

        // Flotación estática
        setY(getY() + alturaFlotacion * delta);
    }

    /**
     * Prepara el hechizo de fuego.
     */
    private void prepararHechizo() {
        if (puedeAtacar && !estaAtacando && !estaMuerto && !recibiendoDano) {
            estaAtacando = true;
            tiempoAtaque = 0;
            puedeAtacar = false;
            lanzandoHechizo = true;
            tiempoPreparacion = 0;

            Gdx.app.log("BrujaFuego", "¡Preparando hechizo de fuego!");
        }
    }

    /**
     * Actualiza el estado del hechizo.
     * MODIFICADO: Ahora crea una bola de fuego.
     */
    @Override
    protected void actualizarAtaque(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;

            if (lanzandoHechizo) {
                tiempoPreparacion += delta;

                // Lanzar hechizo después del tiempo de preparación
                if (tiempoPreparacion >= TIEMPO_PREPARACION_HECHIZO) {
                    lanzarBolaFuego();
                    lanzandoHechizo = false;
                }
            }

            if (tiempoAtaque >= duracionAtaque) {
                estaAtacando = false;
                tiempoAtaque = 0;
            }
        }
    }

    /**
     * Lanza una bola de fuego hacia el objetivo.
     */
    private void lanzarBolaFuego() {
        if (stageReferencia == null || objetivo == null || !objetivo.estaVivo()) {
            return;
        }

        // Calcular posición de origen (manos de la bruja)
        float origenX = getX() + tamanioBase / 2;
        float origenY = getY() + tamanioBase * 0.7f;

        // Calcular dirección hacia el objetivo con un poco de aleatoriedad
        float objetivoX = objetivo.getX() + objetivo.getWidth() / 2;
        float objetivoY = objetivo.getY() + objetivo.getHeight() / 2;

        // Añadir un poco de aleatoriedad para que no sea perfecto
        float variacion = 0.2f;
        objetivoX += (Math.random() - 0.5) * 60 * variacion;
        objetivoY += (Math.random() - 0.5) * 40 * variacion;

        float dirX = objetivoX - origenX;
        float dirY = objetivoY - origenY;

        // Normalizar y ajustar velocidad (más lento que las flechas)
        float distancia = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        float velocidadBase = 250f;

        if (distancia > 0) {
            dirX = dirX / distancia * velocidadBase;
            dirY = dirY / distancia * velocidadBase;
        }

        // Ajustar para que la bola salga desde las manos
        if (mirandoDerecha) {
            origenX += tamanioBase * 0.4f;
        } else {
            origenX -= tamanioBase * 0.4f;
        }

        // Añadir efecto de arco parabólico (ligera curva hacia arriba)
        if (distancia > 100) {
            dirY += 50f; // Dar un poco de elevación
        }

        // Crear la bola de fuego
        BolaFuego bolaFuego = new BolaFuego(origenX, origenY, dirX, dirY, danoAtaque,
            mapaManager, objetivo);

        // Añadir al stage
        stageReferencia.addActor(bolaFuego);

        Gdx.app.log("BrujaFuego", "Bola de fuego lanzada desde (" + origenX + "," + origenY +
            ") hacia (" + objetivoX + "," + objetivoY + ") Velocidad: " + velocidadBase);
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

        // Interrumpir hechizo si estaba lanzando uno
        if (estaAtacando) {
            estaAtacando = false;
            lanzandoHechizo = false;
            Gdx.app.log("BrujaFuego", "¡Hechizo interrumpido!");
        }

        Gdx.app.log("BrujaFuego", "¡Bruja herida! Vida: " + vidaActual + "/" + vidaMaxima);

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
        Gdx.app.log("BrujaFuego", "¡Bruja de fuego derrotada!");
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        // Aplicar efecto de flotación
        float drawY = getY() + alturaFlotacion;

        if (frameActual != null) {
            float alpha = 1.0f;

            // Efectos visuales según estado
            if (recibiendoDano) {
                alpha = 0.5f;
            } else if (estaMuerto) {
                alpha = 1.0f - (tiempoMuerte / 2.0f);
                if (alpha < 0.1f) alpha = 0.1f;
            }

            batch.setColor(1, 1, 1, alpha);

            if (mirandoDerecha) {
                batch.draw(frameActual, getX(), drawY, tamanioBase, tamanioBase);
            } else {
                batch.draw(frameActual,
                    getX() + tamanioBase, drawY,
                    -tamanioBase, tamanioBase);
            }

            batch.setColor(1, 1, 1, 1);
        }
    }
}
