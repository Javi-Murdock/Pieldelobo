package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.GestorJuego.EventManager;
import io.github.javiergames.pieldelobo.GestorJuego.Main;
import io.github.javiergames.pieldelobo.Input.Procesador;
import io.github.javiergames.pieldelobo.Mapas.MapaManager;
import io.github.javiergames.pieldelobo.Mapas.IndicadorNivel;
import io.github.javiergames.pieldelobo.Personajes.Enemigos;
import io.github.javiergames.pieldelobo.Personajes.Esqueleto;
import io.github.javiergames.pieldelobo.Personajes.Bandido;
import io.github.javiergames.pieldelobo.Personajes.Arquero;
import io.github.javiergames.pieldelobo.Personajes.FabricaEnemigos;
import io.github.javiergames.pieldelobo.Personajes.Golem;
import io.github.javiergames.pieldelobo.Personajes.BrujaFuego;
import io.github.javiergames.pieldelobo.Personajes.Necromancer;
import io.github.javiergames.pieldelobo.Personajes.Protagonista;
import io.github.javiergames.pieldelobo.Videos.ViewportManager;

/**
 * Pantalla principal del juego con físicas de plataformas.
 * MODIFICADO: Ahora integra EventManager para manejar el flujo entre niveles
 *
 * @author Javier Gala
 * @version 2.1
 */
public class MainScreen extends PantallaInicio {
    // ====================== COMPONENTES GRÁFICOS ======================
    private Stage stage;
    private Protagonista jugador;
    private Texture fondo;
    private Procesador procesador;
    private MapaManager mapaManager;
    private OrthographicCamera camara;
    private SpriteBatch batch;
    private Viewport viewport;
    private BitmapFont font;
    // ====================== SISTEMA DE MUSICA ======================

    private Music musicaNivel;
    private boolean musicaCargada = false;

    // ====================== SISTEMA DE ENEMIGOS ======================
    private Array<Enemigos> enemigos;
    private int enemigosEliminados = 0;

    // ====================== INDICADOR DE NIVEL ======================
    private IndicadorNivel indicadorNivel;
    private boolean nivelListoParaCompletar = false;
    private boolean mostrandoMensajeIndicador = false;
    private float tiempoMensajeIndicador = 0f;

    // ====================== ESTADO DEL JUEGO ======================
    private GameState gameState;
    private EventManager eventManager;
    private float tiempoTranscurrido = 0;
    private boolean nivelCompletado = false;
    private boolean pausado = false;
    private Procesador procesadorGuardado;

    // ====================== DETECCIÓN DIRECTA DE GAMEPAD ======================
    private boolean usandoGamepad = false;
    private float tiempoSinGamepad = 0f;

    // ====================== RECURSOS HUD ======================
    private Texture corazonLleno;
    private Texture corazonVacio;

    // ====================== CONSTANTES ======================
    private static final int ENEMIGOS_OBJETIVO = 10;
    private static final float TIEMPO_MENSAJE_INDICADOR = 3.0f;
    private static final float TIEMPO_DETECCION_GAMEPAD = 2.0f;

    // ====================== CONTROL DE REINICIO ======================
    private boolean reiniciando = false;

    /**
     * Constructor de la pantalla principal del juego.
     */
    public MainScreen(Main game) {
        super(game);
        this.batch = new SpriteBatch();
        this.enemigos = new Array<>();

        // Inicializar fuentes y managers
        cargarFuenteHUD();

        // Obtener instancias de GameState y EventManager
        gameState = GameState.getInstance();
        eventManager = EventManager.getInstance();

        // Verificar si estamos reiniciando
        reiniciando = gameState.isReiniciandoNivel();

        Gdx.app.log("MainScreen", "Pantalla principal creada. Reiniciando: " + reiniciando);
    }

