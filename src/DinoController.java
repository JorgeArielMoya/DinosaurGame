import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import java.util.Iterator;
import java.util.Random;

public class DinoController {
    @FXML private Pane panelJuego;
    @FXML private Pane mundoLayer;
    @FXML private ImageView dinoEstatico;
    @FXML private Line lineaRecorrido;
    @FXML private Button btnIniciar;
    @FXML private Label lblScore;

    // Constantes del juego
    private final double GROUND_LEVEL = 230.0;
    private final double DINO_BASE_Y = GROUND_LEVEL - 55.0; // antes era -70.0, ahora baja ~15px 160.0 (usando la nueva altura de 70)
    private final double DURACION_SALTO = 400; // ms
    private final double ALTURA_SALTO = -130; // px
    private final double PTERO_SPAWN_CHANCE = 0.3;
    private final Random random = new Random();

    // Variables de estado
    private double spdDino;
    private boolean estaSaltando = false;
    private boolean estaAgachado = false;
    private boolean estaCorriendo = true;
    private double alturaOriginal;
    private int score = 0;

    // Animaciones
    private Timeline animacionMove;
    private Timeline animacionCorrer;
    private Timeline animacionAgacharse;

    // Sprites
    private Image[] runFrames;
    private Image[] duckFrames;

    // Control de spawn de obstáculos
    private double tiempoUltimoObstaculo = 0;
    private final double MIN_TIEMPO = 1.5;

    private boolean juegoTerminado = false;

    public void initialize() {
        // Configuración del dinosaurio
        dinoEstatico.setLayoutY(DINO_BASE_Y);
        dinoEstatico.setLayoutX(20);

        // Configurar teclado
        panelJuego.setFocusTraversable(true);
        panelJuego.setOnKeyPressed(this::handleKeyPress);
        panelJuego.setOnKeyReleased(this::handleKeyRelease);

        // Configurar animaciones y sprites (solo preparan, no reproducen)
        configurarAgachado();
        configurarMovimiento();
        configurarCorrer();

        // El juego queda estático hasta hacer clic en "Iniciar Juego"
        lineaRecorrido.setVisible(false);
    }

