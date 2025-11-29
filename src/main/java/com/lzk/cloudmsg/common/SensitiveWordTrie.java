package com.lzk.cloudmsg.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveWordTrie {

    private final TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        // Initialize with some dummy sensitive words
        addWord("bad");
        addWord("evil");
        addWord("gambling");
    }

    public void addWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEnd = true;
    }

    public boolean contains(String text) {
        if (text == null) return false;
        for (int i = 0; i < text.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < text.length(); j++) {
                node = node.children.get(text.charAt(j));
                if (node == null) {
                    break;
                }
                if (node.isEnd) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }
}
