package io.github.javiergames.pieldelobo.GestorJuego;

import com.badlogic.gdx.Gdx;
import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.LobbyScreen;
import io.github.javiergames.pieldelobo.MainScreen;
import io.github.javiergames.pieldelobo.Personajes.PersonajeLobby;
import io.github.javiergames.pieldelobo.Personajes.Protagonista;
import io.github.javiergames.pieldelobo.DataBase.SaveSystem;

/**
 * Estado global del juego que persiste entre pantallas.
 * Se integra con el DatabaseManager.
 * GESTIÓN MEJORADA: Ahora maneja diálogos y videos pendientes automáticamente.
 *
 * @author Javier Gala
 * @version 2.2
 */
public class GameState {

    // ====================== INSTANCIA SINGLETON ======================
    private static GameState instance;

    // ====================== DEPENDENCIAS ======================
    private DatabaseManager db;

    // ====================== ESTADO ACTUAL DE LA SESIÓN ======================
    private String nivelActual = "";
    private float tiempoNivelActual = 0;
    private boolean enDialogo = false;
    private String ultimaDecisionId = "";
    private SaveSystem.SessionData sessionData;

    // ====================== NUEVO: GESTIÓN DE DIÁLOGOS Y VIDEOS PENDIENTES ======================
    private String dialogoPendienteId;
    private String nivelParaDialogoPendiente;
    private String videoPendienteId;

    // ====================== PARA MANEJO DE MAPAS ESPECÍFICOS ======================
    private String mapaACargar;
    private String nivelSeleccionadoId;

    // ====================== FLAG PARA SABER SI ESTAMOS REINICIANDO ======================
    private boolean reiniciandoNivel = false;

    // ====================== CONSTRUCTOR PRIVADO ======================
    private GameState() {
        db = DatabaseManager.getInstance();
    }

    public static GameState getInstance() {
        if (instance == null) {
            instance = new GameState();
        }
        return instance;
    }

    // ====================== NUEVO: MÉTODOS PARA DIÁLOGOS PENDIENTES ======================

    /**
     * Establece un diálogo pendiente para mostrar cuando el jugador vuelva al lobby
     * Se llama automáticamente al completar un nivel
     *
     * @param nivelCompletadoId ID del nivel que se acaba de completar
     * @param dialogoId ID del diálogo a mostrar
     */
    public void setDialogoPendiente(String nivelCompletadoId, String dialogoId) {
        if (dialogoId == null || dialogoId.isEmpty()) {
            Gdx.app.error("GameState", "Intento de establecer diálogo pendiente nulo");
            return;
        }

        this.dialogoPendienteId = dialogoId;
        this.nivelParaDialogoPendiente = nivelCompletadoId;

        Gdx.app.log("GameState", "📝 Diálogo pendiente establecido:");
        Gdx.app.log("GameState", "   • Después de completar: " + nivelCompletadoId);
        Gdx.app.log("GameState", "   • Diálogo a mostrar: " + dialogoId);
    }

    /**
     * Establece un video pendiente para reproducir después del diálogo
     */
    public void setVideoPendiente(String videoId) {
        this.videoPendienteId = videoId;

        if (videoId != null) {
            Gdx.app.log("GameState", "🎬 Video pendiente establecido: " + videoId);
        }
    }

    /**
     * Obtiene y limpia el diálogo pendiente
     * Se llama cuando el jugador entra al lobby
     *
     * @return ID del diálogo pendiente, o null si no hay
     */
    public String getDialogoPendiente() {
        String temp = dialogoPendienteId;

        if (temp != null) {
            Gdx.app.log("GameState", "📝 Obteniendo diálogo pendiente: " + temp);
            dialogoPendienteId = null;
            nivelParaDialogoPendiente = null;
        }

        return temp;
    }

    /**
     * Obtiene y limpia el video pendiente
     */
    public String getVideoPendiente() {
        String temp = videoPendienteId;

        if (temp != null) {
            Gdx.app.log("GameState", "🎬 Obteniendo video pendiente: " + temp);
            videoPendienteId = null;
        }

        return temp;
    }

    /**
     * Verifica si hay diálogo pendiente
     */
    public boolean tieneDialogoPendiente() {
        return dialogoPendienteId != null && !dialogoPendienteId.isEmpty();
    }

