package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.GestorJuego.Main;
import io.github.javiergames.pieldelobo.Graficos.GameUtils;

/**
 * Menú de pausa como overlay - Versión sin skin.
 * Incluye opciones de guardar/cargar partida, configuración y retorno al juego.
 *
 * @author JavierGames
 * @version 2.0
 */
public class PauseScreen extends PantallaInicio {

    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private Label[] opciones;
    private int opcionSeleccionada = 0;

    /**
     * Constructor del overlay de pausa.
     * Guarda referencia a la pantalla original para poder reanudar correctamente.
     *
     * @param game Instancia principal del juego
     * @param pantallaPausada Pantalla que fue pausada
     */
    private PantallaInicio pantallaPausada;
    private boolean visible = true;

    // Opciones del menú - ACTUALIZADO CON GUARDAR/CARGAR
    private static final String[] TEXTO_OPCIONES = {
        "Reanudar",
        "Guardar Partida",
        "Cargar Partida",
        "Opciones",
        "Menú Principal",
        "Salir del Juego"
    };

    /**
     * Constructor del overlay de pausa
     * GUARDA LA REFERENCIA A LA PANTALLA ORIGINAL
     */
    public PauseScreen(Main game, PantallaInicio pantallaPausada) {
        super(game);
        this.pantallaPausada = pantallaPausada; // Guarda la referencia original
        this.batch = new SpriteBatch();
        cargarFuente();
    }
    private void cargarFuente() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/retrocomputer.ttf")
            );

            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.size = 18;
            parameter.color = com.badlogic.gdx.graphics.Color.WHITE;
            parameter.borderWidth = 1;
            parameter.borderColor = com.badlogic.gdx.graphics.Color.BLACK;
            parameter.shadowOffsetX = 1;
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = new com.badlogic.gdx.graphics.Color(0, 0, 0, 0.7f);

            this.font = generator.generateFont(parameter);
            generator.dispose();

            Gdx.app.log("MainScreen", "Fuente retrocomputer.ttf cargada correctamente");

        } catch (Exception e) {
            Gdx.app.error("MainScreen", "Error al cargar fuente retrocomputer.ttf", e);
            this.font = new BitmapFont();
            this.font.getData().setScale(1.2f);
            Gdx.app.log("MainScreen", "Usando fuente por defecto como fallback");
        }
    }
    @Override
    public void show() {
        crearInterfazPausa();
        Gdx.app.log("PauseScreen", "Overlay de pausa mostrado");
        Gdx.app.log("PauseScreen", "Pantalla pausada: " +
            (pantallaPausada != null ? pantallaPausada.getClass().getSimpleName() : "null"));
    }

    private void crearInterfazPausa() {
        stage = new Stage(new ScreenViewport());

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        // Crear opciones como labels
        opciones = new Label[TEXTO_OPCIONES.length];

        Table menuContainer = new Table();
        menuContainer.defaults().pad(15);

        // Asegurar que la fuente no sea null
        if (font == null) {
            font = new BitmapFont();
            Gdx.app.log("PauseScreen", "Fuente recreada en crearInterfazPausa");
        }

        // Crear LabelStyle con la fuente - CORREGIDO
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        // Título
        Label titulo = new Label("PAUSA", labelStyle);  // Usar labelStyle
        titulo.setFontScale(2.0f);
        menuContainer.add(titulo).padBottom(40).row();

        // Crear opciones - USAR EL MISMO STYLE
        for (int i = 0; i < TEXTO_OPCIONES.length; i++) {
            opciones[i] = new Label(TEXTO_OPCIONES[i], labelStyle);  // Usar labelStyle
            opciones[i].setFontScale(1.3f);
            menuContainer.add(opciones[i]).padBottom(20).row();
        }

        mainTable.add(menuContainer).center();

        // Configurar navegación con teclado
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Keys.UP:
                        navegarArriba();
                        return true;
                    case Keys.DOWN:
                        navegarAbajo();
                        return true;
                    case Keys.ENTER:
                    case Keys.SPACE:
                        activarOpcionSeleccionada();
                        return true;
                    case Keys.ESCAPE:
                        // ESC: Reanudar directamente sin crear nueva instancia
                        reanudarJuegoDirecto();
                        return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(stage);
        actualizarSeleccion();
    }

    public void navegarArriba() {
        opcionSeleccionada--;
        if (opcionSeleccionada < 0) {
            opcionSeleccionada = TEXTO_OPCIONES.length - 1;
        }
        actualizarSeleccion();
    }

    public void navegarAbajo() {
        opcionSeleccionada++;
        if (opcionSeleccionada >= TEXTO_OPCIONES.length) {
            opcionSeleccionada = 0;
        }
        actualizarSeleccion();
    }

    public void activarOpcionSeleccionada() {
        switch (opcionSeleccionada) {
            case 0:
                reanudarJuegoDirecto(); // Cambiado a método directo
                break;
            case 1:
                guardarPartida();
                break;
            case 2:
                cargarPartida();
                break;
            case 3:
                abrirOpciones();
                break;
            case 4:
                volverMenuPrincipal();
                break;
            case 5:
                salirDelJuego();
                break;
        }
    }

    private void actualizarSeleccion() {
        for (int i = 0; i < opciones.length; i++) {
            if (i == opcionSeleccionada) {
                opciones[i].setColor(1, 1, 0, 1); // Amarillo
            } else {
                opciones[i].setColor(1, 1, 1, 1); // Blanco
            }
        }
    }

    // ====================== MÉTODOS DE LAS OPCIONES ======================

    /**
     * MÉTODO CORREGIDO: Reanuda el juego VOLVIENDO a la pantalla original
     */
    private void reanudarJuegoDirecto() {
        Gdx.app.log("PauseScreen", "Reanudando juego DIRECTO...");
        visible = false;

        // IMPORTANTE: Volver a la pantalla ORIGINAL, no crear una nueva
        if (pantallaPausada != null) {
            // Restaurar el input processor de la pantalla original
            restaurarInputProcessor(pantallaPausada);

            // Volver directamente a la pantalla pausada
            game.setScreen(pantallaPausada);
            Gdx.app.log("PauseScreen", "Volviendo a pantalla: " +
                pantallaPausada.getClass().getSimpleName());
        } else {
            // Fallback: Volver al menú principal
            Gdx.app.error("PauseScreen", "No hay pantalla pausada, volviendo al menú");

            game.setScreen(new MenuScreen(game));

        }
    }

    /**
     * Restaura el input processor adecuado según la pantalla
     */
    private void restaurarInputProcessor(PantallaInicio pantalla) {
        try {
            if (pantalla instanceof MainScreen) {
                MainScreen main = (MainScreen) pantalla;
                // Llamar al método de reanudación específico
                main.reanudarDesdePausa();
                Gdx.app.log("PauseScreen", "Input restaurado para MainScreen");
            } else if (pantalla instanceof LobbyScreen) {
                LobbyScreen lobby = (LobbyScreen) pantalla;
                // Llamar al método de reanudación específico
                lobby.reanudarDesdePausa();
                Gdx.app.log("PauseScreen", "Input restaurado para LobbyScreen");
            } else if (pantalla instanceof MenuScreen) {
                // Para MenuScreen, el propio menú configura su input
                Gdx.app.log("PauseScreen", "MenuScreen manejará su propio input");
            }
        } catch (Exception e) {
            Gdx.app.error("PauseScreen", "Error restaurando input processor", e);
        }
    }

    private void guardarPartida() {
        Gdx.app.log("PauseScreen", "Abriendo pantalla de guardado...");
        visible = false;

        // Crear pantalla de guardado, pasando ESTA instancia como pantalla anterior
        SaveScreen saveScreen = new SaveScreen(game, "save", this);
        game.setScreen(saveScreen);
    }

    private void cargarPartida() {
        Gdx.app.log("PauseScreen", "Abriendo pantalla de carga...");
        visible = false;

        // Crear pantalla de carga
        SaveScreen loadScreen = new SaveScreen(game, "load", this);
        game.setScreen(loadScreen);
    }

    private void abrirOpciones() {
        Gdx.app.log("PauseScreen", "Abriendo opciones...");
        visible = false;

        // Pasar la pantalla pausada original para poder volver
        game.setScreen(new OptionsScreen(game, pantallaPausada));
    }

    private void volverMenuPrincipal() {
        Gdx.app.log("PauseScreen", "Volviendo al menú principal...");
        visible = false;

        // ========== DETENER MÚSICA DE CUALQUIER PANTALLA ==========
        try {
            if (pantallaPausada instanceof MainScreen) {
                MainScreen main = (MainScreen) pantallaPausada;
                main.detenerMusica(); // Detener música del nivel
                Gdx.app.log("PauseScreen", "Música de MainScreen detenida");
            } else if (pantallaPausada instanceof LobbyScreen) {
                LobbyScreen lobby = (LobbyScreen) pantallaPausada;
                lobby.detenerMusica(); // Detener música del lobby
                Gdx.app.log("PauseScreen", "Música de LobbyScreen detenida");
            }
        } catch (Exception e) {
            Gdx.app.error("PauseScreen", "Error deteniendo música", e);
        }

        // ========== CREAR NUEVA INSTANCIA DEL MENÚ ==========
        MenuScreen menuScreen = new MenuScreen(game);
        game.setScreen(menuScreen);

        // Opcional: Guardar el estado actual antes de salir
        GameState.getInstance().guardarPartida();

        // Liberar recursos de la pausa
        dispose();

        Gdx.app.log("PauseScreen", "Transición al menú principal completada");
    }

    private void salirDelJuego() {
        Gdx.app.log("PauseScreen", "Saliendo del juego...");

        // Opcional: Guardar antes de salir
        GameState.getInstance().guardarPartida();

        Gdx.app.exit();
    }

    @Override
    public void render(float delta) {
        if (!visible) return;

        // Fondo negro semitransparente
        Gdx.gl.glClearColor(0, 0, 0, 0.7f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujar fondo del menú
        batch.begin();

        // Fondo oscuro para el menú
        batch.setColor(0.1f, 0.1f, 0.2f, 0.95f);
        float menuWidth = 400;
        float menuHeight = 450;
        float menuX = (Gdx.graphics.getWidth() - menuWidth) / 2;
        float menuY = (Gdx.graphics.getHeight() - menuHeight) / 2;
        batch.draw(GameUtils.getWhitePixel(), menuX, menuY, menuWidth, menuHeight);

        // Borde del menú
        batch.setColor(0.3f, 0.3f, 0.6f, 1f);
        float grosorBorde = 4f;
        batch.draw(GameUtils.getWhitePixel(), menuX, menuY, menuWidth, grosorBorde);
        batch.draw(GameUtils.getWhitePixel(), menuX, menuY + menuHeight - grosorBorde, menuWidth, grosorBorde);
        batch.draw(GameUtils.getWhitePixel(), menuX, menuY, grosorBorde, menuHeight);
        batch.draw(GameUtils.getWhitePixel(), menuX + menuWidth - grosorBorde, menuY, grosorBorde, menuHeight);

        batch.end();

        // Renderizar el overlay de pausa
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        Gdx.app.log("PauseScreen", "Liberando recursos del overlay de pausa...");
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
    }

    public void activarBotonSeleccionado() {
        activarOpcionSeleccionada();
    }

    /**
     * Getter para la pantalla pausada (puede ser útil)
     */
    public PantallaInicio getPantallaPausada() {
        return pantallaPausada;
    }

}
