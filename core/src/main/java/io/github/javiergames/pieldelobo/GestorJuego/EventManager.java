package io.github.javiergames.pieldelobo.GestorJuego;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;

/**
 * Maneja eventos y transiciones entre niveles y diálogos.
 * Sistema que conecta la finalización de niveles con los diálogos del lobby.
 *
 * @author Javier Gala
 * @version 2.0
 */
public class EventManager {

    // ====================== INSTANCIA SINGLETON ======================
    private static EventManager instance;

    // ====================== ALMACENAMIENTO DE EVENTOS ======================
    private HashMap<String, Boolean> eventos;
    private HashMap<String, String> nivelesCompletados;

    // ====================== CONSTRUCTOR PRIVADO ======================
    private EventManager() {
        inicializarEventos();
        Gdx.app.log("EventManager", "✅ EventManager inicializado");
    }

    /**
     * Obtiene la instancia única de EventManager
     */
    public static EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    // ====================== INICIALIZACIÓN ======================
    /**
     * Inicializa todos los eventos del juego
     */
    private void inicializarEventos() {
        eventos = new HashMap<>();
        nivelesCompletados = new HashMap<>();

        // Eventos de niveles completados
        eventos.put("nivel_1_completado", false);
        eventos.put("nivel_2_completado", false);
        eventos.put("nivel_3_completado", false);
        eventos.put("nivel_4_completado", false);
        eventos.put("nivel_5_completado", false);

        // Eventos de NPCs disponibles para misiones
        eventos.put("profesor_vega_disponible", false);     // Para nivel 2
        eventos.put("doctora_garcia_mision3", false);       // Para nivel 3
        eventos.put("profesor_leiva_historia", false);      // Para nivel 4
        eventos.put("doctor_salazar_final", false);         // Para nivel 5
        eventos.put("jose_castellanos_secreto", false);     // Para nivel secreto

        // Inicializar niveles completados
        for (int i = 1; i <= 5; i++) {
            nivelesCompletados.put("nivel_" + i, "false");
        }

        Gdx.app.log("EventManager", "Eventos inicializados: " + eventos.size() + " eventos");
    }

    // ====================== MÉTODOS PRINCIPALES ======================

    /**
     * Se llama cuando se completa un nivel
     * Activa los eventos correspondientes para el siguiente nivel
     *
     * @param nivelId ID del nivel completado (ej: "nivel_1")
     */
    public void onNivelCompletado(String nivelId) {
        if (nivelId == null || nivelId.isEmpty()) {
            Gdx.app.error("EventManager", "ID de nivel nulo al marcar como completado");
            return;
        }

        // Marcar nivel como completado
        nivelesCompletados.put(nivelId, "true");
        eventos.put(nivelId + "_completado", true);

        Gdx.app.log("EventManager", "✅ Nivel completado: " + nivelId);

        // NUEVO: Usar SistemaProgresion para determinar qué eventos activar
        SistemaProgresion progresion = SistemaProgresion.getInstance();

        // Activar eventos para el siguiente nivel basado en el nivel completado
        switch (nivelId) {
            case "nivel_1":
                eventos.put("profesor_vega_disponible", true);
                Gdx.app.log("EventManager", "🎯 Evento activado: profesor_vega_disponible");
                break;

            case "nivel_2":
                eventos.put("doctora_garcia_mision3", true);
                Gdx.app.log("EventManager", "🎯 Evento activado: doctora_garcia_mision3");
                break;

            case "nivel_3":
                eventos.put("profesor_leiva_historia", true);
                Gdx.app.log("EventManager", "🎯 Evento activado: profesor_leiva_historia");
                break;

            case "nivel_4":
                eventos.put("doctor_salazar_final", true);
                Gdx.app.log("EventManager", "🎯 Evento activado: doctor_salazar_final");
                break;

            case "nivel_5":
                eventos.put("jose_castellanos_secreto", true);
                Gdx.app.log("EventManager", "🎯 Evento activado: jose_castellanos_secreto");
                break;

            default:
                Gdx.app.log("EventManager", "Nivel completado: " + nivelId + " (sin eventos especiales)");
                break;
        }

        // Log del estado actual
        logEstado();
    }

    // ====================== NUEVO: MÉTODOS PARA DIÁLOGOS ======================

    /**
     * Obtiene el diálogo que debe mostrarse después de completar un nivel
     *
     * @param nivelId ID del nivel completado
     * @return ID del diálogo correspondiente, o null si no hay
     */
    public String getDialogoParaNivelCompletado(String nivelId) {
        if (nivelId == null) return null;

        switch (nivelId) {
            case "nivel_1":
                return "profesor_vega_puerta2";  // Diálogo de la puerta 2
            case "nivel_2":
                return "doctora_garcia_puerta3"; // Diálogo de la puerta 3
            case "nivel_3":
                return "profesor_leiva_puerta4"; // Diálogo de la puerta 4
            case "nivel_4":
                return "doctor_salazar_puerta5"; // Diálogo de la puerta 5
            case "nivel_5":
                return "jose_castellanos_puerta5"; // Diálogo final
            default:
                return null;
        }
    }

