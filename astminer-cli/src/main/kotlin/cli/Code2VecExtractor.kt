package cli

import astminer.common.getNormalizedToken
import astminer.common.model.*
import astminer.common.preOrder
import astminer.common.setNormalizedToken
import astminer.common.splitToSubtokens
import astminer.parse.antlr.python.PythonMethodSplitter
import astminer.parse.antlr.python.PythonParser
import astminer.parse.cpp.FuzzyCppParser
import astminer.parse.cpp.FuzzyMethodSplitter
import astminer.parse.java.GumTreeJavaParser
import astminer.parse.java.GumTreeMethodSplitter
import astminer.parse.ktpsi.*
import astminer.paths.*
import cli.path_saving.KtSourceMethodPathSaver
import cli.path_saving.MethodPathSaver
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import java.io.File


private const val BATCH_SIZE = 16384L


class Code2VecExtractor : CliktCommand() {

    val extensions: List<String> by option(
        "--lang",
        help = "File extensions that will be parsed"
    ).split(",").required()

    val projectRoot: String by option(
        "--project",
        help = "Path to the project that will be parsed"
    ).required()

    val outputDirName: String by option(
        "--output",
        help = "Path to directory where the output will be stored"
    ).required()

    val maxPathHeight: Int by option(
        "--maxH",
        help = "Maximum height of path for code2vec"
    ).int().default(8)

    val maxPathWidth: Int by option(
        "--maxW",
        help = "Maximum width of path. " +
                "Note, that here width is the difference between token indices in contrast to the original code2vec."
    ).int().default(3)

    val maxPathContexts: Int by option(
        "--maxContexts",
        help = "Number of path contexts to keep from each method."
    ).int().default(500)

    val maxTokens: Long by option(
        "--maxTokens",
        help = "Keep only contexts with maxTokens most popular tokens."
    ).long().default(Long.MAX_VALUE)

    val maxPaths: Long by option(
        "--maxPaths",
        help = "Keep only contexts with maxTokens most popular paths."
    ).long().default(Long.MAX_VALUE)

    val loadableFormat: Boolean by option(
            "--loadableFormat",
            help= "Produce output in the format readable by code2vec"
    ).flag()

    val writeStatistics: Boolean by option(
            "--statistics",
            help="Write paths statistics (height, width)"
    ).flag()

    private fun <T : Node> extractFromMethods(
        roots: Sequence<ParseResult<T>>,
        methodSplitter: TreeMethodSplitter<T>,
        miner: PathMiner,
        storage: PathStorage<String>,
        fullPathSaver: MethodPathSaver<T>? = null,
        statisticsSaver: StatisticsCollector? = null
    ) {
        roots.forEachIndexed { index, parsedFile ->
            println("Processing file number $index: ${parsedFile.filePath}")
            if (parsedFile.root != null) {
                val methods = methodSplitter.splitIntoMethods(parsedFile.root!!)
                for (method in methods) {
                    val methodNameNode = method.method.nameNode ?: break
                    val methodRoot = method.method.root
                    val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
                    methodRoot.preOrder().forEach { it.setNormalizedToken() }
                    methodNameNode.setNormalizedToken("METHOD_NAME")

                    // Retrieve paths from every node individually
                    val allRetrievedPaths = miner.retrievePaths(methodRoot)
                    val selectedPaths = allRetrievedPaths.take(maxPathContexts)
                    val labeledPathContexts = LabeledPathContexts(label, selectedPaths.map {
                        toPathContext(it) { node ->
                            node.getNormalizedToken()
                        }
                    })

                    fullPathSaver?.addPath(parsedFile, method)
                    statisticsSaver?.addMethodPaths(allRetrievedPaths)
                    storage.store(labeledPathContexts)
                }
            }
        }

    }

    private fun extract() {
        val outputDir = File(outputDirName)
        for (extension in extensions) {
            val miner = PathMiner(PathRetrievalSettings(maxPathHeight, maxPathWidth))

            val outputDirForLanguage = outputDir.resolve(extension)
            outputDirForLanguage.mkdir()
            val storage = if (loadableFormat) {
                Code2VecFormatPathStorage(outputDirForLanguage.path, true, BATCH_SIZE)
            } else {
                Code2VecPathStorage(outputDirForLanguage.path)
            }

            when (extension) {
                "c", "cpp" -> {
                    val parser = FuzzyCppParser()
                    val roots = parser.parseWithExtensionLazy(File(projectRoot), extension)
                    extractFromMethods(roots, FuzzyMethodSplitter(), miner, storage)
                }
                "java" -> {
                    val parser = GumTreeJavaParser()
                    val roots = parser.parseWithExtensionLazy(File(projectRoot), extension)
                    extractFromMethods(roots, GumTreeMethodSplitter(), miner, storage)
                }
                "py" -> {
                    val parser = PythonParser()
                    val roots = parser.parseWithExtensionLazy(File(projectRoot), extension)
                    extractFromMethods(roots, PythonMethodSplitter(), miner, storage)
                }
                "kt" -> {
                    val loader = KtPSILoader()
                    val roots = loader.parseWithExtensionLazy(File(projectRoot), "txt")
                    val fullPathSaver = KtSourceMethodPathSaver(outputDirForLanguage.path, projectRoot, BATCH_SIZE)
                    val statisticsCollector = if (writeStatistics) StatisticsCollector(outputDirForLanguage.path, BATCH_SIZE) else null
                    extractFromMethods(roots, KtPSIMethodSplitter(), miner, storage, fullPathSaver, statisticsCollector)
                    fullPathSaver.save()
                    statisticsCollector?.save()
                }
                else -> throw UnsupportedOperationException("Unsupported extension $extension")
            }

            // Save stored data on disk
            // TODO: implement batches for path context extraction
            storage.save(maxPaths, maxTokens)
        }
    }

    override fun run() {
        extract()
    }
}