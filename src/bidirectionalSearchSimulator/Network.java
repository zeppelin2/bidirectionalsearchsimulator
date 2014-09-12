package bidirectionalSearchSimulator;

/**
 * Network is an abstract class that models an unstructured network. As the
 * network is decentralized, it does not store information about the nodes.
 * 
 */
public abstract class Network {
    
    protected NetworkStructurer networkStructurer;
    protected int nNodes;  // total number of nodes in the network
    
    /**
     * Constructor.
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pnNodes
     *            total number of nodes in the network
     */
    public Network(final NetworkStructurer pNetworkStructurer, final int pnNodes) {
        networkStructurer = pNetworkStructurer;
        nNodes = pnNodes;
    }
    
    /**
     * Generates the nodes for this network.
     * 
     * @param nNodes
     *            number of nodes to generate
     */
    public abstract void generateNodes();
    
    /**
     * Generates the links for this network.
     */
    public abstract void generateLinks();
    
}
