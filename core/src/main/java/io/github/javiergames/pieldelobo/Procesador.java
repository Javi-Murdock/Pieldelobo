package io.github.javiergames.pieldelobo;

// Importaciones necesarias para el manejo de inputs
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.*;
import com.badlogic.gdx.math.Vector3;

/**
 * Clase que maneja todos los inputs del juego (teclado y gamepad)
 * Implementa tanto InputAdapter como ControllerListener para manejar ambos tipos de entrada
 */
public class Procesador extends InputAdapter implements ControllerListener {
    // Referencia al jugador que controlaremos
    private Protagonista jugador;

    // Estados de las teclas del teclado
    private boolean wPresionado, aPresionado, sPresionado, dPresionado;  // Movimiento WASD
    private boolean spacePresionado, upPresionado, downPresionado, leftPresionado, rightPresionado;  // Otras teclas

    // Estados del gamepad
    private float ejeXGamepad, ejeYGamepad;  // Valores de los ejes analógicos (-1 a 1)
    private boolean botonAGamepad, botonBGamepad;  // Estados de los botones

    // Configuración del gamepad
    private static final float ZONA_MUERTA = 0.2f;  // Margen para ignorar pequeñas fluctuaciones
    private static final int BOTON_A = 0;  // Botón A (XBOX: A, PS: X)
    private static final int BOTON_B = 1;  // Botón B (XBOX: B, PS: Círculo)
    private static final int EJE_X_IZQ = 0;  // Eje horizontal del joystick izquierdo
    private static final int EJE_Y_IZQ = 1;  // Eje vertical del joystick izquierdo

    /**
     * Constructor que recibe el protagonista a controlar
     * @param jugador Instancia del personaje jugador
     */
    public Procesador(Protagonista jugador) {
        this.jugador = jugador;
        configurarGamepad();
    }

    /**
     * Configura el listener para gamepads
     */
    private void configurarGamepad() {
        Controllers.addListener(this);
    }

    // ====================== MÉTODOS DE TECLADO ======================

    /**
     * Se ejecuta cuando se presiona una tecla
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.W: wPresionado = true; break;  // Movimiento arriba
            case Keys.A: aPresionado = true; break;  // Movimiento izquierda
            case Keys.S: sPresionado = true; break;  // Movimiento abajo
            case Keys.D: dPresionado = true; break;  // Movimiento derecha
            case Keys.SPACE: spacePresionado = true; break;  // Salto alternativo
            case Keys.UP: upPresionado = true; break;    // Flecha arriba
            case Keys.DOWN: downPresionado = true; break;  // Flecha abajo
            case Keys.LEFT: leftPresionado = true; break;  // Flecha izquierda
            case Keys.RIGHT: rightPresionado = true; break; // Flecha derecha
        }
        return true;  // Indicamos que hemos procesado el input
    }

    /**
     * Se ejecuta cuando se suelta una tecla
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.W: wPresionado = false; break;
            case Keys.A: aPresionado = false; break;
            case Keys.S: sPresionado = false; break;
            case Keys.D: dPresionado = false; break;
            case Keys.SPACE: spacePresionado = false; break;
            case Keys.UP: upPresionado = false; break;
            case Keys.DOWN: downPresionado = false; break;
            case Keys.LEFT: leftPresionado = false; break;
            case Keys.RIGHT: rightPresionado = false; break;
        }
        return true;
    }

    // ====================== MÉTODOS DE GAMEPAD ======================

    /**
     * Se ejecuta cuando se conecta un gamepad
     */
    @Override
    public void connected(Controller controller) {
        Gdx.app.log("Gamepad", "Conectado: " + controller.getName());
    }

    /**
     * Se ejecuta cuando se desconecta un gamepad
     */
    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("Gamepad", "Desconectado: " + controller.getName());
    }

    /**
     * Se ejecuta cuando se presiona un botón del gamepad
     */
    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case BOTON_A:
                botonAGamepad = true;  // Botón A (generalmente para saltar)
                return true;
            case BOTON_B:
                botonBGamepad = true;  // Botón B (generalmente para atacar)
                return true;
        }
        return false;
    }

    /**
     * Se ejecuta cuando se suelta un botón del gamepad
     */
    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case BOTON_A: botonAGamepad = false; return true;
            case BOTON_B: botonBGamepad = false; return true;
        }
        return false;
    }

    /**
     * Se ejecuta cuando se mueve un eje analógico del gamepad
     * Nota: El eje Y está invertido (-value) porque normalmente en los gamepads
     * hacia arriba es negativo y hacia abajo positivo, y queremos lo contrario
     */
    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        switch (axisCode) {
            case EJE_X_IZQ:  // Eje horizontal
                // Aplicamos zona muerta para ignorar pequeñas fluctuaciones
                ejeXGamepad = Math.abs(value) > ZONA_MUERTA ? value : 0;
                return true;
            case EJE_Y_IZQ:  // Eje vertical (invertido)
                ejeYGamepad = Math.abs(value) > ZONA_MUERTA ? -value : 0;
                return true;
        }
        return false;
    }

    /**
     * Maneja el movimiento del D-Pad (cruz direccional)
     */
    public boolean povMoved(Controller controller, int povCode, int value) {
        // Podrías implementar movimiento alternativo con el D-Pad aquí
        return false;
    }


    // ====================== MÉTODOS NO UTILIZADOS ======================
    // (Pero requeridos por la interfaz ControllerListener)


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
     * Método principal que actualiza el estado del jugador según los inputs
     * @param delta Tiempo transcurrido desde el último frame (en segundos)
     */
    public void actualizar(float delta) {
        float movX = 0, movY = 0;

        // Movimiento con teclado (WASD o flechas)
        if (wPresionado || upPresionado) movY += 1;
        if (sPresionado || downPresionado) movY -= 1;
        if (aPresionado || leftPresionado) movX -= 1;
        if (dPresionado || rightPresionado) movX += 1;

        // Movimiento con gamepad (tiene prioridad sobre teclado)
        if (ejeXGamepad != 0 || ejeYGamepad != 0) {
            movX = ejeXGamepad;
            movY = ejeYGamepad;
        }

        // Acciones del jugador
        if (botonAGamepad || spacePresionado) jugador.saltar();  // Salto con botón A o TAB
        if (botonBGamepad) jugador.atacar();  // Ataque con botón B

        // Aplicar movimiento si hay input
        if (movX != 0 || movY != 0) {
            jugador.mover(delta, movX, movY);
        }

        // Reiniciamos estados momentáneos del gamepad
        botonAGamepad = false;
        botonBGamepad = false;
    }
}