    /**
     * Verifica si hay un diálogo pendiente para mostrar después de completar un nivel
     */
    public boolean tieneDialogoPendiente(String nivelId) {
        return getDialogoParaNivelCompletado(nivelId) != null;
    }

    /**
     * Obtiene el NPC que debe mostrar el diálogo para un nivel completado
     */
    public String getNpcParaDialogo(String dialogoId) {
        if (dialogoId == null) return null;

        switch (dialogoId) {
            case "profesor_vega_puerta2":
                return "Profesor Vega (Ciber)";
            case "doctora_garcia_puerta3":
                return "Doctora García";
            case "profesor_leiva_puerta4":
                return "Profesor Leiva";
            case "doctor_salazar_puerta5":
                return "Doctor Salazar";
            case "jose_castellanos_puerta5":
                return "José Castellanos (Señor)";
            default:
                return "NPC Desconocido";
        }
    }

    // ====================== MÉTODOS DE CONSULTA ======================

    /**
     * Verifica si un nivel ha sido completado
     */
    public boolean isNivelCompletado(String nivelId) {
        boolean completado = nivelesCompletados.getOrDefault(nivelId, "false").equals("true") ||
            eventos.getOrDefault(nivelId + "_completado", false);

        Gdx.app.debug("EventManager", "Verificando nivel " + nivelId + ": " + completado);
        return completado;
    }

    /**
     * Verifica si un evento está activo
     */
    public boolean isEventoActivo(String eventoId) {
        boolean activo = eventos.getOrDefault(eventoId, false);
        Gdx.app.debug("EventManager", "Evento " + eventoId + ": " + (activo ? "ACTIVO" : "INACTIVO"));
        return activo;
    }

    /**
     * Activa un evento específico
     */
    public void activarEvento(String eventoId) {
        eventos.put(eventoId, true);
        Gdx.app.log("EventManager", "🎯 Evento activado manualmente: " + eventoId);
    }

    /**
     * Desactiva un evento específico
     */
    public void desactivarEvento(String eventoId) {
        eventos.put(eventoId, false);
        Gdx.app.log("EventManager", "Evento desactivado: " + eventoId);
    }

    /**
     * Obtiene el próximo NPC que debería ofrecerte una misión
     * Basado en los niveles que has completado
     */
    public String getProximoNPCConMision() {
        // Verificar en orden de progreso
        if (eventos.get("profesor_vega_disponible") && !eventos.get("nivel_2_completado")) {
            return "Profesor Vega (Ciber)";
        }
        if (eventos.get("doctora_garcia_mision3") && !eventos.get("nivel_3_completado")) {
            return "Doctora García";
        }
        if (eventos.get("profesor_leiva_historia") && !eventos.get("nivel_4_completado")) {
            return "Profesor Leiva";
        }
        if (eventos.get("doctor_salazar_final") && !eventos.get("nivel_5_completado")) {
            return "Doctor Salazar";
        }
        if (eventos.get("jose_castellanos_secreto")) {
            return "José Castellanos (Señor)";
        }

        return null;
    }

    /**
     * Obtiene el ID del diálogo que debería activarse para el próximo NPC
     */
    public String getDialogoProximaMision() {
        if (eventos.get("profesor_vega_disponible") && !eventos.get("nivel_2_completado")) {
            return "profesor_vega_puerta2";
        }
        if (eventos.get("doctora_garcia_mision3") && !eventos.get("nivel_3_completado")) {
            return "doctora_garcia_puerta3";
        }
        if (eventos.get("profesor_leiva_historia") && !eventos.get("nivel_4_completado")) {
            return "profesor_leiva_puerta4";
        }
        if (eventos.get("doctor_salazar_final") && !eventos.get("nivel_5_completado")) {
            return "doctor_salazar_puerta5";
        }

        return null;
    }

    /**
     * Obtiene el ID del próximo nivel que debería estar disponible
     */
    public String getProximoNivelDisponible() {
        if (eventos.get("profesor_vega_disponible") && !eventos.get("nivel_2_completado")) {
            return "nivel_2";
        }
        if (eventos.get("doctora_garcia_mision3") && !eventos.get("nivel_3_completado")) {
            return "nivel_3";
        }
        if (eventos.get("profesor_leiva_historia") && !eventos.get("nivel_4_completado")) {
            return "nivel_4";
        }
        if (eventos.get("doctor_salazar_final") && !eventos.get("nivel_5_completado")) {
            return "nivel_5";
        }

        return null;
    }

