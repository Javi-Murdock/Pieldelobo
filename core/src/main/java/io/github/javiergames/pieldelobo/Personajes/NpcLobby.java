package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

import io.github.javiergames.pieldelobo.Dialogos.DialogoManager;
import io.github.javiergames.pieldelobo.GestorJuego.GameState;
import io.github.javiergames.pieldelobo.GestorJuego.SistemaProgresion;
import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * NPC controlado por IA para el lobby con animaciones completas y colisiones.
 * MEJORADO: Ahora cambia diálogo automáticamente según el progreso del jugador.
 * CONECTADO: Al SistemaProgresion para determinar qué diálogo mostrar.
 *
 *  * @author Javier Gala
 *  * @version 3.1
 */
public class NpcLobby extends Actor {
    private TextureAtlas atlas;

    // Animaciones completas como el protagonista
    private Animation<TextureRegion> animacionAndarArriba;
    private Animation<TextureRegion> animacionAndarAbajo;
    private Animation<TextureRegion> animacionAndarIzquierda;
    private Animation<TextureRegion> animacionAndarDerecha;

    // Animaciones idle
    private Animation<TextureRegion> animacionIdleArriba;
    private Animation<TextureRegion> animacionIdleAbajo;
    private Animation<TextureRegion> animacionIdleIzquierda;
    private Animation<TextureRegion> animacionIdleDerecha;

    // Animaciones actuales
    private Animation<TextureRegion> animacionAndar;
    private Animation<TextureRegion> animacionIdle;

    private TextureRegion frameActual;
    private float tiempoAnimacion = 0;

    private MapaManager mapaManager;
    private Random random;
    private float tiempoCambioDireccion = 0;
    private float velocidad = 40f;
    private float velocidadX = 0;
    private float velocidadY = 0;

    // Estados del NPC
    private enum Estado { IDLE, ANDANDO }
    private Estado estadoActual = Estado.IDLE;
    private PersonajeLobby.Direccion direccionActual = PersonajeLobby.Direccion.ABAJO;

    // Configuración de movimiento - MÁRGENES AJUSTADOS
    private static final float TIEMPO_CAMBIO_DIRECCION = 3f;
    private static final float PROBABILIDAD_MOVER = 0.7f;
    private static final float MARGEN_HITBOX_HORIZONTAL = 18f;
    private static final float MARGEN_HITBOX_VERTICAL = 14f;

    // Estado de pausa para diálogos
    private boolean pausado = false;

    // ====================== DIÁLOGOS MEJORADOS ======================
    private String tipoNPC;
    private boolean tieneDialogo = false;
    private String idDialogo = "";
    private float rangoInteraccion = 70f;

    // Para determinar qué diálogo mostrar según progreso
    private java.util.Map<String, String> dialogosPorPuerta;

    /**
     * Constructor del NPC.
     * MEJORADO: Ahora asigna diálogo automáticamente basado en el progreso.
     */
    public NpcLobby(String tipoNpc, float x, float y) {
        this.tipoNPC = tipoNpc;
        this.random = new Random();

        // 1. Cargar animaciones
        cargarAnimacionesCompletas(tipoNpc);

        // 2. Configurar posición y tamaño
        setPosition(x, y);
        setSize(frameActual.getRegionWidth(), frameActual.getRegionHeight());

        // 3. Inicializar sistema de diálogos
        inicializarSistemaDialogos();

        // 4. Obtener diálogo actual basado en progreso
        actualizarDialogoSegunProgreso();

        Gdx.app.log("NpcLobby", "NPC creado: " + tipoNPC +
            " en " + x + ", " + y +
            " diálogo: " + idDialogo);
    }

