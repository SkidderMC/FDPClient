package net.ccbluex.liquidbounce.handler.protocol.api;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import io.netty.channel.ChannelHandlerContext;
import net.raphimc.vialoader.netty.VLLegacyPipeline;

public class ProtocolVLLegacyPipeline extends VLLegacyPipeline {
    public ProtocolVLLegacyPipeline(UserConnection user, ProtocolVersion version) {
        super(user, version);
    }

    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().addBefore(this.packetDecoderName(), "via-decoder", this.createViaDecoder());
        ctx.pipeline().addBefore(this.packetEncoderName(), "via-encoder", this.createViaEncoder());
    }

    protected String decompressName() {
        return "decompress";
    }

    protected String compressName() {
        return "compress";
    }

    protected String packetDecoderName() {
        return "decoder";
    }

    protected String packetEncoderName() {
        return "encoder";
    }

    protected String lengthSplitterName() {
        return "splitter";
    }

    protected String lengthPrependerName() {
        return "prepender";
    }
}
