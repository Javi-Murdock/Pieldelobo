package io.github.javiergames.pieldelobo.Puertas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;

import io.github.javiergames.pieldelobo.Graficos.GameUtils;
import io.github.javiergames.pieldelobo.Personajes.PersonajeLobby;

/**
 * Icono que aparece sobre las puertas cuando el jugador se acerca.
 * Muestra un candado con diferentes estados visuales (cerrado, abierto, inactivo)
 * según el estado de la puerta asociada.
 *
 * @author Javier Gala
 * @version 2.0
 * @see PuertaTransicion
 * @see PersonajeLobby
 */
public class IconoInteraccion extends Actor {

    private Rectangle areaInteraccion;
    private boolean mostrar = false;
    private float tiempoAnimacion = 0f;

    // Referencia a la puerta
    private PuertaTransicion puerta;

    // Fuente para el icono
    private BitmapFont font;
    private boolean usarFuente = false;
    /**
     * Constructor que crea un icono de interacción para una puerta específica.
     *
     * @param puerta La puerta a la que está asociado este icono
     */
    public IconoInteraccion(PuertaTransicion puerta) {
        this.puerta = puerta;

        // Área alrededor de la puerta para mostrar el icono
        float margen = 30f;
        this.areaInteraccion = new Rectangle(
            puerta.getX() - margen,
            puerta.getY() - margen,
            puerta.getWidth() + margen * 2,
            puerta.getHeight() + margen * 2
        );

        setPosition(areaInteraccion.x, areaInteraccion.y);
        setSize(areaInteraccion.width, areaInteraccion.height);

        // Intentar cargar fuente para texto de estado
        try {
            font = new BitmapFont();
            font.getData().setScale(1.5f);
            usarFuente = true;
        } catch (Exception e) {
            usarFuente = false;
            font = null;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar animación solo si se está mostrando
        if (mostrar) {
            tiempoAnimacion += delta;
        }

        // Actualizar posición si la puerta se mueve
        float margen = 30f;
        areaInteraccion.setPosition(
            puerta.getX() - margen,
            puerta.getY() - margen
        );

        setPosition(areaInteraccion.x, areaInteraccion.y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (!mostrar) return;

        float centroX = puerta.getX() + puerta.getWidth() / 2;
        float parteSuperior = puerta.getY() + puerta.getHeight();
        float iconoY = parteSuperior + 15f; // Posición sobre la puerta

        // Animación de flotación
        float offsetY = (float)Math.sin(tiempoAnimacion * 3f) * 3f;
        iconoY += offsetY;

        // Color según estado de la puerta
        Color colorIcono;
        String estadoTexto = "";

        if (puerta.isBloqueada()) {
            colorIcono = Color.RED; // Bloqueada
            estadoTexto = "BLOQUEADA";
        } else if (puerta.isActiva()) {
            colorIcono = Color.GREEN; // Disponible
            estadoTexto = "ENTRAR";
        } else {
            colorIcono = Color.GRAY; // Inactiva
            estadoTexto = "INACTIVA";
        }

        // Dibujar icono circular
        float radio = 12f;
        Color originalColor = batch.getColor();

        // Fondo del icono (con transparencia)
        batch.setColor(colorIcono.r, colorIcono.g, colorIcono.b, 0.8f);
        batch.draw(GameUtils.getWhitePixel(),
            centroX - radio, iconoY - radio,
            radio * 2, radio * 2);

        // Borde blanco del icono
        batch.setColor(1, 1, 1, 0.9f);
        float grosorBorde = 2f;
        batch.draw(GameUtils.getWhitePixel(),
            centroX - radio - grosorBorde, iconoY - radio - grosorBorde,
            radio * 2 + grosorBorde * 2, grosorBorde); // Inferior
        batch.draw(GameUtils.getWhitePixel(),
            centroX - radio - grosorBorde, iconoY + radio,
            radio * 2 + grosorBorde * 2, grosorBorde); // Superior
        batch.draw(GameUtils.getWhitePixel(),
            centroX - radio - grosorBorde, iconoY - radio,
            grosorBorde, radio * 2); // Izquierdo
        batch.draw(GameUtils.getWhitePixel(),
            centroX + radio, iconoY - radio,
            grosorBorde, radio * 2); // Derecho

        // DIBUJAR CANDADO en lugar de la letra "E"
        batch.setColor(1, 1, 1, 1); // Color blanco para el candado

        // Tamaños del candado
        float anchoCandado = 12f;  // Ancho total del candado
        float altoCandado = 14f;   // Alto total del candado
        float grosor = 2f;         // Grosor de las líneas

        // Posición del candado (centrado)
        float candadoX = centroX - anchoCandado/2;
        float candadoY = iconoY - altoCandado/2;

        if (puerta.isBloqueada()) {
            // CANDADO CERRADO
            dibujarCandadoCerrado(batch, candadoX, candadoY, anchoCandado, altoCandado, grosor);
        } else if (puerta.isActiva()) {
            // CANDADO ABIERTO
            dibujarCandadoAbierto(batch, candadoX, candadoY, anchoCandado, altoCandado, grosor);
        } else {
            // CANDADO INACTIVO (medio abierto o tachado)
            dibujarCandadoInactivo(batch, candadoX, candadoY, anchoCandado, altoCandado, grosor);
        }

        // Opcional: Mostrar texto de estado debajo del icono
        if (usarFuente && font != null) {
            font.getData().setScale(0.8f);
            batch.setColor(colorIcono);
            font.draw(batch, estadoTexto,
                centroX - (estadoTexto.length() * 3),
                iconoY - 25);
            font.getData().setScale(1.5f);
        }

        batch.setColor(originalColor);
    }

    /**
     * Dibuja un candado cerrado en la posición especificada.
     *
     * @param batch El batch usado para dibujar
     * @param x Posición X del candado
     * @param y Posición Y del candado
     * @param ancho Ancho del candado
     * @param alto Alto del candado
     * @param grosor Grosor de las líneas del candado
     */
    private void dibujarCandadoCerrado(Batch batch, float x, float y, float ancho, float alto, float grosor) {
        // Arco superior del candado
        float radioArco = ancho / 2.5f;
        float centroArcoX = x + ancho/2;
        float baseArcoY = y + alto * 0.7f;

        // Dibujar arco (semicírculo)
        for (int i = 0; i < 16; i++) {
            float angulo = (float)i * (3.14159f / 16f); // De 0 a PI
            float px = centroArcoX + (float)Math.cos(angulo) * radioArco;
            float py = baseArcoY + (float)Math.sin(angulo) * radioArco;
            batch.draw(GameUtils.getWhitePixel(), px - grosor/2, py - grosor/2, grosor, grosor);
        }

        // Cuerpo rectangular del candado
        float cuerpoAlto = alto * 0.7f;
        batch.draw(GameUtils.getWhitePixel(),
            x, y,
            ancho, grosor); // Base inferior
        batch.draw(GameUtils.getWhitePixel(),
            x, y + cuerpoAlto - grosor,
            ancho, grosor); // Base superior
        batch.draw(GameUtils.getWhitePixel(),
            x, y,
            grosor, cuerpoAlto); // Lado izquierdo
        batch.draw(GameUtils.getWhitePixel(),
            x + ancho - grosor, y,
            grosor, cuerpoAlto); // Lado derecho

        // Cerradura (círculo pequeño en el centro)
        float radioCerradura = grosor * 1.5f;
        batch.draw(GameUtils.getWhitePixel(),
            x + ancho/2 - radioCerradura, y + cuerpoAlto * 0.3f - radioCerradura,
            radioCerradura * 2, radioCerradura * 2);
    }

    /**
     * Dibuja un candado abierto en la posición especificada.
     *
     * @param batch El batch usado para dibujar
     * @param x Posición X del candado
     * @param y Posición Y del candado
     * @param ancho Ancho del candado
     * @param alto Alto del candado
     * @param grosor Grosor de las líneas del candado
     */
    private void dibujarCandadoAbierto(Batch batch, float x, float y, float ancho, float alto, float grosor) {
        // Arco superior del candado (abierto)
        float radioArco = ancho / 2.5f;
        float centroArcoX = x + ancho/2;
        float baseArcoY = y + alto * 0.7f;

        // Dibujar arco abierto (menos de semicírculo)
        for (int i = 3; i < 13; i++) {
            float angulo = (float)i * (3.14159f / 16f);
            float px = centroArcoX + (float)Math.cos(angulo) * radioArco;
            float py = baseArcoY + (float)Math.sin(angulo) * radioArco;
            batch.draw(GameUtils.getWhitePixel(), px - grosor/2, py - grosor/2, grosor, grosor);
        }

        // Cuerpo rectangular del candado
        float cuerpoAlto = alto * 0.7f;
        batch.draw(GameUtils.getWhitePixel(),
            x, y,
            ancho, grosor); // Base inferior
        batch.draw(GameUtils.getWhitePixel(),
            x, y + cuerpoAlto - grosor,
            ancho, grosor); // Base superior
        batch.draw(GameUtils.getWhitePixel(),
            x, y,
            grosor, cuerpoAlto); // Lado izquierdo
        batch.draw(GameUtils.getWhitePixel(),
            x + ancho - grosor, y,
            grosor, cuerpoAlto); // Lado derecho

        // Cerradura abierta (línea diagonal)
        batch.draw(GameUtils.getWhitePixel(),
            x + ancho/2 - grosor, y + cuerpoAlto * 0.3f,
            grosor * 2, grosor);
    }

    /**
     * Dibuja un candado inactivo (punteado/tachado) en la posición especificada.
     *
     * @param batch El batch usado para dibujar
     * @param x Posición X del candado
     * @param y Posición Y del candado
     * @param ancho Ancho del candado
     * @param alto Alto del candado
     * @param grosor Grosor de las líneas del candado
     */
    private void dibujarCandadoInactivo(Batch batch, float x, float y, float ancho, float alto, float grosor) {
        // Arco superior desdibujado
        float radioArco = ancho / 2.5f;
        float centroArcoX = x + ancho/2;
        float baseArcoY = y + alto * 0.7f;

        // Dibujar arco punteado
        for (int i = 0; i < 16; i += 3) {
            float angulo = (float)i * (3.14159f / 16f);
            float px = centroArcoX + (float)Math.cos(angulo) * radioArco;
            float py = baseArcoY + (float)Math.sin(angulo) * radioArco;
            batch.draw(GameUtils.getWhitePixel(), px - grosor/2, py - grosor/2, grosor, grosor);
        }

        // Cuerpo rectangular punteado
        float cuerpoAlto = alto * 0.7f;

        // Base inferior punteada
        for (float i = 0; i < ancho; i += grosor * 2) {
            batch.draw(GameUtils.getWhitePixel(), x + i, y, grosor, grosor);
        }

        // Base superior punteada
        for (float i = 0; i < ancho; i += grosor * 2) {
            batch.draw(GameUtils.getWhitePixel(), x + i, y + cuerpoAlto - grosor, grosor, grosor);
        }

        // Lados punteados
        for (float i = 0; i < cuerpoAlto; i += grosor * 2) {
            batch.draw(GameUtils.getWhitePixel(), x, y + i, grosor, grosor);
            batch.draw(GameUtils.getWhitePixel(), x + ancho - grosor, y + i, grosor, grosor);
        }

        // Línea diagonal de "inactivo" cruzando el candado
        batch.draw(GameUtils.getWhitePixel(),
            x, y + cuerpoAlto,
            ancho, grosor);
    }
    /**
     * Verifica si el icono colisiona con el jugador.
     *
     * @param jugador El personaje del jugador
     * @return true si hay colisión, false en caso contrario
     */
    public boolean colisionaConJugador(PersonajeLobby jugador) {
        return areaInteraccion.overlaps(jugador.getHitbox());
    }
    /**
     * Controla si el icono debe mostrarse o no.
     *
     * @param mostrar true para mostrar el icono, false para ocultarlo
     */
    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
        if (!mostrar) {
            tiempoAnimacion = 0f; // Reiniciar animación
        }
    }

    public boolean isMostrando() {
        return mostrar;
    }
    /**
     * Obtiene la puerta asociada a este icono.
     *
     * @return La puerta transición asociada
     */
    public PuertaTransicion getPuerta() {
        return puerta;
    }
    /**
     * Libera los recursos utilizados por el icono.
     * Debe llamarse cuando el icono ya no sea necesario.
     */
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }

    public Rectangle getAreaInteraccion() {
        return areaInteraccion;
    }
}
