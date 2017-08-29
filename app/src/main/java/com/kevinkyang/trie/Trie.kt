package com.kevinkyang.trie

import java.util.Stack


class TrieNode(var value: Char?,
               val parent: TrieNode? = null,
               var endOfWord: Boolean = false) {

    private val children: ArrayList<TrieNode> = ArrayList()

    fun getChild(char: Char): TrieNode? {
        return children.find {
            node -> node.value == char
        }
    }

    //TODO check for duplicates
    fun addChild(value: Char, endOfWord: Boolean = false): TrieNode {
        children.add(TrieNode(value, this, endOfWord))
        return children.last()
    }

    fun getChildrenIterator(): Iterator<TrieNode> = children.iterator()
}

class Trie(private val caseSensitive: Boolean = false) {

    private val root: TrieNode = TrieNode(null)

    //TODO clear last term/node when you modify the trie as things may have changed
    private var lastSearchTerm: String? = null
    private var lastSearchNode: TrieNode? = null

    // TODO is there more efficient way to search a Trie's children?

    fun buildTrie(wordList: ArrayList<String>) {
        for (word in wordList) {
            addWord(word)
        }
    }

    fun buildTrie(wordList: Array<String>) {
        for (word in wordList) {
            addWord(word)
        }
    }

    fun addWord(word: String) {
        var _word = if (caseSensitive) word else word.toLowerCase()

        var curNode = root
        for ((index, char) in _word.withIndex()) {
            val child = curNode.getChild(char)
            if (child == null) { // child does not exist yet
                var endOfWord = false
                if (index == _word.lastIndex) {
                    endOfWord = true
                }

                curNode = curNode.addChild(char, endOfWord)
            } else {
                if (index == _word.lastIndex) {
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
            curNode = curNode.getChild(char) ?: return results
        }
        //TODO actually use these
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

    private fun doesAddToLastSearch(newSearchTerm: String): Boolean {
        val old = lastSearchTerm
        return old != null && newSearchTerm.startsWith(old, !caseSensitive)
    }

    private fun doesSubtractFromLastSearch(newSearchTerm: String): Boolean {
        val old = lastSearchTerm ?: return false

        val diff = old.length - newSearchTerm.length
        return old != null && diff == 1 &&  old.startsWith(newSearchTerm, !caseSensitive)
    }

    //TODO better name, test, document
    private fun traverse(root: TrieNode, prefix: StringBuilder, results: ArrayList<String>) {
        if (root.value != null) {
            val c = root.value
            prefix.append(c)
        }

        if (root.endOfWord) {
            results.add(prefix.toString())
        }

        root.getChildrenIterator().forEach {
            node -> traverse(node, prefix, results)
        }

        prefix.setLength(prefix.length - 1)
    }
}