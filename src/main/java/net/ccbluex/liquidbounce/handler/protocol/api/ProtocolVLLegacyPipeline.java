package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.ChannelHandlerContext;
import net.raphimc.vialoader.netty.VLLegacyPipeline;

public class ProtocolVLLegacyPipeline extends VLLegacyPipeline {

    public ProtocolVLLegacyPipeline(UserConnection user, ProtocolVersion version) {
        super(user, version);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().addBefore(this.packetDecoderName(), VIA_DECODER_NAME, this.createViaDecoder());
        ctx.pipeline().addBefore(this.packetEncoderName(), VIA_ENCODER_NAME, this.createViaEncoder());
    }

    @Override
    protected String decompressName() {
        return "decompress";
    }

    @Override
    protected String compressName() {
        return "compress";
    }

    @Override
    protected String packetDecoderName() {
        return "decoder";
    }

    @Override
    protected String packetEncoderName() {
        return "encoder";
    }

    @Override
    protected String lengthSplitterName() {
        return "splitter";
    }

    @Override
    protected String lengthPrependerName() {
        return "prepender";
    }

}