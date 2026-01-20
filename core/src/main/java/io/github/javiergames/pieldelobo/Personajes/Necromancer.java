package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.Stage;

/**
 * Enemigo Necromancer - SOLO lanza proyectiles oscuros (sin invocaciones).
 *
 *  * @author Javier Gala
 *  * @version 1.1
 */
public class Necromancer extends Enemigos {

    // Estado de ataque
    private boolean lanzandoProyectil = false;
    private float tiempoPreparacionProyectil = 0;

    // Constantes
    private static final float TIEMPO_PREPARACION_PROYECTIL = 0.6f;
    private static final float TIEMPO_ENTRE_ATAQUES = 2.0f;

    // Referencias
    private Stage stageReferencia;

    // Control de tiempo
    private float tiempoDesdeUltimoAtaque = 0;

    // Flotación
    private float tiempoFlotacion = 0;

    public Necromancer(float x, float y) {
        super(x, y);

        // CONFIGURACIÓN BÁSICA
        this.vidaMaxima = 6;
        this.vidaActual = 6;
        this.velocidad = 40f;
        this.distanciaMaxima = 60f;
        this.danoAtaque = 1;
        this.rangoAtaque = 200f;  // Rango de ataque
        this.duracionAtaque = 1.0f;  // Duración más corta para ataque
        this.cooldownAtaqueMaximo = 1.5f;  // Cooldown más corto

        // TAMAÑO Y HITBOX
        this.tamanioBase = 160f;
        this.offsetHitboxX = 40f;
        this.offsetHitboxY = 20f;
        this.anchoHitbox = 80f;
        this.altoHitbox = 88f;

        // SIN GRAVEDAD
        this.aplicarGravedad = false;
        this.enSuelo = true;

        setSize(tamanioBase, tamanioBase);
        cargarAnimacionesConSeguridad();

        Gdx.app.log("Necromancer", "¡NECROMANCER CREADO EN (" + x + "," + y + ")!");
    }

