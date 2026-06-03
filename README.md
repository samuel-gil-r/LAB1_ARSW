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

```java
// Worker llama esto antes de cada número — bloquea si pause == true
public synchronized void checkPause() {
    while (pause) {
        try {
            wait();
        } catch (Exception e) {
            System.out.println("fail the waiting method");
        }
    }
}

// Llamado tras presionar ENTER — despierta todos los workers
public synchronized void resumeThread() {
    pause = false;
    notifyAll();
}
```

### Evidencia de ejecución

![Pausa con conteo de primos](https://github.com/user-attachments/assets/ef31c655-e73a-4cf1-98c4-dec801a406f4)

![Reanudación tras ENTER](https://github.com/user-attachments/assets/25d69607-2cc3-4d94-9d5a-f7d7f367077f)

### Conclusiones

- **Sin busy-waiting:** los workers llaman `wait()` dentro del `while(pause)` — ceden la CPU completamente mientras están pausados.
- **Sin lost wakeups:** el `while (pause)` re-verifica la condición al despertar, protegiendo contra *spurious wakeups*.
- **Monitor único:** `Control.this` es el único objeto de sincronización; `checkPause()` y `resumeThread()` comparten el mismo lock, eliminando el riesgo de deadlock por monitores cruzados.
- **Consistencia:** cuando `pause = true` se establece, los workers terminan su iteración actual antes de bloquearse — el conteo mostrado puede desfasarse por unos pocos primos, lo cual es aceptable para este ejercicio.

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
