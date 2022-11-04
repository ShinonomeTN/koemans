package com.shinonometn.koemans.utils

/** Represent a simple tree node with multiple children */
interface TreeNode<K, C : TreeNode<K, C>> {
    val hasChild: Boolean

    operator fun get(key: K): C?

    val children: Collection<C>
}

interface MutableTreeNode<K, C : TreeNode<K, C>> : TreeNode<K, C> {
    fun addChild(key: K, child: C)
}