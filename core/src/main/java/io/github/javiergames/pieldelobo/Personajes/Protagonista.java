package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Personaje protagonista del juego con físicas de plataformas.
 * CORREGIDO: Problema de atascamiento en esquinas mejorando el manejo de colisiones.
 * MEJORADO: Sistema de resolución de colisiones más robusto.
 *
 *  * @author Javier Gala
 *  * @version 2.0
 */
public class Protagonista extends Actor {
    // ====================== RECURSOS GRÁFICOS ======================
    private TextureAtlas atlas;
    private Animation<TextureRegion> animacionIdle;
    private Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionSaltar;
    private Animation<TextureRegion> animacionAtacar;
    private Animation<TextureRegion> animacionEspecial;
    private Animation<TextureRegion> animacionDefensa;
    private TextureRegion frameActual;
    private float tiempoAnimacion = 0;

    // ====================== PROPIEDADES FÍSICAS ======================
    private static final float VELOCIDAD = 200f;
    private static final float GRAVEDAD = -600f; // REDUCIDA de -900f a -600f
    private static final float FUERZA_SALTO = 350f; // REDUCIDA de 400f
    private static final float FUERZA_SALTO_ESPECIAL = 450f; // REDUCIDA de 500f
    private float velocidadY = 0;
    private boolean enSuelo = true;
    private int saltosRestantes = 1;
    private static final int MAX_SALTOS = 2;
    private static final float VELOCIDAD_MAX_CAIDA = -500f; // Límite de caída más lento
    private static final float AJUSTE_SUELO_PRECISION = 0.5f; // Precisión del ajuste
    // ====================== SISTEMA DE VIDAS ======================
    private int vidasMaximas = 3;
    private int vidasActuales = 3;
    private boolean invencible = false;
    private float tiempoInvencibilidad = 0;
    private static final float DURACION_INVENCIBILIDAD = 2.0f;

    // ====================== ESTADOS DEL PERSONAJE ======================
    private boolean estaAtacando = false;
    private boolean estaAtacandoEspecial = false;
    private boolean estaDefendiendo = false;
    private boolean mirandoDerecha = true;
    private float tiempoAtaque = 0;
    private float tiempoAtaqueEspecial = 0;
    private static final float DURACION_ATAQUE = 0.3f;
    private static final float DURACION_ATAQUE_ESPECIAL = 0.5f;

    // ====================== ESTADOS DE MOVIMIENTO ======================
    private boolean estaMoviendose = false;
    private float velocidadXActual = 0;
    private boolean huboInputUltimoFrame = false;

    // ====================== CONFIGURACIÓN HITBOX ======================
    private static final float OFFSET_COLISION_X = 10f;
    private static final float OFFSET_COLISION_Y = 2f;
    private static final float ANCHO_COLISION = 20f;
    private static final float ALTO_COLISION = 36f;

    private static final float OFFSET_COMBATE_X = 15f;
    private static final float OFFSET_COMBATE_Y = 5f;
    private static final float ANCHO_COMBATE = 10f;
    private static final float ALTO_COMBATE = 30f;

    // ====================== TAMAÑOS DE SPRITES ======================
    private static final float TAMANIO_BASE = 40f;

    // ====================== SISTEMA DE ATAQUE ======================
    private int danoAtaqueBasico = 1;
    private int danoAtaqueEspecial = 2;
    private float rangoAtaque = 60f;
    private float rangoAtaqueEspecial = 80f;

    // ====================== REFERENCIAS ======================
    private MapaManager mapaManager;

    // ====================== SISTEMA DE COLISIONES MEJORADO ======================
    private boolean intentandoEscaparEsquina = false;   // Para evitar atascamiento
    private float tiempoAtascado = 0;                   // Tiempo atascado en esquina
    private static final float TIEMPO_MAX_ATASCADO = 0.5f; // Medio segundo máximo
    private float ultimaPosicionValidaX = 0;            // Última posición X sin colisión
    private float ultimaPosicionValidaY = 0;            // Última posición Y sin colisión

    // ====================== DEBUG ======================
    private boolean mostrarDebugHitbox = false;

    // ====================== CONSTRUCTOR ======================

    public Protagonista() {
        cargarAnimaciones();
        setPosition(100, 100);
        setSize(TAMANIO_BASE, TAMANIO_BASE);
        ultimaPosicionValidaX = getX();
        ultimaPosicionValidaY = getY();
    }

