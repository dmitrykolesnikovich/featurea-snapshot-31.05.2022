package featurea.keyboard

import featurea.Application
import featurea.desktop.MainPanelProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import java.awt.event.KeyAdapter
import javax.swing.JPanel
import java.awt.event.KeyEvent as SwingKeyEvent

actual class KeyEventProducer actual constructor(override val module: Module) : Component, KeyAdapter() {

    private val app: Application = import()
    private val keyboard: Keyboard = import()
    private val mainPanel: JPanel = import(MainPanelProxy)

    override fun onCreateComponent() {
        mainPanel.addKeyListener(this)
    }

    override fun onDeleteComponent() {
        mainPanel.removeKeyListener(this)
    }

    override fun keyPressed(swingKeyEvent: SwingKeyEvent?) {
        if (!app.isEnable) return
        if (swingKeyEvent == null) return
        val keyEventSource: KeyEventSource = findKeyEventSourceFromSwingKeyEvent(swingKeyEvent)
        keyboard.addEvent(KeyEvent(type = KeyEventType.PRESS, source = keyEventSource))
    }

    override fun keyReleased(swingKeyEvent: SwingKeyEvent?) {
        if (!app.isEnable) return
        if (swingKeyEvent == null) return
        val keyEventSource: KeyEventSource = findKeyEventSourceFromSwingKeyEvent(swingKeyEvent)
        keyboard.addEvent(KeyEvent(type = KeyEventType.RELEASE, source = keyEventSource))
    }

    override fun keyTyped(swingKeyEvent: SwingKeyEvent?) {
        if (!app.isEnable) return
        if (swingKeyEvent == null) return
        val keyEventSource: KeyEventSource = findKeyEventSourceFromSwingKeyEvent(swingKeyEvent)
        keyboard.addEvent(KeyEvent(type = KeyEventType.CLICK, source = keyEventSource))
    }

}
