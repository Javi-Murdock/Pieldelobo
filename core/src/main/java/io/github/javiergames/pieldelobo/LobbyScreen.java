package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.HashMap;

import io.github.javiergames.pieldelobo.DataBase.DatabaseManager;
import io.github.javiergames.pieldelobo.Dialogos.SistemaDialogos;
import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.GestorJuego.Main;
import io.github.javiergames.pieldelobo.Graficos.GameUtils;
import io.github.javiergames.pieldelobo.Input.Procesador;
import io.github.javiergames.pieldelobo.Mapas.MapaManager;
import io.github.javiergames.pieldelobo.Personajes.NpcLobby;
import io.github.javiergames.pieldelobo.Personajes.PersonajeLobby;
import io.github.javiergames.pieldelobo.Puertas.IconoInteraccion;
import io.github.javiergames.pieldelobo.Puertas.PuertaTransicion;
import io.github.javiergames.pieldelobo.Puertas.StageOrdenado;
import io.github.javiergames.pieldelobo.Videos.VideoManager;
import io.github.javiergames.pieldelobo.Videos.ViewportManager;

/**
 * Pantalla del lobby/área de preparación del juego.
 * Sistema completo con NPCs, puertas cargadas desde Tiled, iconos de interacción
 * y sistema de diálogos con decisiones que desbloquean niveles.
 *
 * MODIFICADO: Ahora soporta completamente gamepad/mando:
 * - Botón A (XBOX: A, PS: X) para hablar con NPCs
 * - Botón X (XBOX: X, PS: Cuadrado) para interactuar con puertas
 * - Botón B (XBOX: B, PS: Círculo) para cancelar/volver
 * - Botón Start/Options para menú de pausa
 */
public class LobbyScreen extends PantallaInicio {
    // ====================== COMPONENTES PRINCIPALES ======================
    private StageOrdenado stage;
    private PersonajeLobby jugador;
    private Array<NpcLobby> npcs;
    private Array<PuertaTransicion> puertas;
    private Array<IconoInteraccion> iconosPuertas;
    private Procesador procesador;
    private MapaManager mapaManager;
    private OrthographicCamera camara;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private GlyphLayout layout;
    //Musica
    private Music musica;

    // ====================== SISTEMA DE DIÁLOGOS ======================
    private SistemaDialogos.VentanaDialogo ventanaDialogo;
    private boolean enDialogo = false;
    private NpcLobby npcDialogoActual = null;

    // ====================== CONTROL DE PUERTAS ======================
    private boolean mostrarDialogoPuerta = false;
    private PuertaTransicion puertaSeleccionada = null;

    // ====================== ESTADOS DEL JUEGO ======================
    private boolean pausado = false;
    private Procesador procesadorGuardado;
    private float tiempoIndicador = 0f;

    // ====================== NOTIFICACIONES ======================
    private String notificacionTexto = "";
    private float notificacionTiempo = 0f;
    private static final float NOTIFICACION_DURACION = 3f;
    private static final float NOTIFICACION_ESPECIAL_DURACION = 5f;

    // ====================== CONFIGURACIÓN DE GAMEPAD ======================
    private static final int GAMEPAD_BOTON_A = 0;      // Botón A (XBOX: A, PS: X)
    private static final int GAMEPAD_BOTON_B = 1;      // Botón B (XBOX: B, PS: Círculo)
    private static final int GAMEPAD_BOTON_X = 2;      // Botón X (XBOX: X, PS: Cuadrado)
    private static final int GAMEPAD_BOTON_Y = 3;      // Botón Y (XBOX: Y, PS: Triángulo)
    private static final int GAMEPAD_BOTON_START = 7;  // Botón Start/Options
    private static final int GAMEPAD_BOTON_SELECT = 6; // Botón Select
    private static final float ZONA_MUERTA_GAMEPAD = 0.5f; // Zona muerta para D-Pad

    // ====================== COLORES PARA NOTIFICACIONES ======================
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_FONDO =
        new com.badlogic.gdx.graphics.Color(0.1f, 0.1f, 0.2f, 0.8f);
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_BORDE =
        new com.badlogic.gdx.graphics.Color(0.3f, 0.3f, 0.6f, 1f);
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_TEXTO =
        com.badlogic.gdx.graphics.Color.WHITE;

    // Colores especiales para notificaciones de desbloqueo
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_ESPECIAL_FONDO =
        new com.badlogic.gdx.graphics.Color(0.1f, 0.3f, 0.1f, 0.9f);
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_ESPECIAL_BORDE =
        new com.badlogic.gdx.graphics.Color(0.5f, 1f, 0.5f, 1f);
    private final com.badlogic.gdx.graphics.Color COLOR_NOTIFICACION_ESPECIAL_TEXTO =
        new com.badlogic.gdx.graphics.Color(1f, 1f, 0.8f, 1f);

    // ====================== CONSTRUCTOR ======================
    /**
     * Constructor principal del LobbyScreen.
     */
    public LobbyScreen(Main game) {
        super(game);
        this.batch = new SpriteBatch();
        this.npcs = new Array<>();
        this.puertas = new Array<>();
        this.iconosPuertas = new Array<>();
        this.layout = new GlyphLayout();

        // Cargar fuente básica
        try {
            this.font = new BitmapFont();
            this.font.getData().setScale(1.2f);
            Gdx.app.log("LobbyScreen", "Fuente cargada correctamente");
        } catch (Exception e) {
            Gdx.app.error("LobbyScreen", "No se pudo cargar fuente", e);
            try {
                this.font = new BitmapFont();
            } catch (Exception e2) {
                Gdx.app.error("LobbyScreen", "Error crítico al crear fuente", e2);
            }
        }
    }

    // ====================== MÉTODOS DE CICLO DE VIDA ======================
    @Override
    public void show() {
        Gdx.app.log("LobbyScreen", "=== SHOW LOBBYSCREEN ===");

        // Cargar música si no está cargada
        if (musica == null) {
            cargarMusica();
        }
        // Verificar si viene de un video y reanudar música
        reanudarMusicaDespuesVideo();

        // NUEVO: Verificar si acabamos de volver de un video
        boolean viniendoDeVideo = true; // Asumimos que viene de video por defecto

        // Si ya está inicializado (vuelta desde pausa o video), solo reanudar
        if (jugador != null && mapaManager != null && stage != null) {
            Gdx.app.log("LobbyScreen", "Reanudando desde estado guardado...");

            // Verificar si hay que actualizar puertas después del video
            actualizarPuertasDespuesDeVideo();

            if (procesadorGuardado != null) {
                procesador = procesadorGuardado;
                Gdx.input.setInputProcessor(procesador);
                Gdx.app.log("LobbyScreen", "Input processor restaurado");
            }

            pausado = false;
            enDialogo = false;
            mostrarDialogoPuerta = false;

            // Reanudar NPCs si estaban pausados
            for (NpcLobby npc : npcs) {
                npc.setPausado(false);
            }

            // Re-aplicar viewport
            if (viewport != null) {
                viewport.apply();
            }

            Gdx.app.log("LobbyScreen", "Lobby reanudado correctamente");
            return;
        }

        // Primera vez: inicializar completamente
        try {
            inicializarLobby();
            Gdx.app.log("LobbyScreen", "Lobby listo - NPCs: " + npcs.size + ", Puertas: " + puertas.size);
            Gdx.app.log("LobbyScreen", "Inicialización completada exitosamente");
        } catch (Exception e) {
            Gdx.app.error("LobbyScreen", "Error crítico en inicialización", e);
            inicializarRespaldo();
        }
    }