    /**
     * Inicializa el sistema de diálogos para este NPC
     */
    private void inicializarSistemaDialogos() {
        dialogosPorPuerta = new java.util.HashMap<>();

        // Configurar qué diálogo mostrar en cada puerta para cada tipo de NPC
        switch (tipoNPC.toLowerCase()) {
            case "profesor":
                // Profesor Leiva - Diálogos principales
                dialogosPorPuerta.put("puerta_1", "profesor_leiva_inicio");
                dialogosPorPuerta.put("puerta_2", "profesor_leiva_puerta2");
                dialogosPorPuerta.put("puerta_3", "profesor_leiva_puerta3");
                dialogosPorPuerta.put("puerta_4", "profesor_leiva_puerta4");
                dialogosPorPuerta.put("puerta_5", "profesor_leiva_puerta5");
                rangoInteraccion = 150f; // NPC principal
                break;

            case "ciber":
                // Profesor Vega - Desbloquea nivel 2
                dialogosPorPuerta.put("puerta_1", "profesor_vega_inicio");
                dialogosPorPuerta.put("puerta_2", "profesor_vega_puerta2");
                dialogosPorPuerta.put("puerta_3", "profesor_vega_puerta3");
                dialogosPorPuerta.put("puerta_4", "profesor_vega_puerta4");
                dialogosPorPuerta.put("puerta_5", "profesor_vega_puerta5");
                rangoInteraccion = 120f; // Importante para desbloquear nivel 2
                break;

            case "doctora":
                // Doctora García - Desbloquea nivel 3
                dialogosPorPuerta.put("puerta_1", "doctora_garcia_inicio");
                dialogosPorPuerta.put("puerta_2", "doctora_garcia_puerta2_bien");
                dialogosPorPuerta.put("puerta_3", "doctora_garcia_puerta3");
                dialogosPorPuerta.put("puerta_4", "doctora_garcia_puerta4");
                dialogosPorPuerta.put("puerta_5", "doctora_garcia_puerta5");
                rangoInteraccion = 120f; // Importante para desbloquear nivel 3
                break;

            case "doctor":
                // Doctor Salazar - Desbloquea nivel 5
                dialogosPorPuerta.put("puerta_1", "doctor_salazar_inicio");
                dialogosPorPuerta.put("puerta_2", "doctor_salazar_puerta2");
                dialogosPorPuerta.put("puerta_3", "doctor_salazar_puerta3");
                dialogosPorPuerta.put("puerta_4", "doctor_salazar_puerta4");
                dialogosPorPuerta.put("puerta_5", "doctor_salazar_puerta5");
                rangoInteraccion = 120f; // Importante para desbloquear nivel 5
                break;

            case "medico":
                // Marta Santos - NPC secundario
                dialogosPorPuerta.put("puerta_1", "marta_santos_inicio");
                dialogosPorPuerta.put("puerta_2", "marta_santos_puerta2");
                dialogosPorPuerta.put("puerta_3", "marta_santos_puerta3");
                dialogosPorPuerta.put("puerta_4", "marta_santos_puerta4");
                dialogosPorPuerta.put("puerta_5", "marta_santos_puerta5");
                rangoInteraccion = 80f; // NPC secundario
                break;

            case "senor":
                // José Castellanos - Diálogo final
                dialogosPorPuerta.put("puerta_1", "jose_castellanos_inicio");
                dialogosPorPuerta.put("puerta_2", "jose_castellanos_puerta2");
                dialogosPorPuerta.put("puerta_3", "jose_castellanos_puerta3");
                dialogosPorPuerta.put("puerta_4", "jose_castellanos_puerta4");
                dialogosPorPuerta.put("puerta_5", "jose_castellanos_puerta5");
                rangoInteraccion = 100f; // NPC especial
                break;

            default:
                // NPC por defecto
                DialogoManager dialogoManager = DialogoManager.getInstance();
                idDialogo = dialogoManager.getDialogoIdPorTipo(tipoNPC);
                rangoInteraccion = 70f;
                break;
        }

        Gdx.app.log("NpcLobby", "Sistema de diálogos inicializado para: " + tipoNPC);
    }

