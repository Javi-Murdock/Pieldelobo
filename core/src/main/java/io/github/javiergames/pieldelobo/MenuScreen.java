package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.DataBase.SaveSystem;
import io.github.javiergames.pieldelobo.GestorJuego.Main;

/**
 * Pantalla principal del menú del juego
 * Opciones: Nueva Partida, Cargar Partida, Opciones, Salir
 *
 *  * @author Javier Gala
 *  * @version 2.1
 *
 */
public class MenuScreen extends PantallaInicio {

    private Stage stage;
    private Skin skin;
    private TextButton[] botones;
    private int botonSeleccionado = 0;

    // Fondo
    private Texture fondo;
    private SpriteBatch batch;

    // Música
    private Music musica;

    // Sistema para verificar si hay partidas guardadas
    private SaveSystem saveSystem;
    private boolean hayPartidasGuardadas = false;

    public MenuScreen(Main game) {
        super(game);
        this.saveSystem = SaveSystem.getInstance();
    }

    @Override
    public void show() {
        // Verificar si hay partidas guardadas
        verificarPartidasGuardadas();

        // Cargar fondo
        fondo = new Texture(Gdx.files.internal("fondo-menu.png"));
        batch = new SpriteBatch();

        // Crear interfaz
        crearInterfaz();

        // Cargar y reproducir música
        cargarMusica();

        Gdx.app.log("MenuScreen", "Menú principal iniciado. Partidas guardadas: " + hayPartidasGuardadas);
    }

    private void verificarPartidasGuardadas() {
        try {
            // Verificar si hay al menos una partida guardada
            hayPartidasGuardadas = false;
            for (int i = 1; i <= saveSystem.getMaxSaveSlots(); i++) {
                if (!saveSystem.isSlotEmpty(i)) {
                    hayPartidasGuardadas = true;
                    break;
                }
            }

            Gdx.app.log("MenuScreen", "Verificación completada. Hay partidas: " + hayPartidasGuardadas);

        } catch (Exception e) {
            Gdx.app.error("MenuScreen", "Error verificando partidas guardadas", e);
            hayPartidasGuardadas = false;
        }
    }

