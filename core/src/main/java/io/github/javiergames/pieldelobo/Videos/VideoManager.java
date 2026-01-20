package io.github.javiergames.pieldelobo.Videos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

/**
 * Manager centralizado para gestionar videos del juego.
 * Mapea IDs de video a rutas de archivo y maneja la disponibilidad.
 *
 * @author JavierGames
 * @version 1.0
 */
public class VideoManager {

    // Instancia única (Singleton)
    private static VideoManager instance;

    // Mapa de rutas de videos: ID -> Ruta del archivo
    private Map<String, String> videoPaths;

    /**
     * Constructor privado para patrón Singleton.
     */
    private VideoManager() {
        videoPaths = new HashMap<>();
        cargarVideos();
    }

    /**
     * Obtiene la instancia única de VideoManager.
     *
     * @return Instancia de VideoManager
     */
    public static VideoManager getInstance() {
        if (instance == null) {
            instance = new VideoManager();
        }
        return instance;
    }

    /**
     * Carga todos los videos disponibles en el mapa.
     * Organizados por número de puerta y opción.
     */
    private void cargarVideos() {

        // Video introductorio para nueva partida
        videoPaths.put("intro", "videos/video_intro.ogg");  // Ruta del video introductorio
        // ====================== VIDEOS PUERTA 1 ======================
        videoPaths.put("video1_1", "Videos/video1_1.ogg");  // Abuelo del agente
        videoPaths.put("video1_2", "Videos/video1_2.ogg");  // Genio del nuevo
        videoPaths.put("video1_3", "Videos/video1_3.ogg");  // Criptología ibérica
        videoPaths.put("video1_4", "Videos/video1_4.ogg");  // Trasgu escapando

        // ====================== VIDEOS PUERTA 2 ======================
        videoPaths.put("video2_1", "Videos/video2_1.ogg");  // Siguiente misión
        videoPaths.put("video2_2", "Videos/video2_2.ogg");  // Morcilla vegana

        // ====================== VIDEOS PUERTA 3 ======================
        videoPaths.put("video3_1", "Videos/video3_1.ogg");  // Datos del nivel 3
        videoPaths.put("video3_2", "Videos/video3_2.ogg");  // Más tarde
        videoPaths.put("video3_3", "Videos/video3_3.ogg");  // Bebé dragón
        videoPaths.put("video3_4", "Videos/video3_4.ogg");  // WhatsApp

        // ====================== VIDEOS PUERTA 4 ======================
        videoPaths.put("video4_1", "Videos/video4_1.ogg");  // Seguir avanzando
        videoPaths.put("video4_2", "Videos/video4_2.ogg");  // Historia de puertas
        videoPaths.put("video4_3", "Videos/video4_3.ogg");  // Apuesta ganada
        videoPaths.put("video4_4", "Videos/video4_4.ogg");  // Comisión justa

        // ====================== VIDEOS PUERTA 5 ======================
        videoPaths.put("video5_1", "Videos/video5_1.ogg");  // Historia de la zona
        videoPaths.put("video5_2", "Videos/video5_2.ogg");  // Historia de Romasanta
        videoPaths.put("video5_3", "Videos/video5_3.ogg");  // Simulación Java
        videoPaths.put("video5_4", "Videos/video5_4.ogg");  // Antisistema matrix

        // Video por defecto (en caso de error)
        videoPaths.put("default", "videos/Video Final.ogg");

        Gdx.app.log("VideoManager", "Cargados " + videoPaths.size() + " videos");

        // Verificar archivos existentes
        verificarVideosExisten();
    }

    /**
     * Verifica que los archivos de video existan físicamente.
     */
    private void verificarVideosExisten() {
        int existentes = 0;
        int faltantes = 0;

        for (Map.Entry<String, String> entry : videoPaths.entrySet()) {
            String videoId = entry.getKey();
            String ruta = entry.getValue();

            FileHandle file = Gdx.files.internal(ruta);
            String rutaAbsoluta = file.path();
            String rutaReal = file.file().getAbsolutePath(); // Esta línea puede ayudar

            if (file.exists()) {
                existentes++;
                Gdx.app.debug("VideoManager", "✓ Video encontrado: " + videoId +
                    " -> Ruta: " + ruta +
                    " -> Absoluta: " + rutaAbsoluta +
                    " -> Real: " + rutaReal +
                    " -> Tamaño: " + file.length() + " bytes");
            } else {
                faltantes++;
                Gdx.app.error("VideoManager", "✗ Video NO encontrado: " + videoId +
                    " -> Ruta: " + ruta +
                    " -> Absoluta: " + rutaAbsoluta +
                    " -> Real: " + rutaReal);
            }
        }

        Gdx.app.log("VideoManager",
            "Verificación completada: " + existentes + " existentes, " + faltantes + " faltantes");
    }

    /**
     * Obtiene la ruta completa del video por su ID.
     *
     * @param videoId ID del video (ej: "video1_1", "video2_2")
     * @return Ruta del archivo de video
     */
    public String getVideoPath(String videoId) {
        if (videoId == null || videoId.isEmpty()) {
            Gdx.app.error("VideoManager", "ID de video nulo o vacío");
            return videoPaths.get("default");
        }

        String path = videoPaths.get(videoId);
        if (path == null) {
            Gdx.app.error("VideoManager", "Video no encontrado en mapa: " + videoId);
            return videoPaths.get("default");
        }

        return path;
    }

    /**
     * Verifica si un video específico existe en el mapa.
     *
     * @param videoId ID del video a verificar
     * @return true si el video existe, false en caso contrario
     */
    public boolean hasVideo(String videoId) {
        return videoPaths.containsKey(videoId);
    }

    /**
     * Verifica si el archivo de video existe físicamente.
     *
     * @param videoId ID del video a verificar
     * @return true si el archivo existe, false en caso contrario
     */
    public boolean videoFileExists(String videoId) {
        String path = getVideoPath(videoId);
        return Gdx.files.internal(path).exists();
    }

    /**
     * Obtiene todos los videos disponibles.
     *
     * @return Copia del mapa de videos
     */
    public Map<String, String> getAllVideos() {
        return new HashMap<>(videoPaths);
    }

    /**
     * Obtiene el número total de videos registrados.
     *
     * @return Número de videos en el mapa
     */
    public int getTotalVideos() {
        return videoPaths.size();
    }

    /**
     * Obtiene información de diagnóstico de un video específico.
     *
     * @param videoId ID del video
     * @return String con información del video
     */
    public String getVideoInfo(String videoId) {
        if (!hasVideo(videoId)) {
            return "Video no encontrado: " + videoId;
        }

        String path = getVideoPath(videoId);
        boolean exists = videoFileExists(videoId);

        return String.format("Video %s: %s (%s)",
            videoId, path, exists ? "EXISTE" : "NO EXISTE");
    }
}
