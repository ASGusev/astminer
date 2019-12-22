package astminer.parse.ktpsi

import astminer.common.model.Parser
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


private const val TYPE_START_ELEMENT = 11


class KtPSILoader : Parser<KtPSINode> {
    override fun parse(content: InputStream): KtPSINode? {
        val root = KtPSINode(null, "", "KtFile", false)
        val parents = mutableListOf(root)
        val indents = mutableListOf(0)
        val reader = BufferedReader(InputStreamReader(content))
        reader.readLine()
        var blockIndent = -1
        reader.lines().forEach {
            val (indent, nodeDesc) = extractIndent(it)
            if (blockIndent != -1 && indent > blockIndent)
                return@forEach
            blockIndent = -1

            while (indents.last() >= indent) {
                indents.removeAt(indents.lastIndex)
                parents.removeAt(parents.lastIndex)
            }
            val parent = parents.last()
            val curNode = if (nodeDesc.isNotEmpty()) makeNode(parent, nodeDesc) else null

            if (curNode != null) {
                parent.addChild(curNode)
                parents.add(curNode)
                indents.add(indent)
            } else {
                blockIndent = indent
            }
        }
        return root
    }

    private fun makeNode(parent: KtPSINode?, desc: String): KtPSINode? = when {
            desc.startsWith("PsiElement") -> {
                val typeEnd = desc.indexOf(')')
                val type = desc.substring(TYPE_START_ELEMENT, typeEnd)
                if (type in TypeLabels.TO_REMOVE || type.startsWith(TypeLabels.K_DOC_PREF)) {
                    null
                } else {
                    val tokenStart = typeEnd + 3
                    val tokenEnd = desc.lastIndexOf(')') - 1
                    val token = desc.substring(tokenStart, tokenEnd)
                    val leaf = type in TypeLabels.LEAF_TYPES
                    KtPSINode(parent, type, token, leaf)
                }
            }
            desc.startsWith("PsiWhiteSpace") -> null
            desc.startsWith("PsiComment") -> null
            desc.toUpperCase() == desc -> KtPSINode(parent, desc, "", false)
            desc == TypeLabels.K_DOC -> KtPSINode(parent, TypeLabels.K_DOC, "", false)
            desc == TypeLabels.EMPTY_LIST -> null
            desc.startsWith(TypeLabels.PSI_ERROR_ELEMENT) -> null
            else -> null
        }

    private fun extractIndent(line: String): Pair<Int, String> {
        var indent = 0
        val n = line.length
        while (indent < n && line[indent] == ' ') {
            ++indent
        }
        return Pair(indent, line.substring(indent))
    }
}

class UnknownTokenException(message: String): Exception(message)
