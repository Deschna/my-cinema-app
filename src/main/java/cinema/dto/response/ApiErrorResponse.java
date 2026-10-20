package cinema.dto.response;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Getters are used by Jackson during JSON serialization.
@SuppressWarnings("unused")
public final class ApiErrorResponse {
    private final String timestamp;
    private final int status;
    private final ApiErrorCode code;
    private final String message;
    private final String path;
    private final Map<String, List<String>> fieldErrors;

    public ApiErrorResponse(int status, ApiErrorCode code, String message, String path,
                            Map<String, List<String>> fieldErrors) {
        if (status < 400 || status > 599) {
            throw new IllegalArgumentException("Error status must be between 400 and 599");
        }
        this.timestamp = Instant.now().toString();
        this.status = status;
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
        this.path = Objects.requireNonNull(path);
        Map<String, List<String>> copiedFieldErrors = new LinkedHashMap<>();
        Objects.requireNonNull(fieldErrors).forEach((field, errors) ->
                copiedFieldErrors.put(Objects.requireNonNull(field), List.copyOf(errors)));
        this.fieldErrors = Collections.unmodifiableMap(copiedFieldErrors);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }
}
