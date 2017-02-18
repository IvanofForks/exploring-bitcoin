import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

/**
 * Creates a new Bitcoin wallet in the data directory.
 * Should be run before WalletSync.
 */
public class WalletCreate {

    private static final File WALLET_FILE = new File("data/testnet_wallet_2.wallet");

    public static void main(String[] args)
    throws Exception {
        new WalletCreate().create();
    }

    private void create()
    throws Exception {
        // we operate on the testnet
        final NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);

        // create a brand new wallet and sace it
        final Wallet wallet = new Wallet(params);
        wallet.saveToFile(WALLET_FILE);
    }
}
