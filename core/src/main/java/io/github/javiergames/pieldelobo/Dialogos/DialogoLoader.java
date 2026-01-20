package io.github.javiergames.pieldelobo.Dialogos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carga diálogos desde archivos JSON.
 * Incluye manejo robusto de errores y valores por defecto.
 *
 * @author Javier Gala
 * @version 1.1
 */
public class DialogoLoader {

    public static class DialogoData {
        /** Identificador único del diálogo */
        public String id;
        /** Personaje que habla en el diálogo */
        public String personaje;
        /** Texto del diálogo */
        public String texto;
        /** Opciones disponibles (si las hay) */
        public List<OpcionData> opciones = new ArrayList<>();
        /** ID del siguiente diálogo (para diálogos lineales) */
        public String siguiente;
    }

    public static class OpcionData {
        public String texto;
        public String destino;
    }

    /**
     * Carga diálogos desde un archivo JSON.
     * Si hay errores, crea diálogos por defecto automáticamente.
     *
     * @param rutaArchivo Ruta relativa al archivo JSON
     * @return Mapa de diálogos cargados, nunca retorna null
     */
    public static Map<String, SistemaDialogos.NodoDialogo> cargarDesdeJSON(String rutaArchivo) {
        Map<String, SistemaDialogos.NodoDialogo> dialogos = new HashMap<>();

        Gdx.app.log("DialogoLoader", "Intentando cargar diálogos desde: " + rutaArchivo);

        try {
            FileHandle file = Gdx.files.internal(rutaArchivo);

            if (!file.exists()) {
                Gdx.app.error("DialogoLoader", "Archivo no encontrado: " + rutaArchivo);
                Gdx.app.log("DialogoLoader", "Ruta absoluta intentada: " + file.path());
                Gdx.app.log("DialogoLoader", "Creando diálogos por defecto...");
                return cargarDialogosPorDefecto();
            }

            Gdx.app.log("DialogoLoader", "Archivo encontrado, tamaño: " + file.length() + " bytes");

            String jsonText = file.readString();

            if (jsonText == null || jsonText.isEmpty()) {
                Gdx.app.error("DialogoLoader", "Archivo JSON vacío: " + rutaArchivo);
                return cargarDialogosPorDefecto();
            }

            Gdx.app.log("DialogoLoader", "JSON leído correctamente, parseando...");

            JsonReader jsonReader = new JsonReader();
            JsonValue root = jsonReader.parse(jsonText);

            if (root == null) {
                Gdx.app.error("DialogoLoader", "Error parseando JSON, estructura inválida");
                return cargarDialogosPorDefecto();
            }

            int count = 0;
            for (JsonValue dialogoJson : root) {
                try {
                    String id = dialogoJson.getString("id");
                    String personaje = dialogoJson.getString("personaje", "???");
                    String texto = dialogoJson.getString("texto", "");

                    if (id == null || id.isEmpty()) {
                        Gdx.app.error("DialogoLoader", "Diálogo sin ID, saltando...");
                        continue;
                    }

                    SistemaDialogos.NodoDialogo nodo = new SistemaDialogos.NodoDialogo(id, personaje, texto);

                    if (dialogoJson.has("siguiente")) {
                        nodo.setSiguiente(dialogoJson.getString("siguiente"));
                    }

                    if (dialogoJson.has("opciones")) {
                        JsonValue opcionesJson = dialogoJson.get("opciones");
                        for (JsonValue opcionJson : opcionesJson) {
                            String textoOpcion = opcionJson.getString("texto", "");
                            String destino = opcionJson.getString("destino", null);

                            if (destino != null && !destino.isEmpty()) {
                                nodo.agregarOpcion(textoOpcion, destino);
                            }
                        }
                    }

                    dialogos.put(id, nodo);
                    count++;

                    Gdx.app.debug("DialogoLoader", "Diálogo cargado: " + id + " - " + personaje);

                } catch (Exception e) {
                    Gdx.app.error("DialogoLoader", "Error procesando diálogo JSON", e);
                }
            }

            Gdx.app.log("DialogoLoader", "Diálogos cargados exitosamente: " + count + " diálogos");

            // Si no se cargó ningún diálogo, usar por defecto
            if (dialogos.isEmpty()) {
                Gdx.app.error("DialogoLoader", "No se cargó ningún diálogo, usando por defecto");
                return cargarDialogosPorDefecto();
            }

            return dialogos;

        } catch (Exception e) {
            Gdx.app.error("DialogoLoader", "Error crítico al cargar JSON", e);
            return cargarDialogosPorDefecto();
        }
    }

