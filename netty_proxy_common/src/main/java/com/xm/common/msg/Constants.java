package com.xm.common.msg;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public interface Constants {
    AttributeKey<Channel> NEXT_CHANNEL = AttributeKey.newInstance("nxt_channel");
    AttributeKey<String> INSTANCE=AttributeKey.newInstance("instance");
}
