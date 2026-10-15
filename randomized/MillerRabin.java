package mediamine.randomized;

import java.math.BigInteger;
import java.util.Random;

/**
 * Miller-Rabin probabilistic primality test.
 * MediaMine use case: large numerical processing, e.g. generating/validating
 * large IDs, hash moduli, or cryptographic-style tokens for secure content
 * links, where trial division is too slow.
 */
public class MillerRabin {

    private static final Random RANDOM = new Random();

    public static boolean isProbablyPrime(BigInteger n, int iterations) {
        if (n.compareTo(BigInteger.TWO) < 0) return false;
        if (n.equals(BigInteger.TWO) || n.equals(BigInteger.valueOf(3))) return true;
        if (n.testBit(0) == false) return false; // even

        BigInteger d = n.subtract(BigInteger.ONE);
        int r = 0;
        while (d.testBit(0) == false) {
            d = d.shiftRight(1);
            r++;
        }

        for (int i = 0; i < iterations; i++) {
            BigInteger a = uniformRandom(BigInteger.TWO, n.subtract(BigInteger.TWO));
            BigInteger x = a.modPow(d, n);
            if (x.equals(BigInteger.ONE) || x.equals(n.subtract(BigInteger.ONE))) continue;

            boolean composite = true;
            for (int j = 0; j < r - 1; j++) {
                x = x.modPow(BigInteger.TWO, n);
                if (x.equals(n.subtract(BigInteger.ONE))) {
                    composite = false;
                    break;
                }
            }
            if (composite) return false;
        }
        return true;
    }

    private static BigInteger uniformRandom(BigInteger min, BigInteger max) {
        BigInteger range = max.subtract(min).add(BigInteger.ONE);
        BigInteger result;
        do {
            result = new BigInteger(range.bitLength(), RANDOM);
        } while (result.compareTo(range) >= 0);
        return result.add(min);
    }

    public static void main(String[] args) {
        BigInteger[] candidates = {
                BigInteger.valueOf(1_000_000_007L),
                BigInteger.valueOf(1_000_000_008L),
                new BigInteger("170141183460469231731687303715884105727") // Mersenne prime 2^127-1
        };
        for (BigInteger n : candidates) {
            System.out.println(n + " is probably prime: " + isProbablyPrime(n, 20));
        }
    }
}
