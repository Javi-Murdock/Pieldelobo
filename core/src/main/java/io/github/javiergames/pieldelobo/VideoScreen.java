
package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.files.FileHandle;

import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.GestorJuego.Main;
import io.github.javiergames.pieldelobo.Graficos.GameUtils;

/**
 * Pantalla para reproducir videos usando gdx-video.
 * Reproduce automáticamente, no tiene controles, al terminar vuelve al juego.
 *
 * @author Javier Gala
 * @version 2.0 (Actualizado para gdx-video)
 */
public class VideoScreen implements Screen {

    // ====================== REFERENCIAS PRINCIPALES ======================
    private final Main game;
    private final String videoPath;
    private final Screen pantallaDestino; // Pantalla a la que volver después

    // ====================== RECURSOS GRÁFICOS ======================
    private SpriteBatch batch;
    private VideoPlayer videoPlayer;
    private Texture videoFrame;

    // ====================== CONTROL DEL VIDEO ======================
    private boolean videoTerminado = false;

    // ====================== TRANSICIONES SUAVES ======================
    private float alpha = 0; // Para fade in (0 a 1)
    private boolean haciendoFadeIn = true;
    private boolean haciendoFadeOut = false;
    private float tiempoFade = 0;

    // ====================== IDENTIFICACIÓN DEL VIDEO ======================
    private String videoId = "unknown"; // ID para registro en base de datos

    // ====================== CONTROL DE ERRORES ======================
    private boolean errorCarga = false;
    private float tiempoError = 0;

    // ====================== CONSTRUCTORES ======================

    /**
     * Constructor principal con pantalla destino personalizada.
     *
     * @param game Instancia principal del juego
     * @param videoPath Ruta del archivo de video
     * @param pantallaDestino Pantalla a la que volver después del video
     */
    public VideoScreen(Main game, String videoPath, Screen pantallaDestino) {
        this.game = game;
        this.videoPath = videoPath;
        this.pantallaDestino = pantallaDestino;
        extraerVideoId();

        Gdx.app.log("VideoScreen", "Constructor: " + videoPath + " -> " + pantallaDestino.getClass().getSimpleName());
    }

    /**
     * Constructor simplificado que vuelve al Lobby por defecto.
     *
     * @param game Instancia principal del juego
     * @param videoPath Ruta del archivo de video
     */
    public VideoScreen(Main game, String videoPath) {
        this(game, videoPath, new LobbyScreen(game));
    }

    // ====================== MÉTODOS DEL CICLO DE VIDA ======================

    /**
     * Se llama cuando se muestra la pantalla.
     * Inicializa todos los recursos necesarios.
     */
    @Override
    public void show() {
        Gdx.app.log("VideoScreen", "=== INICIANDO REPRODUCCIÓN DE VIDEO ===");
        Gdx.app.log("VideoScreen", "Ruta del video: " + videoPath);
        Gdx.app.log("VideoScreen", "Video ID: " + videoId);
        Gdx.app.log("VideoScreen", "Destino después: " + pantallaDestino.getClass().getSimpleName());

        batch = new SpriteBatch();

        try {
            // Crear VideoPlayer
            videoPlayer = VideoPlayerCreator.createVideoPlayer();

            // Configurar listener para cuando termine el video
            videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
                @Override
                public void onCompletionListener(FileHandle file) {  // CORRECCIÓN: Cambiado de onCompletion a onCompletionListener
                    Gdx.app.log("VideoScreen", "Video completado");
                    videoTerminado = true;
                    haciendoFadeOut = true;
                }
            });

            // Cargar y reproducir el video
            FileHandle videoFile = Gdx.files.internal(videoPath);
            if (!videoFile.exists()) {
                throw new RuntimeException("Archivo de video no encontrado: " + videoPath);
            }

            // PREPARAR el video primero
            videoPlayer.play(videoFile);
            videoPlayer.setVolume(1.0f);
            videoPlayer.pause(); // Pausar inicialmente para controlar el fade in