    @Override
    protected void cargarAnimaciones() {
        try {
            // Verificar si el archivo existe
            if (!Gdx.files.internal("Necromancer.atlas").exists()) {
                Gdx.app.log("Necromancer", "Atlas 'Necromancer.atlas' no encontrado");
                return;
            }

            atlas = new TextureAtlas(Gdx.files.internal("Necromancer.atlas"));
            Gdx.app.log("Necromancer", "Atlas Necromancer cargado exitosamente");

            // ====================== ANIMACIÓN IDLE ======================
            Array<TextureRegion> idleFrames = new Array<>();
            boolean idleCargado = false;

            for (int i = 0; i <= 7; i++) {
                String frameName = String.format("necromancer%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    idleFrames.add(frame);
                    idleCargado = true;
                    Gdx.app.debug("Necromancer", "Frame idle encontrado: " + frameName);
                }
            }

            if (idleCargado && idleFrames.size > 0) {
                animacionIdle = new Animation<>(0.25f, idleFrames);
                Gdx.app.log("Necromancer", "Animación Idle cargada: " + idleFrames.size + " frames");
            }

            // ====================== ANIMACIÓN CAMINAR ======================
            Array<TextureRegion> walkFrames = new Array<>();
            boolean walkCargado = false;

            for (int i = 20; i <= 27; i++) {
                String frameName = String.format("necromancer%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    walkFrames.add(frame);
                    walkCargado = true;
                    Gdx.app.debug("Necromancer", "Frame caminar encontrado: " + frameName);
                }
            }

            if (walkCargado && walkFrames.size > 0) {
                animacionCaminar = new Animation<>(0.2f, walkFrames);
                Gdx.app.log("Necromancer", "Animación Caminar cargada: " + walkFrames.size + " frames");
            }

            // ====================== ANIMACIÓN ATACAR ======================
            Array<TextureRegion> attackFrames = new Array<>();
            boolean attackCargado = false;

            for (int i = 50; i <= 57; i++) {
                String frameName = String.format("necromancer%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    attackFrames.add(frame);
                    attackCargado = true;
                    Gdx.app.debug("Necromancer", "Frame ataque encontrado: " + frameName);
                }
            }

            if (attackCargado && attackFrames.size > 0) {
                animacionAtacar = new Animation<>(duracionAtaque / attackFrames.size, attackFrames);
                Gdx.app.log("Necromancer", "Animación Atacar cargada: " + attackFrames.size + " frames");
            }

            // ====================== ANIMACIÓN DAÑO ======================
            Array<TextureRegion> hurtFrames = new Array<>();
            boolean hurtCargado = false;

            for (int i = 34; i <= 37; i++) {
                String frameName = String.format("necromancer%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    hurtFrames.add(frame);
                    hurtCargado = true;
                    Gdx.app.debug("Necromancer", "Frame daño encontrado: " + frameName);
                }
            }

            if (hurtCargado && hurtFrames.size > 0) {
                animacionDano = new Animation<>(0.15f, hurtFrames);
                Gdx.app.log("Necromancer", "Animación Daño cargada: " + hurtFrames.size + " frames");
            }

            // ====================== ANIMACIÓN MUERTE ======================
            Array<TextureRegion> deathFrames = new Array<>();
            boolean deathCargado = false;

            // CORRECCIÓN IMPORTANTE: Los frames de muerte comienzan en 102, no en 100
            for (int i = 102; i <= 110; i++) {
                String frameName = String.format("necromancer%03d", i);
                TextureRegion frame = atlas.findRegion(frameName);
                if (frame != null) {
                    deathFrames.add(frame);
                    deathCargado = true;
                    Gdx.app.debug("Necromancer", "Frame muerte encontrado: " + frameName);
                }
            }

            if (deathCargado && deathFrames.size > 0) {
                animacionMuerte = new Animation<>(0.2f, deathFrames);
                Gdx.app.log("Necromancer", "Animación Muerte cargada: " + deathFrames.size + " frames");
            } else {
                Gdx.app.log("Necromancer", "ADVERTENCIA: No se encontró animación de muerte específica");
            }

            // ====================== FRAME INICIAL ======================
            if (animacionIdle != null) {
                frameActual = animacionIdle.getKeyFrame(0);
            }

            Gdx.app.log("Necromancer", "Animaciones cargadas correctamente");

        } catch (Exception e) {
            Gdx.app.error("Necromancer", "Error al cargar animaciones", e);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        // Actualizar temporizador de ataque
        tiempoDesdeUltimoAtaque += delta;

        // Flotación
        tiempoFlotacion += delta;

        // DEBUG: Mostrar estado periódicamente
        if (Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_DEBUG) {
            if (Math.random() < 0.01f) { // 1% de probabilidad cada frame
                Gdx.app.debug("Necromancer_DEBUG",
                    "puedeAtacar=" + puedeAtacar +
                        ", estaAtacando=" + estaAtacando +
                        ", tiempoDesdeUltimoAtaque=" + String.format("%.1f", tiempoDesdeUltimoAtaque));
            }
        }
    }

    // MÉTODO CRÍTICO: Establecer stage
    public void setStage(Stage stage) {
        this.stageReferencia = stage;
        Gdx.app.log("Necromancer", "¡STAGE ESTABLECIDO! " + (stage != null ? "NO NULL" : "NULL"));
    }

    @Override
    protected void actualizarIA(float delta) {
        if (estaMuerto || recibiendoDano) return;

        if (objetivo == null || !objetivo.estaVivo()) {
            patrullar(delta);
            return;
        }

        float distancia = calcularDistanciaAlObjetivo();

        // DEBUG: Mostrar distancia periódicamente
        if (Gdx.app.getLogLevel() >= com.badlogic.gdx.Application.LOG_DEBUG) {
            if (Math.random() < 0.02f) { // 2% de probabilidad cada frame
                Gdx.app.debug("Necromancer_DIST", "Distancia: " + String.format("%.1f", distancia) +
                    " | Rango: " + rangoAtaque);
            }
        }

        if (distancia <= rangoAtaque) {
            // Detener movimiento y mirar al objetivo
            direccionMovimiento.set(0, 0);
            mirarAlObjetivo();

            // ¿Puede atacar?
            if (puedeAtacar && !estaAtacando && tiempoDesdeUltimoAtaque >= TIEMPO_ENTRE_ATAQUES) {
                Gdx.app.log("Necromancer", "¡INICIANDO ATAQUE! Distancia: " + String.format("%.1f", distancia));
                iniciarAtaqueProyectil();
            }
        } else {
            // Acercarse al objetivo
            seguirObjetivo(delta);
        }
    }

    private void iniciarAtaqueProyectil() {
        if (puedeAtacar && !estaAtacando && !estaMuerto && !recibiendoDano) {
            estaAtacando = true;
            lanzandoProyectil = true;
            tiempoAtaque = 0;
            tiempoPreparacionProyectil = 0;
            puedeAtacar = false;
            tiempoDesdeUltimoAtaque = 0;

            Gdx.app.log("Necromancer", "¡PREPARANDO PROYECTIL OSCURO!");
        }
    }

    @Override
    protected void actualizarAtaque(float delta) {
        if (!estaAtacando) return;

        tiempoAtaque += delta;

        if (lanzandoProyectil) {
            tiempoPreparacionProyectil += delta;

            // Lanzar proyectil en el momento adecuado (aproximadamente mitad de animación)
            if (tiempoPreparacionProyectil >= TIEMPO_PREPARACION_PROYECTIL) {
                if (!proyectilLanzado) { // Asegurar que solo se lance una vez
                    lanzarProyectilOscuro();
                    proyectilLanzado = true;
                }
            }
        }

        // Terminar ataque
        if (tiempoAtaque >= duracionAtaque) {
            estaAtacando = false;
            lanzandoProyectil = false;
            tiempoAtaque = 0;
            tiempoPreparacionProyectil = 0;
            proyectilLanzado = false;
            Gdx.app.log("Necromancer", "Ataque terminado");
        }
    }

    // Variable para controlar que el proyectil solo se lance una vez por ataque
    private boolean proyectilLanzado = false;

    private void lanzarProyectilOscuro() {
        // VERIFICACIONES CRÍTICAS
        Gdx.app.log("Necromancer", "=== INTENTANDO LANZAR PROYECTIL ===");
        Gdx.app.log("Necromancer", "1. stageReferencia: " + (stageReferencia != null ? "OK" : "ERROR: NULL"));
        Gdx.app.log("Necromancer", "2. objetivo: " + (objetivo != null ? "OK" : "ERROR: NULL"));

        if (objetivo != null) {
            Gdx.app.log("Necromancer", "3. objetivo.estaVivo(): " + objetivo.estaVivo());
        }

        // Verificaciones esenciales
        if (stageReferencia == null) {
            Gdx.app.error("Necromancer", "ERROR CRÍTICO: stageReferencia es NULL");
            return;
        }

        if (objetivo == null || !objetivo.estaVivo()) {
            Gdx.app.log("Necromancer", "Objetivo no disponible");
            return;
        }

        // CALCULAR POSICIÓN Y DIRECCIÓN
        float origenX = getX() + tamanioBase / 2;
        float origenY = getY() + tamanioBase * 0.7f;

        float objetivoX = objetivo.getX() + objetivo.getWidth() / 2;
        float objetivoY = objetivo.getY() + objetivo.getHeight() / 2;

        Gdx.app.log("Necromancer", "Origen: (" + origenX + "," + origenY + ")");
        Gdx.app.log("Necromancer", "Objetivo: (" + objetivoX + "," + objetivoY + ")");

        // Dirección
        float dirX = objetivoX - origenX;
        float dirY = objetivoY - origenY;

        // Normalizar
        float distancia = (float)Math.sqrt(dirX * dirX + dirY * dirY);
        if (distancia <= 0) {
            Gdx.app.log("Necromancer", "Distancia 0, no se puede lanzar");
            return;
        }

        float velocidadBase = 180f;
        dirX = dirX / distancia * velocidadBase;
        dirY = dirY / distancia * velocidadBase;

        // Ajustar según dirección
        if (mirandoDerecha) {
            origenX += 30f;
        } else {
            origenX -= 30f;
        }

        // CREAR PROYECTIL
        Gdx.app.log("Necromancer", "Creando proyectil en (" + origenX + "," + origenY + ")");

        ProyectilOscuro proyectil = new ProyectilOscuro(
            origenX, origenY,
            dirX, dirY,
            danoAtaque,
            mapaManager,
            objetivo
        );

        // AÑADIR AL STAGE
        stageReferencia.addActor(proyectil);
        Gdx.app.log("Necromancer", "¡PROYECTIL AÑADIDO AL STAGE!");

        // Sonido/efecto visual
        Gdx.app.log("Necromancer", "¡PROYECTIL OSCURO LANZADO CON ÉXITO!");
    }

    @Override
    public void recibirDano() {
        if (estaMuerto || recibiendoDano) return;

        vidaActual--;
        recibiendoDano = true;
        tiempoDano = 0;

        // Interrumpir ataque
        if (estaAtacando) {
            estaAtacando = false;
            lanzandoProyectil = false;
            proyectilLanzado = false;
            Gdx.app.log("Necromancer", "Ataque interrumpido por daño");
        }

        Gdx.app.log("Necromancer", "¡Daño recibido! Vida: " + vidaActual + "/" + vidaMaxima);

        if (vidaActual <= 0) {
            morir();
        }
    }

    @Override
    protected void morir() {
        super.morir();
        Gdx.app.log("Necromancer", "¡Necromancer derrotado!");
    }

    @Override
    public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float parentAlpha) {
        // Efecto de flotación
        float flotacion = (float)Math.sin(tiempoFlotacion * 2) * 3f;
        float drawY = getY() + flotacion;

        if (frameActual != null) {
            float alpha = 1.0f;

            if (recibiendoDano) {
                alpha = 0.5f;
            } else if (estaMuerto) {
                alpha = 1.0f - (tiempoMuerte / 2.0f);
                if (alpha < 0.1f) alpha = 0.1f;
            }

            batch.setColor(1, 1, 1, alpha);

            if (mirandoDerecha) {
                batch.draw(frameActual, getX(), drawY, tamanioBase, tamanioBase);
            } else {
                batch.draw(frameActual,
                    getX() + tamanioBase, drawY,
                    -tamanioBase, tamanioBase);
            }

            batch.setColor(1, 1, 1, 1);
        }
    }

    @Override
    protected void actualizarAnimacion(float delta) {
        tiempoAnimacion += delta;

        // Asegurar que tenemos animación idle
        if (animacionIdle == null) {
            return;
        }

        if (estaMuerto) {
            if (animacionMuerte != null) {
                frameActual = animacionMuerte.getKeyFrame(tiempoMuerte, false);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (recibiendoDano) {
            if (animacionDano != null) {
                frameActual = animacionDano.getKeyFrame(tiempoDano, false);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (estaAtacando) {
            if (animacionAtacar != null) {
                frameActual = animacionAtacar.getKeyFrame(tiempoAnimacion, false);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else if (Math.abs(direccionMovimiento.x) > 0.1f) {
            if (animacionCaminar != null) {
                frameActual = animacionCaminar.getKeyFrame(tiempoAnimacion, true);
            } else {
                frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
            }
        } else {
            frameActual = animacionIdle.getKeyFrame(tiempoAnimacion, true);
        }

        if (frameActual == null) {
            frameActual = animacionIdle.getKeyFrame(0);
        }
    }
}
