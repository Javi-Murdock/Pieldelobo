package io.github.javiergames.pieldelobo.GestorJuego;

import com.badlogic.gdx.Gdx;
import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.Dialogos.DialogoManager;

/**
 * Sistema centralizado para gestionar la progresión automática del juego.
 * Conecta niveles completados con diálogos y desbloqueo de puertas.
 * Controla el flujo: Nivel completado → Diálogo en lobby → Video → Desbloqueo siguiente nivel.
 *
 * @author Javier Gala
 * @version 1.0
 */
public class SistemaProgresion {

    // ====================== INSTANCIA SINGLETON ======================
    private static SistemaProgresion instance;

    // ====================== DEPENDENCIAS ======================
    private GameState gameState;
    private DatabaseManager db;
    private EventManager eventManager;
    private DialogoManager dialogoManager;

    // ====================== CONSTRUCTOR PRIVADO ======================
    private SistemaProgresion() {
        gameState = GameState.getInstance();
        db = DatabaseManager.getInstance();
        eventManager = EventManager.getInstance();
        dialogoManager = DialogoManager.getInstance();

        Gdx.app.log("SistemaProgresion", "✅ Sistema de progresión inicializado");
    }

    // ====================== MÉTODOS DE ACCESO ======================
    public static SistemaProgresion getInstance() {
        if (instance == null) {
            instance = new SistemaProgresion();
        }
        return instance;
    }

    // ====================== MÉTODOS PRINCIPALES ======================

    /**
     * Se llama cuando se completa un nivel.
     * Orquesta todo el proceso post-completado:
     * 1. Actualiza base de datos
     * 2. Activa eventos
     * 3. Prepara diálogo pendiente
     * 4. Prepara video correspondiente
     */
    public void onNivelCompletado(String nivelId) {
        if (nivelId == null || nivelId.isEmpty()) {
            Gdx.app.error("SistemaProgresion", "ID de nivel nulo al completar");
            return;
        }

        Gdx.app.log("SistemaProgresion", "=== 🎮 NIVEL COMPLETADO: " + nivelId + " ===");

        // 1. Actualizar base de datos
        float tiempo = gameState.getTiempoNivelActual();
        db.completarNivel(nivelId, tiempo);

        // 2. Actualizar eventos
        eventManager.onNivelCompletado(nivelId);

        // 3. Determinar y ejecutar acción correspondiente
        ejecutarAccionPostNivel(nivelId);

        // 4. Guardar progreso
        db.saveGame();

        // 5. Log detallado
        logProgresoActual();

        Gdx.app.log("SistemaProgresion", "✅ Proceso post-nivel completado para: " + nivelId);
    }

    /**
     * Ejecuta la acción correspondiente después de completar un nivel
     */
    private void ejecutarAccionPostNivel(String nivelId) {
        switch (nivelId) {
            case "nivel_1":
                prepararPuerta2();
                break;

            case "nivel_2":
                prepararPuerta3();
                break;

            case "nivel_3":
                prepararPuerta4();
                break;

            case "nivel_4":
                prepararPuerta5();
                break;

            case "nivel_5":
                juegoCompletado();
                break;

            default:
                Gdx.app.log("SistemaProgresion", "Nivel completado sin acción específica: " + nivelId);
                break;
        }
    }

    // ====================== PREPARACIÓN DE CADA PUERTA ======================

    /**
     * Prepara todo para la puerta 2 después de completar nivel 1
     */
    private void prepararPuerta2() {
        // Activar evento para Profesor Vega
        eventManager.activarEvento("profesor_vega_disponible");

        // Establecer diálogo pendiente
        gameState.setDialogoPendiente("nivel_1", "profesor_vega_puerta2");

        // Configurar video correspondiente
        gameState.setVideoPendiente("video2_1");

        Gdx.app.log("SistemaProgresion", "🎯 PUERTA 2 PREPARADA");
        Gdx.app.log("SistemaProgresion", "   • NPC: Profesor Vega (Ciber)");
        Gdx.app.log("SistemaProgresion", "   • Diálogo: profesor_vega_puerta2");
        Gdx.app.log("SistemaProgresion", "   • Opción correcta: '¡Pues claro que sí!' (índice 0)");
        Gdx.app.log("SistemaProgresion", "   • Video: video2_1");
        Gdx.app.log("SistemaProgresion", "   • Desbloquea: Nivel 2 (Las Columnas)");
    }

