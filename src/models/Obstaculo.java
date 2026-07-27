package models;

import javafx.animation.Timeline;
import javafx.scene.image.ImageView;

public class Obstaculo {
    private final ImageView imageView;
    private final Timeline[] timelines;
    private final TipoObstaculo tipo;

    public Obstaculo(ImageView imageView, Timeline[] timelines, TipoObstaculo tipo) {
        this.imageView = imageView;
        this.timelines = timelines;
        this.tipo = tipo;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Timeline[] getTimelines() {
        return timelines;
    }

    public TipoObstaculo getTipo() {
        return tipo;
    }

    public void detenerTimelines() {
        if (timelines != null) {
            for (Timeline t : timelines) {
                if (t != null) t.stop();
            }
        }
    }
}