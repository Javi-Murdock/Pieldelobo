package io.github.javiergames.pieldelobo.DataBase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;

/**
 * Gestor de base de datos usando JSON para persistencia multiplataforma.
 * Implementa el esquema definido en el documento del TFG.
 * Maneja datos de usuario, niveles, decisiones, videos y estadísticas.
 *
 * @author Javier Gala
 * @version 2.1
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class DatabaseManager {
    /**
     * Datos completos del juego (equivalente a múltiples tablas).
     * Agrupa todas las estructuras de datos persistentes.
     */
    private static DatabaseManager instance;
    private Json json;
    private GameData gameData;
    private String saveFilePath = "saves/save_game.json";

    // Constructor privado (Singleton)
    private DatabaseManager() {
        json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setTypeName(null); // No incluir nombres de clase

        // Configurar serializadores para HashMap
        configurarSerializadoresHashMap();

        loadGameData();
    }

    /**
     * Configura serializadores personalizados para HashMap
     * para manejar correctamente objetos específicos como LevelState
     */
    private void configurarSerializadoresHashMap() {
        // Serializador para HashMap
        json.setSerializer(HashMap.class, new Json.Serializer<HashMap>() {
            @Override
            public void write(Json json, HashMap object, Class knownType) {
                json.writeObjectStart();
                for (Object key : object.keySet()) {
                    Object value = object.get(key);

                    // NUEVO: Manejar tipos específicos para serialización correcta
                    if (value instanceof LevelState) {
                        // Serializar LevelState como objeto completo
                        json.writeObjectStart(key.toString());
                        LevelState nivel = (LevelState) value;
                        json.writeValue("nivelId", nivel.nivelId);
                        json.writeValue("nombre", nivel.nombre);
                        json.writeValue("estado", nivel.estado.name());
                        json.writeValue("completado", nivel.completado);
                        json.writeValue("mejorTiempo", nivel.mejorTiempo);
                        json.writeValue("muertes", nivel.muertes);
                        json.writeValue("tieneRecompensa", nivel.tieneRecompensa);
                        json.writeValue("fechaCompletado", nivel.fechaCompletado);
                        json.writeObjectEnd();
                    } else if (value instanceof DecisionRecord) {
                        // Serializar DecisionRecord
                        json.writeObjectStart(key.toString());
                        DecisionRecord decision = (DecisionRecord) value;
                        json.writeValue("decisionId", decision.decisionId);
                        json.writeValue("nivelId", decision.nivelId);
                        json.writeValue("textoDecision", decision.textoDecision);
                        json.writeValue("impacto", decision.impacto);
                        json.writeValue("fecha", decision.fecha);
                        json.writeValue("consecuencias", decision.consecuencias);
                        json.writeObjectEnd();
                    } else if (value instanceof VideoRecord) {
                        // Serializar VideoRecord
                        json.writeObjectStart(key.toString());
                        VideoRecord video = (VideoRecord) value;
                        json.writeValue("videoId", video.videoId);
                        json.writeValue("ruta", video.ruta);
                        json.writeValue("descripcion", video.descripcion);
                        json.writeValue("visto", video.visto);
                        json.writeValue("vecesVisto", video.vecesVisto);
                        json.writeValue("ultimaVez", video.ultimaVez);
                        json.writeObjectEnd();
                    } else if (value instanceof GameStats) {
                        // Serializar GameStats
                        json.writeObjectStart(key.toString());
                        GameStats stats = (GameStats) value;
                        json.writeValue("totalDecisiones", stats.totalDecisiones);
                        json.writeValue("decisionesBuenas", stats.decisionesBuenas);
                        json.writeValue("decisionesMalas", stats.decisionesMalas);
                        json.writeValue("npcsHablados", stats.npcsHablados);
                        json.writeValue("objetosRecolectados", stats.objetosRecolectados);
                        json.writeValue("enemigosDerrotados", stats.enemigosDerrotados);
                        json.writeValue("finalesDesbloqueados", stats.finalesDesbloqueados);
                        json.writeObjectEnd();
                    } else if (value instanceof UserData) {
                        // Serializar UserData
                        json.writeObjectStart(key.toString());
                        UserData user = (UserData) value;
                        json.writeValue("nombre", user.nombre);
                        json.writeValue("partidasJugadas", user.partidasJugadas);
                        json.writeValue("tiempoTotalJuego", user.tiempoTotalJuego);
                        json.writeValue("fechaUltimaPartida", user.fechaUltimaPartida);
                        json.writeValue("configuracion", user.configuracion);
                        json.writeObjectEnd();
                    } else {
                        // Para tipos básicos, usar escritura normal
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

                        // NUEVO: Verificar si es un objeto especial
                        if (child.isObject()) {
                            // Verificar si es un LevelState (por campos específicos)
                            if (child.has("nivelId") && child.has("estado")) {
                                value = leerLevelState(json, child);
                            }
                            // Verificar si es un DecisionRecord
                            else if (child.has("decisionId") && child.has("nivelId")) {
                                value = leerDecisionRecord(json, child);
                            }
                            // Verificar si es un VideoRecord
                            else if (child.has("videoId") && child.has("ruta")) {
                                value = leerVideoRecord(json, child);
                            }
                            // Verificar si es un GameStats
                            else if (child.has("totalDecisiones") && child.has("decisionesBuenas")) {
                                value = leerGameStats(json, child);
                            }
                            // Verificar si es un UserData
                            else if (child.has("nombre") && child.has("partidasJugadas")) {
                                value = leerUserData(json, child);
                            }
                            // Objeto HashMap normal
                            else {
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

    /**
     * Lee y crea un LevelState desde JsonValue
     */
    private LevelState leerLevelState(Json json, JsonValue jsonData) {
        LevelState nivel = new LevelState();
        nivel.nivelId = jsonData.getString("nivelId");
        nivel.nombre = jsonData.getString("nombre", "");

        String estadoStr = jsonData.getString("estado", "BLOQUEADO");
        try {
            nivel.estado = LevelState.EstadoNivel.valueOf(estadoStr);
        } catch (Exception e) {
            nivel.estado = LevelState.EstadoNivel.BLOQUEADO;
        }

        nivel.completado = jsonData.getBoolean("completado", false);
        nivel.mejorTiempo = jsonData.getFloat("mejorTiempo", 0);
        nivel.muertes = jsonData.getInt("muertes", 0);
        nivel.tieneRecompensa = jsonData.getBoolean("tieneRecompensa", false);
        nivel.fechaCompletado = jsonData.getString("fechaCompletado", "");

        return nivel;
    }

    /**
     * Lee y crea un DecisionRecord desde JsonValue
     */
    private DecisionRecord leerDecisionRecord(Json json, JsonValue jsonData) {
        DecisionRecord decision = new DecisionRecord();
        decision.decisionId = jsonData.getString("decisionId");
        decision.nivelId = jsonData.getString("nivelId");
        decision.textoDecision = jsonData.getString("textoDecision", "");
        decision.impacto = jsonData.getString("impacto", "");
        decision.fecha = jsonData.getString("fecha", "");

        // Leer consecuencias si existen
        JsonValue consecuenciasVal = jsonData.get("consecuencias");
        if (consecuenciasVal != null && consecuenciasVal.isObject()) {
            decision.consecuencias = json.readValue(HashMap.class, consecuenciasVal);
        } else {
            decision.consecuencias = new HashMap<>();
        }

        return decision;
    }

    /**
     * Lee y crea un VideoRecord desde JsonValue
     */
    private VideoRecord leerVideoRecord(Json json, JsonValue jsonData) {
        VideoRecord video = new VideoRecord();
        video.videoId = jsonData.getString("videoId");
        video.ruta = jsonData.getString("ruta", "");
        video.descripcion = jsonData.getString("descripcion", "");
        video.visto = jsonData.getBoolean("visto", false);
        video.vecesVisto = jsonData.getInt("vecesVisto", 0);
        video.ultimaVez = jsonData.getString("ultimaVez", "");

        return video;
    }

    /**
     * Lee y crea un GameStats desde JsonValue
     */
    private GameStats leerGameStats(Json json, JsonValue jsonData) {
        GameStats stats = new GameStats();
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
     * Lee y crea un UserData desde JsonValue
     */
    private UserData leerUserData(Json json, JsonValue jsonData) {
        UserData user = new UserData();
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

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ====================== CLASES DE DATOS ======================

    /**
     * Datos completos del juego (equivalente a múltiples tablas).
     * Agrupa todas las estructuras de datos persistentes.
     */
    public static class GameData {
        /** Datos del usuario jugador */
        public UserData usuario = new UserData();
        /** Estados de todos los niveles disponibles */
        public HashMap<String, LevelState> niveles = new HashMap<>();
        /** Registro histórico de decisiones tomadas */
        public HashMap<String, DecisionRecord> decisiones = new HashMap<>();
        /** Historial de videos vistos por el jugador */
        public HashMap<String, VideoRecord> videosVistos = new HashMap<>();
        /** Estadísticas globales del juego */
        public GameStats estadisticas = new GameStats();
    }

    /**
     * Datos del usuario (Tabla Usuario del esquema)
     */
    public static class UserData {
        public String nombre = "Jugador";
        public int partidasJugadas = 0;
        public int tiempoTotalJuego = 0; // en segundos
        public String fechaUltimaPartida = "";
        public HashMap<String, String> configuracion = new HashMap<>();
    }

    /**
     * Estado de un nivel individual.
     * Contiene información de progreso, desbloqueo y completado.
     */
    public static class LevelState {
        /** Identificador único del nivel */
        public String nivelId;
        /** Nombre descriptivo del nivel */
        public String nombre;
        /** Estado actual del nivel */
        public EstadoNivel estado = EstadoNivel.BLOQUEADO;
        /** Indica si el nivel ha sido completado */
        public boolean completado = false;
        /** Mejor tiempo de completado en segundos */
        public float mejorTiempo = 0;
        /** Número de muertes en este nivel */
        public int muertes = 0;
        /** Indica si la recompensa fue recogida */
        public boolean tieneRecompensa = false;
        /** Fecha de completado formateada */
        public String fechaCompletado = "";
        /**
         * Enumeración de posibles estados de un nivel.
         */
        public enum EstadoNivel {
            BLOQUEADO, DESBLOQUEADO, EN_PROGRESO, COMPLETADO
        }
    }

    /**
     * Registro de una decisión tomada (Tabla Decisiones del esquema)
     */
    public static class DecisionRecord {
        public String decisionId; // Ej: "dialogo_doctor_opcion1"
        public String nivelId;    // Nivel donde se tomó la decisión
        public String textoDecision; // Texto mostrado al jugador
        public String impacto; // Descripción del impacto (del esquema)
        public String fecha;   // Cuándo se tomó
        public HashMap<String, Object> consecuencias = new HashMap<>();
    }

    /**
     * Registro de video visto (Tabla Videos del esquema)
     */
    public static class VideoRecord {
        public String videoId; // Ej: "video_intro", "video_final_bueno"
        public String ruta;    // Ruta del archivo (del esquema)
        public String descripcion; // Descripción (del esquema)
        public boolean visto = false;
        public int vecesVisto = 0;
        public String ultimaVez = "";
    }

    /**
     * Estadísticas globales del juego
     */
    public static class GameStats {
        public int totalDecisiones = 0;
        public int decisionesBuenas = 0;
        public int decisionesMalas = 0;
        public int npcsHablados = 0;
        public int objetosRecolectados = 0;
        public int enemigosDerrotados = 0;
        public HashMap<String, Integer> finalesDesbloqueados = new HashMap<>();
    }

    // ====================== MÉTODOS PRINCIPALES ======================

    /**
     * Carga los datos del juego desde archivo JSON
     */
    private void loadGameData() {
        try {
            if (Gdx.files.local(saveFilePath).exists()) {
                String jsonText = Gdx.files.local(saveFilePath).readString();
                gameData = json.fromJson(GameData.class, jsonText);
                Gdx.app.log("DatabaseManager", "Partida cargada: " + gameData.usuario.nombre);

                // CORREGIDO: Verificar y reparar datos si es necesario
                repararDatosCargados();
            } else {
                // Crear datos por defecto
                gameData = new GameData();
                inicializarDatosPorDefecto();
                Gdx.app.log("DatabaseManager", "Nueva partida creada");
            }
        } catch (Exception e) {
            Gdx.app.error("DatabaseManager", "Error cargando partida", e);
            gameData = new GameData();
            inicializarDatosPorDefecto();
        }
    }

    /**
     * Repara datos cargados que puedan tener problemas de casting
     */
    private void repararDatosCargados() {
        // Verificar y reparar niveles si es necesario
        for (String key : gameData.niveles.keySet()) {
            Object nivelObj = gameData.niveles.get(key);
            if (nivelObj instanceof HashMap) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;
                try {
                    LevelState nivelReparado = crearLevelStateDesdeHashMap(nivelMap);
                    if (nivelReparado != null) {
                        gameData.niveles.put(key, nivelReparado);
                        Gdx.app.log("DatabaseManager", "Nivel reparado: " + key);
                    }
                } catch (Exception e) {
                    Gdx.app.error("DatabaseManager", "Error reparando nivel: " + key, e);
                }
            }
        }
    }

    /**
     * Crea LevelState desde HashMap (para compatibilidad con saves antiguos)
     */
    private LevelState crearLevelStateDesdeHashMap(HashMap<String, Object> nivelMap) {
        try {
            LevelState nivel = new LevelState();
            nivel.nivelId = (String) nivelMap.getOrDefault("nivelId", "");
            nivel.nombre = (String) nivelMap.getOrDefault("nombre", "");

            // Convertir estado de String a enum
            String estadoStr = (String) nivelMap.getOrDefault("estado", "BLOQUEADO");
            try {
                nivel.estado = LevelState.EstadoNivel.valueOf(estadoStr);
            } catch (Exception e) {
                nivel.estado = LevelState.EstadoNivel.BLOQUEADO;
            }

            Object completadoObj = nivelMap.get("completado");
            if (completadoObj instanceof Boolean) {
                nivel.completado = (Boolean) completadoObj;
            } else if (completadoObj instanceof String) {
                nivel.completado = Boolean.parseBoolean((String) completadoObj);
            }

            // Convertir números
            Object mejorTiempoObj = nivelMap.get("mejorTiempo");
            if (mejorTiempoObj instanceof Number) {
                nivel.mejorTiempo = ((Number) mejorTiempoObj).floatValue();
            } else if (mejorTiempoObj instanceof String) {
                try {
                    nivel.mejorTiempo = Float.parseFloat((String) mejorTiempoObj);
                } catch (NumberFormatException e) {
                    nivel.mejorTiempo = 0;
                }
            }

            Object muertesObj = nivelMap.get("muertes");
            if (muertesObj instanceof Number) {
                nivel.muertes = ((Number) muertesObj).intValue();
            } else if (muertesObj instanceof String) {
                try {
                    nivel.muertes = Integer.parseInt((String) muertesObj);
                } catch (NumberFormatException e) {
                    nivel.muertes = 0;
                }
            }

            nivel.fechaCompletado = (String) nivelMap.getOrDefault("fechaCompletado", "");

            return nivel;
        } catch (Exception e) {
            Gdx.app.error("DatabaseManager", "Error creando LevelState desde HashMap", e);
            return null;
        }
    }

    /**
     * Inicializa los datos por defecto (primer juego)
     */
    private void inicializarDatosPorDefecto() {
        // Usuario por defecto
        gameData.usuario.nombre = "Jugador";
        gameData.usuario.configuracion.put("volumen", "0.7");
        gameData.usuario.configuracion.put("pantallaCompleta", "false");

        // Niveles por defecto (según tu código de puertas)
        agregarNivel("nivel_1", "Nivel 1: La Villa", LevelState.EstadoNivel.BLOQUEADO);
        agregarNivel("nivel_2", "Nivel 2: Las Columnas", LevelState.EstadoNivel.BLOQUEADO);
        agregarNivel("nivel_3", "Nivel 3: Las Cavernas", LevelState.EstadoNivel.BLOQUEADO);
        agregarNivel("nivel_4", "Nivel 4: La Luna", LevelState.EstadoNivel.BLOQUEADO);
        agregarNivel("nivel_5", "Nivel 5: El Castillo", LevelState.EstadoNivel.BLOQUEADO);


        // Videos por defecto
        agregarVideo("intro", "video0_1.ogg", "Introducción al juego");

    }

    /**
     * Guarda todos los datos del juego en archivo JSON.
     * Se ejecuta automáticamente después de cambios importantes.
     */
    public void saveGame() {
        try {
            // Actualizar fecha de última partida
            gameData.usuario.fechaUltimaPartida = java.time.LocalDateTime.now().toString();

            // Convertir a JSON
            String jsonText = json.prettyPrint(gameData);

            // Guardar archivo
            Gdx.files.local(saveFilePath).writeString(jsonText, false);

            Gdx.app.log("DatabaseManager", "Partida guardada: " + saveFilePath);
        } catch (Exception e) {
            Gdx.app.error("DatabaseManager", "Error guardando partida", e);
        }
    }

    /**
     * Crea una nueva partida (reset total)
     */
    public void nuevaPartida(String nombreJugador) {
        gameData = new GameData();
        gameData.usuario.nombre = nombreJugador;
        gameData.usuario.partidasJugadas++;
        inicializarDatosPorDefecto();
        saveGame();
        Gdx.app.log("DatabaseManager", "Nueva partida para: " + nombreJugador);
    }

    // ====================== MÉTODOS PARA NIVELES ======================

    /**
     * Agrega o actualiza un nivel
     */
    public void agregarNivel(String nivelId, String nombre, LevelState.EstadoNivel estado) {
        LevelState nivel = new LevelState();
        nivel.nivelId = nivelId;
        nivel.nombre = nombre;
        nivel.estado = estado;
        gameData.niveles.put(nivelId, nivel);
    }

    /**
     * Desbloquea un nivel
     */
    public void desbloquearNivel(String nivelId) {
        LevelState nivel = gameData.niveles.get(nivelId);
        if (nivel != null) {
            nivel.estado = LevelState.EstadoNivel.DESBLOQUEADO;
            Gdx.app.log("DatabaseManager", "Nivel desbloqueado: " + nivelId);
        }
    }

    /**
     * Completar un nivel y registrar estadísticas.
     * No desbloquea automáticamente el siguiente nivel.
     *
     * @param nivelId ID del nivel completado
     * @param tiempo Tiempo de completado en segundos
     */
    public void completarNivel(String nivelId, float tiempo) {
        LevelState nivel = gameData.niveles.get(nivelId);
        if (nivel != null) {
            nivel.estado = LevelState.EstadoNivel.COMPLETADO;
            nivel.completado = true;
            nivel.fechaCompletado = java.time.LocalDateTime.now().toString();

            // Guardar mejor tiempo
            if (nivel.mejorTiempo == 0 || tiempo < nivel.mejorTiempo) {
                nivel.mejorTiempo = tiempo;
            }

            // **IMPORTANTE: NO desbloquear automáticamente el siguiente nivel**
            // desbloquearSiguienteNivel(nivelId); // <-- COMENTAR O ELIMINAR ESTA LÍNEA

            // **NUEVO: Solo actualizar EventManager**
            try {
                io.github.javiergames.pieldelobo.GestorJuego.EventManager eventManager =
                    io.github.javiergames.pieldelobo.GestorJuego.EventManager.getInstance();
                eventManager.onNivelCompletado(nivelId);

                Gdx.app.log("DatabaseManager", "✅ EventManager actualizado para nivel: " + nivelId);
                Gdx.app.log("DatabaseManager", "⚠️ NOTA: El siguiente nivel NO se desbloquea automáticamente");
                Gdx.app.log("DatabaseManager", "💬 Habla con el NPC correspondiente para desbloquear el siguiente nivel");
            } catch (Exception e) {
                Gdx.app.error("DatabaseManager", "Error actualizando EventManager", e);
            }

            // **NUEVO: Incrementar estadísticas de progreso**
            gameData.estadisticas.finalesDesbloqueados.put("nivel_" +
                nivelId.replace("nivel_", ""), 1);

            // **NUEVO: Guardar inmediatamente**
            saveGame();

            Gdx.app.log("DatabaseManager", "✅ Nivel completado: " + nivelId +
                " en " + String.format("%.1f", tiempo) + "s");
            Gdx.app.log("DatabaseManager", "   Progreso total: " +
                String.format("%.1f", getProgresoTotal()) + "%");
        } else {
            Gdx.app.error("DatabaseManager", "Nivel no encontrado para completar: " + nivelId);
        }
    }

    /**
     * Desbloquea el nivel siguiente basado en el ID
     */
    private void desbloquearSiguienteNivel(String nivelIdActual) {
        // Lógica simple: nivel_1 -> nivel_2 -> nivel_3
        String siguienteId = null;

        switch (nivelIdActual) {
            case "nivel_1":
                siguienteId = "nivel_2";
                break;
            case "nivel_2":
                siguienteId = "nivel_3";
                break;
            case "nivel_3":
                siguienteId = "nivel_4";
                break;
            case "nivel_4":
                siguienteId = "nivel_5";
                break;
            case "nivel_5":
                siguienteId = "nivel_6";
                break;
            case "nivel_6":
                siguienteId = "nivel_7";
                break;
            case "nivel_7":
                siguienteId = "nivel_8";
                break;
            case "nivel_8":
                siguienteId = "nivel_9";
                break;
            case "nivel_9":
                siguienteId = "nivel_10";
                break;
            // Agregar más casos según necesites
        }

        if (siguienteId != null && gameData.niveles.containsKey(siguienteId)) {
            desbloquearNivel(siguienteId);
        }
    }

    /**
     * Verifica si un nivel está desbloqueado.
     * Compatible con saves antiguos mediante conversión automática.
     *
     * @param nivelId ID del nivel a verificar
     * @return true si el nivel está desbloqueado, false en caso contrario
     */
    public boolean isNivelDesbloqueado(String nivelId) {
        Object nivelObj = gameData.niveles.get(nivelId);

        // Caso 1: Es un LevelState directamente
        if (nivelObj instanceof LevelState) {
            LevelState nivel = (LevelState) nivelObj;
            return nivel != null &&
                (nivel.estado == LevelState.EstadoNivel.DESBLOQUEADO ||
                    nivel.estado == LevelState.EstadoNivel.EN_PROGRESO ||
                    nivel.estado == LevelState.EstadoNivel.COMPLETADO);
        }
        // Caso 2: Es un HashMap (viejos saves) - convertirlo
        else if (nivelObj instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;

            // Crear LevelState desde HashMap
            LevelState nivel = crearLevelStateDesdeHashMap(nivelMap);
            if (nivel != null) {
                // Actualizar el mapa con el objeto correcto
                gameData.niveles.put(nivelId, nivel);

                return nivel.estado == LevelState.EstadoNivel.DESBLOQUEADO ||
                    nivel.estado == LevelState.EstadoNivel.EN_PROGRESO ||
                    nivel.estado == LevelState.EstadoNivel.COMPLETADO;
            }
        }
        // Caso 3: No existe el nivel
        else if (nivelObj == null) {
            Gdx.app.log("DatabaseManager", "Nivel no encontrado: " + nivelId);
            // Crear nivel por defecto si no existe
            agregarNivel(nivelId, "Nivel " + nivelId.replace("nivel_", ""),
                LevelState.EstadoNivel.BLOQUEADO);
            return false;
        }

        return false;
    }

    /**
     * Verifica si un nivel está completado
     */
    public boolean isNivelCompletado(String nivelId) {
        Object nivelObj = gameData.niveles.get(nivelId);

        if (nivelObj instanceof LevelState) {
            LevelState nivel = (LevelState) nivelObj;
            return nivel != null && nivel.completado;
        } else if (nivelObj instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;
            Object completadoObj = nivelMap.get("completado");
            if (completadoObj instanceof Boolean) {
                return (Boolean) completadoObj;
            } else if (completadoObj instanceof String) {
                return Boolean.parseBoolean((String) completadoObj);
            }
        }

        return false;
    }

    // ====================== MÉTODOS PARA DECISIONES ======================

    /**
     * Registra una decisión tomada por el jugador
     */
    public void registrarDecision(String decisionId, String nivelId, String texto,
                                  String impacto, HashMap<String, Object> consecuencias) {
        DecisionRecord decision = new DecisionRecord();
        decision.decisionId = decisionId;
        decision.nivelId = nivelId;
        decision.textoDecision = texto;
        decision.impacto = impacto;
        decision.fecha = java.time.LocalDateTime.now().toString();
        decision.consecuencias = consecuencias != null ? consecuencias : new HashMap<>();

        gameData.decisiones.put(decisionId, decision);
        gameData.estadisticas.totalDecisiones++;

        // Clasificar como "buena" o "mala" basado en impacto
        if (impacto.toLowerCase().contains("positivo") ||
            impacto.toLowerCase().contains("bueno")) {
            gameData.estadisticas.decisionesBuenas++;
        } else if (impacto.toLowerCase().contains("negativo") ||
            impacto.toLowerCase().contains("malo")) {
            gameData.estadisticas.decisionesMalas++;
        }

        Gdx.app.log("DatabaseManager", "Decisión registrada: " + decisionId);

        // Aplicar consecuencias inmediatas
        aplicarConsecuencias(decision);
    }

    /**
     * Aplica las consecuencias de una decisión
     */
    private void aplicarConsecuencias(DecisionRecord decision) {
        // Ejemplo: Desbloquear niveles, cambiar estado de NPCs, etc.
        for (HashMap.Entry<String, Object> entry : decision.consecuencias.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.equals("desbloquear_nivel")) {
                desbloquearNivel(value.toString());
            } else if (key.equals("ver_video")) {
                marcarVideoComoVisto(value.toString());
            }
            // Agregar más tipos de consecuencias según necesites
        }
    }

    /**
     * Verifica si ya se tomó una decisión específica
     */
    public boolean isDecisionTomada(String decisionId) {
        return gameData.decisiones.containsKey(decisionId);
    }

    // ====================== MÉTODOS PARA VIDEOS ======================

    /**
     * Agrega un video al registro
     */
    public void agregarVideo(String videoId, String ruta, String descripcion) {
        VideoRecord video = new VideoRecord();
        video.videoId = videoId;
        video.ruta = ruta;
        video.descripcion = descripcion;
        gameData.videosVistos.put(videoId, video);
    }

    /**
     * Marca un video como visto
     */
    public void marcarVideoComoVisto(String videoId) {
        VideoRecord video = gameData.videosVistos.get(videoId);
        if (video != null) {
            video.visto = true;
            video.vecesVisto++;
            video.ultimaVez = java.time.LocalDateTime.now().toString();
            Gdx.app.log("DatabaseManager", "Video marcado como visto: " + videoId);
        }
    }

    /**
     * Verifica si un video ha sido visto
     */
    public boolean isVideoVisto(String videoId) {
        VideoRecord video = gameData.videosVistos.get(videoId);
        return video != null && video.visto;
    }

    // ====================== MÉTODOS PARA ESTADÍSTICAS ======================

    /**
     * Incrementa contador de NPCs hablados
     */
    public void incrementarNpcsHablados() {
        gameData.estadisticas.npcsHablados++;
    }

    /**
     * Incrementa contador de enemigos derrotados
     */
    public void incrementarEnemigosDerrotados() {
        gameData.estadisticas.enemigosDerrotados++;
    }

    /**
     * Desbloquea un final alternativo
     */
    public void desbloquearFinal(String finalId) {
        Integer veces = gameData.estadisticas.finalesDesbloqueados.get(finalId);
        if (veces == null) {
            gameData.estadisticas.finalesDesbloqueados.put(finalId, 1);
        } else {
            gameData.estadisticas.finalesDesbloqueados.put(finalId, veces + 1);
        }
        Gdx.app.log("DatabaseManager", "Final desbloqueado: " + finalId);
    }

    // ====================== MÉTODOS DE ACCESO ======================

    public GameData getGameData() {
        return gameData;
    }

    public UserData getUserData() {
        return gameData.usuario;
    }

    public GameStats getStats() {
        return gameData.estadisticas;
    }

    /**
     * Obtiene el progreso total del jugador en porcentaje.
     * Calcula basado en niveles completados vs totales.
     *
     * @return Porcentaje de progreso (0.0 a 100.0)
     */
    public float getProgresoTotal() {
        int nivelesTotales = gameData.niveles.size();
        int nivelesCompletados = 0;

        for (Object nivelObj : gameData.niveles.values()) {
            boolean completado = false;

            if (nivelObj instanceof LevelState) {
                completado = ((LevelState) nivelObj).completado;
            } else if (nivelObj instanceof HashMap) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;
                Object completadoObj = nivelMap.get("completado");
                if (completadoObj instanceof Boolean) {
                    completado = (Boolean) completadoObj;
                } else if (completadoObj instanceof String) {
                    completado = Boolean.parseBoolean((String) completadoObj);
                }
            }

            if (completado) nivelesCompletados++;
        }

        if (nivelesTotales == 0) return 0;
        return (nivelesCompletados * 100f) / nivelesTotales;
    }

    /**
     * Genera un reporte de progreso
     */
    public String getReporteProgreso() {
        return String.format(
            "Progreso: %.1f%%\n" +
                "Niveles: %d/%d\n" +
                "Decisiones: %d\n" +
                "Videos vistos: %d/%d",
            getProgresoTotal(),
            getNivelesCompletadosCount(),
            gameData.niveles.size(),
            gameData.estadisticas.totalDecisiones,
            getVideosVistosCount(),
            gameData.videosVistos.size()
        );
    }

    private int getNivelesCompletadosCount() {
        int count = 0;
        for (Object nivelObj : gameData.niveles.values()) {
            boolean completado = false;

            if (nivelObj instanceof LevelState) {
                completado = ((LevelState) nivelObj).completado;
            } else if (nivelObj instanceof HashMap) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;
                Object completadoObj = nivelMap.get("completado");
                if (completadoObj instanceof Boolean) {
                    completado = (Boolean) completadoObj;
                } else if (completadoObj instanceof String) {
                    completado = Boolean.parseBoolean((String) completadoObj);
                }
            }

            if (completado) count++;
        }
        return count;
    }

    private int getVideosVistosCount() {
        int count = 0;
        for (VideoRecord video : gameData.videosVistos.values()) {
            if (video.visto) count++;
        }
        return count;
    }

    /**
     * Exporta datos a formato legible (para debug)
     */
    public String exportarDatos() {
        return json.prettyPrint(gameData);
    }

    /**
     * Verifica el estado de un nivel (para debug)
     */
    public String getEstadoNivel(String nivelId) {
        Object nivelObj = gameData.niveles.get(nivelId);

        if (nivelObj instanceof LevelState) {
            LevelState nivel = (LevelState) nivelObj;
            return nivel.estado.name();
        } else if (nivelObj instanceof HashMap) {
            @SuppressWarnings("unchecked")
            HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;
            return (String) nivelMap.getOrDefault("estado", "BLOQUEADO");
        }

        return "NO_EXISTE";
    }
}
