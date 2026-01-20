package io.github.javiergames.pieldelobo.Mapas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Maneja la carga, renderizado y colisiones del mapa Tiled (.tmx).
 * Incluye sistema opcional de debug para colisiones.
 * NUEVO: Ahora incluye sistema para indicadores de nivel.
 *
 * @author Javier Gala
 * @version 2.1
 */
public class MapaManager {
    private TiledMap mapa;
    private OrthogonalTiledMapRenderer renderer;
    private Array<Rectangle> colisiones;
    private boolean cargado;
    private float unidadEscala;

    // Sistema de debug opcional
    private boolean debugColisiones = false;
    private com.badlogic.gdx.graphics.glutils.ShapeRenderer shapeRenderer;
    /**
     * Constructor principal que carga un mapa desde la ruta especificada.
     *
     * @param rutaMapa Ruta del archivo .tmx a cargar
     */
    public MapaManager(String rutaMapa) {
        this.colisiones = new Array<>();
        this.cargado = false;
        this.unidadEscala = 1f;

        try {
            Gdx.app.log("MapaManager", "Cargando mapa: " + rutaMapa);

            TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
            mapa = new TmxMapLoader().load(rutaMapa, params);
            renderer = new OrthogonalTiledMapRenderer(mapa, unidadEscala);

            // Inicializar ShapeRenderer para debug
            shapeRenderer = new com.badlogic.gdx.graphics.glutils.ShapeRenderer();

            cargarColisiones();

            cargado = true;
            Gdx.app.log("MapaManager", "Mapa cargado correctamente: " + rutaMapa);
            Gdx.app.log("MapaManager", "Número de colisiones: " + colisiones.size);

        } catch (Exception e) {
            Gdx.app.error("MapaManager", "Error al cargar mapa " + rutaMapa, e);
            cargarMapaDeRespaldo();
        }
    }

    private void cargarMapaDeRespaldo() {
        Gdx.app.log("MapaManager", "Modo respaldo: sin mapa cargado");
    }

