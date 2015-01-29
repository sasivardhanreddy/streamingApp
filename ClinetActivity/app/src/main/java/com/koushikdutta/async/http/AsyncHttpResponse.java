package com.koushikdutta.async.http;

import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.DataEmitter;

public interface AsyncHttpResponse extends DataEmitter {
    public String protocol();
    public String message();
    public int code();
    public Headers headers();
    public AsyncSocket detachSocket();
    public AsyncHttpRequest getRequest();
}
