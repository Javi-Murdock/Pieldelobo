package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
 * Clase base abstracta para todos los enemigos.
 * Ahora incluye gravedad para que no floten.
 *
 *  * @author Javier Gala
 *  * @version 2.0
 */
public abstract class Enemigos extends Actor {
    // Propiedades comunes
    protected TextureAtlas atlas;
    protected Animation<TextureRegion> animacionIdle;
    protected Animation<TextureRegion> animacionCaminar;
    protected Animation<TextureRegion> animacionAtacar;
    protected Animation<TextureRegion> animacionMuerte;
    protected Animation<TextureRegion> animacionDano;
    protected TextureRegion frameActual;
    protected float tiempoAnimacion = 0;

    // Vida
    protected int vidaMaxima;
    protected int vidaActual;

    // Estados
    protected boolean estaAtacando = false;
    protected boolean mirandoDerecha = true;
    protected boolean estaMuerto = false;
    protected boolean recibiendoDano = false;
    protected float tiempoAtaque = 0;
    protected float tiempoDano = 0;
    protected float tiempoMuerte = 0;

    // Movimiento
    protected float velocidad;
    protected Vector2 direccionMovimiento = new Vector2(1, 0);
    protected float distanciaRecorrida = 0;
    protected float distanciaMaxima;

    // Ataque
    protected boolean puedeAtacar = true;
    protected float cooldownAtaque = 0;
    protected float cooldownAtaqueMaximo;
    protected int danoAtaque;
    protected float rangoAtaque;
    protected float duracionAtaque;

    // Referencias
    protected MapaManager mapaManager;
    protected Protagonista objetivo;

    // Hitbox
    protected float tamanioBase;
    protected float offsetHitboxX;
    protected float offsetHitboxY;
    protected float anchoHitbox;
    protected float altoHitbox;

    // ====================== GRAVEDAD ======================
    public boolean enSuelo = false;
    public float velocidadY = 0;
    protected static final float GRAVEDAD = -800f;
    public boolean aplicarGravedad = true; // Por defecto todos tienen gravedad
    protected int saltosRestantes = 1;

    // Textura para debug
    protected boolean mostrarDebug = false;

    // Flag para saber si las animaciones están inicializadas
    protected boolean animacionesInicializadas = false;

    public Enemigos(float x, float y) {
        setPosition(x, y);

        // INICIALIZAR ANIMACIONES POR DEFECTO INMEDIATAMENTE
        inicializarAnimacionesPorDefecto();

        // Todos los enemigos tienen gravedad por defecto
        this.aplicarGravedad = true;
    }

