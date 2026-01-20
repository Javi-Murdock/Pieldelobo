package io.github.javiergames.pieldelobo.GestorJuego;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import io.github.javiergames.pieldelobo.DataBase.ConfiguracionPantalla;
import io.github.javiergames.pieldelobo.MenuScreen;
/**
 * Clase principal del juego que extiende de Game de LibGDX.
 * Configura el juego, maneja excepciones globales y oculta el cursor.
 *
 * @author Javier Gala
 * @version 1.1
 */
public class Main extends Game {
    /**
     * Método principal de inicialización del juego.
     * Configura manejador de excepciones, oculta cursor y carga configuración.
     */
    @Override
    public void create() {
        // =============================================
        // MANEJADOR GLOBAL DE EXCEPCIONES
        // =============================================
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                Gdx.app.error("CRASH", "=== EXCEPCIÓN NO MANEJADA ===", ex);
                ex.printStackTrace();

                // Escribir en un archivo
                try {
                    java.io.FileWriter writer = new java.io.FileWriter("error_log.txt", true);
                    writer.write("=== CRASH REPORT ===\n");
                    writer.write("Fecha: " + new java.util.Date() + "\n");
                    writer.write("Error: " + ex.toString() + "\n");
                    writer.write("Mensaje: " + ex.getMessage() + "\n");
                    writer.write("Stack trace:\n");
                    for (StackTraceElement element : ex.getStackTrace()) {
                        writer.write("    at " + element + "\n");
                    }
                    writer.write("\n\n");
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // =============================================
        // OCULTAR CURSOR DEL RATÓN
        // =============================================
        //  Ocultar completamente
        Gdx.input.setCursorCatched(true);


        // =============================================
        // INICIALIZACIÓN CON CONFIGURACIÓN DE PANTALLA
        // =============================================
        ConfiguracionPantalla configPantalla = ConfiguracionPantalla.getInstance();
        configPantalla.aplicarConfiguracion();

        Gdx.graphics.setTitle("PielDeLobo");
        Gdx.app.log("Main", "Juego iniciado con resolución: " + configPantalla.getResolucionActual());

        // Inicializar la referencia estática en Screens
        Screens.juego = this;

        // Cargar pantalla principal del menú con try-catch
        try {
            setScreen(new MenuScreen(this));
        } catch (Exception e) {
            Gdx.app.error("Main", "Error al crear MenuScreen", e);
            e.printStackTrace();
        }
    }
    /**
     * Mantiene el cursor oculto en todo momento durante el renderizado.
     */
    @Override
    public void render() {
        // =============================================
        // MANTENER CURSOR OCULTO EN TODO MOMENTO
        // =============================================
        // Esto asegura que el cursor se mantenga oculto incluso si
        // otra pantalla intenta mostrarlo
        if (!Gdx.input.isCursorCatched()) {
            Gdx.input.setCursorCatched(true);
        }

        // Asegurarse de que el viewport se actualice correctamente
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        Gdx.app.log("Main", "Recursos del juego liberados");

        // Opcional: Restaurar cursor al salir del juego
        Gdx.input.setCursorCatched(false);
    }
}
