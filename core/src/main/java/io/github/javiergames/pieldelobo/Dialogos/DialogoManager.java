package io.github.javiergames.pieldelobo.Dialogos;

import com.badlogic.gdx.Gdx;
import java.util.HashMap;
import java.util.Map;

import io.github.javiergames.pieldelobo.GestorJuego.GameState;

/**
 * Gestor centralizado de diálogos extendido para incluir decisiones, consecuencias y videos.
 * NUEVO: Sistema completo de tracking de decisiones del jugador para desbloquear niveles.
 * MODIFICADO: Ahora registra qué opción eligió el jugador en cada diálogo.
 *
 * @author JavierGames
 * @version 2.1
 */
public class DialogoManager {

    // ====================== INSTANCIA SINGLETON ======================
    private static DialogoManager instancia;

    // ====================== ALMACENAMIENTO DE DATOS ======================
    private Map<String, SistemaDialogos.NodoDialogo> dialogos;
    private GameState gameState;

    // Mapa de consecuencias por diálogo
    private Map<String, Map<String, Object>> consecuenciasDialogos;

    // Para almacenar consecuencias pendientes que se mostrarán después del diálogo
    private Map<String, String> consecuenciasPendientes;

    // ====================== NUEVO: TRACKING DE DECISIONES ======================
    /**
     * Mapa que almacena qué opción eligió el jugador en cada diálogo con NPC.
     * Clave: ID del diálogo del NPC (ej: "profesor_leiva_inicio")
     * Valor: Índice de la opción seleccionada (0, 1, 2...)
     */
    private Map<String, Integer> decisionesJugador;

    // ====================== CONSTRUCTOR PRIVADO ======================

    /**
     * Constructor privado para patrón Singleton.
     * Carga diálogos y consecuencias al inicializar.
     */
    private DialogoManager() {
        dialogos = DialogoLoader.cargarDesdeJSON("dialogos/dialogos.json");
        consecuenciasDialogos = new HashMap<>();
        consecuenciasPendientes = new HashMap<>();
        decisionesJugador = new HashMap<>(); // NUEVO: Inicializar tracking
        gameState = GameState.getInstance();
        cargarConsecuencias();

        Gdx.app.log("DialogoManager", "Inicializado - " + dialogos.size() + " diálogos cargados");
    }

    // ====================== MÉTODOS DE ACCESO SINGLETON ======================

    /**
     * Obtiene la instancia única de DialogoManager.
     *
     * @return Instancia de DialogoManager
     */
    public static DialogoManager getInstance() {
        if (instancia == null) {
            instancia = new DialogoManager();
        }
        return instancia;
    }

    // ====================== NUEVO: MÉTODOS DE TRACKING DE DECISIONES ======================

    /**
     * Registra la opción que eligió el jugador en un diálogo con NPC.
     * Esto es crucial para determinar si desbloquea un nivel.
     *
     * @param dialogoId ID del diálogo del NPC
     * @param opcionIndex Índice de la opción seleccionada
     */
    public void registrarDecisionJugador(String dialogoId, int opcionIndex) {
        if (dialogoId == null || opcionIndex < 0) {
            Gdx.app.error("DialogoManager", "Intento de registrar decisión inválida");
            return;
        }

        decisionesJugador.put(dialogoId, opcionIndex);

        Gdx.app.log("DialogoManager",
            "✅ Decisión registrada: " + dialogoId + " -> Opción " + opcionIndex);

        // Verificar inmediatamente si esta decisión desbloquea un nivel
        verificarDesbloqueoNivelPorDecision(dialogoId, opcionIndex);
    }

    /**
     * Verifica si el jugador tomó una decisión específica.
     *
     * @param dialogoId ID del diálogo del NPC
     * @return true si el jugador ya tomó una decisión en este diálogo
     */
    public boolean jugadorTomoDecision(String dialogoId) {
        return decisionesJugador.containsKey(dialogoId);
    }

    /**
     * Obtiene la opción que eligió el jugador en un diálogo.
     *
     * @param dialogoId ID del diálogo del NPC
     * @return Índice de la opción seleccionada, o -1 si no tomó decisión
     */
    public int getOpcionElegida(String dialogoId) {
        return decisionesJugador.getOrDefault(dialogoId, -1);
    }

