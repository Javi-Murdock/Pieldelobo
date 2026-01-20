package io.github.javiergames.pieldelobo.Decisiones;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;

import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.GestorJuego.GameState;

/**
 * Manager específico para decisiones complejas con ramificaciones.
 * Define y ejecuta decisiones que afectan el desarrollo de la historia.
 *
 * @author JavierGames
 * @version 1.0
 */
public class DecisionManager {

    private static DecisionManager instance;
    private GameState gameState;

    // Definiciones de decisiones (podrían cargarse desde JSON)
    private HashMap<String, DecisionDefinition> decisionesDefinidas;

    private DecisionManager() {
        gameState = GameState.getInstance();
        cargarDefinicionesDecisiones();
    }

    public static DecisionManager getInstance() {
        if (instance == null) {
            instance = new DecisionManager();
        }
        return instance;
    }

    /**
     * Definición de una decisión posible en el juego.
     * Contiene opciones, impactos y consecuencias.
     */
    public static class DecisionDefinition {
        /** Identificador único de la decisión */
        public String id;
        /** Texto descriptivo de la situación */
        public String texto;
        /** Opciones disponibles para el jugador */
        public String[] opciones;
        /** Descripciones del impacto de cada opción */
        public String[] impactos;
        /** Consecuencias específicas por opción */
        public HashMap<String, Object>[] consecuencias;
        /** Indica si la decisión afecta el final del juego */
        public boolean esCritica;
        /** IDs de decisiones previas requeridas */
        public String[] requisitos;
    }

    /**
     * Carga las definiciones de decisiones (hardcodeado por ahora)
     */
    @SuppressWarnings("unchecked")
    private void cargarDefinicionesDecisiones() {
        decisionesDefinidas = new HashMap<>();

        // Ejemplo: Decisión con el doctor en el lobby
        DecisionDefinition docDecision = new DecisionDefinition();
        docDecision.id = "doctor_inicio_opcion";
        docDecision.texto = "El doctor te ofrece una píldora experimental. ¿La aceptas?";
        docDecision.opciones = new String[] {
            "Aceptar la píldora",
            "Rechazar la píldora",
            "Pedir más información"
        };
        docDecision.impactos = new String[] {
            "Positivo: Obtienes habilidad especial pero pierdes salud",
            "Neutral: Mantienes tu estado actual",
            "Neutral: El doctor te explica los riesgos"
        };
        docDecision.esCritica = true;
        docDecision.requisitos = new String[] {}; // Sin requisitos

        // Inicializar array de consecuencias
        docDecision.consecuencias = new HashMap[3];

        // Opción 0: Aceptar píldora
        HashMap<String, Object> consecuencias0 = new HashMap<>();
        consecuencias0.put("habilidad_especial", "vision_nocturna");
        consecuencias0.put("salud_maxima", -20);
        docDecision.consecuencias[0] = consecuencias0;

        // Opción 1: Rechazar
        HashMap<String, Object> consecuencias1 = new HashMap<>();
        consecuencias1.put("confianza_doctor", -10);
        docDecision.consecuencias[1] = consecuencias1;

        // Opción 2: Pedir información
        HashMap<String, Object> consecuencias2 = new HashMap<>();
        docDecision.consecuencias[2] = consecuencias2;

        decisionesDefinidas.put(docDecision.id, docDecision);


    }

    /**
     * Ejecuta una decisión y registra sus consecuencias.
     * Aplica inmediatamente las consecuencias definidas.
     *
     * @param decisionId ID de la decisión a ejecutar
     * @param opcionElegida Índice de la opción seleccionada
     */
    @SuppressWarnings("unchecked")
    public void ejecutarDecision(String decisionId, int opcionElegida) {
        DecisionDefinition def = decisionesDefinidas.get(decisionId);
        if (def == null || opcionElegida < 0 || opcionElegida >= def.opciones.length) {
            Gdx.app.error("DecisionManager", "Decisión inválida: " + decisionId);
            return;
        }

        // Verificar requisitos
        if (!cumpleRequisitos(def)) {
            Gdx.app.log("DecisionManager", "No se cumplen los requisitos para: " + decisionId);
            return;
        }

        // Obtener consecuencias para la opción elegida
        HashMap<String, Object> consecuencias = null;
        if (def.consecuencias != null && opcionElegida < def.consecuencias.length) {
            consecuencias = def.consecuencias[opcionElegida];
        } else {
            consecuencias = new HashMap<>();
        }

        // Registrar en la base de datos
        gameState.registrarDecision(
            decisionId + "_opcion" + opcionElegida,
            def.opciones[opcionElegida],
            def.impactos[opcionElegida],
            consecuencias
        );

        Gdx.app.log("DecisionManager",
            "Decisión ejecutada: " + decisionId + " -> Opción " + opcionElegida);
    }

    /**
     * Verifica si se cumplen los requisitos para una decisión
     */
    private boolean cumpleRequisitos(DecisionDefinition def) {
        if (def.requisitos == null || def.requisitos.length == 0) {
            return true;
        }

        DatabaseManager db = DatabaseManager.getInstance();
        for (String requisitoId : def.requisitos) {
            if (!db.isDecisionTomada(requisitoId)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtiene una definición de decisión
     */
    public DecisionDefinition getDecision(String decisionId) {
        return decisionesDefinidas.get(decisionId);
    }

    /**
     * Verifica si una decisión está disponible (cumple requisitos)
     */
    public boolean isDecisionDisponible(String decisionId) {
        DecisionDefinition def = decisionesDefinidas.get(decisionId);
        if (def == null) return false;
        return cumpleRequisitos(def);
    }

    /**
     * Calcula la tendencia moral del jugador (bueno/malo/neutral)
     */
    public String getTendenciaMoral() {
        DatabaseManager.GameStats stats = DatabaseManager.getInstance().getStats();
        int total = stats.totalDecisiones;
        if (total == 0) return "Neutral";

        float ratio = (float) stats.decisionesBuenas / total;

        if (ratio > 0.7) return "Heroico";
        if (ratio > 0.6) return "Bueno";
        if (ratio > 0.4) return "Neutral";
        if (ratio > 0.2) return "Questionable";
        return "Malvado";
    }
}
