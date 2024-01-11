import java.util.ArrayList;
import java.util.List;

class FPNode {
    String value;
    int count;
    FPNode parent;
    FPNode link;
    List<FPNode> children;

    public FPNode(String value, int count, FPNode parent) {
        this.value = value;
        this.count = count;
        this.parent = parent;
        this.link = null;
        this.children = new ArrayList<>();
    }

    public FPNode getChild(String value) {
        for (FPNode node : children) {
            if (node.value.equals(value)) {
                return node;
            }
        }
        return null;
    }

    public FPNode addChild(String value) {
        FPNode child = new FPNode(value, 1, this);
        children.add(child);
        return child;
    }
}