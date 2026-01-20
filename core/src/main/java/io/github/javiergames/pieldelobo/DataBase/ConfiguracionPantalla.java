package io.github.javiergames.pieldelobo.DataBase;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Preferences;

/**
 * Clase dedicada para la gestión completa de configuración de pantalla.
 * Separa la lógica de configuración del Main principal.
 * Maneja resolución, modo pantalla completa y ajustes de visualización.
 *
 * @author Javier Gala
 * @version 1.2
 */
public class ConfiguracionPantalla {

    // ========== CONSTANTES ==========

    /**
     * VALORES POR DEFECTO.
     */
    public static final int ANCHO_POR_DEFECTO = 800;
    public static final int ALTO_POR_DEFECTO = 600;
    public static final boolean PANTALLA_COMPLETA_POR_DEFECTO = false;
    public static final int REFRESCO_POR_DEFECTO = 60;

    // ========== CONFIGURACIÓN ACTUAL ==========

    private boolean pantallaCompleta;
    private int anchoVentana;
    private int altoVentana;
    private int anchoPantallaCompleta;
    private int altoPantallaCompleta;
    private int refresco;

    // ========== INSTANCIA SINGLETON ==========

    private static ConfiguracionPantalla instancia;

    /**
     * Constructor privado para patrón Singleton.
     */
    private ConfiguracionPantalla() {
        cargarConfiguracion();
    }

    /**
     * Obtiene la instancia única de ConfiguracionPantalla.
     */
    public static ConfiguracionPantalla getInstance() {
        if (instancia == null) {
            instancia = new ConfiguracionPantalla();
        }
        return instancia;
    }

    // ========== MÉTODOS DE CONFIGURACIÓN ==========

    /**
     * Carga la configuración desde el archivo de preferencias.
     */
    private void cargarConfiguracion() {
        try {
            Preferences prefs = Gdx.app.getPreferences("PielDeLobo_Pantalla");

            pantallaCompleta = prefs.getBoolean("pantallaCompleta", PANTALLA_COMPLETA_POR_DEFECTO);
            anchoVentana = prefs.getInteger("anchoVentana", ANCHO_POR_DEFECTO);
            altoVentana = prefs.getInteger("altoVentana", ALTO_POR_DEFECTO);
            anchoPantallaCompleta = prefs.getInteger("anchoPantallaCompleta", 0);
            altoPantallaCompleta = prefs.getInteger("altoPantallaCompleta", 0);
            refresco = prefs.getInteger("refresco", REFRESCO_POR_DEFECTO);

            // Si es pantalla completa y no tenemos resolución, detectarla
            if (pantallaCompleta && (anchoPantallaCompleta == 0 || altoPantallaCompleta == 0)) {
                detectarResolucionNativa();
            }

            Gdx.app.log("ConfiguracionPantalla", "Configuración cargada: " +
                (pantallaCompleta ? "Pantalla completa" : "Ventana") + " " +
                getResolucionActual());

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error al cargar configuración", e);
            usarValoresPorDefecto();
        }
    }

    /**
     * Guarda la configuración actual en el archivo de preferencias.
     */
    public void guardarConfiguracion() {
        try {
            Preferences prefs = Gdx.app.getPreferences("PielDeLobo_Pantalla");

            prefs.putBoolean("pantallaCompleta", pantallaCompleta);
            prefs.putInteger("anchoVentana", anchoVentana);
            prefs.putInteger("altoVentana", altoVentana);
            prefs.putInteger("anchoPantallaCompleta", anchoPantallaCompleta);
            prefs.putInteger("altoPantallaCompleta", altoPantallaCompleta);
            prefs.putInteger("refresco", refresco);

            prefs.flush();

            Gdx.app.log("ConfiguracionPantalla", "Configuración guardada");

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error al guardar configuración", e);
        }
    }

    /**
     * Detecta y establece la resolución nativa del monitor.
     */
    private void detectarResolucionNativa() {
        try {
            Monitor monitor = Gdx.graphics.getPrimaryMonitor();
            DisplayMode modoNativo = Gdx.graphics.getDisplayMode(monitor);

            anchoPantallaCompleta = modoNativo.width;
            altoPantallaCompleta = modoNativo.height;
            refresco = modoNativo.refreshRate;

            Gdx.app.log("ConfiguracionPantalla", "Resolución nativa detectada: " +
                anchoPantallaCompleta + "x" + altoPantallaCompleta + " @" + refresco + "Hz");

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error al detectar resolución nativa", e);
            // Valores de fallback
            anchoPantallaCompleta = 1920;
            altoPantallaCompleta = 1080;
            refresco = 60;
        }
    }

    /**
     * Establece valores por defecto.
     */
    private void usarValoresPorDefecto() {
        pantallaCompleta = PANTALLA_COMPLETA_POR_DEFECTO;
        anchoVentana = ANCHO_POR_DEFECTO;
        altoVentana = ALTO_POR_DEFECTO;
        anchoPantallaCompleta = 0;
        altoPantallaCompleta = 0;
        refresco = REFRESCO_POR_DEFECTO;
    }

    // ========== MÉTODOS DE APLICACIÓN ==========

    /**
     * Aplica la configuración actual a la pantalla.
     * Detecta y ajusta automáticamente según el hardware.
     */
    public void aplicarConfiguracion() {
        try {
            if (pantallaCompleta) {
                aplicarPantallaCompleta();
            } else {
                aplicarModoVentana();
            }

            Gdx.app.log("ConfiguracionPantalla", "Configuración aplicada: " + getResolucionActual());

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error al aplicar configuración", e);
            // Forzar modo ventana seguro
            aplicarModoVentanaSeguro();
        }
    }

