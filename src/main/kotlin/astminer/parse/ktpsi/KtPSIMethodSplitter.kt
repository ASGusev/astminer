package astminer.parse.ktpsi

import astminer.common.model.*
import astminer.common.preOrder


class KtPSIMethodSplitter : TreeMethodSplitter<KtPSINode> {
    override fun splitIntoMethods(root: KtPSINode): Collection<MethodInfo<KtPSINode>> {
        val methodRoots = root.preOrder().filter { it.getTypeLabel() == TypeLabels.METHOD_DECLARATION }
        return methodRoots.map { collectMethodInfo(it as KtPSINode) }
    }

    private fun collectMethodInfo(methodRoot: KtPSINode): MethodInfo<KtPSINode> {
        val methodNode = makeMethodNode(methodRoot)
        val elementNode = makeElementNode(methodRoot)
        val methodParameters = extractParametersList(methodRoot)
        return MethodInfo(methodNode, elementNode, methodParameters)
    }

    private fun makeMethodNode(methodRoot: KtPSINode): MethodNode<KtPSINode> {
        val methodNameNode = methodRoot.getChildOfType(TypeLabels.METHOD_IDENTIFIER) as KtPSINode
        val methodType = methodRoot.getChildOfType(TypeLabels.METHOD_TYPE) as KtPSINode?
        return MethodNode(methodRoot, methodType, methodNameNode)
    }

    private fun makeElementNode(methodRoot: KtPSINode): ElementNode<KtPSINode> {
        var classNode = methodRoot.getParent()
        while (classNode !== null && classNode.getTypeLabel() != TypeLabels.CLASS_DECLARATION)
            classNode = classNode.getParent()
        val classNameNode = classNode?.getChildOfType(TypeLabels.CLASS_IDENTIFIER)
        return ElementNode(classNode, classNameNode as KtPSINode?)
    }

    private fun extractParametersList(methodRoot: KtPSINode): List<ParameterNode<KtPSINode>> {
        val parameterListNode = methodRoot.getChildOfType(TypeLabels.PARAMETERS_LIST)
        val parameterNodes = parameterListNode?.getChildrenOfType(TypeLabels.PARAMETER) ?: listOf()
        return parameterNodes.map {
            val paraeterNameNode = it.getChildOfType(TypeLabels.PARAMETER_IDENTIFIER) as KtPSINode?
            val parameterTypeNode = it.getChildOfType(TypeLabels.PARAMETER_TYPE) as KtPSINode?
            ParameterNode(it as KtPSINode, parameterTypeNode, paraeterNameNode)
        }
    }
}