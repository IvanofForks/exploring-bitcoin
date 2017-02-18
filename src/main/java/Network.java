import org.bitcoinj.core.NetworkParameters;

class Network {

    // we operate on the testnet
    static NetworkParameters params() {
        NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
        if (params == null)
            throw new RuntimeException("Network not found");
        return params;
    }
}
