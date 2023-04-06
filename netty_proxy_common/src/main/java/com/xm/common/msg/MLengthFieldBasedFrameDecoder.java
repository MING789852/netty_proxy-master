package com.xm.common.msg;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MLengthFieldBasedFrameDecoder extends LengthFieldBasedFrameDecoder {
    public MLengthFieldBasedFrameDecoder() {
        super(3*1024*1024, 0, 4);
    }
}
