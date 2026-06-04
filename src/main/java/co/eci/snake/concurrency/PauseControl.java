package co.eci.snake.concurrency;

public final class PauseControl {

    private boolean paused = false;

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }


    public synchronized void checkPause() throws InterruptedException {
        while (paused) {
            wait();
        }
    }
}
