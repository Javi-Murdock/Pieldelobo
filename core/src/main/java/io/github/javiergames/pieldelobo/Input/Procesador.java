package io.github.javiergames.pieldelobo.Input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Vector3;

import io.github.javiergames.pieldelobo.Personajes.PersonajeLobby;
import io.github.javiergames.pieldelobo.Personajes.Protagonista;

/**
 * Clase que maneja todos los inputs del juego (teclado y gamepad).
 * Implementa tanto InputAdapter como ControllerListener para ambos tipos de entrada.
 * Convierte las entradas del usuario en acciones para el personaje jugador.
 * Soporta tanto Protagonista como PersonajeLobby.
 *
 * CONTROLES DISPONIBLES:
 * - Teclado: WASD/Flechas (movimiento), SPACE (saltar), E (atacar), Q (defender), F (ataque especial)
 * - Gamepad: Joystick izquierdo (movimiento), Botón A (saltar), Botón B (atacar), Botón X (ataque especial), Botón Y (defender)
 *
 *  * @author Javier Gala
 *  * @version 2.0
 */
public class Procesador extends InputAdapter implements ControllerListener {
    // ====================== REFERENCIAS ======================
    private Protagonista jugador;                     // Referencia al protagonista (juego principal)
    private PersonajeLobby jugadorLobby;              // Referencia al personaje lobby (menús)

    // ====================== ESTADOS DE TECLADO ======================
    private boolean wPresionado, aPresionado, sPresionado, dPresionado; // Movimiento WASD
    private boolean spacePresionado;                  // Tecla espacio (saltar)
    private boolean upPresionado, downPresionado, leftPresionado, rightPresionado; // Flechas
    private boolean ePresionado;                      // Tecla E (ataque básico)
    private boolean fPresionado;                      // Tecla F (ataque especial)
    private boolean qPresionado;                      // Tecla Q (defender/bloquear)

    // ====================== ESTADOS DE GAMEPAD ======================
    private float ejeXGamepad, ejeYGamepad;           // Valores de ejes analógicos (-1 a 1)
    private boolean botonAGamepad;                    // Botón A (XBOX: A, PS: X) - Saltar
    private boolean botonBGamepad;                    // Botón B (XBOX: B, PS: Círculo) - Atacar
    private boolean botonXGamepad;                    // Botón X (XBOX: X, PS: Cuadrado) - Ataque especial
    private boolean botonYGamepad;                    // Botón Y (XBOX: Y, PS: Triángulo) - Defender

    // ====================== CONFIGURACIÓN DE GAMEPAD ======================
    private static final float ZONA_MUERTA = 0.2f;    // Margen para ignorar pequeñas fluctuaciones en ejes
    private static final int BOTON_A = 0;             // Código del botón A
    private static final int BOTON_B = 1;             // Código del botón B
    private static final int BOTON_X = 2;             // Código del botón X
    private static final int BOTON_Y = 3;             // Código del botón Y
    private static final int EJE_X_IZQ = 0;           // Código del eje horizontal joystick izquierdo
    private static final int EJE_Y_IZQ = 1;           // Código del eje vertical joystick izquierdo

    // ====================== CONSTRUCTORES ======================

    /**
     * Constructor para controlar el Protagonista en el juego principal.
     * @param jugador Instancia del personaje jugador principal
     */
    public Procesador(Protagonista jugador) {
        this.jugador = jugador;
        this.jugadorLobby = null;
        configurarGamepad();
        Gdx.app.log("Procesador", "Configurado para Protagonista (Modo Plataformas)");
    }

    /**
     * Constructor para controlar el PersonajeLobby en menús.
     * @param jugadorLobby Instancia del personaje lobby
     */
    public Procesador(PersonajeLobby jugadorLobby) {
        this.jugadorLobby = jugadorLobby;
        this.jugador = null;
        configurarGamepad();
        Gdx.app.log("Procesador", "Configurado para PersonajeLobby (Modo Lobby/Menú)");
    }

    /**
     * Configura el listener para gamepads conectados.
     * Debe llamarse en el constructor para detectar gamepads automáticamente.
     */
    private void configurarGamepad() {
        Controllers.addListener(this);

        // Verificar gamepads conectados
        int gamepadsConectados = Controllers.getControllers().size;
        if (gamepadsConectados > 0) {
            Gdx.app.log("Procesador", "Gamepad(s) detectado(s): " + gamepadsConectados);
            for (Controller controller : Controllers.getControllers()) {
                Gdx.app.log("Procesador", "- " + controller.getName());
            }
        } else {
            Gdx.app.log("Procesador", "No se detectaron gamepads, usando solo teclado");
        }
    }

    // ====================== MÉTODOS DE TECLADO ======================

