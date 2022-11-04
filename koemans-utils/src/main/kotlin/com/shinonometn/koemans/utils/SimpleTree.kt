package com.shinonometn.koemans.utils

open class SimpleTree<K, C> : MutableTreeNode<K, SimpleTree<K, C>> {
    protected open var didChildInitialized = false

    protected open val subTrees by lazy {
        didChildInitialized = true
        mutableMapOf<K, SimpleTree<K, C>>()
    }

    override val hasChild: Boolean
        get() = didChildInitialized

    override val children: Collection<SimpleTree<K, C>>
        get() = if (!hasChild) emptyList() else subTrees.values

    override fun get(key: K): SimpleTree<K, C>? {
        return if (!didChildInitialized) null else subTrees[key]
    }

    override fun addChild(key: K, child: SimpleTree<K, C>) {
        subTrees[key] = child
    }
}