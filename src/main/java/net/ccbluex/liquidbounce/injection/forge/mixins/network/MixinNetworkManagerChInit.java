/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import cc.paimonmc.viamcp.ViaMCP;
import cc.paimonmc.viamcp.handler.CommonTransformer;
import cc.paimonmc.viamcp.handler.MCPDecodeHandler;
import cc.paimonmc.viamcp.handler.MCPEncodeHandler;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public abstract class MixinNetworkManagerChInit {

    @Inject(method={"initChannel"}, at={@At(value="TAIL")}, remap=false)
    private void onInitChannel(Channel p_initChannel_1_, CallbackInfo callbackInfo) {
        if (p_initChannel_1_ instanceof SocketChannel && ViaMCP.getInstance().getVersion() != ViaMCP.PROTOCOL_VERSION) {
            UserConnection user = new UserConnectionImpl(p_initChannel_1_, true);
            new ProtocolPipelineImpl(user);
            p_initChannel_1_.pipeline().addBefore("encoder", CommonTransformer.HANDLER_ENCODER_NAME, new MCPEncodeHandler(user)).addBefore("decoder", CommonTransformer.HANDLER_DECODER_NAME, new MCPDecodeHandler(user));
        }
    }
}