    /**
     * Inicializa todos los componentes del lobby.
     */
    private void inicializarLobby() {
        Gdx.app.log("LOBBY", "=== INICIANDO INICIALIZACIÓN COMPLETA ===");

        long inicio = System.currentTimeMillis();

        // ========== 1. CONFIGURACIÓN DE CÁMARA Y VIEWPORT ==========
        camara = new OrthographicCamera();

        try {
            viewport = ViewportManager.createViewport(ViewportManager.ViewportType.EXTEND, camara);
            Gdx.app.log("LOBBY", "Viewport creado: " +
                ViewportManager.VIRTUAL_WIDTH + "x" + ViewportManager.VIRTUAL_HEIGHT);
        } catch (Exception e) {
            Gdx.app.error("LOBBY", "Error creando viewport, usando por defecto", e);
            viewport = new com.badlogic.gdx.utils.viewport.FitViewport(800, 600, camara);
        }

        // ========== 2. CREAR STAGE ORDENADO ==========
        stage = new StageOrdenado(viewport);
        Gdx.app.log("LOBBY", "StageOrdenado creado");

        // ========== 3. CARGAR MAPA DEL LOBBY ==========
        try {
            mapaManager = new MapaManager("Tiled/nivel_laboratorio.tmx");
            if (mapaManager.estaCargado()) {
                Gdx.app.log("LOBBY", "Mapa cargado: " + mapaManager.getAnchoMapa() + "x" + mapaManager.getAltoMapa());
            } else {
                Gdx.app.log("LOBBY", "Mapa no se pudo cargar, usando modo respaldo");
            }
        } catch (Exception e) {

            Gdx.app.error("LOBBY", "Error crítico cargando mapa", e);
            mapaManager = new MapaManager("");
        }

        // ========== 4. CREAR JUGADOR ==========
        try {
            jugador = new PersonajeLobby();
            jugador.setMapaManager(mapaManager);

            Vector2 spawnJugador = mapaManager.obtenerPosicionSpawnJugador();
            jugador.setPosition(spawnJugador.x, spawnJugador.y);

            Gdx.app.log("LOBBY", "Jugador creado en: " + spawnJugador.x + ", " + spawnJugador.y);
            Gdx.app.log("LOBBY", "Tamaño jugador: " + jugador.getWidth() + "x" + jugador.getHeight());
        } catch (Exception e) {
            Gdx.app.error("LOBBY", "Error creando jugador", e);
            jugador = new PersonajeLobby();
            jugador.setPosition(100, 100);
        }

        // ========== 5. CREAR NPCS DESDE MAPA ==========
        crearNpcs();
        Gdx.app.log("LOBBY", "NPCs creados: " + npcs.size);

        // ========== 6. CREAR PUERTAS E ICONOS DESDE MAPA ==========
        crearPuertasYIconosDesdeMapa();
        Gdx.app.log("LOBBY", "Puertas creadas: " + puertas.size);
        Gdx.app.log("LOBBY", "Iconos creados: " + iconosPuertas.size);

        // ========== 7. AÑADIR ACTORES AL STAGE ==========
        for (PuertaTransicion puerta : puertas) {
            stage.addActor(puerta);
        }

        for (NpcLobby npc : npcs) {
            stage.addActor(npc);
        }

        for (IconoInteraccion icono : iconosPuertas) {
            stage.addActor(icono);
        }

        stage.addActor(jugador);

        // ========== 8. CONFIGURAR SISTEMA DE INPUT ==========
        try {
            procesador = new Procesador(jugador);
            Gdx.input.setInputProcessor(procesador);
            Gdx.app.log("LOBBY", "Input processor configurado");
        } catch (Exception e) {
            Gdx.app.error("LOBBY", "Error configurando input", e);
        }

        // ========== 9. INICIALIZAR SISTEMA DE DIÁLOGOS ==========
        try {
            ventanaDialogo = new SistemaDialogos.VentanaDialogo();
            Gdx.app.log("LOBBY", "Sistema de diálogos inicializado");
        } catch (Exception e) {
            Gdx.app.error("LOBBY", "Error inicializando diálogos", e);
        }

        // ========== 10. CONFIGURAR VISTA DEL MAPA ==========
        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
        }

        // ========== 11. APLICAR VIEWPORT INICIAL ==========
        viewport.apply();

        // ========== 12. INICIALIZAR ESTADOS ==========
        pausado = false;
        enDialogo = false;
        mostrarDialogoPuerta = false;
        notificacionTexto = "";
        notificacionTiempo = 0f;

