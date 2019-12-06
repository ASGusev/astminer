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
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import java.io.File

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

    private fun <T : Node> extractFromMethods(
        roots: List<ParseResult<T>>,
        methodSplitter: TreeMethodSplitter<T>,
        miner: PathMiner,
        storage: Code2VecFormatPathStorage,
        getFullMethodName: ((ParseResult<T>, MethodInfo<T>) -> String)? = null
    ) {
        for (parsedFile in roots) {
            if (parsedFile.root != null) {
                val methods = methodSplitter.splitIntoMethods(parsedFile.root!!)
                for (method in methods) {
                    val methodNameNode = method.method.nameNode ?: break
                    val methodRoot = method.method.root
                    val label = splitToSubtokens(methodNameNode.getToken()).joinToString("|")
                    methodRoot.preOrder().forEach { it.setNormalizedToken() }
                    methodNameNode.setNormalizedToken("METHOD_NAME")


                    // Retrieve paths from every node individually
                    val paths = miner.retrievePaths(methodRoot).take(maxPathContexts)
                    val labeledPathContexts = LabeledPathContexts(label, paths.map {
                        toPathContext(it) { node ->
                            node.getNormalizedToken()
                        }
                    })
                    if (getFullMethodName != null) {
                        val fullMethodName = getFullMethodName(parsedFile, method)
                        storage.store(labeledPathContexts, fullMethodName)
                    } else {
                        storage.store(labeledPathContexts)
                    }
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
            val storage = Code2VecFormatPathStorage(outputDirForLanguage.path)

            when (extension) {
                "c", "cpp" -> {
                    val parser = FuzzyCppParser()
                    val roots = parser.parseWithExtension(File(projectRoot), extension)
                    extractFromMethods(roots, FuzzyMethodSplitter(), miner, storage)
                }
                "java" -> {
                    val parser = GumTreeJavaParser()
                    val roots = parser.parseWithExtension(File(projectRoot), extension)
                    extractFromMethods(roots, GumTreeMethodSplitter(), miner, storage)
                }
                "py" -> {
                    val parser = PythonParser()
                    val roots = parser.parseWithExtension(File(projectRoot), extension)
                    extractFromMethods(roots, PythonMethodSplitter(), miner, storage)
                }
                "kt" -> {
                    val loader = KtPSILoader()
                    val roots = loader.parseWithExtension(File(projectRoot), "txt")
                    val fullMethodNameRestorer = KtFullMethodPathExtractor(projectRoot)
                    extractFromMethods(roots, KtPSIMethodSplitter(), miner, storage,
                            fullMethodNameRestorer::getFullMethodPath)
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