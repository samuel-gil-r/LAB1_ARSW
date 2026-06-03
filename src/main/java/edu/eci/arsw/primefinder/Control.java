package edu.eci.arsw.primefinder;

import java.util.Scanner;

public class Control extends Thread {

    private static final int NTHREADS = 3;
    private static final int MAXVALUE = 30_000_000;
    private static final int TMILISECONDS = 5_000;

    private boolean pause = false;

    private final PrimeFinderThread[] pft;

    private Control() {
        pft = new PrimeFinderThread[NTHREADS];
        int chunk = MAXVALUE / NTHREADS;
        for (int i = 0; i < NTHREADS - 1; i++) {
            pft[i] = new PrimeFinderThread(i * chunk, (i + 1) * chunk, this);
        }
        pft[NTHREADS - 1] = new PrimeFinderThread((NTHREADS - 1) * chunk, MAXVALUE + 1, this);
    }

    public static Control newControl() {
        return new Control();
    }


    public synchronized void checkPause() throws InterruptedException {
        while (pause) {
            wait();
        }
    }

    public synchronized void resumeThread() {
        pause = false;
        notifyAll();
    }

    @Override
    public void run() {
        for (PrimeFinderThread t : pft) {
            t.start();
        }

        Scanner scanner = new Scanner(System.in);

        while (anyAlive()) {
            try {
                Thread.sleep(TMILISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            synchronized (this) {
                pause = true;
            }

            long total = countPrimes();
            System.out.println("\n>>> PAUSA — Primos encontrados hasta ahora: " + total);
            System.out.println("    Presiona ENTER para continuar...");
            scanner.nextLine();

            resumeThread();
        }

        for (PrimeFinderThread t : pft) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }

        System.out.println("\n=== FIN — Total de primos en [0, " + MAXVALUE + "]: "
                + countPrimes() + " ===");
        scanner.close();
    }

    private boolean anyAlive() {
        for (PrimeFinderThread t : pft) {
            if (t.isAlive()) return true;
        }
        return false;
    }

    private long countPrimes() {
        long total = 0;
        for (PrimeFinderThread t : pft) {
            total += t.getPrimesCount();
        }
        return total;
    }
}
