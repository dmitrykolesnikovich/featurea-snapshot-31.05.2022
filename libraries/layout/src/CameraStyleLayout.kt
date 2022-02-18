package featurea.layout

import featurea.math.Size

typealias CameraStyleLayout = (Camera, Size) -> Unit

enum class Horizontal(val layout: CameraStyleLayout) {
    fillTop({ camera, size ->
        TODO()
    }),
    fillCenter({ camera, size ->
        TODO()
    }),
    fillBottom({ camera, size ->
        TODO()
    }),
    anchorLeft({ camera, size ->
        TODO()
    }),
    anchorRight({ camera, size ->
        TODO()
    }),
    fill({ camera, size ->
        TODO()
    })
}

enum class Vertical(val layout: CameraStyleLayout) {
    fillLeft({ camera, size ->
        TODO()
    }),
    fillCenter({ camera, size ->
        TODO()
    }),
    fillRight({ camera, size ->
        TODO()
    }),
    anchorTop({ camera, size ->
        TODO()
    }),
    anchorBottom({ camera, size ->
        TODO()
    }),
    fill({ camera, size ->
        TODO()
    })
}