package astminer.paths

import astminer.common.storage.*
import java.io.File

class Code2VecFormatPathStorage(outputFolderPath: String,
                                batchMode: Boolean = true,
                                fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH) :
        CountingPathStorage<String>(outputFolderPath, batchMode, fragmentsPerBatch) {
    override fun dumpPathContexts(file: File, tokensLimit: Long, pathsLimit: Long) {
        val idToToken = tokensMap.itemPerId()
        val idToPath = pathsMap.itemPerId()
        val idToOrientedNodeType = orientedNodeTypesMap.itemPerId()

        val lines = mutableListOf<String>()
        for (labeledPathContextIds in labeledPathContextIdsList) {
            val pathContextIdsString = labeledPathContextIds.pathContexts.filter {
                tokensMap.getIdRank(it.startTokenId) <= tokensLimit &&
                        tokensMap.getIdRank(it.endTokenId) <= tokensLimit &&
                        pathsMap.getIdRank(it.pathId) <= pathsLimit
            }.joinToString(separator = " ") { pathContextId ->
                val firstToken = idToToken[pathContextId.startTokenId]
                val secondToken = idToToken[pathContextId.endTokenId]
                val path = idToPath[pathContextId.pathId]!!
                        .mapNotNull { idToOrientedNodeType[it] }
                        .joinToString(",") { it.typeLabel + "_" + it.direction }
                val pathHash = path.hashCode()
                "$firstToken,${pathHash},$secondToken"
            }
            lines.add("${labeledPathContextIds.label} $pathContextIdsString")
        }

        writeLinesToFile(lines, file)
    }
}
