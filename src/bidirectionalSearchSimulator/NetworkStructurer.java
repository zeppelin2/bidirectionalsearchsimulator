package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import bidirectionalSearchSimulator.Link.LinkType;

/**
 * NetworkStructurer is a class that maintains information about the structure
 * of the network. Neither the network nor the nodes have access to this
 * information. This choice was made so that the my network could simulate a
 * decentralized and unstructured network without global topology information.
 * The resulting searching techniques must then proceed using only local
 * information stored by each node.
 */
public class NetworkStructurer implements java.io.Serializable {
    
    private static final long serialVersionUID = 5600359596807555861L;
    private final transient SearchCoordinator searchCoordinator;
    private final int nNodes; // number of nodes in the network 
    private final List<Node> nodeList; // list of all nodes in this network
    private final HashMap<Integer, double[]> nodeLocationMap; // locations of
    
    // all the nodes in this network. Lookup uses node ID as key, array of
    // double for x and y locations (at locations 0 and 1 of the array,
    // respectively).
    
    /**
     * Constructor.
     * 
     */
    public NetworkStructurer(final SearchCoordinator pSearchCoordinator,
            final int pnNodes) {
        searchCoordinator = pSearchCoordinator;
        nNodes = pnNodes;
        nodeList = new ArrayList<Node>(pnNodes);
        nodeLocationMap = new HashMap<Integer, double[]>(pnNodes);
    }
    
    /**
     * Gets a node by its ID.
     * 
     * Doesn't feel like a very efficient way of doing things, however, but a
     * HashMap would also result in linear time?
     * 
     * @param nodeID
     *            id of requested node
     * @return node matching the inputed nodeID, null if no such node exists
     */
    public Node getNodeByID(final int nodeID) {
        final Iterator<Node> it = nodeList.iterator();
        
        while (it.hasNext()) {
            final Node currentNode = it.next();
            if (currentNode.getNodeID() == nodeID) {
                return currentNode;
            }
        }
        
        return null;
    }
    
    /**
     * Calculates the total number of nodes in this network.
     * 
     * @return the number of nodes in the network
     */
    public int calculateTotalNumberOfNodes() {
        return nodeList.size();
    }
    
    /**
     * Calculates the total number of links in this network.
     * 
     * @return the number of links in the network
     */
    public int calculateTotalNumberOfLinks() {
        int nTotalDirectedLinks = 0;
        int nTotalUndirectedLinks = 0;
        
        /* Iterates over each node in the set of network nodes. */
        final Iterator<Node> itCurrentNodes = nodeList.iterator();
        Node aCurrentNode;
        
        while (itCurrentNodes.hasNext()) {
            aCurrentNode = itCurrentNodes.next();
            
            /* Iterates over each link in the set of links of the current node. */
            final Iterator<Link> itLink = aCurrentNode.getLinksSet().iterator();
            Link aCurrentLink;
            
            while (itLink.hasNext()) {
                aCurrentLink = itLink.next();
                
                if (aCurrentLink.getLinkType() == LinkType.UNDIRECTED) {
                    nTotalUndirectedLinks++;
                }
                else if (aCurrentLink.getLinkType() == LinkType.DIRECTED) {
                    nTotalDirectedLinks++;
                }
            }
        }
        
        /*
         * Test that the number of undirected links is even, as it should be.
         * Otherwise, signal that there's a problem.
         */
        if ((nTotalUndirectedLinks % 2) == 0) {
            return (nTotalUndirectedLinks / 2) + nTotalDirectedLinks;
        }
        else {
            return -1;
        }
    }
    
    /**
     * Calculates the distance between two points.
     * 
     * @param x1
     *            first point's x-coordinate
     * @param y1
     *            first point's y-coordinate
     * @param x2
     *            second point's x-coordinate
     * @param y2
     *            second point's x-coordinate
     * @return the distance between the two points
     */
    public double calculateDistanceBetweenNodes(final Node node1,
            final Node node2) {
        final double xNode1 = nodeLocationMap.get(node1.getNodeID())[0];
        final double yNode1 = nodeLocationMap.get(node1.getNodeID())[1];
        final double xNode2 = nodeLocationMap.get(node2.getNodeID())[0];
        final double yNode2 = nodeLocationMap.get(node2.getNodeID())[1];
        
        return Math.sqrt(Math.pow(xNode1 - xNode2, 2) +
                Math.pow(yNode1 - yNode2, 2));
    }
    
    /**
     * Calculates the direction of the line connecting two nodes.
     * 
     * @param sourceNode
     *            node at the source end of the direction vector
     * @param destinationNode
     *            node at the destination end of the direction vector
     * @return the direction of the vector (in degrees)
     */
    public double calculateDirection(final Node sourceNode,
            final Node destinationNode) {
        final double xSourceNode =
                nodeLocationMap.get(sourceNode.getNodeID())[0];
        final double ySourceNode =
                nodeLocationMap.get(sourceNode.getNodeID())[1];
        final double xDestinationNode =
                nodeLocationMap.get(destinationNode.getNodeID())[0];
        final double yDestinationNode =
                nodeLocationMap.get(destinationNode.getNodeID())[1];
        
        double direction =
                Math.toDegrees(Math.atan2(xDestinationNode - xSourceNode,
                        yDestinationNode - ySourceNode));
        
        /* Makes the scale 0 - 360 degrees instead of -180 to +180. */
        if (direction < 0) {
            direction += 360;
        }
        
        return direction;
    }
    
    /**
     * Getter for searchCoordinator.
     * 
     * @return the searchCoordinator
     */
    public SearchCoordinator getSearchCoordinator() {
        return searchCoordinator;
    }
    
    /**
     * Getter for nNodes.
     * 
     * @return the nNodes
     */
    public int getnNodes() {
        return nNodes;
    }
    
    /**
     * Getter for nodeList.
     * 
     * @return the nodeList
     */
    public List<Node> getNodeList() {
        return nodeList;
    }
    
    /**
     * Getter for nodeLocationMap.
     * 
     * @return the nodeLocationMap
     */
    public HashMap<Integer, double[]> getNodeLocationMap() {
        return nodeLocationMap;
    }
    
}
