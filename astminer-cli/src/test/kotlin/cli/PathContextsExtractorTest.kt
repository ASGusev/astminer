package cli

import cli.util.CliArgs
import cli.util.languagesToString
import cli.util.verifyPathContextExtraction
import org.junit.Test
import java.io.File

internal class PathContextsExtractorTest {
    private val testDataDir = File("src/test/resources")
    private val pathContextsExtractor = PathContextsExtractor()

    @Test
    fun testDefaultExtraction() {
        val extractedDataDir = createTempDir("extractedData")
        val languages = listOf("java", "py")
        val cliArgs = CliArgs.Builder(testDataDir, extractedDataDir)
            .extensions(languagesToString(languages))
            .build()

        pathContextsExtractor.main(cliArgs.args)
        verifyPathContextExtraction(extractedDataDir, languages, true)
    }
}