    /**
     * Verifica si la decisión tomada por el jugador desbloquea un nivel.
     * Basado en las opciones correctas definidas en los diálogos JSON.
     *
     * @param dialogoId ID del diálogo del NPC
     * @param opcionIndex Índice de la opción seleccionada
     * @return true si la opción desbloquea un nivel
     */
    private boolean verificarDesbloqueoNivelPorDecision(String dialogoId, int opcionIndex) {
        // Mapa de diálogos que desbloquean niveles y cuál es la opción correcta
        Map<String, Integer> opcionesCorrectas = new HashMap<>();

        // Configurar según tus diálogos JSON:
        // Diálogo ID -> Índice de opción correcta (0-indexed)
        opcionesCorrectas.put("profesor_leiva_inicio", 0);       // Preguntar por el abuelo
        opcionesCorrectas.put("profesor_vega_puerta2", 0);       // "¡Pues claro que sí!"
        opcionesCorrectas.put("doctora_garcia_puerta3", 0);      // "Claro"
        opcionesCorrectas.put("profesor_leiva_puerta4", 1);      // "Pues claro" (índice 1)
        opcionesCorrectas.put("doctor_salazar_puerta5", 1);      // "Solo por la película" (índice 1)

        Integer opcionCorrecta = opcionesCorrectas.get(dialogoId);

        if (opcionCorrecta != null) {
            boolean esCorrecta = (opcionIndex == opcionCorrecta);

            Gdx.app.log("DialogoManager",
                "Verificando decisión: " + dialogoId +
                    " -> Elegida: " + opcionIndex +
                    ", Correcta: " + opcionCorrecta +
                    ", Resultado: " + (esCorrecta ? "✅ CORRECTA" : "❌ INCORRECTA"));

            return esCorrecta;
        }

        return false;
    }

    /**
     * Obtiene el ID del nivel que desbloquea un diálogo específico.
     *
     * @param dialogoId ID del diálogo del NPC
     * @return ID del nivel a desbloquear, o null si no desbloquea ningún nivel
     */
    public String getNivelADesbloquear(String dialogoId) {
        // Mapeo de diálogos a niveles
        Map<String, String> dialogoANivel = new HashMap<>();
        dialogoANivel.put("profesor_leiva_inicio", "nivel_1");
        dialogoANivel.put("profesor_vega_puerta2", "nivel_2");
        dialogoANivel.put("doctora_garcia_puerta3", "nivel_3");
        dialogoANivel.put("profesor_leiva_puerta4", "nivel_4");
        dialogoANivel.put("doctor_salazar_puerta5", "nivel_5");

        return dialogoANivel.get(dialogoId);
    }

    // ====================== CARGA DE CONSECUENCIAS ======================