    /**
     * Prepara todo para la puerta 3 después de completar nivel 2
     */
    private void prepararPuerta3() {
        // Activar evento para Doctora García
        eventManager.activarEvento("doctora_garcia_mision3");

        // Establecer diálogo pendiente
        gameState.setDialogoPendiente("nivel_2", "doctora_garcia_puerta3");

        // Configurar video correspondiente
        gameState.setVideoPendiente("video3_1");

        Gdx.app.log("SistemaProgresion", "🎯 PUERTA 3 PREPARADA");
        Gdx.app.log("SistemaProgresion", "   • NPC: Doctora García");
        Gdx.app.log("SistemaProgresion", "   • Diálogo: doctora_garcia_puerta3");
        Gdx.app.log("SistemaProgresion", "   • Opción correcta: 'Claro' (índice 0)");
        Gdx.app.log("SistemaProgresion", "   • Video: video3_1");
        Gdx.app.log("SistemaProgresion", "   • Desbloquea: Nivel 3 (Las Cavernas)");
    }

    /**
     * Prepara todo para la puerta 4 después de completar nivel 3
     */
    private void prepararPuerta4() {
        // Activar evento para Profesor Leiva
        eventManager.activarEvento("profesor_leiva_historia");

        // Establecer diálogo pendiente
        gameState.setDialogoPendiente("nivel_3", "profesor_leiva_puerta4");

        // Configurar video correspondiente
        gameState.setVideoPendiente("video4_2");

        Gdx.app.log("SistemaProgresion", "🎯 PUERTA 4 PREPARADA");
        Gdx.app.log("SistemaProgresion", "   • NPC: Profesor Leiva");
        Gdx.app.log("SistemaProgresion", "   • Diálogo: profesor_leiva_puerta4");
        Gdx.app.log("SistemaProgresion", "   • Opción correcta: 'Pues claro' (índice 1)");
        Gdx.app.log("SistemaProgresion", "   • Video: video4_2");
        Gdx.app.log("SistemaProgresion", "   • Desbloquea: Nivel 4 (La Luna)");
    }

    /**
     * Prepara todo para la puerta 5 después de completar nivel 4
     */
    private void prepararPuerta5() {
        // Activar evento para Doctor Salazar
        eventManager.activarEvento("doctor_salazar_final");

        // Establecer diálogo pendiente
        gameState.setDialogoPendiente("nivel_4", "doctor_salazar_puerta5");

        // Configurar video correspondiente
        gameState.setVideoPendiente("video5_2");

        Gdx.app.log("SistemaProgresion", "🎯 PUERTA 5 PREPARADA");
        Gdx.app.log("SistemaProgresion", "   • NPC: Doctor Salazar");
        Gdx.app.log("SistemaProgresion", "   • Diálogo: doctor_salazar_puerta5");
        Gdx.app.log("SistemaProgresion", "   • Opción correcta: 'Solo por la película' (índice 1)");
        Gdx.app.log("SistemaProgresion", "   • Video: video5_2");
        Gdx.app.log("SistemaProgresion", "   • Desbloquea: Nivel 5 (El Castillo)");
    }

    /**
     * Se llama cuando se completa el juego
     */
    private void juegoCompletado() {
        Gdx.app.log("SistemaProgresion", "🎉 ¡JUEGO COMPLETADO!");
        Gdx.app.log("SistemaProgresion", "   • Todos los niveles terminados");
        Gdx.app.log("SistemaProgresion", "   • Habla con José Castellanos para diálogo especial");

        // Activar evento para diálogo final
        eventManager.activarEvento("jose_castellanos_secreto");
    }

    // ====================== MÉTODOS DE CONSULTA ======================

    /**
     * Obtiene el NPC que debe dar la siguiente misión
     */
    public String getNpcSiguienteMision() {
        // Verificar en orden de progreso
        if (eventManager.isEventoActivo("profesor_vega_disponible") && !db.isNivelCompletado("nivel_2")) {
            return "Profesor Vega (Ciber)";
        }
        if (eventManager.isEventoActivo("doctora_garcia_mision3") && !db.isNivelCompletado("nivel_3")) {
            return "Doctora García";
        }
        if (eventManager.isEventoActivo("profesor_leiva_historia") && !db.isNivelCompletado("nivel_4")) {
            return "Profesor Leiva";
        }
        if (eventManager.isEventoActivo("doctor_salazar_final") && !db.isNivelCompletado("nivel_5")) {
            return "Doctor Salazar";
        }
        if (eventManager.isEventoActivo("jose_castellanos_secreto")) {
            return "José Castellanos (Señor)";
        }

        return null;
    }

    /**
     * Obtiene el diálogo para la siguiente misión
     */
    public String getDialogoSiguienteMision() {
        String npc = getNpcSiguienteMision();

        if (npc != null) {
            if (npc.contains("Vega")) return "profesor_vega_puerta2";
            if (npc.contains("García")) return "doctora_garcia_puerta3";
            if (npc.contains("Leiva")) return "profesor_leiva_puerta4";
            if (npc.contains("Salazar")) return "doctor_salazar_puerta5";
            if (npc.contains("Castellanos")) return "jose_castellanos_puerta5";
        }

        return null;
    }

