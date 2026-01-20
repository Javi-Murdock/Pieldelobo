package io.github.javiergames.pieldelobo.DataBase;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que almacena los datos de la sesión actual del jugador.
 * Contiene información sobre el estado del juego al momento de guardar,
 * incluyendo pantalla actual, posición del jugador, NPCs activos y variables de sesión.
 *
 * @author Javier Gala
 * @version 1.0
 */
public class SessionData {
    /**
     * Pantalla actual donde se encuentra el jugador.
     * Valores típicos: "LobbyScreen", "MainScreen", "MenuScreen"
     */
    public String currentScreen;
    /**
     * ID del nivel actual donde se encuentra el jugador.
     * Ejemplos: "nivel_villa", "nivel_1", "nivel_bosque"
     */
    public String currentLevel;
    /**
     * Ubicación textual descriptiva del lugar actual.
     * Ejemplos: "Laboratorio", "La Villa", "Luna"
     */
    public String location;
    /**
     * Constructor por defecto que inicializa todos los valores.
     * Crea una instancia con valores predeterminados.
     */

    // Posiciones
    public float playerX;           // Posición X del jugador
    public float playerY;           // Posición Y del jugador
    public float cameraX;           // Posición X de la cámara
    public float cameraY;           // Posición Y de la cámara

    // Estado del juego
    public String[] activeNPCs;     // NPCs activos en ese momento

    public Map<String, Object> sessionVariables = new HashMap<>(); // Variables diversas

    // Tiempo y progreso
    public float tiempoNivelActual; // Tiempo transcurrido en el nivel actual
    public String ultimaDecision;   // Última decisión tomada
    public boolean enDialogo;       // Si estaba en diálogo

    /**
     * Constructor con valores iniciales específicos.
     *
     * @param currentScreen Pantalla actual del jugador
     * @param location Ubicación actual del jugador
     * @param playerX Posición X inicial del jugador
     * @param playerY Posición Y inicial del jugador
     */
    public SessionData() {
        this.currentScreen = "";
        this.currentLevel = "";
        this.location = "Desconocido";
        this.playerX = 0;
        this.playerY = 0;
        this.cameraX = 0;
        this.cameraY = 0;
        this.activeNPCs = new String[0];
        this.tiempoNivelActual = 0;
        this.ultimaDecision = "";
        this.enDialogo = false;
    }

    /**
     * Constructor con valores iniciales
     */
    public SessionData(String currentScreen, String location, float playerX, float playerY) {
        this();
        this.currentScreen = currentScreen;
        this.location = location;
        this.playerX = playerX;
        this.playerY = playerY;
    }

    // ====================== GETTERS ======================


