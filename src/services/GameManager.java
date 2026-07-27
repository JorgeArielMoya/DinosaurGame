package services;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import models.Obstaculo;
import java.util.function.Consumer;

public class GameManager {
    private final Pane mundoLayer;
    private final ImageView dinoEstatico;
    private final EnemyManager enemyManager;
    
    private Timeline animacionMove;
    private double spdDino;
    private int score = 0;
    private boolean juegoTerminado = false;
    
    private Consumer<Integer> onScoreUpdate;
    private Runnable onGameOver;

    public GameManager(Pane mundoLayer, ImageView dinoEstatico, double groundLevel) {
        this.mundoLayer = mundoLayer;
        this.dinoEstatico = dinoEstatico;
        this.enemyManager = new EnemyManager(mundoLayer, groundLevel);
        inicializarBuclePrincipal();
    }

    private void inicializarBuclePrincipal() {
        final long[] lastTime = {System.nanoTime()};
        final double DINO_FIXED_X = 20;

        animacionMove = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (juegoTerminado) return;

            long now = System.nanoTime();
            double deltaTime = (now - lastTime[0]) / 1e9;
            lastTime[0] = now;

            double offset = mundoLayer.getLayoutX() + DINO_FIXED_X - dinoEstatico.getLayoutX();
            mundoLayer.setLayoutX(mundoLayer.getLayoutX() - offset - spdDino);

            enemyManager.actualizar(deltaTime, spdDino);
            enemyManager.cleanObstacles();
            verificarColisiones();

            spdDino += 0.002;
            score++;
            
            if (onScoreUpdate != null) {
                onScoreUpdate.accept(score);
            }
        }));
        animacionMove.setCycleCount(Animation.INDEFINITE);
    }

    private void verificarColisiones() {
        if (juegoTerminado) return;

        var boundsDino = dinoEstatico.getBoundsInParent();
        double margen = 5;
        double dinoX = boundsDino.getMinX() + margen;
        double dinoY = boundsDino.getMinY() + margen;
        double dinoWidth = boundsDino.getWidth() - (margen * 2);
        double dinoHeight = boundsDino.getHeight() - (margen * 2);

        for (Obstaculo obs : enemyManager.getObstaculosActivos()) {
            var boundsObstaculo = obs.getImageView().getBoundsInParent();

            if (dinoX < boundsObstaculo.getMaxX() &&
                dinoX + dinoWidth > boundsObstaculo.getMinX() &&
                dinoY < boundsObstaculo.getMaxY() &&
                dinoY + dinoHeight > boundsObstaculo.getMinY()) {
                
                detenerTodo();
                if (onGameOver != null) onGameOver.run();
                break;
            }
        }
    }

    public void iniciar(double velocidadInicial) {
        this.spdDino = velocidadInicial;
        this.score = 0;
        this.juegoTerminado = false;
        animacionMove.play();
    }

    public void detenerTodo() {
        juegoTerminado = true;
        animacionMove.stop();
        enemyManager.detenerTodos();
    }

    public void limpiarListaObstaculos() {
        enemyManager.limpiarLista();
    }

    public void setOnScoreUpdate(Consumer<Integer> onScoreUpdate) {
        this.onScoreUpdate = onScoreUpdate;
    }

    public void setOnGameOver(Runnable onGameOver) {
        this.onGameOver = onGameOver;
    }
}