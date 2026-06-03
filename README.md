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


```


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
