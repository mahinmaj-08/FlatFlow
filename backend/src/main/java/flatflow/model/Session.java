package flatflow.model;

import java.time.Instant;

/** A signed-in browser: the random token it sends, whose it is, and when it was created. */
public record Session(String token, int userId, Instant createdAt) {}
