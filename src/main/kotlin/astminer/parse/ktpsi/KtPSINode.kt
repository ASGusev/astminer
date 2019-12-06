package astminer.parse.ktpsi

import astminer.common.model.Node

class KtPSINode(
        private val parent: KtPSINode?,
        private val typeLabel: String,
        private val token: String,
        private val leaf: Boolean) : Node {
    private val childrenList = ArrayList<KtPSINode>()
    private val metadata = HashMap<String, Any>()

    override fun getTypeLabel() = typeLabel

    override fun getChildren() = childrenList

    override fun getParent() = parent

    override fun getToken() = token

    override fun isLeaf() = leaf

    override fun getMetadata(key: String): Any? = metadata[key]

    override fun setMetadata(key: String, value: Any) {
        metadata[key] = value
    }

    internal fun addChild(child: KtPSINode) = childrenList.add(child)
}