    /**
     * Carga todas las consecuencias especiales para cada diálogo, incluyendo videos.
     * MODIFICADO: Ahora incluye configuraciones de desbloqueo de niveles.
     */
    private void cargarConsecuencias() {
        Gdx.app.log("DialogoManager", "=== CARGANDO CONSECUENCIAS DE DIÁLOGOS ===");

        // ====================== PROFESOR LEIVA - PUERTA 1 ======================
        Map<String, Object> consecuenciasProfesorInicio = new HashMap<>();
        consecuenciasProfesorInicio.put("tipo", "dialogo_consecuencia");
        consecuenciasProfesorInicio.put("accion", "desbloquear_nivel_si_correcto");
        consecuenciasProfesorInicio.put("nivel_id", "nivel_1");
        consecuenciasProfesorInicio.put("opcion_correcta", 0); // Preguntar por el abuelo
        consecuenciasProfesorInicio.put("mensaje_correcto", "¡Nivel 1: La Villa desbloqueado!\nVe a la puerta del laboratorio.");
        consecuenciasProfesorInicio.put("mensaje_incorrecto", "Tal vez deberías mostrar más interés...");
        consecuenciasDialogos.put("profesor_leiva_inicio", consecuenciasProfesorInicio);

        // Video del abuelo
        Map<String, Object> consecuenciasVideo1_1 = new HashMap<>();
        consecuenciasVideo1_1.put("tipo", "ver_video");
        consecuenciasVideo1_1.put("video_id", "video1_1");
        consecuenciasVideo1_1.put("mensaje", "Abuelo del agente - Primera misión en Allariz");
        consecuenciasDialogos.put("profesor_leiva_abuelo_video", consecuenciasVideo1_1);

        // Video si elige opción negativa
        Map<String, Object> consecuenciasVideo1_2 = new HashMap<>();
        consecuenciasVideo1_2.put("tipo", "ver_video");
        consecuenciasVideo1_2.put("video_id", "video1_2");
        consecuenciasVideo1_2.put("mensaje", "Genio del nuevo - Carácter fuerte");
        consecuenciasDialogos.put("profesor_leiva_molesto", consecuenciasVideo1_2);

        // ====================== DOCTOR SALAZAR - VIDEOS PUERTA 1 ======================
// Video para cuando el doctor Salazar NO SABE algo
        Map<String, Object> consecuenciasVideo1_3 = new HashMap<>();
        consecuenciasVideo1_3.put("tipo", "ver_video");
        consecuenciasVideo1_3.put("video_id", "video1_3");  // ← Video 1_3
        consecuenciasVideo1_3.put("mensaje", "Criptología ibérica - Investigación compleja");
        consecuenciasDialogos.put("doctor_salazar_nosabe", consecuenciasVideo1_3);

// Video para cuando habla trasgus
        Map<String, Object> consecuenciasVideo1_4 = new HashMap<>();
        consecuenciasVideo1_4.put("tipo", "ver_video");
        consecuenciasVideo1_4.put("video_id", "video1_4");  // ← Video 1_4
        consecuenciasVideo1_4.put("mensaje", "Trasgu escapando - Leyenda gallega");
        consecuenciasDialogos.put("doctor_salazar_pelicula", consecuenciasVideo1_4);

        // ====================== PROFESOR VEGA - PUERTA 2 ======================
        Map<String, Object> consecuenciasProfesorVega = new HashMap<>();
        consecuenciasProfesorVega.put("tipo", "dialogo_consecuencia");
        consecuenciasProfesorVega.put("accion", "desbloquear_nivel_si_correcto");
        consecuenciasProfesorVega.put("nivel_id", "nivel_2");
        consecuenciasProfesorVega.put("opcion_correcta", 0); // "¡Pues claro que sí!"
        consecuenciasProfesorVega.put("mensaje_correcto", "¡Nivel 2: Las Columnas desbloqueado!\nTe espera una nueva puerta.");
        consecuenciasProfesorVega.put("mensaje_incorrecto", "Parece que no estás listo para más desafíos...");
        consecuenciasDialogos.put("profesor_vega_puerta2", consecuenciasProfesorVega);

        // Videos del profesor Vega
        Map<String, Object> consecuenciasVideo2_1 = new HashMap<>();
        consecuenciasVideo2_1.put("tipo", "ver_video");
        consecuenciasVideo2_1.put("video_id", "video2_1");
        consecuenciasVideo2_1.put("mensaje", "Siguiente misión - Aceptando desafío");
        consecuenciasDialogos.put("profesor_vega_puerta2_opcion1", consecuenciasVideo2_1);

        Map<String, Object> consecuenciasVideo2_2 = new HashMap<>();
        consecuenciasVideo2_2.put("tipo", "ver_video");
        consecuenciasVideo2_2.put("video_id", "video2_2");
        consecuenciasVideo2_2.put("mensaje", "Morcilla vegana - Sangre fría");
        consecuenciasDialogos.put("profesor_vega_puerta2_opcion2", consecuenciasVideo2_2);

        // ====================== DOCTORA GARCÍA - PUERTA 3 ======================
        Map<String, Object> consecuenciasDoctoraGarcia = new HashMap<>();
        consecuenciasDoctoraGarcia.put("tipo", "dialogo_consecuencia");
        consecuenciasDoctoraGarcia.put("accion", "desbloquear_nivel_si_correcto");
        consecuenciasDoctoraGarcia.put("nivel_id", "nivel_3");
        consecuenciasDoctoraGarcia.put("opcion_correcta", 0); // "Claro"
        consecuenciasDoctoraGarcia.put("mensaje_correcto", "¡Nivel 3: Las Cavernas desbloqueado!\nRecoge los datos solicitados.");
        consecuenciasDoctoraGarcia.put("mensaje_incorrecto", "Quizás en otro momento...");
        consecuenciasDialogos.put("doctora_garcia_puerta3", consecuenciasDoctoraGarcia);

        // Videos de la doctora García
        Map<String, Object> consecuenciasVideo3_1 = new HashMap<>();
        consecuenciasVideo3_1.put("tipo", "ver_video");
        consecuenciasVideo3_1.put("video_id", "video3_1");
        consecuenciasVideo3_1.put("mensaje", "Datos del nivel 3 - Misión de recolección");
        consecuenciasDialogos.put("doctora_garcia_puerta3_claro", consecuenciasVideo3_1);

        Map<String, Object> consecuenciasVideo3_2 = new HashMap<>();
        consecuenciasVideo3_2.put("tipo", "ver_video");
        consecuenciasVideo3_2.put("video_id", "video3_2");
        consecuenciasVideo3_2.put("mensaje", "Más tarde - Postergando tarea");
        consecuenciasDialogos.put("doctora_garcia_puerta3_ocupado", consecuenciasVideo3_2);

        // ====================== DOCTOR SALAZAR - PUERTA 3 ======================
// Video 3_3 - Doctor Salazar puerta 3 "verlo"
        Map<String, Object> consecuenciasVideo3_3 = new HashMap<>();
        consecuenciasVideo3_3.put("tipo", "ver_video");
        consecuenciasVideo3_3.put("video_id", "video3_3");
        consecuenciasVideo3_3.put("mensaje", "Bebé dragón - Leyenda del monstruo");
        consecuenciasDialogos.put("doctor_salazar_puerta3_verlo", consecuenciasVideo3_3);

// Video 3_4 - Doctor Salazar puerta 3 "legal"
        Map<String, Object> consecuenciasVideo3_4 = new HashMap<>();
        consecuenciasVideo3_4.put("tipo", "ver_video");
        consecuenciasVideo3_4.put("video_id", "video3_4");
        consecuenciasVideo3_4.put("mensaje", "WhatsApp - Comunicación moderna");
        consecuenciasDialogos.put("doctor_salazar_puerta3_legal", consecuenciasVideo3_4);

        // ====================== PROFESOR LEIVA - PUERTA 4 ======================
        Map<String, Object> consecuenciasProfesorLeiva4 = new HashMap<>();
        consecuenciasProfesorLeiva4.put("tipo", "dialogo_consecuencia");
        consecuenciasProfesorLeiva4.put("accion", "desbloquear_nivel_si_correcto");
        consecuenciasProfesorLeiva4.put("nivel_id", "nivel_4");
        consecuenciasProfesorLeiva4.put("opcion_correcta", 1); // "Pues claro" (índice 1)
        consecuenciasProfesorLeiva4.put("mensaje_correcto", "¡Nivel 4: La Luna desbloqueado!\nContinúa tu entrenamiento.");
        consecuenciasProfesorLeiva4.put("mensaje_incorrecto", "Tu tiempo es valioso, mejor continúa.");
        consecuenciasDialogos.put("profesor_leiva_puerta4", consecuenciasProfesorLeiva4);

        // Videos del profesor Leiva (puerta 4)
        Map<String, Object> consecuenciasVideo4_1 = new HashMap<>();
        consecuenciasVideo4_1.put("tipo", "ver_video");
        consecuenciasVideo4_1.put("video_id", "video4_1");
        consecuenciasVideo4_1.put("mensaje", "Seguir avanzando - Prioridades claras");
        consecuenciasDialogos.put("profesor_leiva_puerta4_ocupado", consecuenciasVideo4_1);

        Map<String, Object> consecuenciasVideo4_2 = new HashMap<>();
        consecuenciasVideo4_2.put("tipo", "ver_video");
        consecuenciasVideo4_2.put("video_id", "video4_2");
        consecuenciasVideo4_2.put("mensaje", "Historia de puertas - Lección aburrida");
        consecuenciasDialogos.put("profesor_leiva_puerta4_claro", consecuenciasVideo4_2);

        // ====================== DOCTORA GARCÍA - PUERTA 4 ======================
// Video 4_3 - Doctora García puerta 4 "apuesta"
        Map<String, Object> consecuenciasVideo4_3 = new HashMap<>();
        consecuenciasVideo4_3.put("tipo", "ver_video");
        consecuenciasVideo4_3.put("video_id", "video4_3");
        consecuenciasVideo4_3.put("mensaje", "Apuesta ganada - Suerte del principiante");
        consecuenciasDialogos.put("doctora_garcia_puerta4_apuesta", consecuenciasVideo4_3);

// Video 4_4 - Doctora García puerta 4 "comision"
        Map<String, Object> consecuenciasVideo4_4 = new HashMap<>();
        consecuenciasVideo4_4.put("tipo", "ver_video");
        consecuenciasVideo4_4.put("video_id", "video4_4");
        consecuenciasVideo4_4.put("mensaje", "Comisión justa - Ética profesional");
        consecuenciasDialogos.put("doctora_garcia_puerta4_comision", consecuenciasVideo4_4);

        // ====================== DOCTOR SALAZAR - PUERTA 5 ======================
        Map<String, Object> consecuenciasDoctorSalazar5 = new HashMap<>();
        consecuenciasDoctorSalazar5.put("tipo", "dialogo_consecuencia");
        consecuenciasDoctorSalazar5.put("accion", "desbloquear_nivel_si_correcto");
        consecuenciasDoctorSalazar5.put("nivel_id", "nivel_5");
        consecuenciasDoctorSalazar5.put("opcion_correcta", 1); // "Solo por la película" (índice 1)
        consecuenciasDoctorSalazar5.put("mensaje_correcto", "¡Nivel 5: Castillo de Montalbán desbloqueado!\nTu última misión te espera.");
        consecuenciasDoctorSalazar5.put("mensaje_incorrecto", "Sin conocer la historia, mejor no arriesgarse.");
        consecuenciasDialogos.put("doctor_salazar_puerta5", consecuenciasDoctorSalazar5);

        // Videos del doctor Salazar (puerta 5)
        Map<String, Object> consecuenciasVideo5_1 = new HashMap<>();
        consecuenciasVideo5_1.put("tipo", "ver_video");
        consecuenciasVideo5_1.put("video_id", "video5_1");
        consecuenciasVideo5_1.put("mensaje", "Historia de la zona - Ignorancia peligrosa");
        consecuenciasDialogos.put("doctor_salazar_puerta5_nosabe", consecuenciasVideo5_1);

        Map<String, Object> consecuenciasVideo5_2 = new HashMap<>();
        consecuenciasVideo5_2.put("tipo", "ver_video");
        consecuenciasVideo5_2.put("video_id", "video5_2");
        consecuenciasVideo5_2.put("mensaje", "Historia de Romasanta - Leyenda gallega");
        consecuenciasDialogos.put("doctor_salazar_puerta5_pelicula", consecuenciasVideo5_2);

        // ====================== JOSÉ CASTELLANOS - PUERTA 5 ======================
// Video 5_3 - José Castellanos puerta 5 "videojuego"
        Map<String, Object> consecuenciasVideo5_3 = new HashMap<>();
        consecuenciasVideo5_3.put("tipo", "ver_video");
        consecuenciasVideo5_3.put("video_id", "video5_3");
        consecuenciasVideo5_3.put("mensaje", "Simulación Java - Programación avanzada");
        consecuenciasDialogos.put("jose_castellanos_puerta5_videojuego", consecuenciasVideo5_3);

// Video 5_4 - José Castellanos puerta 5 "matrix"
        Map<String, Object> consecuenciasVideo5_4 = new HashMap<>();
        consecuenciasVideo5_4.put("tipo", "ver_video");
        consecuenciasVideo5_4.put("video_id", "video5_4");
        consecuenciasVideo5_4.put("mensaje", "Antisistema matrix - Teorías conspirativas");
        consecuenciasDialogos.put("jose_castellanos_puerta5_matrix", consecuenciasVideo5_4);

        // ====================== LOG DE CARGA ======================
        Gdx.app.log("DialogoManager", "Consecuencias cargadas para " + consecuenciasDialogos.size() + " diálogos");
        Gdx.app.log("DialogoManager", "Sistema de desbloqueo por diálogos configurado correctamente");
    }

