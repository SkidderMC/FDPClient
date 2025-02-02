/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.font.fontmanager

import net.ccbluex.liquidbounce.ui.font.CustomFontInfo
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.border.EmptyBorder

class CustomFontInfoEditor(
    title: String,
    default: CustomFontInfo
) : JDialog(null as JFrame?, title, true) {
    private val nameField = JTextField(20).apply {
        text = default.name
    }
    private val sizeField = JTextField(20).apply {
        text = default.fontSize.toString()

        inputVerifier = object : InputVerifier() {
            override fun verify(input: JComponent): Boolean {
                return (input as JTextField).text.toIntOrNull().let {
                    it != null && it > 0
                }
            }
        }
    }
    private var formData = default

    private val border = EmptyBorder(10, 10, 10, 10)

    private fun addGridItem(item: JComponent) {
        add(JPanel(BorderLayout()).apply {
            border = this@CustomFontInfoEditor.border
            add(item, BorderLayout.CENTER)
        })
    }

    init {
        layout = GridLayout(3, 2, 10, 10)

        addGridItem(JLabel("Name:"))
        addGridItem(nameField)

        addGridItem(JLabel("Size:"))
        addGridItem(sizeField)

        val submitButton = JButton("OK").apply {
            addActionListener { onSubmit() }
        }
        addGridItem(submitButton)

        val cancelButton = JButton("Cancel").apply {
            addActionListener { dispose() }
        }
        addGridItem(cancelButton)

        pack()
        setLocationRelativeTo(parent)
    }

    private fun onSubmit() {
        val name = nameField.text
        val fontSize = sizeField.text.toIntOrNull()
        if (name.isNotBlank() && fontSize != null) {
            formData = formData.copy(name = name, fontSize = fontSize)
            dispose()
        } else {
            JOptionPane.showMessageDialog(this, "Invalid font info!", "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    fun showDialog(): CustomFontInfo {
        isVisible = true
        return formData
    }
}