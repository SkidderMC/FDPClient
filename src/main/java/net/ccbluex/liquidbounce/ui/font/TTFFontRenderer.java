/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */

package net.ccbluex.liquidbounce.ui.font;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * Created by Zeb on 12/19/2016.
 */
public class TTFFontRenderer {

    /**
     * The margin on each texture.
     */
    private static final int MARGIN = 4;
    /**
     * The character that invokes color in a string when rendered.
     */
    private static final char COLOR_INVOKER = '\247';
    /**
     * The random offset in obfuscated text.
     */
    private static final int RANDOM_OFFSET = 1;
    /**
     * The font to be drawn.
     */
    @Getter
    private final Font font;
    /**
     * All the character data information (regular).
     */
    private final CharacterData[] regularData;
    /**
     * All the character data information (bold).
     */
    private final CharacterData[] boldData;
    /**
     * All the character data information (italics).
     */
    private final CharacterData[] italicsData;
    /**
     * All the color codes used in minecraft.
     */
    private final int[] colorCodes = new int[32];
    /**
     * If fractional metrics should be used in the font renderer.
     */
    private final boolean fractionalMetrics;

    public TTFFontRenderer(final Font font) {
        this(font, 256);
    }

    public TTFFontRenderer(final Font font, final int characterCount) {
        this(font, characterCount, true);
    }

    public TTFFontRenderer(final Font font, final int characterCount, final boolean fractionalMetrics) {
        this.font = font;
        this.fractionalMetrics = fractionalMetrics;

        // Generates all the character textures.
        this.regularData = setup(new CharacterData[characterCount], Font.PLAIN);
        this.boldData = setup(new CharacterData[characterCount], Font.BOLD);
        this.italicsData = setup(new CharacterData[characterCount], Font.ITALIC);
    }

    /**
     * Sets up the character data and textures.
     *
     * @param characterData The array of character data that should be filled.
     * @param type          The font type. (Regular, Bold, and Italics)
     */
    private CharacterData[] setup(final CharacterData[] characterData, final int type) {
        // Quickly generates the colors.
        generateColors();

        // Changes the type of the font to the given type.
        final Font font = this.font.deriveFont(type);

        // An image just to get font data.
        final BufferedImage utilityImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        // The graphics of the utility image.
        final Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();

        // Sets the font of the utility image to the font.
        utilityGraphics.setFont(font);

        // The font metrics of the utility image.
        final FontMetrics fontMetrics = utilityGraphics.getFontMetrics();

        // Iterates through all the characters in the character set of the font renderer.
        for (int index = 0; index < characterData.length; index++) {
            // The character at the current index.
            final char character = (char) index;

            // The width and height of the character according to the font.
            final Rectangle2D characterBounds = fontMetrics.getStringBounds(character + "", utilityGraphics);

            // The width of the character texture.
            final float width = (float) characterBounds.getWidth() + (2 * MARGIN);

            // The height of the character texture.
            final float height = (float) characterBounds.getHeight();

            // The image that the character will be rendered to.
            final BufferedImage characterImage = new BufferedImage(MathHelper.ceiling_double_int(width), MathHelper.ceiling_double_int(height), BufferedImage.TYPE_INT_ARGB);

            // The graphics of the character image.
            final Graphics2D graphics = (Graphics2D) characterImage.getGraphics();

            // Sets the font to the input font/
            graphics.setFont(font);

            // Sets the color to white with no alpha.
            graphics.setColor(new Color(255, 255, 255, 0));

            // Fills the entire image with the color above, makes it transparent.
            graphics.fillRect(0, 0, characterImage.getWidth(), characterImage.getHeight());

            // Sets the color to white to draw the character.
            graphics.setColor(Color.WHITE);

            // Enables anti-aliasing so the font doesn't have aliasing.
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, this.fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);

            // Draws the character.
            graphics.drawString(character + "", MARGIN, fontMetrics.getAscent());

            // Generates a new texture id.
            final int textureId = GlStateManager.generateTexture();

            // Allocates the texture in opengl.
            createTexture(textureId, characterImage);

            // Initiates the character data and stores it in the data array.
            characterData[index] = new CharacterData(character, characterImage.getWidth(), characterImage.getHeight(), textureId);
        }

