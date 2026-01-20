package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.javiergames.pieldelobo.Mapas.MapaManager;

/**
 * Fábrica para crear diferentes tipos de enemigos según información del mapa.
 * Ahora configura la gravedad apropiadamente.
 *
 *  * @author Javier Gala
 *  * @version 2.1
 */
public class FabricaEnemigos {

    private MapaManager mapaManager;
    private Protagonista objetivo;

    public FabricaEnemigos(MapaManager mapaManager, Protagonista objetivo) {
        this.mapaManager = mapaManager;
        this.objetivo = objetivo;
    }

    /**
     * Crea un enemigo según la información de spawn.
     */
    public Enemigos crearEnemigo(MapaManager.EnemigoSpawnInfo spawnInfo) {
        Enemigos enemigo = null;
        String tipo = spawnInfo.tipo.toLowerCase();

        // Determinar qué tipo de enemigo crear
        if (tipo.contains("esqueleto")) {
            enemigo = new Esqueleto(spawnInfo.posicion.x, spawnInfo.posicion.y);
        }
        else if (tipo.contains("bandido_pesado") || tipo.contains("heavy")) {
            enemigo = new Bandido(spawnInfo.posicion.x, spawnInfo.posicion.y, true);
        }
        else if (tipo.contains("bandido_ligero") || tipo.contains("light") || tipo.contains("bandido")) {
            enemigo = new Bandido(spawnInfo.posicion.x, spawnInfo.posicion.y, false);
        }
        else if (tipo.contains("arquero")) {
            enemigo = new Arquero(spawnInfo.posicion.x, spawnInfo.posicion.y);
        }
        else if (tipo.contains("golem")) {
            enemigo = new Golem(spawnInfo.posicion.x, spawnInfo.posicion.y);
        }
        else if (tipo.contains("bruja") || tipo.contains("mago")) {
            enemigo = new BrujaFuego(spawnInfo.posicion.x, spawnInfo.posicion.y);
            // La BrujaFuego ya tiene gravedad desactivada en su constructor
        }
        else if (tipo.contains("necromancer")) {
            enemigo = new Necromancer(spawnInfo.posicion.x, spawnInfo.posicion.y);
            // El Necromancer ya tiene gravedad desactivada en su constructor
        }
        else {
            Gdx.app.error("FabricaEnemigos", "Tipo de enemigo desconocido: " + spawnInfo.tipo);
            return null;
        }

        // Configurar propiedades personalizadas si existen
        if (spawnInfo.vidaPersonalizada > 0) {
            Gdx.app.log("FabricaEnemigos", "Vida personalizada para " + spawnInfo.tipo +
                ": " + spawnInfo.vidaPersonalizada);
        }

        // Configurar dirección inicial
        if (enemigo != null && !spawnInfo.mirandoDerecha) {
            enemigo.mirandoDerecha = false;
            Gdx.app.log("FabricaEnemigos", spawnInfo.tipo + " mirando a la izquierda");
        }

        // Configurar referencias comunes
        if (enemigo != null) {
            enemigo.setMapaManager(mapaManager);
            enemigo.setObjetivo(objetivo);

            // Si el enemigo tiene gravedad, corregir su posición inicial
            if (enemigo.aplicarGravedad) {
                enemigo.corregirPosicionInicial();
            }
        }

        return enemigo;
    }

    /**
     * Crea todos los enemigos desde un array de spawn info.
     */
    public Array<Enemigos> crearEnemigosDesdeSpawnInfo(Array<MapaManager.EnemigoSpawnInfo> spawnInfos) {
        Array<Enemigos> enemigos = new Array<>();

        for (MapaManager.EnemigoSpawnInfo spawnInfo : spawnInfos) {
            Enemigos enemigo = crearEnemigo(spawnInfo);
            if (enemigo != null) {
                enemigos.add(enemigo);
                Gdx.app.log("FabricaEnemigos", "Enemigo creado: " + spawnInfo.tipo +
                    " en " + spawnInfo.posicion + " (Gravedad: " + enemigo.aplicarGravedad + ")");
            }
        }

        Gdx.app.log("FabricaEnemigos", "Total enemigos creados: " + enemigos.size);
        return enemigos;
    }
}