    /**
     * Carga la fuente retrocomputer.ttf para el HUD.
     */
    private void cargarFuenteHUD() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/retrocomputer.ttf")
            );

            FreeTypeFontParameter parameter = new FreeTypeFontParameter();
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
            Gdx.app.error("MainScreen", "Error al cargar fuente", e);
            this.font = new BitmapFont();
            this.font.getData().setScale(1.2f);
            Gdx.app.log("MainScreen", "Usando fuente por defecto como fallback");
        }
    }

    @Override
    public void show() {
        // Si ya está inicializado y solo estábamos pausados, reanudar
        if (jugador != null && mapaManager != null && pausado) {
            Gdx.app.log("MainScreen", "Reanudando desde pausa...");

            // Reanudar música si estaba pausada
            if (musicaNivel != null && !musicaNivel.isPlaying()) {
                musicaNivel.play();
            }
            reanudarDesdePausa();
            return;

        }


        // Inicializar juego por primera vez
        inicializarJuego();
        // Inicializar juego por primera vez
        inicializarJuego();

        // Cargar música del nivel
        cargarMusicaNivel();

        // Verificar y establecer nivel actual
        if (gameState.getNivelActual() == null || gameState.getNivelActual().isEmpty()) {
            String nivelId = gameState.getNivelSeleccionadoId();
            if (nivelId != null && !nivelId.isEmpty()) {
                gameState.setNivelActual(nivelId);
            }
        }

        Gdx.app.log("MainScreen", "Juego inicializado - Nivel: " +
            (gameState.getNivelActual() != null ? gameState.getNivelActual() : "No especificado"));

        // Log del estado de eventos
        eventManager.logEstado();
    }

    /**
     * Inicializa todos los componentes del juego.
     */
    private void inicializarJuego() {
        Gdx.app.log("MainScreen", "=== INICIALIZANDO JUEGO ===");
        Gdx.app.log("MainScreen", "Reiniciando: " + reiniciando);

        // ====================== CONFIGURAR CÁMARA Y VIEWPORT ======================
        camara = new OrthographicCamera();
        viewport = ViewportManager.createViewport(ViewportManager.ViewportType.EXTEND, camara);
        stage = new Stage(viewport);

        // ====================== CARGAR MAPA SEGÚN SELECCIÓN ======================
        cargarMapaSeleccionado();

        // ====================== CREAR JUGADOR ======================
        jugador = new Protagonista();
        jugador.setMapaManager(mapaManager);

        // Obtener posición de spawn del jugador desde el mapa
        Vector2 spawnJugador = mapaManager.obtenerPosicionSpawnJugador();
        jugador.setPosition(spawnJugador.x, spawnJugador.y);
        Gdx.app.log("MainScreen", "Spawn jugador en: " + spawnJugador.x + ", " + spawnJugador.y);

        // ====================== CONFIGURAR STAGE ======================
        stage.addActor(jugador);

        // ====================== CONFIGURAR INPUT ======================
        procesador = new Procesador(jugador);
        Gdx.input.setInputProcessor(procesador);

        // Añadir detección directa de gamepad
        configurarGamepadDirecto();

        // ====================== CARGAR RECURSOS DE RESPALDO ======================
        fondo = new Texture("fondo.jpg");

        // ====================== CARGAR RECURSOS HUD ======================
        try {
            corazonLleno = new Texture(Gdx.files.internal("corazon_lleno.png"));
            corazonVacio = new Texture(Gdx.files.internal("corazon_vacio.png"));
            Gdx.app.log("MainScreen", "Texturas HUD cargadas correctamente");
        } catch (Exception e) {
            Gdx.app.error("MainScreen", "Error al cargar texturas HUD", e);
            corazonLleno = crearTexturaRespaldo(true);
            corazonVacio = crearTexturaRespaldo(false);
            Gdx.app.log("MainScreen", "Texturas de respaldo creadas");
        }

        // ====================== CONFIGURAR VISTA DEL MAPA ======================
        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
        }

        // Aplicar viewport inicial
        viewport.apply();

        // ====================== CARGAR ENEMIGOS DESDE MAPA TILED ======================
        enemigos = new Array<>();

        // Crear fábrica de enemigos
        FabricaEnemigos fabrica = new FabricaEnemigos(mapaManager, jugador);

        // Obtener información de spawn del mapa
        Array<MapaManager.EnemigoSpawnInfo> spawnInfos = mapaManager.obtenerInfoSpawnEnemigos();

        if (spawnInfos.size > 0) {
            // Crear enemigos desde la información del mapa
            Array<Enemigos> enemigosDelMapa = fabrica.crearEnemigosDesdeSpawnInfo(spawnInfos);
            enemigos.addAll(enemigosDelMapa);

            // Añadir enemigos al stage
            for (Enemigos enemigo : enemigosDelMapa) {
                stage.addActor(enemigo);
            }

            Gdx.app.log("MainScreen", "Enemigos cargados desde mapa: " + enemigos.size);
        } else {
            // Si no hay enemigos en el mapa, crear algunos por defecto
            Gdx.app.log("MainScreen", "No se encontraron enemigos en el mapa, creando enemigos por defecto");
            crearEnemigosPorDefecto();
        }

        // CORREGIR POSICIÓN DE TODOS LOS ENEMIGOS
        corregirPosicionesEnemigos();

        // ====================== CREAR INDICADOR DE NIVEL ======================
        crearIndicadorNivelDesdeMapa();

        // DEBUG: Verificar gamepad
        if (procesador.hayGamepadConectado()) {
            Gdx.app.log("MainScreen", "✓ Gamepad detectado: " +
                com.badlogic.gdx.controllers.Controllers.getControllers().size + " conectados");
        }

        // Log información de debug
        ViewportManager.logViewportInfo(viewport, "MainScreen");
        Gdx.app.log("MainScreen", "Juego inicializado correctamente");
        Gdx.app.log("MainScreen", "Enemigos en nivel: " + enemigos.size);

        // Resetear flag de reinicio después de inicializar
        if (reiniciando) {
            gameState.setReiniciandoNivel(false);
            reiniciando = false;
        }
    }

    /**
     * Carga el mapa específico basado en la puerta seleccionada.
     */
    private void cargarMapaSeleccionado() {
        Gdx.app.log("MainScreen", "=== CARGANDO MAPA SELECCIONADO ===");

        // Debug: mostrar estado actual
        verificarCargaMapa();

        // Inicializar variables
        String rutaMapaFinal = null;
        boolean usarRutaDirecta = false;

        // ========== OPCIÓN 1: Verificar mapa específico en GameState ==========
        String mapaACargar = gameState.getMapaACargar();

        if (mapaACargar != null && !mapaACargar.isEmpty()) {
            Gdx.app.log("MainScreen", "Mapa especificado en GameState: " + mapaACargar);

            // Verificar si el archivo existe
            if (gameState.existeMapa(mapaACargar)) {
                rutaMapaFinal = mapaACargar;
                usarRutaDirecta = true;
                Gdx.app.log("MainScreen", "✅ Usando mapa especificado en GameState");
            } else {
                Gdx.app.error("MainScreen", "❌ Archivo no encontrado: " + mapaACargar);
            }
        }

        // ========== OPCIÓN 2: Determinar por ID del nivel ==========
        if (rutaMapaFinal == null) {
            String nivelId = gameState.getNivelSeleccionadoId();

            if (nivelId != null && !nivelId.isEmpty()) {
                Gdx.app.log("MainScreen", "Nivel seleccionado ID: " + nivelId);

                // Obtener ruta del mapa usando el switch case
                rutaMapaFinal = gameState.obtenerRutaMapaPorNivelId(nivelId);

                // Verificar si existe
                if (gameState.existeMapa(rutaMapaFinal)) {
                    Gdx.app.log("MainScreen", "✅ Mapeo por ID exitoso: " + nivelId + " -> " + rutaMapaFinal);
                } else {
                    Gdx.app.error("MainScreen", "❌ Mapa mapeado no existe: " + rutaMapaFinal);
                    rutaMapaFinal = null;
                }
            }
        }

        // ========== OPCIÓN 3: Por nivel actual ==========
        if (rutaMapaFinal == null) {
            String nivelActual = gameState.getNivelActual();

            if (nivelActual != null && !nivelActual.isEmpty()) {
                Gdx.app.log("MainScreen", "Nivel actual: " + nivelActual);

                rutaMapaFinal = gameState.obtenerRutaMapaPorNivelId(nivelActual);

                if (gameState.existeMapa(rutaMapaFinal)) {
                    Gdx.app.log("MainScreen", "✅ Mapeo por nivel actual exitoso");
                } else {
                    Gdx.app.error("MainScreen", "❌ Mapa no existe: " + rutaMapaFinal);
                    rutaMapaFinal = null;
                }
            }
        }

        // ========== OPCIÓN 4: Último recurso ==========
        if (rutaMapaFinal == null) {
            rutaMapaFinal = "Tiled/nivel_villa.tmx";
            Gdx.app.log("MainScreen", "⚠️ Usando mapa por defecto");
        }

        // ========== CARGAR EL MAPA ==========
        Gdx.app.log("MainScreen", "Cargando mapa: " + rutaMapaFinal);

        // Crear MapaManager con la ruta final
        mapaManager = new MapaManager(rutaMapaFinal);

        // ========== VERIFICAR CARGA ==========
        if (mapaManager.estaCargado()) {
            Gdx.app.log("MainScreen",
                "✅ Mapa cargado exitosamente: " +
                    mapaManager.getAnchoMapa() + "x" + mapaManager.getAltoMapa());

            // Log información del mapa
            mapaManager.logInfoMapa();
        } else {
            Gdx.app.error("MainScreen", "❌ Error al cargar el mapa");

            // Intentar cargar mapa por defecto
            mapaManager = new MapaManager("Tiled/nivel_villa.tmx");
            if (mapaManager.estaCargado()) {
                Gdx.app.log("MainScreen", "🔄 Mapa por defecto cargado como respaldo");
            }
        }

        // ========== LIMPIAR SELECCIÓN DESPUÉS DE USARLA ==========
        // Solo limpiar si no estamos reiniciando y si usamos ruta directa
        if (!reiniciando && usarRutaDirecta) {
            gameState.limpiarSeleccionMapa();
            Gdx.app.log("MainScreen", "Selección de mapa limpiada en GameState");
        } else if (reiniciando) {
            Gdx.app.log("MainScreen", "Manteniendo selección de mapa (reinicio en curso)");
        }
    }

    /**
     * Método para debug: verifica qué mapa se está cargando
     */
    private void verificarCargaMapa() {
        Gdx.app.log("MainScreen", "=== VERIFICACIÓN DE CARGA DE MAPA ===");
        Gdx.app.log("MainScreen", "1. Nivel actual: " + gameState.getNivelActual());
        Gdx.app.log("MainScreen", "2. Mapa a cargar: " + gameState.getMapaACargar());
        Gdx.app.log("MainScreen", "3. Nivel seleccionado ID: " + gameState.getNivelSeleccionadoId());
        Gdx.app.log("MainScreen", "4. Reiniciando: " + reiniciando);

        if (gameState.getMapaACargar() != null) {
            String ruta = gameState.getMapaACargar();
            boolean existe = Gdx.files.internal(ruta).exists();
            Gdx.app.log("MainScreen", "5. Archivo existe: " + existe + " (" + ruta + ")");
        }

        // Verificar mapeos por ID
        String[] niveles = {"nivel_1", "nivel_2", "nivel_3", "nivel_4", "nivel_5",
            "nivel_6", "nivel_7", "nivel_8"};
        for (String nivel : niveles) {
            String mapeo = gameState.obtenerRutaMapaPorNivelId(nivel);
            boolean existe = Gdx.files.internal(mapeo).exists();
            Gdx.app.log("MainScreen", "6. Mapeo " + nivel + " -> " + mapeo + " (existe: " + existe + ")");
        }
    }

    /**
     * Configura la detección directa de gamepad.
     */
    private void configurarGamepadDirecto() {
        // Verificar gamepads conectados
        int gamepads = com.badlogic.gdx.controllers.Controllers.getControllers().size;
        if (gamepads > 0) {
            Gdx.app.log("MainScreen", "Configurando detección directa de gamepad");
            usandoGamepad = true;
        }
    }

    /**
     * Detección y manejo directo de gamepad.
     */
    private void manejarGamepadDirecto(float delta) {
        if (com.badlogic.gdx.controllers.Controllers.getControllers().size == 0) {
            if (usandoGamepad) {
                tiempoSinGamepad += delta;
                if (tiempoSinGamepad > TIEMPO_DETECCION_GAMEPAD) {
                    usandoGamepad = false;
                    Gdx.app.log("MainScreen", "Gamepad desconectado o inactivo");
                }
            }
            return;
        }

        // Hay gamepad conectado
        if (!usandoGamepad) {
            usandoGamepad = true;
            Gdx.app.log("MainScreen", "Gamepad detectado - modo activo");
        }
        tiempoSinGamepad = 0f;

        try {
            com.badlogic.gdx.controllers.Controller controller =
                com.badlogic.gdx.controllers.Controllers.getControllers().first();

            // Detectar ejes para movimiento
            float ejeX = controller.getAxis(0);  // Eje X del joystick izquierdo
            float ejeY = controller.getAxis(1);  // Eje Y del joystick izquierdo

            // Aplicar zona muerta
            if (Math.abs(ejeX) > 0.2f || Math.abs(ejeY) > 0.2f) {
                // Mover al jugador directamente con el gamepad
                if (jugador != null && !jugador.estaDefendiendo()) {
                    jugador.mover(delta, ejeX, 0);
                }
            }

            // Detectar botones presionados
            for (int i = 0; i < 15; i++) {
                if (controller.getButton(i)) {
                    manejarBotonGamepad(i);
                    break; // Solo procesar un botón a la vez
                }
            }

        } catch (Exception e) {
            Gdx.app.error("MainScreen", "Error en detección directa de gamepad", e);
        }
    }

    /**
     * Maneja los botones del gamepad.
     */
    private void manejarBotonGamepad(int buttonCode) {
        if (jugador == null) return;

        switch (buttonCode) {
            case 0: // Botón A (XBOX: A, PS: X) - Saltar
                if (!jugador.estaAtacando()) {
                    jugador.saltar();
                }
                break;

            case 1: // Botón B (XBOX: B, PS: Círculo) - Atacar
                if (!jugador.estaAtacando()) {
                    jugador.atacar();
                }
                break;

            case 2: // Botón X (XBOX: X, PS: Cuadrado) - Ataque especial
                if (!jugador.estaAtacando()) {
                    jugador.atacarEspecial();
                }
                break;

            case 3: // Botón Y (XBOX: Y, PS: Triángulo) - Defender
                jugador.defender(true);
                break;

            case 7: // Botón Start
            case 9: // Botón Start alternativo
                if (!pausado) {
                    mostrarPausa();
                }
                break;
        }
    }

    /**
     * Maneja la liberación de botones del gamepad.
     */
    private void manejarSueltaBotonGamepad() {
        if (jugador == null) return;

        try {
            if (com.badlogic.gdx.controllers.Controllers.getControllers().size > 0) {
                com.badlogic.gdx.controllers.Controller controller =
                    com.badlogic.gdx.controllers.Controllers.getControllers().first();

                // Solo manejar botón Y (defender) que necesita saber cuando se suelta
                if (!controller.getButton(3)) { // Botón Y
                    jugador.defender(false);
                }
            }
        } catch (Exception e) {
            // Ignorar errores
        }
    }

    /**
     * Crea enemigos por defecto cuando no hay en el mapa
     */
    private void crearEnemigosPorDefecto() {
        // Enemigos de ejemplo (posición hardcodeada)
        Esqueleto esqueleto = new Esqueleto(300, 100);
        configurarEnemigo(esqueleto);

        Bandido bandidoPesado = new Bandido(500, 100, true);
        configurarEnemigo(bandidoPesado);

        Arquero arquero = new Arquero(900, 200);
        configurarEnemigo(arquero);

        Gdx.app.log("MainScreen", "Enemigos por defecto creados: " + enemigos.size);
    }

    /**
     * Crea una textura de respaldo para corazones.
     */
    private Texture crearTexturaRespaldo(boolean lleno) {
        Pixmap pixmap = new Pixmap(40, 40, Pixmap.Format.RGBA8888);

        if (lleno) {
            pixmap.setColor(1, 0, 0, 1);
            pixmap.fillCircle(10, 15, 8);
            pixmap.fillCircle(30, 15, 8);
            pixmap.fillTriangle(5, 15, 35, 15, 20, 35);
        } else {
            pixmap.setColor(1, 0, 0, 0.3f);
            pixmap.drawCircle(10, 15, 8);
            pixmap.drawCircle(30, 15, 8);
            pixmap.drawLine(5, 15, 20, 35);
            pixmap.drawLine(35, 15, 20, 35);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Configura un enemigo con las referencias necesarias.
     */
    private void configurarEnemigo(Enemigos enemigo) {
        enemigo.setMapaManager(mapaManager);
        enemigo.setObjetivo(jugador);

        // PASAR STAGE A ENEMIGOS CON PROYECTILES
        if (enemigo instanceof Arquero) {
            ((Arquero) enemigo).setStage(stage);
        }
        else if (enemigo instanceof BrujaFuego) {
            ((BrujaFuego) enemigo).setStage(stage);
        }
        else if (enemigo instanceof Necromancer) {
            ((Necromancer) enemigo).setStage(stage);
        }

        // Posición
        corregirPosicionEnemigo(enemigo);

        // Añadir al stage
        stage.addActor(enemigo);
        enemigos.add(enemigo);
    }

    /**
     * Corrige la posición inicial del enemigo para que esté sobre el suelo.
     */
    private void corregirPosicionEnemigo(Enemigos enemigo) {
        if (mapaManager == null || !enemigo.aplicarGravedad) return;

        float xOriginal = enemigo.getX();
        float yOriginal = enemigo.getY();

        String nombreClase = enemigo.getClass().getSimpleName();

        // Diferentes ajustes según el tipo de enemigo
        int maxBajar = 200;

        // El arquero necesita bajar más porque sus sprites son más altos
        if (nombreClase.equals("Arquero")) {
            maxBajar = 300;
        }

        // Bajar hasta encontrar suelo
        boolean encontroSuelo = false;
        for (int i = 0; i < maxBajar; i++) {
            enemigo.setY(enemigo.getY() - 1);
            if (mapaManager.hayColision(enemigo.getHitbox())) {
                enemigo.setY(enemigo.getY() + 1); // Subir un píxel para no estar dentro del suelo
                enemigo.enSuelo = true;
                enemigo.velocidadY = 0;
                encontroSuelo = true;
                break;
            }
        }

        // Si no encontró suelo después de bajar
        if (!encontroSuelo) {
            enemigo.setY(yOriginal); // Volver a la posición original

            // Intentar subir para evitar colisión inicial
            for (int i = 0; i < 100; i++) {
                enemigo.setY(enemigo.getY() + 1);
                if (!mapaManager.hayColision(enemigo.getHitbox())) {
                    break;
                }
            }
        }
    }

    /**
     * Corrige la posición de todos los enemigos existentes.
     */
    private void corregirPosicionesEnemigos() {
        for (Enemigos enemigo : enemigos) {
            corregirPosicionEnemigo(enemigo);
        }
        Gdx.app.log("MainScreen", "Posiciones de enemigos corregidas");
    }

    /**
     * Crea el indicador de nivel desde la información del mapa.
     */
    private void crearIndicadorNivelDesdeMapa() {
        if (mapaManager == null) {
            Gdx.app.log("MainScreen", "MapaManager es null, no se puede crear indicador");
            return;
        }

        // Obtener información de indicadores desde el mapa
        Array<MapaManager.IndicadorSpawnInfo> indicadores =
            mapaManager.obtenerInfoIndicadoresNivel();

        if (indicadores.size > 0) {
            // Usar el primer indicador encontrado
            MapaManager.IndicadorSpawnInfo info = indicadores.first();
            indicadorNivel = new IndicadorNivel(
                info.posicion.x, info.posicion.y,
                info.tamaño.x > 0 ? info.tamaño.x : 100,
                info.tamaño.y > 0 ? info.tamaño.y : 200
            );

            // Agregar al stage
            stage.addActor(indicadorNivel);

            Gdx.app.log("MainScreen", "Indicador de nivel creado: " + info.nombre +
                " en [" + info.posicion.x + "," + info.posicion.y + "]");
        } else {
            // Crear indicador por defecto (centro del mapa)
            float x = mapaManager.getAnchoMapa() / 2 - 50;
            float y = mapaManager.getAltoMapa() / 2 - 100;
            indicadorNivel = new IndicadorNivel(x, y, 100, 200);
            stage.addActor(indicadorNivel);

            Gdx.app.log("MainScreen", "Indicador por defecto creado en centro del mapa");
        }

        // Inicialmente el indicador está invisible
        indicadorNivel.setTodosEnemigosEliminados(false);
    }

    @Override
    public void render(float delta) {
        // Actualizar tiempo del juego
        tiempoTranscurrido += delta;
        if (gameState != null) {
            gameState.actualizarTiempoNivel(delta);
        }

        // Actualizar tiempo del mensaje del indicador
        if (mostrandoMensajeIndicador) {
            tiempoMensajeIndicador += delta;
            if (tiempoMensajeIndicador >= TIEMPO_MENSAJE_INDICADOR) {
                mostrandoMensajeIndicador = false;
            }
        }

        // Verificar si se presionó ESC para pausa
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            mostrarPausa();
            return;
        }

        // Si el juego está pausado, no actualizar
        if (pausado) {
            return;
        }

        // ========== DETECCIÓN DIRECTA DE GAMEPAD ==========
        manejarGamepadDirecto(delta);
        manejarSueltaBotonGamepad();

        // ========== LÓGICA PRINCIPAL DEL JUEGO ==========

        // Limpiar pantalla
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar cámara para seguir al jugador
        actualizarCamara();

        // Renderizar mapa o fondo de respaldo
        renderizarFondo();

        // Actualizar procesador de input (solo para teclado)
        procesador.actualizar(delta);

        // Actualizar lógica del juego
        actualizarLogicaJuego(delta);

        // Verificar combate
        verificarCombate();

        // Limpiar enemigos muertos
        limpiarEnemigosMuertos();

        // Verificar condiciones de victoria/derrota
        verificarEstadoJuego();

        // Actualizar y dibujar stage (jugador, enemigos e indicador)
        stage.act(delta);
        stage.draw();

        // Dibujar HUD
        dibujarHUD();

        // Dibujar mensaje del indicador si es necesario
        if (mostrandoMensajeIndicador) {
            dibujarMensajeIndicador();
        }
    }

    /**
     * Actualiza la posición de la cámara para seguir al jugador
     */
    private void actualizarCamara() {
        if (jugador != null) {
            float targetX = jugador.getX() + jugador.getWidth() / 2;
            float targetY = jugador.getY() + jugador.getHeight() / 2;

            float anchoMapa = mapaManager.getAnchoMapa();
            float altoMapa = mapaManager.getAltoMapa();
            float anchoViewport = viewport.getWorldWidth();
            float altoViewport = viewport.getWorldHeight();

            targetX = Math.max(targetX, anchoViewport / 2);
            targetX = Math.min(targetX, anchoMapa - anchoViewport / 2);
            targetY = Math.max(targetY, altoViewport / 2);
            targetY = Math.min(targetY, altoMapa - altoViewport / 2);

            camara.position.set(targetX, targetY, 0);
        }
        camara.update();
    }

    /**
     * Renderiza el fondo (mapa o textura de respaldo)
     */
    private void renderizarFondo() {
        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
            mapaManager.renderizar();
        } else {
            batch.begin();
            batch.setProjectionMatrix(camara.combined);
            batch.draw(fondo, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }
    }

    /**
     * Lógica adicional del juego
     */
    private void actualizarLogicaJuego(float delta) {
        // Lógica adicional del juego
    }

    /**
     * Verifica combate entre jugador y enemigos
     */
    private void verificarCombate() {
        // 1. Ataques del jugador hacia enemigos
        if (jugador.estaAtacando()) {
            Rectangle areaAtaque = jugador.getAreaAtaque();

            for (Enemigos enemigo : enemigos) {
                if (enemigo.estaVivo() && areaAtaque.overlaps(enemigo.getHitbox())) {
                    enemigo.recibirDano();
                    Gdx.app.log("Combate", "¡Golpe a " +
                        enemigo.getClass().getSimpleName() + "!");

                    if (!enemigo.estaVivo()) {
                        enemigosEliminados++;
                        Gdx.app.log("Combate", "Enemigo eliminado! Total: " +
                            enemigosEliminados + "/" + ENEMIGOS_OBJETIVO);

                        // Verificar si todos los enemigos han sido eliminados
                        if (enemigosEliminados >= ENEMIGOS_OBJETIVO && indicadorNivel != null) {
                            activarIndicadorNivel();
                        }
                    }
                }
            }
        }

        // 2. Daño por contacto con enemigos
        verificarDañoPorContacto();

        // 3. Los proyectiles ya manejan su propia colisión con el jugador
    }

    /**
     * Verifica daño por contacto con enemigos
     */
    private void verificarDañoPorContacto() {
        for (Enemigos enemigo : enemigos) {
            if (enemigo.estaVivo() && !enemigo.estaAtacando() &&
                jugador.getHitbox().overlaps(enemigo.getHitbox())) {
                jugador.recibirDano();
                break;
            }
        }
    }

    /**
     * Limpia enemigos muertos de la lista
     */
    private void limpiarEnemigosMuertos() {
        for (int i = enemigos.size - 1; i >= 0; i--) {
            Enemigos enemigo = enemigos.get(i);
            if (!enemigo.estaVivo() && enemigo.getStage() == null) {
                enemigos.removeIndex(i);
            }
        }
    }

    /**
     * Cuenta enemigos muertos en la lista
     */
    private int contarEnemigosMuertosEnLista() {
        int muertos = 0;
        for (Enemigos enemigo : enemigos) {
            if (!enemigo.estaVivo()) {
                muertos++;
            }
        }
        return muertos;
    }

    /**
     * Verifica el estado del juego (victoria/derrota)
     */
    private void verificarEstadoJuego() {
        // Verificar si el jugador murió
        if (!jugador.estaVivo()) {
            reiniciarNivel();
            return;
        }

        // Verificar si se completó el nivel (eliminar todos los enemigos)
        if (enemigosEliminados >= ENEMIGOS_OBJETIVO && !nivelCompletado) {
            activarIndicadorNivel();
        }

        // Verificar si el jugador tocó el indicador activo
        verificarContactoIndicador();
    }

    /**
     * Activa el indicador de nivel cuando se eliminan todos los enemigos.
     */
    private void activarIndicadorNivel() {
        if (indicadorNivel != null && !nivelListoParaCompletar) {
            indicadorNivel.setTodosEnemigosEliminados(true);
            nivelListoParaCompletar = true;

            // Mostrar mensaje al jugador
            mostrandoMensajeIndicador = true;
            tiempoMensajeIndicador = 0f;

            Gdx.app.log("MainScreen", "¡Todos los enemigos eliminados! " +
                "Busca el portal de salida.");

            // Reproducir sonido de activación
            try {
                com.badlogic.gdx.audio.Sound sound =
                    Gdx.audio.newSound(Gdx.files.internal("sounds/portal_activate.wav"));
                if (sound != null) {
                    sound.play(0.5f);
                }
            } catch (Exception e) {
                // Silenciar si no hay sonido disponible
            }
        }
    }

    /**
     * Verifica si el jugador tocó el indicador activo.
     */
    private void verificarContactoIndicador() {
        if (indicadorNivel != null &&
            indicadorNivel.isVisible() &&
            indicadorNivel.getHitbox().overlaps(jugador.getHitbox())) {

            Gdx.app.log("MainScreen", "¡Jugador tocó el indicador de salida!");
            completarNivel();
        }
    }

    /**
     * Muestra el menú de pausa
     */
    private void mostrarPausa() {
        Gdx.app.log("MainScreen", "Mostrando menú de pausa");
        pausado = true;
        procesadorGuardado = procesador;

        game.setScreen(new PauseScreen(game, this));
    }

    /**
     * Reanuda el juego
     */
    public void reanudarJuego() {
        Gdx.app.log("MainScreen", "Juego reanudado");
        pausado = false;

        if (procesadorGuardado != null) {
            procesador = procesadorGuardado;
            Gdx.input.setInputProcessor(procesador);
        }
    }

    /**
     * Reanuda desde pausa
     */
    public void reanudarDesdePausa() {
        Gdx.app.log("MainScreen", "Reanudando desde pausa");
        pausado = false;

        if (procesadorGuardado != null) {
            procesador = procesadorGuardado;
            Gdx.input.setInputProcessor(procesador);
        }

        Gdx.app.log("MainScreen", "Input processor restaurado");
    }

    /**
     * Reinicia el nivel actual.
     */
    private void reiniciarNivel() {
        Gdx.app.log("MainScreen", "¡Jugador muerto! Reiniciando nivel...");

        // Guardar partida actual
        if (gameState != null) {
            gameState.guardarPartida();
        }
        // Detener musica actual
        if (musicaNivel != null) {
            musicaNivel.stop();
        }
        // Establecer que estamos reiniciando
        gameState.setReiniciandoNivel(true);

        // NO limpiar la selección de mapa - mantener el mismo nivel
        // Solo crear nuevo MainScreen
        game.setScreen(new MainScreen(game));
    }

    /**
     * Completa el nivel actual.
     * MODIFICADO: Ahora también actualiza EventManager
     */
    private void completarNivel() {
        if (nivelCompletado) return;

        nivelCompletado = true;
        Gdx.app.log("MainScreen", "¡Nivel completado!");

        if (gameState != null) {
            String nivelId = gameState.getNivelActual();
            if (nivelId == null || nivelId.isEmpty()) {
                nivelId = gameState.getNivelSeleccionadoId();
            }

            if (nivelId != null && !nivelId.isEmpty()) {
                // **CORREGIDO: Pasar ambos parámetros (nivelId y tiempo)**
                gameState.completarNivel(nivelId, tiempoTranscurrido);

                // **NUEVO: Actualizar EventManager (ya se hace dentro de gameState.completarNivel)**
                // No es necesario llamarlo aquí porque gameState.completarNivel ya lo hace

                // **NUEVO: Mostrar progreso actual**
                io.github.javiergames.pieldelobo.DataBase.DatabaseManager db =
                    io.github.javiergames.pieldelobo.DataBase.DatabaseManager.getInstance();
                float progreso = db.getProgresoTotal();
                Gdx.app.log("MainScreen", "Progreso total actual: " + String.format("%.1f", progreso) + "%");

                // **NUEVO: Obtener próxima misión**
                EventManager eventManager = EventManager.getInstance();
                String proximoNPC = eventManager.getProximoNPCConMision();
                if (proximoNPC != null) {
                    Gdx.app.log("MainScreen", "Próxima misión: Habla con " + proximoNPC);
                }

                // Guardar partida
                gameState.guardarPartida();
            }
        }

        Gdx.app.log("MainScreen", "Tiempo en nivel: " +
            String.format("%.1f", tiempoTranscurrido) + "s");
        Gdx.app.log("MainScreen", "Enemigos eliminados: " + enemigosEliminados);

        // Volver al lobby después de un breve retraso
        volverAlLobby();
    }


    /**
     * Vuelve al lobby después de completar el nivel.
     * MODIFICADO: Ahora muestra información sobre la próxima misión
     */
    private void volverAlLobby() {
        Gdx.app.log("MainScreen", "Volviendo al lobby...");

        // **DETENER MÚSICA DEL NIVEL ANTES DE SALIR**
        detenerMusica();

        // **NUEVO: Mostrar información sobre la próxima misión**
        mostrarInformacionProximaMision();

        // Resto del código existente...
        // Reproducir sonido de éxito
        try {
            com.badlogic.gdx.audio.Sound sound =
                Gdx.audio.newSound(Gdx.files.internal("sounds/level_complete.wav"));
            if (sound != null) {
                sound.play(0.7f);
            }
        } catch (Exception e) {
            // Silenciar si no hay sonido disponible
        }

        // Esperar un momento antes de cambiar de pantalla
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500); // 1.5 segundos

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new LobbyScreen(game));
                            dispose();
                        }
                    });
                } catch (InterruptedException e) {
                    Gdx.app.error("MainScreen", "Error en hilo de retorno al lobby", e);
                    // Cambiar inmediatamente si hay error
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new LobbyScreen(game));
                            dispose();
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * **NUEVO: Muestra información sobre la próxima misión**
     * Se llama antes de volver al lobby
     */
    private void mostrarInformacionProximaMision() {
        String proximoNPC = eventManager.getProximoNPCConMision();

        if (proximoNPC != null) {
            Gdx.app.log("MainScreen", "🎯 INFORMACIÓN DE PRÓXIMA MISIÓN:");
            Gdx.app.log("MainScreen", "   Habla con: " + proximoNPC);

            String dialogoId = eventManager.getDialogoProximaMision();
            if (dialogoId != null) {
                Gdx.app.log("MainScreen", "   Diálogo: " + dialogoId);
            }

            String proximoNivel = eventManager.getProximoNivelDisponible();
            if (proximoNivel != null) {
                Gdx.app.log("MainScreen", "   Desbloqueará: " + proximoNivel);
            }

            // Podrías mostrar esto en pantalla también
            mostrarMensajeProximaMision(proximoNPC);
        } else {
            Gdx.app.log("MainScreen", "¡No hay más misiones disponibles!");
        }
    }

    /**
     * **NUEVO: Muestra un mensaje en pantalla sobre la próxima misión**
     */
    private void mostrarMensajeProximaMision(String npcNombre) {
        // Esta función podría mostrar un mensaje en pantalla
        // Por ahora solo lo logueamos
        Gdx.app.log("MainScreen", "💬 Recuerda: Habla con " + npcNombre + " para tu próxima misión");
    }

    /**
     * Dibuja un mensaje informativo sobre el indicador de nivel.
     */
    private void dibujarMensajeIndicador() {
        if (!mostrandoMensajeIndicador || font == null) return;

        batch.begin();
        batch.setProjectionMatrix(camara.combined);

        // Calcular opacidad
        float alpha = 1.0f;
        if (tiempoMensajeIndicador > TIEMPO_MENSAJE_INDICADOR - 1.0f) {
            float fadeTime = tiempoMensajeIndicador - (TIEMPO_MENSAJE_INDICADOR - 1.0f);
            alpha = 1.0f - fadeTime;
            // Efecto de parpadeo en los últimos segundos
            if (fadeTime > 0.5f) {
                alpha = (float)Math.sin(fadeTime * 10f) * 0.5f + 0.5f;
            }
        }

        // Configurar fuente
        font.setColor(1, 1, 0.5f, alpha);
        font.getData().setScale(1.3f);

        // Texto del mensaje
        String mensaje = "¡Busca el portal de salida!";

        // Calcular posición
        float x = camara.position.x;
        float y = camara.position.y + 100;

        // Calcular dimensiones del texto
        GlyphLayout layout = new GlyphLayout();
        layout.setText(font, mensaje);
        float textoWidth = layout.width;
        float textoHeight = layout.height;

        // Crear un fondo semi-transparente
        float padding = 15f;

        // Guardar color actual
        com.badlogic.gdx.graphics.Color originalBatchColor = batch.getColor();

        // Configurar color para el fondo
        batch.setColor(0, 0, 0, alpha * 0.6f);

        // Dibujar fondo
        if (corazonLleno != null) {
            batch.draw(corazonLleno,
                x - textoWidth/2 - padding, y - textoHeight/2 - padding,
                textoWidth + padding*2, textoHeight + padding*2);
        }

        // Restaurar color para el texto
        batch.setColor(originalBatchColor);

        // Dibujar texto
        font.draw(batch, mensaje, x - textoWidth/2, y + textoHeight/2);

        // Restaurar configuración de fuente
        font.setColor(1, 1, 1, 1);
        font.getData().setScale(1.0f);

        batch.end();
    }
    /**
     * Carga la música del nivel actual
     */
    private void cargarMusicaNivel() {
        Gdx.app.log("MainScreen", "Cargando música para el nivel...");

        try {
            // Primero, asegurarse de que no haya música previa
            if (musicaNivel != null) {
                musicaNivel.dispose();
                musicaNivel = null;
            }

            // Obtener el ID del nivel actual
            String nivelId = gameState.getNivelActual();
            if (nivelId == null || nivelId.isEmpty()) {
                nivelId = gameState.getNivelSeleccionadoId();
            }

            // Determinar qué música cargar según el nivel
            String rutaMusica = "Musica/nivel_generico.mp3"; // Ruta por defecto

            if (nivelId != null) {
                switch (nivelId) {
                    case "nivel_1":
                    case "nivel_villa":
                    case "La Villa":
                        rutaMusica = "Musica/Villa.mp3";
                        break;
                    case "nivel_2":
                    case "jb-32.tmx":
                    case "Las Columnas":
                        rutaMusica = "Musica/Columnas.mp3";
                        break;

                    case "nivel_3":
                    case "mylevel1":
                    case "Las Cavernas":
                        rutaMusica = "Musica/Caverna.mp3";
                        break;
                    case "nivel_4":
                    case "level25":
                    case "La Luna":
                        rutaMusica = "Musica/Luna.mp3";
                        break;
                    case "nivel_5":
                    case "magicland":
                    case "El Cartillo":
                        rutaMusica = "Musica/Castillo.mp3";
                        break;
                    default:
                        rutaMusica = "musica/nivel_generico.mp3";
                        break;
                }
            }

            Gdx.app.log("MainScreen", "Intentando cargar música: " + rutaMusica);

            // Verificar si el archivo existe
            if (Gdx.files.internal(rutaMusica).exists()) {
                musicaNivel = Gdx.audio.newMusic(Gdx.files.internal(rutaMusica));
                musicaNivel.setLooping(true);
                musicaNivel.setVolume(0.5f);
                musicaNivel.play();
                musicaCargada = true;
                Gdx.app.log("MainScreen", "✅ Música cargada: " + rutaMusica);
            } else {
                Gdx.app.error("MainScreen", "❌ Archivo de música no encontrado: " + rutaMusica);

                // Intentar con música genérica como respaldo
                try {
                    rutaMusica = "musica/nivel_generico.mp3";
                    if (Gdx.files.internal(rutaMusica).exists()) {
                        musicaNivel = Gdx.audio.newMusic(Gdx.files.internal(rutaMusica));
                        musicaNivel.setLooping(true);
                        musicaNivel.setVolume(0.5f);
                        musicaNivel.play();
                        musicaCargada = true;
                        Gdx.app.log("MainScreen", "✅ Música genérica cargada como respaldo");
                    }
                } catch (Exception e2) {
                    Gdx.app.error("MainScreen", "Error cargando música de respaldo", e2);
                    musicaNivel = null;
                    musicaCargada = false;
                }
            }

        } catch (Exception e) {
            Gdx.app.error("MainScreen", "Error cargando música del nivel", e);
            musicaNivel = null;
            musicaCargada = false;
        }
    }

    /**
     * Detiene la música del nivel
     */
    public void detenerMusica() {
        Gdx.app.log("MainScreen", "Deteniendo música del nivel...");

        if (musicaNivel != null) {
            musicaNivel.stop();
            musicaNivel.dispose();
            musicaNivel = null;
            musicaCargada = false;
            Gdx.app.log("MainScreen", "Música del nivel detenida correctamente");
        }
    }

    /**
     * Pausa la música del nivel
     */
    public void pausarMusica() {
        if (musicaNivel != null && musicaNivel.isPlaying()) {
            musicaNivel.pause();
            Gdx.app.log("MainScreen", "Música del nivel pausada");
        }
    }

    /**
     * Reanuda la música del nivel
     */
    public void reanudarMusica() {
        if (musicaNivel != null && !musicaNivel.isPlaying()) {
            musicaNivel.play();
            Gdx.app.log("MainScreen", "Música del nivel reanudada");
        }
    }
    /**
     * Dibuja el HUD (Heads-Up Display) en pantalla
     */
    private void dibujarHUD() {
        batch.begin();
        batch.setProjectionMatrix(camara.combined);

        // ====================== DIBUJAR VIDAS ======================
        int vidas = jugador.getVidasActuales();
        int vidasMax = jugador.getVidasMaximas();

        for (int i = 0; i < vidasMax; i++) {
            Texture corazon = (i < vidas) ? corazonLleno : corazonVacio;
            float x = camara.position.x - camara.viewportWidth / 2 + 10 + i * 45;
            float y = camara.position.y + camara.viewportHeight / 2 - 50;
            batch.draw(corazon, x, y, 40, 40);
        }

        // ====================== DIBUJAR TEXTO INFORMATIVO ======================
        float textoX = camara.position.x - camara.viewportWidth / 2 + 10;
        float textoY = camara.position.y + camara.viewportHeight / 2 - 80;

        font.setColor(0.9f, 0.9f, 0.9f, 1);

        // Enemigos eliminados
        font.draw(batch, "ENEMIES: " + enemigosEliminados + "/" + ENEMIGOS_OBJETIVO,
            textoX, textoY);

        // Tiempo
        font.draw(batch, "TIME: " + String.format("%.1f", tiempoTranscurrido) + "s",
            textoX, textoY - 25);

        // Enemigos vivos
        int enemigosVivos = 0;
        for (Enemigos enemigo : enemigos) {
            if (enemigo.estaVivo()) enemigosVivos++;
        }
        font.draw(batch, "ALIVE: " + enemigosVivos,
            textoX, textoY - 50);

        // Indicador de nivel (si está activo)
        if (nivelListoParaCompletar && indicadorNivel != null) {
            font.setColor(1, 1, 0.5f, 1);
            font.draw(batch, "PORTAL ACTIVE",
                textoX, textoY - 75);
            font.setColor(0.9f, 0.9f, 0.9f, 1);
        }

        // Instrucciones
        font.setColor(0.7f, 0.7f, 0.2f, 1);
        font.draw(batch, "ESC: PAUSE",
            textoX, textoY - 100);

        // Indicador de gamepad
        if (usandoGamepad) {
            font.setColor(0.2f, 0.7f, 0.2f, 1);
            font.draw(batch, "GAMEPAD ACTIVE",
                textoX, textoY - 125);
        }

        // **NUEVO: Información de progreso**
        if (gameState != null) {
            float progreso = gameState.getProgresoTotal();
            font.setColor(0.5f, 0.8f, 1f, 1);
            font.draw(batch, "PROGRESS: " + String.format("%.1f", progreso) + "%",
                textoX, textoY - 150);
        }

        font.setColor(0.9f, 0.9f, 0.9f, 1);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

        ViewportManager.logViewportInfo(viewport, "MainScreen");

        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("MainScreen", "Liberando recursos del juego...");

        if (jugador != null) {
            jugador.dispose();
        }


        if (enemigos != null) {
            for (Enemigos enemigo : enemigos) {
                enemigo.dispose();
            }
            enemigos.clear();
        }

        // **LIBERAR MÚSICA**
        if (musicaNivel != null) {
            musicaNivel.stop();
            musicaNivel.dispose();
            Gdx.app.log("MainScreen", "Música liberada");
        }

        if (indicadorNivel != null) {
            indicadorNivel.dispose();
        }

        if (fondo != null) fondo.dispose();
        if (corazonLleno != null) corazonLleno.dispose();
        if (corazonVacio != null) corazonVacio.dispose();
        if (font != null) font.dispose();

        if (stage != null) stage.dispose();
        if (mapaManager != null) mapaManager.dispose();
        if (batch != null) batch.dispose();

        Gdx.app.log("MainScreen", "Todos los recursos liberados correctamente");
    }

    // ====================== GETTERS PÚBLICOS ======================

    public Protagonista getJugador() {
        return jugador;
    }

    public boolean estaPausado() {
        return pausado;
    }

    public int getEnemigosEliminados() {
        return enemigosEliminados;
    }

    public float getTiempoTranscurrido() {
        return tiempoTranscurrido;
    }

    public boolean isNivelCompletado() {
        return nivelCompletado;
    }

    public IndicadorNivel getIndicadorNivel() {
        return indicadorNivel;
    }

    public boolean isNivelListoParaCompletar() {
        return nivelListoParaCompletar;
    }
}
