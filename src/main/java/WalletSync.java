import org.bitcoinj.core.*;
import org.bitcoinj.net.discovery.DnsDiscovery;
import org.bitcoinj.store.BlockStore;
import org.bitcoinj.store.SPVBlockStore;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Working as an SPV client node, synchronizes an existing wallet against the blockchain.
 * The blockchain state is kept in a disk file (blockstore). Every time we run the app, it reads
 * the blockstore, downloads new block headers (if any) and looks for transactions (Bloom filter)
 * for our particular wallet.
 *
 * Any newly discovered transactions get reported via the event handler.
 * Also the complete wallet state gets printed out after the sync finishes.
 *
 * If you switch to a different wallet, please delete the blockstore and let the app sync with
 * the entire blockchain again, as the transactions from the blocks which have been
 * already processed won't be rediscovered after the wallet change.
 *
 * Please be aware of how bitcoinj utilizes Bloom filters, as your identity may be very easily
 * discovered (compromised) by the nodes you are connecting to.
 */
public class WalletSync {
    private static final File WALLET_FILE = new File("data/testnet_wallet.wallet");
    private static final File BLOCKSTORE_FILE = new File("data/testnet_blockStore.dat");

    public static void main(String[] args)
    throws Exception {
        new WalletSync().sync();
    }

    private void sync()
    throws Exception {
        // we operate on the testnet
        final NetworkParameters params = Network.params();

        // load the wallet and print it
        final Wallet wallet = loadWallet();
        printWallet(wallet);

        // enable wallet autosave so the wallet file gets updated
        // when we encounter new transactions on the blockchain
        enableWalletAutosave(wallet);

        // initialize the API
        final BlockStore blockStore = new SPVBlockStore(params, BLOCKSTORE_FILE);
        final BlockChain blockChain = new BlockChain(params, wallet, blockStore);
        final PeerGroup peerGroup = new PeerGroup(params, blockChain);
        peerGroup.setMaxConnections(4);
        peerGroup.addPeerDiscovery(new DnsDiscovery(params));

        // if we want to use a local Bitcoin node:
        // peerGroup.addAddress(InetAddress.getLocalHost());

        // add some events so we see the connections and reconnections
        peerGroup.addConnectedEventListener((peer, peerCount) ->
                System.out.println("Connected to " +peer + "; total peers: " + peerCount));
        peerGroup.addDisconnectedEventListener((peer, peerCount) ->
                System.out.println("Disconnected from " + peer + "; total peers: " + peerCount));

        // connect to at least three nodes
        peerGroup.startAsync();
        peerGroup.waitForPeers(3).get();

        // add an event so we
        wallet.addCoinsReceivedEventListener((w, tx, prevBalance, newBalance) -> {
            System.out.println("\nNew transaction received:\n" + tx.toString());
        });

        // download and process the remaining part of the blockchain
        peerGroup.downloadBlockChain();
        peerGroup.stopAsync();

        // print the wallet after the blockchain sync
        printWallet(wallet);

        // save the wallet
        saveWallet(wallet);
    }

    private Wallet loadWallet() throws UnreadableWalletException {
        return Wallet.loadFromFile(WALLET_FILE);
    }

    private void printWallet(final Wallet wallet) throws IOException {
        System.out.println("**********");
        System.out.println("Wallet balance is: " + wallet.getBalance().toFriendlyString());
        System.out.println("Current receive address is: " + wallet.currentReceiveAddress());
        System.out.println("Issued receive addresses are:");
        System.out.println("**********");
        System.out.println(wallet.toString());
        System.out.println("**********");
    }

    private void enableWalletAutosave(final Wallet wallet) {
        wallet.autosaveToFile(WALLET_FILE, 10, TimeUnit.SECONDS, null);
    }

    private void saveWallet(final Wallet wallet) throws IOException {
        wallet.saveToFile(WALLET_FILE);
    }
}
