
import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.MemoryBlockStore;

import java.util.concurrent.Future;

/**
 * Sample app to demonstrate how can easy can we iterate the Bitcoin blockchain in Java
 * using bitcoinj. In this example I look for a particular block, and then a particular
 * transaction in it.
 *
 * I do look for specific hashes, but you can very easily iterate the entire blockchain
 * like that!
 */
public class FetchOneBlock {

    public static void main(String[] args)
    throws Exception {
        new FetchOneBlock().fetchOneBlock();
    }

    private void fetchOneBlock() throws Exception {
        // we operate on the testnet
        final NetworkParameters params = Network.params();

        // an example block hash and a transaction hash from the testnet
        final String blockHash = "00000000000001235cf913d49fa17d188502759f51ed5c615af1ee0e6a972028";
        final String transactionHash = "b35bffab551ae54b545bc8e444fbcce63375c4bfd27db3fdc26808bd69e2da52";

        // configure the API
        final BlockStore blockStore = new MemoryBlockStore(params);
        final BlockChain chain = new BlockChain(params, blockStore);
        final PeerGroup peerGroup = new PeerGroup(params, chain);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));

        // connect to the network
        peerGroup.start();
        peerGroup.waitForPeers(1).get();

        // get the first peer we have connected to
        final Peer peer = peerGroup.getConnectedPeers().get(0);

        // get the block we are looking for
        final Future<Block> futureBlock = peer.getBlock(Sha256Hash.wrap(blockHash));
        final Block block = futureBlock.get();

        // look for the particular transaction in it
        final Transaction transaction = block.getTransactions().stream()
                .filter((t) -> t.getHashAsString().equals(transactionHash))
                .findFirst()
                .get();

        // print some block and transaction details
        System.out.println("Block hash: " + block.getHashAsString());
        System.out.println("Block date: " + block.getTime());
        System.out.println("Block merkle root: " + block.getMerkleRoot());
        System.out.println("Block nonce: " + block.getNonce() + "\n");
        System.out.println("Transaction hash: " + transaction.getHashAsString());
        System.out.println("Transaction inputs:");
        transaction.getInputs().forEach(input -> System.out.println(" " + input.getScriptSig()));
        System.out.println("Transaction outputs:");
        transaction.getOutputs().forEach(output -> System.out.println(" " + output.getScriptPubKey()));

        // you can easily extract much more details from the block and the transactions
        // bear in mind that following money flow on the blockchain is really easy!
    }
}
