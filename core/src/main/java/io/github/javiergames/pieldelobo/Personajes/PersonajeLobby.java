package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Personaje para pantallas de menú/lobby con movimiento estilo top-down.
 * Controlado por la clase Procesador al igual que el Protagonista.
 * Incluye animaciones para todas las direcciones.
 *
 *  * @author Javier Gala
 *  * @version 4.1
 */
public class PersonajeLobby extends Actor {
    // ====================== RECURSOS GRÁFICOS ======================

    private TextureAtlas atlas;
    private Animation<TextureRegion> animacionAndar;
    private Animation<TextureRegion> animacionIdle;
    private Animation<TextureRegion> animacionAndarArriba;
    private Animation<TextureRegion> animacionAndarAbajo;
    private Animation<TextureRegion> animacionAndarIzquierda;
    private Animation<TextureRegion> animacionAndarDerecha;

    // Animaciones idle para cada dirección
    private Animation<TextureRegion> animacionIdleArriba;
    private Animation<TextureRegion> animacionIdleAbajo;
    private Animation<TextureRegion> animacionIdleIzquierda;
    private Animation<TextureRegion> animacionIdleDerecha;

    private TextureRegion frameActual;
    private float tiempoAnimacion = 0;

    // ====================== PROPIEDADES DE MOVIMIENTO ======================

    /**
     * Velocidad base del personaje en píxeles por segundo
     */
    private static final float VELOCIDAD = 120f;

    /**
     * Velocidades actuales en cada eje
     */
    private float velocidadX = 0;
    private float velocidadY = 0;

    // ====================== DIRECCIONES Y ESTADOS ======================

    /**
     * Enum que define todas las posibles direcciones del personaje
     * Incluye direcciones cardinales y diagonales para movimiento octogonal
     */
    public enum Direccion {
        ARRIBA, ABAJO, IZQUIERDA, DERECHA,
        ARRIBA_IZQUIERDA, ARRIBA_DERECHA,
        ABAJO_IZQUIERDA, ABAJO_DERECHA
    }

    /**
     * Dirección actual del personaje
     */
    private Direccion direccionActual = Direccion.ABAJO;

    /**
     * Indica si el personaje se está moviendo
     */
    private boolean estaAndando = false;

    /**
     * Indica si hubo input en el último frame (para detectar cuando se sueltan las teclas)
     */
    private boolean huboInputUltimoFrame = false;

    // ====================== CONFIGURACIÓN HITBOX ======================

    /**
     * Márgenes para el hitbox (ajustados para ser más precisos)
     */
    private static final float MARGEN_HITBOX_HORIZONTAL = 18f;
    private static final float MARGEN_HITBOX_VERTICAL = 14f;

    /**
     * Flag para debug del hitbox
     */
    private static final boolean DEBUG_HITBOX = false;

    // ====================== REFERENCIAS ======================

    /**
     * Manager del mapa para detección de colisiones
     */
    private MapaManager mapaManager;

    // ====================== CONSTRUCTOR ======================

    /**
     * Constructor principal del PersonajeLobby
     * Carga las animaciones y establece posición y tamaño inicial
     */
    public PersonajeLobby() {
        cargarAnimaciones();
        setPosition(100, 100);
        setSizeInicial();
        Gdx.app.log("PersonajeLobby", "Personaje creado correctamente. Tamaño: " + getWidth() + "x" + getHeight());
    }

    // ====================== CARGA DE RECURSOS ======================

    /**
     * Carga todas las animaciones desde el atlas de texturas
     */
    private void cargarAnimaciones() {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("Personajes_Laboratorio.atlas"));

            // Animación idle ARRIBA
            Array<TextureRegion> idleArriba = new Array<>();
            idleArriba.add(atlas.findRegion("Prota_laboratorio001")); // Sprite específico para idle arriba
            animacionIdleArriba = new Animation<>(0.3f, idleArriba);

            // Animación idle ABAJO
            Array<TextureRegion> idleAbajo = new Array<>();
            idleAbajo.add(atlas.findRegion("Prota_laboratorio007")); // Sprite específico para idle abajo
            animacionIdleAbajo = new Animation<>(0.3f, idleAbajo);

            // Animación idle IZQUIERDA
            Array<TextureRegion> idleIzquierda = new Array<>();
            idleIzquierda.add(atlas.findRegion("Prota_laboratorio010")); // Sprite específico para idle izquierda
            animacionIdleIzquierda = new Animation<>(0.3f, idleIzquierda);

