package cli.path_saving

import astminer.common.model.MethodInfo
import astminer.common.model.Node
import astminer.common.model.ParseResult

interface MethodPathSaver<T: Node> {
    fun addPath(parseResult: ParseResult<T>, method: MethodInfo<T>)

    fun save()
}