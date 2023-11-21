package cc.paimonmc.viamcp.handler;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@ChannelHandler.Sharable
public class MCPDecodeHandler extends MessageToMessageDecoder<ByteBuf> {
    private final UserConnection info;
    private boolean handledCompression;
    private boolean skipDoubleTransform;

    public MCPDecodeHandler(final UserConnection info) {
        this.info = info;
    }

    public UserConnection getInfo() {
        return info;
    }

    // https://github.com/ViaVersion/ViaVersion/blob/master/velocity/src/main/java/us/myles/ViaVersion/velocity/handlers/VelocityDecodeHandler.java
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf bytebuf, final List<Object> out) throws Exception {
        if (skipDoubleTransform) {
            skipDoubleTransform = false;
            out.add(bytebuf.retain());
            return;
        }

        if (!info.checkIncomingPacket()) {
            throw CancelDecoderException.generate(null);
        }

        if (!info.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);

        try {
            final boolean needsCompress = handleCompressionOrder(ctx, transformedBuf);

            info.transformIncoming(transformedBuf, CancelDecoderException::generate);

            if (needsCompress) {
                CommonTransformer.compress(ctx, transformedBuf);
                skipDoubleTransform = true;
            }

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    private boolean handleCompressionOrder(final ChannelHandlerContext ctx, final ByteBuf buf) throws InvocationTargetException {
        if (handledCompression) {
            return false;
        }

        final int decoderIndex = ctx.pipeline().names().indexOf("decompress");

        if (decoderIndex == -1) {
            return false;
        }

        handledCompression = true;

        if (decoderIndex > ctx.pipeline().names().indexOf("via-decoder")) {
            // Need to decompress this packet due to bad order
            CommonTransformer.decompress(ctx, buf);
            final ChannelHandler encoder = ctx.pipeline().get("via-encoder");
            final ChannelHandler decoder = ctx.pipeline().get("via-decoder");
            ctx.pipeline().remove(encoder);
            ctx.pipeline().remove(decoder);
            ctx.pipeline().addAfter("compress", "via-encoder", encoder);
            ctx.pipeline().addAfter("decompress", "via-decoder", decoder);
            return true;
        }

        return false;
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) {
            return;
        }
        super.exceptionCaught(ctx, cause);
    }
}