    /**
     * Se ejecuta cuando se presiona una tecla.
     * Actualiza los estados de las teclas presionadas.
     *
     * @param keycode Código de la tecla presionada
     * @return true indica que el input fue procesado
     */
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            // Movimiento básico (WASD)
            case Keys.W: wPresionado = true; break;
            case Keys.A: aPresionado = true; break;
            case Keys.S: sPresionado = true; break;
            case Keys.D: dPresionado = true; break;

            // Movimiento alternativo (Flechas)
            case Keys.UP: upPresionado = true; break;
            case Keys.DOWN: downPresionado = true; break;
            case Keys.LEFT: leftPresionado = true; break;
            case Keys.RIGHT: rightPresionado = true; break;

            // Acciones principales
            case Keys.SPACE:
                spacePresionado = true;
                Gdx.app.log("Input", "Tecla SPACE presionada (Salto)");
                break;

            case Keys.E:
                ePresionado = true;
                Gdx.app.log("Input", "Tecla E presionada (Ataque básico)");
                break;

            case Keys.F:
                fPresionado = true;
                Gdx.app.log("Input", "Tecla F presionada (Ataque especial)");
                break;

            case Keys.Q:
                qPresionado = true;
                Gdx.app.log("Input", "Tecla Q presionada (Defender)");
                break;
        }
        return true; // Indicar que procesamos el input
    }

    /**
     * Se ejecuta cuando se suelta una tecla.
     * Actualiza los estados de las teclas liberadas.
     *
     * @param keycode Código de la tecla liberada
     * @return true indica que el input fue procesado
     */
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.W: wPresionado = false; break;
            case Keys.A: aPresionado = false; break;
            case Keys.S: sPresionado = false; break;
            case Keys.D: dPresionado = false; break;

            case Keys.UP: upPresionado = false; break;
            case Keys.DOWN: downPresionado = false; break;
            case Keys.LEFT: leftPresionado = false; break;
            case Keys.RIGHT: rightPresionado = false; break;

            case Keys.SPACE: spacePresionado = false; break;
            case Keys.E: ePresionado = false; break;
            case Keys.F: fPresionado = false; break;
            case Keys.Q: qPresionado = false; break;
        }
        return true;
    }

    // ====================== MÉTODOS DE GAMEPAD ======================

    /**
     * Se ejecuta cuando se conecta un gamepad.
     * @param controller Controlador que se conectó
     */
    @Override
    public void connected(Controller controller) {
        Gdx.app.log("Gamepad", "Conectado: " + controller.getName());
    }

    /**
     * Se ejecuta cuando se desconecta un gamepad.
     * @param controller Controlador que se desconectó
     */
    @Override
    public void disconnected(Controller controller) {
        Gdx.app.log("Gamepad", "Desconectado: " + controller.getName());
        // Resetear estados del gamepad
        ejeXGamepad = 0;
        ejeYGamepad = 0;
        botonAGamepad = false;
        botonBGamepad = false;
        botonXGamepad = false;
        botonYGamepad = false;
    }

    /**
     * Se ejecuta cuando se presiona un botón del gamepad.
     * @param controller Controlador que generó el evento
     * @param buttonCode Código del botón presionado
     * @return true si el evento fue procesado
     */
    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case BOTON_A:
                botonAGamepad = true;
                Gdx.app.log("Gamepad", "Botón A presionado (Salto)");
                return true;

            case BOTON_B:
                botonBGamepad = true;
                Gdx.app.log("Gamepad", "Botón B presionado (Ataque básico)");
                return true;

            case BOTON_X:
                botonXGamepad = true;
                Gdx.app.log("Gamepad", "Botón X presionado (Ataque especial)");
                return true;

            case BOTON_Y:
                botonYGamepad = true;
                Gdx.app.log("Gamepad", "Botón Y presionado (Defender)");
                return true;
        }
        return false;
    }

    /**
     * Se ejecuta cuando se suelta un botón del gamepad.
     * @param controller Controlador que generó el evento
     * @param buttonCode Código del botón liberado
     * @return true si el evento fue procesado
     */
    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        switch (buttonCode) {
            case BOTON_A: botonAGamepad = false; return true;
            case BOTON_B: botonBGamepad = false; return true;
            case BOTON_X: botonXGamepad = false; return true;
            case BOTON_Y: botonYGamepad = false; return true;
        }
        return false;
    }

    /**
     * Se ejecuta cuando se mueve un eje analógico del gamepad.
     * Nota: El eje Y está invertido (-value) porque en gamepads
     * hacia arriba es negativo y queremos positivo para arriba.
     *
     * @param controller Controlador que generó el evento
     * @param axisCode Código del eje movido
     * @param value Valor del eje (-1 a 1)
     * @return true si el evento fue procesado
     */
    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        switch (axisCode) {
            case EJE_X_IZQ:  // Eje horizontal
                // Aplicar zona muerta para ignorar fluctuaciones pequeñas
                ejeXGamepad = Math.abs(value) > ZONA_MUERTA ? value : 0;
                return true;

            case EJE_Y_IZQ:  // Eje vertical (invertido)
                ejeYGamepad = Math.abs(value) > ZONA_MUERTA ? -value : 0;
                return true;
        }
        return false;
    }

    // ====================== MÉTODOS DE INPUT NO UTILIZADOS (requeridos por interfaz) ======================


    public boolean povMoved(Controller controller, int povCode, int value) {
        // D-Pad movement (cruz direccional)
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

    // ====================== LÓGICA PRINCIPAL DE ACTUALIZACIÓN ======================

    /**
     * Método principal que actualiza el estado del jugador según los inputs.
     * Combina entradas de teclado y gamepad, dando prioridad al gamepad.
     * Se debe llamar en cada frame del juego.
     *
     * @param delta Tiempo transcurrido desde el último frame (en segundos)
     */
    public void actualizar(float delta) {
        float movX = 0, movY = 0;

        // DEBUG: Verificar estados del gamepad
        if (Math.abs(ejeXGamepad) > ZONA_MUERTA || Math.abs(ejeYGamepad) > ZONA_MUERTA ||
            botonAGamepad || botonBGamepad || botonXGamepad || botonYGamepad) {
            Gdx.app.log("Procesador_Debug",
                String.format("Gamepad - X:%.2f Y:%.2f A:%b B:%b X:%b Y:%b",
                    ejeXGamepad, ejeYGamepad,
                    botonAGamepad, botonBGamepad, botonXGamepad, botonYGamepad));
        }

        // ====================== CALCULAR MOVIMIENTO CON TECLADO ======================
        // Movimiento horizontal (A/D o Flechas Izquierda/Derecha)
        if (aPresionado || leftPresionado) movX -= 1;
        if (dPresionado || rightPresionado) movX += 1;

        // Movimiento vertical solo para PersonajeLobby (W/S o Flechas Arriba/Abajo)
        if (jugadorLobby != null) {
            if (wPresionado || upPresionado) movY += 1;
            if (sPresionado || downPresionado) movY -= 1;
        }

        // ====================== CALCULAR MOVIMIENTO CON GAMEPAD (PRIORIDAD) ======================
        // Gamepad tiene prioridad sobre teclado si se está usando
        if (Math.abs(ejeXGamepad) > ZONA_MUERTA) {
            movX = ejeXGamepad;
        }
        if (Math.abs(ejeYGamepad) > ZONA_MUERTA && jugadorLobby != null) {
            movY = ejeYGamepad;
        }

        // ====================== APLICAR ACCIONES SEGÚN EL TIPO DE PERSONAJE ======================
        if (jugador != null) {
            actualizarProtagonista(delta, movX);
        } else if (jugadorLobby != null) {
            actualizarPersonajeLobby(delta, movX, movY);
        }
    }

    /**
     * Actualiza las acciones específicas del Protagonista (juego principal de plataformas).
     * @param delta Tiempo transcurrido
     * @param movX Dirección horizontal de movimiento (-1 izquierda, 1 derecha, 0 quieto)
     */
    private void actualizarProtagonista(float delta, float movX) {
        // DEBUG: Mostrar información de input
        if (botonAGamepad || botonBGamepad || botonXGamepad || botonYGamepad) {
            Gdx.app.log("Protagonista_Input",
                String.format("Botones - A:%b B:%b X:%b Y:%b",
                    botonAGamepad, botonBGamepad, botonXGamepad, botonYGamepad));
        }

        // SALTO (SPACE o Botón A del gamepad)
        if ((spacePresionado || botonAGamepad) && !jugador.estaAtacando()) {
            Gdx.app.log("Protagonista", "SALTANDO - space:" + spacePresionado + " botonA:" + botonAGamepad);
            jugador.saltar();
        }

        // ATAQUE BÁSICO (E o Botón B del gamepad)
        if ((ePresionado || botonBGamepad) && !jugador.estaAtacando()) {
            Gdx.app.log("Protagonista", "ATACANDO - e:" + ePresionado + " botonB:" + botonBGamepad);
            jugador.atacar();
        }

        // ATAQUE ESPECIAL (F o Botón X del gamepad)
        if ((fPresionado || botonXGamepad) && !jugador.estaAtacando()) {
            Gdx.app.log("Protagonista", "ATAQUE ESPECIAL - f:" + fPresionado + " botonX:" + botonXGamepad);
            jugador.atacarEspecial();
        }

        // DEFENDER/BLOQUEAR (Q o Botón Y del gamepad)
        if (qPresionado || botonYGamepad) {
            jugador.defender(true);
        } else {
            jugador.defender(false);
        }

        // MOVIMIENTO HORIZONTAL
        // IMPORTANTE: Llamar siempre, incluso con movX = 0, para transiciones de animación
        jugador.mover(delta, movX, 0);
    }

    /**
     * Actualiza las acciones específicas del PersonajeLobby (menús/lobby).
     * @param delta Tiempo transcurrido
     * @param movX Dirección horizontal de movimiento
     * @param movY Dirección vertical de movimiento
     */
    private void actualizarPersonajeLobby(float delta, float movX, float movY) {
        // PersonajeLobby tiene movimiento libre en ambas direcciones
        jugadorLobby.mover(delta, movX, movY);
    }

    // ====================== MÉTODOS DE ACCESO Y UTILIDAD ======================

    /**
     * Verifica si hay gamepads conectados al sistema.
     * @return true si hay al menos un gamepad conectado
     */
    public boolean hayGamepadConectado() {
        return Controllers.getControllers().size > 0;
    }

    /**
     * Obtiene información del estado actual del gamepad.
     * Útil para debugging o mostrar información al jugador.
     * @return String con información del estado del gamepad
     */
    public String getEstadoGamepad() {
        return String.format("Gamepad - Eje X: %.2f, Eje Y: %.2f, Botones: A=%b B=%b X=%b Y=%b",
            ejeXGamepad, ejeYGamepad,
            botonAGamepad, botonBGamepad, botonXGamepad, botonYGamepad);
    }

    /**
     * Obtiene el tipo de personaje que está siendo controlado actualmente.
     * @return "Protagonista", "PersonajeLobby" o "Ninguno"
     */
    public String getTipoPersonaje() {
        if (jugador != null) return "Protagonista";
        if (jugadorLobby != null) return "PersonajeLobby";
        return "Ninguno";
    }

    /**
     * Obtiene información de los controles activos.
     * @return String con información de los controles
     */
    public String getInfoControles() {
        StringBuilder info = new StringBuilder();
        info.append("Controles activos: ");

        if (aPresionado || leftPresionado) info.append("[IZQ] ");
        if (dPresionado || rightPresionado) info.append("[DER] ");
        if (spacePresionado) info.append("[SALTAR] ");
        if (ePresionado) info.append("[ATACAR] ");
        if (fPresionado) info.append("[ESPECIAL] ");
        if (qPresionado) info.append("[DEFENDER] ");

        if (Math.abs(ejeXGamepad) > ZONA_MUERTA || Math.abs(ejeYGamepad) > ZONA_MUERTA) {
            info.append("[GAMEPAD] ");
        }

        return info.toString();
    }

    /**
     * Limpia todos los estados de input.
     * Útil cuando se cambia de pantalla o se reinicia el juego.
     */
    public void limpiarEstados() {
        // Limpiar estados de teclado
        wPresionado = aPresionado = sPresionado = dPresionado = false;
        spacePresionado = false;
        upPresionado = downPresionado = leftPresionado = rightPresionado = false;
        ePresionado = fPresionado = qPresionado = false;

        // Limpiar estados de gamepad
        ejeXGamepad = ejeYGamepad = 0;
        botonAGamepad = botonBGamepad = botonXGamepad = botonYGamepad = false;

        Gdx.app.log("Procesador", "Estados de input limpiados");
    }

    // ====================== GETTERS PARA ESTADOS DEL GAMEPAD ======================

    /**
     * Obtiene el valor actual del eje X del gamepad.
     * @return Valor del eje X (-1 a 1)
     */
    public float getEjeXGamepad() {
        return ejeXGamepad;
    }

    /**
     * Obtiene el valor actual del eje Y del gamepad.
     * @return Valor del eje Y (-1 a 1)
     */
    public float getEjeYGamepad() {
        return ejeYGamepad;
    }

    /**
     * Verifica si el botón A está presionado.
     * @return true si el botón A está presionado
     */
    public boolean isBotonAPresionado() {
        return botonAGamepad;
    }

    /**
     * Verifica si el botón B está presionado.
     * @return true si el botón B está presionado
     */
    public boolean isBotonBPresionado() {
        return botonBGamepad;
    }

    /**
     * Verifica si el botón X está presionado.
     * @return true si el botón X está presionado
     */
    public boolean isBotonXPresionado() {
        return botonXGamepad;
    }

    /**
     * Verifica si el botón Y está presionado.
     * @return true si el botón Y está presionado
     */
    public boolean isBotonYPresionado() {
        return botonYGamepad;
    }
}
