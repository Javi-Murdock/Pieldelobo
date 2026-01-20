package io.github.javiergames.pieldelobo.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Actor que representa un punto de finalización del nivel con animaciones de totem.
 * Es transparente hasta que se eliminan todos los enemigos.
 * El jugador puede pasar por encima para completar el nivel.
 *
 *  * @author Javier Gala
 *  * @version 2.1
 */
public class IndicadorNivel extends Actor {

    // ====================== CONSTANTES ======================
    private static final float ALPHA_RATE = 0.8f; // Velocidad de transparencia
    private static final float HITBOX_OFFSET_X = 25f; // Offset horizontal del hitbox
    private static final float HITBOX_OFFSET_Y = 80f; // Offset vertical del hitbox (los totems son altos)
    private static final float DEFAULT_WIDTH = 100f; // Ancho por defecto
    private static final float DEFAULT_HEIGHT = 200f; // Alto por defecto (los totems son 200x400)
    private static final float ANIMATION_SPEED = 0.1f; // Velocidad de la animación
    private static final float PULSE_INTENSITY = 0.05f; // Intensidad del pulso (más sutil)

    // ====================== COMPONENTES GRÁFICOS ======================
    private TextureAtlas atlas; // Atlas del totem
    private Animation<TextureRegion> animacionIdle; // Animación normal
    private Animation<TextureRegion> animacionActivo; // Animación cuando está activo
    private TextureRegion frameActual; // Frame actual a dibujar
    private float tiempoAnimacion = 0; // Tiempo acumulado para animaciones
    private TextureRegion texturaRespaldo; // Textura de respaldo si no hay atlas

    // ====================== ESTADOS ======================
    private boolean todosEnemigosEliminados = false; // True cuando se eliminan todos los enemigos
    private boolean visible = false; // True cuando se debe mostrar
    private float alpha = 0.0f; // Nivel de transparencia (0 = invisible, 1 = visible)
    private boolean activo = false; // True cuando el indicador está completamente activo
    private boolean usandoRespaldo = false; // True si estamos usando textura de respaldo

    // ====================== EFECTOS VISUALES ======================
    private float scale = 1.0f; // Escala para efecto de pulso
    private float pulseTime = 0.0f; // Tiempo acumulado para animación de pulso
    private float glowAlpha = 0.0f; // Alpha para efecto de brillo
    private float glowDirection = 1.0f; // Dirección del brillo (1 = aumentar, -1 = disminuir)

    // ====================== COLISIONES ======================
    private Rectangle hitbox; // Hitbox para detección de colisiones

    // ====================== CONSTRUCTOR ======================

    /**
     * Constructor principal del indicador de nivel.
     * @param x Posición horizontal
     * @param y Posición vertical
     * @param width Ancho del indicador
     * @param height Alto del indicador
     */
    public IndicadorNivel(float x, float y, float width, float height) {
        // Establecer tamaño y posición
        setBounds(x, y, width, height);

        // Crear hitbox (más pequeño que el sprite visual, centrado)
        hitbox = new Rectangle(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y,
            width - HITBOX_OFFSET_X * 2, height - HITBOX_OFFSET_Y * 2);

        // Cargar las animaciones del totem
        cargarAnimaciones();

        Gdx.app.log("IndicadorNivel", "Creado en: [" + x + ", " + y +
            "] Tamaño: [" + width + "x" + height + "]");
    }

