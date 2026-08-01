# Dino Game 🦖🌵

¡Un clásico juego estilo dinosaurio desarrollado en **Java** utilizando **JavaFX**! Supera obstáculos, esquiva al Pterodáctilo y a Mr. Fantástico mientras aumentas tu puntaje en este divertido proyecto.

---

## 🛠️ Tecnologías Utilizadas

* **Java 23**
* **JavaFX** (Interfaces gráficas, transiciones y animaciones con Timeline)
* **FXML** (Estructura de vistas)

---

## 🎮 Características del Juego

* **Movimiento Dinámico:** Aumento progresivo de la velocidad del juego conforme avanza el puntaje de la partida.
* **Sistema de Obstáculos Variados:** Generación aleatoria de cactus, Pterodáctilos voladores y obstáculos especiales.
* **Mecánicas del Dinosaurio:** Capacidad de correr, saltar y agacharse de forma fluida mediante controles de teclado (`Espacio`, `Arriba`, `Abajo`, `S`).
* **Puntuación y Récords:** Registro en tiempo real del puntaje actual y guardado de la mejor puntuación de la sesión.

---

## 📁 Estructura del Proyecto

```text
DinosaurGame/
├── controllers/    # Controladores de la interfaz (DinoController)
├── services/       # Lógica del juego (GameManager, DinoManager, EnemyManager)
├── models/         # Modelos de datos y entidades del juego
├── views/          # Archivos FXML e interfaces gráficas
└── resources/      # Sprites de animación (Dino y obstáculos)