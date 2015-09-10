package com.drmtx.app.exception;

import org.springframework.http.HttpStatus;

public class RedditCommentsAnalysisException extends Exception {

    private static final long serialVersionUID = -3435780724135290271L;

    private HttpStatus httpStatus;

    public RedditCommentsAnalysisException() {
    }

    public RedditCommentsAnalysisException(String message) {
        this(null, message);
    }

    public RedditCommentsAnalysisException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public RedditCommentsAnalysisException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public RedditCommentsAnalysisException(HttpStatus httpStatus, String msg, Throwable cause) {
        this(msg, cause);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