    /**
     * Constructor simplificado con tamaño por defecto.
     * @param x Posición horizontal
     * @param y Posición vertical
     */
    public IndicadorNivel(float x, float y) {
        this(x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    // ====================== CARGA DE ANIMACIONES ======================

    /**
     * Carga las animaciones del totem desde el sprite sheet.
     * Usa las frames del totem para crear animaciones suaves.
     */
    private void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe
            if (!Gdx.files.internal("Totem.atlas").exists()) {
                Gdx.app.log("IndicadorNivel", "Archivo Totem.atlas no encontrado");
                crearTexturaRespaldo();
                usandoRespaldo = true;
                return;
            }

            // Intentar cargar el atlas del totem
            atlas = new TextureAtlas(Gdx.files.internal("Totem.atlas"));

            if (atlas == null) {
                Gdx.app.error("IndicadorNivel", "No se pudo cargar el atlas Totem.atlas");
                crearTexturaRespaldo();
                usandoRespaldo = true;
                return;
            }

            // Verificar si hay regiones en el atlas
            if (atlas.getRegions().size == 0) {
                Gdx.app.error("IndicadorNivel", "Atlas Totem.atlas está vacío");
                crearTexturaRespaldo();
                usandoRespaldo = true;
                return;
            }

            // Crear array para frames de animación idle (estado normal)
            Array<TextureRegion> idleFrames = new Array<>();

            // Intentar cargar los primeros 8 frames
            int framesCargados = 0;
            for (int i = 0; i < 8; i++) {
                String frameName = String.format("totem%03d", i);
                TextureRegion region = atlas.findRegion(frameName);
                if (region != null) {
                    idleFrames.add(region);
                    framesCargados++;
                }
            }

            if (framesCargados == 0) {
                // Si no encontró frames con el formato esperado, usar todos los frames disponibles
                Gdx.app.log("IndicadorNivel", "No se encontraron frames con formato 'totem###', usando todas las regiones");
                idleFrames.addAll(atlas.getRegions());
            }

            // Crear animación idle (más lenta)
            if (idleFrames.size > 0) {
                animacionIdle = new Animation<>(ANIMATION_SPEED * 2, idleFrames, Animation.PlayMode.LOOP);
            } else {
                throw new Exception("No se encontraron frames en el atlas");
            }

            // Crear array para frames de animación activa
            Array<TextureRegion> activeFrames = new Array<>();

            // Intentar cargar todos los frames disponibles para animación activa
            int maxFrames = 13; // Intentar hasta 13 frames
            for (int i = 0; i < maxFrames; i++) {
                String frameName = String.format("totem%03d", i);
                TextureRegion region = atlas.findRegion(frameName);
                if (region != null) {
                    activeFrames.add(region);
                }
            }

            // Si no encontró suficientes frames, usar los mismos que idle
            if (activeFrames.size < 5) {
                activeFrames.clear();
                activeFrames.addAll(idleFrames);
            }

            // Crear animación activa (más rápida)
            animacionActivo = new Animation<>(ANIMATION_SPEED, activeFrames, Animation.PlayMode.LOOP);

            // Establecer frame inicial
            frameActual = idleFrames.first();

            Gdx.app.log("IndicadorNivel", "Animaciones cargadas correctamente: " +
                idleFrames.size + " frames idle, " +
                activeFrames.size + " frames activo");

        } catch (Exception e) {
            Gdx.app.error("IndicadorNivel", "Error cargando animaciones del totem", e);
            // Crear sprite de respaldo
            crearTexturaRespaldo();
            usandoRespaldo = true;
        }
    }