    /**
     * Actualiza el diálogo del NPC según el progreso actual del jugador
     * Se debe llamar cuando el jugador vuelve al lobby después de completar un nivel
     */
    public void actualizarDialogoSegunProgreso() {
        String puertaActual = determinarPuertaActual();
        String nuevoDialogo = dialogosPorPuerta.get(puertaActual);

        // Si no hay diálogo específico para esta puerta, usar el por defecto
        if (nuevoDialogo == null) {
            DialogoManager dialogoManager = DialogoManager.getInstance();
            nuevoDialogo = dialogoManager.getDialogoIdPorTipo(tipoNPC);
        }

        // Solo actualizar si el diálogo cambió
        if (nuevoDialogo != null && !nuevoDialogo.equals(this.idDialogo)) {
            this.idDialogo = nuevoDialogo;
            this.tieneDialogo = (this.idDialogo != null && !this.idDialogo.isEmpty());

            Gdx.app.log("NpcLobby", "🔄 Diálogo actualizado:");
            Gdx.app.log("NpcLobby", "   • NPC: " + tipoNPC);
            Gdx.app.log("NpcLobby", "   • Puerta: " + puertaActual);
            Gdx.app.log("NpcLobby", "   • Diálogo: " + idDialogo);

            // Verificar si este es el NPC de la próxima misión
            SistemaProgresion progresion = SistemaProgresion.getInstance();
            String npcSiguienteMision = progresion.getNpcSiguienteMision();

            if (npcSiguienteMision != null && npcSiguienteMision.contains(tipoNPC)) {
                Gdx.app.log("NpcLobby", "   ⚠️ Este NPC tiene la próxima misión!");
                Gdx.app.log("NpcLobby", "   • Diálogo de misión: " + progresion.getDialogoSiguienteMision());
            }
        }
    }

    /**
     * Determina en qué puerta está el jugador basado en su progreso
     */
    private String determinarPuertaActual() {
        GameState gameState = GameState.getInstance();
        SistemaProgresion progresion = SistemaProgresion.getInstance();

        // Verificar niveles completados para determinar la puerta actual
        if (gameState.isNivelCompletado("nivel_5")) {
            return "puerta_5";
        } else if (gameState.isNivelCompletado("nivel_4")) {
            return "puerta_5";
        } else if (gameState.isNivelCompletado("nivel_3")) {
            return "puerta_4";
        } else if (gameState.isNivelCompletado("nivel_2")) {
            return "puerta_3";
        } else if (gameState.isNivelCompletado("nivel_1")) {
            return "puerta_2";
        } else {
            return "puerta_1";
        }
    }

    /**
     * Verifica si este NPC es el que debe dar la próxima misión
     */
    public boolean esNpcProximaMision() {
        SistemaProgresion progresion = SistemaProgresion.getInstance();
        String npcSiguienteMision = progresion.getNpcSiguienteMision();

        if (npcSiguienteMision == null) return false;

        // Verificar si el nombre del NPC coincide
        return npcSiguienteMision.toLowerCase().contains(tipoNPC.toLowerCase());
    }

    /**
     * Obtiene el diálogo de misión para este NPC (si es el NPC correcto)
     */
    public String getDialogoMision() {
        if (esNpcProximaMision()) {
            SistemaProgresion progresion = SistemaProgresion.getInstance();
            return progresion.getDialogoSiguienteMision();
        }
        return idDialogo; // Si no es NPC de misión, usar diálogo normal
    }

    // ====================== ANIMACIONES (CÓDIGO EXISTENTE) ======================

