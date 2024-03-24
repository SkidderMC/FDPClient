/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.injection.forge;

import net.ccbluex.liquidbounce.handler.script.remapper.injection.transformers.AbstractJavaLinkerTransformer;
import net.ccbluex.liquidbounce.injection.transformers.ForgeNetworkTransformer;
import net.ccbluex.liquidbounce.injection.transformers.OptimizeTransformer;
import net.ccbluex.liquidbounce.injection.transformers.ViaForgeSupportTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class TransformerLoader implements IFMLLoadingPlugin {

    public TransformerLoader() {
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.fdpclient.json");
        MixinEnvironment.getDefaultEnvironment().setSide(MixinEnvironment.Side.CLIENT);
    }

    /**
     * Return a list of classes that implements the IClassTransformer interface
     */
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ForgeNetworkTransformer.class.getName(), AbstractJavaLinkerTransformer.class.getName(), ViaForgeSupportTransformer.class.getName(), OptimizeTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    /**
     * Return the class name of an implementor of "IFMLCallHook", that will be run, in the
     * main thread, to perform any additional setup this coremod may require. It will be
     * run <strong>prior</strong> to Minecraft starting, so it CANNOT operate on minecraft
     * itself. The game will deliberately crash if this code is detected to trigger a
     * minecraft class loading
     */
    @Override
    public String getSetupClass() {
        return null;
    }

    /**
     * Inject coremod data into this coremod
     * This data includes:
     * "mcLocation" : the location of the minecraft directory,
     * "coremodList" : the list of coremods
     * "coremodLocation" : the file this coremod loaded from,
     *
     * @param data
     */
    @Override
    public void injectData(Map<String, Object> data) {

    }

    /**
     * Return an optional access transformer class for this coremod. It will be injected post-deobf
     * so ensure your ATs conform to the new srgnames scheme.
     *
     * @return the name of an access transformer class or null if none is provided
     */
    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}