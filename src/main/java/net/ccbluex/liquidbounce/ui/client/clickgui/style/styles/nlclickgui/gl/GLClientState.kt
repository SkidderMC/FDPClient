package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.gl

enum class GLClientState(
    val glName: String,
    override val cap: Int
) : GLenum {
    COLOR("GL_COLOR_ARRAY", 0x8076),
    EDGE("GL_EDGE_FLAG_ARRAY", 0x8079),
    FOG("GL_FOG_COORD_ARRAY", 0x8457),
    INDEX("GL_INDEX_ARRAY", 0x8077),
    NORMAL("GL_NORMAL_ARRAY", 0x8075),
    SECONDARY_COLOR("GL_SECONDARY_COLOR_ARRAY", 0x845E),
    TEXTURE("GL_TEXTURE_COORD_ARRAY", 0x8078),
    VERTEX("GL_VERTEX_ARRAY", 0x8074)
}
