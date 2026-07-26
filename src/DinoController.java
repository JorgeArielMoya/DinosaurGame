import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    @FXML
    private Pane panelJuego;

    @FXML
    private Pane mundoLayer;

    @FXML
    private ImageView dinoEstatico;

    @FXML
    private Line lineaRecorrido;

    // Constantes del juego
    private final double GROUND_LEVEL = 67.0; // LayoutY de la línea de recorrido
    private final double DINO_BASE_Y = GROUND_LEVEL - 60.0; // 60 = altura del dinosaurio
    private final double DURACION_SALTO = 400; // ms
    private final double ALTURA_SALTO = -130; // px
    private final double DISTANCIA_MINIMA = 200;
    private final double COOLDOWN_OBSTACULOS = 1.0;
    private final double PTERO_SPAWN_CHANCE = 0.3;
    private final Random random = new Random();

    // Variables de estado
    private double spdDino = 2.0;
    private boolean estaSaltando = false;
    private boolean estaAgachado = false;
    private boolean estaCorriendo = true;
    private double alturaOriginal;

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

        // Configurar animaciones y sprites
        configurarAgachado();
        configurarMovimiento();
        configurarCorrer();

        // Iniciar animación de correr al inicio
        animacionCorrer.play();

        // Debug visual
        lineaRecorrido.setVisible(false);
    }

    // ============== MOVIMIENTO ==============
    private void configurarMovimiento() {
        final long[] lastTime = {System.nanoTime()};
        final double DINO_FIXED_X = 20;

        animacionMove = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            long now = System.nanoTime();
            double deltaTime = (now - lastTime[0]) / 1e9;
            lastTime[0] = now;

            double offset = mundoLayer.getLayoutX() + DINO_FIXED_X - dinoEstatico.getLayoutX();
            mundoLayer.setLayoutX(mundoLayer.getLayoutX() - offset - spdDino);

            generarObstaculo(deltaTime);
            cleanObstacles();

            spdDino += 0.001;
        }));

        animacionMove.setCycleCount(Animation.INDEFINITE);
        animacionMove.play();
    }

    // ============== ANIMACIÓN DE CORRER ==============
    private void configurarCorrer() {
        runFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/images/Pie1.png")),
            new Image(getClass().getResourceAsStream("/images/Pie2.png"))
        };

        animacionCorrer = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == runFrames[0] ? runFrames[1] : runFrames[0]);
        }));

        animacionCorrer.setCycleCount(Animation.INDEFINITE);
        dinoEstatico.setImage(runFrames[0]); // Inicial
    }

    // ============== AGACHADO ==============
    private void configurarAgachado() {
        duckFrames = new Image[]{
            new Image(getClass().getResourceAsStream("/images/Pie1Agachado.png")),
            new Image(getClass().getResourceAsStream("/images/Pie2Agachado.png"))
        };

        animacionAgacharse = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            Image current = dinoEstatico.getImage();
            dinoEstatico.setImage(current == duckFrames[0] ? duckFrames[1] : duckFrames[0]);
        }));

        animacionAgacharse.setCycleCount(Animation.INDEFINITE);
        alturaOriginal = dinoEstatico.getFitHeight();
    }

    private void agacharse() {
        if (!estaSaltando && !estaAgachado) {
            estaAgachado = true;
            dinoEstatico.setFitHeight(alturaOriginal * 0.6);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() + (alturaOriginal * 0.4));
            animacionAgacharse.play();
            animacionCorrer.pause();
        }
    }

    private void dejarAgacharse() {
        if (estaAgachado) {
            estaAgachado = false;
            dinoEstatico.setFitHeight(alturaOriginal);
            dinoEstatico.setLayoutY(dinoEstatico.getLayoutY() - (alturaOriginal * 0.4));
            animacionAgacharse.stop();
            animacionCorrer.play();
        }
    }

    // ============== SALTO ==============
    private void saltarDinosario() {
        estaSaltando = true;

        TranslateTransition salto = new TranslateTransition(Duration.millis(DURACION_SALTO), dinoEstatico);
        salto.setByY(ALTURA_SALTO);
        salto.setAutoReverse(true);
        salto.setCycleCount(2);
        animacionCorrer.pause();

        salto.setOnFinished(e -> {
            dinoEstatico.setTranslateY(0);
            if (!estaAgachado) {
                animacionCorrer.play();
            }
            estaSaltando = false;
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
            new Image(getClass().getResourceAsStream("/ImagesObs/Cactus" + cantCactus + ".png"))
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
            new Image(getClass().getResourceAsStream("/ImagesObs/AletaAbajo.png")),
            new Image(getClass().getResourceAsStream("/ImagesObs/AletaArriba.png"))
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
        detenerJuego(); // ya la tienes definida
        System.out.println("¡GAME OVER!");
        // Aquí puedes notificar a otra clase/controller que muestre el menú
        // de "Reintentar", si ese botón vive en otro FXML/controller.
    }

    // ============== MÉTODOS PARA INICIAR/DETENER ==============
    public void iniciarJuego() {
        // Reiniciar estado del juego
        mundoLayer.setLayoutX(0);
        dinoEstatico.setLayoutY(DINO_BASE_Y);
        dinoEstatico.setImage(runFrames[0]);
        spdDino = 2.0;
        tiempoUltimoObstaculo = 0;
        estaSaltando = false;
        estaAgachado = false;

        // Limpiar obstáculos anteriores
        cleanObstacles();

        // Iniciar animaciones
        animacionMove.play();
        animacionCorrer.play();
    }

    public void detenerJuego() {
        animacionMove.stop();
        animacionCorrer.stop();
        animacionAgacharse.stop();
    }
}