    // ====================== MÉTODOS DE GESTIÓN DE DIÁLOGOS ======================

    /**
     * Obtiene un diálogo por su ID.
     *
     * @param id ID del diálogo a obtener
     * @return NodoDialogo correspondiente, o diálogo de error si no existe
     */
    public SistemaDialogos.NodoDialogo obtenerDialogo(String id) {
        if (id == null || id.isEmpty()) {
            Gdx.app.error("DialogoManager", "ID de diálogo nulo o vacío");
            return crearDialogoError("null_id");
        }

        SistemaDialogos.NodoDialogo dialogo = dialogos.get(id);
        if (dialogo == null) {
            Gdx.app.error("DialogoManager", "Diálogo no encontrado: " + id);
            return crearDialogoError(id);
        }
        return dialogo;
    }

    /**
     * Obtiene el ID de diálogo inicial por tipo de NPC.
     *
     * @param tipoNPC Tipo del NPC (ej: "profesor", "doctor", "doctora")
     * @return ID del diálogo inicial correspondiente
     */
    public String getDialogoIdPorTipo(String tipoNPC) {
        if (tipoNPC == null) {
            return "default_inicio";
        }

        switch (tipoNPC.toLowerCase()) {
            case "profesor":
                return "profesor_leiva_inicio";
            case "ciber":
                return "profesor_vega_inicio";
            case "doctora":
                return "doctora_garcia_inicio";
            case "doctor":
                return "doctor_salazar_inicio";
            case "medico":
                return "marta_santos_inicio";
            case "senor":
                return "jose_castellanos_inicio";
            default:
                Gdx.app.log("DialogoManager", "Tipo NPC desconocido: " + tipoNPC + ", usando default");
                return "default_inicio";
        }
    }

