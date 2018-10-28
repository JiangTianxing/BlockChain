// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;

    private ByteArrayWrapper oldest, newest;
    private Map<ByteArrayWrapper, BlockNode> blocks;
    private TransactionPool txPool;
    private int maxHeight;

    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        Transaction firstTx = genesisBlock.getCoinbase();
        UTXOPool utxoPool = new UTXOPool();
        for (int i = 0; i < firstTx.numOutputs(); i++) {
            Transaction.Output output = firstTx.getOutput(i);
            UTXO utxo = new UTXO(firstTx.getHash(), i);
            utxoPool.addUTXO(utxo, output);
        }

        this.newest = new ByteArrayWrapper(firstTx.getHash());
        this.oldest = this.newest;

        this.blocks = new HashMap<>();
        BlockNode currentNode = new BlockNode(genesisBlock, null, utxoPool);
        blocks.put(oldest, currentNode);

        this.txPool = new TransactionPool();
        txPool.addTransaction(firstTx);

        this.maxHeight = currentNode.getHeight();
    }

    public BlockNode getMaxHeightBlockNode() {
        return this.blocks.get(this.newest);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        // IMPLEMENT THIS
        return this.getMaxHeightBlockNode().getBlock();
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        // IMPLEMENT THIS
        return this.getMaxHeightBlockNode().getUtxoPool();
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        // IMPLEMENT THIS
        return this.txPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        // IMPLEMENT THIS
        if (block.getPrevBlockHash() == null) return false;
        ByteArrayWrapper preHash = new ByteArrayWrapper(block.getPrevBlockHash());
        if (!blocks.containsKey(preHash)) return false;

        BlockNode pNode = blocks.get(preHash);
        UTXOPool utxoPool = pNode.getUTXOPoolCopy();
        TxHandler txHandler = new TxHandler(utxoPool);

        List<Transaction> transactions = block.getTransactions();
        int size = transactions.size();
        Transaction[] accepted = txHandler.handleTxs(transactions.toArray(new Transaction[size]));
        if (accepted.length != size) return false;

        utxoPool = txHandler.getUtxoPool();
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output output = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, output);
        }

        BlockNode node = new BlockNode(block, pNode, utxoPool);
        node.setHeight(pNode.getHeight() + 1);

        if (node.getHeight() <= maxHeight - CUT_OFF_AGE) return false;
        ByteArrayWrapper hash = new ByteArrayWrapper(block.getHash());
        blocks.put(hash, node);

        if (node.getHeight() > maxHeight) {
            maxHeight = node.getHeight();
            newest = node.getHash();
        }
        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        // IMPLEMENT THIS
        this.txPool.addTransaction(tx);
    }

}