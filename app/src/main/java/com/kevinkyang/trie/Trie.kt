package com.kevinkyang.trie


class TrieNode(var value: Char?, var endOfWord: Boolean = false) {

    private val children: ArrayList<TrieNode> = ArrayList()

    fun getChild(char: Char): TrieNode? {
        for (node in children) {
            if (node.value == char) {
                return node;
            }
        }

        return null
    }

    fun addChild(value: Char, endOfWord: Boolean = false): TrieNode {
        children.add(TrieNode(value, endOfWord))
        return children.last()
    }

    fun getListOfChildren(): ArrayList<TrieNode> {
        return ArrayList(children)
    }
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
        //TODO for each character in the prefix, go to the next trienode, immediately returning if a child is not found. if you get through the whole prefix, store it in lastSearchTerm/Node. Check if the prefix itself is a word, then traverse to the end of each subtrie and add those to the list
        var curNode = root;
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

        val children = curNode.getListOfChildren()
        for (child in children) {
            traverse(child, StringBuilder(prefix), results)
        }

        return results
    }

    //TODO better name
    private fun traverse(root: TrieNode, prefix: StringBuilder, results: ArrayList<String>) {

        //TODO depth-first traversal of the subtrie; each recursive call/iteration adds to the prefix. When you get to an endofword, add to results. After making the recursive calls, delete the last character from the stringbuilder; converting to iterative would reduce space complexity


    }
}