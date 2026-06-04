package co.eci.snake.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;

public final class Snake {

  private static final AtomicInteger ID_GEN = new AtomicInteger(0);

  private final int id;
  private final Deque<Position> body = new ArrayDeque<>();
  private volatile Direction direction;
  private int maxLength = 5;

  private volatile boolean alive = true;
  private volatile long deathTime = Long.MAX_VALUE;

  private Snake(Position start, Direction dir) {
    this.id = ID_GEN.getAndIncrement();
    body.addFirst(start);
    this.direction = dir;
  }

  public static Snake of(int x, int y, Direction dir) {
    return new Snake(new Position(x, y), dir);
  }

  public int id() { return id; }

  public boolean isAlive() { return alive; }

  public long deathTime() { return deathTime; }

  public void markDead() {
    alive = false;
    deathTime = System.currentTimeMillis();
  }

  public Direction direction() { return direction; }


  public synchronized void turn(Direction dir) {
    if ((direction == Direction.UP    && dir == Direction.DOWN)  ||
        (direction == Direction.DOWN  && dir == Direction.UP)    ||
        (direction == Direction.LEFT  && dir == Direction.RIGHT) ||
        (direction == Direction.RIGHT && dir == Direction.LEFT)) {
      return;
    }
    this.direction = dir;
  }

  public synchronized Position head() { return body.peekFirst(); }

  public synchronized Deque<Position> snapshot() { return new ArrayDeque<>(body); }

  public synchronized int bodySize() { return body.size(); }

  public synchronized void advance(Position newHead, boolean grow) {
    body.addFirst(newHead);
    if (grow) maxLength++;
    while (body.size() > maxLength) body.removeLast();
  }
}
