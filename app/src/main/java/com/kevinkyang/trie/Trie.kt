package com.kevinkyang.trie

import java.util.*


class TrieNode(var value: Char?, var endOfWord: Boolean = false) {

    private val children: ArrayList<TrieNode> = ArrayList()

    fun getChild(char: Char): TrieNode? {
        for (node in children) {
            if (node.value == char) {
                return node
            }
        }

        return null
    }

    fun addChild(value: Char, endOfWord: Boolean = false): TrieNode {
        children.add(TrieNode(value, endOfWord))
        return children.last()
    }

    fun getChildrenIterator(): Iterator<TrieNode> = children.iterator()

    fun hasChildren(): Boolean = children.isEmpty()
}

class Trie {

    val root: TrieNode = TrieNode(null)

    //TODO store the last searched term and node here; that way, if the user just searches for the same term with a new letter, you don't have to restart the search; clear this when you modify the trie as things may have changed;
    //TODO should we modify TrieNode to have a reference to its parent, so you can do this for searches where the user gives the same word with a letter deleted? it seems that you can store a reference to the parent without storing a whole new object
    private var lastSearchTerm: String? = null
    private var lastSearchNode: TrieNode? = null

    // TODO is there more efficient way to search a Trie's children?

    fun buildTrie(wordList: ArrayList<String>) {
        for (word in wordList) {
            addWord(word)
        }
    }

    fun addWord(word: String) {
        var curNode = root
        for ((index, char) in word.withIndex()) {
            val child = curNode.getChild(char)
            if (child == null) { // child does not exist yet
                var endOfWord = false
                if (index == word.lastIndex) {
                    endOfWord = true
                }

                curNode = curNode.addChild(char, endOfWord)
            } else {
                if (index == word.lastIndex) {
                    child.endOfWord = true
                }

                curNode = child
            }
        }
    }

    fun search(prefix: String): ArrayList<String> {
        val results = ArrayList<String>()

        var curNode = root
        for (char in prefix) {
            val child = curNode.getChild(char)
            if (child == null) {
                return results
            } else {
                curNode = child
            }
        }

        lastSearchNode = curNode
        lastSearchTerm = prefix

        if (curNode.endOfWord) {
            results.add(prefix)
        }

        curNode.getChildrenIterator().forEach {
            node -> traverse(node, StringBuilder(prefix), results)
        }

        return results
    }

    //TODO better name, test, document, decide on rec or iter
    private fun traverse(root: TrieNode, prefix: StringBuilder, results: ArrayList<String>) {
        if (root.value != null) {
            val c = root.value
            prefix.append(c)
        }

        if (root.endOfWord) {
            results.add(prefix.toString())
        } else {
            root.getChildrenIterator().forEach {
                node -> traverse(node, prefix, results)
            }
        }

        prefix.setLength(prefix.length - 1)
    }

    private fun traverseIterative(root: TrieNode, prefix: StringBuilder, results: ArrayList<String>) {
        val stack = Stack<TrieNode>()

        stack.push(root)
        while (!stack.empty()) {
            val node = stack.pop()
            if (node.value != null) {
                val c = root.value
                prefix.append(c)
            }

            if (root.endOfWord) {
                results.add(prefix.toString())
            } else {
                node.getChildrenIterator().forEach {
                    node -> stack.push(node)
                }
            }

            prefix.setLength(prefix.length - 1)
        }
    }
}