    private void crearInterfaz() {
        stage = new Stage(new ScreenViewport());

        // Intentar cargar skin personalizado
        try {
            skin = new Skin(Gdx.files.internal("starsoldierui/star-soldier-ui.json"));
            Gdx.app.log("MenuScreen", "Skin personalizado cargado");
        } catch (Exception e) {
            // Si falla, usar skin por defecto
            try {
                skin = new Skin(Gdx.files.internal("uiskin.json"));
                Gdx.app.log("MenuScreen", "Skin por defecto cargado");
            } catch (Exception e2) {
                skin = new Skin();
                Gdx.app.error("MenuScreen", "No se pudo cargar ningún skin", e2);
            }
        }

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Table menuContainer = new Table();
        menuContainer.defaults().pad(10);

        // Título
        TextButton titulo = new TextButton("PIEL DE LOBO", skin);
        titulo.getLabel().setFontScale(2.0f);
        menuContainer.add(titulo).colspan(2).padBottom(40).row();

        // ========== CREAR BOTONES ==========

        // 1. Botón NUEVA PARTIDA
        TextButton nuevaPartidaButton = new TextButton("NUEVA PARTIDA", skin);
        nuevaPartidaButton.getLabel().setFontScale(1.5f);

        // 2. Botón CARGAR PARTIDA
        TextButton cargarPartidaButton = new TextButton("CARGAR PARTIDA", skin);
        cargarPartidaButton.getLabel().setFontScale(1.5f);

        // Si no hay partidas, deshabilitar este botón
        if (!hayPartidasGuardadas) {
            cargarPartidaButton.setDisabled(true);
            cargarPartidaButton.setColor(0.5f, 0.5f, 0.5f, 0.7f);
        }

        // 3. Botón OPCIONES
        TextButton opcionesButton = new TextButton("OPCIONES", skin);
        opcionesButton.getLabel().setFontScale(1.5f);

        // 4. Botón SALIR
        TextButton salirButton = new TextButton("SALIR DEL JUEGO", skin);
        salirButton.getLabel().setFontScale(1.5f);

        // Array de botones para navegación
        botones = new TextButton[]{nuevaPartidaButton, cargarPartidaButton, opcionesButton, salirButton};

        // ========== LISTENERS PARA BOTONES ==========

        // NUEVA PARTIDA
        nuevaPartidaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                iniciarNuevaPartida();
            }
        });

        // CARGAR PARTIDA
        cargarPartidaButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (hayPartidasGuardadas) {
                    abrirCargarPartida();
                }
                // Si no hay partidas, no hace nada (botón deshabilitado)
            }
        });

        // OPCIONES
        opcionesButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                abrirOpciones();
            }
        });

        // SALIR
        salirButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                salirDelJuego();
            }
        });

        // ========== AÑADIR BOTONES AL MENÚ ==========

        menuContainer.add(nuevaPartidaButton).padBottom(20).row();
        menuContainer.add(cargarPartidaButton).padBottom(20).row();
        menuContainer.add(opcionesButton).padBottom(20).row();
        menuContainer.add(salirButton).padBottom(20).row();

        mainTable.add(menuContainer);

        // ========== CONFIGURAR NAVEGACIÓN CON TECLADO ==========
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
                        activarBotonSeleccionado();
                        return true;
                    case Keys.ESCAPE:
                        salirDelJuego();
                        return true;
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(stage);
        actualizarSeleccion();
    }

    private void cargarMusica() {
        try {
            musica = Gdx.audio.newMusic(Gdx.files.internal("musica/musica-menu.mp3"));
            musica.setLooping(true);
            musica.setVolume(0.5f);
            musica.play();
            Gdx.app.log("MenuScreen", "Música de menú cargada");
        } catch (Exception e) {
            Gdx.app.error("MenuScreen", "Error cargando música", e);
            musica = null;
        }
    }

    // ========== NAVEGACIÓN CON TECLADO ==========

    public void navegarArriba() {
        botonSeleccionado--;
        if (botonSeleccionado < 0) {
            botonSeleccionado = botones.length - 1;
        }
        actualizarSeleccion();
    }

    public void navegarAbajo() {
        botonSeleccionado++;
        if (botonSeleccionado >= botones.length) {
            botonSeleccionado = 0;
        }
        actualizarSeleccion();
    }

    public void activarBotonSeleccionado() {
        switch (botonSeleccionado) {
            case 0: // NUEVA PARTIDA
                iniciarNuevaPartida();
                break;
            case 1: // CARGAR PARTIDA
                if (hayPartidasGuardadas) {
                    abrirCargarPartida();
                }
                break;
            case 2: // OPCIONES
                abrirOpciones();
                break;
            case 3: // SALIR
                salirDelJuego();
                break;
        }
    }

    private void actualizarSeleccion() {
        for (int i = 0; i < botones.length; i++) {
            if (i == botonSeleccionado) {
                // Botón seleccionado - resaltar
                botones[i].setColor(1, 1, 0, 1); // Amarillo

                // Añadir efecto visual de selección
                botones[i].getLabel().setFontScale(1.7f);
            } else {
                // Botón no seleccionado
                botones[i].setColor(1, 1, 1, 1); // Blanco normal

                // Para el botón de cargar si está deshabilitado
                if (i == 1 && !hayPartidasGuardadas) {
                    botones[i].setColor(0.5f, 0.5f, 0.5f, 0.7f);
                }

                // Restaurar tamaño normal
                botones[i].getLabel().setFontScale(1.5f);
            }
        }
    }

    // ========== MÉTODOS DE ACCIÓN ==========

    private void iniciarNuevaPartida() {
        Gdx.app.log("MenuScreen", "Iniciando NUEVA PARTIDA...");

        // Detener música del menú
        if (musica != null) {
            musica.stop();
            musica.dispose();
        }

        // Crear nueva partida en la base de datos
        DatabaseManager db = DatabaseManager.getInstance();
        db.nuevaPartida("Jugador");

        Gdx.app.log("MenuScreen", "Nueva partida creada, yendo al lobby...");

        // Ir directamente al lobby
        game.setScreen(new LobbyScreen(game));
    }

    private void abrirCargarPartida() {
        Gdx.app.log("MenuScreen", "Abriendo pantalla de CARGA...");

        // Detener música del menú
        if (musica != null) {
            musica.stop();
        }

        // Ir a la pantalla de carga
        SaveScreen loadScreen = new SaveScreen(game, "load", this);
        game.setScreen(loadScreen);
    }

    private void abrirOpciones() {
        Gdx.app.log("MenuScreen", "Abriendo OPCIONES...");

        // Detener música del menú
        if (musica != null) {
            musica.stop();
        }

        game.setScreen(new OptionsScreen(game, this));
    }

    private void salirDelJuego() {
        Gdx.app.log("MenuScreen", "Saliendo del juego...");

        // Detener música
        if (musica != null) {
            musica.stop();
            musica.dispose();
        }

        // Cerrar el juego
        Gdx.app.exit();
    }

    // ========== RENDER Y OTROS MÉTODOS ==========

    @Override
    public void render(float delta) {
        // Fondo oscuro
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // DIBUJAR FONDO
        batch.begin();
        // Escalar el fondo para llenar la pantalla
        batch.draw(fondo, 0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());
        batch.end();

        // UI encima
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
        Gdx.app.log("MenuScreen", "Liberando recursos del menú...");

        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (fondo != null) fondo.dispose();
        if (batch != null) batch.dispose();
        if (musica != null) {
            musica.stop();
            musica.dispose();
        }

        Gdx.app.log("MenuScreen", "Recursos liberados");
    }

    /**
     * Método para cuando se vuelve al menú desde otra pantalla
     */
    public void volverAlMenu() {
        Gdx.app.log("MenuScreen", "Volviendo al menú principal");

        // Verificar de nuevo si hay partidas guardadas
        verificarPartidasGuardadas();

        // Reiniciar la selección
        botonSeleccionado = 0;

        // Reiniciar la interfaz
        crearInterfaz();

        // Reiniciar la música si estaba parada
        if (musica != null && !musica.isPlaying()) {
            musica.play();
        }
    }
}
