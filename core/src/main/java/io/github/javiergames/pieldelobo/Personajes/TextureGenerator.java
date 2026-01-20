package io.github.javiergames.pieldelobo.Personajes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureGenerator {

    public static TextureRegion crearFlechaTexture() {
        Pixmap pixmap = new Pixmap(32, 8, Pixmap.Format.RGBA8888);

        // Color amarillo para la flecha
        pixmap.setColor(1, 1, 0, 1);

        // Triángulo para la punta
        pixmap.fillTriangle(0, 4, 24, 0, 24, 8);

        // Rectángulo para el cuerpo
        pixmap.setColor(0.8f, 0.8f, 0, 1);
        pixmap.fillRectangle(24, 2, 8, 4);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        return new TextureRegion(texture);
    }

    public static TextureRegion[] crearBolaFuegoFrames() {
        TextureRegion[] frames = new TextureRegion[4];

        for (int i = 0; i < 4; i++) {
            Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);

            // Color que cambia del amarillo al rojo
            float progreso = i / 3f;
            float r = 1.0f;
            float g = 1.0f - progreso * 0.5f;
            float b = 0.2f;

            pixmap.setColor(r, g, b, 1);
            int radio = 12 + i * 2;
            pixmap.fillCircle(16, 16, radio);

            // Borde brillante
            pixmap.setColor(1, 1, 0.8f, 1);
            pixmap.drawCircle(16, 16, radio + 1);

            Texture texture = new Texture(pixmap);
            frames[i] = new TextureRegion(texture);
            pixmap.dispose();
        }

        return frames;
    }
}
