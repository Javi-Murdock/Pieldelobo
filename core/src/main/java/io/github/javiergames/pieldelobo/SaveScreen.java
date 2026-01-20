package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;

import java.util.Map;

import io.github.javiergames.pieldelobo.DataBase.SaveSystem;
import io.github.javiergames.pieldelobo.GestorJuego.Main;

/**
 * Pantalla para guardar y cargar partidas - MEJORADA
 *
 *  * @author Javier Gala
 *  * @version 2.1
 *
 */
public class SaveScreen extends PantallaInicio {

    private Stage stage;
    private BitmapFont font;
    private BitmapFont fontSmall; // Nueva fuente más pequeña
    private SpriteBatch batch;

    private SaveSystem saveSystem;
    private Map<Integer, SaveSystem.SaveInfo> saves;

    // Modo: "save" o "load"
    private String mode = "save";

    // Slot seleccionado
    private int selectedSlot = 1;

    // Componentes UI
    private Table[] slotTables; // Tablas para cada slot
    private Label lblTitle;
    private Label lblInstructions;
    private TextField txtSaveName;
    private TextButton btnConfirm, btnCancel, btnDelete;

    // Pantalla anterior (para volver)
    private PantallaInicio previousScreen;

    // Constantes para mejorar legibilidad
    private static final int SLOT_WIDTH = 250;
    private static final int SLOT_HEIGHT = 100; // Aumentado para más espacio
    private static final int SLOTS_PER_ROW = 3;
    private static final float PADDING = 10f;
    private static final float TITLE_SCALE = 1.8f;
    private static final float INSTRUCTION_SCALE = 0.9f;

    public SaveScreen(Main game, String mode, PantallaInicio previousScreen) {
        super(game);
        this.mode = mode;
        this.previousScreen = previousScreen;
        this.saveSystem = SaveSystem.getInstance();
        this.batch = new SpriteBatch();
    }

    @Override
    public void show() {
        cargarFuentes();
        crearStage();
        refreshSaves();
        crearUI();
        Gdx.input.setInputProcessor(stage);
    }