            // Animación idle DERECHA
            Array<TextureRegion> idleDerecha = new Array<>();
            idleDerecha.add(atlas.findRegion("Prota_laboratorio004")); // Sprite específico para idle derecha
            animacionIdleDerecha = new Animation<>(0.3f, idleDerecha);

            // Animación por defecto (usaremos idle abajo)
            animacionIdle = animacionIdleAbajo;

            // Animación de andar - ARRIBA
            Array<TextureRegion> andarArriba = new Array<>();
            andarArriba.add(atlas.findRegion("Prota_laboratorio000"));
            andarArriba.add(atlas.findRegion("Prota_laboratorio001"));
            andarArriba.add(atlas.findRegion("Prota_laboratorio002"));
            animacionAndarArriba = new Animation<>(0.2f, andarArriba);

            // Animación de andar - ABAJO
            Array<TextureRegion> andarAbajo = new Array<>();
            andarAbajo.add(atlas.findRegion("Prota_laboratorio006"));
            andarAbajo.add(atlas.findRegion("Prota_laboratorio007"));
            andarAbajo.add(atlas.findRegion("Prota_laboratorio008"));
            animacionAndarAbajo = new Animation<>(0.15f, andarAbajo);

            // Animación de andar - IZQUIERDA
            Array<TextureRegion> andarIzquierda = new Array<>();
            andarIzquierda.add(atlas.findRegion("Prota_laboratorio009"));
            andarIzquierda.add(atlas.findRegion("Prota_laboratorio010"));
            andarIzquierda.add(atlas.findRegion("Prota_laboratorio011"));
            animacionAndarIzquierda = new Animation<>(0.15f, andarIzquierda);

            // Animación de andar - DERECHA
            Array<TextureRegion> andarDerecha = new Array<>();
            andarDerecha.add(atlas.findRegion("Prota_laboratorio003"));
            andarDerecha.add(atlas.findRegion("Prota_laboratorio004"));
            andarDerecha.add(atlas.findRegion("Prota_laboratorio005"));
            animacionAndarDerecha = new Animation<>(0.15f, andarDerecha);

            // Animación de andar por defecto (abajo)
            animacionAndar = animacionAndarAbajo;

            // Frame inicial
            frameActual = animacionIdleAbajo.getKeyFrame(0);

