package model;

import java.util.*;

/**
 * Node for Uncertain FP-tree (UFPNode)
 */
public class UFPNode {

    public String item;              // null for root
    public double count;             // expected count contribution (sum of probabilities)
    public UFPNode parent;
    public Map<String, UFPNode> children;
    public UFPNode nodeLink;         // link to next node with same item

    public UFPNode(String item, double count, UFPNode parent) {
        this.item = item;
        this.count = count;
        this.parent = parent;
        this.children = new LinkedHashMap<>();
        this.nodeLink = null;
    }

    public UFPNode getChild(String item) {
        return children.get(item);
    }

    public void addChild(UFPNode child) {
        children.put(child.item, child);
    }

    public void incrementCount(double delta) {
        this.count += delta;
    }

    /**
     * Return list of items in path from root (excluding current node). E.g. for
     * node representing X with parent chain (root -> A -> B -> X), this returns
     * [A, B].
     */
    public List<String> pathItemsToRoot() {
        List<String> path = new ArrayList<>();
        UFPNode cur = this.parent; // exclude current node's item
        while (cur != null && cur.item != null) {
            path.add(cur.item);
            cur = cur.parent;
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public String toString() {
        return "(" + item + ":" + String.format("%.4f", count) + ")";
    }
}