    private void cargarFuentes() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/retrocomputer.ttf")
            );

            // Fuente normal
            FreeTypeFontGenerator.FreeTypeFontParameter normalParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            normalParam.size = 18;
            normalParam.color = com.badlogic.gdx.graphics.Color.WHITE;
            normalParam.borderWidth = 1;
            normalParam.borderColor = com.badlogic.gdx.graphics.Color.BLACK;
            this.font = generator.generateFont(normalParam);

            // Fuente pequeña para información detallada
            FreeTypeFontGenerator.FreeTypeFontParameter smallParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
            smallParam.size = 14;
            smallParam.color = com.badlogic.gdx.graphics.Color.WHITE;
            smallParam.borderWidth = 1;
            smallParam.borderColor = com.badlogic.gdx.graphics.Color.BLACK;
            this.fontSmall = generator.generateFont(smallParam);

            generator.dispose();
            Gdx.app.log("SaveScreen", "Fuentes cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("SaveScreen", "Error al cargar fuentes", e);
            this.font = new BitmapFont();
            this.font.getData().setScale(1.2f);
            this.fontSmall = new BitmapFont();
            this.fontSmall.getData().setScale(0.9f);
        }
    }

    private void crearStage() {
        stage = new Stage(new ScreenViewport());
    }

    private void refreshSaves() {
        saves = saveSystem.getAllSaves();
    }

    private void crearUI() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20); // Padding general
        stage.addActor(mainTable);

        // Crear estilos
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle smallLabelStyle = new Label.LabelStyle(fontSmall, Color.WHITE);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.cursor = null;
        textFieldStyle.selection = null;
        textFieldStyle.background = null;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;

        // Título
        lblTitle = new Label(mode.equals("save") ? "GUARDAR PARTIDA" : "CARGAR PARTIDA", labelStyle);
        lblTitle.setFontScale(TITLE_SCALE);
        lblTitle.setAlignment(Align.center);
        mainTable.add(lblTitle).colspan(3).padBottom(40).row();

        // Crear tabla de slots
        slotTables = new Table[SaveSystem.getInstance().getMaxSaveSlots()];

        Table slotsContainer = new Table();
        slotsContainer.defaults().pad(PADDING);

        for (int i = 1; i <= slotTables.length; i++) {
            final int slot = i;

            // Crear una tabla por cada slot para mejor organización
            Table slotTable = crearSlotTable(slot, labelStyle, smallLabelStyle);
            slotTables[i-1] = slotTable;

            // Hacer clicable toda la tabla
            slotTable.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selectSlot(slot);
                }
            });

            slotsContainer.add(slotTable).width(SLOT_WIDTH).height(SLOT_HEIGHT);

            // Nueva fila cada 3 slots
            if (i % SLOTS_PER_ROW == 0) {
                slotsContainer.row();
            }
        }

        mainTable.add(slotsContainer).colspan(3).padBottom(30).row();

        // Campo nombre de save (solo en modo guardar)
        if (mode.equals("save")) {
            Table nameTable = new Table();
            Label lblName = new Label("Nombre:", labelStyle);
            lblName.setAlignment(Align.right);

            txtSaveName = new TextField("", textFieldStyle);
            txtSaveName.setMaxLength(30);
            txtSaveName.setMessageText("Introduce nombre...");
            txtSaveName.setAlignment(Align.left);

            // Generar nombre automático
            String autoName = saveSystem.generateSaveName(selectedSlot);
            txtSaveName.setText(autoName);

            nameTable.add(lblName).width(100).padRight(10);
            nameTable.add(txtSaveName).width(300);
            mainTable.add(nameTable).colspan(3).padBottom(20).row();
        }

        // Instrucciones - con contraste mejorado
        lblInstructions = new Label("", labelStyle);
        lblInstructions.setFontScale(INSTRUCTION_SCALE);
        lblInstructions.setAlignment(Align.center);

        // Fondo para instrucciones
        Table instructionTable = new Table();
        instructionTable.setBackground(new TextButton.TextButtonStyle().up);
        instructionTable.pad(10);
        instructionTable.add(lblInstructions).pad(5);

        mainTable.add(instructionTable).colspan(3).padTop(20).padBottom(20).row();

        updateInstructions();

        // Botones - con mejor espaciado
        Table buttonsTable = new Table();
        buttonsTable.defaults().pad(15).width(160).height(50);

        // Crear botones con texto más claro
        if (mode.equals("load")) {
            btnDelete = crearBoton("ELIMINAR", Color.RED, buttonStyle);
            btnDelete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    deleteSave();
                }
            });
            buttonsTable.add(btnDelete);
        }

        btnCancel = crearBoton("CANCELAR", Color.LIGHT_GRAY, buttonStyle);
        btnCancel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goBack();
            }
        });

        String confirmText = mode.equals("save") ? "GUARDAR" : "CARGAR";
        Color confirmColor = mode.equals("save") ? Color.GREEN : Color.CYAN;
        btnConfirm = crearBoton(confirmText, confirmColor, buttonStyle);
        btnConfirm.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (mode.equals("save")) {
                    saveGame();
                } else {
                    loadGame();
                }
            }
        });

        buttonsTable.add(btnCancel);
        buttonsTable.add(btnConfirm);

        mainTable.add(buttonsTable).colspan(3).padTop(20).row();

        // Leyenda de controles
        Table controlsTable = new Table();
        Label controlsLabel = new Label("[↑↓←→] Navegar  [ENTER] Seleccionar  [ESC] Cancelar" +
            (mode.equals("load") ? "  [DEL] Eliminar" : ""), smallLabelStyle);
        controlsLabel.setAlignment(Align.center);
        controlsTable.add(controlsLabel);
        mainTable.add(controlsTable).colspan(3).padTop(20).row();

        // Input para navegación con teclado
        configurarNavegacionTeclado();

        // Seleccionar primer slot por defecto
        selectSlot(1);
    }

    private Table crearSlotTable(int slot, Label.LabelStyle normalStyle, Label.LabelStyle smallStyle) {
        SaveSystem.SaveInfo info = saves.get(slot);
        Table slotTable = new Table();
        slotTable.setBackground(new TextButton.TextButtonStyle().up); // Fondo para mejor visibilidad
        slotTable.pad(5);

        if (info != null && info.exists) {
            // Slot ocupado - información organizada
            Label slotNumLabel = new Label("SLOT " + slot, normalStyle);
            slotNumLabel.setAlignment(Align.center);

            Label nameLabel = new Label(info.saveName, smallStyle);
            nameLabel.setAlignment(Align.center);
            nameLabel.setEllipsis(true);

            Label progressLabel = new Label(info.progress, smallStyle);
            progressLabel.setAlignment(Align.center);

            Label timeLabel = new Label(info.timestamp, smallStyle);
            timeLabel.setAlignment(Align.center);

            slotTable.add(slotNumLabel).center().row();
            slotTable.add(nameLabel).center().expand().fill().row();
            slotTable.add(progressLabel).center().row();
            slotTable.add(timeLabel).center().row();

            slotTable.setColor(Color.GREEN);
        } else {
            // Slot vacío
            Label slotNumLabel = new Label("SLOT " + slot, normalStyle);
            slotNumLabel.setAlignment(Align.center);

            Label emptyLabel = new Label("<VACÍO>", smallStyle);
            emptyLabel.setAlignment(Align.center);

            slotTable.add(slotNumLabel).center().row();
            slotTable.add(emptyLabel).center().expand().fill().row();

            slotTable.setColor(Color.GRAY);
        }

        return slotTable;
    }

    private TextButton crearBoton(String texto, Color color, TextButton.TextButtonStyle baseStyle) {
        TextButton boton = new TextButton(texto, baseStyle);
        boton.getLabel().setColor(color);
        return boton;
    }

    private void configurarNavegacionTeclado() {
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                switch (keycode) {
                    case Keys.UP:
                        navigateUp();
                        return true;
                    case Keys.DOWN:
                        navigateDown();
                        return true;
                    case Keys.LEFT:
                        navigateLeft();
                        return true;
                    case Keys.RIGHT:
                        navigateRight();
                        return true;
                    case Keys.ENTER:
                    case Keys.SPACE:
                        if (mode.equals("save")) {
                            saveGame();
                        } else {
                            loadGame();
                        }
                        return true;
                    case Keys.ESCAPE:
                        goBack();
                        return true;
                    case Keys.DEL:
                        if (mode.equals("load")) {
                            deleteSave();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void selectSlot(int slot) {
        // Deseleccionar anterior - QUITAR COLOR AMARILLO
        if (selectedSlot >= 1 && selectedSlot <= slotTables.length) {
            Table oldSlotTable = slotTables[selectedSlot-1];
            SaveSystem.SaveInfo oldInfo = saves.get(selectedSlot);

            // Obtener todas las etiquetas de la tabla anterior - SIN PARÁMETRO
            for (Actor actor : oldSlotTable.getChildren()) {
                if (actor instanceof Label) {
                    Label label = (Label) actor;
                    if (oldInfo != null && oldInfo.exists) {
                        // Restaurar colores normales para slot ocupado
                        if (label.getText().toString().startsWith("SLOT")) {
                            label.setColor(Color.GREEN);
                        } else {
                            label.setColor(Color.LIGHT_GRAY);
                        }
                    } else {
                        // Restaurar color gris para slot vacío
                        label.setColor(Color.GRAY);
                    }
                }
            }
        }

        // Seleccionar nuevo - PONER AMARILLO
        selectedSlot = slot;
        Table newSlotTable = slotTables[selectedSlot-1];

        // Poner TODAS las etiquetas en AMARILLO - SIN PARÁMETRO
        for (Actor actor : newSlotTable.getChildren()) {
            if (actor instanceof Label) {
                ((Label) actor).setColor(Color.YELLOW);
            }
        }

        // Actualizar campo nombre si estamos guardando
        if (mode.equals("save") && txtSaveName != null) {
            SaveSystem.SaveInfo info = saves.get(selectedSlot);
            if (info != null && info.exists) {
                txtSaveName.setText(info.saveName);
            } else {
                String autoName = saveSystem.generateSaveName(selectedSlot);
                txtSaveName.setText(autoName);
            }
        }

        updateInstructions();
    }

    private void updateInstructions() {
        SaveSystem.SaveInfo info = saves.get(selectedSlot);

        if (mode.equals("save")) {
            if (info != null && info.exists) {
                lblInstructions.setText("¡ATENCIÓN! Sobreescribirás la partida existente.");
                lblInstructions.setColor(Color.ORANGE);
            } else {
                lblInstructions.setText("Crear nueva partida en este slot.");
                lblInstructions.setColor(Color.GREEN);
            }
        } else {
            if (info != null && info.exists) {
                lblInstructions.setText("Partida disponible. Presiona CARGAR para continuar.");
                lblInstructions.setColor(Color.CYAN);
            } else {
                lblInstructions.setText("Slot vacío. No se puede cargar.");
                lblInstructions.setColor(Color.RED);
            }
        }
    }

    private void navigateUp() {
        if (selectedSlot > SLOTS_PER_ROW) {
            selectSlot(selectedSlot - SLOTS_PER_ROW);
        }
    }

    private void navigateDown() {
        if (selectedSlot + SLOTS_PER_ROW <= slotTables.length) {
            selectSlot(selectedSlot + SLOTS_PER_ROW);
        }
    }

    private void navigateLeft() {
        if (selectedSlot > 1) {
            selectSlot(selectedSlot - 1);
        }
    }

    private void navigateRight() {
        if (selectedSlot < slotTables.length) {
            selectSlot(selectedSlot + 1);
        }
    }

    private void saveGame() {
        String saveName = txtSaveName != null ? txtSaveName.getText().trim() : "";

        if (saveName.isEmpty()) {
            saveName = saveSystem.generateSaveName(selectedSlot);
        }

        boolean success = saveSystem.saveGame(selectedSlot, saveName);

        if (success) {
            lblInstructions.setText("¡PARTIDA GUARDADA CON ÉXITO!");
            lblInstructions.setColor(Color.GREEN);

            refreshSaves();

            // Actualizar la UI completa para evitar superposición
            actualizarUI();

            Gdx.app.postRunnable(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                goBack();
            });

        } else {
            lblInstructions.setText("ERROR al guardar. Intenta de nuevo.");
            lblInstructions.setColor(Color.RED);
        }
    }

    private void loadGame() {
        SaveSystem.SaveInfo info = saves.get(selectedSlot);

        if (info == null || !info.exists) {
            lblInstructions.setText("No hay partida en este slot.");
            lblInstructions.setColor(Color.RED);
            return;
        }

        // Detener música primero
        detenerMusicaSiEsNecesario();

        // Luego cargar la partida
        boolean success = saveSystem.loadGame(selectedSlot);

        if (success) {
            lblInstructions.setText("¡Partida cargada con éxito!");
            lblInstructions.setColor(Color.GREEN);
        } else {
            lblInstructions.setText("ERROR al cargar la partida.");
            lblInstructions.setColor(Color.RED);
        }
    }

    private void detenerMusicaSiEsNecesario() {
        // Solo necesitamos detener música si venimos del lobby
        if (isViniendoDeLobby()) {
            LobbyScreen lobby = obtenerLobbyScreen();
            if (lobby != null) {
                try {
                    lobby.detenerMusica();
                } catch (Exception e) {
                    Gdx.app.error("SaveScreen", "No se pudo detener la música", e);
                }
            }
        }
    }

    private boolean isViniendoDeLobby() {
        if (previousScreen instanceof PauseScreen) {
            PauseScreen pause = (PauseScreen) previousScreen;
            return pause.getPantallaPausada() instanceof LobbyScreen;
        }
        return previousScreen instanceof LobbyScreen;
    }

    private LobbyScreen obtenerLobbyScreen() {
        if (previousScreen instanceof PauseScreen) {
            PauseScreen pause = (PauseScreen) previousScreen;
            PantallaInicio pantalla = pause.getPantallaPausada();
            if (pantalla instanceof LobbyScreen) {
                return (LobbyScreen) pantalla;
            }
        } else if (previousScreen instanceof LobbyScreen) {
            return (LobbyScreen) previousScreen;
        }
        return null;
    }

    private void deleteSave() {
        SaveSystem.SaveInfo info = saves.get(selectedSlot);

        if (info == null || !info.exists) {
            lblInstructions.setText("No hay partida para eliminar en este slot.");
            lblInstructions.setColor(Color.RED);
            return;
        }

        // Confirmación
        boolean success = saveSystem.deleteSave(selectedSlot);

        if (success) {
            lblInstructions.setText("Partida eliminada exitosamente.");
            lblInstructions.setColor(Color.RED);

            refreshSaves();

            // Actualizar la UI completa para evitar superposición
            actualizarUI();

        } else {
            lblInstructions.setText("ERROR al eliminar la partida.");
            lblInstructions.setColor(Color.RED);
        }
    }

    private void actualizarUI() {
        try {
            // Guardar el slot seleccionado actual
            int slotActual = selectedSlot;

            // Limpiar completamente el stage
            stage.clear();

            // Reinicializar variables
            txtSaveName = null;
            btnConfirm = null;
            btnCancel = null;
            btnDelete = null;

            // Volver a crear toda la interfaz desde cero
            crearUI();

            // Restaurar la selección del slot
            if (slotActual >= 1 && slotActual <= slotTables.length) {
                selectSlot(slotActual);
            } else {
                selectSlot(1); // Selección por defecto
            }

            Gdx.app.log("SaveScreen", "UI actualizada correctamente");

        } catch (Exception e) {
            Gdx.app.error("SaveScreen", "Error al actualizar UI", e);
            // En caso de error, volver a crear la UI desde cero
            try {
                stage.clear();
                crearUI();
                selectSlot(1);
            } catch (Exception e2) {
                Gdx.app.error("SaveScreen", "Error crítico al recrear UI", e2);
                // Si falla dos veces, volver a la pantalla anterior
                goBack();
            }
        }
    }

    private void goBack() {
        if (previousScreen != null) {
            game.setScreen(previousScreen);
            if (previousScreen instanceof PauseScreen) {
                ((PauseScreen) previousScreen).show();
            }
        } else {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1);
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
        if (font != null) font.dispose();
        if (fontSmall != null) fontSmall.dispose();
        if (batch != null) batch.dispose();
    }
}
