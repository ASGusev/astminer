package astminer.paths

import astminer.common.model.LabeledPathContexts
import astminer.common.model.PathStorage
import astminer.common.storage.writeLinesToFile
import java.io.File


private data class HashedContext(val startToken: String, val pathHash: Int, val endToken: String)

private data class HashedPathContexts(val label: String, val contexts: List<HashedContext>)


class Code2VecSimplePathStorage(private val outputFolderPath: String,
                                private val batchMode: Boolean = true,
                                private val fragmentsPerBatch: Long = DEFAULT_FRAGMENTS_PER_BATCH):
        PathStorage<String> {
    private val hashedMethodsPathContexts = ArrayList<HashedPathContexts>()
    private var batchesWritten = 0

    override fun store(labeledPathContexts: LabeledPathContexts<String>) {
        val hashedContexts = labeledPathContexts.pathContexts.map { pathContext ->
            val path = pathContext.orientedNodeTypes.joinToString(",") { it.typeLabel + "_" + it.direction }
            val pathHash = path.hashCode()
            HashedContext(pathContext.startToken, pathHash, pathContext.endToken)
        }
        hashedMethodsPathContexts.add(HashedPathContexts(labeledPathContexts.label, hashedContexts))
        if (batchMode && hashedMethodsPathContexts.size.toLong() == fragmentsPerBatch) {
            save()
        }
    }

    override fun save() {
        val outFile = if (batchMode) {
            File("$outputFolderPath/path_contexts_${batchesWritten++}.csv")
        } else {
            File("$outputFolderPath/path_contexts.csv")
        }
        val lines = hashedMethodsPathContexts.map { hashedPathContext ->
            val pathContextsString = hashedPathContext.contexts.joinToString(" ")
                    { "${it.startToken},${it.pathHash},${it.endToken}" }
            hashedPathContext.label + " " + pathContextsString
        }
        writeLinesToFile(lines, outFile)
        hashedMethodsPathContexts.clear()
    }

    override fun save(pathsLimit: Long, tokensLimit: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}