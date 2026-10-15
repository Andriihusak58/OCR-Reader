import java.awt.image.BufferedImage;

/**
 * Джерело зображення гри. Боти не знають, звідки береться скріншот
 * (екран ПК через Robot, емулятор через ADB тощо).
 */
public interface ScreenSource {

    /** Повертає знімок вказаної області екрана гри. */
    BufferedImage capture(Region region);
}