    private void cargarAnimacionesCompletas(String tipoNpc) {
        try {
            atlas = new TextureAtlas(Gdx.files.internal("Personajes_Laboratorio.atlas"));
            String prefix = obtenerPrefijoNpc(tipoNpc);

            // Animaciones idle
            Array<TextureRegion> idleArriba = new Array<>();
            idleArriba.add(atlas.findRegion(prefix + "001"));
            animacionIdleArriba = new Animation<>(0.3f, idleArriba);

            Array<TextureRegion> idleAbajo = new Array<>();
            idleAbajo.add(atlas.findRegion(prefix + "007"));
            animacionIdleAbajo = new Animation<>(0.3f, idleAbajo);

            Array<TextureRegion> idleIzquierda = new Array<>();
            idleIzquierda.add(atlas.findRegion(prefix + "010"));
            animacionIdleIzquierda = new Animation<>(0.3f, idleIzquierda);

            Array<TextureRegion> idleDerecha = new Array<>();
            idleDerecha.add(atlas.findRegion(prefix + "004"));
            animacionIdleDerecha = new Animation<>(0.3f, idleDerecha);

            animacionIdle = animacionIdleAbajo;

            // Animaciones andar
            Array<TextureRegion> andarArriba = new Array<>();
            andarArriba.add(atlas.findRegion(prefix + "000"));
            andarArriba.add(atlas.findRegion(prefix + "001"));
            andarArriba.add(atlas.findRegion(prefix + "002"));
            animacionAndarArriba = new Animation<>(0.2f, andarArriba);

            Array<TextureRegion> andarAbajo = new Array<>();
            andarAbajo.add(atlas.findRegion(prefix + "006"));
            andarAbajo.add(atlas.findRegion(prefix + "007"));
            andarAbajo.add(atlas.findRegion(prefix + "008"));
            animacionAndarAbajo = new Animation<>(0.15f, andarAbajo);

            Array<TextureRegion> andarIzquierda = new Array<>();
            andarIzquierda.add(atlas.findRegion(prefix + "009"));
            andarIzquierda.add(atlas.findRegion(prefix + "010"));
            andarIzquierda.add(atlas.findRegion(prefix + "011"));
            animacionAndarIzquierda = new Animation<>(0.15f, andarIzquierda);

            Array<TextureRegion> andarDerecha = new Array<>();
            andarDerecha.add(atlas.findRegion(prefix + "003"));
            andarDerecha.add(atlas.findRegion(prefix + "004"));
            andarDerecha.add(atlas.findRegion(prefix + "005"));
            animacionAndarDerecha = new Animation<>(0.15f, andarDerecha);

            animacionAndar = animacionAndarAbajo;
            frameActual = animacionIdleAbajo.getKeyFrame(0);

        } catch (Exception e) {
            Gdx.app.error("NpcLobby", "Error al cargar animaciones para NPC: " + tipoNpc, e);
            frameActual = new TextureRegion();
        }
    }