    /**
     * Crea una textura de respaldo en caso de error.
     */
    private void crearTexturaRespaldo() {
        try {
            // Crear una textura simple programáticamente
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
                (int)DEFAULT_WIDTH, (int)DEFAULT_HEIGHT, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);

            // Crear un totem simple (rectángulo con detalles)
            // Base marrón
            pixmap.setColor(0.6f, 0.4f, 0.2f, 1.0f);
            pixmap.fillRectangle(30, 0, 40, 180);

            // Detalles azules
            pixmap.setColor(0.2f, 0.4f, 1.0f, 1.0f);
            pixmap.fillRectangle(35, 20, 30, 15);
            pixmap.fillRectangle(35, 60, 30, 15);
            pixmap.fillRectangle(35, 100, 30, 15);
            pixmap.fillRectangle(35, 140, 30, 15);

            // Parte superior
            pixmap.setColor(0.8f, 0.6f, 0.3f, 1.0f);
            pixmap.fillTriangle(50, 180, 30, 160, 70, 160);

            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            texturaRespaldo = new TextureRegion(texture);
            frameActual = texturaRespaldo;

            Gdx.app.log("IndicadorNivel", "Textura de respaldo creada");

        } catch (Exception e) {
            Gdx.app.error("IndicadorNivel", "Error creando textura de respaldo", e);
            // Último recurso: frame null
            frameActual = null;
        }
    }

    // ====================== MÉTODOS DE CONTROL ======================

    /**
     * Establece si todos los enemigos han sido eliminados.
     * Cuando esto es true, el indicador comienza a hacerse visible y activo.
     * @param eliminados True si todos los enemigos están eliminados
     */
    public void setTodosEnemigosEliminados(boolean eliminados) {
        if (eliminados && !todosEnemigosEliminados) {
            Gdx.app.log("IndicadorNivel", "¡Todos los enemigos eliminados! Activando animaciones...");
            activo = true;

            // Reproducir sonido de activación (opcional)
            try {
                com.badlogic.gdx.audio.Sound sound =
                    Gdx.audio.newSound(Gdx.files.internal("sounds/totem_activate.wav"));
                if (sound != null) {
                    sound.play(0.6f);
                }
            } catch (Exception e) {
                // Silenciar si no hay sonido disponible
            }
        }
        this.todosEnemigosEliminados = eliminados;
    }

    /**
     * Obtiene el estado de visibilidad del indicador.
     * @return True si el indicador es visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Obtiene si el nivel está listo para completarse.
     * @return True si todos los enemigos están eliminados y el indicador es visible
     */
    public boolean isCompletado() {
        return todosEnemigosEliminados && visible && alpha > 0.7f;
    }

    /**
     * Obtiene el hitbox del indicador para detección de colisiones.
     * @return Rectángulo que representa el área de colisión
     */
    public Rectangle getHitbox() {
        return hitbox;
    }

    /**
     * Obtiene si el indicador está activo (con animaciones completas).
     * @return True si está activo
     */
    public boolean isActivo() {
        return activo;
    }

    // ====================== MÉTODOS DE CICLO DE VIDA ======================

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar tiempo de animación
        tiempoAnimacion += delta;

        // Actualizar transparencia según el estado de los enemigos
        actualizarTransparencia(delta);

        // Actualizar animación según el estado
        actualizarAnimacion();

        // Actualizar efectos visuales
        actualizarEfectosVisuales(delta);

        // Actualizar posición del hitbox (por si el actor se mueve)
        hitbox.setPosition(getX() + HITBOX_OFFSET_X, getY() + HITBOX_OFFSET_Y);
    }

    /**
     * Actualiza la transparencia del indicador.
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void actualizarTransparencia(float delta) {
        if (todosEnemigosEliminados) {
            // Aumentar alpha gradualmente hasta 1.0
            if (alpha < 1.0f) {
                alpha += delta * ALPHA_RATE;
                if (alpha > 1.0f) alpha = 1.0f;
            }
            visible = alpha > 0.1f; // Visible si alpha > 10%
        } else {
            // Disminuir alpha gradualmente hasta 0.0
            if (alpha > 0.0f) {
                alpha -= delta * ALPHA_RATE;
                if (alpha < 0.0f) alpha = 0.0f;
            }
            visible = false; // No visible si los enemigos no están eliminados
        }
    }

    /**
     * Actualiza la animación según el estado del indicador.
     */
    private void actualizarAnimacion() {
        if (frameActual == null) return;

        if (usandoRespaldo) {
            // Si estamos usando respaldo, no hay animación, solo mostrar la textura
            return;
        }

        if (activo && animacionActivo != null) {
            // Usar animación activa (más rápida y completa)
            frameActual = animacionActivo.getKeyFrame(tiempoAnimacion, true);
        } else if (animacionIdle != null) {
            // Usar animación idle (más lenta)
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    /**
     * Actualiza los efectos visuales (pulso, brillo).
     * @param delta Tiempo transcurrido desde el último frame
     */
    private void actualizarEfectosVisuales(float delta) {
        if (visible) {
            // Efecto de pulso suave
            pulseTime += delta * 1.5f;
            scale = 1.0f + (float)Math.sin(pulseTime) * PULSE_INTENSITY;

            // Efecto de brillo pulsante cuando está activo
            if (activo) {
                glowAlpha += delta * glowDirection * 0.8f;
                if (glowAlpha > 0.6f) {
                    glowAlpha = 0.6f;
                    glowDirection = -1.0f; // Cambiar dirección
                } else if (glowAlpha < 0.0f) {
                    glowAlpha = 0.0f;
                    glowDirection = 1.0f; // Cambiar dirección
                }
            }

            // Mantener pulseTime en un rango manejable
            if (pulseTime > 100f) {
                pulseTime = 0f;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // No dibujar si no es visible o si no hay frame actual
        if (!visible || frameActual == null) return;

        // Calcular alpha final (combinar alpha del indicador con alpha del batch)
        float finalAlpha = alpha * parentAlpha;

        // Guardar color original del batch
        com.badlogic.gdx.graphics.Color originalColor = batch.getColor();

        // ====================== DIBUJAR EFECTO DE BRILLO (si está activo y no usando respaldo) ======================
        if (activo && glowAlpha > 0 && !usandoRespaldo) {
            float glowScale = scale * 1.2f; // El brillo es un poco más grande

            // Configurar color para el brillo (azul brillante)
            batch.setColor(0.3f, 0.6f, 1.0f, finalAlpha * glowAlpha * 0.7f);

            // Dibujar el brillo
            float centerX = getX() + getWidth() / 2;
            float centerY = getY() + getHeight() / 2;
            float glowWidth = getWidth() * glowScale;
            float glowHeight = getHeight() * glowScale;

            batch.draw(frameActual,
                centerX - glowWidth/2, centerY - glowHeight/2,
                glowWidth, glowHeight);
        }

        // ====================== DIBUJAR TOTEM PRINCIPAL ======================
        // Aplicar transparencia y color normal
        batch.setColor(1, 1, 1, finalAlpha);

        // Calcular posición y tamaño con efecto de pulso
        float centerX = getX() + getWidth() / 2;
        float centerY = getY() + getHeight() / 2;
        float scaledWidth = getWidth() * scale;
        float scaledHeight = getHeight() * scale;

        // Dibujar el totem centrado y escalado
        batch.draw(frameActual,
            centerX - scaledWidth/2, centerY - scaledHeight/2,
            scaledWidth, scaledHeight);

        // ====================== DIBUJAR EFECTO DE PARTICULAS (opcional, si está muy activo y no usando respaldo) ======================
        if (activo && alpha > 0.8f && !usandoRespaldo) {
            dibujarEfectoParticulas(batch, finalAlpha, centerX, centerY);
        }

        // Restaurar color original del batch
        batch.setColor(originalColor);
    }

    /**
     * Dibuja efectos de partículas alrededor del totem cuando está muy activo.
     * @param batch Batch para dibujar
     * @param alpha Alpha final
     * @param centerX Centro X del totem
     * @param centerY Centro Y del totem
     */
    private void dibujarEfectoParticulas(Batch batch, float alpha, float centerX, float centerY) {
        // Usar el frame actual para partículas
        float particleTime = tiempoAnimacion * 2.0f;

        for (int i = 0; i < 4; i++) {
            float angle = particleTime + (i * 90); // 4 partículas, 90 grados separadas
            float distance = 60f + (float)Math.sin(particleTime * 0.5f + i) * 20f;

            float particleX = centerX + (float)Math.cos(Math.toRadians(angle)) * distance;
            float particleY = centerY + (float)Math.sin(Math.toRadians(angle)) * distance;

            // Tamaño pulsante de partícula
            float particleSize = 8f + (float)Math.sin(particleTime * 3f + i) * 3f;

            // Color azul brillante
            batch.setColor(0.4f, 0.7f, 1.0f, alpha * 0.6f);

            // Dibujar partícula
            batch.draw(frameActual,
                particleX - particleSize/2, particleY - particleSize/2,
                particleSize, particleSize);
        }

        // Restaurar color blanco para el resto del dibujado
        batch.setColor(1, 1, 1, 1);
    }

    // ====================== LIMPIEZA DE RECURSOS ======================

    @Override
    public boolean remove() {
        // Liberar recursos gráficos
        if (atlas != null) {
            atlas.dispose();
            Gdx.app.log("IndicadorNivel", "Atlas del totem liberado");
        }

        if (texturaRespaldo != null && texturaRespaldo.getTexture() != null) {
            texturaRespaldo.getTexture().dispose();
            Gdx.app.log("IndicadorNivel", "Textura de respaldo liberada");
        }

        return super.remove();
    }

    /**
     * Libera explícitamente los recursos del indicador.
     */
    public void dispose() {
        remove();
    }

    // ====================== GETTERS Y SETTERS ======================

    public float getAlpha() {
        return alpha;
    }

    public float getScale() {
        return scale;
    }

    public boolean isTodosEnemigosEliminados() {
        return todosEnemigosEliminados;
    }

    public float getTiempoAnimacion() {
        return tiempoAnimacion;
    }

    public boolean isUsandoRespaldo() {
        return usandoRespaldo;
    }
}
