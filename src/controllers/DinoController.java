package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

public class DinoController {
    @FXML private Pane panelJuego;
    @FXML private Pane mundoLayer;
    @FXML private ImageView dinoEstatico;
    @FXML private Line lineaRecorrido;
    @FXML private Button btnIniciar;
    @FXML private Label lblScore;
    @FXML private Label lblRecord;

    private int recordActual = 0;
    private final double GROUND_LEVEL = 230.0;

    private GameManager gameEngine;
    private DinoManager dinoManager;

    public void initialize() {
        panelJuego.setFocusTraversable(true);
        panelJuego.setOnKeyPressed(this::handleKeyPress);
        panelJuego.setOnKeyReleased(this::handleKeyRelease);

        lineaRecorrido.setVisible(true);

        dinoManager = new DinoManager(dinoEstatico, GROUND_LEVEL);
        gameEngine = new GameManager(mundoLayer, dinoEstatico, GROUND_LEVEL);

        gameEngine.setOnScoreUpdate(score -> {
        lblScore.setText("Score: " + score);

            if (score > recordActual) {
                recordActual = score;
                lblRecord.setText("Record: " + recordActual);
            }
        });
        
        gameEngine.setOnGameOver(this::manejarGameOver);
    }

    @FXML
    public void iniciarJuego() {
        btnIniciar.setVisible(false);
        lineaRecorrido.setVisible(true);
        mundoLayer.setLayoutX(0);

        lblScore.setText("Score: 0");
        gameEngine.limpiarListaObstaculos();
        dinoManager.reiniciarPosicion();
        
        gameEngine.iniciar(3.5);
        panelJuego.requestFocus();
    }

    private void manejarGameOver() {
        dinoManager.detenerAnimaciones();
        btnIniciar.setText("Reintentar");
        btnIniciar.setVisible(true);
    }

    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.UP) {
            dinoManager.saltar();
        } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            dinoManager.agacharse();
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            dinoManager.dejarAgacharse();
        }
    }
}