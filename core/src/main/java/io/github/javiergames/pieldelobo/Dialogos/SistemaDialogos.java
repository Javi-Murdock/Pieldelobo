package io.github.javiergames.pieldelobo.Dialogos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.Graficos.GameUtils;

/**
 * Sistema de diálogos con ventana gráfica centrada.
 * SIMPLIFICADO: No muestra mensajes de consecuencia durante el diálogo.
 *
 * @author Javier Gala
 * @version 1.3
 */
public class SistemaDialogos {

    /**
     * Nodo de diálogo con posibles consecuencias.
     * Representa un punto en la conversación con opciones.
     */
    public static class NodoDialogo {
        /** Identificador único del nodo */
        public String id;
        /** Nombre del personaje que habla */
        public String personaje;
        /** Texto del diálogo */
        public String texto;
        /** Lista de opciones disponibles */
        public List<OpcionDialogo> opciones;
        /** ID del siguiente diálogo (para flujo lineal) */
        public String siguienteId;
        private GameState gameState;
        /**
         * Agrega una opción de respuesta al diálogo.
         *
         * @param texto Texto de la opción
         * @param destinoId ID del diálogo destino
         * @return Esta instancia para encadenamiento
         */
        public NodoDialogo(String id, String personaje, String texto) {
            this.id = id;
            this.personaje = personaje;
            this.texto = texto;
            this.opciones = new ArrayList<>();
            this.gameState = GameState.getInstance();
        }

        public NodoDialogo agregarOpcion(String texto, String destinoId) {
            OpcionDialogo opcion = new OpcionDialogo(texto, destinoId);
            opciones.add(opcion);
            return this;
        }

        public NodoDialogo setSiguiente(String siguienteId) {
            this.siguienteId = siguienteId;
            return this;
        }

        public List<OpcionDialogo> getOpciones() {
            return opciones;
        }
    }

    /**
     * Opción de diálogo.
     */
    public static class OpcionDialogo {
        public String texto;
        public String destinoId;

        public OpcionDialogo(String texto, String destinoId) {
            this.texto = texto;
            this.destinoId = destinoId;
        }
    }

    /**
     * Ventana de diálogo simplificada.
     * Maneja renderizado y entrada del usuario.
     */
    public static class VentanaDialogo {
        // ========== CONFIGURACIÓN VISUAL ==========
        private static final float PADDING = 25f;
        private static final float LINE_HEIGHT = 32f;
        private static final float TEXT_SPEED = 35f;
        private static final float OPCION_SPACING = 5f;

        // ========== COLORES ==========
        private Color colorFondo = new Color(0.1f, 0.1f, 0.2f, 0.95f);
        private Color colorBorde = new Color(0.3f, 0.3f, 0.6f, 1f);
        private Color colorTexto = new Color(1f, 1f, 1f, 1f);
        private Color colorPersonaje = new Color(1f, 0.9f, 0.5f, 1f);
        private Color colorSeleccion = new Color(1f, 1f, 0.5f, 1f);
        private Color colorOpcionNormal = new Color(0.8f, 0.8f, 0.8f, 1f);

        // ========== PROPIEDADES ==========
        private SpriteBatch batch;
        private BitmapFont font;
        private GlyphLayout layout;

        private NodoDialogo nodoActual;
        private int opcionSeleccionada = 0;
        private boolean mostrandoOpciones = false;
        private float tiempoTexto = 0f;
        private String textoMostrado = "";
        private boolean textoCompleto = false;

        // Referencias
        private DialogoManager dialogoManager;

        // Dimensiones
        private float anchoVentana = 700f;
        private float altoVentana = 250f;

        public VentanaDialogo() {
            this.layout = new GlyphLayout();
            this.dialogoManager = DialogoManager.getInstance();
            inicializarFuentes();
            batch = new SpriteBatch();
            Gdx.app.log("VentanaDialogo", "Ventana de diálogo inicializada");
        }

        private void inicializarFuentes() {
            try {
                FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                    Gdx.files.internal("fonts/PixelifySans-SemiBold.ttf"));
                FreeTypeFontGenerator.FreeTypeFontParameter param =
                    new FreeTypeFontGenerator.FreeTypeFontParameter();
                param.size = 22;
                param.color = colorTexto;
                param.borderWidth = 1f;
                param.borderColor = Color.BLACK;
                font = generator.generateFont(param);
                generator.dispose();
            } catch (Exception e) {
                Gdx.app.log("VentanaDialogo", "No se pudo cargar fuente personalizada, usando por defecto");
                font = new BitmapFont();
                font.getData().setScale(1.3f);
                font.setColor(colorTexto);
            }
        }

