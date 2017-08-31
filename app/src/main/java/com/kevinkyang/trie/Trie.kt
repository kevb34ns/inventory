package com.kevinkyang.trie


class TrieNode(var value: Char?,
               val parent: TrieNode? = null,
               var endOfWord: Boolean = false) {

    private val children: ArrayList<TrieNode> = ArrayList()

    fun getChild(char: Char): TrieNode? {
        return children.find {
            node -> node.value == char
        }
    }

    fun addChild(value: Char, endOfWord: Boolean = false): TrieNode {
        val child = getChild(value)
        if (child != null) {
            return child
        }

        children.add(TrieNode(value, this, endOfWord))
        return children.last()
    }

    fun removeChild(value: Char) {
        children.forEachIndexed { index, node ->
            if (node.value == value) {
                children.removeAt(index)
                return
            }
        }
    }

    fun hasChildren(): Boolean = !children.isEmpty()

    fun getChildrenIterator(): Iterator<TrieNode> = children.iterator()
}

class Trie(private val caseSensitive: Boolean = false) {

    private val mRoot: TrieNode = TrieNode(null)

    private var mLastSearchNode: TrieNode? = null
    private var mLastSearchTerm: String? = null

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
        val _word = if (caseSensitive) word else word.toLowerCase()
        clearLastSearch()

        var curNode = mRoot
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

    fun removeWord(word: String) {
        var curNode = getEndOfWord(word) ?: return
        curNode.endOfWord = false
        while (!curNode.hasChildren()) {
            val c = curNode.value ?: return
            curNode = curNode.parent ?: return
            curNode.removeChild(c)
        }
    }

    fun search(prefix: String): ArrayList<String> {
        val results = ArrayList<String>()

        val curNode = getSearchNode(prefix) ?: return results

        mLastSearchNode = curNode
        mLastSearchTerm = prefix

        if (curNode.endOfWord) {
            results.add(prefix)
        }

        curNode.getChildrenIterator().forEach {
            node -> traverse(node, StringBuilder(prefix), results)
        }

        return results
    }

    private fun getSearchNode(prefix: String): TrieNode? {
        val lastSearchNode = mLastSearchNode
        val lastSearchTerm = mLastSearchTerm
        if (lastSearchNode != null && lastSearchTerm != null) {
            if (doesAddToLastSearch(prefix)) {
                return lastSearchNode.getChild(prefix[lastSearchTerm.length])
            } else if(doesSubtractFromLastSearch(prefix)) {
                return lastSearchNode.parent
            }
        }

        return getEndOfWord(prefix)
    }

    private fun getEndOfWord(word: String): TrieNode? {
        var node = mRoot

        for (char in word) {
            node = node.getChild(char) ?: return null
        }

        return node
    }

    private fun doesAddToLastSearch(newSearchTerm: String): Boolean {
        val old = mLastSearchTerm ?: return false
        return newSearchTerm.startsWith(old, !caseSensitive)
    }

    private fun doesSubtractFromLastSearch(newSearchTerm: String): Boolean {
        val old = mLastSearchTerm ?: return false

        val diff = old.length - newSearchTerm.length
        return diff == 1 &&  old.startsWith(newSearchTerm, !caseSensitive)
    }

    private fun clearLastSearch() {
        mLastSearchNode = null
        mLastSearchTerm = null
    }

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