    /**
     * Obtiene el video para la siguiente misión
     */
    public String getVideoSiguienteMision() {
        String dialogo = getDialogoSiguienteMision();

        if (dialogo != null) {
            switch (dialogo) {
                case "profesor_vega_puerta2": return "video2_1";
                case "doctora_garcia_puerta3": return "video3_1";
                case "profesor_leiva_puerta4": return "video4_2";
                case "doctor_salazar_puerta5": return "video5_2";
                case "jose_castellanos_puerta5": return "video5_3";
            }
        }

        return null;
    }

    /**
     * Obtiene el nivel que desbloquea la siguiente misión
     */
    public String getNivelSiguienteMision() {
        String dialogo = getDialogoSiguienteMision();

        if (dialogo != null) {
            switch (dialogo) {
                case "profesor_vega_puerta2": return "nivel_2";
                case "doctora_garcia_puerta3": return "nivel_3";
                case "profesor_leiva_puerta4": return "nivel_4";
                case "doctor_salazar_puerta5": return "nivel_5";
                default: return null;
            }
        }

        return null;
    }

    /**
     * Verifica si hay una misión pendiente
     */
    public boolean tieneMisionPendiente() {
        return getNpcSiguienteMision() != null;
    }

    /**
     * Verifica si el jugador ha completado todos los niveles
     */
    public boolean isJuegoCompletado() {
        return db.isNivelCompletado("nivel_1") &&
            db.isNivelCompletado("nivel_2") &&
            db.isNivelCompletado("nivel_3") &&
            db.isNivelCompletado("nivel_4") &&
            db.isNivelCompletado("nivel_5");
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    /**
     * Muestra información detallada del progreso actual
     */
    public String getInfoProgreso() {
        StringBuilder info = new StringBuilder();
        info.append("=== 🎮 PROGRESO ACTUAL ===\n\n");

        // Niveles completados
        info.append("NIVELES:\n");
        for (int i = 1; i <= 5; i++) {
            String nivel = "nivel_" + i;
            boolean completado = db.isNivelCompletado(nivel);
            info.append("  • Nivel ").append(i).append(": ").append(completado ? "✅ COMPLETADO" : "❌ PENDIENTE").append("\n");
        }

        // Próxima misión
        String siguienteMision = getNpcSiguienteMision();
        if (siguienteMision != null) {
            info.append("\n⚠️ PRÓXIMA MISIÓN:\n");
            info.append("  • Habla con: ").append(siguienteMision).append("\n");

            String dialogo = getDialogoSiguienteMision();
            if (dialogo != null) {
                info.append("  • Diálogo: ").append(dialogo).append("\n");
            }

            String video = getVideoSiguienteMision();
            if (video != null) {
                info.append("  • Video: ").append(video).append("\n");
            }

            String nivel = getNivelSiguienteMision();
            if (nivel != null) {
                info.append("  • Desbloqueará: ").append(nivel).append("\n");
            }
        } else if (isJuegoCompletado()) {
            info.append("\n🎉 ¡JUEGO COMPLETADO!\n");
            info.append("  • Habla con José Castellanos\n");
            info.append("  • para el diálogo final\n");
        } else {
            info.append("\nℹ️ No hay misiones pendientes\n");
            info.append("  • Habla con Profesor Leiva\n");
            info.append("  • para comenzar\n");
        }

        // Progreso porcentual
        float progreso = db.getProgresoTotal();
        info.append("\n📊 PROGRESO TOTAL: ").append(String.format("%.1f", progreso)).append("%\n");

        return info.toString();
    }

    /**
     * Log del progreso actual (para debug)
     */
    private void logProgresoActual() {
        Gdx.app.log("SistemaProgresion", "=== 📊 PROGRESO ACTUAL ===");

        // Niveles
        for (int i = 1; i <= 5; i++) {
            String nivel = "nivel_" + i;
            boolean completado = db.isNivelCompletado(nivel);
            Gdx.app.log("SistemaProgresion", "Nivel " + i + ": " + (completado ? "✅" : "❌"));
        }

        // Próxima misión
        String siguienteMision = getNpcSiguienteMision();
        if (siguienteMision != null) {
            Gdx.app.log("SistemaProgresion", "PRÓXIMA MISIÓN: " + siguienteMision);
            Gdx.app.log("SistemaProgresion", "Diálogo: " + getDialogoSiguienteMision());
        }

        // Progreso
        float progreso = db.getProgresoTotal();
        Gdx.app.log("SistemaProgresion", "PROGRESO TOTAL: " + String.format("%.1f", progreso) + "%");
        Gdx.app.log("SistemaProgresion", "=================================");
    }

    /**
     * Reinicia todo el sistema (para nueva partida)
     */
    public void reiniciar() {
        // Reiniciar eventos
        eventManager.reiniciar();

        // Limpiar estado del juego
        gameState.reset();

        Gdx.app.log("SistemaProgresion", "🔄 Sistema de progresión reiniciado");
    }
}
