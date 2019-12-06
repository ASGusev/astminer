package astminer.parse.ktpsi

import astminer.common.model.MethodInfo
import astminer.common.model.ParseResult
import java.nio.file.Paths


class KtFullMethodPathExtractor(projectDir: String) {
    private val projectPath = Paths.get(projectDir)

    fun getFullMethodPath(parseResult: ParseResult<KtPSINode>, method: MethodInfo<KtPSINode>): String {
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
        val sourcePath = Paths.get(parseResult.filePath)
        val relativeSourcePath = projectPath.relativize(sourcePath)
        return relativeSourcePath.toString() + " " + classNames.joinToString(".") + " " + method.method.name()
    }
}