    /**
     * Verifica si el jugador está listo para recibir una nueva misión
     */
    public boolean tieneMisionPendiente() {
        return getProximoNPCConMision() != null;
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    /**
     * Reinicia todos los eventos (para nueva partida)
     */
    public void reiniciar() {
        inicializarEventos();
        Gdx.app.log("EventManager", "🔄 Todos los eventos reiniciados");
    }

    /**
     * Obtiene un resumen del progreso del jugador
     */
    public String getResumenProgreso() {
        int totalNiveles = 5;
        int completados = 0;

        for (int i = 1; i <= totalNiveles; i++) {
            if (isNivelCompletado("nivel_" + i)) {
                completados++;
            }
        }

        float porcentaje = (completados * 100f) / totalNiveles;

        StringBuilder resumen = new StringBuilder();
        resumen.append("=== PROGRESO DEL JUGADOR ===\n");
        resumen.append("Niveles completados: ").append(completados).append("/").append(totalNiveles).append("\n");
        resumen.append("Progreso: ").append(String.format("%.1f", porcentaje)).append("%\n\n");

        // Agregar próximas misiones
        String proximoNPC = getProximoNPCConMision();
        if (proximoNPC != null) {
            resumen.append("Próxima misión:\n");
            resumen.append("  • Habla con: ").append(proximoNPC).append("\n");

            String dialogo = getDialogoProximaMision();
            if (dialogo != null) {
                resumen.append("  • Diálogo: ").append(dialogo).append("\n");
            }

            String proximoNivel = getProximoNivelDisponible();
            if (proximoNivel != null) {
                resumen.append("  • Desbloqueará: ").append(proximoNivel).append("\n");
            }
        } else {
            resumen.append("¡No hay misiones pendientes!\n");
        }

        return resumen.toString();
    }

    /**
     * Muestra el estado actual de todos los eventos (para debug)
     */
    public void logEstado() {
        Gdx.app.log("EventManager", "=== ESTADO DE EVENTOS ===");

        // Niveles completados
        Gdx.app.log("EventManager", "NIVELES COMPLETADOS:");
        for (int i = 1; i <= 5; i++) {
            String nivel = "nivel_" + i;
            boolean completado = isNivelCompletado(nivel);
            Gdx.app.log("EventManager", "  " + nivel + ": " + (completado ? "✅" : "❌"));
        }

        // Eventos activos
        Gdx.app.log("EventManager", "EVENTOS ACTIVOS:");
        for (String key : eventos.keySet()) {
            if (eventos.get(key)) {
                Gdx.app.log("EventManager", "  " + key + ": ✅");
            }
        }

        // Próxima misión
        String proximoNPC = getProximoNPCConMision();
        if (proximoNPC != null) {
            Gdx.app.log("EventManager", "PRÓXIMA MISIÓN: Hablar con " + proximoNPC);
        }

        Gdx.app.log("EventManager", "=================================");
    }

    /**
     * Guarda el estado de los eventos en un mapa
     */
    public java.util.HashMap<String, Object> guardarEstado() {
        java.util.HashMap<String, Object> estado = new java.util.HashMap<>();

        // Guardar eventos
        estado.put("eventos", new java.util.HashMap<>(eventos));

        // Guardar niveles completados
        estado.put("niveles_completados", new java.util.HashMap<>(nivelesCompletados));

        Gdx.app.log("EventManager", "Estado guardado: " + eventos.size() + " eventos");
        return estado;
    }

    /**
     * Carga el estado de los eventos desde un mapa
     */
    @SuppressWarnings("unchecked")
    public void cargarEstado(java.util.HashMap<String, Object> estado) {
        if (estado == null) {
            Gdx.app.error("EventManager", "Estado nulo al cargar");
            return;
        }

        try {
            // Cargar eventos
            Object eventosObj = estado.get("eventos");
            if (eventosObj instanceof java.util.HashMap) {
                eventos = (java.util.HashMap<String, Boolean>) eventosObj;
            }

            // Cargar niveles completados
            Object nivelesObj = estado.get("niveles_completados");
            if (nivelesObj instanceof java.util.HashMap) {
                nivelesCompletados = (java.util.HashMap<String, String>) nivelesObj;
            }

            Gdx.app.log("EventManager", "Estado cargado: " + eventos.size() + " eventos");
            logEstado();

        } catch (Exception e) {
            Gdx.app.error("EventManager", "Error al cargar estado", e);
            inicializarEventos(); // Reiniciar en caso de error
        }
    }
}
