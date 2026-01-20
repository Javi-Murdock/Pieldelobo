package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.javiergames.pieldelobo.DataBase.ConfiguracionPantalla;
import io.github.javiergames.pieldelobo.GestorJuego.Main;

/**
 * Pantalla de opciones para configurar resolución y modo de pantalla.
 * Permite cambiar entre modo ventana y pantalla completa, y seleccionar resoluciones.
 *
 * @author Javier Gala
 * @version 2.0
 */
public class OptionsScreen extends PantallaInicio {

    private Stage stage;
    private Skin skin;
    private TextButton[] botones;
    private int botonSeleccionado = 0;

    // Para navegación por teclado
    private boolean enResolucion = false;
    private boolean enPantallaCompleta = false;

    // Componentes de la UI
    private SelectBox<String> resolucionSelect;
    private CheckBox pantallaCompletaCheck;
    private Label resolucionLabel;
    private Label pantallaCompletaLabel;

    // Lista de resoluciones disponibles
    private String[] resolucionesRecomendadas;

    // Referencia para volver atrás
    private PantallaInicio pantallaAnterior;

    // Fondo
    private SpriteBatch batch;

    /**
     * Constructor básico sin pantalla anterior especificada.
     *
     * @param game Instancia principal del juego
     */
    public OptionsScreen(Main game) {
        super(game);
        this.pantallaAnterior = null;
        this.batch = new SpriteBatch();
    }

    /**
     * Constructor con pantalla anterior
     */
    public OptionsScreen(Main game, PantallaInicio pantallaAnterior) {
        super(game);
        this.pantallaAnterior = pantallaAnterior;
        this.batch = new SpriteBatch();
    }

    @Override
    public void show() {
        crearInterfaz();
        cargarConfiguracionActual();
    }

