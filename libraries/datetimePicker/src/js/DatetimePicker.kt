package featurea.datetimePicker

import featurea.js.RootElementProxy
import featurea.js.keep
import featurea.runtime.Component
import featurea.runtime.Module
import featurea.runtime.import
import kotlinx.html.*
import kotlinx.html.dom.append
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.get

// website/public/index.html
/*
<body>
    <!-- skip -->

    <!-- timePicker -->
    <script src="<%= BASE_URL %>js/jquery.min.js"></script>
    <script src="<%= BASE_URL %>js/timepicki.js"></script>
    <link rel="stylesheet" href="<%= BASE_URL %>js/timepicki.css">
    <div class="content" style="left: 0; top: 0; width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; overflow: hidden">
        <input type="text" name="timepicker" class="time_element" style="display: none;"/>
    </div>
    <script>$(document).ready(function() { $(".time_element").timepicki({ show_meridian: false, disable_keyboard_mobile: true }); });</script>
</body>
*/
/*
<div id="timePickerDiv" class="content input-disable" style="left: 0; top: 0; width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; overflow: hidden">
    <input type="text" name="timepicker" class="time_element" style="display: none;"/>
</div>
<script>$(document).ready(function() { $('.time_element').timepicki({ show_meridian: false, disable_keyboard_mobile: true }); });</script>
*/
actual class DatetimePicker actual constructor(override val module: Module) : Component {

    private val rootElement: HTMLElement = import(RootElementProxy)

    init {
        rootElement.append {
            div {
                id = "timePickerDiv"
                classes += "content input-disable"
                style =
                    "left: 0; top: 0; width: 100%; height: 100%; display: flex; justify-content: center; align-items: center; overflow: hidden"
                input {
                    type = InputType.text; name = "timepicker"; classes += "time_element"; style = "display: none;"
                }
            }
        }
        val timeElement = rootElement.querySelector("input.time_element")
        keep(timeElement)
        js("$(timeElement).timepicki({ show_meridian: false, disable_keyboard_mobile: true });")
    }

    actual fun show(hours: Int, minutes: Int, change: (time: String) -> Unit) {
        val timePickerDiv: HTMLElement = rootElement.querySelector("#timePickerDiv") as HTMLElement
        val timepickerButton: HTMLElement = rootElement.querySelector("#timepicker_wrap_button") as HTMLElement
        val timeElement: HTMLElement = rootElement.querySelector("input.time_element") as HTMLElement

        timepickerButton.onclick = {
            @Suppress("NAME_SHADOWING")
            val timeElement: HTMLElement = timeElement
            val hoursResult: String = timeElement.attributes["data-timepicki-tim"]?.value ?: "00"
            val minutesResult: String = timeElement.attributes["data-timepicki-mini"]?.value ?: "00"
            js("timeElement.close_timepicki()")
            timePickerDiv.classList.add("input-disable")
            change("$hoursResult:$minutesResult")
        }
        timeElement.setAttribute("data-timepicki-tim", hours.toString())
        timeElement.setAttribute("data-timepicki-mini", minutes.toString())
        js("timeElement.open_timepicki()")
        timePickerDiv.classList.remove("input-disable")
    }

}