    // ====================== MÉTODOS DE APLICACIÓN DE CONSECUENCIAS ======================

    /**
     * Aplica las consecuencias de un diálogo.
     * Incluye desbloqueo de niveles y reproducción de videos.
     *
     * @param idDialogo ID del diálogo actual
     * @param indiceOpcion Índice de la opción seleccionada
     * @return true si se aplicó una consecuencia importante
     */
    public boolean aplicarConsecuencias(String idDialogo, int indiceOpcion) {
        if (idDialogo == null) {
            Gdx.app.error("DialogoManager", "ID de diálogo nulo al aplicar consecuencias");
            return false;
        }

        // NUEVO: Registrar la decisión del jugador ANTES de procesar consecuencias
        if (indiceOpcion >= 0) {
            registrarDecisionJugador(idDialogo, indiceOpcion);
            Gdx.app.log("DialogoManager", "Decisión del jugador registrada: " +
                idDialogo + " -> Opción " + indiceOpcion);
        }

        Map<String, Object> consecuencias = consecuenciasDialogos.get(idDialogo);
        if (consecuencias == null) {
            Gdx.app.debug("DialogoManager", "No hay consecuencias para: " + idDialogo);
            return false;
        }

        String tipo = (String) consecuencias.get("tipo");
        if (tipo == null) {
            Gdx.app.error("DialogoManager", "Consecuencia sin tipo para: " + idDialogo);
            return false;
        }

        boolean consecuenciaImportante = false;

        try {
            switch (tipo) {
                case "desbloquear_nivel":
                    consecuenciaImportante = procesarDesbloqueoNivel(consecuencias);
                    break;

                case "ver_video":
                    procesarVideo(consecuencias);
                    break;

                case "dialogo_consecuencia":
                    // NUEVO: Procesa desbloqueo condicional basado en la opción elegida
                    consecuenciaImportante = procesarDialogoConsecuencia(consecuencias, idDialogo, indiceOpcion);
                    break;

                case "registrar_decision":
                    procesarRegistroDecision(consecuencias, idDialogo, indiceOpcion);
                    break;

                case "consejo":
                    procesarConsejo(consecuencias);
                    break;

                default:
                    Gdx.app.log("DialogoManager", "Tipo de consecuencia desconocido: " + tipo);
            }
        } catch (Exception e) {
            Gdx.app.error("DialogoManager", "Error procesando consecuencias para: " + idDialogo, e);
        }

        return consecuenciaImportante;
    }

