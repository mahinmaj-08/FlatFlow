package flatflow.data;

import java.sql.SQLException;

/** Wraps a database error so services don't have to deal with SQLException. Becomes HTTP 500. */
public class DataException extends RuntimeException {
    public DataException(SQLException cause) {
        super("Database error: " + cause.getMessage(), cause);
    }
}
