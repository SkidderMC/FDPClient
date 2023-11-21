package cc.paimonmc.viamcp.handler;

import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.lang.reflect.InvocationTargetException;

public class CommonTransformer {
    public static final String HANDLER_DECODER_NAME = "via-decoder";
    public static final String HANDLER_ENCODER_NAME = "via-encoder";

    public static void decompress(final ChannelHandlerContext ctx, final ByteBuf buf) throws InvocationTargetException {
        final ChannelHandler handler = ctx.pipeline().get("decompress");
        final ByteBuf decompressed = handler instanceof MessageToMessageDecoder ? (ByteBuf) PipelineUtil.callDecode((MessageToMessageDecoder<?>) handler, ctx, buf).get(0) : (ByteBuf) PipelineUtil.callDecode((ByteToMessageDecoder) handler, ctx, buf).get(0);

        try {
            buf.clear().writeBytes(decompressed);
        } finally {
            decompressed.release();
        }
    }

    public static void compress(final ChannelHandlerContext ctx, final ByteBuf buf) throws Exception {
        final ByteBuf compressed = ctx.alloc().buffer();

        try {
            PipelineUtil.callEncode((MessageToByteEncoder<?>) ctx.pipeline().get("compress"), ctx, buf, compressed);
            buf.clear().writeBytes(compressed);
        } finally {
            compressed.release();
        }
    }
}