    // ====================== CARGA DE RECURSOS ======================

    private void cargarAnimaciones() {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("sprites.atlas"));

            Array<TextureRegion> idleFrames = new Array<>();
            idleFrames.add(atlas.findRegion("idle/quieto0"));
            idleFrames.add(atlas.findRegion("idle/quieto1"));
            idleFrames.add(atlas.findRegion("idle/quieto2"));
            animacionIdle = new Animation<>(0.15f, idleFrames);

            Array<TextureRegion> runFrames = new Array<>();
            runFrames.add(atlas.findRegion("run/run0"));
            runFrames.add(atlas.findRegion("run/run1"));
            runFrames.add(atlas.findRegion("run/run2"));
            runFrames.add(atlas.findRegion("run/run3"));
            runFrames.add(atlas.findRegion("run/run4"));
            runFrames.add(atlas.findRegion("run/run5"));
            runFrames.add(atlas.findRegion("run/run6"));
            runFrames.add(atlas.findRegion("run/run7"));
            runFrames.add(atlas.findRegion("run/run8"));
            runFrames.add(atlas.findRegion("run/run9"));
            animacionCorrer = new Animation<>(0.05f, runFrames);

            Array<TextureRegion> jumpFrames = new Array<>();
            jumpFrames.add(atlas.findRegion("jump/jump0"));
            jumpFrames.add(atlas.findRegion("jump/jump1"));
            animacionSaltar = new Animation<>(0.2f, jumpFrames);

            Array<TextureRegion> attackFrames = new Array<>();
            attackFrames.add(atlas.findRegion("attack/attack0"));
            attackFrames.add(atlas.findRegion("attack/attack1"));
            attackFrames.add(atlas.findRegion("attack/attack2"));
            attackFrames.add(atlas.findRegion("attack/attack3"));
            animacionAtacar = new Animation<>(DURACION_ATAQUE / 4, attackFrames);

            Array<TextureRegion> specialFrames = new Array<>();
            specialFrames.add(atlas.findRegion("attack/attack2"));
            specialFrames.add(atlas.findRegion("attack/attack3"));
            specialFrames.add(atlas.findRegion("attack/attack0"));
            specialFrames.add(atlas.findRegion("attack/attack1"));
            animacionEspecial = new Animation<>(DURACION_ATAQUE_ESPECIAL / 4, specialFrames);

            Array<TextureRegion> defenseFrames = new Array<>();
            defenseFrames.add(atlas.findRegion("idle/quieto0"));
            defenseFrames.add(atlas.findRegion("idle/quieto1"));
            animacionDefensa = new Animation<>(0.1f, defenseFrames);