    /**
     * Procesa el desbloqueo de un nivel.
     */
    private boolean procesarDesbloqueoNivel(Map<String, Object> consecuencias) {
        String nivelId = (String) consecuencias.get("nivel_id");
        if (nivelId == null) {
            Gdx.app.error("DialogoManager", "Desbloqueo nivel sin ID");
            return false;
        }

        gameState.desbloquearNivel(nivelId);

        String mensaje = (String) consecuencias.get("mensaje");
        if (mensaje == null) {
            mensaje = "¡Nivel desbloqueado: " + nivelId + "!";
        }

        añadirConsecuenciaPendiente("desbloquear_nivel", mensaje);
        Gdx.app.log("DialogoManager", "Nivel desbloqueado: " + nivelId);

        return true;
    }

    /**
     * Procesa diálogos con consecuencias condicionales.
     * NUEVO: Verifica si la opción elegida es la correcta para desbloquear niveles.
     */
    private boolean procesarDialogoConsecuencia(Map<String, Object> consecuencias, String idDialogo, int indiceOpcion) {
        boolean consecuenciaImportante = false;
        String accion = (String) consecuencias.get("accion");

        if (accion == null) {
            Gdx.app.error("DialogoManager", "Diálogo consecuencia sin acción");
            return false;
        }

        // NUEVO: Procesar desbloqueo condicional basado en opción elegida
        if ("desbloquear_nivel_si_correcto".equals(accion)) {
            String nivelId = (String) consecuencias.get("nivel_id");
            Integer opcionCorrecta = (Integer) consecuencias.get("opcion_correcta");

            if (nivelId == null || opcionCorrecta == null) {
                Gdx.app.error("DialogoManager", "Faltan parámetros para desbloqueo condicional");
                return false;
            }

            // Verificar si el jugador eligió la opción correcta
            boolean opcionCorrectaElegida = (indiceOpcion == opcionCorrecta);

            Gdx.app.log("DialogoManager",
                "Verificando opción: Diálogo=" + idDialogo +
                    ", Elegida=" + indiceOpcion +
                    ", Correcta=" + opcionCorrecta +
                    ", Resultado=" + (opcionCorrectaElegida ? "✅" : "❌"));

            if (opcionCorrectaElegida) {
                // ¡Opción correcta! Desbloquear nivel
                gameState.desbloquearNivel(nivelId);
                consecuenciaImportante = true;

                String mensajeCorrecto = (String) consecuencias.get("mensaje_correcto");
                if (mensajeCorrecto == null) {
                    mensajeCorrecto = "¡Nivel desbloqueado: " + nivelId + "!";
                }

                añadirConsecuenciaPendiente("desbloqueo_exitoso", mensajeCorrecto);
                Gdx.app.log("DialogoManager", "✅ Nivel desbloqueado por opción correcta: " + nivelId);

            } else {
                // Opción incorrecta, mostrar mensaje de error
                String mensajeIncorrecto = (String) consecuencias.get("mensaje_incorrecto");
                if (mensajeIncorrecto != null) {
                    añadirConsecuenciaPendiente("consejo", mensajeIncorrecto);
                }

                Gdx.app.log("DialogoManager", "❌ Opción incorrecta, nivel NO desbloqueado: " + nivelId);
            }
        }

        return consecuenciaImportante;
    }

    /**
     * Procesa la reproducción de un video.
     */
    private void procesarVideo(Map<String, Object> consecuencias) {
        String videoId = (String) consecuencias.get("video_id");
        String videoMensaje = (String) consecuencias.get("mensaje");

        if (videoId == null) {
            Gdx.app.error("DialogoManager", "Video sin ID");
            return;
        }

        // Marcar video como visto en la base de datos
        gameState.marcarVideoVisto(videoId);

        // Guardar el video para reproducir después del diálogo
        añadirConsecuenciaPendiente("video_pendiente_id", videoId);

        if (videoMensaje != null) {
            añadirConsecuenciaPendiente("video_pendiente_mensaje", videoMensaje);
        } else {
            añadirConsecuenciaPendiente("video_pendiente_mensaje", "Reproduciendo video: " + videoId);
        }

        Gdx.app.log("DialogoManager", "Video programado: " + videoId + " - " + videoMensaje);
    }

