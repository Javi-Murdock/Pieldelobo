package io.github.javiergames.pieldelobo.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector3;

import io.github.javiergames.pieldelobo.MenuScreen;
import io.github.javiergames.pieldelobo.PauseScreen;

/**
 * Procesador de input específico para menús.
 * Maneja navegación con teclado (flechas, Enter, Espacio) y gamepad.
 * Puede ser utilizado tanto por MenuScreen como por PauseScreen.
 */

public class ProcesadorMenu extends InputAdapter implements ControllerListener {

    private MenuScreen menuScreen;
    private PauseScreen pauseScreen;

    // Estados para controlar repetición de teclas
    private boolean arribaPresionado = false;
    private boolean abajoPresionado = false;
    private boolean enterPresionado = false;
    private boolean espacioPresionado = false;

    // Estados de gamepad
    private float ultimoEjeY = 0;
    private boolean botonAPresionado = false;

    // Control de tiempo para evitar repetición rápida
    private float tiempoDesdeUltimoMovimiento = 0;
    private static final float TIEMPO_ESPERA_MOVIMIENTO = 0.2f; // segundos

    // Configuración de gamepad
    private static final float ZONA_MUERTA = 0.5f;
    private static final int BOTON_A = 0;
    private static final int EJE_Y_IZQ = 1;

    /**
     * Constructor para MenuScreen
     * @param menuScreen Instancia de MenuScreen a controlar
     */
    public ProcesadorMenu(MenuScreen menuScreen) {
        this.menuScreen = menuScreen;
        this.pauseScreen = null;
        configurarGamepad();
    }

    /**
     * Constructor para PauseScreen
     * @param pauseScreen Instancia de PauseScreen a controlar
     */
    public ProcesadorMenu(PauseScreen pauseScreen) {
        this.pauseScreen = pauseScreen;
        this.menuScreen = null;
        configurarGamepad();
    }

    /**
     * Configura el listener para gamepads conectados
     */
    private void configurarGamepad() {
        Controllers.addListener(this);
        Gdx.app.log("ProcesadorMenu", "Gamepad configurado para menú");
    }

    // ====================== MÉTODOS DE TECLADO ======================

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.UP:
                arribaPresionado = true;
                return true;
            case Keys.DOWN:
                abajoPresionado = true;
                return true;
            case Keys.ENTER:
                enterPresionado = true;
                return true;
            case Keys.SPACE:
                espacioPresionado = true;
                return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.UP:
                arribaPresionado = false;
                return true;
            case Keys.DOWN:
                abajoPresionado = false;
                return true;
            case Keys.ENTER:
                enterPresionado = false;
                return true;
            case Keys.SPACE:
                espacioPresionado = false;
                return true;
        }
        return false;
    }

    // ====================== MÉTODOS DE GAMEPAD ======================

    @Override
    public void connected(Controller controller) {
        Gdx.app.log("ProcesadorMenu", "Gamepad conectado: " + controller.getName());
    }

    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("ProcesadorMenu", "Gamepad desconectado: " + controller.getName());
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        if (buttonCode == BOTON_A) {
            botonAPresionado = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        if (buttonCode == BOTON_A) {
            botonAPresionado = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        if (axisCode == EJE_Y_IZQ) {
            // Invertir el eje Y (en gamepads, arriba suele ser negativo)
            ultimoEjeY = -value;
            return true;
        }
        return false;
    }

    // ====================== MÉTODOS NO UTILIZADOS ======================


    public boolean povMoved(Controller controller, int povCode, int value) {
        return false;
    }


    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }


    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }


    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }

    // ====================== LÓGICA PRINCIPAL ======================

    /**
     * Método que debe llamarse en cada frame para procesar inputs.
     * Controla la navegación en menús con teclado y gamepad.
     */
    public void actualizar() {
        float delta = Gdx.graphics.getDeltaTime();
        tiempoDesdeUltimoMovimiento += delta;

        // Procesar teclado
        procesarTeclado();

        // Procesar gamepad
        procesarGamepad();
    }

    /**
     * Procesa las entradas del teclado para navegación en menús
     */
    private void procesarTeclado() {
        // Solo procesar movimiento si ha pasado el tiempo de espera
        if (tiempoDesdeUltimoMovimiento >= TIEMPO_ESPERA_MOVIMIENTO) {
            if (arribaPresionado) {
                navegarArriba();
                tiempoDesdeUltimoMovimiento = 0;
            } else if (abajoPresionado) {
                navegarAbajo();
                tiempoDesdeUltimoMovimiento = 0;
            }
        }

        // Procesar acciones (sin delay)
        if (enterPresionado || espacioPresionado) {
            activarBotonSeleccionado();
            enterPresionado = false;
            espacioPresionado = false;
        }
    }

    /**
     * Procesa las entradas del gamepad para navegación en menús
     */
    private void procesarGamepad() {
        // Procesar movimiento con joystick
        if (Math.abs(ultimoEjeY) > ZONA_MUERTA) {
            if (tiempoDesdeUltimoMovimiento >= TIEMPO_ESPERA_MOVIMIENTO) {
                if (ultimoEjeY < -ZONA_MUERTA) {
                    // Joystick hacia arriba
                    navegarArriba();
                } else if (ultimoEjeY > ZONA_MUERTA) {
                    // Joystick hacia abajo
                    navegarAbajo();
                }
                tiempoDesdeUltimoMovimiento = 0;
            }
        } else {
            // Resetear el timer cuando el joystick vuelve al centro
            tiempoDesdeUltimoMovimiento = TIEMPO_ESPERA_MOVIMIENTO;
        }

        // Procesar botón A del gamepad
        if (botonAPresionado) {
            activarBotonSeleccionado();
            botonAPresionado = false;
        }
    }

    /**
     * Navega al botón anterior en el menú
     */
    public void navegarArriba() {
        if (menuScreen != null) {
            menuScreen.navegarArriba();
        } else if (pauseScreen != null) {
            pauseScreen.navegarArriba();
        }
    }

    /**
     * Navega al botón siguiente en el menú
     */
    public void navegarAbajo() {
        if (menuScreen != null) {
            menuScreen.navegarAbajo();
        } else if (pauseScreen != null) {
            pauseScreen.navegarAbajo();
        }
    }

    /**
     * Ejecuta la acción del botón actualmente seleccionado
     */
    public void activarBotonSeleccionado() {
        if (menuScreen != null) {
            menuScreen.activarBotonSeleccionado();
        } else if (pauseScreen != null) {
            pauseScreen.activarBotonSeleccionado();
        }
    }

    /**
     * @return true si se detectó input de gamepad
     */
    public boolean hayGamepadConectado() {
        return Controllers.getControllers().size > 0;
    }

}
