package services;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class DinoManager {
    private final ImageView dinoEstatico;
    private final double dinoBaseY;

    private final double DURACION_SALTO = 400;
    private final double ALTURA_SALTO = -130;
    private double alturaOriginal;

    private boolean estaSaltando = false;
    private boolean estaAgachado = false;
    private boolean estaCorriendo = true;

    private Timeline animacionCorrer;
    private Timeline animacionAgacharse;
    private Image[] runFrames;
    private Image[] duckFrames;

    public DinoManager(ImageView dinoEstatico, double groundLevel) {
        this.dinoEstatico = dinoEstatico;
        this.dinoBaseY = groundLevel - 55.0;
        
        dinoEstatico.setLayoutY(dinoBaseY);
        dinoEstatico.setLayoutX(20);

        configurarFrames();
    }

    private void configurarFrames() {
        runFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/images/imagesDino/PieIzquierdo.png")),
            new Image(getClass().getResourceAsStream("/images/imagesDino/PieDerecho.png"))
        };

        animacionCorrer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == runFrames[0] ? runFrames[1] : runFrames[0]);
        }));
        animacionCorrer.setCycleCount(Animation.INDEFINITE);
        alturaOriginal = dinoEstatico.getFitHeight();

        duckFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/images/imagesDino/AgachadoIzquierdo.png")),
            new Image(getClass().getResourceAsStream("/images/imagesDino/AgachadoDerecho.png"))
        };

        animacionAgacharse = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == duckFrames[0] ? duckFrames[1] : duckFrames[0]);
        }));
        animacionAgacharse.setCycleCount(Animation.INDEFINITE);
    }

    public void reiniciarPosicion() {
        dinoEstatico.setLayoutY(dinoBaseY);
        dinoEstatico.setFitHeight(alturaOriginal);
        dinoEstatico.setTranslateY(0);
        dinoEstatico.setImage(runFrames[0]);

        estaSaltando = false;
        estaAgachado = false;
        estaCorriendo = true;
        animacionCorrer.play();
    }

    public void saltar() {
        if (estaSaltando || estaAgachado) return;
        estaSaltando = true;

        TranslateTransition salto = new TranslateTransition(Duration.millis(DURACION_SALTO), dinoEstatico);
        salto.setByY(ALTURA_SALTO);
        salto.setAutoReverse(true);
        salto.setCycleCount(2);

        if (estaCorriendo) {
            estaCorriendo = false;
            animacionCorrer.pause();
        }

        salto.setOnFinished(e -> {
            dinoEstatico.setTranslateY(0);
            dinoEstatico.setImage(runFrames[0]);
            estaSaltando = false;        
            if (!estaAgachado) {
                estaCorriendo = true;
                animacionCorrer.play();        
            }
        });
        salto.play();
    }

    public void agacharse() {
        if (!estaSaltando && !estaAgachado) {
            estaAgachado = true;
            dinoEstatico.setFitHeight(alturaOriginal * 0.6);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() + (alturaOriginal * 0.4));
            animacionAgacharse.play();
            if (estaCorriendo) {
                estaCorriendo = false;
                animacionCorrer.pause();
            }
        }
    }

    public void dejarAgacharse() {
        if (estaAgachado) {
            estaAgachado = false;
            dinoEstatico.setFitHeight(alturaOriginal);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() - (alturaOriginal * 0.4));
            animacionAgacharse.stop();
            if (!estaSaltando) {
                estaCorriendo = true;
                animacionCorrer.play();
            }
        }
    }

    public void detenerAnimaciones() {
        animacionCorrer.stop();
        animacionAgacharse.stop();
    }
}