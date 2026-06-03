package edu.eci.arsw.primefinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrimeFinderThread extends Thread {

    private final int a;
    private final int b;
    private final List<Integer> primes = new ArrayList<>();
    private final Control control;

    public PrimeFinderThread(int a, int b, Control control) {
        this.a = a;
        this.b = b;
        this.control = control;
    }

    @Override
    public void run() {
        for (int i = a; i < b; i++) {
            control.checkPause();
            if (isPrime(i)) {
                primes.add(i);
            }
        }
    }

    public int getPrimesCount() {
        return primes.size();
    }

    public List<Integer> getPrimes() {
        return Collections.unmodifiableList(primes);
    }

    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        for (int i = 3; (long) i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
