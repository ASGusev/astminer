package cli

import astminer.common.model.ASTPath
import astminer.common.model.Node
import java.io.OutputStreamWriter
import java.nio.file.Paths
import kotlin.math.max


data class PathStatistic(val depth: Int, val width: Int)


private const val META_LEAF_INDEX = "leaf_index"


private fun Node.getLeafIndex(): Int = this.getMetadata(META_LEAF_INDEX) as Int


class StatisticsCollector(outDir: String, private val batchSize: Long) {
    private val outPath = Paths.get(outDir)
    private val statisticsList = mutableListOf<List<PathStatistic>>()
    private var batchesWritten = 0

    fun addMethodPaths(astPaths: Collection<ASTPath>) {
        statisticsList.add(astPaths.map { path ->
            val pathHeight =  max(path.upwardNodes.size, path.downwardNodes.size)
            val pathWidth = path.downwardNodes.last().getLeafIndex() - path.upwardNodes[0].getLeafIndex()
            PathStatistic(pathHeight, pathWidth)
        })
        if (statisticsList.size.toLong() == batchSize) {
            save()
        }
    }

    fun save() {
        val outFileName = if (batchSize > 0) {
            "path_context_statistics_${batchesWritten++}.txt"
        } else {
            "path_context_statistics.txt"
        }

        val outFile = outPath.resolve(outFileName).toFile()
        outFile.parentFile.mkdirs()
        val outFileWriter = OutputStreamWriter(outFile.outputStream())
        outFileWriter.use {
            for (methodStatistics in statisticsList) {
                outFileWriter.write(methodStatistics.joinToString(" ", "", "\n") { "${it.depth},${it.width}" })
            }
        }

        statisticsList.clear()
    }
}