package featurea.keyboard

import android.text.Editable
import android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import featurea.android.FeatureaActivity
import featurea.android.MainActivityProxy
import featurea.android.RootLayoutProxy
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import

actual class KeyEventProducer actual constructor(override val module: Module) : Component {

    private val keyboard: Keyboard = import()
    private val mainActivity: FeatureaActivity = import(MainActivityProxy)
    private val rootLayout: LinearLayout = import(RootLayoutProxy)

    internal lateinit var numberInputTextField: EditText

    override fun onCreateComponent() {
        mainActivity.runOnUiThread {
            mainActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) // https://stackoverflow.com/a/9732789/909169
            numberInputTextField = EditText(mainActivity)
            numberInputTextField.layoutParams = ViewGroup.LayoutParams(40, 1)
            numberInputTextField.isFocusable = true
            numberInputTextField.isFocusableInTouchMode = true
            numberInputTextField.inputType = TYPE_NUMBER_FLAG_DECIMAL
            numberInputTextField.keyListener = DigitsKeyListener.getInstance("0123456789.")
            numberInputTextField.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            numberInputTextField.setImeActionLabel("Done", EditorInfo.IME_ACTION_DONE)
            numberInputTextField.addTextChangedListener(object : TextWatcher {

                private var inputSource: KeyEventSource? = null
                private var keyChar = 0.toChar()
                private var isConsumed = false

                override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                    inputSource = null
                    keyChar = 0.toChar()
                    isConsumed = false
                }

                override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                    if (count != 0 && count != 1) return
                    if (after != 0 && after != 1) return
                    if (count == after) return
                    if (count > after) {
                        keyChar = 0.toChar()
                        inputSource = KeyEventSource.BACKSPACE
                    } else {
                        val length = charSequence.length
                        keyChar = charSequence.subSequence(length - 1, length)[0]
                        inputSource = keyChar.toKeyEventSource()
                    }
                    keyboard.addEvent(KeyEvent(KeyEventType.PRESS, inputSource ?: KeyEventSource.UNKNOWN))
                    isConsumed = true
                }

                override fun afterTextChanged(editable: Editable) {
                    if (isConsumed) {
                        keyboard.addEvent(KeyEvent(KeyEventType.RELEASE, inputSource ?: KeyEventSource.UNKNOWN))
                    }
                }
            })
            numberInputTextField.setOnEditorActionListener { textView, actionId, keyEvent ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    keyboard.addEvent(KeyEvent(KeyEventType.PRESS, KeyEventSource.ENTER))
                    keyboard.addEvent(KeyEvent(KeyEventType.RELEASE, KeyEventSource.ENTER))
                    true
                } else {
                    false
                }
            }
            rootLayout.addView(numberInputTextField)
        }
    }

}