    /**
     * Obtiene la pantalla actual del jugador.
     *
     * @return Nombre de la pantalla actual
     */
    public String getCurrentScreen() {
        return currentScreen;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public String getLocation() {
        return location;
    }

    public float getPlayerX() {
        return playerX;
    }

    public float getPlayerY() {
        return playerY;
    }

    public float getCameraX() {
        return cameraX;
    }

    public float getCameraY() {
        return cameraY;
    }

    public String[] getActiveNPCs() {
        return activeNPCs != null ? activeNPCs : new String[0];
    }

    public Map<String, Object> getSessionVariables() {
        return sessionVariables;
    }

    public float getTiempoNivelActual() {
        return tiempoNivelActual;
    }

    public String getUltimaDecision() {
        return ultimaDecision;
    }

    public boolean isEnDialogo() {
        return enDialogo;
    }

    // ====================== SETTERS ======================

    public void setCurrentScreen(String currentScreen) {
        this.currentScreen = currentScreen;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    /**
     * Establece la posición del jugador.
     *
     * @param x Coordenada X de la posición
     * @param y Coordenada Y de la posición
     */

    public void setPlayerPosition(float x, float y) {
        this.playerX = x;
        this.playerY = y;
    }

    public void setCameraPosition(float x, float y) {
        this.cameraX = x;
        this.cameraY = y;
    }

    public void setActiveNPCs(String[] activeNPCs) {
        this.activeNPCs = activeNPCs != null ? activeNPCs : new String[0];
    }

    public void setSessionVariables(Map<String, Object> sessionVariables) {
        this.sessionVariables = sessionVariables != null ? sessionVariables : new HashMap<>();
    }

    public void setTiempoNivelActual(float tiempoNivelActual) {
        this.tiempoNivelActual = tiempoNivelActual;
    }

    public void setUltimaDecision(String ultimaDecision) {
        this.ultimaDecision = ultimaDecision;
    }

    public void setEnDialogo(boolean enDialogo) {
        this.enDialogo = enDialogo;
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    /**
     * Agrega una variable de sesión personalizada.
     * Útil para almacenar datos temporales específicos del juego.
     *
     * @param key Clave identificadora de la variable
     * @param value Valor a almacenar (cualquier objeto)
     */
    public void putVariable(String key, Object value) {
        sessionVariables.put(key, value);
    }

    /**
     * Obtiene una variable de sesión
     */
    public Object getVariable(String key) {
        return sessionVariables.get(key);
    }

    /**
     * Obtiene una variable de sesión con valor por defecto
     */
    public Object getVariable(String key, Object defaultValue) {
        return sessionVariables.getOrDefault(key, defaultValue);
    }

    /**
     * Verifica si existe una variable de sesión
     */
    public boolean hasVariable(String key) {
        return sessionVariables.containsKey(key);
    }

    /**
     * Obtiene la posición del jugador como array [x, y].
     *
     * @return Array con las coordenadas X e Y
     */
    public void clearVariables() {
        sessionVariables.clear();
    }

    /**
     * Obtiene la posición del jugador como array [x, y]
     */
    public float[] getPlayerPosition() {
        return new float[]{playerX, playerY};
    }

    /**
     * Obtiene la posición de la cámara como array [x, y]
     */
    public float[] getCameraPosition() {
        return new float[]{cameraX, cameraY};
    }

    /**
     * Verifica si hay NPCs activos
     */
    public boolean hasActiveNPCs() {
        return activeNPCs != null && activeNPCs.length > 0;
    }

    /**
     * Agrega un NPC activo a la lista.
     * Verifica si ya existe antes de agregarlo.
     *
     * @param npcId Identificador único del NPC
     */
    public void addActiveNPC(String npcId) {
        if (activeNPCs == null) {
            activeNPCs = new String[0];
        }

        // Verificar si ya existe
        for (String npc : activeNPCs) {
            if (npc.equals(npcId)) {
                return; // Ya existe
            }
        }

        // Agregar nuevo
        String[] newArray = new String[activeNPCs.length + 1];
        System.arraycopy(activeNPCs, 0, newArray, 0, activeNPCs.length);
        newArray[activeNPCs.length] = npcId;
        activeNPCs = newArray;
    }

    /**
     * Elimina un NPC activo
     */
    public void removeActiveNPC(String npcId) {
        if (activeNPCs == null || activeNPCs.length == 0) {
            return;
        }

        int index = -1;
        for (int i = 0; i < activeNPCs.length; i++) {
            if (activeNPCs[i].equals(npcId)) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            String[] newArray = new String[activeNPCs.length - 1];
            System.arraycopy(activeNPCs, 0, newArray, 0, index);
            System.arraycopy(activeNPCs, index + 1, newArray, index, activeNPCs.length - index - 1);
            activeNPCs = newArray;
        }
    }

    /**
     * Clona este objeto SessionData
     */
    public SessionData clone() {
        SessionData clone = new SessionData();

        clone.currentScreen = this.currentScreen;
        clone.currentLevel = this.currentLevel;
        clone.location = this.location;
        clone.playerX = this.playerX;
        clone.playerY = this.playerY;
        clone.cameraX = this.cameraX;
        clone.cameraY = this.cameraY;
        clone.tiempoNivelActual = this.tiempoNivelActual;
        clone.ultimaDecision = this.ultimaDecision;
        clone.enDialogo = this.enDialogo;

        // Clonar array de NPCs
        if (this.activeNPCs != null) {
            clone.activeNPCs = new String[this.activeNPCs.length];
            System.arraycopy(this.activeNPCs, 0, clone.activeNPCs, 0, this.activeNPCs.length);
        }

        // Clonar mapa de variables
        clone.sessionVariables = new HashMap<>(this.sessionVariables);

        return clone;
    }

    /**
     * Convierte los datos a String para debug
     */
    @Override
    public String toString() {
        return String.format(
            "SessionData[Screen: %s, Location: %s, Pos: (%.1f, %.1f), Level: %s, Dialogo: %b]",
            currentScreen, location, playerX, playerY, currentLevel, enDialogo
        );
    }

    /**
     * Crea una instancia de SessionData preconfigurada para el lobby.
     *
     * @param playerX Posición X inicial en el lobby
     * @param playerY Posición Y inicial en el lobby
     * @return Instancia de SessionData configurada para el lobby
     */
    public static SessionData forLobby(float playerX, float playerY) {
        SessionData data = new SessionData();
        data.currentScreen = "LobbyScreen";
        data.location = "Laboratorio";
        data.playerX = playerX;
        data.playerY = playerY;
        return data;
    }

    /**
     * Crea una instancia de SessionData para un nivel
     */
    public static SessionData forLevel(String levelId, float playerX, float playerY) {
        SessionData data = new SessionData();
        data.currentScreen = "MainScreen";
        data.currentLevel = levelId;

        // Determinar ubicación textual según el ID del nivel
        if (levelId.contains("villa")) {
            data.location = "La Villa";
        } else if (levelId.contains("bosque")) {
            data.location = "El Bosque";
        } else if (levelId.contains("cavernas")) {
            data.location = "Las Cavernas";
        } else {
            data.location = "En Aventura";
        }

        data.playerX = playerX;
        data.playerY = playerY;
        return data;
    }

    /**
     * Crea una instancia de SessionData para el menú
     */
    public static SessionData forMenu() {
        SessionData data = new SessionData();
        data.currentScreen = "MenuScreen";
        data.location = "Menú Principal";
        return data;
    }

    /**
     * Valida si los datos de sesión son básicamente correctos.
     * Verifica que los campos esenciales no sean nulos o vacíos.
     *
     * @return true si los datos son válidos, false en caso contrario
     */
    public boolean isValid() {
        return currentScreen != null && !currentScreen.isEmpty() &&
            location != null && !location.isEmpty();
    }

    /**
     * Restablece a valores por defecto
     */
    public void reset() {
        currentScreen = "";
        currentLevel = "";
        location = "Desconocido";
        playerX = 0;
        playerY = 0;
        cameraX = 0;
        cameraY = 0;
        activeNPCs = new String[0];
        tiempoNivelActual = 0;
        ultimaDecision = "";
        enDialogo = false;
        sessionVariables.clear();
    }
}
