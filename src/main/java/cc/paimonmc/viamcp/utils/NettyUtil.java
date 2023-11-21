package cc.paimonmc.viamcp.utils;

import cc.paimonmc.viamcp.handler.CommonTransformer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;

public class NettyUtil {
    public static ChannelPipeline decodeEncodePlacement(final ChannelPipeline instance, String base, final String newHandler, final ChannelHandler handler) {
        switch (base) {
            case "decoder": {
                if (instance.get(CommonTransformer.HANDLER_DECODER_NAME) != null) {
                    base = CommonTransformer.HANDLER_DECODER_NAME;
                }

                break;
            }
            case "encoder": {
                if (instance.get(CommonTransformer.HANDLER_ENCODER_NAME) != null) {
                    base = CommonTransformer.HANDLER_ENCODER_NAME;
                }

                break;
            }
        }

        return instance.addBefore(base, newHandler, handler);
    }
}
