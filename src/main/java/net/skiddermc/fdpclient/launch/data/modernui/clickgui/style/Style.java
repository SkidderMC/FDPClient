/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.skiddermc.fdpclient.launch.data.modernui.clickgui.style;

import net.skiddermc.fdpclient.launch.data.modernui.clickgui.Panel;
import net.skiddermc.fdpclient.launch.data.modernui.clickgui.elements.ButtonElement;
import net.skiddermc.fdpclient.launch.data.modernui.clickgui.elements.ModuleElement;
import net.skiddermc.fdpclient.utils.MinecraftInstance;

public abstract class Style extends MinecraftInstance {

    public abstract void drawPanel(final int mouseX, final int mouseY, final Panel panel);

    public abstract void drawDescription(final int mouseX, final int mouseY, final String text);

    public abstract void drawButtonElement(final int mouseX, final int mouseY, final ButtonElement buttonElement);

    public abstract void drawModuleElement(final int mouseX, final int mouseY, final ModuleElement moduleElement);

}
