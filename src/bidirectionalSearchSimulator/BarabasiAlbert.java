package bidirectionalSearchSimulator;

/**
 * BarabasiAlbert is a class that models an Barbasi-Albert (BA) random
 * graph.
 * 
 * These graphs incorporate the features of network growth and preferential
 * attachment. The idea is that the more connected a node is, the more likely it
 * is to receive new links. The degrees of nodes are said to follow a power law,
 * which translates to few nodes with a very high degree and many with a low
 * degree. The high degree nodes can then act as supernodes (i.e. hubs) in
 * communication and networking, something which can be exploited when designing
 * efficient search algorithms.
 * 
 * The initial number of nodes must be greater or equal to two and it has been
 * recommended to use at least 1000 nodes in order to obtain a satisfactory
 * scale-free network (source:
 * http://wiki.cns.iu.edu/pages/viewpage.action?pageId=1704040)
 */
public class BarabasiAlbert extends Network {
    
    private final int nInitialNodes;  // number of nodes that the network
    // contains initially
    private final int nLinksToAddAtEachStep;  // number of links to add each time
    // a node is added to the network. Should be less than the number of initial
    // nodes of the network.
    private int nTotalLinks = 0;  // total number of links in the network
    private double linkProbability;  // probability of generating a link between
    
    // two nodes
    
    /**
     * Constructor.
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pnNodes
     *            total number of nodes in the network
     * @param pnInitialNodes
     *            number of nodes that the network contains initially
     * @param pnLinksToAddAtEachStep
     *            number of links to add each time
     *            a node is added to the network. Should be less than the number
     *            of initial
     *            nodes of the network.
     */
    public BarabasiAlbert(final NetworkStructurer pNetworkStructurer,
            final int pnNodes, final int pnInitialNodes,
            final int pnLinksToAddAtEachStep) {
        super(pNetworkStructurer, pnNodes);
        nInitialNodes = pnInitialNodes;
        nLinksToAddAtEachStep = pnLinksToAddAtEachStep;
    }
    
    /**
     * Generates the nodes for the initial connected network.
     */
    @Override
    public void generateNodes() {
        for (int iNode = 0; iNode < nInitialNodes; iNode++) {
            /* Creates a node. */
            networkStructurer.getNodeList().add(iNode, new Node(iNode));
            
            /* Generates and stores the node's location. */
            networkStructurer.getNodeLocationMap().put(iNode,
                    new double[] {Math.random(), Math.random()});
        }
    }
    
    /**
     * Generates the links for the initial connected network.
     */
    @Override
    public void generateLinks() {
        for (int iNode1 = 0; iNode1 < nInitialNodes; iNode1++) {
            final Node node1 = networkStructurer.getNodeList().get(iNode1);
            
            for (int iNode2 = iNode1 + 1; iNode2 < nInitialNodes; iNode2++) {
                final Node node2 = networkStructurer.getNodeList().get(iNode2);
                
                new Link(node1.getNodeID(), node2.getNodeID(), networkStructurer);
                
                nTotalLinks++;
            }
        }
    }
    
    /**
     * Generates the resulting Barabasi-Albert network by building on the
     * initial generated network.
     */
    public void generateBAModel() {
        for (int iNode1 = nInitialNodes; iNode1 < nNodes; iNode1++) {
            /* Creates a node. */
            final Node aNewNode = new Node(iNode1);
            networkStructurer.getNodeList().add(aNewNode);
            
            /* Generates and stores the node's location. */
            networkStructurer.getNodeLocationMap().put(iNode1,
                    new double[] {Math.random(), Math.random()});
            
            /*
             * Attaches the new node to a predefined number of other nodes
             * following a distribution proportional to the vertex degree. If
             * the number of nodes in the network is less than the predefined
             * number of links to add at each step, then lower the latter to
             * match the number of nodes until this number increases to the point
             * where the full number of links at each step can be used.
             *
             * The "size - 1" in the test condition and allocation of number of
             * links to add at each step is to account for the fact that we
             * created and added a node to the network earlier in this method.
             */
            int nLinksToAddAtEachStepCounter;
            
            if ((networkStructurer.getNodeList().size() - 1) < nLinksToAddAtEachStep) {
                nLinksToAddAtEachStepCounter =
                        networkStructurer.getNodeList().size() - 1;
            }
            else {
                nLinksToAddAtEachStepCounter = nLinksToAddAtEachStep;
            }
            
            int aRandomNodeID;
            Node aRandomNode;
            Link aNewLink;
            
            while (nLinksToAddAtEachStepCounter > 0) {
                /* Chooses a random node in the network. */
                aRandomNodeID =
                        (int) Math.floor(Math.random() *
                                networkStructurer.getNodeList().size());
                aRandomNode =
                        networkStructurer.getNodeList().get(aRandomNodeID);
                
                /*
                 * Checks that the total number of links in the network is greater
                 * than zero to avoid a division by zero error. This could arise
                 * if only one node is present.
                 */
                if (nTotalLinks > 0) {
                    linkProbability =
                            (double) aRandomNode.degree() / nTotalLinks;
                }
                else {
                    linkProbability = 1;
                }
                
                /*
                 * Generates link.
                 *
                 * First, it makes sure that the link is not a self-link
                 * (i.e. a link that links the node that was just created back
                 * to itself) and that the link does not already exist.
                 */
                if ((Math.random() < linkProbability) &&
                        (aNewNode.getNodeID() != aRandomNode.getNodeID()) &&
                        (!aNewNode.isConnectedTo(aRandomNode))) {
                    aNewLink =
                            new Link(aNewNode.getNodeID(), aRandomNode
                                    .getNodeID(), networkStructurer);
                    
                    aNewNode.addLinkToLinkSet(aNewLink);
                    aRandomNode.addLinkToLinkSet(aNewLink);
                    
                    nTotalLinks++;
                    nLinksToAddAtEachStepCounter--;
                }
            }
        }
    }
    
}
