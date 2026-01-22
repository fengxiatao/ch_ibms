package com.jokeep.enhance;

public class ResultVo {
    private int code;
    private String msg;
    private Object data;
    private long time;

    public ResultVo(int code, String msg){
        this(code,msg,null);
    }

    public ResultVo(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public void setCode(int code) {
        this.code=code;
    }

    public void setMsg(String msg) {
        this.msg=msg;
    }

    public void setData(Object data) {
        this.data=data;
    }
}
