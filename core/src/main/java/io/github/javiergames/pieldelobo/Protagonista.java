package io.github.javiergames.pieldelobo;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class Protagonista extends Actor {
    // Animaciones
    private Animation<TextureRegion> animacionIdle;
    private Animation<TextureRegion> animacionCorrer;
    private Animation<TextureRegion> animacionSaltar;
    private Animation<TextureRegion> animacionAtacar;
    private TextureRegion frameActual;
    private float tiempoAnimacion = 0;

    // Física
    private static final float VELOCIDAD = 200f;
    private static final float GRAVEDAD = -900f;
    private static final float FUERZA_SALTO = 400f;
    private float velocidadY = 0;
    private boolean enSuelo = true;
    private int saltosRestantes = 1;

    // Estados
    private boolean estaAtacando = false;
    private boolean mirandoDerecha = true;
    private float tiempoAtaque = 0;
    private static final float DURACION_ATAQUE = 0.3f;



    public Protagonista() {
        cargarAnimaciones();
        setPosition(100, 100);
    }

    private void cargarAnimaciones() {

// Cargar otras animaciones (ejemplo - debes reemplazar con tus archivos reales)
        Texture idleSheet = new Texture(Gdx.files.internal("120x80_PNGSheets/_CrouchFull/quieto.png"));
        TextureRegion[] idleFrames = TextureRegion.split(idleSheet, idleSheet.getWidth()/4, idleSheet.getHeight())[0];
        animacionIdle = new Animation<>(0.15f, Array.with(idleFrames));

        Texture runSheet = new Texture(Gdx.files.internal("120x80_PNGSheets/_Run/run.png"));
        TextureRegion[] runFrames = TextureRegion.split(runSheet, runSheet.getWidth()/6, runSheet.getHeight())[0];
        animacionCorrer = new Animation<>(0.1f, Array.with(runFrames));

        Texture jumpSheet = new Texture(Gdx.files.internal("120x80_PNGSheets/_JumpFallInbetween/JUMP.png"));
        TextureRegion[] jumpFrames = TextureRegion.split(jumpSheet, jumpSheet.getWidth()/3, jumpSheet.getHeight())[0];
        animacionSaltar = new Animation<>(0.2f, Array.with(jumpFrames));

        frameActual = animacionIdle.getKeyFrame(0);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        actualizarAnimacion(delta);
        actualizarFisicas(delta);
        actualizarAtaque(delta);
    }

    private void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        if (estaAtacando) {
            frameActual = animacionAtacar.getKeyFrame(tiempoAnimacion, false);
        } else if (!enSuelo) {
            frameActual = animacionSaltar.getKeyFrame(tiempoAnimacion, true);
        } else if (Math.abs(getVelocityX()) > 0.1f) {
            frameActual = animacionCorrer.getKeyFrame(tiempoAnimacion, true);
        } else {
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }
    }

    private void actualizarFisicas(float delta) {
        // Aplicar gravedad
        if (!enSuelo) {
            velocidadY += GRAVEDAD * delta;
            moveBy(0, velocidadY * delta);
        }

        // Detección de suelo
        if (getY() <= 0) {
            setY(0);
            enSuelo = true;
            velocidadY = 0;
            saltosRestantes = 1;
        } else {
            enSuelo = false;
        }

        mantenerDentroPantalla();
    }

    private void actualizarAtaque(float delta) {
        if (estaAtacando) {
            tiempoAtaque += delta;
            if (tiempoAtaque >= DURACION_ATAQUE) {
                estaAtacando = false;
                tiempoAtaque = 0;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Calcular offset basado en el frame actual
        float offsetX = 0;
        float offsetY = 0;



        // Ajustar posición según dirección y offset
        float drawX = mirandoDerecha ? getX() - offsetX : getX() + offsetX - frameActual.getRegionWidth();
        float drawY = getY() - offsetY;

        // Dibujar con la escala adecuada
        batch.draw(frameActual,
            drawX, drawY,
            frameActual.getRegionWidth() / 2f, 0,
            frameActual.getRegionWidth(), frameActual.getRegionHeight(),
            mirandoDerecha ? 1 : -1, 4, 0);
    }

    public void mover(float delta, float direccionX, float direccionY) {
        if (estaAtacando) return;

        if (direccionX != 0 && direccionY != 0) {
            float factor = (float) Math.sqrt(direccionX * direccionX + direccionY * direccionY);
            direccionX /= factor;
            direccionY /= factor;
        }

        if (direccionX > 0 && !mirandoDerecha) {
            mirandoDerecha = true;
        } else if (direccionX < 0 && mirandoDerecha) {
            mirandoDerecha = false;
        }

        float movX = direccionX * VELOCIDAD * delta;
        float movY = enSuelo ? direccionY * VELOCIDAD * delta : 0;
        moveBy(movX, movY);
    }

    public void saltar() {
        if ((enSuelo || saltosRestantes > 0) && !estaAtacando) {
            velocidadY = FUERZA_SALTO;
            enSuelo = false;
            saltosRestantes--;
            tiempoAnimacion = 0;
        }
    }

    public void atacar() {
        if (!estaAtacando && enSuelo) {
            estaAtacando = true;
            tiempoAtaque = 0;
            tiempoAnimacion = 0;
        }
    }

    private void mantenerDentroPantalla() {
        if (getX() < 0) setX(0);
        if (getY() < 0) setY(0);
        if (getX() + getWidth() > Gdx.graphics.getWidth())
            setX(Gdx.graphics.getWidth() - getWidth());
        if (getY() + getHeight() > Gdx.graphics.getHeight())
            setY(Gdx.graphics.getHeight() - getHeight());
    }

    public void dispose() {
        animacionAtacar.getKeyFrames()[0].getTexture().dispose();
        animacionIdle.getKeyFrames()[0].getTexture().dispose();
        animacionCorrer.getKeyFrames()[0].getTexture().dispose();
        animacionSaltar.getKeyFrames()[0].getTexture().dispose();
    }

    // Métodos de acceso
    public boolean estaAtacando() { return estaAtacando; }
    public boolean estaEnSuelo() { return enSuelo; }
    private float getVelocityX() { return VELOCIDAD; }
}
