package controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import models.Obstaculo;
import models.TipoObstaculo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EnemyController {
    private final Pane mundoLayer;
    private final double groundLevel;
    private final Random random = new Random();
    private final List<Obstaculo> obstaculosActivos = new ArrayList<>();
    
    private double tiempoUltimoObstaculo = 0;
    private final double MIN_TIEMPO = 1.5;
    private final double PTERO_SPAWN_CHANCE = 0.3;
    private final double FANTASTICO_SPAWN_CHANCE = 0.15;

    public EnemyController(Pane mundoLayer, double groundLevel) {
        this.mundoLayer = mundoLayer;
        this.groundLevel = groundLevel;
    }

    public void actualizar(double deltaTime, double spdDino) {
        tiempoUltimoObstaculo += deltaTime;
        if (tiempoUltimoObstaculo >= MIN_TIEMPO) {
            double roll = random.nextDouble();

            if (roll < PTERO_SPAWN_CHANCE) {
                generarPtero(spdDino);
            } else if (roll < PTERO_SPAWN_CHANCE + FANTASTICO_SPAWN_CHANCE) {
                generarFantastico(spdDino);
            } else {
                generarCactus(spdDino);
            }

            tiempoUltimoObstaculo = 0;
        }
    }

    private void generarCactus(double spdDino) {
        int cantCactus = random.nextInt(1, 4);
        ImageView cactusImg = new ImageView(
            new Image(getClass().getResourceAsStream("/images/imagesObs/Cactus" + cantCactus + ".png"))
        );

        cactusImg.setFitHeight(40);
        cactusImg.setPreserveRatio(true);
        cactusImg.setLayoutX(mundoLayer.getPrefWidth());
        cactusImg.setLayoutY(groundLevel - cactusImg.getFitHeight());

        Timeline movimientoCactus = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            cactusImg.setLayoutX(cactusImg.getLayoutX() - spdDino);
        }));
        movimientoCactus.setCycleCount(Animation.INDEFINITE);
        movimientoCactus.play();

        Obstaculo obstaculo = new Obstaculo(cactusImg, new Timeline[]{movimientoCactus}, TipoObstaculo.CACTUS);
        obstaculosActivos.add(obstaculo);
        mundoLayer.getChildren().add(cactusImg);
    }

    private void generarPtero(double spdDino) {
        Image[] aleteoFrames = {
            new Image(getClass().getResourceAsStream("/images/imagesObs/AlaAbajo.png")),
            new Image(getClass().getResourceAsStream("/images/imagesObs/AlaArriba.png"))
        };

        ImageView imagenPtero = new ImageView(aleteoFrames[0]);
        imagenPtero.setFitHeight(30);
        imagenPtero.setPreserveRatio(true);

        double minFlyHeight = groundLevel - 80;
        double maxFlyHeight = groundLevel - 30;

        imagenPtero.setLayoutX(mundoLayer.getPrefWidth());
        imagenPtero.setLayoutY(minFlyHeight + (Math.random() * (maxFlyHeight - minFlyHeight)));

        Timeline animacionAleteo = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            Image current = imagenPtero.getImage();
            imagenPtero.setImage(current == aleteoFrames[0] ? aleteoFrames[1] : aleteoFrames[0]);
        }));
        animacionAleteo.setCycleCount(Animation.INDEFINITE);
        animacionAleteo.play();

        Timeline movimiento = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            imagenPtero.setLayoutX(imagenPtero.getLayoutX() - spdDino);
        }));
        movimiento.setCycleCount(Animation.INDEFINITE);
        movimiento.play();

        Obstaculo obstaculo = new Obstaculo(imagenPtero, new Timeline[]{animacionAleteo, movimiento}, TipoObstaculo.PTERODACTILO);
        obstaculosActivos.add(obstaculo);
        mundoLayer.getChildren().add(imagenPtero);
    }

    private void generarFantastico(double spdDino) {
        ImageView imagenFantastico = new ImageView(
            new Image(getClass().getResourceAsStream("/images/imagesObs/MrFantastico.png"))
        );

        imagenFantastico.setFitHeight(45); // ajusta el tamaño a tu gusto
        imagenFantastico.setPreserveRatio(true);

        imagenFantastico.setLayoutX(mundoLayer.getPrefWidth());
        imagenFantastico.setLayoutY(groundLevel - imagenFantastico.getFitHeight());

        Timeline movimiento = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            imagenFantastico.setLayoutX(imagenFantastico.getLayoutX() - spdDino);
        }));
        movimiento.setCycleCount(Animation.INDEFINITE);
        movimiento.play();

        Obstaculo obstaculo = new Obstaculo(imagenFantastico, new Timeline[]{movimiento}, TipoObstaculo.FANTASTICO);
        obstaculosActivos.add(obstaculo);
        mundoLayer.getChildren().add(imagenFantastico);
    }

    public void cleanObstacles() {
        Iterator<Obstaculo> iterator = obstaculosActivos.iterator();
        while (iterator.hasNext()) {
            Obstaculo obs = iterator.next();
            if (obs.getImageView().getLayoutX() + mundoLayer.getLayoutX() < -50) {
                obs.detenerTimelines();
                mundoLayer.getChildren().remove(obs.getImageView());
                iterator.remove();
            }
        }
    }

    public void detenerTodos() {
        for (Obstaculo obs : obstaculosActivos) {
            obs.detenerTimelines();
        }
    }

    public void limpiarLista() {
        detenerTodos();
        for (Obstaculo obs : obstaculosActivos) {
            mundoLayer.getChildren().remove(obs.getImageView());
        }
        obstaculosActivos.clear();
        tiempoUltimoObstaculo = 0;
    }

    public List<Obstaculo> getObstaculosActivos() {
        return obstaculosActivos;
    }
}