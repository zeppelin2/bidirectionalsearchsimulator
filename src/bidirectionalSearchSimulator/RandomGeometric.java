package bidirectionalSearchSimulator;

import java.util.Iterator;
import java.util.List;

/**
 * RandomGeometric is a class that models a random geometric graph.
 * 
 * Nodes are placed randomly within the network space and create links with
 * other nodes found within a certain proximity (i.e. distance).
 */
public class RandomGeometric extends Network {
    
    private final double linkProbability; // probability used in this sense is related
    
    // to the distance between nodes. A higher "probability" increases the
    // permitted distance between nodes to form links and thus increases the
    // probability of forming a link between two nodes
    
    /**
     * Constructor.
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pnNodes
     *            total number of nodes in the network
     * @param pLinkProbability
     *            probability (distance-based) that a link is generated between
     *            two nodes
     */
    public RandomGeometric(final NetworkStructurer pNetworkStructurer,
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
     * if the distance between the two nodes is less than or equal to the
     * probability linkProbability.
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
                
                // Even though this method examines each pair of nodes twice
                // (i.e. 'node A and node B' as well as 'node B and node A'),
                // the distance between the two nodes remains constant between
                // examinations and thus the link will be created upon the first
                // examination if it's less than the link probability. Thus, no
                // halving of the link probability is needed (see Erdos-Renyi).
                //
                // Also check that we aren't connecting a node to itself and
                // that node1 isn't already connected to node2 (as node1 to
                // node2 and node2 to node1 are the same link and should not
                // be connected twice).
                if (networkStructurer.calculateDistanceBetweenNodes(node1,
                        node2) < linkProbability &&
                        node1 != node2 && !node1.isConnectedTo(node2)) {
                    new Link(node1.getNodeID(), node2.getNodeID(),
                            networkStructurer);
                }
            }
        }
    }
}
