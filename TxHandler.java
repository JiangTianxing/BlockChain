import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = utxoPool;
    }

    public boolean isValidTx(Transaction tx) {
        double sumOfInput = 0;
        double sumOfOutput = 0;
        Set<UTXO> used = new HashSet<>();
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            //1.all outputs claimed by tx are in the current UTXO pool,
            if (!utxoPool.contains(utxo)) return false;
            //2.the signatures on each input of tx are valid
            Transaction.Output originOutput = utxoPool.getTxOutput(utxo);
            if (!Crypto.verifySignature(originOutput.address, tx.getRawDataToSign(i), input.signature)) return true;
            //3.no UTXO is claimed multiple times by tx
            if (used.contains(utxo)) return false;
            used.add(utxo);
            sumOfInput += originOutput.value;
        }
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            //4.all of tx's output values are non-negative
            if (output.value < 0) return false;
            sumOfOutput += output.value;
        }
        //5.the sum of tx's input values is greater than or equal to the sum of its output values; and false otherwise
        return !(sumOfInput < sumOfOutput);
    }

    /*
     * Handles each epoch by receiving an unordered array of proposed * transactions,
     * checking each transaction for correctness,
     * returning a mutually valid array of accepted transactions,
     * and updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> txVis = new HashSet<>();
        for (Transaction tx : possibleTxs) {
            if (txVis.contains(tx)) continue;
            if (!this.isValidTx(tx)) continue;
            //updating the current UTXO pool
            for (int i = 0; i < tx.numInputs(); i++) {
                Transaction.Input input = tx.getInput(i);
                UTXO utxo = new UTXO(input.prevTxHash, i);
                utxoPool.removeUTXO(utxo);
            }
            for (int i = 0; i < tx.numOutputs(); i++) {
                Transaction.Output output = tx.getOutput(i);
                UTXO utxo = new UTXO(tx.getHash(), i);
                utxoPool.addUTXO(utxo, output);
            }
            txVis.add(tx);
        }
        int index = 0;
        Transaction[] transactions = new Transaction[txVis.size()];
        for (Transaction tx : txVis) {
            transactions[index++] = tx;
        }
        return transactions;
    }

    public UTXOPool getUtxoPool() {
        return this.utxoPool;
    }
}