        /**
         * Muestra un diálogo específico por ID.
         *
         * @param idDialogo ID del diálogo a mostrar
         */
        public void mostrarDialogo(String idDialogo) {
            nodoActual = dialogoManager.obtenerDialogo(idDialogo);
            if (nodoActual != null) {
                reiniciarEstado();
                Gdx.app.log("VentanaDialogo", "Mostrando diálogo: " + idDialogo);
            } else {
                Gdx.app.error("VentanaDialogo", "Diálogo no encontrado: " + idDialogo);
            }
        }

        /**
         * Actualiza el estado del diálogo.
         */
        public void actualizar(float delta) {
            if (nodoActual == null) return;

            // Efecto de escritura normal
            if (!textoCompleto) {
                tiempoTexto += delta * TEXT_SPEED;
                int chars = Math.min((int)tiempoTexto, nodoActual.texto.length());
                textoMostrado = nodoActual.texto.substring(0, chars);

                if (chars >= nodoActual.texto.length()) {
                    textoCompleto = true;
                    if (!nodoActual.opciones.isEmpty()) {
                        mostrandoOpciones = true;
                    }
                }
            }
        }

        /**
         * Renderiza la ventana de diálogo.
         */
        public void render() {
            if (nodoActual == null) return;

            // Render normal
            float x = (Gdx.graphics.getWidth() - anchoVentana) / 2;
            float y = 0;

            batch.begin();

            // ========== FONDO CON BORDE ==========
            // Borde externo
            batch.setColor(colorBorde);
            float grosorBorde = 5f;
            batch.draw(GameUtils.getWhitePixel(),
                x - grosorBorde, y - grosorBorde,
                anchoVentana + grosorBorde * 2, altoVentana + grosorBorde * 2);

            // Fondo principal
            batch.setColor(colorFondo);
            batch.draw(GameUtils.getWhitePixel(), x, y, anchoVentana, altoVentana);

            // Borde interno
            batch.setColor(colorBorde.r, colorBorde.g, colorBorde.b, 0.7f);
            float grosorInterno = 2f;
            batch.draw(GameUtils.getWhitePixel(),
                x + grosorInterno, y + grosorInterno,
                anchoVentana - grosorInterno * 2, grosorInterno);
            batch.draw(GameUtils.getWhitePixel(),
                x + grosorInterno, y + altoVentana - grosorInterno,
                anchoVentana - grosorInterno * 2, grosorInterno);
            batch.draw(GameUtils.getWhitePixel(),
                x + grosorInterno, y + grosorInterno,
                grosorInterno, altoVentana - grosorInterno * 2);
            batch.draw(GameUtils.getWhitePixel(),
                x + anchoVentana - grosorInterno, y + grosorInterno,
                grosorInterno, altoVentana - grosorInterno * 2);

            batch.setColor(1f, 1f, 1f, 1f);

            // ========== CONTENIDO ==========
            // Personaje
            if (nodoActual.personaje != null && !nodoActual.personaje.isEmpty()) {
                font.setColor(colorPersonaje);
                String personajeTexto = nodoActual.personaje + ":";
                font.draw(batch, personajeTexto,
                    x + PADDING, y + altoVentana - PADDING);
            }

            // Texto del diálogo
            font.setColor(colorTexto);
            float textoX = x + PADDING;
            float textoY = y + altoVentana - PADDING - 45f;
            float textoAncho = anchoVentana - PADDING * 2;

            dibujarTextoConLineas(textoMostrado, textoX, textoY, textoAncho);

            // ========== OPCIONES ==========
            if (textoCompleto && mostrandoOpciones && !nodoActual.opciones.isEmpty()) {
                float opcionesY = y + PADDING + 40f;
                float opcionAltura = LINE_HEIGHT + OPCION_SPACING;

                for (int i = 0; i < nodoActual.opciones.size(); i++) {
                    OpcionDialogo opcion = nodoActual.opciones.get(i);

                    // Fondo para la opción seleccionada
                    if (i == opcionSeleccionada) {
                        batch.setColor(colorBorde.r, colorBorde.g, colorBorde.b, 0.3f);
                        float opcionX = x + PADDING - 10f;
                        float opcionY = opcionesY - i * opcionAltura - 25f;
                        batch.draw(GameUtils.getWhitePixel(),
                            opcionX, opcionY,
                            anchoVentana - PADDING * 2 + 20f, 30f);
                        batch.setColor(1f, 1f, 1f, 1f);
                    }

                    // Texto de la opción
                    String textoOpcion = (i == opcionSeleccionada ? "► " : "  ") + opcion.texto;

                    if (i == opcionSeleccionada) {
                        font.setColor(colorSeleccion);
                    } else {
                        font.setColor(colorOpcionNormal);
                    }

                    font.draw(batch, textoOpcion,
                        x + PADDING + 15f, opcionesY - i * opcionAltura);
                }
            }

            // ========== INDICADOR PARA CONTINUAR ==========
            if (textoCompleto && !mostrandoOpciones && nodoActual.siguienteId != null) {
                // Triángulo indicador parpadeante
                float alpha = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() * 0.005f);
                batch.setColor(1f, 1f, 1f, alpha);

                float indicadorX = x + anchoVentana - PADDING - 15f;
                float indicadorY = y + PADDING + 20f;

                batch.draw(GameUtils.getWhitePixel(),
                    indicadorX, indicadorY,
                    12f, 12f);

                batch.setColor(1f, 1f, 1f, 1f);

                // Texto "Presiona Enter"
                font.setColor(colorOpcionNormal);
                font.draw(batch, "ENTER", indicadorX - 50f, indicadorY + 8f);
            }

