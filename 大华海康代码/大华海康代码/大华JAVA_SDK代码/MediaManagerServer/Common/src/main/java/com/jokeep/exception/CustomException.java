package com.jokeep.exception;

public class CustomException extends RuntimeException {
    private int code;
    private int level;//异常提示等级 ，默认为 0，toast  1,dialog

    public CustomException(String error) {
        super(error);
    }

    public CustomException(String error, int code) {
        super(error);
        this.code = code;
        this.level = 0;
    }

    public CustomException(String error, int code, int level) {
        super(error);
        this.code = code;
        this.level = level;
    }

    public CustomException(String error, Throwable e) {
        super(error, e);
    }

    public CustomException(String error, Throwable e, int code) {
        super(error, e);
        this.code = code;
        this.level = 0;
    }

    public int getCode() {
        return code;
    }

    public int getLevel() {
        return level;
    }
}