        // Returns the filled character data array.
        return characterData;
    }

    /**
     * Uploads the opengl texture.
     *
     * @param textureId The texture id to upload to.
     * @param image     The image to upload.
     */
    private void createTexture(final int textureId, final BufferedImage image) {
        // Array of all the colors in the image.
        final int[] pixels = new int[image.getWidth() * image.getHeight()];

        // Fetches all the colors in the image.
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        // Buffer that will store the texture data.
        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB

        // Puts all the pixel data into the buffer.
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                // The pixel in the image.
                final int pixel = pixels[y * image.getWidth() + x];

                // Puts the data into the byte buffer.
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        // Flips the byte buffer, not sure why this is needed.
        buffer.flip();

        // Binds the opengl texture by the texture id.
        GlStateManager.bindTexture(textureId);

        // Sets the texture parameter stuff.
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Uploads the texture to opengl.
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Binds the opengl texture 0.
        GlStateManager.bindTexture(0);
    }

    /**
     * Renders the given string.
     *
     * @param text  The text to be rendered.
     * @param x     The x position of the text.
     * @param y     The y position of the text.
     * @param color The color of the text.
     */
    public void drawString(final String text, final float x, final float y, final int color) {
        renderString(text, x, y, color, false);
    }

    /**
     * Renders the given string.
     *
     * @param text  The text to be rendered.
     * @param x     The x position of the text.
     * @param y     The y position of the text.
     * @param color The color of the text.
     */
    public void drawStringWithShadow(final String text, final float x, final float y, final int color) {
        GL11.glTranslated(0.5, 0.5, 0);
        renderString(text, x, y, color, true);
        GL11.glTranslated(-0.5, -0.5, 0);
        renderString(text, x, y, color, false);
    }

    /**
     * Renders the given string.
     *
     * @param text   The text to be rendered.
     * @param x      The x position of the text.
     * @param y      The y position of the text.
     * @param shadow If the text should be rendered with the shadow color.
     * @param color  The color of the text.
     */
    private void renderString(final String text, float x, float y, final int color, final boolean shadow) {
        // Returns if the text is empty.
        if (text.isEmpty()) return;

        // Pushes the matrix to store gl values.
        GL11.glPushMatrix();

        // Scales down to make the font look better.
        GlStateManager.scale(0.5, 0.5, 1);

        // Removes half the margin to render in the right spot.
        x -= (float) MARGIN / 2;
        y -= (float) MARGIN / 2;

        // Adds 0.5 to x and y.
        x += 0.5F;
        y += 0.5F;

        // Doubles the position because of the scaling.
        x *= 2;
        y *= 2;

        // The character texture set to be used. (Regular by default)
        CharacterData[] characterData = regularData;

        // Booleans to handle the style.
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;

        // The length of the text used for the draw loop.
        final int length = text.length();

        // The multiplier.
        final float multiplier = (shadow ? 4 : 1);

        final float a = (float) (color >> 24 & 255) / 255F;
        final float r = (float) (color >> 16 & 255) / 255F;
        final float g = (float) (color >> 8 & 255) / 255F;
        final float b = (float) (color & 255) / 255F;

        GL11.glColor4f(r / multiplier, g / multiplier, b / multiplier, a);

        // Loops through the text.
        for (int i = 0; i < length; i++) {
            // The character at the index of 'i'.
            char character = text.charAt(i);

            // The previous character.
            final char previous = i > 0 ? text.charAt(i - 1) : '.';

            // Continues if the previous color was the color invoker.
            if (previous == COLOR_INVOKER) continue;

            // Sets the color if the character is the color invoker and the character index is less than the length.
            if (character == COLOR_INVOKER && i < length) {

                // The color index of the character after the current character.
                int index = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                // If the color character index is of the normal color invoking characters.
                if (index < 16) {
                    // Resets all the styles.
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;

                    // Sets the character data to the regular type.
                    characterData = regularData;

                    // Clamps the index just to be safe in case an odd character somehow gets in here.
                    if (index < 0 || index > 15) index = 15;

                    // Adds 16 to the color index to get the darker shadow color.
                    if (shadow) index += 16;

                    // Gets the text color from the color codes array.
                    final int textColor = this.colorCodes[index];

                    // Sets the current color.
                    GL11.glColor4d((textColor >> 16) / 255d, (textColor >> 8 & 255) / 255d, (textColor & 255) / 255d, a);
                } else if (index == 16)
                    obfuscated = true;
                else if (index == 17)
                    // Sets the character data to the bold type.
                    characterData = boldData;
                else if (index == 18)
                    strikethrough = true;
                else if (index == 19)
                    underlined = true;
                else if (index == 20)
                    // Sets the character data to the italics type.
                    characterData = italicsData;
                else if (index == 21) {
                    // Resets the style.
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;

                    // Sets the character data to the regular type.
                    characterData = regularData;

                    // Sets the color to white
                    GL11.glColor4d(1 * (shadow ? 0.25 : 1), 1 * (shadow ? 0.25 : 1), 1 * (shadow ? 0.25 : 1), a);
                }
            } else {
                // Continues to not crash!
                if (character > 255) continue;

                // Sets the character to a random char if obfuscated is enabled.
                if (obfuscated)
                    character = (char) (((int) character) + RANDOM_OFFSET);

                // Draws the character.
                drawChar(character, characterData, x, y);

                // The character data for the given character.
                final CharacterData charData = characterData[character];

                // Draws the strikethrough line if enabled.
                if (strikethrough)
                    drawLine(new Vector2f(0, charData.height / 2f), new Vector2f(charData.width, charData.height / 2f), 3);

                // Draws the underline if enabled.
                if (underlined)
                    drawLine(new Vector2f(0, charData.height - 15), new Vector2f(charData.width, charData.height - 15), 3);

                // Adds to the offset.
                x += charData.width - (2 * MARGIN);
            }
        }

        // Restores previous values.
        GL11.glPopMatrix();

        // Sets the color back to white so no odd rendering problems happen.
        GL11.glColor4d(1, 1, 1, 1);

        GlStateManager.bindTexture(0);
    }

    /**
     * Gets the width of the given text.
     *
     * @param text The text to get the width of.
     * @return The width of the given text.
     */
    public float getWidth(final String text) {

        // The width of the string.
        float width = 0;

        // The character texture set to be used. (Regular by default)
        CharacterData[] characterData = regularData;

        // The length of the text.
        final int length = text.length();

        // Loops through the text.
        for (int i = 0; i < length; i++) {
            // The character at the index of 'i'.
            final char character = text.charAt(i);

            // The previous character.
            final char previous = i > 0 ? text.charAt(i - 1) : '.';

            // Continues if the previous color was the color invoker.
            if (previous == COLOR_INVOKER) continue;

            // Sets the color if the character is the color invoker and the character index is less than the length.
            if (character == COLOR_INVOKER && i < length) {

                // The color index of the character after the current character.
                final int index = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                if (index == 17)
                    // Sets the character data to the bold type.
                    characterData = boldData;
                else if (index == 20)
                    // Sets the character data to the italics type.
                    characterData = italicsData;
                else if (index == 21)
                    // Sets the character data to the regular type.
                    characterData = regularData;
            } else {
                // Continues to not crash!
                if (character > 255) continue;

                // The character data for the given character.
                final CharacterData charData = characterData[character];

                // Adds to the offset.
                width += (charData.width - (2 * MARGIN)) / 2;
            }
        }

        // Returns the width.
        return width + (float) MARGIN / 2;
    }

    /**
     * Gets the height of the given text.
     *
     * @param text The text to get the height of.
     * @return The height of the given text.
     */
    public float getHeight(final String text) {

        // The height of the string.
        float height = 0;

        // The character texture set to be used. (Regular by default)
        CharacterData[] characterData = regularData;

        // The length of the text.
        final int length = text.length();

        // Loops through the text.
        for (int i = 0; i < length; i++) {
            // The character at the index of 'i'.
            final char character = text.charAt(i);

            // The previous character.
            final char previous = i > 0 ? text.charAt(i - 1) : '.';

            // Continues if the previous color was the color invoker.
            if (previous == COLOR_INVOKER) continue;

            // Sets the color if the character is the color invoker and the character index is less than the length.
            if (character == COLOR_INVOKER && i < length) {

                // The color index of the character after the current character.
                final int index = "0123456789abcdefklmnor".indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));

                if (index == 17)
                    // Sets the character data to the bold type.
                    characterData = boldData;
                else if (index == 20)
                    // Sets the character data to the italics type.
                    characterData = italicsData;
                else if (index == 21)
                    // Sets the character data to the regular type.
                    characterData = regularData;
            } else {
                // Continues to not crash!
                if (character > 255) continue;

                // The character data for the given character.
                final CharacterData charData = characterData[character];

                // Sets the height if its bigger.
                height = Math.max(height, charData.height);
            }
        }

        // Returns the height.
        return height / 2 - (float) MARGIN / 2;
    }

    /**
     * Draws the character.
     *
     * @param character     The character to be drawn.
     * @param characterData The character texture set to be used.
     */
    private void drawChar(final char character, final CharacterData[] characterData, final float x, final float y) {
        // The char data that stores the character data.
        final CharacterData charData = characterData[character];

        // Binds the character data texture.
        charData.bind();
        GL11.glPushMatrix();

        // Enables blending.
        GL11.glEnable(GL11.GL_BLEND);

        // Sets the blending function.
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Begins drawing the quad.
        GL11.glBegin(GL11.GL_QUADS);
        {
            // Maps out where the texture should be drawn.
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2d(x, y);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2d(x, y + charData.height);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2d(x + charData.width, y + charData.height);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2d(x + charData.width, y);
        }
        // Ends the quad.
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();

        // Binds the opengl texture by the texture id.
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    /**
     * Draws a line from start to end with the given width.
     *
     * @param start The starting point of the line.
     * @param end   The ending point of the line.
     * @param width The thickness of the line.
     */
    private void drawLine(final Vector2f start, final Vector2f end, final float width) {
        // Disables textures so we can draw a solid line.
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // Sets the width.
        GL11.glLineWidth(width);

        // Begins drawing the line.
        GL11.glBegin(GL11.GL_LINES);
        {
            GL11.glVertex2f(start.x, start.y);
            GL11.glVertex2f(end.x, end.y);
        }
        // Ends drawing the line.
        GL11.glEnd();

        // Enables texturing back on.
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    /**
     * Generates all the colors.
     */
    private void generateColors() {
        // Iterates through 32 colors.
        for (int i = 0; i < 32; i++) {
            // Not sure what this variable is.
            final int thingy = (i >> 3 & 1) * 85;

            // The red value of the color.
            int red = (i >> 2 & 1) * 170 + thingy;

            // The green value of the color.
            int green = (i >> 1 & 1) * 170 + thingy;

            // The blue value of the color.
            int blue = (i & 1) * 170 + thingy;

            // Increments the red by 85, not sure why does this in minecraft's font renderer.
            if (i == 6) red += 85;

            // Used to make the shadow darker.
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            // Sets the color in the color code at the index of 'i'.
            this.colorCodes[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    /**
     * Class that holds the data for each character.
     */
    static class CharacterData {

        /**
         * The id of the character texture.
         */
        private final int textureId;
        /**
         * The character the data belongs to.
         */
        public char character;
        /**
         * The width of the character.
         */
        public float width;
        /**
         * The height of the character.
         */
        public float height;

        public CharacterData(final char character, final float width, final float height, final int textureId) {
            this.character = character;
            this.width = width;
            this.height = height;
            this.textureId = textureId;
        }

        /**
         * Binds the texture.
         */
        public void bind() {
            // Binds the opengl texture by the texture id.
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        }
    }
}