    // ============== MOVIMIENTO ==============
    private void configurarMovimiento() {
        final long[] lastTime = {System.nanoTime()};
        final double DINO_FIXED_X = 20;

        animacionMove = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (juegoTerminado) return;

            long now = System.nanoTime();
            double deltaTime = (now - lastTime[0]) / 1e9;
            lastTime[0] = now;

            double offset = mundoLayer.getLayoutX() + DINO_FIXED_X - dinoEstatico.getLayoutX();
            mundoLayer.setLayoutX(mundoLayer.getLayoutX() - offset - spdDino);

            generarObstaculo(deltaTime);
            cleanObstacles();
            verificarColisiones();

            spdDino += 0.002;

            score++;
            lblScore.setText("Score: " + score);
        }));

        animacionMove.setCycleCount(Animation.INDEFINITE);
        // No se reproduce aquí; arranca en iniciarJuego()
    }

    // ============== ANIMACIÓN DE CORRER ==============
    private void configurarCorrer() {
        runFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/Images/ImagesDino/PieIzquierdo.png")),
            new Image(getClass().getResourceAsStream("/Images/ImagesDino/PieDerecho.png"))
        };

        animacionCorrer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == runFrames[0] ? runFrames[1] : runFrames[0]);
        }));

        animacionCorrer.setCycleCount(Animation.INDEFINITE);
        dinoEstatico.setImage(runFrames[0]); // Imagen inicial estática
    }

    private void reanudarCorrer() {
        if (!estaSaltando && !estaAgachado && !estaCorriendo) {
            estaCorriendo = true;
            animacionCorrer.play();
        }
    }

    private void pausarCorrer() {
        if (estaCorriendo) {
            estaCorriendo = false;
            animacionCorrer.pause();
        }
    }

    // ============== AGACHADO ==============
    private void configurarAgachado() {
        duckFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/Images/ImagesDino/AgachadoPieIzquierdo.png")),
            new Image(getClass().getResourceAsStream("/Images/ImagesDino/AgachadoPieDerecho.png"))
        };

        animacionAgacharse = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == duckFrames[0] ? duckFrames[1] : duckFrames[0]);
        }));

        animacionAgacharse.setCycleCount(Animation.INDEFINITE);
        alturaOriginal = dinoEstatico.getFitHeight();
    }

    private void agacharse() {
        if (!estaSaltando && !estaAgachado && !juegoTerminado) {
            estaAgachado = true;
            dinoEstatico.setFitHeight(alturaOriginal * 0.6);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() + (alturaOriginal * 0.4));
            animacionAgacharse.play();
            pausarCorrer();
        }
    }

    private void dejarAgacharse() {
        if (estaAgachado) {
            estaAgachado = false;
            dinoEstatico.setFitHeight(alturaOriginal);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() - (alturaOriginal * 0.4));
            animacionAgacharse.stop();
            reanudarCorrer();
        }
    }

    // ============== SALTO ==============
    private void saltarDinosario() {
        if (juegoTerminado) return;

        estaSaltando = true;

        TranslateTransition salto = new TranslateTransition(Duration.millis(DURACION_SALTO), dinoEstatico);
        salto.setByY(ALTURA_SALTO);
        salto.setAutoReverse(true);
        salto.setCycleCount(2);
        pausarCorrer();

        salto.setOnFinished(e -> {
            dinoEstatico.setTranslateY(0);
            dinoEstatico.setImage(runFrames[0]);
            estaSaltando = false;        
            if (!estaAgachado) {
                reanudarCorrer();        
            }
        });

        salto.play();
    }

    // ============== MANEJO DE TECLAS ==============
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.UP) {
            if (!estaSaltando) {
                saltarDinosario();
            }
        } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            agacharse();
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.S) {
            dejarAgacharse();
        }
    }

    // ============== GENERACIÓN DE OBSTÁCULOS ==============
    private void generarObstaculo(double deltaTime) {
        tiempoUltimoObstaculo += deltaTime;

        if (tiempoUltimoObstaculo >= MIN_TIEMPO) {
            if (random.nextDouble() < PTERO_SPAWN_CHANCE) {
                generarPtero();
            } else {
                generarCactus();
            }
            tiempoUltimoObstaculo = 0;
        }
    }

    private void generarCactus() {
        int cantCactus = random.nextInt(1, 4);
        ImageView cactus = new ImageView(
            new Image(getClass().getResourceAsStream("/Images/ImagesObs/Cactus" + cantCactus + ".png"))
        );

        cactus.setFitHeight(40);
        cactus.setPreserveRatio(true);
        cactus.setLayoutX(mundoLayer.getPrefWidth());
        cactus.setLayoutY(GROUND_LEVEL - cactus.getFitHeight());

        Timeline movimientoCactus = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            cactus.setLayoutX(cactus.getLayoutX() - spdDino);
        }));

        movimientoCactus.setCycleCount(Animation.INDEFINITE);
        movimientoCactus.play();

        cactus.setUserData(new Timeline[]{movimientoCactus});
        mundoLayer.getChildren().add(cactus);
    }

    private void generarPtero() {
        Image[] aleteoFrames = {
            new Image(getClass().getResourceAsStream("/Images/ImagesObs/AlaAbajo.png")),
            new Image(getClass().getResourceAsStream("/Images/ImagesObs/AlaArriba.png"))
        };

        ImageView imagenPtero = new ImageView(aleteoFrames[0]);
        imagenPtero.setFitHeight(30);

        double minFlyHeight = GROUND_LEVEL - 80;
        double maxFlyHeight = GROUND_LEVEL - 30;

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

        imagenPtero.setUserData(new Timeline[]{animacionAleteo, movimiento});
        mundoLayer.getChildren().add(imagenPtero);
    }

    // ============== LIMPIEZA DE OBSTÁCULOS ==============
    private void cleanObstacles() {
        Iterator<Node> iterator = mundoLayer.getChildren().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (node != dinoEstatico && node != lineaRecorrido) {
                if (node.getLayoutX() + mundoLayer.getLayoutX() < -50) {
                    if (node instanceof ImageView) {
                        Timeline[] timelines = (Timeline[]) node.getUserData();
                        if (timelines != null) {
                            for (Timeline t : timelines) {
                                if (t != null) t.stop();
                            }
                        }
                    }
                    iterator.remove();
                }
            }
        }
    }

    // ============== COLISIONES ==============
    private void verificarColisiones() {
        if (juegoTerminado) return;

        var boundsDino = dinoEstatico.getBoundsInParent();
        double margen = 5;
        double dinoX = boundsDino.getMinX() + margen;
        double dinoY = boundsDino.getMinY() + margen;
        double dinoWidth = boundsDino.getWidth() - (margen * 2);
        double dinoHeight = boundsDino.getHeight() - (margen * 2);

        for (Node node : mundoLayer.getChildren()) {
            if (node instanceof ImageView && node != dinoEstatico) {
                var boundsObstaculo = node.getBoundsInParent();

                if (dinoX < boundsObstaculo.getMaxX() &&
                    dinoX + dinoWidth > boundsObstaculo.getMinX() &&
                    dinoY < boundsObstaculo.getMaxY() &&
                    dinoY + dinoHeight > boundsObstaculo.getMinY()) {

                    terminarJuego();
                    return;
                }
            }
        }
    }

   private void terminarJuego() {
        if (juegoTerminado) return;
        
        juegoTerminado = true;
        detenerJuego();

        for (Node node : mundoLayer.getChildren()) {
            if (node instanceof ImageView && node != dinoEstatico) {
                Timeline[] timelines = (Timeline[]) node.getUserData();
                if (timelines != null) {
                    for (Timeline t : timelines) {
                        if (t != null) t.stop();
                    }
                }
            }
        }

        btnIniciar.setText("Reintentar");
        btnIniciar.setVisible(true);
        System.out.println("¡GAME OVER!");
    }

    // ============== MÉTODOS PARA INICIAR/DETENER ==============
    @FXML
    public void iniciarJuego() {
        btnIniciar.setVisible(false);
        lineaRecorrido.setVisible(true);

        juegoTerminado = false;
        mundoLayer.setLayoutX(0);
        dinoEstatico.setLayoutY(DINO_BASE_Y);
        dinoEstatico.setFitHeight(alturaOriginal);
        dinoEstatico.setTranslateY(0);
        dinoEstatico.setImage(runFrames[0]);

        spdDino = 3.5;
        estaSaltando = false;
        estaAgachado = false;
        estaCorriendo = true;

        score = 0;
        lblScore.setText("Score: 0");

        cleanObstacles();

        animacionMove.play();
        animacionCorrer.play();

        panelJuego.requestFocus();
    }

    public void detenerJuego() {
        animacionMove.stop();
        animacionCorrer.stop();
        animacionAgacharse.stop();
    }
}