            batch.end();
        }

        private void dibujarTextoConLineas(String texto, float startX, float startY, float maxAncho) {
            if (texto == null || texto.isEmpty()) return;

            String[] palabras = texto.split(" ");
            StringBuilder linea = new StringBuilder();
            float yPos = startY;
            int lineCount = 0;
            float maxLineas = 4;

            for (String palabra : palabras) {
                String prueba = linea.length() == 0 ? palabra : linea + " " + palabra;
                layout.setText(font, prueba);

                if (layout.width > maxAncho && linea.length() > 0) {
                    font.draw(batch, linea.toString(), startX, yPos);
                    linea = new StringBuilder(palabra);
                    yPos -= LINE_HEIGHT;
                    lineCount++;

                    if (lineCount >= maxLineas) {
                        yPos -= 10f;
                    }
                } else {
                    linea = new StringBuilder(prueba);
                }
            }

            if (linea.length() > 0) {
                font.draw(batch, linea.toString(), startX, yPos);
            }
        }

        /**
         * Navega entre opciones.
         */
        public void navegarArriba() {
            if (mostrandoOpciones && textoCompleto && !nodoActual.opciones.isEmpty()) {
                opcionSeleccionada--;
                if (opcionSeleccionada < 0) {
                    opcionSeleccionada = nodoActual.opciones.size() - 1;
                }
                reproducirSonido("sounds/menu_move.wav", 0.5f);
            }
        }

        public void navegarAbajo() {
            if (mostrandoOpciones && textoCompleto && !nodoActual.opciones.isEmpty()) {
                opcionSeleccionada++;
                if (opcionSeleccionada >= nodoActual.opciones.size()) {
                    opcionSeleccionada = 0;
                }
                reproducirSonido("sounds/menu_move.wav", 0.5f);
            }
        }

        /**
         * Confirma la selección actual y aplica consecuencias.
         *
         * @return true si el diálogo continúa, false si termina
         */
        public boolean confirmar() {
            if (nodoActual == null) {
                Gdx.app.error("VentanaDialogo", "Intento de confirmar con nodo nulo");
                return false;
            }

            Gdx.app.log("VentanaDialogo", "Confirmando en diálogo: " + nodoActual.id);

            // ====================== PRIMERO: ACELERAR TEXTO SI NO ESTÁ COMPLETO ======================
            if (!textoCompleto) {
                textoMostrado = nodoActual.texto;
                textoCompleto = true;

                if (!nodoActual.opciones.isEmpty()) {
                    mostrandoOpciones = true;
                }

                reproducirSonido("sounds/menu_select.wav", 0.3f);
                Gdx.app.log("VentanaDialogo", "Texto acelerado a completo");
                return true;
            }

            // ====================== SEGUNDO: PROCESAR OPCIÓN SELECCIONADA ======================
            boolean tieneOpciones = mostrandoOpciones && !nodoActual.opciones.isEmpty();
            boolean tieneSiguiente = nodoActual.siguienteId != null;

            Gdx.app.log("VentanaDialogo",
                "Estado - Opciones: " + tieneOpciones +
                    ", Siguiente: " + tieneSiguiente +
                    ", Índice seleccionado: " + opcionSeleccionada);

            // CASO 1: Diálogo con opciones
            if (tieneOpciones) {
                OpcionDialogo opcion = nodoActual.opciones.get(opcionSeleccionada);
                Gdx.app.log("VentanaDialogo", "Opción seleccionada: " + opcion.texto + " -> " + opcion.destinoId);

                // Aplicar consecuencias INMEDIATAMENTE al seleccionar opción
                boolean consecuenciaAplicada = dialogoManager.aplicarConsecuencias(nodoActual.id, opcionSeleccionada);
                Gdx.app.log("VentanaDialogo", "Consecuencia aplicada: " + consecuenciaAplicada);

                if (opcion.destinoId != null && !opcion.destinoId.isEmpty()) {
                    // Ir al siguiente diálogo
                    mostrarDialogo(opcion.destinoId);
                    return true;
                } else {
                    // Fin del diálogo ramificado
                    Gdx.app.log("VentanaDialogo", "Fin de diálogo ramificado");
                    return false;
                }
            }

            // CASO 2: Diálogo lineal con siguiente
            else if (tieneSiguiente) {
                // Aplicar consecuencias para diálogo lineal (índice -1)
                boolean consecuenciaAplicada = dialogoManager.aplicarConsecuencias(nodoActual.id, -1);
                Gdx.app.log("VentanaDialogo", "Consecuencia lineal aplicada: " + consecuenciaAplicada);

                // Ir al siguiente diálogo
                mostrarDialogo(nodoActual.siguienteId);
                return true;
            }

            // CASO 3: ÚLTIMO DIÁLOGO - Aplicar consecuencias finales
            else {
                Gdx.app.log("VentanaDialogo", "ÚLTIMO DIÁLOGO - Aplicando consecuencias finales");

                // Para diálogo sin opciones, usar índice -1
                boolean consecuenciaAplicada = dialogoManager.aplicarConsecuencias(nodoActual.id, -1);
                Gdx.app.log("VentanaDialogo", "Consecuencia final aplicada: " + consecuenciaAplicada);

                // Verificar si hay consecuencias pendientes (videos)
                boolean tienePendientes = dialogoManager.tieneConsecuenciasPendientes();
                Gdx.app.log("VentanaDialogo", "Consecuencias pendientes después de aplicar: " + tienePendientes);

                if (tienePendientes) {
                    Gdx.app.log("VentanaDialogo", "Hay " +
                        dialogoManager.obtenerConsecuenciasPendientesParaVerificar().size() +
                        " consecuencias pendientes");
                }

                // Fin del diálogo
                return false;
            }
        }


        /**
         * Reproduce un sonido.
         */
        private void reproducirSonido(String ruta, float volumen) {
            try {
                com.badlogic.gdx.audio.Sound sound = Gdx.audio.newSound(Gdx.files.internal(ruta));
                sound.play(volumen);
            } catch (Exception e) {
                // Silenciar si no hay sonido
            }
        }

        /**
         * Reinicia el estado del diálogo.
         */
        private void reiniciarEstado() {
            opcionSeleccionada = 0;
            mostrandoOpciones = false;
            tiempoTexto = 0f;
            textoMostrado = "";
            textoCompleto = false;
        }

        /**
         * Cierra el diálogo.
         */
        public void cerrar() {
            if (nodoActual != null) {
                Gdx.app.log("VentanaDialogo", "Cerrando diálogo: " + nodoActual.id);
            }
            nodoActual = null;
            reiniciarEstado();
        }

        /**
         * Verifica si hay diálogo activo.
         */
        public boolean isActivo() {
            return nodoActual != null;
        }

        // Métodos para personalización
        public void setTamanio(float ancho, float alto) {
            this.anchoVentana = ancho;
            this.altoVentana = alto;
        }

        public void setColores(Color fondo, Color borde, Color texto) {
            this.colorFondo.set(fondo);
            this.colorBorde.set(borde);
            this.colorTexto.set(texto);
            if (font != null) {
                font.setColor(texto);
            }
        }

        public void dispose() {
            if (font != null) {
                font.dispose();
                Gdx.app.log("VentanaDialogo", "Fuente liberada");
            }
            if (batch != null) {
                batch.dispose();
                Gdx.app.log("VentanaDialogo", "Batch liberado");
            }
        }
    }
}
