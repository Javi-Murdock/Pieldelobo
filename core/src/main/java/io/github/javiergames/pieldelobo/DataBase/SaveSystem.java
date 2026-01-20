package io.github.javiergames.pieldelobo.DataBase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.GestorJuego.Screens;
import io.github.javiergames.pieldelobo.LobbyScreen;
import io.github.javiergames.pieldelobo.MainScreen;
import io.github.javiergames.pieldelobo.MenuScreen;

/**
 * Sistema completo de guardado manual de partidas.
 * Permite múltiples slots de guardado con serialización JSON.
 * Integra datos del juego, configuración y sesión actual.
 *
 * @author Javier Gala
 * @version 2.0
 * @see DatabaseManager
 * @see SessionData
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SaveSystem {

    private static SaveSystem instance;
    private Json json;
    private Preferences prefs;

    // Configuración
    /**
     * Número máximo de slots de guardado disponibles.
     * Valor constante: 6 slots.
     */
    private static final int MAX_SAVE_SLOTS = 6;
    /**
     * Prefijo utilizado para las claves de guardado en preferencias.
     */
    private static final String SAVE_PREFIX = "save_slot_";

    private SaveSystem() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setTypeName(null);

        // Configurar serializadores para HashMap
        configurarSerializadoresHashMap();

        prefs = Gdx.app.getPreferences("PielDeLobo_Saves");
    }

    /**
     * Configura serializadores personalizados para manejar objetos específicos
     */
    private void configurarSerializadoresHashMap() {
        // Serializador para HashMap
        json.setSerializer(HashMap.class, new Json.Serializer<HashMap>() {
            @Override
            public void write(Json json, HashMap object, Class knownType) {
                json.writeObjectStart();
                for (Object key : object.keySet()) {
                    Object value = object.get(key);

                    // NUEVO: Manejar tipos específicos de DatabaseManager
                    if (value instanceof DatabaseManager.LevelState) {
                        DatabaseManager.LevelState nivel = (DatabaseManager.LevelState) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("nivelId", nivel.nivelId);
                        json.writeValue("nombre", nivel.nombre);
                        json.writeValue("estado", nivel.estado.name());
                        json.writeValue("completado", nivel.completado);
                        json.writeValue("mejorTiempo", nivel.mejorTiempo);
                        json.writeValue("muertes", nivel.muertes);
                        json.writeValue("tieneRecompensa", nivel.tieneRecompensa);
                        json.writeValue("fechaCompletado", nivel.fechaCompletado);
                        json.writeObjectEnd();
                    } else if (value instanceof DatabaseManager.DecisionRecord) {
                        DatabaseManager.DecisionRecord decision = (DatabaseManager.DecisionRecord) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("decisionId", decision.decisionId);
                        json.writeValue("nivelId", decision.nivelId);
                        json.writeValue("textoDecision", decision.textoDecision);
                        json.writeValue("impacto", decision.impacto);
                        json.writeValue("fecha", decision.fecha);
                        json.writeValue("consecuencias", decision.consecuencias);
                        json.writeObjectEnd();
                    } else if (value instanceof DatabaseManager.VideoRecord) {
                        DatabaseManager.VideoRecord video = (DatabaseManager.VideoRecord) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("videoId", video.videoId);
                        json.writeValue("ruta", video.ruta);
                        json.writeValue("descripcion", video.descripcion);
                        json.writeValue("visto", video.visto);
                        json.writeValue("vecesVisto", video.vecesVisto);
                        json.writeValue("ultimaVez", video.ultimaVez);
                        json.writeObjectEnd();
                    } else if (value instanceof DatabaseManager.GameStats) {
                        DatabaseManager.GameStats stats = (DatabaseManager.GameStats) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("totalDecisiones", stats.totalDecisiones);
                        json.writeValue("decisionesBuenas", stats.decisionesBuenas);
                        json.writeValue("decisionesMalas", stats.decisionesMalas);
                        json.writeValue("npcsHablados", stats.npcsHablados);
                        json.writeValue("objetosRecolectados", stats.objetosRecolectados);
                        json.writeValue("enemigosDerrotados", stats.enemigosDerrotados);
                        json.writeValue("finalesDesbloqueados", stats.finalesDesbloqueados);
                        json.writeObjectEnd();
                    } else if (value instanceof DatabaseManager.UserData) {
                        DatabaseManager.UserData user = (DatabaseManager.UserData) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("nombre", user.nombre);
                        json.writeValue("partidasJugadas", user.partidasJugadas);
                        json.writeValue("tiempoTotalJuego", user.tiempoTotalJuego);
                        json.writeValue("fechaUltimaPartida", user.fechaUltimaPartida);
                        json.writeValue("configuracion", user.configuracion);
                        json.writeObjectEnd();
                    } else if (value instanceof DatabaseManager.GameData) {
                        DatabaseManager.GameData gameData = (DatabaseManager.GameData) value;
                        json.writeObjectStart(key.toString());
                        json.writeValue("usuario", gameData.usuario);
                        json.writeValue("niveles", gameData.niveles);
                        json.writeValue("decisiones", gameData.decisiones);
                        json.writeValue("videosVistos", gameData.videosVistos);
                        json.writeValue("estadisticas", gameData.estadisticas);
                        json.writeObjectEnd();
                    } else {
                        // Para tipos básicos
                        json.writeValue(key.toString(), value);
                    }
                }
                json.writeObjectEnd();
            }

            @Override
            public HashMap read(Json json, JsonValue jsonData, Class type) {
                HashMap<String, Object> map = new HashMap<>();
                if (jsonData != null) {
                    for (JsonValue child = jsonData.child; child != null; child = child.next) {
                        Object value;

                        if (child.isObject()) {
                            // Verificar tipo de objeto
                            if (child.has("nivelId") && child.has("estado")) {
                                value = leerLevelStateDesdeJson(child);
                            } else if (child.has("decisionId") && child.has("nivelId")) {
                                value = leerDecisionRecordDesdeJson(child);
                            } else if (child.has("videoId") && child.has("ruta")) {
                                value = leerVideoRecordDesdeJson(child);
                            } else if (child.has("totalDecisiones") && child.has("decisionesBuenas")) {
                                value = leerGameStatsDesdeJson(child);
                            } else if (child.has("nombre") && child.has("partidasJugadas")) {
                                value = leerUserDataDesdeJson(child);
                            } else if (child.has("usuario") && child.has("niveles")) {
                                value = leerGameDataDesdeJson(child);
                            } else {
                                // Objeto HashMap normal
                                value = json.readValue(HashMap.class, child);
                            }
                        } else if (child.isString()) {
                            value = child.asString();
                        } else if (child.isBoolean()) {
                            value = child.asBoolean();
                        } else if (child.isDouble()) {
                            value = child.asDouble();
                        } else if (child.isLong()) {
                            value = child.asLong();
                        } else if (child.isArray()) {
                            value = json.readValue(Object[].class, child);
                        } else {
                            value = child.asString();
                        }
                        map.put(child.name, value);
                    }
                }
                return map;
            }
        });
    }

    // ====================== MÉTODOS AUXILIARES DE LECTURA ======================

    /**
     * Lee LevelState desde JsonValue
     */
    private DatabaseManager.LevelState leerLevelStateDesdeJson(JsonValue jsonData) {
        DatabaseManager.LevelState nivel = new DatabaseManager.LevelState();
        nivel.nivelId = jsonData.getString("nivelId");
        nivel.nombre = jsonData.getString("nombre", "");

        String estadoStr = jsonData.getString("estado", "BLOQUEADO");
        try {
            nivel.estado = DatabaseManager.LevelState.EstadoNivel.valueOf(estadoStr);
        } catch (Exception e) {
            nivel.estado = DatabaseManager.LevelState.EstadoNivel.BLOQUEADO;
        }

        nivel.completado = jsonData.getBoolean("completado", false);
        nivel.mejorTiempo = jsonData.getFloat("mejorTiempo", 0);
        nivel.muertes = jsonData.getInt("muertes", 0);
        nivel.tieneRecompensa = jsonData.getBoolean("tieneRecompensa", false);
        nivel.fechaCompletado = jsonData.getString("fechaCompletado", "");

        return nivel;
    }

    /**
     * Lee DecisionRecord desde JsonValue
     */
    private DatabaseManager.DecisionRecord leerDecisionRecordDesdeJson(JsonValue jsonData) {
        DatabaseManager.DecisionRecord decision = new DatabaseManager.DecisionRecord();
        decision.decisionId = jsonData.getString("decisionId");
        decision.nivelId = jsonData.getString("nivelId");
        decision.textoDecision = jsonData.getString("textoDecision", "");
        decision.impacto = jsonData.getString("impacto", "");
        decision.fecha = jsonData.getString("fecha", "");

        // Las consecuencias ya deberían ser un HashMap
        JsonValue consecuenciasVal = jsonData.get("consecuencias");
        if (consecuenciasVal != null && consecuenciasVal.isObject()) {
            // Esto leerá correctamente como HashMap<String, Object>
            decision.consecuencias = new HashMap<>();
        }

        return decision;
    }

    /**
     * Lee VideoRecord desde JsonValue
     */
    private DatabaseManager.VideoRecord leerVideoRecordDesdeJson(JsonValue jsonData) {
        DatabaseManager.VideoRecord video = new DatabaseManager.VideoRecord();
        video.videoId = jsonData.getString("videoId");
        video.ruta = jsonData.getString("ruta", "");
        video.descripcion = jsonData.getString("descripcion", "");
        video.visto = jsonData.getBoolean("visto", false);
        video.vecesVisto = jsonData.getInt("vecesVisto", 0);
        video.ultimaVez = jsonData.getString("ultimaVez", "");

        return video;
    }

    /**
     * Lee GameStats desde JsonValue
     */
    private DatabaseManager.GameStats leerGameStatsDesdeJson(JsonValue jsonData) {
        DatabaseManager.GameStats stats = new DatabaseManager.GameStats();
        stats.totalDecisiones = jsonData.getInt("totalDecisiones", 0);
        stats.decisionesBuenas = jsonData.getInt("decisionesBuenas", 0);
        stats.decisionesMalas = jsonData.getInt("decisionesMalas", 0);
        stats.npcsHablados = jsonData.getInt("npcsHablados", 0);
        stats.objetosRecolectados = jsonData.getInt("objetosRecolectados", 0);
        stats.enemigosDerrotados = jsonData.getInt("enemigosDerrotados", 0);

        // Leer finales desbloqueados
        JsonValue finalesVal = jsonData.get("finalesDesbloqueados");
        if (finalesVal != null && finalesVal.isObject()) {
            stats.finalesDesbloqueados = new HashMap<>();
            for (JsonValue finalVal = finalesVal.child; finalVal != null; finalVal = finalVal.next) {
                stats.finalesDesbloqueados.put(finalVal.name, finalVal.asInt());
            }
        }

        return stats;
    }

    /**
     * Lee UserData desde JsonValue
     */
    private DatabaseManager.UserData leerUserDataDesdeJson(JsonValue jsonData) {
        DatabaseManager.UserData user = new DatabaseManager.UserData();
        user.nombre = jsonData.getString("nombre", "Jugador");
        user.partidasJugadas = jsonData.getInt("partidasJugadas", 0);
        user.tiempoTotalJuego = jsonData.getInt("tiempoTotalJuego", 0);
        user.fechaUltimaPartida = jsonData.getString("fechaUltimaPartida", "");

        // Leer configuración
        JsonValue configVal = jsonData.get("configuracion");
        if (configVal != null && configVal.isObject()) {
            user.configuracion = new HashMap<>();
            for (JsonValue configItem = configVal.child; configItem != null; configItem = configItem.next) {
                user.configuracion.put(configItem.name, configItem.asString());
            }
        }

        return user;
    }

    /**
     * Lee GameData desde JsonValue
     */
    private DatabaseManager.GameData leerGameDataDesdeJson(JsonValue jsonData) {
        DatabaseManager.GameData gameData = new DatabaseManager.GameData();

        // Leer usuario
        JsonValue usuarioVal = jsonData.get("usuario");
        if (usuarioVal != null && usuarioVal.isObject()) {
            gameData.usuario = leerUserDataDesdeJson(usuarioVal);
        }

        // Leer niveles
        JsonValue nivelesVal = jsonData.get("niveles");
        if (nivelesVal != null && nivelesVal.isObject()) {
            gameData.niveles = new HashMap<>();
            for (JsonValue nivelVal = nivelesVal.child; nivelVal != null; nivelVal = nivelVal.next) {
                DatabaseManager.LevelState nivel = leerLevelStateDesdeJson(nivelVal);
                if (nivel != null && nivel.nivelId != null) {
                    gameData.niveles.put(nivel.nivelId, nivel);
                }
            }
        }

        // Leer decisiones
        JsonValue decisionesVal = jsonData.get("decisiones");
        if (decisionesVal != null && decisionesVal.isObject()) {
            gameData.decisiones = new HashMap<>();
            for (JsonValue decisionVal = decisionesVal.child; decisionVal != null; decisionVal = decisionVal.next) {
                DatabaseManager.DecisionRecord decision = leerDecisionRecordDesdeJson(decisionVal);
                if (decision != null && decision.decisionId != null) {
                    gameData.decisiones.put(decision.decisionId, decision);
                }
            }
        }

        // Leer videos
        JsonValue videosVal = jsonData.get("videosVistos");
        if (videosVal != null && videosVal.isObject()) {
            gameData.videosVistos = new HashMap<>();
            for (JsonValue videoVal = videosVal.child; videoVal != null; videoVal = videoVal.next) {
                DatabaseManager.VideoRecord video = leerVideoRecordDesdeJson(videoVal);
                if (video != null && video.videoId != null) {
                    gameData.videosVistos.put(video.videoId, video);
                }
            }
        }

        // Leer estadísticas
        JsonValue statsVal = jsonData.get("estadisticas");
        if (statsVal != null && statsVal.isObject()) {
            gameData.estadisticas = leerGameStatsDesdeJson(statsVal);
        }

        return gameData;
    }

    public static SaveSystem getInstance() {
        if (instance == null) {
            instance = new SaveSystem();
        }
        return instance;
    }

    // ====================== CLASES DE DATOS ======================

    /**
     * Datos completos de una partida guardada.
     * Incluye información del juego, configuración y sesión.
     */
    public static class SaveGameData {

        /** Nombre descriptivo del guardado */
        public String saveName;
        /** Marca de tiempo del guardado */
        public String timestamp;
        /** Tiempo total de juego formateado */
        public String playTime;
        /** Número de slot donde se guardó */
        public int saveSlot;
        /** Ruta de la captura de pantalla asociada */
        public String screenshotPath;
        /** Datos persistentes del juego */
        public DatabaseManager.GameData gameData;
        /** Configuración del jugador */
        public ConfigData configData;
        /** Estado de la sesión actual */
        public SessionData sessionData;
        /** Nivel del jugador calculado */
        public int level;
        /** Progreso porcentual del juego */
        public float progress;
        /** Ubicación actual del jugador */
        public String location;

        public SaveGameData() {
            this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            this.location = "Desconocido";
        }
    }

    /**
     * Datos de configuración del jugador
     */
    /**
     * Datos de configuración del jugador.
     * Incluye ajustes de video, audio y controles.
     */
    public static class ConfigData {
        /** Resolución de pantalla (ej: "1920x1080") */
        public String resolution;
        /** Modo pantalla completa activo/inactivo */
        public boolean fullscreen;
        /** Volumen general del juego (0.0 a 1.0) */
        public float volumeMaster;
        /** Volumen de la música (0.0 a 1.0) */
        public float volumeMusic;
        /** Volumen de efectos de sonido (0.0 a 1.0) */
        public float volumeEffects;
        /** Idioma seleccionado */
        public String language;
        /** Mapeo de teclas personalizadas */
        public HashMap<String, String> keybindings = new HashMap<>();

        public ConfigData() {
            resolution = "800x600";
            fullscreen = false;
            volumeMaster = 0.7f;
            volumeMusic = 0.5f;
            volumeEffects = 0.8f;
            language = "es";
        }
    }

    /**
     * Datos de la sesión actual (qué estaba haciendo el jugador)
     */
    public static class SessionData {
        public String currentScreen;
        public String currentLevel;
        public String location;
        public float[] playerPosition;
        public float[] cameraPosition;
        public String[] activeNPCs;
        public HashMap<String, Object> sessionVariables = new HashMap<>();

        public SessionData() {
            playerPosition = new float[]{0, 0};
            cameraPosition = new float[]{0, 0};
            activeNPCs = new String[0];
            location = "Desconocido";
            currentScreen = "";
            currentLevel = "";
        }
    }

    // ====================== MÉTODOS PRINCIPALES ======================

    /**
     * Guarda la partida actual en un slot específico.
     *
     * @param slot Número de slot (1 a MAX_SAVE_SLOTS)
     * @param saveName Nombre descriptivo del guardado
     * @return true si el guardado fue exitoso, false en caso contrario
     * @throws IllegalArgumentException si el slot está fuera de rango
     */
    public boolean saveGame(int slot, String saveName) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            Gdx.app.error("SaveSystem", "Slot inválido: " + slot);
            return false;
        }

        try {
            SaveGameData saveData = collectAllData();
            saveData.saveSlot = slot;
            saveData.saveName = (saveName != null && !saveName.isEmpty()) ?
                saveName : "Partida " + slot;

            String jsonData = json.prettyPrint(saveData);

            String key = SAVE_PREFIX + slot;
            prefs.putString(key, jsonData);
            prefs.flush();

            updateSaveList(slot, saveData);

            Gdx.app.log("SaveSystem", "Partida guardada en slot " + slot + ": " + saveData.saveName);
            return true;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error guardando partida", e);
            return false;
        }
    }

    /**
     * Carga una partida desde un slot específico.
     * Restaura todos los datos del juego y configuración.
     *
     * @param slot Número de slot a cargar (1 a MAX_SAVE_SLOTS)
     * @return true si la carga fue exitosa, false en caso contrario
     * @throws IllegalArgumentException si el slot está fuera de rango
     */
    public boolean loadGame(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            Gdx.app.error("SaveSystem", "Slot inválido: " + slot);
            return false;
        }

        try {
            String key = SAVE_PREFIX + slot;
            String jsonData = prefs.getString(key, "");

            if (jsonData.isEmpty()) {
                Gdx.app.log("SaveSystem", "Slot " + slot + " está vacío");
                return false;
            }

            SaveGameData saveData = json.fromJson(SaveGameData.class, jsonData);
            restoreAllData(saveData);

            Gdx.app.log("SaveSystem", "Partida cargada desde slot " + slot + ": " + saveData.saveName);
            return true;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error cargando partida", e);
            return false;
        }
    }

    /**
     * Elimina una partida guardada
     */
    public boolean deleteSave(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) {
            return false;
        }

        try {
            String key = SAVE_PREFIX + slot;
            prefs.remove(key);
            prefs.flush();

            updateSaveList(slot, null);

            Gdx.app.log("SaveSystem", "Partida eliminada del slot " + slot);
            return true;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error eliminando partida", e);
            return false;
        }
    }

    /**
     * Recopila TODOS los datos del juego
     */
    private SaveGameData collectAllData() {
        SaveGameData saveData = new SaveGameData();

        DatabaseManager db = DatabaseManager.getInstance();
        saveData.gameData = db.getGameData();

        saveData.configData = collectConfigData();
        saveData.sessionData = collectSessionData();

        saveData.progress = db.getProgresoTotal();
        saveData.level = calculatePlayerLevel();
        saveData.location = getCurrentLocation();
        saveData.playTime = formatPlayTime(db.getGameData().usuario.tiempoTotalJuego);

        return saveData;
    }

    /**
     * Recopila datos de configuración
     */
    private ConfigData collectConfigData() {
        ConfigData config = new ConfigData();

        ConfiguracionPantalla screenConfig = ConfiguracionPantalla.getInstance();
        config.resolution = screenConfig.getResolucionActual();
        config.fullscreen = screenConfig.isPantallaCompleta();

        return config;
    }

    /**
     * Recopila datos de la sesión actual
     */
    private SessionData collectSessionData() {
        SessionData session = new SessionData();
        GameState gameState = GameState.getInstance();

        // Obtener ubicación actual
        String currentLocation = getCurrentLocation();
        session.location = currentLocation;

        // Determinar pantalla actual
        if (Screens.juego != null && Screens.juego.getScreen() != null) {
            Screen pantallaActual = Screens.juego.getScreen();
            session.currentScreen = pantallaActual.getClass().getSimpleName();
        }

        // Establecer posición según ubicación
        if (currentLocation.equals("Laboratorio")) {
            session.playerPosition[0] = 300;
            session.playerPosition[1] = 200;
        } else if (currentLocation.equals("La Villa") ||
            currentLocation.equals("El Bosque") ||
            currentLocation.equals("Las Cavernas")) {
            session.playerPosition[0] = 100;
            session.playerPosition[1] = 100;
            session.currentLevel = gameState.getNivelActual();
        } else {
            session.playerPosition[0] = 0;
            session.playerPosition[1] = 0;
        }

        // Variables de sesión
        session.sessionVariables.put("ultimaDecision", gameState.getUltimaDecision());
        session.sessionVariables.put("enDialogo", gameState.isEnDialogo());
        session.sessionVariables.put("tiempoNivelActual", gameState.getTiempoNivelActual());

        return session;
    }

    /**
     * Restaura TODOS los datos del guardado
     */
    private void restoreAllData(SaveGameData saveData) {
        DatabaseManager db = DatabaseManager.getInstance();
        importGameData(db, saveData.gameData);

        applyConfigData(saveData.configData);

        GameState.getInstance().setSessionData(saveData.sessionData);

        navigateToSavedScreen(saveData.sessionData);
    }

    /**
     * Importa datos del juego al DatabaseManager
     */
    private void importGameData(DatabaseManager db, DatabaseManager.GameData gameData) {
        if (gameData == null) {
            Gdx.app.error("SaveSystem", "GameData es null, no se puede importar");
            return;
        }

        // Crear una nueva instancia de GameData y copiar los valores
        if (gameData.usuario != null) {
            db.getGameData().usuario = gameData.usuario;
        }

        if (gameData.estadisticas != null) {
            db.getGameData().estadisticas = gameData.estadisticas;
        }

        if (gameData.niveles != null) {
            db.getGameData().niveles.clear();
            db.getGameData().niveles.putAll(gameData.niveles);
        }

        if (gameData.decisiones != null) {
            db.getGameData().decisiones.clear();
            db.getGameData().decisiones.putAll(gameData.decisiones);
        }

        if (gameData.videosVistos != null) {
            db.getGameData().videosVistos.clear();
            db.getGameData().videosVistos.putAll(gameData.videosVistos);
        }

        db.saveGame();
        Gdx.app.log("SaveSystem", "Datos del juego importados correctamente");
    }

    /**
     * Aplica la configuración guardada - VERSIÓN CORREGIDA
     */
    private void applyConfigData(ConfigData config) {
        if (config == null) {
            Gdx.app.log("SaveSystem", "ConfigData es null, usando configuración por defecto");
            return;
        }

        ConfiguracionPantalla screenConfig = ConfiguracionPantalla.getInstance();

        if (config.resolution != null && !config.resolution.isEmpty()) {
            try {
                // CORREGIDO: Eliminar la parte de refresh rate si existe
                String resolutionText = config.resolution;

                // Si tiene @, eliminar todo desde el @
                if (resolutionText.contains("@")) {
                    resolutionText = resolutionText.substring(0, resolutionText.indexOf("@")).trim();
                }

                // Si tiene "Hz", eliminar
                if (resolutionText.contains("Hz")) {
                    resolutionText = resolutionText.replace("Hz", "").trim();
                }

                // Separar por 'x'
                String[] parts = resolutionText.split("x");
                if (parts.length == 2) {
                    int width = Integer.parseInt(parts[0].trim());
                    int height = Integer.parseInt(parts[1].trim());

                    if (config.fullscreen) {
                        screenConfig.setResolucionPantallaCompleta(width, height, 60);
                    } else {
                        screenConfig.setResolucionVentana(width, height);
                    }

                    Gdx.app.log("SaveSystem", "Resolución aplicada: " + width + "x" + height +
                        " (fullscreen: " + config.fullscreen + ")");
                }
            } catch (Exception e) {
                Gdx.app.error("SaveSystem", "Error aplicando resolución: " + config.resolution, e);
                // Usar valores por defecto
                if (config.fullscreen) {
                    screenConfig.setResolucionPantallaCompleta(1920, 1080, 60);
                } else {
                    screenConfig.setResolucionVentana(800, 600);
                }
            }
        }

        // Aplicar otros ajustes de configuración si existen
        if (config.volumeMaster >= 0) {
            // Aquí podrías aplicar volumen si tienes un sistema de audio
        }
    }

    /**
     * Navega a la pantalla guardada
     */
    private void navigateToSavedScreen(SessionData sessionData) {
        if (Screens.juego == null) return;

        if (sessionData.currentScreen != null) {
            if (sessionData.currentScreen.equals("LobbyScreen")) {
                Screens.juego.setScreen(new LobbyScreen(Screens.juego));
            } else if (sessionData.currentScreen.equals("MainScreen")) {
                MainScreen mainScreen = new MainScreen(Screens.juego);
                Screens.juego.setScreen(mainScreen);
            } else if (sessionData.currentScreen.equals("MenuScreen")) {
                Screens.juego.setScreen(new MenuScreen(Screens.juego));
            } else {
                Screens.juego.setScreen(new LobbyScreen(Screens.juego));
            }
        } else {
            Screens.juego.setScreen(new LobbyScreen(Screens.juego));
        }
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    /**
     * Actualiza la lista de saves disponibles
     */
    private void updateSaveList(int slot, SaveGameData saveData) {
        String listKey = "save_list";
        String listJson = prefs.getString(listKey, "{}");

        try {
            HashMap<String, String> saveList = json.fromJson(HashMap.class, listJson);
            if (saveList == null) {
                saveList = new HashMap<>();
            }

            if (saveData == null) {
                saveList.remove(String.valueOf(slot));
            } else {
                String info = saveData.saveName + "|" + saveData.timestamp + "|" +
                    String.format("%.1f", saveData.progress) + "%";
                saveList.put(String.valueOf(slot), info);
            }

            prefs.putString(listKey, json.toJson(saveList));
            prefs.flush();

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error actualizando lista de saves", e);
        }
    }

    /**
     * Obtiene información de todos los slots de guardado.
     * Útil para mostrar una lista de partidas guardadas.
     *
     * @return Mapa con información de cada slot
     */
    public Map<Integer, SaveInfo> getAllSaves() {
        HashMap<Integer, SaveInfo> saves = new HashMap<>();

        String listKey = "save_list";
        String listJson = prefs.getString(listKey, "{}");

        try {
            HashMap<String, String> saveList = json.fromJson(HashMap.class, listJson);
            if (saveList == null) {
                return saves;
            }

            for (Map.Entry<String, String> entry : saveList.entrySet()) {
                try {
                    int slot = Integer.parseInt(entry.getKey());
                    String[] infoParts = entry.getValue().split("\\|");

                    SaveInfo info = new SaveInfo();
                    info.slot = slot;

                    if (infoParts.length > 0) info.saveName = infoParts[0];
                    if (infoParts.length > 1) info.timestamp = infoParts[1];
                    if (infoParts.length > 2) info.progress = infoParts[2];

                    String saveKey = SAVE_PREFIX + slot;
                    if (prefs.contains(saveKey)) {
                        info.exists = true;
                    }

                    saves.put(slot, info);

                } catch (Exception e) {
                    Gdx.app.error("SaveSystem", "Error parseando save info", e);
                }
            }

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error obteniendo lista de saves", e);
        }

        return saves;
    }

    /**
     * Obtiene información detallada de un save
     */
    public SaveGameData getSaveInfo(int slot) {
        if (slot < 1 || slot > MAX_SAVE_SLOTS) return null;

        try {
            String key = SAVE_PREFIX + slot;
            String jsonData = prefs.getString(key, "");

            if (jsonData.isEmpty()) return null;

            return json.fromJson(SaveGameData.class, jsonData);

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error obteniendo info de save", e);
            return null;
        }
    }

    /**
     * Formatea el tiempo de juego
     */
    private String formatPlayTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    /**
     * Calcula el "nivel" del jugador basado en progreso
     */
    private int calculatePlayerLevel() {
        DatabaseManager db = DatabaseManager.getInstance();
        float progress = db.getProgresoTotal();

        if (progress >= 100) return 10;
        if (progress >= 90) return 9;
        if (progress >= 80) return 8;
        if (progress >= 70) return 7;
        if (progress >= 60) return 6;
        if (progress >= 50) return 5;
        if (progress >= 40) return 4;
        if (progress >= 30) return 3;
        if (progress >= 20) return 2;
        if (progress >= 10) return 1;
        return 0;
    }

    /**
     * Obtiene la ubicación actual del jugador
     */
    private String getCurrentLocation() {
        if (Screens.juego != null && Screens.juego.getScreen() != null) {
            String screenName = Screens.juego.getScreen().getClass().getSimpleName();

            if (screenName.equals("LobbyScreen")) {
                return "Laboratorio";
            } else if (screenName.equals("MainScreen")) {
                GameState gs = GameState.getInstance();
                String nivel = gs.getNivelActual();
                if (nivel != null) {
                    if (nivel.contains("villa")) return "La Villa";
                    if (nivel.contains("bosque")) return "El Bosque";
                    if (nivel.contains("cavernas")) return "Las Cavernas";
                }
                return "En Aventura";
            } else {
                return "Menú";
            }
        }
        return "Desconocido";
    }

    // ====================== CLASES DE AYUDA ======================

    /**
     * Información básica de un guardado para listados.
     * Contiene datos mínimos para mostrar en interfaces de usuario.
     */
    public static class SaveInfo {
        /** Número de slot */
        public int slot;
        /** Nombre del guardado */
        public String saveName;
        /** Fecha y hora del guardado */
        public String timestamp;
        /** Progreso porcentual formateado */
        public String progress;
        /** Indica si el slot contiene un guardado válido */
        public boolean exists;
        @Override
        public String toString() {
            return "Slot " + slot + ": " + saveName + " (" + progress + ") - " + timestamp;
        }
    }

    // ====================== MÉTODOS PÚBLICOS ======================

    public int getMaxSaveSlots() {
        return MAX_SAVE_SLOTS;
    }

    public boolean isSlotEmpty(int slot) {
        String key = SAVE_PREFIX + slot;
        return !prefs.contains(key) || prefs.getString(key, "").isEmpty();
    }

    /**
     * Crea un nombre de save automático
     */
    public String generateSaveName(int slot) {
        String location = getCurrentLocation();
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
        return location + " - " + sdf.format(now);
    }

    /**
     * Exporta un save a archivo externo (para backup)
     */
    public boolean exportSave(int slot, String filePath) {
        try {
            String key = SAVE_PREFIX + slot;
            String jsonData = prefs.getString(key, "");

            if (jsonData.isEmpty()) return false;

            Gdx.files.local(filePath).writeString(jsonData, false);
            Gdx.app.log("SaveSystem", "Save exportado a: " + filePath);
            return true;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error exportando save", e);
            return false;
        }
    }

    /**
     * Importa un save desde archivo externo
     */
    public boolean importSave(String filePath, int slot) {
        try {
            if (!Gdx.files.local(filePath).exists()) {
                return false;
            }

            String jsonData = Gdx.files.local(filePath).readString();
            SaveGameData testData = json.fromJson(SaveGameData.class, jsonData);
            if (testData == null) return false;

            String key = SAVE_PREFIX + slot;
            prefs.putString(key, jsonData);
            prefs.flush();

            Gdx.app.log("SaveSystem", "Save importado a slot " + slot);
            return true;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Error importando save", e);
            return false;
        }
    }
}
