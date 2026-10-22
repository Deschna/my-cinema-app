package cinema.dao;

import org.hibernate.exception.ConstraintViolationException;

public final class MySqlConstraintErrors {
    private static final int MYSQL_DUPLICATE_KEY_ERROR_CODE = 1062;
    private static final int MYSQL_ROW_IS_REFERENCED_ERROR_CODE = 1451;

    private MySqlConstraintErrors() {
    }

    public static boolean isDuplicateKey(
            Throwable failure, String tableName, String constraintName) {
        ConstraintViolationException violation = findConstraintViolation(
                failure, MYSQL_DUPLICATE_KEY_ERROR_CODE);
        if (violation == null) {
            return false;
        }
        String reportedName = violation.getConstraintName();
        // MySQL can report the key with or without its table name.
        return constraintName.equals(reportedName)
                || (tableName + "." + constraintName).equals(reportedName);
    }

    public static boolean isReferencedRow(Throwable failure) {
        return findConstraintViolation(failure, MYSQL_ROW_IS_REFERENCED_ERROR_CODE) != null;
    }

    private static ConstraintViolationException findConstraintViolation(
            Throwable failure, int errorCode) {
        // Hibernate failures may be nested inside persistence or transaction exceptions.
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && constraintViolation.getErrorCode() == errorCode) {
                return constraintViolation;
            }
        }
        return null;
    }
}
