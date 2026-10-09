package com.quiz.util;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small brute-force protection: after MAX_FAILURES wrong passwords for the same
 * (client, email) pair, further attempts are refused for LOCK_MILLIS.
 */
public final class LoginThrottle {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_MILLIS = 5 * 60 * 1000L;

    private static final class Entry {
        int failures;
        long lockedUntil;
    }

    private static final Map<String, Entry> ENTRIES = new ConcurrentHashMap<>();

    private LoginThrottle() {
    }

    private static String key(String client, String email) {
        return client + "|" + (email == null ? "" : email.toLowerCase());
    }

    public static boolean isBlocked(String client, String email) {
        Entry e = ENTRIES.get(key(client, email));
        return e != null && e.lockedUntil > System.currentTimeMillis();
    }

    public static void recordFailure(String client, String email) {
        purgeExpired();
        ENTRIES.compute(key(client, email), (k, e) -> {
            Entry entry = e == null ? new Entry() : e;
            entry.failures++;
            if (entry.failures >= MAX_FAILURES) {
                entry.lockedUntil = System.currentTimeMillis() + LOCK_MILLIS;
                entry.failures = 0;
            }
            return entry;
        });
    }

    public static void recordSuccess(String client, String email) {
        ENTRIES.remove(key(client, email));
    }

    private static void purgeExpired() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Entry>> it = ENTRIES.entrySet().iterator();
        while (it.hasNext()) {
            Entry e = it.next().getValue();
            if (e.failures == 0 && e.lockedUntil < now) {
                it.remove();
            }
        }
    }
}
