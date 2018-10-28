import java.util.List;

public class BlockNode {
    private Block block;
    private BlockNode parent;
    private List<BlockNode> children;
    private int height;
    private UTXOPool utxoPool;

    public BlockNode(Block block, BlockNode parent, UTXOPool utxoPool) {
        this.block = block;
        this.utxoPool = utxoPool;
        if (parent == null) {
            height = 1;
        } else {
            parent.children.add(this);
            height = parent.height + 1;
        }
        this.parent = parent;
    }

    public ByteArrayWrapper getHash() {
        return new ByteArrayWrapper(block.getHash());
    }

    public UTXOPool getUTXOPoolCopy() {
        return new UTXOPool(utxoPool);
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public BlockNode getParent() {
        return parent;
    }

    public void setParent(BlockNode parent) {
        this.parent = parent;
    }

    public List<BlockNode> getChildren() {
        return children;
    }

    public void setChildren(List<BlockNode> children) {
        this.children = children;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public UTXOPool getUtxoPool() {
        return utxoPool;
    }

    public void setUtxoPool(UTXOPool utxoPool) {
        this.utxoPool = utxoPool;
    }
}