    /**
     * Crea diálogos por defecto cuando no se puede cargar el archivo.
     */
    private static Map<String, SistemaDialogos.NodoDialogo> cargarDialogosPorDefecto() {
        Gdx.app.log("DialogoLoader", "=== CARGANDO DIÁLOGOS POR DEFECTO ===");

        Map<String, SistemaDialogos.NodoDialogo> dialogos = new HashMap<>();

        // Diálogo de error básico
        SistemaDialogos.NodoDialogo errorDialogo = new SistemaDialogos.NodoDialogo(
            "error", "Sistema",
            "Error: No se pudieron cargar los diálogos.\n" +
                "Verifica que el archivo dialogos.json exista en:\n" +
                "assets/dialogos/dialogos.json"
        );
        errorDialogo.setSiguiente(null);
        dialogos.put("error", errorDialogo);

        // Diálogo por defecto para cada tipo de NPC
        SistemaDialogos.NodoDialogo profesor = new SistemaDialogos.NodoDialogo(
            "profesor_leiva_inicio", "Profesor Leiva",
            "Hola, soy el profesor Leiva. Parece que hay un problema con los diálogos."
        );
        profesor.setSiguiente(null);
        dialogos.put("profesor_leiva_inicio", profesor);

        SistemaDialogos.NodoDialogo doctor = new SistemaDialogos.NodoDialogo(
            "doctor_salazar_inicio", "Doctor Salazar",
            "Soy el doctor Salazar. Los diálogos no se cargaron correctamente."
        );
        doctor.setSiguiente(null);
        dialogos.put("doctor_salazar_inicio", doctor);

        SistemaDialogos.NodoDialogo doctora = new SistemaDialogos.NodoDialogo(
            "doctora_garcia_inicio", "Doctora García",
            "Soy la doctora García. Hay un problema técnico con las conversaciones."
        );
        doctora.setSiguiente(null);
        dialogos.put("doctora_garcia_inicio", doctora);

        SistemaDialogos.NodoDialogo defaultDialogo = new SistemaDialogos.NodoDialogo(
            "default_inicio", "Sistema",
            "Diálogos no disponibles temporalmente.\n" +
                "Por favor, verifica los archivos del juego."
        );
        defaultDialogo.setSiguiente("error");
        dialogos.put("default_inicio", defaultDialogo);

        Gdx.app.log("DialogoLoader", "Diálogos por defecto creados: " + dialogos.size() + " diálogos");

        return dialogos;
    }

    /**
     * Verifica si la ruta del archivo es correcta.
     */
    public static String verificarRuta(String rutaArchivo) {
        FileHandle file = Gdx.files.internal(rutaArchivo);

        StringBuilder info = new StringBuilder();
        info.append("Verificación de ruta: ").append(rutaArchivo).append("\n");
        info.append("  Existe: ").append(file.exists()).append("\n");
        info.append("  Ruta: ").append(file.path()).append("\n");
        info.append("  Tamaño: ").append(file.exists() ? file.length() + " bytes" : "N/A").append("\n");

        if (file.exists()) {
            try {
                String content = file.readString();
                info.append("  Contenido válido: ").append(content != null && !content.isEmpty()).append("\n");
                info.append("  Primeros 100 chars: ").append(content.substring(0, Math.min(100, content.length()))).append("...");
            } catch (Exception e) {
                info.append("  Error leyendo: ").append(e.getMessage());
            }
        }

        return info.toString();
    }
}