    /**
     * Aplica el modo de pantalla completa.
     */
    private void aplicarPantallaCompleta() {
        // Si no tenemos resolución, detectarla
        if (anchoPantallaCompleta == 0 || altoPantallaCompleta == 0) {
            detectarResolucionNativa();
        }

        try {
            // Buscar modo de pantalla que coincida con nuestra configuración
            Monitor monitor = Gdx.graphics.getPrimaryMonitor();
            DisplayMode[] modos = Gdx.graphics.getDisplayModes(monitor);

            DisplayMode modoSeleccionado = null;
            for (DisplayMode modo : modos) {
                if (modo.width == anchoPantallaCompleta &&
                    modo.height == altoPantallaCompleta &&
                    modo.refreshRate == refresco) {
                    modoSeleccionado = modo;
                    break;
                }
            }

            // Si no encontramos exacto, usar el modo nativo
            if (modoSeleccionado == null) {
                modoSeleccionado = Gdx.graphics.getDisplayMode(monitor);
                // Actualizar nuestra configuración con el modo real
                anchoPantallaCompleta = modoSeleccionado.width;
                altoPantallaCompleta = modoSeleccionado.height;
                refresco = modoSeleccionado.refreshRate;
            }

            Gdx.graphics.setFullscreenMode(modoSeleccionado);

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error en pantalla completa, usando ventana", e);
            pantallaCompleta = false;
            aplicarModoVentana();
        }
    }

    /**
     * Aplica el modo ventana.
     */
    private void aplicarModoVentana() {
        try {
            // Asegurar que la ventana no sea más grande que la pantalla
            Monitor monitor = Gdx.graphics.getPrimaryMonitor();
            DisplayMode modo = Gdx.graphics.getDisplayMode(monitor);

            int anchoMaximo = modo.width - 100; // Dejar margen
            int altoMaximo = modo.height - 100;

            int anchoFinal = Math.min(anchoVentana, anchoMaximo);
            int altoFinal = Math.min(altoVentana, altoMaximo);

            Gdx.graphics.setWindowedMode(anchoFinal, altoFinal);

            // Actualizar si hubo ajuste
            if (anchoFinal != anchoVentana || altoFinal != altoVentana) {
                anchoVentana = anchoFinal;
                altoVentana = altoFinal;
                Gdx.app.log("ConfiguracionPantalla", "Resolución ajustada a: " +
                    anchoFinal + "x" + altoFinal);
            }

        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error en modo ventana", e);
            aplicarModoVentanaSeguro();
        }
    }

    /**
     * Aplica modo ventana con valores seguros garantizados.
     */
    private void aplicarModoVentanaSeguro() {
        try {
            Gdx.graphics.setWindowedMode(ANCHO_POR_DEFECTO, ALTO_POR_DEFECTO);
            anchoVentana = ANCHO_POR_DEFECTO;
            altoVentana = ALTO_POR_DEFECTO;
            pantallaCompleta = false;
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error crítico en modo ventana seguro", e);
        }
    }

    // ========== MÉTODOS PÚBLICOS ==========

    /**
     * Alterna entre modo ventana y pantalla completa.
     * Guarda automáticamente la preferencia.
     */
    public void alternarPantallaCompleta() {
        pantallaCompleta = !pantallaCompleta;
        aplicarConfiguracion();
        guardarConfiguracion();
    }

    /**
     * Cambia la resolución de ventana.
     */
    public void setResolucionVentana(int ancho, int alto) {
        this.anchoVentana = ancho;
        this.altoVentana = alto;
        this.pantallaCompleta = false;

        aplicarConfiguracion();
        guardarConfiguracion();
    }

    /**
     * Cambia la resolución de pantalla completa.
     */
    public void setResolucionPantallaCompleta(int ancho, int alto, int refresco) {
        this.anchoPantallaCompleta = ancho;
        this.altoPantallaCompleta = alto;
        this.refresco = refresco;
        this.pantallaCompleta = true;

        aplicarConfiguracion();
        guardarConfiguracion();
    }

    /**
     * Obtiene las resoluciones disponibles para pantalla completa.
     * Detecta automáticamente las opciones soportadas por el monitor.
     *
     * @return Array de modos de pantalla disponibles
     */
    public DisplayMode[] getResolucionesDisponibles() {
        try {
            Monitor monitor = Gdx.graphics.getPrimaryMonitor();
            return Gdx.graphics.getDisplayModes(monitor);
        } catch (Exception e) {
            Gdx.app.error("ConfiguracionPantalla", "Error al obtener resoluciones", e);
            return new DisplayMode[0];
        }
    }

    /**
     * Obtiene resoluciones recomendadas para ventana.
     */
    public String[] getResolucionesRecomendadasVentana() {
        return new String[] {
            "800x600",
            "1024x768",
            "1280x720",
            "1366x768",
            "1600x900",
            "1920x1080"
        };
    }

    // ========== MÉTODOS DE ACCESO ==========

    public boolean isPantallaCompleta() {
        return pantallaCompleta;
    }

    public int getAnchoActual() {
        return pantallaCompleta ? anchoPantallaCompleta : anchoVentana;
    }

    public int getAltoActual() {
        return pantallaCompleta ? altoPantallaCompleta : altoVentana;
    }

    public String getResolucionActual() {
        if (pantallaCompleta) {
            return anchoPantallaCompleta + "x" + altoPantallaCompleta + " @" + refresco + "Hz";
        } else {
            return anchoVentana + "x" + altoVentana;
        }
    }

    public int getAnchoVentana() {
        return anchoVentana;
    }

    public int getAltoVentana() {
        return altoVentana;
    }

    public int getRefresco() {
        return refresco;
    }
}
