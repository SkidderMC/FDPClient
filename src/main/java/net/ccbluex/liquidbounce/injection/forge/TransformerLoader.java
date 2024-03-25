/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge;

import net.ccbluex.liquidbounce.injection.transformers.AbstractJavaLinkerHandler;
import net.ccbluex.liquidbounce.injection.transformers.ForgeNetworkTransformer;
import net.ccbluex.liquidbounce.injection.transformers.OptimizeTransformer;
import net.ccbluex.liquidbounce.injection.transformers.ViaForgeSupportTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class TransformerLoader implements IFMLLoadingPlugin {

    /**
     * Return a list of classes that implements the IClassTransformer interface
     */
    public TransformerLoader() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.fdpclient.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{ForgeNetworkTransformer.class.getName(), AbstractJavaLinkerHandler.class.getName(), OptimizeTransformer.class.getName(), ViaForgeSupportTransformer.class.getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}