    private void crearInterfaz() {
        stage = new Stage(new ScreenViewport());

        // Intentar cargar skin personalizado primero
        try {
            skin = new Skin(Gdx.files.internal("starsoldierui/star-soldier-ui.json"));
        } catch (Exception e) {
            // Si falla, usar skin por defecto
            skin = new Skin(Gdx.files.internal("uiskin.json"));
        }

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        stage.addActor(mainTable);

        Table menuContainer = new Table();
        menuContainer.defaults().pad(10);

        // Título
        Label titulo = new Label("OPCIONES", skin);
        titulo.setFontScale(1.5f);
        menuContainer.add(titulo).colspan(2).padBottom(30).row();

        // Configurar resoluciones disponibles
        inicializarResoluciones();

        // Resolución
        resolucionLabel = new Label("Resolución:", skin);
        menuContainer.add(resolucionLabel).right().padRight(20);

        resolucionSelect = new SelectBox<>(skin);
        resolucionSelect.setItems(resolucionesRecomendadas);
        menuContainer.add(resolucionSelect).width(200).left().row();

        // Pantalla completa
        pantallaCompletaLabel = new Label("Pantalla completa:", skin);
        menuContainer.add(pantallaCompletaLabel).right().padRight(20);

        pantallaCompletaCheck = new CheckBox("", skin);
        menuContainer.add(pantallaCompletaCheck).left().row();

        // Botones
        TextButton aplicarButton = new TextButton("Aplicar", skin);
        TextButton guardarButton = new TextButton("Guardar y Volver", skin);
        TextButton cancelarButton = new TextButton("Cancelar", skin);
        TextButton defaultsButton = new TextButton("Valores por Defecto", skin);

        botones = new TextButton[]{aplicarButton, guardarButton, cancelarButton, defaultsButton};

        // Listeners para botones
        aplicarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                aplicarConfiguracion();
            }
        });

        guardarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                aplicarConfiguracion();
                guardarConfiguracion();
                volverAPantallaAnterior();
            }
        });

        cancelarButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volverAPantallaAnterior();
            }
        });

        defaultsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                restaurarValoresPorDefecto();
            }
        });

        // Añadir botones
        menuContainer.add(aplicarButton).colspan(2).row();
        menuContainer.add(guardarButton).colspan(2).row();
        menuContainer.add(cancelarButton).colspan(2).row();
        menuContainer.add(defaultsButton).colspan(2).row();

        // Añadir controles para navegación con teclado
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
                    case Keys.LEFT:
                        navegarIzquierda();
                        return true;
                    case Keys.RIGHT:
                        navegarDerecha();
                        return true;
                    case Keys.ENTER:
                    case Keys.SPACE:
                        activarSeleccion();
                        return true;
                    case Keys.ESCAPE:
                        volverAPantallaAnterior();
                        return true;
                }
                return false;
            }
        });

        // Listeners para cambios en los controles
        pantallaCompletaCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                actualizarDisponibilidadResoluciones();
            }
        });

        mainTable.add(menuContainer);
        Gdx.input.setInputProcessor(stage);
        actualizarSeleccion();
    }

    private void inicializarResoluciones() {
        ConfiguracionPantalla config = ConfiguracionPantalla.getInstance();
        resolucionesRecomendadas = config.getResolucionesRecomendadasVentana();
    }

    private void cargarConfiguracionActual() {
        ConfiguracionPantalla config = ConfiguracionPantalla.getInstance();

        // Cargar modo pantalla
        pantallaCompletaCheck.setChecked(config.isPantallaCompleta());

        // Cargar resolución actual
        String resolucionActual;
        if (config.isPantallaCompleta()) {
            resolucionActual = config.getAnchoActual() + "x" + config.getAltoActual();
        } else {
            resolucionActual = config.getAnchoVentana() + "x" + config.getAltoVentana();
        }

        // Buscar y seleccionar la resolución actual en el SelectBox
        for (int i = 0; i < resolucionesRecomendadas.length; i++) {
            if (resolucionesRecomendadas[i].equals(resolucionActual)) {
                resolucionSelect.setSelectedIndex(i);
                break;
            }
        }

        // Si no se encontró exacta, usar la primera
        if (resolucionSelect.getSelectedIndex() < 0 && resolucionesRecomendadas.length > 0) {
            resolucionSelect.setSelectedIndex(0);
        }

        actualizarDisponibilidadResoluciones();
    }

    private void actualizarDisponibilidadResoluciones() {
        if (pantallaCompletaCheck.isChecked()) {
            resolucionLabel.setText("Resolución (Pantalla completa):");
        } else {
            resolucionLabel.setText("Resolución (Ventana):");
        }
    }

    public void navegarArriba() {
        if (enResolucion) {
            // Cambiar a navegación de botones
            enResolucion = false;
            enPantallaCompleta = false;
            botonSeleccionado = 0;
        } else if (enPantallaCompleta) {
            // Ir a resolución
            enResolucion = true;
            enPantallaCompleta = false;
        } else {
            // Navegar entre botones
            botonSeleccionado--;
            if (botonSeleccionado < 0) {
                // Ir a pantalla completa
                enPantallaCompleta = true;
                botonSeleccionado = botones.length - 1;
            }
        }
        actualizarSeleccion();
    }

    public void navegarAbajo() {
        if (enResolucion) {
            // Ir a pantalla completa
            enResolucion = false;
            enPantallaCompleta = true;
        } else if (enPantallaCompleta) {
            // Ir al primer botón
            enPantallaCompleta = false;
            botonSeleccionado = 0;
        } else {
            // Navegar entre botones
            botonSeleccionado++;
            if (botonSeleccionado >= botones.length) {
                // Ir a resolución
                enResolucion = true;
                botonSeleccionado = 0;
            }
        }
        actualizarSeleccion();
    }

    public void navegarIzquierda() {
        if (enResolucion) {
            // Cambiar a resolución anterior
            int indiceActual = resolucionSelect.getSelectedIndex();
            if (indiceActual > 0) {
                resolucionSelect.setSelectedIndex(indiceActual - 1);
            } else {
                // Ir al final de la lista
                resolucionSelect.setSelectedIndex(resolucionesRecomendadas.length - 1);
            }
        } else if (enPantallaCompleta) {
            // Cambiar estado de pantalla completa
            pantallaCompletaCheck.setChecked(!pantallaCompletaCheck.isChecked());
            actualizarDisponibilidadResoluciones();
        }
        actualizarSeleccion();
    }

    public void navegarDerecha() {
        if (enResolucion) {
            // Cambiar a siguiente resolución
            int indiceActual = resolucionSelect.getSelectedIndex();
            if (indiceActual < resolucionesRecomendadas.length - 1) {
                resolucionSelect.setSelectedIndex(indiceActual + 1);
            } else {
                // Ir al principio de la lista
                resolucionSelect.setSelectedIndex(0);
            }
        } else if (enPantallaCompleta) {
            // Cambiar estado de pantalla completa
            pantallaCompletaCheck.setChecked(!pantallaCompletaCheck.isChecked());
            actualizarDisponibilidadResoluciones();
        }
        actualizarSeleccion();
    }

    public void activarSeleccion() {
        if (enResolucion) {
            // Al presionar ENTER en resolución, mover a pantalla completa
            enResolucion = false;
            enPantallaCompleta = true;
        } else if (enPantallaCompleta) {
            // Al presionar ENTER en pantalla completa, cambiar estado
            pantallaCompletaCheck.setChecked(!pantallaCompletaCheck.isChecked());
            actualizarDisponibilidadResoluciones();
        } else {
            // Activar botón seleccionado
            activarBotonSeleccionado();
        }
        actualizarSeleccion();
    }

    public void activarBotonSeleccionado() {
        switch (botonSeleccionado) {
            case 0:
                aplicarConfiguracion();
                break;
            case 1:
                aplicarConfiguracion();
                guardarConfiguracion();
                volverAPantallaAnterior();
                break;
            case 2:
                volverAPantallaAnterior();
                break;
            case 3:
                restaurarValoresPorDefecto();
                break;
        }
    }

    private void actualizarSeleccion() {
        // Actualizar colores de botones
        for (int i = 0; i < botones.length; i++) {
            if (i == botonSeleccionado && !enResolucion && !enPantallaCompleta) {
                botones[i].setColor(1, 1, 0, 1); // Amarillo para botón seleccionado
            } else {
                botones[i].setColor(1, 1, 1, 1); // Blanco para otros botones
            }
        }

        // Actualizar visualización para controles
        if (enResolucion) {
            // Resaltar SelectBox de resolución
            resolucionLabel.setColor(1, 1, 0, 1); // Amarillo
            pantallaCompletaLabel.setColor(1, 1, 1, 1); // Blanco
        } else if (enPantallaCompleta) {
            // Resaltar CheckBox de pantalla completa
            pantallaCompletaLabel.setColor(1, 1, 0, 1); // Amarillo
            resolucionLabel.setColor(1, 1, 1, 1); // Blanco
        } else {
            // Quitar resaltado de controles
            resolucionLabel.setColor(1, 1, 1, 1); // Blanco
            pantallaCompletaLabel.setColor(1, 1, 1, 1); // Blanco
        }
    }

    private void aplicarConfiguracion() {
        try {
            ConfiguracionPantalla config = ConfiguracionPantalla.getInstance();

            // Obtener resolución seleccionada
            String resolucionStr = resolucionSelect.getSelected();
            String[] partes = resolucionStr.split("x");

            if (partes.length == 2) {
                int ancho = Integer.parseInt(partes[0]);
                int alto = Integer.parseInt(partes[1]);

                if (pantallaCompletaCheck.isChecked()) {
                    config.setResolucionPantallaCompleta(ancho, alto, 60);
                } else {
                    config.setResolucionVentana(ancho, alto);
                }

                Gdx.app.log("OptionsScreen", "Configuración aplicada: " + resolucionStr +
                    " (" + (pantallaCompletaCheck.isChecked() ? "Pantalla completa" : "Ventana") + ")");
            }

        } catch (Exception e) {
            Gdx.app.error("OptionsScreen", "Error al aplicar configuración", e);
        }
    }

    private void guardarConfiguracion() {
        ConfiguracionPantalla.getInstance().guardarConfiguracion();
        Gdx.app.log("OptionsScreen", "Configuración guardada");
    }

    private void restaurarValoresPorDefecto() {
        ConfiguracionPantalla config = ConfiguracionPantalla.getInstance();

        pantallaCompletaCheck.setChecked(ConfiguracionPantalla.PANTALLA_COMPLETA_POR_DEFECTO);

        String resolucionDefault = ConfiguracionPantalla.ANCHO_POR_DEFECTO + "x" +
            ConfiguracionPantalla.ALTO_POR_DEFECTO;

        // Buscar la resolución por defecto en el SelectBox
        for (int i = 0; i < resolucionesRecomendadas.length; i++) {
            if (resolucionesRecomendadas[i].equals(resolucionDefault)) {
                resolucionSelect.setSelectedIndex(i);
                break;
            }
        }

        actualizarDisponibilidadResoluciones();
        Gdx.app.log("OptionsScreen", "Valores por defecto restaurados");
    }

    private void volverAPantallaAnterior() {
        if (pantallaAnterior != null) {
            // Volver a la pantalla anterior
            game.setScreen(pantallaAnterior);
        } else {
            // Volver al menú principal
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        if (stage != null) stage.dispose();
        if (skin != null) skin.dispose();
        if (batch != null) batch.dispose();

        Gdx.app.log("OptionsScreen", "Recursos liberados");
    }
}