    private String obtenerPrefijoNpc(String tipoNpc) {
        switch (tipoNpc.toLowerCase()) {
            case "doctor": return "Doctor_laboratorio";
            case "doctora": return "Doctora_laboratorio";
            case "medico": return "Medico_laboratorio";
            case "profesor": return "Profesor_laboratorio";
            case "senor": return "Senor_laboratorio";
            case "ciber": return "Ciber_laboratorio";
            default: return "Medico_laboratorio";
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!pausado) {
            actualizarIA(delta);
            actualizarAnimacion(delta);
            aplicarMovimiento(delta);
        } else {
            tiempoAnimacion += delta;
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    private void actualizarIA(float delta) {
        tiempoCambioDireccion += delta;

        if (tiempoCambioDireccion >= TIEMPO_CAMBIO_DIRECCION) {
            tiempoCambioDireccion = 0;

            if (random.nextFloat() < PROBABILIDAD_MOVER) {
                estadoActual = Estado.ANDANDO;
                int dir = random.nextInt(8);

                switch (dir) {
                    case 0:
                        velocidadX = 0; velocidadY = velocidad;
                        direccionActual = PersonajeLobby.Direccion.ARRIBA; break;
                    case 1:
                        velocidadX = 0; velocidadY = -velocidad;
                        direccionActual = PersonajeLobby.Direccion.ABAJO; break;
                    case 2:
                        velocidadX = -velocidad; velocidadY = 0;
                        direccionActual = PersonajeLobby.Direccion.IZQUIERDA; break;
                    case 3:
                        velocidadX = velocidad; velocidadY = 0;
                        direccionActual = PersonajeLobby.Direccion.DERECHA; break;
                    case 4:
                        velocidadX = -velocidad * 0.7f; velocidadY = velocidad * 0.7f;
                        direccionActual = PersonajeLobby.Direccion.ARRIBA_IZQUIERDA; break;
                    case 5:
                        velocidadX = velocidad * 0.7f; velocidadY = velocidad * 0.7f;
                        direccionActual = PersonajeLobby.Direccion.ARRIBA_DERECHA; break;
                    case 6:
                        velocidadX = -velocidad * 0.7f; velocidadY = -velocidad * 0.7f;
                        direccionActual = PersonajeLobby.Direccion.ABAJO_IZQUIERDA; break;
                    case 7:
                        velocidadX = velocidad * 0.7f; velocidadY = -velocidad * 0.7f;
                        direccionActual = PersonajeLobby.Direccion.ABAJO_DERECHA; break;
                }
            } else {
                estadoActual = Estado.IDLE;
                velocidadX = 0; velocidadY = 0;
                actualizarAnimacionIdlePorDireccion();
            }
        }
    }

    private void aplicarMovimiento(float delta) {
        if (estadoActual == Estado.ANDANDO) {
            float xAnterior = getX();
            float yAnterior = getY();

            setX(getX() + velocidadX * delta);
            setY(getY() + velocidadY * delta);

            if (mapaManager != null && mapaManager.hayColision(getHitbox())) {
                setX(xAnterior); setY(yAnterior);
                estadoActual = Estado.IDLE;
                velocidadX = 0; velocidadY = 0;
                actualizarAnimacionIdlePorDireccion();
            }

            mantenerDentroMapa();
        }
    }

    private void mantenerDentroMapa() {
        if (mapaManager == null) return;

        float minX = 0, minY = 0;
        float maxX = mapaManager.getAnchoMapa() - getWidth();
        float maxY = mapaManager.getAltoMapa() - getHeight();

        if (getX() < minX) setX(minX);
        if (getY() < minY) setY(minY);
        if (getX() > maxX) setX(maxX);
        if (getY() > maxY) setY(maxY);
    }

    private void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        if (estadoActual == Estado.ANDANDO) {
            actualizarAnimacionAndarPorDireccion();
            frameActual = animacionAndar.getKeyFrame(tiempoAnimacion, true);
        } else {
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    private void actualizarAnimacionAndarPorDireccion() {
        switch (direccionActual) {
            case ARRIBA: case ARRIBA_IZQUIERDA: case ARRIBA_DERECHA:
                animacionAndar = animacionAndarArriba; break;
            case ABAJO: case ABAJO_IZQUIERDA: case ABAJO_DERECHA:
                animacionAndar = animacionAndarAbajo; break;
            case IZQUIERDA:
                animacionAndar = animacionAndarIzquierda; break;
            case DERECHA:
                animacionAndar = animacionAndarDerecha; break;
        }
    }

    private void actualizarAnimacionIdlePorDireccion() {
        switch (direccionActual) {
            case ARRIBA: case ARRIBA_IZQUIERDA: case ARRIBA_DERECHA:
                animacionIdle = animacionIdleArriba; break;
            case ABAJO: case ABAJO_IZQUIERDA: case ABAJO_DERECHA:
                animacionIdle = animacionIdleAbajo; break;
            case IZQUIERDA:
                animacionIdle = animacionIdleIzquierda; break;
            case DERECHA:
                animacionIdle = animacionIdleDerecha; break;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (frameActual != null) {
            Color colorOriginal = batch.getColor();
            batch.setColor(1, 1, 1, 1);
            batch.draw(frameActual, getX(), getY(), getWidth(), getHeight());
            batch.setColor(colorOriginal);
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(
            getX() + MARGEN_HITBOX_HORIZONTAL,
            getY() + MARGEN_HITBOX_VERTICAL,
            getWidth() - MARGEN_HITBOX_HORIZONTAL * 2,
            getHeight() - MARGEN_HITBOX_VERTICAL * 2
        );
    }

    public boolean hayColisionCon(NpcLobby otroNpc) {
        return getHitbox().overlaps(otroNpc.getHitbox());
    }

    public boolean hayColisionCon(PersonajeLobby jugador) {
        return getHitbox().overlaps(jugador.getHitbox());
    }

    public void setMapaManager(MapaManager mapaManager) {
        this.mapaManager = mapaManager;
    }

    public void dispose() {
        if (atlas != null) {
            atlas.dispose();
            Gdx.app.log("NpcLobby", "Recursos del NPC liberados");
        }
    }

    // ====================== GETTERS ======================

    public PersonajeLobby.Direccion getDireccionActual() {
        return direccionActual;
    }

    public boolean estaAndando() {
        return estadoActual == Estado.ANDANDO;
    }

    // ====================== DIÁLOGOS ======================

    public boolean puedeInteractuar(PersonajeLobby jugador) {
        if (!tieneDialogo || pausado) return false;

        float dx = getX() - jugador.getX();
        float dy = getY() - jugador.getY();
        float distancia = (float) Math.sqrt(dx * dx + dy * dy);

        boolean enRango = distancia <= rangoInteraccion;

        if (enRango && Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_DEBUG) {
            Gdx.app.debug("NpcLobby", "NPC '" + idDialogo + "' en rango: " +
                String.format("%.1f", distancia) + "/" + rangoInteraccion);
        }

        return enRango;
    }

    public String getIdDialogo() {
        return idDialogo;
    }

    public boolean tieneDialogo() {
        return tieneDialogo;
    }

    public void setRangoInteraccion(float rango) {
        this.rangoInteraccion = rango;
    }

    public String getTipoNPC() {
        return tipoNPC;
    }

    // ====================== PAUSA PARA DIÁLOGOS ======================

    public void setPausado(boolean pausado) {
        this.pausado = pausado;
        if (pausado) {
            velocidadX = 0; velocidadY = 0;
            estadoActual = Estado.IDLE;
            actualizarAnimacionIdlePorDireccion();
            tiempoAnimacion = 0;
            Gdx.app.log("NpcLobby", "NPC pausado");
        } else {
            Gdx.app.log("NpcLobby", "NPC reanudado");
        }
    }

    public boolean estaPausado() {
        return pausado;
    }

    /**
     * NUEVO: Verifica si este NPC tiene un diálogo de misión pendiente
     */
    public boolean tieneMisionPendiente() {
        return esNpcProximaMision();
    }

    /**
     * NUEVO: Obtiene información sobre el diálogo de este NPC
     */
    public String getInfoDialogo() {
        StringBuilder info = new StringBuilder();
        info.append("NPC: ").append(tipoNPC).append("\n");
        info.append("Diálogo: ").append(idDialogo).append("\n");
        info.append("Tiene diálogo: ").append(tieneDialogo).append("\n");

        if (esNpcProximaMision()) {
            info.append("⚠️ TIENE MISIÓN PENDIENTE\n");
            SistemaProgresion progresion = SistemaProgresion.getInstance();
            info.append("   • Diálogo misión: ").append(progresion.getDialogoSiguienteMision()).append("\n");
            info.append("   • Video: ").append(progresion.getVideoSiguienteMision()).append("\n");
            info.append("   • Desbloquea: ").append(progresion.getNivelSiguienteMision());
        }

        return info.toString();
    }
}