    private void cargarColisiones() {
        if (mapa == null) return;

        String[] posiblesNombres = {"colisiones", "collision", "obstaculos", "walls", "Colisiones"};

        for (String nombre : posiblesNombres) {
            MapLayer capa = mapa.getLayers().get(nombre);
            if (capa != null) {
                Gdx.app.log("MapaManager", "Encontrada capa de colisiones: " + nombre);

                for (MapObject obj : capa.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        colisiones.add(rect);
                        Gdx.app.log("MapaManager", "Colisión cargada: " + rect);
                    }
                }
                return;
            }
        }
        Gdx.app.log("MapaManager", "No se encontró capa de colisiones");
    }

    // ====================== MÉTODOS DE OBTENCIÓN DE INFORMACIÓN ======================
    /**
     * Obtiene el ancho total del mapa en píxeles.
     *
     * @return Ancho del mapa o 800 si hay error
     */
    public float getAnchoMapa() {
        if (!cargado || mapa == null) return 800;
        try {
            int tileWidth = mapa.getProperties().get("tilewidth", Integer.class);
            int mapWidth = mapa.getProperties().get("width", Integer.class);
            return tileWidth * mapWidth * unidadEscala;
        } catch (Exception e) {
            Gdx.app.error("MapaManager", "Error al obtener ancho del mapa", e);
            return 800;
        }
    }
    /**
     * Obtiene el alto total del mapa en píxeles.
     *
     * @return Alto del mapa o 600 si hay error
     */
    public float getAltoMapa() {
        if (!cargado || mapa == null) return 600;
        try {
            int tileHeight = mapa.getProperties().get("tileheight", Integer.class);
            int mapHeight = mapa.getProperties().get("height", Integer.class);
            return tileHeight * mapHeight * unidadEscala;
        } catch (Exception e) {
            Gdx.app.error("MapaManager", "Error al obtener alto del mapa", e);
            return 600;
        }
    }
    /**
     * Muestra información de debug sobre colisiones en la consola.
     * Útil para desarrollo y testing.
     *
     * @param hitbox Hitbox a verificar contra las colisiones del mapa
     */
    public void mostrarDebugColisiones(Rectangle hitbox) {
        if (!debugColisiones) return;

        Gdx.app.log("Colisiones", "=== DEBUG DE COLISIONES ===");
        Gdx.app.log("Colisiones", "Hitbox: " + hitbox);

        for (int i = 0; i < colisiones.size; i++) {
            Rectangle colision = colisiones.get(i);
            Gdx.app.log("Colisiones", "Colisión " + i + ": " + colision);

            if (hitbox.overlaps(colision)) {
                Gdx.app.log("Colisiones", "¡COLISIÓN DETECTADA con rect " + i + "!");
            }
        }
    }
    /**
     * Verifica si hay colisión entre un hitbox y las colisiones del mapa.
     *
     * @param hitboxJugador Hitbox a verificar
     * @return true si hay colisión, false en caso contrario
     */
    public boolean hayColision(Rectangle hitboxJugador) {
        if (!cargado) {
            return false;
        }

        for (Rectangle colision : colisiones) {
            if (hitboxJugador.overlaps(colision)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtiene la posición de spawn específica para el jugador desde el mapa.
     * Busca objetos llamados "spawn_jugador" o "player_spawn" en capas específicas.
     *
     * @return Vector2 con la posición de spawn, o posición por defecto (100, 100) si no se encuentra
     */
    public Vector2 obtenerPosicionSpawnJugador() {
        if (!cargado) {
            Gdx.app.log("MapaManager", "Mapa no cargado, usando posición por defecto para jugador");
            return new Vector2(100, 100);
        }

        // Buscar específicamente el spawn del jugador
        String[] posiblesCapasSpawn = {"spawn", "objects", "objetos", "points", "player", "spawns"};

        for (String nombreCapa : posiblesCapasSpawn) {
            MapLayer spawnLayer = mapa.getLayers().get(nombreCapa);
            if (spawnLayer != null) {
                for (MapObject obj : spawnLayer.getObjects()) {
                    // Buscar objeto llamado "spawn_jugador" o con propiedad específica
                    if (obj instanceof RectangleMapObject) {
                        String nombreObjeto = obj.getName();
                        if (nombreObjeto != null && (nombreObjeto.equals("spawn_jugador") || nombreObjeto.equals("player_spawn"))) {
                            Rectangle spawn = ((RectangleMapObject) obj).getRectangle();
                            float x = spawn.x;
                            float y = spawn.y;
                            Gdx.app.log("MapaManager", "Spawn JUGADOR encontrado: " + x + ", " + y);
                            return new Vector2(x, y);
                        }
                    }
                }
            }
        }

        Gdx.app.log("MapaManager", "No se encontró spawn específico para jugador, usando spawn genérico");
        return obtenerPosicionInicial(); // Fallback al método antiguo
    }

    /**
     * Obtiene información de spawn para NPCs desde el mapa
     */
    public Array<NpcSpawnInfo> obtenerInfoSpawnNpcs() {
        Array<NpcSpawnInfo> npcInfo = new Array<>();

        if (!cargado) {
            Gdx.app.log("MapaManager", "Mapa no cargado, sin información de NPCs");
            return npcInfo;
        }

        String[] posiblesCapas = {"spawn", "objects", "objetos", "points", "npcs", "spawns"};

        for (String nombreCapa : posiblesCapas) {
            MapLayer spawnLayer = mapa.getLayers().get(nombreCapa);
            if (spawnLayer != null) {
                for (MapObject obj : spawnLayer.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        String nombreObjeto = obj.getName();
                        if (nombreObjeto != null && nombreObjeto.startsWith("spawn_npc")) {
                            Rectangle spawn = ((RectangleMapObject) obj).getRectangle();

                            // Obtener propiedades del objeto
                            MapProperties propiedades = obj.getProperties();
                            String tipo = "medico"; // Valor por defecto

                            // Verificar si existe la propiedad "tipo"
                            if (propiedades.containsKey("tipo")) {
                                tipo = propiedades.get("tipo", "medico", String.class);
                            }

                            NpcSpawnInfo info = new NpcSpawnInfo(
                                new Vector2(spawn.x, spawn.y),
                                tipo
                            );
                            npcInfo.add(info);
                            Gdx.app.log("MapaManager", "Spawn NPC encontrado: " + tipo + " en " + spawn.x + ", " + spawn.y);
                        }
                    }
                }
            }
        }

        if (npcInfo.size == 0) {
            Gdx.app.log("MapaManager", "No se encontraron spawns de NPCs específicos");
        }

        return npcInfo;
    }

    /**
     * Obtiene posición inicial (método legacy - mantener compatibilidad)
     */
    public Vector2 obtenerPosicionInicial() {
        if (!cargado) {
            Gdx.app.log("MapaManager", "Mapa no cargado, usando posición por defecto");
            return new Vector2(100, 100);
        }

        String[] posiblesCapasSpawn = {"spawn", "objects", "objetos", "points", "player"};

        for (String nombreCapa : posiblesCapasSpawn) {
            MapLayer spawnLayer = mapa.getLayers().get(nombreCapa);
            if (spawnLayer != null) {
                for (MapObject obj : spawnLayer.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle spawn = ((RectangleMapObject) obj).getRectangle();
                        float x = spawn.x;
                        float y = spawn.y;
                        Gdx.app.log("MapaManager", "Spawn encontrado en capa " + nombreCapa + ": " + x + ", " + y);
                        return new Vector2(x, y);
                    }
                }
            }
        }

        Gdx.app.log("MapaManager", "No se encontró spawn, buscando área segura...");

        float[] posicionesX = {100, 200, 300, 400};
        float[] posicionesY = {100, 200, 300};

        for (float x : posicionesX) {
            for (float y : posicionesY) {
                Rectangle testHitbox = new Rectangle(x, y, 32, 32);
                if (!hayColision(testHitbox)) {
                    Gdx.app.log("MapaManager", "Área segura encontrada: " + x + ", " + y);
                    return new Vector2(x, y);
                }
            }
        }

        Gdx.app.log("MapaManager", "Usando posición por defecto");
        return new Vector2(100, 500);
    }

    /**
     * Obtiene información de puertas/transiciones desde el mapa Tiled
     */
    public Array<PuertaInfo> obtenerInfoPuertas() {
        Array<PuertaInfo> puertasInfo = new Array<>();

        if (!cargado) {
            Gdx.app.log("MapaManager", "Mapa no cargado, sin información de puertas");
            return puertasInfo;
        }

        // Buscar en diferentes nombres de capa
        String[] posiblesCapas = {"puertas", "doors", "transiciones", "zonas", "objects", "objetos", "points"};

        for (String nombreCapa : posiblesCapas) {
            MapLayer capa = mapa.getLayers().get(nombreCapa);
            if (capa != null) {
                Gdx.app.log("MapaManager", "Buscando puertas en capa: " + nombreCapa);

                for (MapObject obj : capa.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        MapProperties propiedades = obj.getProperties();

                        // Verificar si es una puerta (por nombre o propiedad)
                        String nombreObjeto = obj.getName();
                        boolean esPuerta = false;
                        String tipoObjeto = "";

                        if (nombreObjeto != null) {
                            esPuerta = nombreObjeto.toLowerCase().contains("puerta") ||
                                nombreObjeto.toLowerCase().contains("door") ||
                                nombreObjeto.toLowerCase().contains("entrada") ||
                                nombreObjeto.toLowerCase().contains("nivel");
                            tipoObjeto = nombreObjeto;
                        }

                        // También verificar propiedad "tipo" si existe
                        if (propiedades.containsKey("tipo")) {
                            String tipo = propiedades.get("tipo", "", String.class);
                            esPuerta = esPuerta || tipo.toLowerCase().contains("puerta") ||
                                tipo.toLowerCase().contains("door") ||
                                tipo.toLowerCase().contains("nivel");
                            tipoObjeto = tipo;
                        }

                        // Si no tiene nombre específico pero está en capa "puertas", asumir que es puerta
                        if (nombreCapa.equals("puertas") || nombreCapa.equals("doors")) {
                            esPuerta = true;
                        }

                        if (esPuerta) {
                            // Obtener propiedades con valores por defecto
                            String idNivel = propiedades.get("nivel",
                                tipoObjeto.isEmpty() ? "nivel_1" : tipoObjeto, String.class);
                            String nombre = propiedades.get("nombre",
                                "Nivel " + idNivel.replace("nivel_", ""), String.class);
                            boolean activa = propiedades.get("activa", true, Boolean.class);
                            boolean bloqueada = propiedades.get("bloqueada", false, Boolean.class);
                            String mapaDestino = propiedades.get("mapa", "Tiled/nivel_villa.tmx", String.class);

                            PuertaInfo info = new PuertaInfo(
                                new Vector2(rect.x, rect.y),
                                new Vector2(rect.width, rect.height),
                                idNivel, nombre, activa, bloqueada, mapaDestino
                            );

                            puertasInfo.add(info);
                            Gdx.app.log("MapaManager", "Puerta encontrada: " + nombre +
                                " -> " + idNivel + " en [" + rect.x + "," + rect.y + "]");
                        }
                    }
                }
            }
        }

        if (puertasInfo.size == 0) {
            Gdx.app.log("MapaManager", "No se encontraron puertas en el mapa");
        } else {
            Gdx.app.log("MapaManager", "Encontradas " + puertasInfo.size + " puertas");
        }

        return puertasInfo;
    }

    /**
     * Obtiene información de enemigos desde el mapa Tiled.
     * Busca objetos en capas específicas y los convierte en información de spawn.
     */
    public Array<EnemigoSpawnInfo> obtenerInfoSpawnEnemigos() {
        Array<EnemigoSpawnInfo> enemigosInfo = new Array<>();

        if (!cargado || mapa == null) {
            Gdx.app.log("MapaManager", "Mapa no cargado, sin información de enemigos");
            return enemigosInfo;
        }

        // Buscar en diferentes nombres de capa para enemigos
        String[] posiblesCapas = {"enemigos", "enemies", "spawns", "objects", "objetos", "npcs", "personajes"};

        for (String nombreCapa : posiblesCapas) {
            MapLayer capa = mapa.getLayers().get(nombreCapa);
            if (capa != null) {
                Gdx.app.log("MapaManager", "Buscando enemigos en capa: " + nombreCapa);

                for (MapObject obj : capa.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        MapProperties propiedades = obj.getProperties();

                        // Obtener el tipo de enemigo (nombre del objeto o propiedad "tipo")
                        String nombreObjeto = obj.getName();
                        String tipoEnemigo = "";

                        // Intentar obtener de propiedad "tipo"
                        if (propiedades.containsKey("tipo")) {
                            tipoEnemigo = propiedades.get("tipo", "", String.class);
                        }
                        // Si no, usar el nombre del objeto
                        else if (nombreObjeto != null && !nombreObjeto.isEmpty()) {
                            tipoEnemigo = nombreObjeto.toLowerCase();
                        }

                        // Si tenemos un tipo válido, crear información de spawn
                        if (!tipoEnemigo.isEmpty() && esTipoEnemigoValido(tipoEnemigo)) {
                            // Obtener propiedades adicionales
                            int vida = propiedades.get("vida", -1, Integer.class);
                            boolean mirandoDerecha = propiedades.get("mirandoDerecha", true, Boolean.class);

                            EnemigoSpawnInfo info = new EnemigoSpawnInfo(
                                new Vector2(rect.x, rect.y),
                                tipoEnemigo,
                                vida,
                                mirandoDerecha
                            );

                            enemigosInfo.add(info);
                            Gdx.app.log("MapaManager", "Enemigo encontrado: " + tipoEnemigo +
                                " en [" + rect.x + "," + rect.y + "]");
                        }
                    }
                }
            }
        }

        Gdx.app.log("MapaManager", "Total enemigos encontrados en mapa: " + enemigosInfo.size);
        return enemigosInfo;
    }

    /**
     * Obtiene información de indicadores de nivel desde el mapa Tiled.
     * Busca objetos con nombre "indicador_nivel" o similar.
     * ¡NUEVO MÉTODO! Para implementar el sistema de indicadores de final de nivel.
     */
    public Array<IndicadorSpawnInfo> obtenerInfoIndicadoresNivel() {
        Array<IndicadorSpawnInfo> indicadoresInfo = new Array<>();

        if (!cargado || mapa == null) {
            Gdx.app.log("MapaManager", "Mapa no cargado, sin información de indicadores");
            return indicadoresInfo;
        }

        // Buscar en diferentes nombres de capa
        String[] posiblesCapas = {"indicadores", "points", "objects", "objetos", "spawns", "fin", "exit"};

        for (String nombreCapa : posiblesCapas) {
            MapLayer capa = mapa.getLayers().get(nombreCapa);
            if (capa != null) {
                for (MapObject obj : capa.getObjects()) {
                    if (obj instanceof RectangleMapObject) {
                        Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                        String nombreObjeto = obj.getName();

                        // Verificar si es un indicador de nivel
                        boolean esIndicador = false;
                        if (nombreObjeto != null) {
                            esIndicador = nombreObjeto.toLowerCase().contains("indicador") ||
                                nombreObjeto.toLowerCase().contains("fin") ||
                                nombreObjeto.toLowerCase().contains("meta") ||
                                nombreObjeto.toLowerCase().contains("exit") ||
                                nombreObjeto.toLowerCase().contains("salida");
                        }

                        // También verificar propiedades
                        MapProperties propiedades = obj.getProperties();
                        if (propiedades.containsKey("tipo")) {
                            String tipo = propiedades.get("tipo", "", String.class);
                            esIndicador = esIndicador ||
                                tipo.toLowerCase().contains("indicador") ||
                                tipo.toLowerCase().contains("fin") ||
                                tipo.toLowerCase().contains("exit");
                        }

                        if (esIndicador) {
                            String nombre = propiedades.get("nombre", "Portal de Salida", String.class);
                            IndicadorSpawnInfo info = new IndicadorSpawnInfo(
                                new Vector2(rect.x, rect.y),
                                new Vector2(rect.width, rect.height),
                                nombre
                            );

                            indicadoresInfo.add(info);
                            Gdx.app.log("MapaManager", "Indicador de nivel encontrado: " + nombre +
                                " en [" + rect.x + "," + rect.y + "]");
                        }
                    }
                }
            }
        }

        Gdx.app.log("MapaManager", "Total indicadores encontrados: " + indicadoresInfo.size);
        return indicadoresInfo;
    }

    /**
     * Verifica si un tipo de enemigo es válido.
     */
    private boolean esTipoEnemigoValido(String tipo) {
        String[] tiposValidos = {
            "esqueleto", "bandido_pesado", "bandido_ligero", "bandido",
            "arquero", "golem", "bruja_fuego", "necromancer"
        };

        for (String valido : tiposValidos) {
            if (tipo.toLowerCase().contains(valido)) {
                return true;
            }
        }
        return false;
    }

    // ====================== MÉTODOS DE RENDERIZADO ======================

    public void renderizar() {
        if (cargado && renderer != null) {
            renderer.render();
        }

        // Dibujar colisiones en modo debug (opcional)
        if (debugColisiones && cargado && shapeRenderer != null) {
            shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(0, 1, 0, 0.3f); // Verde semi-transparente

            for (Rectangle colision : colisiones) {
                shapeRenderer.rect(colision.x, colision.y, colision.width, colision.height);
            }

            shapeRenderer.end();
        }
    }

    public void setView(com.badlogic.gdx.graphics.OrthographicCamera camara) {
        if (cargado && renderer != null) {
            renderer.setView(camara);
        }
        if (shapeRenderer != null) {
            shapeRenderer.setProjectionMatrix(camara.combined);
        }
    }

    // ====================== MÉTODOS DE DEBUG ======================

    public void setDebugColisiones(boolean debug) {
        this.debugColisiones = debug;
        Gdx.app.log("MapaManager", "Debug colisiones: " + debug);
    }

    public void toggleDebugColisiones() {
        this.debugColisiones = !this.debugColisiones;
        Gdx.app.log("MapaManager", "Debug colisiones: " + debugColisiones);
    }

    public void dispose() {
        if (renderer != null) renderer.dispose();
        if (mapa != null) mapa.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        Gdx.app.log("MapaManager", "Recursos del mapa liberados");
    }

    /**
     * Método de debug para ver información del mapa cargado
     */
    public void logInfoMapa() {
        if (!cargado || mapa == null) {
            Gdx.app.log("MapaManager", "❌ Mapa no cargado o no disponible");
            return;
        }

        try {
            int tileWidth = mapa.getProperties().get("tilewidth", Integer.class);
            int tileHeight = mapa.getProperties().get("tileheight", Integer.class);
            int mapWidth = mapa.getProperties().get("width", Integer.class);
            int mapHeight = mapa.getProperties().get("height", Integer.class);

            Gdx.app.log("MapaManager", "=== INFORMACIÓN DEL MAPA ===");
            Gdx.app.log("MapaManager", "✅ Tiles: " + tileWidth + "x" + tileHeight);
            Gdx.app.log("MapaManager", "✅ Dimensiones: " + mapWidth + "x" + mapHeight + " tiles");
            Gdx.app.log("MapaManager", "✅ Tamaño total: " + getAnchoMapa() + "x" + getAltoMapa() + " px");
            Gdx.app.log("MapaManager", "✅ Capas: " + mapa.getLayers().getCount());
            Gdx.app.log("MapaManager", "✅ Colisiones: " + colisiones.size);

            // Listar capas disponibles
            for (int i = 0; i < mapa.getLayers().getCount(); i++) {
                Gdx.app.log("MapaManager", "   - Capa " + i + ": " + mapa.getLayers().get(i).getName());
            }

        } catch (Exception e) {
            Gdx.app.error("MapaManager", "Error obteniendo info del mapa", e);
        }
    }

    // ====================== CLASES INTERNAS ======================

    /**
     * Clase para almacenar información de spawn de NPCs
     */
    public static class NpcSpawnInfo {
        public Vector2 posicion;
        public String tipo;

        public NpcSpawnInfo(Vector2 posicion, String tipo) {
            this.posicion = posicion;
            this.tipo = tipo;
        }

        @Override
        public String toString() {
            return "NpcSpawnInfo{posicion=" + posicion + ", tipo='" + tipo + "'}";
        }
    }

    /**
     * Clase para almacenar información de spawn de enemigos
     */
    public static class EnemigoSpawnInfo {
        public Vector2 posicion;
        public String tipo;
        public int vidaPersonalizada;
        public boolean mirandoDerecha;

        public EnemigoSpawnInfo(Vector2 posicion, String tipo, int vidaPersonalizada, boolean mirandoDerecha) {
            this.posicion = posicion;
            this.tipo = tipo;
            this.vidaPersonalizada = vidaPersonalizada;
            this.mirandoDerecha = mirandoDerecha;
        }

        @Override
        public String toString() {
            return "EnemigoSpawnInfo{tipo='" + tipo + "', posicion=" + posicion +
                ", vida=" + vidaPersonalizada + ", mirandoDerecha=" + mirandoDerecha + "}";
        }
    }

    /**
     * Clase para almacenar información de puertas.
     */
    public static class PuertaInfo {
        /** Posición de la puerta en el mapa */
        public Vector2 posicion;
        /** Tamaño del área interactiva */
        public Vector2 tamaño;
        /** ID del nivel que desbloquea */
        public String idNivel;
        /** Nombre descriptivo de la puerta */
        public String nombre;
        /** Indica si la puerta está activa */
        public boolean activa;
        /** Indica si la puerta está bloqueada */
        public boolean bloqueada;
        /** Ruta del mapa a cargar al interactuar */
        public String mapaDestino;

        public PuertaInfo(Vector2 posicion, Vector2 tamaño,
                          String idNivel, String nombre,
                          boolean activa, boolean bloqueada, String mapaDestino) {
            this.posicion = posicion;
            this.tamaño = tamaño;
            this.idNivel = idNivel;
            this.nombre = nombre;
            this.activa = activa;
            this.bloqueada = bloqueada;
            this.mapaDestino = mapaDestino;
        }

        @Override
        public String toString() {
            return "PuertaInfo{" +
                "posicion=" + posicion +
                ", tamaño=" + tamaño +
                ", idNivel='" + idNivel + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activa=" + activa +
                ", bloqueada=" + bloqueada +
                ", mapaDestino='" + mapaDestino + '\'' +
                '}';
        }
    }

    /**
     * Clase para almacenar información de indicadores de nivel
     * ¡NUEVA CLASE! Para el sistema de indicadores de final de nivel.
     */
    public static class IndicadorSpawnInfo {
        public Vector2 posicion;
        public Vector2 tamaño;
        public String nombre;

        public IndicadorSpawnInfo(Vector2 posicion, Vector2 tamaño, String nombre) {
            this.posicion = posicion;
            this.tamaño = tamaño;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "IndicadorSpawnInfo{posicion=" + posicion +
                ", tamaño=" + tamaño + ", nombre='" + nombre + "'}";
        }
    }

    // ====================== GETTERS ======================

    public boolean estaCargado() { return cargado; }
    public int getNumeroColisiones() { return colisiones.size; }
    public boolean isDebugColisiones() { return debugColisiones; }
}

