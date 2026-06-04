# LAB 1 — ARSW: Programación Concurrente en Java 21

**Escuela Colombiana de Ingeniería Julio Garavito**  
**Asignatura:** Arquitecturas de Software (ARSW)  
**Estudiante:** Samuel Gil  
**Fecha:** Junio 2026

---

## Descripción

Laboratorio de programación concurrente en Java 21. Se trabajan dos ejercicios:

- **Parte I (Calentamiento):** PrimeFinder con pausa/reanudación usando `wait()` / `notifyAll()`.
- **Parte II:** Snake Race concurrente con hilos virtuales, sincronización y corrección de condiciones de carrera.

---

## Requisitos

- JDK 21 (Temurin recomendado)
- Maven 3.9+

---

## Parte I — PrimeFinder con `wait` / `notify`

### Descripción

Programa multi-hilo que busca números primos en el rango `[0, 30 000 000]` usando 3 hilos trabajadores. Cada 5 segundos:

1. Se **pausan** todos los hilos trabajadores.
2. Se **muestra** cuántos primos se han encontrado.
3. El programa **espera ENTER** para reanudar.

### Diseño de sincronización

La clase `Control` actúa como monitor. Todos los métodos de sincronización son `synchronized` sobre `this`:

<img width="481" height="383" alt="image" src="https://github.com/user-attachments/assets/1b7a4cbd-01e1-40a1-aedc-0bba080db576" />


### Evidencia de ejecución
<img width="437" height="613" alt="image" src="https://github.com/user-attachments/assets/baaff236-8db6-4372-baaa-7c1c1a003e40" />



### Conclusiones

- **Espera por ENTER con Scanner:** para cumplir la condición de que el programa espere al usuario antes de reanudar, el hilo `Control` usa `scanner.nextLine()` que lo bloquea hasta que se presiona ENTER. Solo después llama a `resumeThread()`.
- **Sin busy-waiting:** mediante `wait()` todos los hilos trabajadores se suspenden completamente hasta ser notificados — no consumen CPU mientras esperan.
- **Sin lost wakeups:** `notifyAll()` despierta a **todos** los hilos en espera sin importar cuál se durmió primero, y el `while (pause)` re-verifica la condición al despertar, protegiendo contra *spurious wakeups*.
- **Monitor único:** `Control.this` es el único objeto de sincronización; `checkPause()` y `resumeThread()` comparten el mismo lock, eliminando el riesgo de deadlock por monitores cruzados.

### Cómo ejecutar

```bash
cd Lab_SnakeRace-Java21


mvn compile


mvn exec:java -Pprime
```

---

## Parte II — Snake Race concurrente

### Descripción

Juego donde N serpientes corren de forma autónoma, cada una en su propio hilo virtual (Java 21). Incluye ratones, obstáculos, teletransportadores y turbo.

### Cómo ejecutar

```bash

mvn exec:java

mvn "-Dsnakes=4" exec:java
```

### Controles

| Tecla | Acción |
|---|---|
| Flechas | Controla serpiente 0 |
| WASD | Controla serpiente 1 |
| Espacio / botón Action | Pausar / Reanudar |

---

### 1. Análisis de concurrencia

El juego crea N serpientes autónomas, cada una ejecutándose en su propio **hilo virtual** (Java 21). Los hilos virtuales permiten lanzar cientos de serpientes sin el coste de memoria de los hilos plataforma (~1 MB/hilo), ya que la JVM multiplexa muchos hilos virtuales sobre pocos hilos del SO.

**Condiciones de carrera identificadas:**

| Situación | Riesgo |
|---|---|
| Múltiples serpientes consumen ratones/obstáculos simultáneamente | Modificación concurrente de `HashSet` sin protección → estado inconsistente |
| `randomEmpty()` genera posiciones nuevas mientras otra serpiente lee el tablero | Corrupción del mapa de posiciones libres |
| La serpiente avanza (`advance()`) mientras la UI dibuja el cuerpo | Lectura parcial del `ArrayDeque` → `ConcurrentModificationException` |
| Flag `paused` leído por hilos trabajadores sin barrera de memoria | Posible busy-waiting o missed wakeup |

**Estructuras no seguras en el código original:**

- `HashSet<Position>` en `Board` — no thread-safe para escrituras concurrentes
- `ArrayDeque<Position>` en `Snake.body` — no thread-safe
- `ArrayList<Snake>` como contenedor de serpientes — lanza `ConcurrentModificationException` al iterar mientras se agregan/eliminan

**Espera activa eliminada:**

El mecanismo de pausa original podría haber utilizado un bucle `while (paused) {}` (busy-waiting) que consume CPU innecesariamente. Se sustituyó por el patrón monitor con `wait()` / `notifyAll()` en la clase `PauseControl`.

---

### 2. Correcciones mínimas — Regiones críticas protegidas

Solo se protegen las secciones estrictamente necesarias:

| Clase | Protección aplicada | Por qué |
|---|---|---|
| `Board` | Todos los métodos `public` son `synchronized`; los getters retornan copias defensivas | `board.step()` modifica colecciones leídas también por la UI |
| `Snake` | Métodos `synchronized`; campos `volatile alive` y `volatile direction` | Runner y UI acceden concurrentemente al cuerpo y estado |
| `SnakeApp.snakes` | `CopyOnWriteArrayList<Snake>` | La iteración en el render no lanza `ConcurrentModificationException`; las escrituras (inicialización) son infrecuentes |
| `PauseControl` | Monitor clásico (`synchronized` + `while (paused) { wait(); }`) | Bloquea runners sin consumir CPU; `notifyAll()` en `resume()` despierta a todos |
| `GameClock.state` | `AtomicReference<GameState>` | Cambios atómicos de estado sin lock explícito |

El orden de adquisición de locks es siempre **board → snake** (nunca al revés), eliminando el riesgo de deadlock.

---

### 3. Control de ejecución seguro (UI)

El botón **Pause / Resume** y la tecla **SPACE** llaman a `togglePause()`, que coordina dos mecanismos:

1. `clock.pause()` → detiene el `ScheduledExecutorService` del reloj (sin repaint mientras pausado).
2. `pauseControl.pause()` → los runners llaman a `checkPause()` en cada iteración y se bloquean con `wait()` hasta que se invoque `resume()`.

Al pausar se muestran inmediatamente:

- **Serpiente viva más larga:** `snakes.stream().filter(alive).max(bodySize)`
- **Primera en morir:** `snakes.stream().filter(dead).min(deathTime)`

La lectura de `isAlive()` en `paintComponent()` se realiza **una sola vez por serpiente** antes del loop de segmentos, evitando que un mismo fotograma pinte algunos segmentos en color y otros en gris (tearing visual).

---

### 4. Robustez bajo carga

Prueba con 20 serpientes (`-Dsnakes=20`):

- **Sin `ConcurrentModificationException`:** la iteración sobre `CopyOnWriteArrayList` opera sobre una snapshot inmutable.
- **Sin lecturas inconsistentes:** `Board.step()` es atómico (lock de tablero); `Snake.snapshot()` toma copia defensiva bajo lock de serpiente.
- **Sin deadlocks:** orden de locks siempre board → snake; `PauseControl` usa un único monitor.
- **Sin degradación notable de rendimiento:** los hilos virtuales no saturan el pool de hilos del SO.

Los colores de las serpientes se generan con el ángulo de tono HSB distribuido por *golden ratio* (`id × 0.618 mod 1`), permitiendo distinguir visualmente hasta decenas de serpientes sin repetir colores similares.

---

## Ejecutar pruebas

```bash
mvn clean verify
```
