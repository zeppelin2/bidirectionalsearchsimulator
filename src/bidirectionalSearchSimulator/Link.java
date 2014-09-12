package bidirectionalSearchSimulator;

import java.io.Serializable;

/**
 * Link is a class that models a link connecting two nodes in a network.
 */
public class Link implements Serializable {
    
    private static final long serialVersionUID = 7281849745239218665L;
    
    public enum LinkType {
        DIRECTED, UNDIRECTED
    }
    
    private final NetworkStructurer networkStructurer;  // the network structurer
    // associated with the network in which this link will belong. It allows
    // conversion of the source and destination node IDs into their respective
    // node instances.
    //
    // Initially, I felt strongly about not including this association in the
    // link class, as I felt that a link should not have access to this set of
    // network information. It should only maintain information on the two nodes
    // it is connected to. I changed my mind for two reasons: first, the process
    // of generating links was incredibly resource heavy (especially time) when
    // instances of the source and destination node were stored (instead of their
    // respective IDs). Second, there became the problem of a stackOverflowError
    // when trying to serialize the network structurer due to the large amount
    // of cyclic references between a node and a link -- a node maintains which
    // links it has and a link maintains the two nodes to which it is connected.
    // Both of the above made it very difficult to create a large network
    // (e.g. 10 000 nodes) in a reasonable amount of time and still be able to
    // save it.
    
    private final int sourceNodeID;        // ID of node at the source end of the link
    private final int destinationNodeID;   // ID of node at the destination end of the link
    
    private LinkType linkType;       // this link's type
    
    /**
     * Constructor which uses two nodes to define a link.
     * 
     * @param pNodeSource
     *            node at the source end of the link
     * @param pNodeDestination
     *            node at the destination end of the link
     */
    public Link(final int pSourceNodeID, final int pDestinationNodeID,
            final NetworkStructurer pNetworkStructurer) {
        networkStructurer = pNetworkStructurer;
        sourceNodeID = pSourceNodeID;
        destinationNodeID = pDestinationNodeID;
        
        linkType = LinkType.UNDIRECTED; // by default, a link is undirected
        
        /*
         * Add this link to the link sets of both the source and destination
         * nodes.
         */
        networkStructurer.getNodeByID(sourceNodeID).addLinkToLinkSet(this);
        networkStructurer.getNodeByID(destinationNodeID).addLinkToLinkSet(this);
    }
    
    /**
     * Checks to see if this link connects to this node.
     * 
     * @param aNode
     *            the node to check if is connected by this link
     * @return true if this link connects to the node, false otherwise
     * 
     */
    public boolean doesConnect(final Node aNode) {
        if ((sourceNodeID == aNode.getNodeID()) ||
                (destinationNodeID == aNode.getNodeID())) {
            return true;
        }
        else {
            return false;
        }
    }
    
    /**
     * Returns the node at the other end of the link that is connected by pNode.
     * 
     * @param pNode
     *            the node provided as input for which the opposite node is
     *            desired
     * @return the node opposite of pNode
     */
    public Node connectedBy(final Node pNode) {
        if (pNode.getNodeID() == sourceNodeID) {
            return networkStructurer.getNodeByID(destinationNodeID);
        }
        else {
            return networkStructurer.getNodeByID(sourceNodeID);
        }
    }
    
    /**
     * Getter for linkType.
     * 
     * @return the linkType
     */
    public LinkType getLinkType() {
        return linkType;
    }
    
    /**
     * Setter for linkType.
     * 
     * @param linkType
     *            the linkType to set
     */
    public void setLinkType(final LinkType linkType) {
        this.linkType = linkType;
    }
    
    /**
     * Getter for networkStructurer.
     * 
     * @return the networkStructurer
     */
    public NetworkStructurer getNetworkStructurer() {
        return networkStructurer;
    }
    
    /**
     * Getter for sourceNodeID.
     * 
     * @return the sourceNodeID
     */
    public int getSourceNodeID() {
        return sourceNodeID;
    }
    
    /**
     * Getter for destinationNodeID.
     * 
     * @return the destinationNodeID
     */
    public int getDestinationNodeID() {
        return destinationNodeID;
    }
    
//    /**
//     * Getter for nodeSourceID.
//     *
//     * @return the nodeSourceID
//     */
//    public int getNodeSourceID() {
//        return nodeSourceID;
//    }
//
//    /**
//     * Getter for nodeDestinationID.
//     *
//     * @return the nodeDestinationID
//     */
//    public int getNodeDestinationID() {
//        return nodeDestinationID;
//    }
    
}
