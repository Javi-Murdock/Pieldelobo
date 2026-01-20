package io.github.javiergames.pieldelobo.Puertas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import io.github.javiergames.pieldelobo.Graficos.GameUtils;
import io.github.javiergames.pieldelobo.Personajes.PersonajeLobby;
/**
 * Actor que representa una puerta/zona de transición en el lobby.
 * Es invisible y solo sirve para detección de colisiones.
 * Controla el acceso a diferentes niveles del juego.
 *
 * @author Javier Gala
 * @version 2.0
 */
public class PuertaTransicion extends Actor {

    private Rectangle hitbox;
    private String idNivel;
    private String nombreMostrar;
    private String mapaDestino;
    private boolean activa = true;
    private boolean bloqueada = false;

    // Nuevo: control de visibilidad
    private boolean visible = false; // Por defecto INVISIBLE

    // Propiedades visuales (mantenidas por compatibilidad)
    private Color colorBase = new Color(0.2f, 0.4f, 0.8f, 0.8f);
    private Color colorBloqueado = new Color(0.5f, 0.2f, 0.2f, 0.6f);
    private Color colorInactivo = new Color(0.3f, 0.3f, 0.3f, 0.4f);

    /**
     * Constructor de una puerta de transición.
     *
     * @param x Posición X de la puerta
     * @param y Posición Y de la puerta
     * @param ancho Ancho de la puerta
     * @param alto Alto de la puerta
     * @param idNivel ID único del nivel al que lleva la puerta
     * @param nombreMostrar Nombre descriptivo para mostrar al jugador
     * @param activa Estado inicial de activación
     * @param bloqueada Estado inicial de bloqueo
     * @param mapaDestino Ruta del archivo .tmx del mapa a cargar
     */
    public PuertaTransicion(float x, float y, float ancho, float alto,
                            String idNivel, String nombreMostrar, boolean activa,
                            boolean bloqueada, String mapaDestino) {
        this.idNivel = idNivel;
        this.nombreMostrar = nombreMostrar;
        this.activa = activa;
        this.bloqueada = bloqueada;
        this.mapaDestino = mapaDestino;

        setPosition(x, y);
        setSize(ancho, alto);

        // Hitbox para detección de colisiones
        float margen = 15f;
        this.hitbox = new Rectangle(x - margen, y - margen,
            ancho + margen * 2, alto + margen * 2);

        Gdx.app.log("PuertaTransicion", "Creada (INVISIBLE): " + nombreMostrar +
            " en [" + x + "," + y + "] activa: " + activa +
            " bloqueada: " + bloqueada);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar hitbox con posición actual
        float margen = 15f;
        hitbox.setPosition(getX() - margen, getY() - margen);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // NO DIBUJAR NADA - solo es una zona de colisión invisible
        // Comentar todo el código de dibujo original
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
    /**
     * Verifica si hay colisión entre la puerta y el jugador.
     *
     * @param jugador Personaje del jugador
     * @return true si hay colisión, false en caso contrario
     */
    public boolean colisionaConJugador(PersonajeLobby jugador) {
        return hitbox.overlaps(jugador.getHitbox());
    }
    /**
     * Obtiene el ID del nivel asociado a esta puerta.
     *
     * @return ID del nivel
     */
    public String getIdNivel() {
        return idNivel;
    }
    /**
     * Obtiene el nombre descriptivo de la puerta.
     *
     * @return Nombre para mostrar
     */

    public String getNombreMostrar() {
        return nombreMostrar;
    }
    /**
     * Obtiene la ruta del mapa destino.
     *
     * @return Ruta del archivo .tmx
     */

    public String getMapaDestino() {
        return mapaDestino;
    }
    /**
     * Verifica si la puerta está activa y no bloqueada.
     *
     * @return true si está activa y disponible
     */

    public boolean isActiva() {
        return activa && !bloqueada;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public boolean isBloqueada() {
        return bloqueada;
    }

    public void setBloqueada(boolean bloqueada) {
        this.bloqueada = bloqueada;
    }

    /**
     * Controla el estado de visibilidad de la puerta.
     *
     * @param visible true para hacer visible, false para invisible
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    /**
     * Verifica si la puerta es visible.
     *
     * @return Estado de visibilidad
     */
    public boolean isVisible() {
        return visible;
    }

    public void dispose() {
        // No hay recursos específicos que liberar
    }

    // Métodos para cambiar colores (mantenidos por compatibilidad)
    public void setColorBase(Color color) {
        this.colorBase = color;
    }

    public void setColorBloqueado(Color color) {
        this.colorBloqueado = color;
    }

    public void setColorInactivo(Color color) {
        this.colorInactivo = color;
    }
}