    /**
     * Inicializa animaciones básicas por defecto para evitar NullPointerException.
     */
    private void inicializarAnimacionesPorDefecto() {
        try {
            // Crear un frame de color sólido para animaciones por defecto
            Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 0, 0, 1); // Rojo (color de error/placeholder)
            pixmap.fill();
            Texture defaultTexture = new Texture(pixmap);
            TextureRegion defaultFrame = new TextureRegion(defaultTexture);
            pixmap.dispose();

            // Crear arrays de frames por defecto
            Array<TextureRegion> defaultFrames = new Array<>();
            defaultFrames.add(defaultFrame);

            // Inicializar TODAS las animaciones con frames por defecto
            animacionIdle = new Animation<TextureRegion>(0.2f, defaultFrames);
            animacionCaminar = new Animation<TextureRegion>(0.15f, defaultFrames);
            animacionAtacar = new Animation<TextureRegion>(0.1f, defaultFrames);
            animacionDano = new Animation<TextureRegion>(0.1f, defaultFrames);
            animacionMuerte = new Animation<TextureRegion>(0.2f, defaultFrames);

            // Establecer frame actual
            frameActual = defaultFrame;
            animacionesInicializadas = true;

            Gdx.app.log(getClass().getSimpleName(), "Animaciones por defecto inicializadas");

        } catch (Exception e) {
            Gdx.app.error(getClass().getSimpleName(),
                "Error crítico al inicializar animaciones por defecto", e);
            // En caso de error extremo, al menos asegurar que frameActual no sea null
            frameActual = new TextureRegion();
        }
    }

    /**
     * Carga las animaciones con manejo de errores.
     * Las clases hijas deben llamar a este método en sus constructores.
     */
    public void cargarAnimacionesConSeguridad() {
        try {
            cargarAnimaciones();

            // Verificar que todas las animaciones necesarias estén inicializadas
            if (animacionIdle == null) {
                Gdx.app.log(getClass().getSimpleName(), "Animación Idle no cargada, manteniendo por defecto");
            }

            if (animacionCaminar == null) {
                Gdx.app.log(getClass().getSimpleName(), "Animación Caminar no cargada, manteniendo por defecto");
            }

            if (animacionAtacar == null) {
                Gdx.app.log(getClass().getSimpleName(), "Animación Atacar no cargada, manteniendo por defecto");
            }

            if (animacionDano == null) {
                Gdx.app.log(getClass().getSimpleName(), "Animación Daño no cargada, manteniendo por defecto");
            }

            if (animacionMuerte == null) {
                Gdx.app.log(getClass().getSimpleName(),
                    "ADVERTENCIA: Animación Muerte no cargada. El enemigo no mostrará animación de muerte.");
            }

            // Asegurar que frameActual no sea null
            if (frameActual == null && animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            } else if (frameActual == null) {
                // Último recurso
                Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
                pixmap.setColor(1, 1, 0, 1); // Amarillo
                pixmap.fill();
                frameActual = new TextureRegion(new Texture(pixmap));
                pixmap.dispose();
            }

            Gdx.app.log(getClass().getSimpleName(), "Animaciones cargadas con seguridad");

        } catch (Exception e) {
            Gdx.app.error(getClass().getSimpleName(),
                "Error en cargarAnimacionesConSeguridad", e);
            // Las animaciones por defecto ya están inicializadas, así que continuamos
        }
    }

    protected abstract void cargarAnimaciones();

    @Override
    public void act(float delta) {
        super.act(delta);

        if (estaMuerto) {
            actualizarMuerte(delta);
            return;
        }

        // Actualizar físicas (gravedad) primero
        if (aplicarGravedad) {
            actualizarFisicas(delta);
        }

        if (recibiendoDano) {
            actualizarDano(delta);
        }

        if (!recibiendoDano && !estaAtacando) {
            actualizarIA(delta);
        }

        actualizarAnimacion(delta);
        actualizarAtaque(delta);
        actualizarCooldown(delta);
    }

    /**
     * Actualiza las físicas del enemigo (gravedad).
     */
    protected void actualizarFisicas(float delta) {
        if (estaMuerto || !aplicarGravedad) {
            return;
        }

        float yAnterior = getY();

        // Aplicar gravedad si no está en el suelo
        if (!enSuelo) {
            velocidadY += GRAVEDAD * delta;
            setY(getY() + velocidadY * delta);
        }

        // Verificar colisiones verticales con el mapa
        if (mapaManager != null) {
            Rectangle hitboxActual = getHitbox();

            if (mapaManager.hayColision(hitboxActual)) {
                // Colisión detectada - revertir movimiento vertical
                setY(yAnterior);

                if (velocidadY < 0) {
                    // Tocando suelo
                    enSuelo = true;
                    velocidadY = 0;
                    saltosRestantes = 1;
                } else if (velocidadY > 0) {
                    // Golpeó techo
                    velocidadY = 0;
                }
            } else {
                // No hay colisión - sigue en el aire
                if (velocidadY < 0) {
                    enSuelo = false;
                }
            }
        }
    }

    protected void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        // VERIFICACIONES DE NULL ANTES DE USAR CUALQUIER ANIMACIÓN
        if (!animacionesInicializadas) {
            return; // No podemos actualizar animaciones
        }

        // Asegurar que tenemos animación idle (siempre debería existir)
        if (animacionIdle == null) {
            return;
        }

        if (estaMuerto) {
            // Animación de muerte tiene prioridad
            if (animacionMuerte != null) {
                frameActual = animacionMuerte.getKeyFrame(tiempoMuerte, false);
            } else {
                // Si no hay animación de muerte, usar idle con efecto de desvanecimiento
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (recibiendoDano) {
            if (animacionDano != null) {
                frameActual = animacionDano.getKeyFrame(tiempoDano, false);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (estaAtacando) {
            if (animacionAtacar != null) {
                frameActual = animacionAtacar.getKeyFrame(tiempoAnimacion, false);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (Math.abs(direccionMovimiento.x) > 0.1f) {
            if (animacionCaminar != null) {
                frameActual = animacionCaminar.getKeyFrame(tiempoAnimacion, true);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else {
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }

        // Asegurar que frameActual no sea null (último recurso)
        if (frameActual == null) {
            frameActual = animacionIdle.getKeyFrame(0);
        }
    }

    /**
     * Actualiza el estado de muerte del enemigo.
     */
    protected void actualizarMuerte(float delta) {
        tiempoMuerte += delta;

        // Si tenemos animación de muerte, usarla
        if (animacionMuerte != null) {
            frameActual = animacionMuerte.getKeyFrame(tiempoMuerte, false);

            // Verificar si la animación ha terminado
            if (animacionMuerte.isAnimationFinished(tiempoMuerte)) {
                // Esperar un poco extra antes de eliminar para que se vea el último frame
                if (tiempoMuerte > animacionMuerte.getAnimationDuration() + 0.5f) {
                    eliminar();
                }
            }
        } else {
            // Si no hay animación de muerte, usar animación por defecto con parpadeo
            if (animacionesInicializadas && animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            }

            // Eliminar después de un tiempo fijo
            if (tiempoMuerte > 2.0f) { // Dar más tiempo para que se vea
                eliminar();
            }
        }
    }

    protected void actualizarIA(float delta) {
        if (objetivo == null || !objetivo.estaVivo()) {
            patrullar(delta);
            return;
        }

        float distancia = calcularDistanciaAlObjetivo();

        if (distancia <= rangoAtaque) {
            direccionMovimiento.set(0, 0);
            mirarAlObjetivo();

            if (puedeAtacar && !estaAtacando) {
                atacar();
            }
        } else {
            seguirObjetivo(delta);
        }
    }

    protected float calcularDistanciaAlObjetivo() {
        if (objetivo == null) return Float.MAX_VALUE;

        return Vector2.dst(
            getX() + getWidth()/2, getY() + getHeight()/2,
            objetivo.getX() + objetivo.getWidth()/2,
            objetivo.getY() + objetivo.getHeight()/2
        );
    }

    protected void mirarAlObjetivo() {
        if (objetivo != null) {
            mirandoDerecha = (objetivo.getX() > getX());
        }
    }

    protected void patrullar(float delta) {
        distanciaRecorrida += Math.abs(direccionMovimiento.x) * velocidad * delta;

        if (distanciaRecorrida >= distanciaMaxima) {
            direccionMovimiento.x *= -1;
            mirandoDerecha = (direccionMovimiento.x > 0);
            distanciaRecorrida = 0;
        }

        moverHorizontalmente(direccionMovimiento.x * velocidad * delta);
    }

    protected void seguirObjetivo(float delta) {
        if (objetivo == null) return;

        float direccionX = Math.signum(objetivo.getX() - getX());
        direccionMovimiento.x = direccionX;
        mirandoDerecha = (direccionX > 0);

        moverHorizontalmente(direccionX * velocidad * delta);
    }

    protected void moverHorizontalmente(float deltaX) {
        if (estaMuerto || recibiendoDano) return;

        float xAnterior = getX();
        setX(getX() + deltaX);

        if (mapaManager != null && mapaManager.hayColision(getHitbox())) {
            setX(xAnterior);
            direccionMovimiento.x *= -1;
            mirandoDerecha = (direccionMovimiento.x > 0);
        }

        // Verificar si hay suelo delante al moverse
        if (aplicarGravedad && enSuelo) {
            verificarBorde();
        }
    }

    /**
     * Verifica si hay un borde delante para evitar caídas.
     */
    protected void verificarBorde() {
        if (mapaManager == null || estaMuerto) return;

        // Crear un hitbox de prueba un poco delante y abajo
        Rectangle testHitbox = new Rectangle(
            getX() + offsetHitboxX + (mirandoDerecha ? anchoHitbox : -10),
            getY() + offsetHitboxY - 20,
            10,
            20
        );

        // Si no hay suelo delante, cambiar dirección
        if (!mapaManager.hayColision(testHitbox)) {
            direccionMovimiento.x *= -1;
            mirandoDerecha = (direccionMovimiento.x > 0);
        }
    }

    /**
     * Salto básico para enemigos.
     */
    protected void saltar() {
        if (saltosRestantes > 0 && enSuelo && aplicarGravedad) {
            velocidadY = 300f; // Fuerza de salto
            enSuelo = false;
            saltosRestantes--;
        }
    }

    public void atacar() {
        if (puedeAtacar && !estaAtacando && !estaMuerto && !recibiendoDano) {
            estaAtacando = true;
            tiempoAtaque = 0;
            puedeAtacar = false;

            Gdx.app.log(getClass().getSimpleName(), "¡Atacando!");
        }
    }

    protected void actualizarAtaque(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;

            // Verificar si golpea al objetivo a la mitad del ataque
            if (tiempoAtaque >= duracionAtaque / 2 && tiempoAtaque - delta < duracionAtaque / 2) {
                verificarGolpe();
            }

            if (tiempoAtaque >= duracionAtaque) {
                estaAtacando = false;
                tiempoAtaque = 0;
            }
        }
    }

    protected void verificarGolpe() {
        if (objetivo != null && estaEnRangoAtaque() && !objetivo.estaInvencible()) {
            objetivo.recibirDano();
            Gdx.app.log(getClass().getSimpleName(), "¡Golpe al protagonista!");
        }
    }

    protected boolean estaEnRangoAtaque() {
        if (objetivo == null) return false;
        return calcularDistanciaAlObjetivo() <= rangoAtaque;
    }

    protected void actualizarCooldown(float delta) {
        if (!puedeAtacar) {
            cooldownAtaque += delta;
            if (cooldownAtaque >= cooldownAtaqueMaximo) {
                puedeAtacar = true;
                cooldownAtaque = 0;
            }
        }
    }

    public void recibirDano() {
        if (estaMuerto || recibiendoDano) return;

        vidaActual--;
        recibiendoDano = true;
        tiempoDano = 0;

        Gdx.app.log(getClass().getSimpleName(), "Recibió daño. Vida: " + vidaActual + "/" + vidaMaxima);

        if (vidaActual <= 0) {
            morir();
        }
    }

    protected void actualizarDano(float delta) {
        tiempoDano += delta;
        if (tiempoDano >= 0.3f) { // 0.3 segundos de animación de daño
            recibiendoDano = false;
        }
    }

    /**
     * Método mejorado para manejar la muerte del enemigo.
     */
    protected void morir() {
        estaMuerto = true;
        tiempoMuerte = 0;

        // Detener cualquier movimiento o ataque
        direccionMovimiento.set(0, 0);
        estaAtacando = false;
        recibiendoDano = false;
        puedeAtacar = false;
        velocidadY = 0;

        // Forzar el primer frame de la animación de muerte si existe
        if (animacionMuerte != null) {
            frameActual = animacionMuerte.getKeyFrame(0);
            Gdx.app.log(getClass().getSimpleName(),
                "Mostrando animación de muerte");
        } else {
            Gdx.app.log(getClass().getSimpleName(),
                "No hay animación de muerte específica. Usando animación por defecto.");
        }

        Gdx.app.log(getClass().getSimpleName(), "¡Eliminado! (Tiempo muerte iniciado)");
    }

    protected void eliminar() {
        // Solo eliminar si aún está en el stage
        if (getStage() != null) {
            remove();
            Gdx.app.log(getClass().getSimpleName(), "Removido del stage");
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (frameActual == null) {
            return;
        }

        // Si está muerto y la animación de muerte terminó, no dibujar
        if (estaMuerto && animacionMuerte != null &&
            animacionMuerte.isAnimationFinished(tiempoMuerte)) {
            return;
        }

        float alpha = 1.0f;

        // Efectos visuales según estado
        if (recibiendoDano) {
            alpha = 0.5f; // Parpadeo al recibir daño
        } else if (estaMuerto) {
            // Desvanecimiento gradual durante la muerte
            alpha = 1.0f - (tiempoMuerte / 2.0f);
            if (alpha < 0.1f) alpha = 0.1f;
        }

        batch.setColor(1, 1, 1, alpha);

        if (mirandoDerecha) {
            batch.draw(frameActual, getX(), getY(), tamanioBase, tamanioBase);
        } else {
            batch.draw(frameActual,
                getX() + tamanioBase, getY(),
                -tamanioBase, tamanioBase);
        }

        batch.setColor(1, 1, 1, 1);
    }

    public Rectangle getHitbox() {
        return new Rectangle(
            getX() + offsetHitboxX,
            getY() + offsetHitboxY,
            anchoHitbox,
            altoHitbox
        );
    }

    // Getters y Setters
    public void setMapaManager(MapaManager mapaManager) {
        this.mapaManager = mapaManager;
    }

    public void setObjetivo(Protagonista objetivo) {
        this.objetivo = objetivo;
    }

    public boolean estaVivo() {
        return !estaMuerto && vidaActual > 0;
    }

    public boolean estaAtacando() {
        return estaAtacando;
    }

    public void setMostrarDebug(boolean mostrar) {
        this.mostrarDebug = mostrar;
    }

    @Override
    public float getWidth() {
        return tamanioBase;
    }

    @Override
    public float getHeight() {
        return tamanioBase;
    }

    /**
     * Establece si el enemigo debe aplicar gravedad.
     */
    public void setAplicarGravedad(boolean aplicarGravedad) {
        this.aplicarGravedad = aplicarGravedad;
        this.enSuelo = !aplicarGravedad; // Si no tiene gravedad, considerarlo "en suelo"
    }

    /**
     * Corrige la posición inicial para que esté sobre el suelo.
     */
    public void corregirPosicionInicial() {
        if (!aplicarGravedad || mapaManager == null) return;

        // Bajar hasta encontrar suelo
        for (int i = 0; i < 200; i++) {
            setY(getY() - 1);
            if (mapaManager.hayColision(getHitbox())) {
                setY(getY() + 1); // Subir un píxel para no estar dentro del suelo
                enSuelo = true;
                velocidadY = 0;
                break;
            }
        }
    }

    public void dispose() {
        if (atlas != null) atlas.dispose();
    }
}