    /**
     * Procesa el registro de una decisión.
     */
    private void procesarRegistroDecision(Map<String, Object> consecuencias, String idDialogo, int indiceOpcion) {
        String decisionId = (String) consecuencias.get("decision_id");
        if (decisionId == null) {
            Gdx.app.error("DialogoManager", "Decisión sin ID");
            return;
        }

        String decisionTexto = (String) consecuencias.get("decision_texto");
        String decisionImpacto = (String) consecuencias.get("decision_impacto");
        if (decisionImpacto == null) {
            decisionImpacto = "Consecuencia de conversación";
        }

        HashMap<String, Object> consDecision = new HashMap<>();
        consDecision.put("dialogo_id", idDialogo);
        consDecision.put("opcion_elegida", indiceOpcion);

        gameState.registrarDecision(
            decisionId,
            decisionTexto != null ? decisionTexto : "Decisión en diálogo",
            decisionImpacto,
            consDecision
        );
        Gdx.app.log("DialogoManager", "Decisión registrada: " + decisionId);
    }

    /**
     * Procesa consejos para el jugador.
     */
    private void procesarConsejo(Map<String, Object> consecuencias) {
        String consejoMensaje = (String) consecuencias.get("mensaje");
        if (consejoMensaje != null) {
            añadirConsecuenciaPendiente("consejo", consejoMensaje);
            Gdx.app.debug("DialogoManager", "Consejo añadido: " + consejoMensaje);
        }
    }

    // ====================== GESTIÓN DE CONSECUENCIAS PENDIENTES ======================

    /**
     * Añade una consecuencia pendiente para mostrar después del diálogo.
     *
     * @param tipo Tipo de consecuencia ("desbloqueo_exitoso", "video_pendiente_id", "consejo")
     * @param mensaje Mensaje o ID asociado a la consecuencia
     */
    private void añadirConsecuenciaPendiente(String tipo, String mensaje) {
        if (consecuenciasPendientes == null) {
            consecuenciasPendientes = new HashMap<>();
        }

        if (tipo == null || mensaje == null) {
            Gdx.app.error("DialogoManager", "Intento de añadir consecuencia con valores nulos");
            return;
        }

        consecuenciasPendientes.put(tipo, mensaje);
        Gdx.app.debug("DialogoManager", "Consecuencia pendiente: " + tipo + " -> " + mensaje);
    }

    /**
     * Verifica si hay consecuencias pendientes.
     *
     * @return true si hay consecuencias pendientes, false en caso contrario
     */
    public boolean tieneConsecuenciasPendientes() {
        return consecuenciasPendientes != null && !consecuenciasPendientes.isEmpty();
    }

    /**
     * Obtiene las consecuencias pendientes sin limpiarlas (solo para verificar).
     * Útil para verificar si hay videos antes de limpiar el diálogo.
     *
     * @return Copia del mapa de consecuencias pendientes
     */
    public Map<String, String> obtenerConsecuenciasPendientesParaVerificar() {
        if (consecuenciasPendientes == null) {
            return new HashMap<>();
        }
        return new HashMap<>(consecuenciasPendientes);
    }

    /**
     * Obtiene y limpia todas las consecuencias pendientes.
     * Después de llamar a este método, el mapa de consecuencias pendientes queda vacío.
     *
     * @return Mapa con las consecuencias pendientes
     */
    public Map<String, String> obtenerYLimpiarConsecuenciasPendientes() {
        Map<String, String> temp = new HashMap<>();

        if (consecuenciasPendientes != null) {
            temp.putAll(consecuenciasPendientes);
            consecuenciasPendientes.clear();
        }

        Gdx.app.log("DialogoManager", "Consecuencias pendientes obtenidas: " + temp.size() + " items");
        return temp;
    }

    // ====================== MÉTODOS DE UTILIDAD ======================

    /**
     * Verifica si un diálogo tiene consecuencias importantes (desbloqueos de nivel).
     *
     * @param idDialogo ID del diálogo a verificar
     * @return true si tiene consecuencias importantes, false en caso contrario
     */
    public boolean tieneConsecuenciasImportantes(String idDialogo) {
        Map<String, Object> consecuencias = consecuenciasDialogos.get(idDialogo);
        if (consecuencias == null) return false;

        String tipo = (String) consecuencias.get("tipo");
        return "desbloquear_nivel".equals(tipo) ||
            ("dialogo_consecuencia".equals(tipo) &&
                "desbloquear_nivel_si_correcto".equals(consecuencias.get("accion")));
    }