            Gdx.app.log("VideoScreen", "VideoPlayer creado y configurado");
            errorCarga = false;

        } catch (Exception e) {
            Gdx.app.error("VideoScreen", "Error cargando video con gdx-video: " + videoPath, e);
            errorCarga = true;
            videoFrame = new Texture(Gdx.files.internal("badlogic.jpg"));
            tiempoError = 3.0f; // Mostrar error por 3 segundos
        }

        // Inicializar transiciones
        alpha = 0;
        haciendoFadeIn = true;
        haciendoFadeOut = false;
        tiempoFade = 0;
        videoTerminado = false;
    }

    /**
     * Método principal de renderizado, llamado cada frame.
     *
     * @param delta Tiempo transcurrido desde el último frame en segundos
     */
    @Override
    public void render(float delta) {
        tiempoFade += delta;

        // ====================== MANEJO DE ERRORES ======================
        if (errorCarga) {
            manejarError(delta);
            return;
        }

        // ====================== ACTUALIZAR VIDEO PLAYER ======================
        if (videoPlayer != null && !videoTerminado) {
            videoPlayer.update();

            // Verificar si el video terminó (por si el listener falló)
            if (!videoPlayer.isPlaying() && !haciendoFadeIn && alpha >= 1.0f) {
                Gdx.app.log("VideoScreen", "VideoPlayer dejó de reproducir, marcando como terminado");
                videoTerminado = true;
                haciendoFadeOut = true;
            }
        }

        // ====================== ACTUALIZAR TRANSICIONES ======================
        actualizarTransiciones(delta);

        // ====================== VERIFICAR FADE OUT COMPLETO ======================
        if (haciendoFadeOut && alpha <= 0) {
            volverAlJuego();
            return; // No renderizar más
        }

        // ====================== RENDERIZAR PANTALLA ======================
        renderizarPantalla();

        // ====================== DETECCIÓN DE ENTRADA ======================
        verificarEntradaUsuario();
    }

    /**
     * Maneja la pantalla cuando hay error cargando el video.
     */
    private void manejarError(float delta) {
        tiempoError -= delta;

        if (tiempoError <= 0 || Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
            volverAlJuego();
            return;
        }

        // Renderizar pantalla de error
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        if (videoFrame != null) {
            // Calcular dimensiones manteniendo relación de aspecto
            float videoWidth = videoFrame.getWidth();
            float videoHeight = videoFrame.getHeight();
            float aspectRatio = videoWidth / videoHeight;

            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            float screenAspect = screenWidth / screenHeight;

            float drawWidth, drawHeight, drawX, drawY;

            if (aspectRatio > screenAspect) {
                drawWidth = screenWidth;
                drawHeight = screenWidth / aspectRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2;
            } else {
                drawHeight = screenHeight;
                drawWidth = screenHeight * aspectRatio;
                drawX = (screenWidth - drawWidth) / 2;
                drawY = 0;
            }

            // Dibujar la textura de error
            batch.draw(videoFrame, drawX, drawY, drawWidth, drawHeight);

            // Mensaje de error
            batch.setColor(1, 0, 0, 1);
            batch.draw(GameUtils.getWhitePixel(),
                screenWidth/2 - 200, screenHeight/2 - 20, 400, 40);
            batch.setColor(1, 1, 1, 1);
        }

        batch.end();
    }

    /**
     * Actualiza las transiciones de fade in/out.
     */
    private void actualizarTransiciones(float delta) {
        if (haciendoFadeIn) {
            alpha += delta * 2; // Fade in rápido (0.5 segundos)
            if (alpha >= 0.5f && videoPlayer != null) {
                // Comenzar a reproducir cuando el fade in esté a la mitad
                videoPlayer.play();
            }
            if (alpha >= 1) {
                alpha = 1;
                haciendoFadeIn = false;
                Gdx.app.log("VideoScreen", "Fade in completado, reproduciendo video");
            }
        }

        if (haciendoFadeOut) {
            alpha -= delta * 2; // Fade out rápido (0.5 segundos)
            if (alpha <= 0) {
                alpha = 0;
            }
        }
    }

    /**
     * Renderiza el video y los elementos de la pantalla.
     */
    private void renderizarPantalla() {
        // Limpiar pantalla con color negro
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.setColor(1, 1, 1, alpha);

        if (videoPlayer != null && videoPlayer.getTexture() != null) {
            Texture videoTexture = videoPlayer.getTexture();

            // Calcular dimensiones manteniendo relación de aspecto
            float videoWidth = videoTexture.getWidth();
            float videoHeight = videoTexture.getHeight();
            float aspectRatio = videoWidth / videoHeight;

            float screenWidth = Gdx.graphics.getWidth();
            float screenHeight = Gdx.graphics.getHeight();
            float screenAspect = screenWidth / screenHeight;

            float drawWidth, drawHeight, drawX, drawY;

            if (aspectRatio > screenAspect) {
                // Video más ancho que pantalla (letterbox vertical)
                drawWidth = screenWidth;
                drawHeight = screenWidth / aspectRatio;
                drawX = 0;
                drawY = (screenHeight - drawHeight) / 2;
            } else {
                // Video más alto que pantalla (letterbox horizontal)
                drawHeight = screenHeight;
                drawWidth = screenHeight * aspectRatio;
                drawX = (screenWidth - drawWidth) / 2;
                drawY = 0;
            }

            // Dibujar el video centrado
            batch.draw(videoTexture, drawX, drawY, drawWidth, drawHeight);

            // Dibujar bordes negros si hay letterbox
            if (drawY > 0) {
                batch.setColor(0, 0, 0, alpha);
                batch.draw(GameUtils.getWhitePixel(), 0, 0, screenWidth, drawY);
                batch.draw(GameUtils.getWhitePixel(), 0, drawY + drawHeight,
                    screenWidth, screenHeight - (drawY + drawHeight));
                batch.setColor(1, 1, 1, alpha);
            }
        } else {
            // Si no hay video, pantalla negra
            batch.setColor(0, 0, 0, alpha);
            batch.draw(GameUtils.getWhitePixel(), 0, 0,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        batch.end();
    }

    /**
     * Verifica si el usuario quiere saltar el video.
     */
    private void verificarEntradaUsuario() {
        // Si se presiona cualquier tecla o clic, terminar inmediatamente
        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY)) {
            if (!haciendoFadeOut) {
                haciendoFadeOut = true;
                videoTerminado = true;
                Gdx.app.log("VideoScreen", "Video saltado por usuario");

                // Detener reproducción inmediatamente
                if (videoPlayer != null && videoPlayer.isPlaying()) {
                    videoPlayer.pause();
                }
            }
        }
    }

    /**
     * Vuelve al juego después del video.
     * Registra el video como visto y vuelve a la pantalla destino.
     */
    private void volverAlJuego() {
        Gdx.app.log("VideoScreen", "=== VOLVIENDO AL JUEGO ===");

        // Registrar que se vio el video en la base de datos
        registrarVideoVisto();

        // Si la pantalla destino es LobbyScreen, forzar actualización
        if (pantallaDestino instanceof LobbyScreen) {
            LobbyScreen lobby = (LobbyScreen) pantallaDestino;
            Gdx.app.log("VideoScreen", "Destino es LobbyScreen, forzando actualización de puertas");
        }

        // Volver a la pantalla destino
        if (pantallaDestino != null) {
            game.setScreen(pantallaDestino);
            Gdx.app.log("VideoScreen", "Volviendo a: " + pantallaDestino.getClass().getSimpleName());
        } else {
            // Si no hay pantalla destino, volver al lobby por defecto
            game.setScreen(new LobbyScreen(game));
            Gdx.app.log("VideoScreen", "Volviendo a LobbyScreen (por defecto)");
        }

        // Liberar recursos
        dispose();
    }

    /**
     * Registra el video como visto en la base de datos.
     */
    private void registrarVideoVisto() {
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            db.marcarVideoComoVisto(videoId);

            Gdx.app.log("VideoScreen", "Video registrado como visto: " + videoId);

            // Incrementar estadísticas si es necesario
            db.getStats().npcsHablados++;

        } catch (Exception e) {
            // No crítico si falla el registro
            Gdx.app.error("VideoScreen", "Error registrando video", e);
        }
    }

    /**
     * Extrae el ID del video del path del archivo.
     */
    private void extraerVideoId() {
        try {
            String nombreArchivo = videoPath.substring(videoPath.lastIndexOf('/') + 1);
            this.videoId = nombreArchivo.substring(0, nombreArchivo.lastIndexOf('.'));
            Gdx.app.log("VideoScreen", "ID extraído: " + videoId + " de " + videoPath);
        } catch (Exception e) {
            Gdx.app.error("VideoScreen", "Error extrayendo ID del video", e);
            this.videoId = "unknown";
        }
    }

    // ====================== MÉTODOS DE CICLO DE VIDA RESTANTES ======================

    @Override
    public void resize(int width, int height) {
        // gdx-video maneja el resize automáticamente
        Gdx.app.log("VideoScreen", "Resize a: " + width + "x" + height);
    }

    @Override
    public void pause() {
        Gdx.app.log("VideoScreen", "Pantalla pausada");
        if (videoPlayer != null && videoPlayer.isPlaying()) {
            videoPlayer.pause();
        }
    }

    @Override
    public void resume() {
        Gdx.app.log("VideoScreen", "Pantalla reanudada");
        if (videoPlayer != null && !videoPlayer.isPlaying() && !videoTerminado && !haciendoFadeOut) {
            videoPlayer.play();
        }
    }

    @Override
    public void hide() {
        Gdx.app.log("VideoScreen", "Pantalla ocultada");
        if (videoPlayer != null && videoPlayer.isPlaying()) {
            videoPlayer.pause();
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("VideoScreen", "=== LIBERANDO RECURSOS DEL VIDEO ===");

        try {
            if (videoPlayer != null) {
                videoPlayer.dispose();
                videoPlayer = null;
                Gdx.app.log("VideoScreen", "VideoPlayer liberado");
            }

            if (videoFrame != null) {
                videoFrame.dispose();
                videoFrame = null;
                Gdx.app.log("VideoScreen", "Textura de error liberada");
            }

            if (batch != null) {
                batch.dispose();
                batch = null;
                Gdx.app.log("VideoScreen", "SpriteBatch liberado");
            }

        } catch (Exception e) {
            Gdx.app.error("VideoScreen", "Error durante dispose", e);
        }

        Gdx.app.log("VideoScreen", "Recursos liberados correctamente");
    }

    // ====================== GETTERS PARA DIAGNÓSTICO ======================

    /**
     * Obtiene el ID del video actual.
     *
     * @return ID del video
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Obtiene el tiempo transcurrido de reproducción.
     *
     * @return Tiempo en segundos
     */
    public float getTiempoTranscurrido() {
        if (videoPlayer != null) {
            return (float) videoPlayer.getCurrentTimestamp();
        }
        return tiempoFade;
    }

    /**
     * Verifica si el video ha terminado.
     *
     * @return true si el video terminó, false en caso contrario
     */
    public boolean isVideoTerminado() {
        return videoTerminado;
    }

    /**
     * Verifica si hubo error cargando el video.
     *
     * @return true si hay error, false en caso contrario
     */
    public boolean isErrorCarga() {
        return errorCarga;
    }

    /**
     * Obtiene el progreso del fade actual (0 a 1).
     *
     * @return Progreso normalizado
     */
    public float getProgresoFade() {
        return alpha;
    }
}
