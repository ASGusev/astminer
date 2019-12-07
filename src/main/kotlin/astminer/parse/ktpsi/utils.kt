package astminer.parse.ktpsi

import astminer.common.model.MethodInfo


fun getFullMethodName(method: MethodInfo<KtPSINode>): String {
    var pos = method.enclosingElement.root
    val classNames = mutableListOf<String>()

    while (pos != null) {
        if (pos.getTypeLabel() == TypeLabels.CLASS_DECLARATION) {
            val className = pos.getChildOfType(TypeLabels.IDENTIFIER)?.getToken()
            if (className != null)
                classNames.add(className)
        }
        pos = pos.getParent()
    }
    return if (classNames.isNotEmpty()) {
        classNames.joinToString(".", postfix = ".") + method.method.name()
    } else {
        method.method.name()!!
    }
}