        // ========== 13. LOG FINAL ==========
        long fin = System.currentTimeMillis();
        Gdx.app.log("LOBBY", "=== INICIALIZACIÓN COMPLETADA EN " + (fin - inicio) + "ms ===");
        Gdx.app.log("LOBBY", "Resumen: " + npcs.size + " NPCs, " + puertas.size + " puertas, " +
            iconosPuertas.size + " iconos");
    }

    /**
     * Inicialización de respaldo en caso de error crítico.
     */
    private void inicializarRespaldo() {
        Gdx.app.log("LOBBY", "=== MODO RESPAIDO ACTIVADO ===");

        // Configuración mínima
        camara = new OrthographicCamera();
        camara.setToOrtho(false, 800, 600);

        viewport = new com.badlogic.gdx.utils.viewport.ScreenViewport(camara);
        stage = new StageOrdenado(viewport);

        // Jugador básico
        jugador = new PersonajeLobby();
        jugador.setPosition(400, 300);
        stage.addActor(jugador);

        // Input básico
        procesador = new Procesador(jugador);
        Gdx.input.setInputProcessor(procesador);

        // Diálogos básicos
        ventanaDialogo = new SistemaDialogos.VentanaDialogo();

        pausado = false;
        enDialogo = false;

        Gdx.app.log("LOBBY", "Modo respaldo activado - funcionalidad limitada");
    }

    // ====================== MÉTODOS DE CREACIÓN DE ENTIDADES ======================

    /**
     * Crea NPCs desde la información del mapa Tiled.
     */
    private void crearNpcs() {
        npcs.clear();

        Array<MapaManager.NpcSpawnInfo> spawnsInfo = mapaManager.obtenerInfoSpawnNpcs();

        if (spawnsInfo.size == 0) {
            Gdx.app.log("LOBBY", "No se encontraron spawns de NPCs en el mapa, creando por defecto");
            crearNpcsPorDefecto();
        } else {
            Gdx.app.log("LOBBY", "Creando " + spawnsInfo.size + " NPCs desde mapa...");

            for (MapaManager.NpcSpawnInfo info : spawnsInfo) {
                try {
                    NpcLobby npc = new NpcLobby(info.tipo, info.posicion.x, info.posicion.y);
                    npc.setMapaManager(mapaManager);
                    npcs.add(npc);

                    Gdx.app.log("LOBBY", "  NPC creado: tipo='" + info.tipo +
                        "' diálogo='" + npc.getIdDialogo() +
                        "' pos=[" + info.posicion.x + "," + info.posicion.y + "]");

                } catch (Exception e) {
                    Gdx.app.error("LOBBY", "Error creando NPC: " + info.tipo, e);
                }
            }
        }

        Gdx.app.log("LOBBY", "Total NPCs creados: " + npcs.size);
    }

    /**
     * Crea NPCs por defecto cuando no hay información en el mapa.
     */
    private void crearNpcsPorDefecto() {
        String[] tiposNpc = {"profesor", "doctor", "doctora", "medico", "ciber", "senor"};
        Vector2[] posiciones = {
            new Vector2(300, 400),
            new Vector2(500, 300),
            new Vector2(200, 350),
            new Vector2(400, 200),
            new Vector2(600, 400),
            new Vector2(100, 250)
        };

        for (int i = 0; i < Math.min(tiposNpc.length, posiciones.length); i++) {
            try {
                NpcLobby npc = new NpcLobby(tiposNpc[i], posiciones[i].x, posiciones[i].y);
                npc.setMapaManager(mapaManager);
                npcs.add(npc);

                Gdx.app.log("LOBBY", "  NPC por defecto: " + tiposNpc[i] +
                    " diálogo: " + npc.getIdDialogo());
            } catch (Exception e) {
                Gdx.app.error("LOBBY", "Error creando NPC por defecto: " + tiposNpc[i], e);
            }
        }
    }

    /**
     * Crea puertas invisibles y sus iconos de interacción desde el mapa Tiled.
     */
    private void crearPuertasYIconosDesdeMapa() {
        puertas.clear();
        iconosPuertas.clear();

        GameState gameState = GameState.getInstance();

        // Obtener información de puertas desde el mapa
        Array<MapaManager.PuertaInfo> puertasInfo = mapaManager.obtenerInfoPuertas();

        if (puertasInfo.size == 0) {
            Gdx.app.log("LOBBY", "No se encontraron puertas en el mapa, creando por defecto");
            crearPuertasYIconosPorDefecto();
            return;
        }

        Gdx.app.log("LOBBY", "Creando " + puertasInfo.size + " puertas desde mapa...");

        for (MapaManager.PuertaInfo info : puertasInfo) {
            try {
                // NUEVO: Usar método robusto para verificar desbloqueo
                boolean desbloqueada = verificarNivelDesbloqueadoRobusto(info.idNivel, gameState);
                boolean activa = desbloqueada || info.idNivel.equals("nivel_1"); // Nivel 1 siempre activo

                // Crear puerta invisible (solo para detección de colisiones)
                PuertaTransicion puerta = new PuertaTransicion(
                    info.posicion.x, info.posicion.y,
                    info.tamaño.x, info.tamaño.y,
                    info.idNivel, info.nombre,
                    activa, !desbloqueada, // Bloqueada = no desbloqueada
                    info.mapaDestino
                );

                puerta.setVisible(false); // IMPORTANTE: Hacer invisible
                puertas.add(puerta);

                // Crear icono de interacción para esta puerta
                IconoInteraccion icono = new IconoInteraccion(puerta);
                iconosPuertas.add(icono);

                Gdx.app.log("LOBBY", "  Puerta creada: " + info.nombre +
                    " -> " + info.idNivel +
                    " estado: " + (desbloqueada ? "DESBLOQUEADA" : "BLOQUEADA") +
                    " pos=[" + info.posicion.x + "," + info.posicion.y + "]");

            } catch (Exception e) {
                Gdx.app.error("LOBBY", "Error creando puerta: " + info.nombre, e);

                // Crear puerta por defecto (siempre bloqueada) en caso de error
                PuertaTransicion puertaFallback = new PuertaTransicion(
                    info.posicion.x, info.posicion.y,
                    info.tamaño.x, info.tamaño.y,
                    info.idNivel, info.nombre,
                    false, true, // Siempre bloqueada si hay error
                    info.mapaDestino
                );
                puertaFallback.setVisible(false);
                puertas.add(puertaFallback);
                iconosPuertas.add(new IconoInteraccion(puertaFallback));

                Gdx.app.log("LOBBY", "  Puerta fallback creada (siempre bloqueada): " + info.nombre);
            }
        }
    }

    /**
     * Método robusto para verificar niveles desbloqueados con manejo de errores
     */
    private boolean verificarNivelDesbloqueadoRobusto(String nivelId, GameState gameState) {
        try {
            return gameState.isNivelDesbloqueado(nivelId);
        } catch (ClassCastException e) {
            Gdx.app.error("LOBBY", "Error de casteo al verificar nivel: " + nivelId, e);

            // Intentar recuperar de otra manera
            DatabaseManager db = DatabaseManager.getInstance();
            DatabaseManager.GameData gameData = db.getGameData();

            Object nivelObj = gameData.niveles.get(nivelId);
            if (nivelObj == null) {
                Gdx.app.log("LOBBY", "Nivel no encontrado en datos: " + nivelId);
                return false;
            }

            // Si es HashMap, intentar extraer información
            if (nivelObj instanceof HashMap) {
                @SuppressWarnings("unchecked")
                HashMap<String, Object> nivelMap = (HashMap<String, Object>) nivelObj;

                String estadoStr = (String) nivelMap.getOrDefault("estado", "BLOQUEADO");
                boolean desbloqueado = estadoStr.equals("DESBLOQUEADO") ||
                    estadoStr.equals("EN_PROGRESO") ||
                    estadoStr.equals("COMPLETADO");

                Gdx.app.log("LOBBY", "Nivel " + nivelId + " (HashMap) estado: " + estadoStr +
                    " -> desbloqueado: " + desbloqueado);

                // Intentar reparar el dato
                try {
                    DatabaseManager.LevelState nivelReparado = new DatabaseManager.LevelState();
                    nivelReparado.nivelId = nivelId;
                    nivelReparado.nombre = (String) nivelMap.getOrDefault("nombre", "Nivel " + nivelId.replace("nivel_", ""));

                    try {
                        nivelReparado.estado = DatabaseManager.LevelState.EstadoNivel.valueOf(estadoStr);
                    } catch (Exception ex) {
                        nivelReparado.estado = DatabaseManager.LevelState.EstadoNivel.BLOQUEADO;
                    }

                    // Actualizar en el mapa
                    gameData.niveles.put(nivelId, nivelReparado);
                    Gdx.app.log("LOBBY", "Nivel reparado y actualizado: " + nivelId);

                } catch (Exception ex2) {
                    Gdx.app.error("LOBBY", "No se pudo reparar nivel: " + nivelId, ex2);
                }

                return desbloqueado;
            }

            return false;
        } catch (Exception e) {
            Gdx.app.error("LOBBY", "Error inesperado verificando nivel: " + nivelId, e);
            return false;
        }
    }

    /**
     * Crea puertas e iconos por defecto cuando no hay información en el mapa.
     */
    private void crearPuertasYIconosPorDefecto() {
        GameState gameState = GameState.getInstance();

        // Array de niveles por defecto
        String[] niveles = {"nivel_1", "nivel_2", "nivel_3", "nivel_4", "nivel_5",
            "nivel_6", "nivel_7", "nivel_8", "nivel_9", "nivel_10"};

        String[] nombres = {"Nivel 1: La Villa", "Nivel 2: El Bosque", "Nivel 3: Las Cavernas",
            "Nivel 4", "Nivel 5", "Nivel 6", "Nivel 7", "Nivel 8",
            "Nivel 9", "Nivel 10"};

        float[] posicionesX = {200, 400, 600, 200, 400, 600, 200, 400, 600, 800};
        float[] posicionesY = {150, 150, 150, 300, 300, 300, 450, 450, 450, 150};

        for (int i = 0; i < niveles.length; i++) {
            try {
                boolean desbloqueado = verificarNivelDesbloqueadoRobusto(niveles[i], gameState);
                boolean activa = desbloqueado || niveles[i].equals("nivel_1");

                PuertaTransicion puerta = new PuertaTransicion(
                    posicionesX[i], posicionesY[i], 64, 96,
                    niveles[i], nombres[i],
                    activa, !desbloqueado,
                    "Tiled/nivel_villa.tmx"
                );
                puerta.setVisible(false);
                puertas.add(puerta);
                iconosPuertas.add(new IconoInteraccion(puerta));

            } catch (Exception e) {
                Gdx.app.error("LOBBY", "Error creando puerta por defecto: " + niveles[i], e);
            }
        }

        Gdx.app.log("LOBBY", "Puertas por defecto creadas: " + puertas.size + " puertas");
    }

    // ====================== MÉTODO PRINCIPAL DE RENDER ======================
    @Override
    public void render(float delta) {
        // Actualizar tiempo para animaciones
        tiempoIndicador += delta;

        // Actualizar tiempo de notificación
        if (notificacionTiempo > 0) {
            notificacionTiempo -= delta;
            if (notificacionTiempo <= 0) {
                notificacionTexto = "";
            }
        }

        // ========== MODO DIÁLOGO ACTIVO ==========
        if (enDialogo || mostrarDialogoPuerta) {
            renderModoDialogo(delta);
            return;
        }

        // ========== MODO JUEGO NORMAL ==========
        renderModoNormal(delta);
    }

    /**
     * Renderiza cuando hay diálogo activo (NPC o puerta).
     */
    private void renderModoDialogo(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Actualizar y renderizar cámara
        camara.update();

        // Renderizar fondo (mapa)
        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
            mapaManager.renderizar();
        }

        // Renderizar stage (actores estáticos)
        stage.draw();

        // Dibujar indicadores de NPCs (si no es diálogo de puerta)
        if (!mostrarDialogoPuerta) {
            dibujarIndicadoresNPCs();
        }

        // Manejar diálogos de NPC
        if (enDialogo && ventanaDialogo.isActivo()) {
            ventanaDialogo.actualizar(delta);
            ventanaDialogo.render();
            manejarControlesDialogoNPC();
        }

        // Manejar diálogo de puerta
        if (mostrarDialogoPuerta) {
            manejarDialogoPuerta();
        }

        // Dibujar notificación si hay
        if (!notificacionTexto.isEmpty()) {
            dibujarNotificacion();
        }
    }

    /**
     * Renderiza el modo normal de juego (sin diálogos).
     */
    private void renderModoNormal(float delta) {
        // Limpiar pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ========== SEGUIMIENTO DE CÁMARA ==========
        if (jugador != null) {
            camara.position.set(
                jugador.getX() + jugador.getWidth() / 2,
                jugador.getY() + jugador.getHeight() / 2,
                0
            );
        }
        camara.update();

        // ========== RENDERIZAR MAPA ==========
        if (mapaManager.estaCargado()) {
            mapaManager.setView(camara);
            mapaManager.renderizar();
        }

        // ========== ACTUALIZAR VISIBILIDAD DE ICONOS ==========
        actualizarIconosPuertas();

        // ========== DETECCIÓN DE INTERACCIONES (CON GAMEPAD) ==========
        detectarInteraccionesConGamepad();

        // ========== ACTUALIZAR LÓGICA DEL JUEGO ==========
        procesador.actualizar(delta);
        stage.act(delta);
        verificarColisiones();
        stage.draw();

        // ========== DIBUJAR INDICADORES VISUALES ==========
        dibujarIndicadoresNPCs();

        // ========== DIBUJAR NOTIFICACIÓN ==========
        if (!notificacionTexto.isEmpty()) {
            dibujarNotificacion();
        }
    }

    /**
     * Detecta interacciones del jugador con NPCs y puertas usando gamepad.
     * SOPORTA COMPLETO PARA GAMEPAD:
     * - Botón A para hablar con NPCs
     * - Botón X para interactuar con puertas
     * - Botón Start para menú de pausa
     */
    private void detectarInteraccionesConGamepad() {
        boolean gamepadConectado = procesador != null && procesador.hayGamepadConectado();

        // INTERACCIÓN CON NPCs (Tecla Z o Botón A del gamepad)
        if ((Gdx.input.isKeyJustPressed(Keys.Z) ||
            (gamepadConectado && procesador.isBotonAPresionado())) && !enDialogo) {
            // IMPORTANTE: Para evitar activación múltiple, verificamos que sea un "just pressed"
            // Como el gamepad mantiene el estado, necesitamos un sistema de debounce
            NpcLobby npcMasCercano = obtenerNpcMasCercano();
            if (npcMasCercano != null) {
                iniciarDialogo(npcMasCercano);
                // Reseteamos el estado para evitar activación continua
                if (gamepadConectado) {
                    // El reset se hace en el procesador cuando se suelta el botón
                }
            }
        }

        // INTERACCIÓN CON PUERTAS (Tecla E o Botón X del gamepad)
        if ((Gdx.input.isKeyJustPressed(Keys.E) ||
            (gamepadConectado && procesador.isBotonXPresionado())) && !mostrarDialogoPuerta) {
            for (IconoInteraccion icono : iconosPuertas) {
                if (icono.colisionaConJugador(jugador) && icono.isMostrando()) {
                    mostrarDialogoPuerta(icono.getPuerta());
                    break;
                }
            }
        }

        // MENÚ DE PAUSA (ESC o Botón Start del gamepad)
        if ((Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
            (gamepadConectado && Gdx.input.isKeyJustPressed(Keys.BUTTON_START))) &&
            !enDialogo && !mostrarDialogoPuerta) {
            mostrarPausa();
        }
    }

    /**
     * Encuentra el NPC más cercano con el que se puede interactuar.
     */
    private NpcLobby obtenerNpcMasCercano() {
        NpcLobby npcMasCercano = null;
        float distanciaMinima = Float.MAX_VALUE;

        for (NpcLobby npc : npcs) {
            if (npc.tieneDialogo() && npc.puedeInteractuar(jugador)) {
                float dx = npc.getX() - jugador.getX();
                float dy = npc.getY() - jugador.getY();
                float distancia = (float) Math.sqrt(dx * dx + dy * dy);

                if (distancia < distanciaMinima) {
                    distanciaMinima = distancia;
                    npcMasCercano = npc;
                }
            }
        }

        return npcMasCercano;
    }

    // ====================== SISTEMA DE DIÁLOGOS CON NPCs ======================

    /**
     * Inicia un diálogo con un NPC.
     */
    private void iniciarDialogo(NpcLobby npc) {
        if (npc == null || !npc.tieneDialogo()) {
            Gdx.app.log("LobbyScreen", "Intento de diálogo con NPC sin diálogo");
            return;
        }

        enDialogo = true;
        npcDialogoActual = npc;
        ventanaDialogo.mostrarDialogo(npc.getIdDialogo());

        // Pausar todos los NPCs durante el diálogo
        for (NpcLobby n : npcs) {
            n.setPausado(true);
        }

        // Ocultar todos los iconos durante el diálogo
        for (IconoInteraccion icono : iconosPuertas) {
            icono.setMostrar(false);
        }

        Gdx.app.log("LobbyScreen",
            "Diálogo iniciado con NPC: " + npc.getIdDialogo() +
                " - Posición NPC: [" + npc.getX() + "," + npc.getY() + "]");
    }

    /**
     * Maneja los controles durante un diálogo con NPC.
     * SOPORTA COMPLETO PARA GAMEPAD:
     * - D-Pad o joystick izquierdo para navegar
     * - Botón A para confirmar
     * - Botón B para cancelar
     */
    private void manejarControlesDialogoNPC() {
        boolean gamepadConectado = procesador != null && procesador.hayGamepadConectado();

        // Navegación con flechas, D-Pad o joystick del gamepad
        if (Gdx.input.isKeyJustPressed(Keys.UP) ||
            (gamepadConectado && Gdx.input.isKeyJustPressed(Keys.DPAD_UP)) ||
            (gamepadConectado && procesador.getEjeYGamepad() < -ZONA_MUERTA_GAMEPAD)) {
            ventanaDialogo.navegarArriba();
        }

        if (Gdx.input.isKeyJustPressed(Keys.DOWN) ||
            (gamepadConectado && Gdx.input.isKeyJustPressed(Keys.DPAD_DOWN)) ||
            (gamepadConectado && procesador.getEjeYGamepad() > ZONA_MUERTA_GAMEPAD)) {
            ventanaDialogo.navegarAbajo();
        }

        // Confirmar selección (Z, Enter o Botón A del gamepad)
        if (Gdx.input.isKeyJustPressed(Keys.Z) ||
            Gdx.input.isKeyJustPressed(Keys.ENTER) ||
            (gamepadConectado && procesador.isBotonAPresionado())) {
            boolean continua = ventanaDialogo.confirmar();
            if (!continua) {
                terminarDialogoNPC();
            }
        }

        // Cancelar diálogo (X, Escape o Botón B del gamepad)
        if (Gdx.input.isKeyJustPressed(Keys.X) ||
            Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
            (gamepadConectado && procesador.isBotonBPresionado())) {
            terminarDialogoNPC();
        }
    }

    /**
     * Termina el diálogo con NPC y actualiza el estado del juego.
     */
    private void terminarDialogoNPC() {
        // Guardar referencia al NPC antes de cerrar
        String npcId = npcDialogoActual != null ? npcDialogoActual.getIdDialogo() : "";

        Gdx.app.log("LobbyScreen", "=== TERMINANDO DIÁLOGO CON NPC ===");
        Gdx.app.log("LobbyScreen", "NPC ID: " + npcId);

        // 1. Cerrar ventana de diálogo primero
        enDialogo = false;
        if (ventanaDialogo != null) {
            ventanaDialogo.cerrar();
        }

        // 2. Reanudar todos los NPCs
        for (NpcLobby n : npcs) {
            n.setPausado(false);
        }

        // 3. VERIFICAR VIDEOS PRIMERO (antes de cualquier notificación)
        Gdx.app.log("LobbyScreen", "Paso 1: Verificando videos pendientes...");
        manejarVideosDespuesDialogo();

        // 4. Verificar si se está reproduciendo un video (si es así, salir del método)
        if (game.getScreen() instanceof VideoScreen) {
            Gdx.app.log("LobbyScreen", "✅ Video iniciado, saliendo del método");
            npcDialogoActual = null;
            return;
        }

        // 5. Si NO hay video, mostrar otras consecuencias
        Gdx.app.log("LobbyScreen", "Paso 2: No hay video, mostrando otras consecuencias...");
        mostrarConsecuenciasPendientes();

        // 6. Actualizar estado de puertas
        actualizarEstadoPuertasDespuesDialogo();

        // 7. Verificación específica para profesor Leiva
        if (npcId != null && npcId.contains("profesor_leiva")) {
            verificarNivelDesbloqueado("nivel_1");
        }

        // 8. Limpiar referencia al NPC
        npcDialogoActual = null;

        Gdx.app.log("LobbyScreen", "✅ Diálogo terminado completamente");
    }

    /**
     * Método para reanudar la música después de volver de un video.
     */
    public void reanudarMusicaDespuesVideo() {
        if (musica == null) {
            cargarMusica(); // Cargar si no existe
        } else if (!musica.isPlaying()) {
            musica.play();
            Gdx.app.log("LobbyScreen", "Música reanudada después del video");
        }
    }

    /**
     * Muestra las consecuencias pendientes después de terminar un diálogo.
     */
    private void mostrarConsecuenciasPendientes() {
        io.github.javiergames.pieldelobo.Dialogos.DialogoManager dialogoManager =
            io.github.javiergames.pieldelobo.Dialogos.DialogoManager.getInstance();

        if (dialogoManager.tieneConsecuenciasPendientes()) {
            java.util.Map<String, String> consecuencias = dialogoManager.obtenerYLimpiarConsecuenciasPendientes();

            for (java.util.Map.Entry<String, String> entry : consecuencias.entrySet()) {
                String tipo = entry.getKey();
                String mensaje = entry.getValue();

                if ("desbloquear_nivel".equals(tipo)) {
                    mostrarNotificacionEspecial(mensaje);
                    Gdx.app.log("LobbyScreen", "Mostrando consecuencia después de diálogo: " + mensaje);
                } else if ("consejo".equals(tipo)) {
                    mostrarNotificacion("💡 " + mensaje);
                } else {
                    mostrarNotificacion(mensaje);
                }
            }
        }
    }

    /**
     * Muestra una notificación especial para desbloqueos importantes.
     */
    private void mostrarNotificacionEspecial(String mensaje) {
        notificacionTexto = "✨ ¡DESBLOQUEO! ✨\n" + mensaje;
        notificacionTiempo = NOTIFICACION_ESPECIAL_DURACION;

        Gdx.app.log("NOTIFICACION_ESPECIAL", mensaje);

        // Reproducir sonido especial
        try {
            com.badlogic.gdx.audio.Sound sound =
                Gdx.audio.newSound(Gdx.files.internal("sounds/unlock.wav"));
            if (sound != null) {
                sound.play(0.7f);
            }
        } catch (Exception e) {
            // Silenciar si no hay sonido disponible
        }
    }

    /**
     * Actualiza el estado de las puertas después de un diálogo.
     */
    private void actualizarEstadoPuertasDespuesDialogo() {
        GameState gameState = GameState.getInstance();
        boolean huboCambios = false;

        for (PuertaTransicion puerta : puertas) {
            String nivelId = puerta.getIdNivel();
            boolean desbloqueado = gameState.isNivelDesbloqueado(nivelId);
            boolean actualmenteBloqueada = puerta.isBloqueada();

            if (desbloqueado && actualmenteBloqueada) {
                puerta.setBloqueada(false);
                puerta.setActiva(true);
                huboCambios = true;

                String mensajePuerta = "¡" + puerta.getNombreMostrar() + " ahora está disponible!";
                mostrarNotificacion(mensajePuerta);

                Gdx.app.log("LobbyScreen",
                    "PUERTA ACTUALIZADA: " + puerta.getNombreMostrar() +
                        " ahora está DESBLOQUEADA");

                aplicarEfectoVisualPuerta(puerta);
            }
        }

        if (huboCambios) {
            Gdx.app.log("LobbyScreen", "Cambios detectados en puertas después del diálogo");
        }
    }

    /**
     * Aplica efectos visuales a una puerta desbloqueada.
     */
    private void aplicarEfectoVisualPuerta(PuertaTransicion puerta) {
        Gdx.app.log("LobbyScreen",
            "Aplicando efecto visual al icono de: " + puerta.getNombreMostrar());

        try {
            // Reproducir sonido de desbloqueo
            com.badlogic.gdx.audio.Sound sound =
                Gdx.audio.newSound(Gdx.files.internal("sounds/door_unlock.wav"));
            if (sound != null) {
                sound.play(0.7f);
            }
        } catch (Exception e) {
            // Ignorar si no hay sonido
        }
    }

    /**
     * Verifica específicamente si un nivel fue desbloqueado.
     */
    private void verificarNivelDesbloqueado(String nivelId) {
        GameState gameState = GameState.getInstance();

        if (gameState.isNivelDesbloqueado(nivelId)) {
            for (PuertaTransicion puerta : puertas) {
                if (puerta.getIdNivel().equals(nivelId)) {
                    Gdx.app.log("LobbyScreen",
                        "Nivel verificado como desbloqueado: " + puerta.getNombreMostrar());
                    break;
                }
            }
        }
    }

    // ====================== SISTEMA DE INTERACCIÓN CON PUERTAS ======================

    /**
     * Muestra el diálogo de interacción con una puerta.
     */
    private void mostrarDialogoPuerta(PuertaTransicion puerta) {
        puertaSeleccionada = puerta;
        mostrarDialogoPuerta = true;

        // Pausar NPCs
        for (NpcLobby npc : npcs) {
            npc.setPausado(true);
        }

        // Ocultar todos los iconos durante el diálogo
        for (IconoInteraccion icono : iconosPuertas) {
            icono.setMostrar(false);
        }

        Gdx.app.log("LobbyScreen",
            "Mostrando diálogo puerta: " + puerta.getNombreMostrar() +
                " estado: " + (puerta.isBloqueada() ? "BLOQUEADA" :
                puerta.isActiva() ? "DISPONIBLE" : "INACTIVA"));
    }

    /**
     * Maneja el diálogo y controles de una puerta.
     * SOPORTA COMPLETO PARA GAMEPAD:
     * - Botón A para entrar
     * - Botón B para cancelar
     */
    private void manejarDialogoPuerta() {
        if (puertaSeleccionada == null) {
            cancelarDialogoPuerta();
            return;
        }

        boolean gamepadConectado = procesador != null && procesador.hayGamepadConectado();

        // Crear cámara temporal para pantalla completa
        com.badlogic.gdx.graphics.OrthographicCamera screenCamera =
            new com.badlogic.gdx.graphics.OrthographicCamera();
        screenCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenCamera.update();

        batch.begin();
        batch.setProjectionMatrix(screenCamera.combined);

        // FONDO OSCURO SEMI-TRANSPARENTE
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(GameUtils.getWhitePixel(),
            0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight());

        // DEFINIR DIMENSIONES DEL CUADRO DE DIÁLOGO
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();
        float boxWidth = screenWidth * 0.6f;
        float boxHeight = screenHeight * 0.4f;
        float boxX = (screenWidth - boxWidth) / 2;
        float boxY = (screenHeight - boxHeight) / 2;

        // DIBUJAR CUADRO PRINCIPAL
        batch.setColor(0.1f, 0.1f, 0.2f, 0.95f);
        batch.draw(GameUtils.getWhitePixel(), boxX, boxY, boxWidth, boxHeight);

        // BORDE DEL CUADRO
        batch.setColor(0.3f, 0.3f, 0.6f, 1f);
        float border = 4f;
        batch.draw(GameUtils.getWhitePixel(), boxX - border, boxY - border,
            boxWidth + border * 2, border);
        batch.draw(GameUtils.getWhitePixel(), boxX - border, boxY + boxHeight,
            boxWidth + border * 2, border);
        batch.draw(GameUtils.getWhitePixel(), boxX - border, boxY,
            border, boxHeight);
        batch.draw(GameUtils.getWhitePixel(), boxX + boxWidth, boxY,
            border, boxHeight);

        batch.setColor(1, 1, 1, 1);

        // TEXTO DENTRO DEL CUADRO
        if (font != null) {
            float margin = 30f;
            float textAreaX = boxX + margin;
            float textAreaY = boxY + boxHeight - margin;
            float textAreaWidth = boxWidth - margin * 2;

            // TÍTULO (nombre de la puerta)
            font.getData().setScale(1.6f);
            String titulo = puertaSeleccionada.getNombreMostrar();
            layout.setText(font, titulo);
            float tituloX = textAreaX + (textAreaWidth - layout.width) / 2;
            float tituloY = textAreaY;

            font.draw(batch, titulo, tituloX, tituloY);

            // ESTADO (centrado debajo del título)
            tituloY -= 50f;
            font.getData().setScale(1.3f);

            String estadoTexto;
            com.badlogic.gdx.graphics.Color colorEstado;

            if (puertaSeleccionada.isBloqueada()) {
                estadoTexto = "BLOQUEADA";
                colorEstado = com.badlogic.gdx.graphics.Color.RED;
            } else if (!puertaSeleccionada.isActiva()) {
                estadoTexto = "INACTIVA";
                colorEstado = com.badlogic.gdx.graphics.Color.GRAY;
            } else {
                estadoTexto = "DISPONIBLE";
                colorEstado = com.badlogic.gdx.graphics.Color.GREEN;
            }

            layout.setText(font, estadoTexto);
            float estadoX = textAreaX + (textAreaWidth - layout.width) / 2;

            font.setColor(colorEstado);
            font.draw(batch, estadoTexto, estadoX, tituloY);
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);

            // DESCRIPCIÓN (debajo del estado)
            tituloY -= 40f;
            font.getData().setScale(1.1f);

            String descripcion;
            if (puertaSeleccionada.isBloqueada()) {
                descripcion = "Completa los requisitos para desbloquear este nivel";
            } else if (puertaSeleccionada.isActiva()) {
                descripcion = "¿Deseas entrar a este nivel?";
            } else {
                descripcion = "Esta puerta no está disponible actualmente";
            }

            layout.setText(font, descripcion);
            float descX = textAreaX + (textAreaWidth - layout.width) / 2;

            font.draw(batch, descripcion, descX, tituloY);

            // CONTROLES (parte inferior del cuadro)
            font.getData().setScale(1.0f);
            font.setColor(0.8f, 0.8f, 0.8f, 1f);

            String controles;
            if (puertaSeleccionada.isBloqueada() || !puertaSeleccionada.isActiva()) {
                controles = gamepadConectado ?
                    "Presiona B para continuar" : "Presiona ESC para continuar";
            } else {
                controles = gamepadConectado ?
                    "A: Entrar   |   B: Cancelar" : "ENTER: Entrar   |   ESC: Cancelar";
            }

            layout.setText(font, controles);
            float controlesX = textAreaX + (textAreaWidth - layout.width) / 2;
            float controlesY = boxY + margin + 30f;

            font.draw(batch, controles, controlesX, controlesY);

            // Restaurar configuración de fuente
            font.setColor(com.badlogic.gdx.graphics.Color.WHITE);
            font.getData().setScale(1.2f);
        }

        batch.end();

        // CONTROLES DE TECLADO Y GAMEPAD
        if (Gdx.input.isKeyJustPressed(Keys.ENTER) ||
            (gamepadConectado && procesador.isBotonAPresionado())) {
            if (puertaSeleccionada.isActiva() && !puertaSeleccionada.isBloqueada()) {
                entrarANivel(puertaSeleccionada);
            } else {
                cancelarDialogoPuerta();
            }
        }

        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE) ||
            (gamepadConectado && procesador.isBotonBPresionado())) {
            cancelarDialogoPuerta();
        }
    }

    /**
     * Entra en el nivel seleccionado a través de la puerta.
     */
    /**
     * Entra en el nivel seleccionado a través de la puerta.
     */
    private void entrarANivel(PuertaTransicion puerta) {
        Gdx.app.log("LobbyScreen", "=== ENTRANDO A NIVEL ===");
        Gdx.app.log("LobbyScreen", "Puerta: " + puerta.getNombreMostrar());
        Gdx.app.log("LobbyScreen", "ID Nivel: " + puerta.getIdNivel());
        Gdx.app.log("LobbyScreen", "Mapa destino original: " + puerta.getMapaDestino());

        try {
            // Obtener GameState
            GameState gameState = GameState.getInstance();

            // ========== 1. CONFIGURAR MAPA EN GAMESTATE ==========

            // Primero, establecer el nivel actual
            gameState.setNivelActual(puerta.getIdNivel());

            // Determinar qué mapa cargar
            String mapaACargar;

            // SOLUCIÓN: Ignorar mapaDestino si es el por defecto o vacío
            String mapaDestino = puerta.getMapaDestino();
            boolean mapaDestinoValido = mapaDestino != null &&
                !mapaDestino.isEmpty() &&
                !mapaDestino.equals("Tiled/nivel_villa.tmx") &&
                !mapaDestino.equals("default") &&
                !mapaDestino.toLowerCase().contains("villa");

            if (mapaDestinoValido) {
                // Si la puerta tiene un mapa destino específico DIFERENTE a villa, usarlo
                mapaACargar = mapaDestino;
                Gdx.app.log("LobbyScreen", "Usando mapa destino específico de la puerta: " + mapaACargar);
            } else {
                // Si no, usar el mapeo por ID del nivel desde GameState
                mapaACargar = gameState.obtenerRutaMapaPorNivelId(puerta.getIdNivel());
                Gdx.app.log("LobbyScreen", "Usando mapeo por ID: " + puerta.getIdNivel() + " -> " + mapaACargar);

                // Verificar que el archivo existe
                boolean existe = gameState.existeMapa(mapaACargar);
                if (!existe) {
                    Gdx.app.error("LobbyScreen", "Mapa no encontrado: " + mapaACargar);
                    // Fallback al mapa por defecto
                    mapaACargar = "Tiled/nivel_villa.tmx";
                    Gdx.app.log("LobbyScreen", "Usando fallback: " + mapaACargar);
                }
            }

            // Establecer el mapa a cargar en GameState
            gameState.setMapaACargar(mapaACargar);

            // También establecer el ID del nivel seleccionado
            gameState.setNivelSeleccionadoId(puerta.getIdNivel());

            // Asegurarse de que no esté marcado como reinicio
            gameState.setReiniciandoNivel(false);

            // Log para debug
            Gdx.app.log("LobbyScreen", "Configuración GameState completada:");
            Gdx.app.log("LobbyScreen", "  - Nivel actual: " + gameState.getNivelActual());
            Gdx.app.log("LobbyScreen", "  - Mapa a cargar: " + gameState.getMapaACargar());
            Gdx.app.log("LobbyScreen", "  - Nivel seleccionado ID: " + gameState.getNivelSeleccionadoId());
            Gdx.app.log("LobbyScreen", "  - Reiniciando nivel: " + gameState.isReiniciandoNivel());

            // ========== 2. DETENER MÚSICA DEL LOBBY ==========
            if (musica != null) {
                musica.stop();
                musica.dispose();
                musica = null;
                Gdx.app.log("LobbyScreen", "Música del lobby detenida");
            }

            // ========== 3. CREAR Y MOSTRAR MAINSCREEN ==========
            // Crear MainScreen (usará la configuración de GameState)
            MainScreen nivel = new MainScreen(game);

            // Cambiar a MainScreen
            game.setScreen(nivel);

            // Liberar recursos del lobby
            dispose();

            Gdx.app.log("LobbyScreen", "✅ Transición a MainScreen completada");

        } catch (Exception e) {
            Gdx.app.error("LobbyScreen", "❌ Error al crear MainScreen", e);

            // Mostrar mensaje de error al jugador
            mostrarNotificacion("Error al cargar el nivel. Inténtalo de nuevo.");

            // Cancelar diálogo y regresar al lobby
            cancelarDialogoPuerta();
        }
    }
    /**
     * Cancela el diálogo de puerta.
     */
    private void cancelarDialogoPuerta() {
        mostrarDialogoPuerta = false;
        puertaSeleccionada = null;

        // Reanudar NPCs
        for (NpcLobby npc : npcs) {
            npc.setPausado(false);
        }

        Gdx.app.log("LobbyScreen", "Diálogo puerta cancelado");
    }

    // ====================== SISTEMA DE VIDEOS DESPUÉS DE DIÁLOGOS ======================

    /**
     * Verifica si hay videos pendientes para reproducir después del diálogo.
     */
    private void manejarVideosDespuesDialogo() {
        Gdx.app.log("LobbyScreen", "=== VERIFICANDO VIDEOS DESPUÉS DE DIÁLOGO ===");

        io.github.javiergames.pieldelobo.Dialogos.DialogoManager dialogoManager =
            io.github.javiergames.pieldelobo.Dialogos.DialogoManager.getInstance();

        if (dialogoManager == null) {
            Gdx.app.error("LobbyScreen", "DialogoManager es null");
            return;
        }

        // Obtener consecuencias pendientes SIN LIMPIAR (solo para verificación)
        java.util.Map<String, String> consecuencias = dialogoManager.obtenerConsecuenciasPendientesParaVerificar();

        if (consecuencias == null || consecuencias.isEmpty()) {
            Gdx.app.log("LobbyScreen", "No hay consecuencias pendientes para verificar");
            return;
        }

        Gdx.app.log("LobbyScreen", "Consecuencias pendientes encontradas: " + consecuencias.size());
        Gdx.app.log("LobbyScreen", "Claves disponibles: " + consecuencias.keySet());

        // CORRECCIÓN: Usar las claves correctas
        String videoId = consecuencias.get("video_pendiente_id");
        String videoMensaje = consecuencias.get("video_pendiente_mensaje");

        if (videoId != null && !videoId.isEmpty()) {
            Gdx.app.log("LobbyScreen", "✅ VIDEO PENDIENTE ENCONTRADO: " + videoId);
            Gdx.app.log("LobbyScreen", "Mensaje del video: " + videoMensaje);

            // IMPORTANTE: Limpiar las consecuencias antes de reproducir el video
            dialogoManager.obtenerYLimpiarConsecuenciasPendientes();

            // Reproducir el video inmediatamente
            reproducirVideoInmediato(videoId, videoMensaje);
        } else {
            Gdx.app.log("LobbyScreen", "❌ No hay video ID en consecuencias pendientes");

            // Mostrar otras consecuencias si las hay
            mostrarConsecuenciasPendientes();
            actualizarEstadoPuertasDespuesDialogo();
        }
    }

    /**
     * Reproduce el video pendiente después de un diálogo.
     */
    private void reproducirVideoInmediato(String videoId, String videoMensaje) {
        Gdx.app.log("LobbyScreen", "=== INICIANDO REPRODUCCIÓN DE VIDEO ===");
        Gdx.app.log("LobbyScreen", "Video ID: " + videoId);
        Gdx.app.log("LobbyScreen", "Mensaje: " + videoMensaje);

        // Obtener la ruta del video usando VideoManager
        VideoManager videoManager = VideoManager.getInstance();
        String videoPath = videoManager.getVideoPath(videoId);

        Gdx.app.log("LobbyScreen", "Ruta del video: " + videoPath);

        // Verificar si el archivo existe
        if (!Gdx.files.internal(videoPath).exists()) {
            Gdx.app.error("LobbyScreen", "❌ ARCHIVO DE VIDEO NO ENCONTRADO: " + videoPath);

            // Mostrar notificación de error
            if (videoMensaje != null) {
                mostrarNotificacion("⚠️ Error: Video '" + videoId + "' no encontrado");
            }

            // Continuar con otras consecuencias
            mostrarConsecuenciasPendientes();
            actualizarEstadoPuertasDespuesDialogo();
            return;
        }

        // Mostrar mensaje informativo del video
        if (videoMensaje != null) {
            mostrarNotificacion("🎬 " + videoMensaje);
        } else {
            mostrarNotificacion("🎬 Reproduciendo video...");
        }

        // Pausar música del lobby durante el video
        if (musica != null && musica.isPlaying()) {
            musica.pause();
            Gdx.app.log("LobbyScreen", "Música pausada para reproducción de video");
        }

        // Crear y mostrar la pantalla de video
        VideoScreen videoScreen = new VideoScreen(game, videoPath, this);
        game.setScreen(videoScreen);

        Gdx.app.log("LobbyScreen", "✅ Pantalla de video iniciada correctamente");
        Gdx.app.log("LobbyScreen", "Volverá a: LobbyScreen");
    }

    // ====================== SISTEMA DE NOTIFICACIONES ======================

    /**
     * Muestra una notificación en pantalla.
     */
    public void mostrarNotificacion(String mensaje) {
        notificacionTexto = mensaje;
        notificacionTiempo = NOTIFICACION_DURACION;

        Gdx.app.log("NOTIFICACION", mensaje);

        try {
            com.badlogic.gdx.audio.Sound sound =
                Gdx.audio.newSound(Gdx.files.internal("sounds/notification.wav"));
            if (sound != null) {
                sound.play(0.5f);
            }
        } catch (Exception e) {
            // Silenciar si no hay sonido disponible
        }
    }

    /**
     * Dibuja la notificación actual en pantalla.
     */
    private void dibujarNotificacion() {
        if (notificacionTexto == null || notificacionTexto.isEmpty() || notificacionTiempo <= 0) {
            return;
        }

        batch.begin();

        // Calcular opacidad (parpadeo al final)
        float alpha = Math.min(1.0f, notificacionTiempo * 2f);
        if (notificacionTiempo < 0.5f) {
            alpha = 0.5f + 0.5f * (float)Math.sin(notificacionTiempo * 20f);
        }

        // Determinar si es notificación especial
        boolean esEspecial = notificacionTexto.contains("✨");

        // Dimensiones de la notificación
        float ancho = esEspecial ? 450f : 400f;
        float alto = notificacionTexto.contains("\n") ? 80f : 60f;
        if (esEspecial) alto += 20f;

        float x = (Gdx.graphics.getWidth() - ancho) / 2;
        float y = Gdx.graphics.getHeight() - 100f;

        // Seleccionar colores según tipo
        com.badlogic.gdx.graphics.Color colorFondo, colorBorde, colorTexto;

        if (esEspecial) {
            colorFondo = COLOR_NOTIFICACION_ESPECIAL_FONDO;
            colorBorde = COLOR_NOTIFICACION_ESPECIAL_BORDE;
            colorTexto = COLOR_NOTIFICACION_ESPECIAL_TEXTO;
        } else {
            colorFondo = COLOR_NOTIFICACION_FONDO;
            colorBorde = COLOR_NOTIFICACION_BORDE;
            colorTexto = COLOR_NOTIFICACION_TEXTO;
        }

        // Fondo con opacidad
        batch.setColor(
            colorFondo.r,
            colorFondo.g,
            colorFondo.b,
            colorFondo.a * alpha
        );
        batch.draw(GameUtils.getWhitePixel(), x, y, ancho, alto);

        // Borde
        batch.setColor(
            colorBorde.r,
            colorBorde.g,
            colorBorde.b,
            colorBorde.a * alpha
        );
        float borde = esEspecial ? 3f : 2f;
        batch.draw(GameUtils.getWhitePixel(), x - borde, y - borde, ancho + borde * 2, borde);
        batch.draw(GameUtils.getWhitePixel(), x - borde, y + alto, ancho + borde * 2, borde);
        batch.draw(GameUtils.getWhitePixel(), x - borde, y, borde, alto);
        batch.draw(GameUtils.getWhitePixel(), x + ancho, y, borde, alto);

        // Texto de notificación
        if (font != null) {
            font.setColor(
                colorTexto.r,
                colorTexto.g,
                colorTexto.b,
                colorTexto.a * alpha
            );

            if (esEspecial) {
                font.getData().setScale(1.4f);
            } else {
                font.getData().setScale(1.2f);
            }

            // Dividir en líneas si es necesario
            String[] lineas = notificacionTexto.split("\n");
            for (int i = 0; i < lineas.length; i++) {
                layout.setText(font, lineas[i]);
                float textoX = x + (ancho - layout.width) / 2;
                float textoY = y + alto - 20f - (i * 30f);
                font.draw(batch, lineas[i], textoX, textoY);
            }

            // Restaurar configuración
            font.setColor(1f, 1f, 1f, 1f);
            font.getData().setScale(1.2f);
        }

        batch.end();
    }

    // ====================== MÉTODOS DE ACTUALIZACIÓN ======================

    /**
     * Actualiza la visibilidad de los iconos de puertas según proximidad del jugador.
     */
    private void actualizarIconosPuertas() {
        for (IconoInteraccion icono : iconosPuertas) {
            boolean cerca = icono.colisionaConJugador(jugador);
            icono.setMostrar(cerca);
        }
    }

    /**
     * Dibuja indicadores visuales sobre NPCs con los que se puede interactuar.
     */
    private void dibujarIndicadoresNPCs() {
        batch.begin();
        batch.setProjectionMatrix(camara.combined);

        for (NpcLobby npc : npcs) {
            if (npc.tieneDialogo() && npc.puedeInteractuar(jugador)) {
                dibujarIndicadorExclamacion(npc);
            }
        }

        batch.end();
    }

    /**
     * Dibuja un indicador de exclamación sobre un NPC.
     */
    private void dibujarIndicadorExclamacion(NpcLobby npc) {
        try {
            float centroX = npc.getX() + npc.getWidth() / 2;
            float parteSuperiorY = npc.getY() + npc.getHeight();
            float indicadorY = parteSuperiorY + 15f;

            // Animación de parpadeo
            float alpha = 0.4f + 0.6f * (float)Math.abs(Math.sin(tiempoIndicador * 3f));
            batch.setColor(1f, 0f, 0f, alpha);

            // Dibujar signo de exclamación (!)
            float anchoDelgado = 4f;
            float anchoAncho = 8f;
            float altoPalo = 14f;
            float altoPunto = 6f;
            float espacio = 2f;

            // Punto superior
            float puntoX = centroX - anchoAncho / 2;
            float puntoY = indicadorY;
            batch.draw(GameUtils.getWhitePixel(), puntoX, puntoY, anchoAncho, altoPunto);

            // Palo vertical
            float paloX = centroX - anchoDelgado / 2;
            float paloY = indicadorY + altoPunto + espacio;
            batch.draw(GameUtils.getWhitePixel(), paloX, paloY, anchoDelgado, altoPalo);

            batch.setColor(1f, 1f, 1f, 1f);
        } catch (Exception e) {
            Gdx.app.error("LobbyScreen", "Error dibujando indicador NPC", e);
        }
    }

    // ====================== SISTEMA DE COLISIONES ======================

    /**
     * Verifica colisiones entre entidades.
     */
    private void verificarColisiones() {
        // Colisiones entre NPCs
        for (int i = 0; i < npcs.size; i++) {
            for (int j = i + 1; j < npcs.size; j++) {
                if (npcs.get(i).hayColisionCon(npcs.get(j))) {
                    separarNpcs(npcs.get(i), npcs.get(j));
                }
            }
        }

        // Colisiones jugador-NPC
        for (NpcLobby npc : npcs) {
            if (npc.hayColisionCon(jugador)) {
                empujarJugadorDeNpc(npc);
            }
        }
    }

    /**
     * Separa dos NPCs que están colisionando.
     */
    private void separarNpcs(NpcLobby npc1, NpcLobby npc2) {
        float dx = npc2.getX() - npc1.getX();
        float dy = npc2.getY() - npc1.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float separationForce = 2f;
            float sepX = (dx / distance) * separationForce;
            float sepY = (dy / distance) * separationForce;

            npc1.setX(npc1.getX() - sepX);
            npc1.setY(npc1.getY() - sepY);
            npc2.setX(npc2.getX() + sepX);
            npc2.setY(npc2.getY() + sepY);

            mantenerNpcEnMapa(npc1);
            mantenerNpcEnMapa(npc2);
        }
    }

    /**
     * Empuja al jugador fuera de un NPC.
     */
    private void empujarJugadorDeNpc(NpcLobby npc) {
        float dx = jugador.getX() - npc.getX();
        float dy = jugador.getY() - npc.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float pushForce = 1.5f;
            float pushX = (dx / distance) * pushForce;
            float pushY = (dy / distance) * pushForce;

            float xAnterior = jugador.getX();
            float yAnterior = jugador.getY();

            jugador.setX(jugador.getX() + pushX);
            jugador.setY(jugador.getY() + pushY);

            if (mapaManager.hayColision(jugador.getHitbox())) {
                jugador.setX(xAnterior);
                jugador.setY(yAnterior);
            } else {
                mantenerJugadorEnMapa();
            }
        }
    }

    /**
     * Mantiene al jugador dentro de los límites del mapa.
     */
    private void mantenerJugadorEnMapa() {
        if (mapaManager == null) return;

        float minX = 0;
        float minY = 0;
        float maxX = mapaManager.getAnchoMapa() - jugador.getWidth();
        float maxY = mapaManager.getAltoMapa() - jugador.getHeight();

        if (jugador.getX() < minX) jugador.setX(minX);
        if (jugador.getY() < minY) jugador.setY(minY);
        if (jugador.getX() > maxX) jugador.setX(maxX);
        if (jugador.getY() > maxY) jugador.setY(maxY);
    }

    /**
     * Mantiene a un NPC dentro de los límites del mapa.
     */
    private void mantenerNpcEnMapa(NpcLobby npc) {
        if (mapaManager == null) return;

        float minX = 0;
        float minY = 0;
        float maxX = mapaManager.getAnchoMapa() - npc.getWidth();
        float maxY = mapaManager.getAltoMapa() - npc.getHeight();

        if (npc.getX() < minX) npc.setX(minX);
        if (npc.getY() < minY) npc.setY(minY);
        if (npc.getX() > maxX) npc.setX(maxX);
        if (npc.getY() > maxY) npc.setY(maxY);
    }

    // ====================== SISTEMA DE PAUSA ======================

    /**
     * Muestra el menú de pausa.
     */
    private void mostrarPausa() {
        Gdx.app.log("LobbyScreen", "Mostrando menú de pausa");
        pausado = true;
        procesadorGuardado = procesador;

        game.setScreen(new PauseScreen(game, this));
    }

    /**
     * Reanuda el juego desde el menú de pausa.
     */
    public void reanudarDesdePausa() {
        Gdx.app.log("LobbyScreen", "Reanudando desde pausa");
        pausado = false;

        if (procesadorGuardado != null) {
            procesador = procesadorGuardado;
            Gdx.input.setInputProcessor(procesador);
            Gdx.app.log("LobbyScreen", "Input processor restaurado");
        }
    }

    // ====================== MÉTODOS DE CICLO DE VIDA (CONTINUACIÓN) ======================

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("LobbyScreen", "Resize: " + width + "x" + height);

        if (viewport != null) {
            viewport.update(width, height, true);

            if (mapaManager != null && mapaManager.estaCargado()) {
                mapaManager.setView(camara);
            }
        }
    }

    @Override
    public void pause() {
        Gdx.app.log("LobbyScreen", "Juego pausado");
        pausado = true;
    }

    @Override
    public void resume() {
        Gdx.app.log("LobbyScreen", "Juego reanudado");
        pausado = false;
    }

    @Override
    public void hide() {
        Gdx.app.log("LobbyScreen", "Pantalla ocultada");
    }

    @Override
    public void dispose() {
        Gdx.app.log("LobbyScreen", "=== LIBERANDO RECURSOS DEL LOBBY ===");

        long inicio = System.currentTimeMillis();

        try {
            // 1. Liberar sistema de diálogos
            if (ventanaDialogo != null) {
                ventanaDialogo.dispose();
                Gdx.app.log("LobbyScreen", "Sistema de diálogos liberado");
            }

            // 2. Liberar jugador
            if (jugador != null) {
                jugador.dispose();
                Gdx.app.log("LobbyScreen", "Jugador liberado");
            }

            // 3. Liberar NPCs
            for (NpcLobby npc : npcs) {
                try {
                    npc.dispose();
                } catch (Exception e) {
                    Gdx.app.error("LobbyScreen", "Error liberando NPC", e);
                }
            }
            npcs.clear();
            Gdx.app.log("LobbyScreen", "NPCs liberados");

            // 4. Liberar puertas
            for (PuertaTransicion puerta : puertas) {
                try {
                    puerta.dispose();
                } catch (Exception e) {
                    Gdx.app.error("LobbyScreen", "Error liberando puerta", e);
                }
            }
            puertas.clear();
            Gdx.app.log("LobbyScreen", "Puertas liberadas");

            // 5. Liberar iconos
            for (IconoInteraccion icono : iconosPuertas) {
                try {
                    icono.dispose();
                } catch (Exception e) {
                    Gdx.app.error("LobbyScreen", "Error liberando icono", e);
                }
            }
            iconosPuertas.clear();
            Gdx.app.log("LobbyScreen", "Iconos liberados");

            // 6. Liberar stage
            if (stage != null) {
                stage.dispose();
                Gdx.app.log("LobbyScreen", "Stage liberado");
            }

            // 7. Liberar mapa
            if (mapaManager != null) {
                mapaManager.dispose();
                Gdx.app.log("LobbyScreen", "Mapa liberado");
            }
            // 8. Liberar música
            if (musica != null) {
                musica.stop();
                musica.dispose();
                musica = null;
                Gdx.app.log("LobbyScreen", "Música liberada");
            }


            // 9. Liberar batch
            if (batch != null) {
                batch.dispose();
                Gdx.app.log("LobbyScreen", "Batch liberado");
            }

            // 10. Liberar fuente
            if (font != null) {
                font.dispose();
                Gdx.app.log("LobbyScreen", "Fuente liberada");
            }

            // 11. Liberar utilidades gráficas
            GameUtils.dispose();
            Gdx.app.log("LobbyScreen", "GameUtils liberado");

        } catch (Exception e) {
            Gdx.app.error("LobbyScreen", "Error durante dispose", e);
        }

        long fin = System.currentTimeMillis();
        Gdx.app.log("LobbyScreen", "=== RECURSOS LIBERADOS EN " + (fin - inicio) + "ms ===");
    }

    private void cargarMusica() {
        try {
            // Primero, asegurarse de que no haya música previa
            if (musica != null) {
                musica.dispose();
                musica = null;
            }
            musica = Gdx.audio.newMusic(Gdx.files.internal("musica/Laboratorio_Laton.mp3"));
            musica.setLooping(true);
            musica.setVolume(0.5f);
            musica.play();
            Gdx.app.log("MenuScreen", "Música de menú cargada");
        } catch (Exception e) {
            Gdx.app.error("MenuScreen", "Error cargando música", e);
            musica = null;
        }
    }

    /**
     * Detiene la música del lobby antes de volver al menú principal.
     */
    public void detenerMusica() {
        Gdx.app.log("LobbyScreen", "Deteniendo música del lobby...");

        if (musica != null) {
            musica.stop();
            musica.dispose();
            musica = null;
            Gdx.app.log("LobbyScreen", "Música del lobby detenida correctamente");
        }
    }
    /**
     * Actualiza el estado de todas las puertas después de volver de un video.
     */
    private void actualizarPuertasDespuesDeVideo() {
        Gdx.app.log("LobbyScreen", "=== ACTUALIZANDO PUERTAS DESPUÉS DE VIDEO ===");

        GameState gameState = GameState.getInstance();
        boolean huboCambios = false;

        for (PuertaTransicion puerta : puertas) {
            String nivelId = puerta.getIdNivel();

            try {
                boolean desbloqueado = gameState.isNivelDesbloqueado(nivelId);
                boolean actualmenteBloqueada = puerta.isBloqueada();

                Gdx.app.log("LobbyScreen",
                    "Verificando puerta " + puerta.getNombreMostrar() +
                        " - Desbloqueado: " + desbloqueado +
                        " - Actualmente bloqueada: " + actualmenteBloqueada);

                if (desbloqueado && actualmenteBloqueada) {
                    // ¡Nivel desbloqueado! Actualizar puerta
                    puerta.setBloqueada(false);
                    puerta.setActiva(true);
                    huboCambios = true;

                    Gdx.app.log("LobbyScreen",
                        "✅ PUERTA ACTUALIZADA: " + puerta.getNombreMostrar() +
                            " ahora está DESBLOQUEADA");

                    // Mostrar notificación
                    mostrarNotificacionEspecial("¡" + puerta.getNombreMostrar() + " ahora está disponible!");

                    // Aplicar efecto visual
                    aplicarEfectoVisualPuerta(puerta);
                }
            } catch (Exception e) {
                Gdx.app.error("LobbyScreen", "Error verificando puerta: " + nivelId, e);
            }
        }

        if (huboCambios) {
            Gdx.app.log("LobbyScreen", "✅ Cambios aplicados a puertas después del video");
        } else {
            Gdx.app.log("LobbyScreen", "No se detectaron cambios en puertas después del video");
        }
    }

    // ====================== MÉTODOS DE ACCESO (GETTERS) ======================

    public Procesador getProcesador() {
        return procesador;
    }

    public boolean estaPausado() {
        return pausado;
    }

    public int getNumeroNpcs() {
        return npcs.size;
    }

    public int getNumeroPuertas() {
        return puertas.size;
    }

    public int getNumeroIconos() {
        return iconosPuertas.size;
    }

    public boolean estaEnDialogo() {
        return enDialogo;
    }

    public PersonajeLobby getJugador() {
        return jugador;
    }

    public MapaManager getMapaManager() {
        return mapaManager;
    }

    public Array<NpcLobby> getNpcs() {
        return npcs;
    }

    public Array<PuertaTransicion> getPuertas() {
        return puertas;
    }
}
