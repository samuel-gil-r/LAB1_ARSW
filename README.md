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
<img width="665" height="561" alt="image" src="https://github.com/user-attachments/assets/f12414f0-0c51-4107-85f1-524f4853fed0" />


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

## Ejecutar pruebas

```bash
mvn clean verify
```
