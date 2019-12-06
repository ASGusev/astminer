package astminer.parse.ktpsi

import astminer.common.model.Parser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


private const val TYPE_START_ELEMENT = 11
private const val TYPE_START_COMMENT = 11
private const val TYPE_START_WHITESPACE = 14


class KtPSILoader : Parser<KtPSINode> {
    override fun parse(content: InputStream): KtPSINode? {
        val root = KtPSINode(null, "", "KtFile")
        val parents = mutableListOf(root)
        val indents = mutableListOf(0)
        val reader = BufferedReader(InputStreamReader(content))
        reader.readLine()
        reader.lines()
                .forEach {
                    val (indent, nodeDesc) = extractIndent(it)
                    while (indents.last() >= indent) {
                        indents.removeAt(indents.lastIndex)
                        parents.removeAt(parents.lastIndex)
                    }
                    val parent = parents.last()
                    val curNode = makeNode(parent, nodeDesc)
                    parent.addChild(curNode)
                    parents.add(curNode)
                    indents.add(indent)
                }
        return root
    }

    private fun makeNode(parent: KtPSINode?, desc: String): KtPSINode = when {
            desc.startsWith("PsiElement") -> {
                val typeEnd = desc.indexOf(')')
                val type = desc.substring(TYPE_START_ELEMENT, typeEnd)
                val tokenStart = typeEnd + 2
                val tokenEnd = desc.indexOf(')', tokenStart)
                val token = desc.substring(tokenStart, tokenEnd)
                KtPSINode(parent, type, token)
            }
            desc.startsWith("PsiWhiteSpace") -> {
                val tokenEnd = desc.indexOf(')')
                val token = desc.substring(TYPE_START_WHITESPACE, tokenEnd)
                KtPSINode(parent, TypeLabels.WHITE_SPACE, token)
            }
            desc.startsWith("PsiComment") -> {
                val typeEnd = desc.indexOf(')')
                val type = desc.substring(TYPE_START_COMMENT, typeEnd)
                val tokenStart = typeEnd + 2
                val tokenEnd = desc.indexOf(')', tokenStart)
                val token = desc.substring(tokenStart, tokenEnd)
                KtPSINode(parent, type, token)
            }
            desc.toUpperCase() == desc -> KtPSINode(parent, desc, "")
            desc == TypeLabels.K_DOC -> KtPSINode(parent, TypeLabels.K_DOC, "")
            desc == TypeLabels.EMPTY_LIST -> KtPSINode(parent, TypeLabels.EMPTY_LIST, "")
            desc.startsWith(TypeLabels.PSI_ERROR_ELEMENT) -> KtPSINode(parent, TypeLabels.PSI_ERROR_ELEMENT, "")
            else -> {
                throw UnknownTokenException("Unknown node: $desc")
            }
        }

    private fun extractIndent(line: String): Pair<Int, String> {
        var indent = 0
        while (line[indent] == ' ')
            ++indent
        return Pair(indent, line.substring(indent))
    }
}

class UnknownTokenException(message: String): Exception(message)
