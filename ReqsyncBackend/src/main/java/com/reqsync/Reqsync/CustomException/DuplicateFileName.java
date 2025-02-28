package com.reqsync.Reqsync.CustomException;

public class DuplicateFileName extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public DuplicateFileName(String message) {
        super(message);
    }

}