    /**
     * Verifica si hay video pendiente
     */
    public boolean tieneVideoPendiente() {
        return videoPendienteId != null && !videoPendienteId.isEmpty();
    }

    /**
     * Limpia todos los pendientes
     */
    public void limpiarPendientes() {
        dialogoPendienteId = null;
        nivelParaDialogoPendiente = null;
        videoPendienteId = null;

        Gdx.app.log("GameState", "🧹 Todos los pendientes limpiados");
    }

    // ====================== MÉTODOS DE CONVENIENCIA ======================

    /**
     * Inicia una nueva partida
     */
    public void nuevaPartida(String nombreJugador) {
        db.nuevaPartida(nombreJugador);
        resetEstadoSesion();
        limpiarPendientes();

        Gdx.app.log("GameState", "🆕 Nueva partida: " + nombreJugador);
    }

    public void setSessionData(SaveSystem.SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * Carga una partida existente
     */
    public void cargarPartida() {
        // El DatabaseManager ya carga automáticamente
        resetEstadoSesion();
        Gdx.app.log("GameState", "📂 Partida cargada");
    }

    /**
     * Guarda el estado actual
     */
    public void guardarPartida() {
        db.saveGame();
        Gdx.app.log("GameState", "💾 Partida guardada");
    }

    /**
     * Resetea el estado de la sesión actual (no los datos persistentes)
     */
    public void reset() {
        nivelActual = "";
        tiempoNivelActual = 0;
        enDialogo = false;
        ultimaDecisionId = "";
        limpiarPendientes();

        // También limpiar selección de mapa
        mapaACargar = null;
        nivelSeleccionadoId = null;
        reiniciandoNivel = false;

        Gdx.app.log("GameState", "🔄 Estado de sesión reiniciado");
    }

    private void resetEstadoSesion() {
        nivelActual = "";
        tiempoNivelActual = 0;
        enDialogo = false;
        ultimaDecisionId = "";
    }

    // ====================== MÉTODOS PARA MANEJO DE MAPAS ======================

    /**
     * Establece el mapa específico a cargar al entrar a MainScreen
     */
    public void setMapaACargar(String rutaMapa) {
        this.mapaACargar = rutaMapa;
        Gdx.app.log("GameState", "🗺️ Mapa a cargar establecido: " + rutaMapa);
    }

    /**
     * Obtiene el mapa a cargar (si fue establecido previamente)
     */
    public String getMapaACargar() {
        return mapaACargar;
    }

    /**
     * Establece el ID del nivel seleccionado
     */
    public void setNivelSeleccionadoId(String nivelId) {
        this.nivelSeleccionadoId = nivelId;
        Gdx.app.log("GameState", "🎯 Nivel seleccionado ID: " + nivelId);
    }

    /**
     * Obtiene el ID del nivel seleccionado
     */
    public String getNivelSeleccionadoId() {
        return nivelSeleccionadoId;
    }

    /**
     * Limpia la selección de mapa después de usarla
     */
    public void limpiarSeleccionMapa() {
        // Solo limpiar si no estamos reiniciando
        if (!reiniciandoNivel) {
            this.mapaACargar = null;
            this.nivelSeleccionadoId = null;
            Gdx.app.log("GameState", "🗺️ Selección de mapa limpiada");
        }
    }

    /**
     * Determina la ruta del mapa basada en el ID del nivel
     */
    public String obtenerRutaMapaPorNivelId(String nivelId) {
        if (nivelId == null || nivelId.isEmpty()) {
            return "Tiled/nivel_villa.tmx";
        }

        String idLower = nivelId.toLowerCase();

        Gdx.app.log("GameState", "🔍 Buscando mapa para nivel ID: " + nivelId);

        // Mapeo de IDs a mapas Tiled
        switch (idLower) {
            case "nivel_1":
            case "nivel_villa":
            case "La Villa":
                Gdx.app.log("GameState", "   → Mapeado a: Tiled/nivel_villa.tmx");
                return "Tiled/nivel_villa.tmx";

            case "nivel_2":
            case "jb-32.tmx":
            case "Las Columnas":
                Gdx.app.log("GameState", "   → Mapeado a: Tiled/jb-32.tmx");
                return "Tiled/jb-32.tmx";

            case "nivel_3":
            case "mylevel1":
            case "Las Cavernas":
                Gdx.app.log("GameState", "   → Mapeado a: Tiled/mylevel1.tmx");
                return "Tiled/mylevel1.tmx";

            case "nivel_4":
            case "level25":
            case "La Luna":
                Gdx.app.log("GameState", "   → Mapeado a: Tiled/level25.tmx");
                return "Tiled/level25.tmx";

            case "nivel_5":
            case "magicland":
            case "El Cartillo":
                Gdx.app.log("GameState", "   → Mapeado a: Tiled/MagicLand.tmx");
                return "Tiled/MagicLand.tmx";

            default:
                Gdx.app.log("GameState", "⚠️ ID de nivel desconocido: '" + nivelId + "', usando mapa por defecto");
                return "Tiled/nivel_villa.tmx";
        }
    }

    /**
     * Verifica si un archivo de mapa existe
     */
    public boolean existeMapa(String rutaMapa) {
        try {
            boolean existe = Gdx.files.internal(rutaMapa).exists();
            Gdx.app.log("GameState", "🔍 Verificando mapa: " + rutaMapa + " → " + (existe ? "✅ EXISTE" : "❌ NO EXISTE"));
            return existe;
        } catch (Exception e) {
            Gdx.app.error("GameState", "Error verificando mapa: " + rutaMapa, e);
            return false;
        }
    }

    /**
     * Obtiene el nombre amigable del nivel
     */
    public String obtenerNombreNivel(String nivelId) {
        if (nivelId == null || nivelId.isEmpty()) {
            return "La Villa";
        }

        String idLower = nivelId.toLowerCase();

        switch (idLower) {
            case "nivel_1":
            case "nivel_villa":
                return "La Villa";

            case "nivel_2":
            case "jb-32.tmx":
                return "Las Columnas";

            case "nivel_3":
            case "mylevel1":
                return "Las Cavernas";

            case "nivel_4":
            case "level25":
                return "La Luna";

            case "nivel_5":
            case "magicland":
                return "El Castillo";

            default:
                return "Nivel Desconocido";
        }
    }

    // ====================== MÉTODOS PARA REINICIOS ======================

    /**
     * Marca que estamos reiniciando el nivel
     */
    public void setReiniciandoNivel(boolean reiniciando) {
        this.reiniciandoNivel = reiniciando;
        Gdx.app.log("GameState", "🔄 Reiniciando nivel: " + reiniciando);
    }

    /**
     * Verifica si estamos reiniciando el nivel
     */
    public boolean isReiniciandoNivel() {
        return reiniciandoNivel;
    }

    // ====================== GETTERS Y SETTERS ======================

    public void setNivelActual(String nivelId) {
        this.nivelActual = nivelId;
        Gdx.app.log("GameState", "🎮 Nivel actual establecido: " + nivelId);
    }

    public String getNivelActual() {
        return nivelActual;
    }

    public void actualizarTiempoNivel(float delta) {
        tiempoNivelActual += delta;
    }

    public float getTiempoNivelActual() {
        return tiempoNivelActual;
    }

    public void setEnDialogo(boolean enDialogo) {
        this.enDialogo = enDialogo;

        if (enDialogo) {
            Gdx.app.log("GameState", "💬 Entrando en diálogo");
        } else {
            Gdx.app.log("GameState", "💬 Saliendo de diálogo");
        }
    }

    public boolean isEnDialogo() {
        return enDialogo;
    }

    public void setUltimaDecision(String decisionId) {
        this.ultimaDecisionId = decisionId;

        if (decisionId != null && !decisionId.isEmpty()) {
            Gdx.app.log("GameState", "🤔 Última decisión: " + decisionId);
        }
    }

    public String getUltimaDecision() {
        return ultimaDecisionId;
    }

    // ====================== DELEGACIÓN AL DATABASE MANAGER ======================

    public boolean isNivelDesbloqueado(String nivelId) {
        boolean desbloqueado = db.isNivelDesbloqueado(nivelId);
        Gdx.app.debug("GameState", "Verificando si nivel desbloqueado " + nivelId + ": " + desbloqueado);
        return desbloqueado;
    }

    public boolean isNivelCompletado(String nivelId) {
        boolean completado = db.isNivelCompletado(nivelId);
        Gdx.app.debug("GameState", "Verificando si nivel completado " + nivelId + ": " + completado);
        return completado;
    }

    public void desbloquearNivel(String nivelId) {
        Gdx.app.log("GameState", "🔓 Desbloqueando nivel: " + nivelId);
        db.desbloquearNivel(nivelId);
    }

    /**
     * Completa un nivel usando el SistemaProgresion
     * Este método ahora coordina todo el proceso automático
     */
    public void completarNivel(String nivelId, float tiempo) {
        Gdx.app.log("GameState", "=== 🎮 COMPLETANDO NIVEL ===");
        Gdx.app.log("GameState", "   • Nivel: " + nivelId);
        Gdx.app.log("GameState", "   • Tiempo: " + String.format("%.1f", tiempo) + "s");

        // Usar el SistemaProgresion para manejar todo automáticamente
        SistemaProgresion progresion = SistemaProgresion.getInstance();
        progresion.onNivelCompletado(nivelId);

        // Resetear tiempo del nivel actual
        tiempoNivelActual = 0;

        // Resetear flag de reinicio
        reiniciandoNivel = false;

        Gdx.app.log("GameState", "✅ Nivel " + nivelId + " procesado correctamente");

        // Mostrar información sobre qué hacer después
        String siguienteMision = progresion.getNpcSiguienteMision();
        if (siguienteMision != null) {
            Gdx.app.log("GameState", "⚠️ ATENCIÓN: Habla con " + siguienteMision + " en el lobby");
            Gdx.app.log("GameState", "   • Diálogo: " + progresion.getDialogoSiguienteMision());
            Gdx.app.log("GameState", "   • Video: " + progresion.getVideoSiguienteMision());
        }
    }

    public void registrarDecision(String decisionId, String texto, String impacto,
                                  java.util.HashMap<String, Object> consecuencias) {
        db.registrarDecision(decisionId, nivelActual, texto, impacto, consecuencias);
        ultimaDecisionId = decisionId;

        Gdx.app.log("GameState", "📝 Decisión registrada: " + decisionId);
    }

    public void marcarVideoVisto(String videoId) {
        db.marcarVideoComoVisto(videoId);
        Gdx.app.log("GameState", "🎬 Video marcado como visto: " + videoId);
    }

    public String getReporteProgreso() {
        return db.getReporteProgreso();
    }

    public float getProgresoTotal() {
        return db.getProgresoTotal();
    }

    public DatabaseManager.GameData getDatosCompletos() {
        return db.getGameData();
    }

    public SaveSystem.SessionData getSessionData() {
        SaveSystem.SessionData data = new SaveSystem.SessionData();

        // Rellenar con datos actuales
        if (Screens.juego != null && Screens.juego.getScreen() != null) {
            data.currentScreen = Screens.juego.getScreen().getClass().getSimpleName();
        }
        data.currentLevel = getNivelActual();

        // Posición del jugador (depende de la pantalla)
        if (Screens.juego != null && Screens.juego.getScreen() instanceof LobbyScreen) {
            LobbyScreen lobby = (LobbyScreen) Screens.juego.getScreen();
            PersonajeLobby jugador = lobby.getJugador();
            if (jugador != null) {
                data.playerPosition[0] = jugador.getX();
                data.playerPosition[1] = jugador.getY();
            }
        } else if (Screens.juego != null && Screens.juego.getScreen() instanceof MainScreen) {
            MainScreen main = (MainScreen) Screens.juego.getScreen();
            Protagonista jugador = main.getJugador();
            if (jugador != null) {
                data.playerPosition[0] = jugador.getX();
                data.playerPosition[1] = jugador.getY();
            }
        }

        // Variables de sesión
        data.sessionVariables.put("ultimaDecision", ultimaDecisionId);
        data.sessionVariables.put("enDialogo", enDialogo);
        data.sessionVariables.put("tiempoNivelActual", tiempoNivelActual);
        data.sessionVariables.put("reiniciandoNivel", reiniciandoNivel);

        // NUEVO: Añadir información de pendientes
        if (tieneDialogoPendiente()) {
            data.sessionVariables.put("dialogoPendienteId", dialogoPendienteId);
            data.sessionVariables.put("nivelParaDialogoPendiente", nivelParaDialogoPendiente);
        }
        if (tieneVideoPendiente()) {
            data.sessionVariables.put("videoPendienteId", videoPendienteId);
        }

        return data;
    }

    /**
     * Restaura el estado de sesión
     */
    public void restoreSession() {
        if (sessionData == null) return;

        Gdx.app.log("GameState", "🔄 Restaurando sesión guardada");

        // Restaurar variables básicas
        if (sessionData.sessionVariables.containsKey("ultimaDecision")) {
            Object ultimaDecisionObj = sessionData.sessionVariables.get("ultimaDecision");
            if (ultimaDecisionObj instanceof String) {
                ultimaDecisionId = (String) ultimaDecisionObj;
            }
        }

        if (sessionData.sessionVariables.containsKey("enDialogo")) {
            Object enDialogoObj = sessionData.sessionVariables.get("enDialogo");
            if (enDialogoObj instanceof Boolean) {
                enDialogo = (Boolean) enDialogoObj;
            }
        }

        if (sessionData.sessionVariables.containsKey("tiempoNivelActual")) {
            Object tiempoObj = sessionData.sessionVariables.get("tiempoNivelActual");
            if (tiempoObj instanceof Number) {
                tiempoNivelActual = ((Number) tiempoObj).floatValue();
            }
        }

        if (sessionData.sessionVariables.containsKey("reiniciandoNivel")) {
            Object reiniciandoObj = sessionData.sessionVariables.get("reiniciandoNivel");
            if (reiniciandoObj instanceof Boolean) {
                reiniciandoNivel = (Boolean) reiniciandoObj;
            }
        }

        // NUEVO: Restaurar pendientes
        if (sessionData.sessionVariables.containsKey("dialogoPendienteId")) {
            Object dialogoObj = sessionData.sessionVariables.get("dialogoPendienteId");
            if (dialogoObj instanceof String) {
                dialogoPendienteId = (String) dialogoObj;
            }
        }

        if (sessionData.sessionVariables.containsKey("nivelParaDialogoPendiente")) {
            Object nivelObj = sessionData.sessionVariables.get("nivelParaDialogoPendiente");
            if (nivelObj instanceof String) {
                nivelParaDialogoPendiente = (String) nivelObj;
            }
        }

        if (sessionData.sessionVariables.containsKey("videoPendienteId")) {
            Object videoObj = sessionData.sessionVariables.get("videoPendienteId");
            if (videoObj instanceof String) {
                videoPendienteId = (String) videoObj;
            }
        }

        Gdx.app.log("GameState", "✅ Sesión restaurada:");
        if (tieneDialogoPendiente()) {
            Gdx.app.log("GameState", "   • Diálogo pendiente: " + dialogoPendienteId);
        }
        if (tieneVideoPendiente()) {
            Gdx.app.log("GameState", "   • Video pendiente: " + videoPendienteId);
        }
    }

    /**
     * Log del estado actual (para debug)
     */
    public void logEstado() {
        Gdx.app.log("GameState", "=== 📊 ESTADO ACTUAL ===");
        Gdx.app.log("GameState", "Nivel actual: " + nivelActual);
        Gdx.app.log("GameState", "Mapa a cargar: " + (mapaACargar != null ? mapaACargar : "Ninguno"));
        Gdx.app.log("GameState", "Nivel seleccionado ID: " + (nivelSeleccionadoId != null ? nivelSeleccionadoId : "Ninguno"));
        Gdx.app.log("GameState", "Tiempo nivel actual: " + String.format("%.1f", tiempoNivelActual) + "s");
        Gdx.app.log("GameState", "En diálogo: " + enDialogo);
        Gdx.app.log("GameState", "Última decisión: " + ultimaDecisionId);
        Gdx.app.log("GameState", "Reiniciando nivel: " + reiniciandoNivel);
        Gdx.app.log("GameState", "Diálogo pendiente: " + (tieneDialogoPendiente() ? dialogoPendienteId : "Ninguno"));
        Gdx.app.log("GameState", "Video pendiente: " + (tieneVideoPendiente() ? videoPendienteId : "Ninguno"));
        Gdx.app.log("GameState", "Progreso total: " + String.format("%.1f", getProgresoTotal()) + "%");
        Gdx.app.log("GameState", "=================================");
    }
}
