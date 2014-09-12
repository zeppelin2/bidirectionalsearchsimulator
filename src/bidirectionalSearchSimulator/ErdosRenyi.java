package bidirectionalSearchSimulator;

import java.util.Iterator;
import java.util.List;

/**
 * ErdosRenyi is a class that models an Erdos-Renyi (ER) random
 * graph.
 * 
 * These graphs can be constructed by connecting nodes randomly, where each link
 * has the same probability for being included in the graph, independent of all
 * other links.
 */
public class ErdosRenyi extends Network {
    
    private final double linkProbability;  // probability that a link is generated
    
    // between two nodes
    
    /**
     * Constructor.
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pnNodes
     *            total number of nodes in the network
     * @param pLinkProbability
     *            probability that a link is generated between two nodes
     */
    public ErdosRenyi(final NetworkStructurer pNetworkStructurer,
            final int pnNodes, final double pLinkProbability) {
        super(pNetworkStructurer, pnNodes);
        linkProbability = pLinkProbability;
    }
    
    /**
     * Generates the nodes for this network.
     */
    @Override
    public void generateNodes() {
        for (int iNode = 0; iNode < nNodes; iNode++) {
            /* Creates a node. */
            networkStructurer.getNodeList().add(new Node(iNode));
            
            /* Generates and stores the node's location. */
            networkStructurer.getNodeLocationMap().put(iNode,
                    new double[] {Math.random(), Math.random()});
        }
    }
    
    /**
     * Generates the links for this network. Each link is generated
     * independently of the others based on the link probability.
     */
    @Override
    public void generateLinks() {
        List<Node> nodeList = networkStructurer.getNodeList();
        final Iterator<Node> it1 = nodeList.iterator();
        
        while (it1.hasNext()) {
            Node node1 = it1.next();
            Iterator<Node> it2 = nodeList.iterator();
            
            while (it2.hasNext()) {
                Node node2 = it2.next();
                
                // Since this method examines each pair of nodes twice (i.e.
                // 'node A and node B' as well as 'node B and node A'), it
                // effectively doubles the probability of generating a link. To
                // compensate, we divide the link probability in half.
                //
                // Also check that we aren't connecting a node to itself and
                // that node1 isn't already connected to node2 (as node1 to
                // node2 and node2 to node1 are the same link and should not
                // be connected twice).
                if (Math.random() < linkProbability * 0.5 && node1 != node2 &&
                        !node1.isConnectedTo(node2)) {
                    new Link(node1.getNodeID(), node2.getNodeID(),
                            networkStructurer);
                }
            }
        }
    }
    
}
