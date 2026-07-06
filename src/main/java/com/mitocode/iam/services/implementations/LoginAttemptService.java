package com.mitocode.iam.services.implementations;

import com.mitocode.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private record Attempts(int count, Instant first) {}
    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();

    public void check(String email) {
        var value = attempts.get(key(email));
        if (value != null && value.first().isAfter(Instant.now().minus(5, ChronoUnit.MINUTES)) && value.count() >= 5) {
            throw new TooManyRequestsException("Demasiados intentos. Intente nuevamente en unos minutos");
        }
    }

    public void failure(String email) {
        attempts.compute(key(email), (ignored, old) -> old == null || old.first().isBefore(Instant.now().minus(5, ChronoUnit.MINUTES))
                ? new Attempts(1, Instant.now()) : new Attempts(old.count() + 1, old.first()));
    }

    public void success(String email) { attempts.remove(key(email)); }
    private String key(String email) { return email == null ? "" : email.trim().toLowerCase(Locale.ROOT); }
}