            Gdx.app.log("PersonajeLobby", "Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("PersonajeLobby", "Error al cargar animaciones", e);
            // En caso de error, crear una textura de respaldo
            frameActual = new TextureRegion();
        }
    }

    // ====================== MÉTODOS PRINCIPALES ======================

    /**
     * Método principal de actualización llamado cada frame
     * @param delta Tiempo transcurrido desde el último frame
     */
    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar el estado de movimiento incluso cuando no hay input
        actualizarEstadoMovimiento();

        // Actualizar la animación
        actualizarAnimacion(delta);
    }

    /**
     * Actualiza el estado de movimiento del personaje
     */
    private void actualizarEstadoMovimiento() {
        // Si no hay velocidad, el personaje no está andando
        if (velocidadX == 0 && velocidadY == 0) {
            if (estaAndando) {
                // Acabamos de dejar de movernos, actualizar a idle
                estaAndando = false;
                actualizarAnimacionIdlePorDireccion();
                tiempoAnimacion = 0; // Reiniciar animación
                Gdx.app.log("PersonajeLobby", "Cambiando a estado IDLE - Dirección: " + direccionActual);
            }
        } else {
            estaAndando = true;
        }
    }

    /**
     * Método para mover el personaje usando input del Procesador
     * @param delta Tiempo transcurrido desde el último frame
     * @param direccionX Dirección en X (-1 izquierda, 1 derecha)
     * @param direccionY Dirección en Y (-1 abajo, 1 arriba)
     */
    public void mover(float delta, float direccionX, float direccionY) {
        // Actualizar velocidades
        velocidadX = direccionX * VELOCIDAD;
        velocidadY = direccionY * VELOCIDAD;

        // Si hay input, determinar dirección
        if (direccionX != 0 || direccionY != 0) {
            determinarDireccion(direccionX, direccionY);

            // Normalizar velocidad diagonal para mantener velocidad constante
            if (velocidadX != 0 && velocidadY != 0) {
                float factor = (float) (VELOCIDAD / Math.sqrt(velocidadX * velocidadX + velocidadY * velocidadY));
                velocidadX *= factor;
                velocidadY *= factor;
            }

            // Aplicar movimiento con detección de colisiones
            aplicarMovimiento(delta);
        }
    }

    /**
     * Determina la dirección del personaje basándose en el vector de movimiento
     * @param direccionX Componente X del vector de movimiento
     * @param direccionY Componente Y del vector de movimiento
     */
    private void determinarDireccion(float direccionX, float direccionY) {
        // Determinar la dirección basada en el vector de movimiento
        if (direccionY > 0) {
            if (direccionX < 0) {
                direccionActual = Direccion.ARRIBA_IZQUIERDA;
            } else if (direccionX > 0) {
                direccionActual = Direccion.ARRIBA_DERECHA;
            } else {
                direccionActual = Direccion.ARRIBA;
            }
        } else if (direccionY < 0) {
            if (direccionX < 0) {
                direccionActual = Direccion.ABAJO_IZQUIERDA;
            } else if (direccionX > 0) {
                direccionActual = Direccion.ABAJO_DERECHA;
            } else {
                direccionActual = Direccion.ABAJO;
            }
        } else {
            if (direccionX < 0) {
                direccionActual = Direccion.IZQUIERDA;
            } else if (direccionX > 0) {
                direccionActual = Direccion.DERECHA;
            }
            // Si no hay movimiento en X, mantener la dirección anterior
        }
    }

    /**
     * Aplica el movimiento y verifica colisiones
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void aplicarMovimiento(float delta) {
        // Guardar posición anterior para posibles colisiones
        float xAnterior = getX();
        float yAnterior = getY();

        // Aplicar movimiento
        setX(getX() + velocidadX * delta);
        setY(getY() + velocidadY * delta);

        // Verificar colisiones con el mapa
        if (mapaManager != null && mapaManager.hayColision(getHitbox())) {
            // Revertir movimiento en caso de colisión
            setX(xAnterior);
            setY(yAnterior);
            // También detener la velocidad en caso de colisión
            velocidadX = 0;
            velocidadY = 0;
        }

        // Mantener dentro de los límites del mapa (opcional)
        mantenerDentroMapa();
    }

    /**
     * Actualiza la animación según el estado del personaje
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        if (estaAndando) {
            actualizarAnimacionPorDireccion();
            frameActual = animacionAndar.getKeyFrame(tiempoAnimacion, true);
        } else {
            // Para idle, usar animación loop pero con frame rate más lento
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    /**
     * Actualiza la animación de andar según la dirección actual
     */
    private void actualizarAnimacionPorDireccion() {
        // Cambiar la animación de andar según la dirección
        switch (direccionActual) {
            case ARRIBA:
            case ARRIBA_IZQUIERDA:
            case ARRIBA_DERECHA:
                animacionAndar = animacionAndarArriba;
                break;
            case ABAJO:
            case ABAJO_IZQUIERDA:
            case ABAJO_DERECHA:
                animacionAndar = animacionAndarAbajo;
                break;
            case IZQUIERDA:
                animacionAndar = animacionAndarIzquierda;
                break;
            case DERECHA:
                animacionAndar = animacionAndarDerecha;
                break;
        }
    }

    /**
     * Actualiza la animación idle según la última dirección
     */
    private void actualizarAnimacionIdlePorDireccion() {
        // Cambiar la animación idle según la última dirección
        switch (direccionActual) {
            case ARRIBA:
            case ARRIBA_IZQUIERDA:
            case ARRIBA_DERECHA:
                animacionIdle = animacionIdleArriba;
                Gdx.app.log("PersonajeLobby", "Cambiando a idle ARRIBA");
                break;
            case ABAJO:
            case ABAJO_IZQUIERDA:
            case ABAJO_DERECHA:
                animacionIdle = animacionIdleAbajo;
                Gdx.app.log("PersonajeLobby", "Cambiando a idle ABAJO");
                break;
            case IZQUIERDA:
                animacionIdle = animacionIdleIzquierda;
                Gdx.app.log("PersonajeLobby", "Cambiando a idle IZQUIERDA");
                break;
            case DERECHA:
                animacionIdle = animacionIdleDerecha;
                Gdx.app.log("PersonajeLobby", "Cambiando a idle DERECHA");
                break;
        }
    }

    /**
     * Mantiene al personaje dentro de los límites del mapa
     */
    private void mantenerDentroMapa() {
        if (mapaManager == null) return;

        try {
            // Obtener dimensiones del mapa
            float minX = 0;
            float minY = 0;
            float maxX = mapaManager.getAnchoMapa() - getWidth();
            float maxY = mapaManager.getAltoMapa() - getHeight();

            // Aplicar límites
            if (getX() < minX) setX(minX);
            if (getY() < minY) setY(minY);
            if (getX() > maxX) setX(maxX);
            if (getY() > maxY) setY(maxY);
        } catch (Exception e) {
            Gdx.app.error("PersonajeLobby", "Error en mantenerDentroMapa", e);
        }
    }

    // ====================== RENDERIZADO ======================

    /**
     * Dibuja el personaje en pantalla
     * @param batch Batch para dibujado
     * @param parentAlpha Alpha heredado del padre
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (frameActual != null) {
            // Guardar color original del batch
            Color colorOriginal = batch.getColor();

            // Establecer color completamente opaco (sin transparencia)
            batch.setColor(1, 1, 1, 1);

            // Dibujar el frame
            batch.draw(frameActual, getX(), getY(), getWidth(), getHeight());

            // Restaurar color original
            batch.setColor(colorOriginal);
        }

        // Debug hitbox (opcional)
        if (DEBUG_HITBOX) {
            dibujarHitboxDebug(batch);
        }
    }

    /**
     * Método para dibujar el hitbox en modo debug
     */
    private void dibujarHitboxDebug(Batch batch) {
        Rectangle hitbox = getHitbox();
        // Necesitarías implementar esto con ShapeRenderer o texturas simples
        // Esto es solo un placeholder para el concepto
        Gdx.app.log("HITBOX_DEBUG", "Hitbox: " + hitbox);
    }

    // ====================== COLISIONES ======================

    /**
     * Obtiene el hitbox del personaje para detección de colisiones
     * @return Rectángulo que representa el área de colisión
     */
    public Rectangle getHitbox() {
        // Hitbox ajustado al tamaño real del sprite con márgenes proporcionales
        return new Rectangle(
            getX() + MARGEN_HITBOX_HORIZONTAL,
            getY() + MARGEN_HITBOX_VERTICAL,
            getWidth() - MARGEN_HITBOX_HORIZONTAL * 2,
            getHeight() - MARGEN_HITBOX_VERTICAL * 2
        );
    }

    // ====================== CONFIGURACIÓN DE TAMAÑO ======================

    /**
     * Establece el tamaño inicial del personaje basado en el frame actual
     */
    private void setSizeInicial() {
        if (frameActual != null) {
            setSize(frameActual.getRegionWidth(), frameActual.getRegionHeight());
        } else {
            setSize(64, 59); // Tamaño por defecto basado en el atlas
        }
    }

    /**
     * @return El ancho del personaje basado en el frame actual
     */
    @Override
    public float getWidth() {
        return frameActual != null ? frameActual.getRegionWidth() : 64;
    }

    /**
     * @return El alto del personaje basado en el frame actual
     */
    @Override
    public float getHeight() {
        return frameActual != null ? frameActual.getRegionHeight() : 59;
    }

    // ====================== LIMPIEZA DE RECURSOS ======================

    /**
     * Libera los recursos utilizados por el personaje
     */
    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
            Gdx.app.log("PersonajeLobby", "Recursos liberados correctamente");
        }
    }

    // ====================== MÉTODOS DE ACCESO ======================

    /**
     * Establece el manager del mapa para colisiones
     * @param mapaManager Instancia de MapaManager
     */
    public void setMapaManager(MapaManager mapaManager) {
        this.mapaManager = mapaManager;
    }

    /**
     * @return true si el personaje se está moviendo
     */
    public boolean estaAndando() {
        return estaAndando;
    }

    /**
     * @return La dirección actual del personaje
     */
    public Direccion getDireccionActual() {
        return direccionActual;
    }

    /**
     * Permite ajustar los márgenes del hitbox en tiempo de ejecución
     * @param horizontal Margen horizontal
     * @param vertical Margen vertical
     */
    public void setMargenesHitbox(float horizontal, float vertical) {
        // Nota: Esto no funcionaría con las constantes actuales
        // Podrías convertir las constantes en variables si necesitas esta funcionalidad
        Gdx.app.log("PersonajeLobby", "Usar constantes MARGEN_HITBOX_HORIZONTAL y MARGEN_HITBOX_VERTICAL para ajustar");
    }
    // En ambas clases, añade este método:

}
