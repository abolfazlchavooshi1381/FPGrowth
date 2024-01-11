import java.util.*;

class FPTree {
    Map<String, Integer> frequent;
    Map<String, FPNode> headers;
    FPNode root;

    public FPTree(List<List<String>> transactions, int threshold, String rootValue, int rootCount) {
        this.frequent = findFrequentItems(transactions, threshold);
        this.headers = buildHeaderTable(this.frequent);
        this.root = buildFPTree(transactions, rootValue, rootCount, this.frequent, this.headers);
    }

    public Map<String, Integer> findFrequentItems(List<List<String>> transactions, int threshold) {
        Map<String, Integer> items = new HashMap<>();
        for (List<String> transaction : transactions) {
            for (String item : transaction) {
                items.put(item, items.getOrDefault(item, 0) + 1);
            }
        }

        items.entrySet().removeIf(entry -> entry.getValue() < threshold);

        return items;
    }

    public Map<String, FPNode> buildHeaderTable(Map<String, Integer> frequent) {
        Map<String, FPNode> headers = new HashMap<>();
        for (String key : frequent.keySet()) {
            headers.put(key, null);
        }
        return headers;
    }

    public FPNode buildFPTree(List<List<String>> transactions, String rootValue,
                              int rootCount, Map<String, Integer> frequent, Map<String, FPNode> headers) {
        FPNode root = new FPNode(rootValue, rootCount, null);

        for (List<String> transaction : transactions) {
            List<String> sortedItems = new ArrayList<>();
            for (String item : transaction) {
                if (frequent.containsKey(item)) {
                    sortedItems.add(item);
                }
            }
            sortedItems.sort(Comparator.comparingInt(frequent::get).reversed());

            if (!sortedItems.isEmpty()) {
                insertTree(sortedItems, root, headers);
            }
        }

        return root;
    }

    public void insertTree(List<String> items, FPNode node, Map<String, FPNode> headers) {
        String first = items.get(0);
        FPNode child = node.getChild(first);

        if (child != null) {
            child.count += 1;
        } else {
            child = node.addChild(first);
            if (headers.get(first) == null) {
                headers.put(first, child);
            } else {
                FPNode current = headers.get(first);
                while (current.link != null) {
                    current = current.link;
                }
                current.link = child;
            }
        }

        List<String> remainingItems = items.subList(1, items.size());
        if (!remainingItems.isEmpty()) {
            insertTree(remainingItems, child, headers);
        }
    }

    public boolean treeHasSinglePath(FPNode node) {
        int numChildren = node.children.size();
        if (numChildren > 1) {
            return false;
        } else if (numChildren == 0) {
            return true;
        } else {
            return treeHasSinglePath(node.children.get(0));
        }
    }

    public Map<String, Integer> minePatterns(int threshold) {
        if (treeHasSinglePath(root)) {
            return generatePatternList();
        } else {
            return zipPatterns(mineSubTrees(threshold));
        }
    }

    public Map<String, Integer> zipPatterns(Map<String, Integer> patterns) {
        String suffix = root.value;
        if (suffix != null) {
            Map<String, Integer> newPatterns = new HashMap<>();
            for (String key : patterns.keySet()) {
                List<String> keyList = new ArrayList<>(Arrays.asList(key.split(" ")));
                keyList.add(suffix);
                Collections.sort(keyList);
                newPatterns.put(String.join(" ", keyList), patterns.get(key));
            }
            return newPatterns;
        }
        return patterns;
    }

    public Map<String, Integer> generatePatternList() {
        Map<String, Integer> patterns = new HashMap<>();
        Set<String> items = frequent.keySet();

        List<String> suffixValue = (root.value == null) ? new ArrayList<>() : Collections.singletonList(root.value);
        patterns.put(String.join(" ", suffixValue), root.count);

        for (int i = 1; i <= items.size(); i++) {
            for (Set<String> subset : generateCombinations(items, i)) {
                List<String> patternList = new ArrayList<>(subset);
                patternList.addAll(suffixValue);
                Collections.sort(patternList);
                String pattern = String.join(" ", patternList);
                patterns.put(pattern, getMinimumFrequency(subset));
            }
        }

        return patterns;
    }

    public Map<String, Integer> mineSubTrees(int threshold) {
        Map<String, Integer> patterns = new HashMap<>();
        List<String> miningOrder = new ArrayList<>(frequent.keySet());
        miningOrder.sort(Comparator.comparingInt(frequent::get));

        for (String item : miningOrder) {
            List<FPNode> suffixes = new ArrayList<>();
            List<List<String>> conditionalTreeInput = new ArrayList<>();
            FPNode node = headers.get(item);

            while (node != null) {
                suffixes.add(node);
                node = node.link;
            }

            for (FPNode suffix : suffixes) {
                int frequency = suffix.count;
                List<String> path = new ArrayList<>();
                FPNode parent = suffix.parent;

                while (parent.parent != null) {
                    path.add(parent.value);
                    parent = parent.parent;
                }

                for (int i = 0; i < frequency; i++) {
                    conditionalTreeInput.add(new ArrayList<>(path));
                }
            }

            FPTree subtree = new FPTree(conditionalTreeInput, threshold, item, frequent.get(item));
            Map<String, Integer> subtreePatterns = subtree.minePatterns(threshold);

            for (String pattern : subtreePatterns.keySet()) {
                patterns.put(pattern, patterns.getOrDefault(pattern, 0) + subtreePatterns.get(pattern));
            }
        }

        return patterns;
    }

    public Set<Set<String>> generateCombinations(Set<String> items, int size) {
        Set<Set<String>> result = new HashSet<>();
        generateCombinationsHelper(items.toArray(new String[0]), size, 0, new HashSet<>(), result);
        return result;
    }

    private void generateCombinationsHelper(String[] items, int size, int index, Set<String> current,
                                            Set<Set<String>> result) {
        if (current.size() == size) {
            result.add(new HashSet<>(current));
            return;
        }

        for (int i = index; i < items.length; i++) {
            current.add(items[i]);
            generateCombinationsHelper(items, size, i + 1, current, result);
            current.remove(items[i]);
        }
    }

    private int getMinimumFrequency(Set<String> items) {
        int minFrequency = Integer.MAX_VALUE;
        for (String item : items) {
            minFrequency = Math.min(minFrequency, frequent.get(item));
        }
        return minFrequency;
    }
}