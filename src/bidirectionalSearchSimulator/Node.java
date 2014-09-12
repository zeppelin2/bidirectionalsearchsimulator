package bidirectionalSearchSimulator;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Node is a class that models a node in a network.
 */
public class Node implements Serializable {
    
    private static final long serialVersionUID = 4202835272064624740L;
    private int nodeID;         // this node's unique identifier
    private Set<Link> linkSet;  // set of all links connected to this node
    
    /**
     * Constructor.
     * 
     * @param pNodeID
     *            this node's unique identifier
     */
    public Node(final int pNodeID) {
        nodeID = pNodeID;
        linkSet = new HashSet<Link>();
    }
    
    /**
     * Adds a link to the link set of this node.
     * 
     * @param linkToAdd
     *            the link to add to the link set of this node
     */
    public void addLinkToLinkSet(final Link linkToAdd) {
        linkSet.add(linkToAdd);
    }
    
    /**
     * Checks to see if this node is connected to another node.
     * 
     * @return true if the two nodes are connected, false otherwise
     */
    public boolean isConnectedTo(final Node anotherNode) {
        final Iterator<Link> it = linkSet.iterator();
        
        while (it.hasNext()) {
            final Link currentLink = it.next();
            if (currentLink.doesConnect(anotherNode)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Picks a random neighbour amongst the direct neighbours of this node. If
     * the node has no neighbours, it simply returns itself.
     * 
     * @return the randomly chosen neighbour or null
     */
    public Node pickRandomNeighbour() {
        if (linkSet.size() == 0) {
            return this;
        }
        
        final int randomLinkPosition =
                (int) (Math.floor(Math.random() * linkSet.size()));
        int linkPositionCounter = 0;
        
        /* Iterates over each link in the set of links belonging to this node. */
        final Iterator<Link> itLinks = linkSet.iterator();
        Link currentLink;
        
        while (itLinks.hasNext()) {
            currentLink = itLinks.next();
            
            if (linkPositionCounter == randomLinkPosition) {
                return currentLink.connectedBy(this);
            }
            
            linkPositionCounter++;
        }
        
        return null;
    }
    
    /**
     * Calculates the degree of this node.
     * 
     * @return the degree of this node
     */
    public int degree() {
        return linkSet.size();
    }
    
    /**
     * Getter for nodeID.
     * 
     * @return the nodeID
     */
    public int getNodeID() {
        return nodeID;
    }
    
    /**
     * Setter for nodeID.
     * 
     * @param nodeID
     *            the nodeID to set
     */
    public void setNodeID(final int nodeID) {
        this.nodeID = nodeID;
    }
    
    /**
     * Getter for linkSet.
     * 
     * @return the linkSet
     */
    public Set<Link> getLinksSet() {
        return linkSet;
    }
    
    /**
     * Setter for linkSet.
     * 
     * @param linksSet
     *            the linkSet to set
     */
    public void setLinksSet(final Set<Link> linksSet) {
        linkSet = linksSet;
    }
    
}