    /**
     * Obtiene información sobre las consecuencias de un diálogo.
     *
     * @param idDialogo ID del diálogo
     * @return String con información de las consecuencias
     */
    public String getInfoConsecuencias(String idDialogo) {
        Map<String, Object> consecuencias = consecuenciasDialogos.get(idDialogo);
        if (consecuencias == null) return "Sin consecuencias";

        String tipo = (String) consecuencias.get("tipo");
        switch (tipo) {
            case "desbloquear_nivel":
                return "Desbloquea nivel: " + consecuencias.get("nivel_id");
            case "ver_video":
                return "Reproduce video: " + consecuencias.get("video_id");
            case "dialogo_consecuencia":
                String accion = (String) consecuencias.get("accion");
                if ("desbloquear_nivel_si_correcto".equals(accion)) {
                    return "Desbloquea nivel " + consecuencias.get("nivel_id") +
                        " si eliges opción: " + consecuencias.get("opcion_correcta");
                }
                return "Consecuencia condicional";
            case "registrar_decision":
                return "Registra decisión: " + consecuencias.get("decision_id");
            case "consejo":
                return "Da consejo al jugador";
            default:
                return "Consecuencia tipo: " + tipo;
        }
    }

    /**
     * Crea un diálogo de error cuando no se encuentra el diálogo solicitado.
     */
    private SistemaDialogos.NodoDialogo crearDialogoError(String idFaltante) {
        SistemaDialogos.NodoDialogo errorDialogo = new SistemaDialogos.NodoDialogo(
            "error", "Sistema",
            "Diálogo '" + idFaltante + "' no encontrado.\n" +
                "Verifica el archivo dialogos.json"
        );
        errorDialogo.setSiguiente(null);
        return errorDialogo;
    }

    /**
     * Recarga todos los diálogos desde el archivo JSON.
     * Útil para debugging durante el desarrollo.
     */
    public void recargar() {
        dialogos = DialogoLoader.cargarDesdeJSON("dialogos/dialogos.json");
        consecuenciasDialogos.clear();
        if (consecuenciasPendientes != null) {
            consecuenciasPendientes.clear();
        }
        cargarConsecuencias();
        Gdx.app.log("DialogoManager", "Diálogos recargados desde archivo");
    }

    /**
     * Obtiene el número total de diálogos cargados.
     *
     * @return Número de diálogos
     */
    public int getTotalDialogos() {
        return dialogos.size();
    }

    /**
     * Obtiene estadísticas del gestor de diálogos.
     *
     * @return String con estadísticas
     */
    public String getEstadisticas() {
        return String.format(
            "DialogoManager Stats:\n" +
                "  Diálogos: %d\n" +
                "  Consecuencias: %d\n" +
                "  Decisiones jugador: %d\n" +
                "  Pendientes: %d",
            getTotalDialogos(),
            consecuenciasDialogos.size(),
            decisionesJugador.size(),
            (consecuenciasPendientes != null ? consecuenciasPendientes.size() : 0)
        );
    }

    /**
     * Limpia todas las consecuencias pendientes.
     * Útil para resetear el estado después de errores.
     */
    public void limpiarConsecuenciasPendientes() {
        if (consecuenciasPendientes != null) {
            consecuenciasPendientes.clear();
            Gdx.app.log("DialogoManager", "Consecuencias pendientes limpiadas");
        }
    }

    /**
     * Obtiene todos los niveles desbloqueados por el jugador.
     *
     * @return Array con los IDs de niveles desbloqueados
     */
    public java.util.List<String> getNivelesDesbloqueados() {
        java.util.List<String> niveles = new java.util.ArrayList<>();

        for (Map.Entry<String, Integer> decision : decisionesJugador.entrySet()) {
            String dialogoId = decision.getKey();
            int opcionElegida = decision.getValue();

            if (verificarDesbloqueoNivelPorDecision(dialogoId, opcionElegida)) {
                String nivelId = getNivelADesbloquear(dialogoId);
                if (nivelId != null && !niveles.contains(nivelId)) {
                    niveles.add(nivelId);
                }
            }
        }

        return niveles;
    }

    /**
     * Verifica si un nivel específico está desbloqueado.
     *
     * @param nivelId ID del nivel a verificar
     * @return true si el nivel está desbloqueado, false en caso contrario
     */
    public boolean isNivelDesbloqueado(String nivelId) {
        // Primero verificar en GameState (base de datos)
        if (gameState.isNivelDesbloqueado(nivelId)) {
            return true;
        }

        // Luego verificar en nuestras decisiones locales
        for (String nivelDesbloqueado : getNivelesDesbloqueados()) {
            if (nivelDesbloqueado.equals(nivelId)) {
                return true;
            }
        }

        return false;
    }
}
