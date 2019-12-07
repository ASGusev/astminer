package cli.path_saving

import astminer.common.model.MethodInfo
import astminer.common.model.ParseResult
import astminer.parse.ktpsi.KtPSINode
import astminer.parse.ktpsi.getFullMethodName
import java.io.OutputStreamWriter
import java.nio.file.Paths

class KtSourceMethodPathSaver(outDir: String, projectDir: String, private val batchSize: Long):
        MethodPathSaver<KtPSINode> {
    private val projectPath = Paths.get(projectDir)
    private val outPath = Paths.get(outDir)
    private val pathsList = mutableListOf<String>()
    private var batchesWritten = 0

    override fun addPath(parseResult: ParseResult<KtPSINode>, method: MethodInfo<KtPSINode>) {
        val sourcePath = Paths.get(parseResult.filePath)
        val relativeSourcePath = projectPath.relativize(sourcePath)
        val fullName = getFullMethodName(method)
        pathsList.add("$relativeSourcePath,$fullName")
        if (pathsList.size.toLong() == batchSize) {
            save()
        }
    }

    override fun save() {
        val outFileName = if (batchSize > 0) {
            "full_method_paths_${batchesWritten++}.csv"
        } else {
            "full_method_paths.csv"
        }

        val outFile = outPath.resolve(outFileName).toFile()
        outFile.parentFile.mkdirs()
        val outFileWriter = OutputStreamWriter(outFile.outputStream())
        outFileWriter.use {
            for (path in pathsList) {
                outFileWriter.write(path + "\n")
            }
        }

        pathsList.clear()
    }
}