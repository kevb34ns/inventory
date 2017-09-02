package com.kevinkyang.inventory;


import com.kevinkyang.trie.Trie;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TrieTest {

	static final String[] wordList = {
			"ad", "add", "addition", "ads", "advertisement", "soda", "soso", "mumble", "bumble", "bumblebee", "africa", "asia"
	};

	@Test
	public void addRetrieveWords() throws Exception {
		Trie trie = new Trie();
		trie.buildTrie(wordList);

		ArrayList<String> results = trie.search("ad");
		System.out.println("RESULTS:");
		for (String s : results) {
			System.out.println(s);
		}
		assertEquals(results.size(), 5);
	}

	@Test
	public void testLastSearchCaching() throws Exception {
		Trie trie = new Trie();
		trie.buildTrie(wordList);
		trie.search("ad");

		System.out.println("ADDING...");
		ArrayList<String> results = trie.search("add");
		System.out.println("RESULTS:");
		for (String s : results) {
			System.out.println(s);
		}

		System.out.println("SUBTRACTING...");
		results = trie.search("ad");
		System.out.println("RESULTS:");
		for (String s : results) {
			System.out.println(s);
		}
	}
}
