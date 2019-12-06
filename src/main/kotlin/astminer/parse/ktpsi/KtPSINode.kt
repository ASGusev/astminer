package astminer.parse.ktpsi

import astminer.common.model.Node

class KtPSINode(private val parent: KtPSINode?, private val typeLabel: String, private val token: String) : Node {
    private val childrenList = mutableListOf<KtPSINode>()
    private val metadata = mutableMapOf<String, Any>()

    override fun getTypeLabel() = typeLabel

    override fun getChildren() = childrenList

    override fun getParent() = parent

    override fun getToken() = token

    override fun isLeaf() = childrenList.isEmpty()

    override fun getMetadata(key: String): Any? = metadata[key]

    override fun setMetadata(key: String, value: Any) {
        metadata[key] = value
    }

    internal fun addChild(child: KtPSINode) = childrenList.add(child)
}