            frameActual = animacionIdle.getKeyFrame(0);

        } catch (Exception e) {
            Gdx.app.error("Protagonista", "Error al cargar animaciones", e);
            frameActual = new TextureRegion();
        }
    }

    // ====================== MÉTODOS DE CICLO DE VIDA ======================

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar tiempo atascado
        if (intentandoEscaparEsquina) {
            tiempoAtascado += delta;
            if (tiempoAtascado > TIEMPO_MAX_ATASCADO) {
                escaparDeEsquina();
            }
        }

        actualizarAnimacion(delta);
        actualizarFisicas(delta);
        actualizarAtaques(delta);
        actualizarInvencibilidad(delta);
    }

    private void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        if (estaDefendiendo) {
            frameActual = animacionDefensa.getKeyFrame(tiempoAnimacion, true);
        } else if (estaAtacandoEspecial) {
            frameActual = animacionEspecial.getKeyFrame(tiempoAnimacion, false);
        } else if (estaAtacando) {
            frameActual = animacionAtacar.getKeyFrame(tiempoAnimacion, false);
        } else if (!enSuelo) {
            // Animaciones de salto/caída
            if (velocidadY > 50) {
                // SALTO (subiendo) - usar animación de salto
                frameActual = animacionSaltar.getKeyFrame(tiempoAnimacion, false);
            } else if (velocidadY < -50) {
                // CAÍDA (bajando rápido) - podrías usar una animación específica
                // Por ahora usamos la misma de salto pero podrías tener una animación de caída
                frameActual = animacionSaltar.getKeyFrame(tiempoAnimacion, true);
            } else {
                // FLOTANDO (en el pico del salto) - mantener último frame de salto
                frameActual = animacionSaltar.getKeyFrame(animacionSaltar.getAnimationDuration() * 0.5f, false);
            }
        } else if (estaMoviendose && enSuelo) {
            frameActual = animacionCorrer.getKeyFrame(tiempoAnimacion, true);
        } else {
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    /**
     * Actualiza las físicas con manejo mejorado de colisiones en esquinas.
     * NUEVO: Sistema de detección y escape de esquinas.
     */
    private void actualizarFisicas(float delta) {
        float xAnterior = getX();
        float yAnterior = getY();

        // ===== 1. APLICAR GRAVEDAD MODERADA =====
        if (!enSuelo) { // Solo aplicar gravedad si no está en suelo
            velocidadY += GRAVEDAD * delta;
            if (velocidadY < VELOCIDAD_MAX_CAIDA) {
                velocidadY = VELOCIDAD_MAX_CAIDA;
            }
        }

        // ===== 2. MOVIMIENTO VERTICAL SUAVE =====
        float movimientoY = velocidadY * delta;

        if (movimientoY != 0) {
            setY(getY() + movimientoY);

            // Verificar colisión después de mover
            if (mapaManager != null && mapaManager.hayColision(getHitboxColision())) {
                // Revertir movimiento
                setY(yAnterior);

                if (movimientoY < 0) {
                    // Colisión con el suelo
                    enSuelo = true;
                    velocidadY = 0;
                    saltosRestantes = MAX_SALTOS;

                    // Ajuste sutil del suelo (menos agresivo)
                    ajusteSueloSutil();

                } else if (movimientoY > 0) {
                    // Colisión con techo
                    velocidadY = 0;
                }
            } else {
                // Si no hay colisión y estábamos cayendo, no está en suelo
                if (movimientoY < 0) {
                    enSuelo = false;
                }
            }
        }

        // ===== 3. VERIFICACIÓN DEL SUELO (solo si parece que está en suelo) =====
        if (enSuelo && velocidadY == 0) {
            // Verificar si realmente sigue en el suelo
            if (!verificarSueloDebajo()) {
                enSuelo = false;
                // Aplicar un poco de gravedad inmediatamente para caer suavemente
                velocidadY = -50f;
            }
        }

        // ===== 4. PROTECCIÓN CONTRA CAÍDAS =====
        if (getY() < -50) {
            setY(100);
            setX(100);
            velocidadY = 0;
            enSuelo = false;
            recibirDano();
        }
    }
    /**
     * Ajuste sutil para colocarse sobre el suelo (menos agresivo)
     */
    private void ajusteSueloSutil() {
        if (mapaManager == null) return;

        float incremento = AJUSTE_SUELO_PRECISION;
        int maxAjustes = 5; // Máximo de ajustes (antes era 100)

        // Solo hacer pequeños ajustes si es necesario
        for (int i = 0; i < maxAjustes; i++) {
            // Subir un poco
            setY(getY() + incremento);

            // Verificar si ya no hay colisión
            if (!mapaManager.hayColision(getHitboxColision())) {
                // Estamos justo sobre el suelo - perfecto
                return;
            }
        }

        // Si después de los ajustes sigue en colisión, volver a la posición anterior
        // y usar un método más simple
        for (int i = 0; i < maxAjustes * 2; i++) {
            setY(getY() - incremento);
            if (!mapaManager.hayColision(getHitboxColision())) {
                return;
            }
        }
    }

    /**
     * Verifica suavemente si hay suelo debajo
     */
    private boolean verificarSueloDebajo() {
        if (mapaManager == null) return false;

        float yOriginal = getY();
        boolean haySuelo = false;

        // Probar con una distancia pequeña
        setY(yOriginal - 2f);
        haySuelo = mapaManager.hayColision(getHitboxColision());

        // Restaurar posición
        setY(yOriginal);

        return haySuelo;
    }
    /*
    **
        * Método mejorado para ajustar la posición exacta sobre el suelo
 */
    private void ajustarPosicionExactaSuelo() {
        if (mapaManager == null) return;

        float incremento = 0.5f; // Incremento muy pequeño
        float maxBusqueda = 10f; // Máximo de píxeles a buscar

        // Guardar posición original
        float yOriginal = getY();

        // Buscar hacia arriba hasta encontrar el borde exacto del suelo
        for (float i = 0; i < maxBusqueda; i += incremento) {
            setY(yOriginal + i);
            if (!mapaManager.hayColision(getHitboxColision())) {
                // Encontramos la posición justo encima del suelo
                // Retrocedemos un poco para estar seguro
                setY(getY() - incremento);
                break;
            }
        }
    }

    /**
     * Método mejorado para verificar si está realmente en el suelo
     */
    private boolean estaRealmenteEnSuelo() {
        if (mapaManager == null) return false;

        // Pequeño desplazamiento hacia abajo (más pequeño para mayor precisión)
        float yOriginal = getY();
        setY(yOriginal - 1f);

        boolean haySuelo = mapaManager.hayColision(getHitboxColision());

        // Verificar también ligeramente a los lados para detectar bordes
        if (!haySuelo) {
            // Probar desplazamiento izquierdo
            float xOriginal = getX();
            setX(xOriginal - 5f);
            setY(yOriginal - 1f);
            haySuelo = mapaManager.hayColision(getHitboxColision());
            setX(xOriginal);

            if (!haySuelo) {
                // Probar desplazamiento derecho
                setX(xOriginal + 5f);
                setY(yOriginal - 1f);
                haySuelo = mapaManager.hayColision(getHitboxColision());
                setX(xOriginal);
            }
        }

        // Restaurar posición original
        setY(yOriginal);

        return haySuelo;
    }
    /**
     * Ajusta la posición del personaje para colocarlo justo sobre el suelo.
     */
    private void ajustarPosicionSuelo() {
        if (mapaManager == null) return;

        float incremento = 1f;
        float maxBusqueda = 20f; // Máximo de píxeles a buscar

        // Buscar hacia arriba hasta encontrar el suelo exacto
        for (float i = 0; i < maxBusqueda; i += incremento) {
            setY(getY() + incremento);
            if (!mapaManager.hayColision(getHitboxColision())) {
                // Encontramos la posición justo encima del suelo
                setY(getY() - incremento); // Retroceder un paso
                break;
            }
        }
    }



    /**
     * Verifica si el personaje está realmente en el suelo.
     */
    private void verificarEstadoSuelo() {
        if (mapaManager == null) {
            enSuelo = false;
            return;
        }

        // Pequeño desplazamiento hacia abajo para verificar suelo
        float yOriginal = getY();
        setY(yOriginal - 2f); // Pequeño desplazamiento de 2 píxeles

        boolean haySuelo = mapaManager.hayColision(getHitboxColision());

        setY(yOriginal); // Restaurar posición

        if (haySuelo) {
            enSuelo = true;
            saltosRestantes = MAX_SALTOS;
        } else {
            // Si no hay suelo justo debajo, verificar si hay suelo cerca
            // Esto previene que quede "flotando" en bordes
            enSuelo = false;
        }
    }

    /**
     * Verifica si el personaje está atascado en una esquina.
     * NUEVO: Detección inteligente de atascamiento.
     */
    private void verificarAtascamientoEnEsquina() {
        if (mapaManager == null) return;

        // Verificar si está pegado a una pared en X y también tiene colisión en Y
        boolean colisionDerecha = verificarColisionEnDireccion(5, 0);  // Derecha
        boolean colisionIzquierda = verificarColisionEnDireccion(-5, 0); // Izquierda
        boolean colisionArriba = verificarColisionEnDireccion(0, 5);   // Arriba
        boolean colisionAbajo = verificarColisionEnDireccion(0, -5);   // Abajo

        // Si está pegado a una pared y también al suelo/techo, podría estar en esquina
        if ((colisionDerecha || colisionIzquierda) && (colisionArriba || colisionAbajo)) {
            if (!intentandoEscaparEsquina) {
                Gdx.app.debug("Protagonista", "Posible atascamiento en esquina detectado");
                intentandoEscaparEsquina = true;
                tiempoAtascado = 0;
            }
        }
    }

    /**
     * Verifica colisión en una dirección específica.
     */
    private boolean verificarColisionEnDireccion(float deltaX, float deltaY) {
        if (mapaManager == null) return false;

        float xOriginal = getX();
        float yOriginal = getY();

        setX(xOriginal + deltaX);
        setY(yOriginal + deltaY);

        boolean hayColision = mapaManager.hayColision(getHitboxColision());

        setX(xOriginal);
        setY(yOriginal);

        return hayColision;
    }

    /**
     * Intenta escapar de una esquina atascada.
     * NUEVO: Sistema de escape automático.
     */
    private void escaparDeEsquina() {
        Gdx.app.log("Protagonista", "¡Intentando escapar de esquina atascada!");

        // Intentar diferentes direcciones de escape
        float[][] direccionesEscapa = {
            {10, 0},   // Derecha
            {-10, 0},  // Izquierda
            {0, 10},   // Arriba
            {0, -10},  // Abajo
            {10, 10},  // Diagonal derecha-arriba
            {-10, 10}, // Diagonal izquierda-arriba
        };

        for (float[] dir : direccionesEscapa) {
            float testX = getX() + dir[0];
            float testY = getY() + dir[1];

            setX(testX);
            setY(testY);

            if (!mapaManager.hayColision(getHitboxColision())) {
                Gdx.app.log("Protagonista", "¡Escapado de esquina en dirección " + dir[0] + "," + dir[1] + "!");
                ultimaPosicionValidaX = getX();
                ultimaPosicionValidaY = getY();
                intentandoEscaparEsquina = false;
                tiempoAtascado = 0;
                return;
            }
        }

        // Si no pudo escapar, volver a última posición válida
        Gdx.app.log("Protagonista", "No se pudo escapar, volviendo a posición válida");
        setX(ultimaPosicionValidaX);
        setY(ultimaPosicionValidaY);
        intentandoEscaparEsquina = false;
        tiempoAtascado = 0;
        saltar(); // Dar un pequeño impulso
    }

    private void actualizarAtaques(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;
            if (tiempoAtaque >= DURACION_ATAQUE) {
                estaAtacando = false;
                tiempoAtaque = 0;
            }
        }

        if (estaAtacandoEspecial) {
            tiempoAtaqueEspecial += delta;
            if (tiempoAtaqueEspecial >= DURACION_ATAQUE_ESPECIAL) {
                estaAtacandoEspecial = false;
                tiempoAtaqueEspecial = 0;
            }
        }
    }

    private void actualizarInvencibilidad(float delta) {
        if (invencible) {
            tiempoInvencibilidad += delta;
            if (tiempoInvencibilidad >= DURACION_INVENCIBILIDAD) {
                invencible = false;
            }
        }
    }

    // ====================== SISTEMA DE VIDAS Y DAÑO ======================

    public void recibirDano() {
        if (invencible || estaDefendiendo) {
            return;
        }

        vidasActuales--;
        invencible = true;
        tiempoInvencibilidad = 0;

        Gdx.app.log("Protagonista", "¡Daño recibido! Vidas restantes: " + vidasActuales);

        if (vidasActuales <= 0) {
            morir();
        }
    }

    private void morir() {
        Gdx.app.log("Protagonista", "¡Has muerto! Se debe reiniciar el nivel.");
    }

    public void restaurarVidas() {
        vidasActuales = vidasMaximas;
        invencible = false;
    }

    public void agregarVida() {
        if (vidasActuales < vidasMaximas) {
            vidasActuales++;
        }
    }

    // ====================== MOVIMIENTO MEJORADO ======================

    /**
     * Sistema de movimiento mejorado para evitar atascamientos.
     */
    public void mover(float delta, float direccionX, float direccionY) {
        if (estaDefendiendo) {
            return; // No moverse si está defendiendo
        }

        boolean hayInputEsteFrame = (direccionX != 0);

        if (hayInputEsteFrame) {
            estaMoviendose = true;
            velocidadXActual = direccionX * VELOCIDAD;

            if (direccionX > 0 && !mirandoDerecha) {
                mirandoDerecha = true;
            } else if (direccionX < 0 && mirandoDerecha) {
                mirandoDerecha = false;
            }
        } else {
            estaMoviendose = false;
            velocidadXActual = 0;
            if (huboInputUltimoFrame) {
                tiempoAnimacion = 0;
            }
        }

        huboInputUltimoFrame = hayInputEsteFrame;

        // MOVIMIENTO HORIZONTAL SIMPLE
        if (direccionX != 0 && enSuelo) { // SOLO mover horizontalmente si está en suelo
            float xAnterior = getX();
            float movX = direccionX * VELOCIDAD * delta;
            setX(getX() + movX);

            if (mapaManager != null && mapaManager.hayColision(getHitboxColision())) {
                // Colisión horizontal - revertir
                setX(xAnterior);
                velocidadXActual = 0;
            }
        } else if (direccionX != 0 && !enSuelo) {
            // Movimiento en el aire (más lento)
            float xAnterior = getX();
            float movX = direccionX * VELOCIDAD * delta * 0.7f; // 70% de velocidad en aire
            setX(getX() + movX);

            if (mapaManager != null && mapaManager.hayColision(getHitboxColision())) {
                setX(xAnterior);
            }
        }
    }

    /**
     * Intenta un pequeño ajuste vertical para superar bordes.
     */
    private boolean intentarAjusteVertical() {
        if (mapaManager == null) return false;

        float yOriginal = getY();
        boolean pudoMoverse = false;

        // Probar subiendo poco a poco
        for (int i = 1; i <= 3; i++) {
            setY(yOriginal + i);
            if (!mapaManager.hayColision(getHitboxColision())) {
                pudoMoverse = true;
                Gdx.app.debug("Protagonista", "Ajuste vertical exitoso: +" + i + "px");
                break;
            }
            setY(yOriginal); // Volver a original para siguiente prueba
        }

        // Si no pudo subiendo, probar bajando (solo si no está en el aire)
        if (!pudoMoverse && enSuelo) {
            for (int i = 1; i <= 3; i++) {
                setY(yOriginal - i);
                if (!mapaManager.hayColision(getHitboxColision())) {
                    pudoMoverse = true;
                    Gdx.app.debug("Protagonista", "Ajuste vertical exitoso: -" + i + "px");
                    break;
                }
                setY(yOriginal);
            }
        }

        if (!pudoMoverse) {
            setY(yOriginal); // Restaurar posición original
        }

        return pudoMoverse;
    }

    public void saltar() {
        if ((enSuelo || saltosRestantes > 0) && !estaDefendiendo) {
            velocidadY = FUERZA_SALTO;
            enSuelo = false;
            if (!enSuelo) {
                saltosRestantes--;
            }
            tiempoAnimacion = 0;

            // Pequeño impulso horizontal si se estaba moviendo
            if (estaMoviendose) {
                velocidadXActual *= 1.2f; // Aumenta un 20% el impulso horizontal
            }
        }
    }

    public void saltarEspecial() {
        if ((enSuelo || saltosRestantes > 0) && !estaDefendiendo && !intentandoEscaparEsquina) {
            velocidadY = FUERZA_SALTO_ESPECIAL;
            enSuelo = false;
            saltosRestantes--;
            tiempoAnimacion = 0;
        }
    }

    public void atacar() {
        if (!estaAtacando && !estaAtacandoEspecial && !estaDefendiendo && !intentandoEscaparEsquina) {
            estaAtacando = true;
            tiempoAtaque = 0;
            tiempoAnimacion = 0;
        }
    }

    public void atacarEspecial() {
        if (!estaAtacando && !estaAtacandoEspecial && !estaDefendiendo && !intentandoEscaparEsquina) {
            estaAtacandoEspecial = true;
            tiempoAtaqueEspecial = 0;
            tiempoAnimacion = 0;
        }
    }

    public void defender(boolean defender) {
        if (estaAtacando || estaAtacandoEspecial) {
            return;
        }
        estaDefendiendo = defender;
    }

    // ====================== HITBOXES ======================

    public Rectangle getHitboxColision() {
        return new Rectangle(
            getX() + OFFSET_COLISION_X,
            getY() + OFFSET_COLISION_Y,
            ANCHO_COLISION,
            ALTO_COLISION
        );
    }

    public Rectangle getHitboxCombate() {
        return new Rectangle(
            getX() + OFFSET_COMBATE_X,
            getY() + OFFSET_COMBATE_Y,
            ANCHO_COMBATE,
            ALTO_COMBATE
        );
    }

    @Deprecated
    public Rectangle getHitbox() {
        return getHitboxColision();
    }

    public Rectangle getAreaAtaque() {
        float anchoAtaque = estaAtacandoEspecial ? rangoAtaqueEspecial : rangoAtaque;
        float altoAtaque = 40f;
        float offsetX = mirandoDerecha ? TAMANIO_BASE/2 : -anchoAtaque;

        return new Rectangle(
            getX() + offsetX,
            getY() + 10f,
            anchoAtaque,
            altoAtaque
        );
    }

    // ====================== RENDERIZADO ======================

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (frameActual == null) return;

        float drawX = getX();
        float drawY = getY();

        if (invencible) {
            float alpha = (float) Math.sin(tiempoInvencibilidad * 20) * 0.5f + 0.5f;
            batch.setColor(1, 1, 1, alpha);
        }

        if (estaAtacando || estaAtacandoEspecial) {
            float ancho = 120f;
            float alto = 80f;
            float offsetX = (TAMANIO_BASE - ancho) / 2f;

            if (mirandoDerecha) {
                batch.draw(frameActual, drawX + offsetX, drawY, ancho, alto);
            } else {
                batch.draw(frameActual, drawX + offsetX + ancho, drawY, -ancho, alto);
            }

        } else if (!enSuelo) {
            float ancho = 120f;
            float alto = 40f;
            float offsetX = (TAMANIO_BASE - ancho) / 2f;

            if (mirandoDerecha) {
                batch.draw(frameActual, drawX + offsetX, drawY, ancho, alto);
            } else {
                batch.draw(frameActual, drawX + offsetX + ancho, drawY, -ancho, alto);
            }

        } else {
            if (mirandoDerecha) {
                batch.draw(frameActual, drawX, drawY, TAMANIO_BASE, TAMANIO_BASE);
            } else {
                batch.draw(frameActual, drawX + TAMANIO_BASE, drawY, -TAMANIO_BASE, TAMANIO_BASE);
            }
        }

        if (invencible) {
            batch.setColor(1, 1, 1, 1);
        }

        if (mostrarDebugHitbox) {
            dibujarHitboxesDebug(batch);
        }
    }

    private void dibujarHitboxesDebug(Batch batch) {
        Rectangle hitboxColision = getHitboxColision();
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1,
            com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

        pixmap.setColor(0, 1, 0, 0.3f);
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture pixelVerde = new com.badlogic.gdx.graphics.Texture(pixmap);
        batch.setColor(0, 1, 0, 0.3f);
        batch.draw(pixelVerde, hitboxColision.x, hitboxColision.y, hitboxColision.width, hitboxColision.height);

        Rectangle hitboxCombate = getHitboxCombate();
        pixmap.setColor(1, 0, 0, 0.5f);
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture pixelRojo = new com.badlogic.gdx.graphics.Texture(pixmap);
        batch.setColor(1, 0, 0, 0.5f);
        batch.draw(pixelRojo, hitboxCombate.x, hitboxCombate.y, hitboxCombate.width, hitboxCombate.height);

        batch.setColor(1, 1, 1, 1);
        pixelVerde.dispose();
        pixelRojo.dispose();
        pixmap.dispose();
    }

    // ====================== GETTERS Y SETTERS ======================

    public void setMapaManager(MapaManager mapaManager) {
        this.mapaManager = mapaManager;
    }

    public int getVidasActuales() {
        return vidasActuales;
    }

    public int getVidasMaximas() {
        return vidasMaximas;
    }

    public boolean estaInvencible() {
        return invencible;
    }

    public boolean estaVivo() {
        return vidasActuales > 0;
    }

    public boolean estaMirandoDerecha() {
        return mirandoDerecha;
    }

    public boolean estaAtacando() {
        return estaAtacando || estaAtacandoEspecial;
    }

    public boolean estaAtacandoBasico() {
        return estaAtacando;
    }

    public boolean estaAtacandoEspecial() {
        return estaAtacandoEspecial;
    }

    public boolean estaDefendiendo() {
        return estaDefendiendo;
    }

    public boolean estaEnSuelo() {
        return enSuelo;
    }

    public boolean estaMoviendose() {
        return estaMoviendose;
    }

    public float getVelocityX() {
        return Math.abs(velocidadXActual);
    }

    public int getDanoAtaque() {
        return estaAtacandoEspecial ? danoAtaqueEspecial : danoAtaqueBasico;
    }

    public float getVelocityY() {
        return velocidadY;
    }

    public int getSaltosRestantes() {
        return saltosRestantes;
    }

    @Override
    public float getWidth() {
        return TAMANIO_BASE;
    }

    @Override
    public float getHeight() {
        return TAMANIO_BASE;
    }

    public void setMostrarDebugHitbox(boolean mostrar) {
        this.mostrarDebugHitbox = mostrar;
    }

    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
        }
    }
}
