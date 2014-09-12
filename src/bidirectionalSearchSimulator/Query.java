package bidirectionalSearchSimulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Query is a class that models an individual search query. The implementations
 * of a search query vary slightly depending on if it is used in a
 * flooding-based search strategy vs. a random walk-based strategy.
 * 
 * In flooding, the entire process of flooding the network from a single source
 * is considered a query. The search keeps track of the set of nodes where the
 * flood currently resides and these are what get propagated throughout the
 * network.
 * 
 * In random walks, each separate walker is considered its own query. The main
 * reason for this differentiation is that the path that each walker takes is
 * important in random walks than in floods (i.e. it can be used as memory in
 * a random walk) and makes more sense to have a separate entity follow its own
 * path.
 * 
 * Note that the set of visited nodes stored by the query includes the
 * current node(s) where the search query currently resides. This is so that
 * a node may be marked as visited even if it does not necessarily propagate
 * the query.
 * 
 * Missing from this class is the concept of time-to-live. As not all search
 * queries use a time-to-live, I decided to leave it up to the individual search
 * scheme to decide whether or not to implement it.
 */
public class Query implements Serializable {
    
    private static final long serialVersionUID = 2618525798433524854L;
    private final transient Search search;        // the search to which this query belongs
    private final int queryID;          // identifier of current search query
    private transient boolean firstHop;           // reveals if query has had its first hop yet
    private Node sourceNode;            // source node for each search instance
    private Set<Node> targetNodesSet;   // set of target nodes for each search instance
    private transient Set<Node> currentNodesSet;  // set of nodes where the search currently resides
    private transient Set<Node> visitedNodesSet;  // set of nodes visited during the search
    private transient HashMap<Integer, ArrayList<Integer>> currentNodesPrevHopMap;  // a sort of
    
    // memory that can be used by the search strategy to maintain information regarding
    // visited nodes and the order in which they were visited. See each search implementation to
    // find out the keys and values (the description starts with "Stores previous hop information").
    
    /**
     * Constructor.
     * 
     * @param pSearch
     *            the search to which this query belongs
     * @param pQueryID
     *            identifier of current search query
     */
    public Query(final Search pSearch, final int pQueryID) {
        search = pSearch;
        queryID = pQueryID;
        firstHop = true;
        
        targetNodesSet = new HashSet<Node>();
        currentNodesSet = new HashSet<Node>();
        currentNodesPrevHopMap = new HashMap<Integer, ArrayList<Integer>>();
        visitedNodesSet = new HashSet<Node>();
        
    }
    
    /**
     * Getter for queryID.
     * 
     * @return the queryID
     */
    public int getQueryID() {
        return queryID;
    }
    
    /**
     * Getter for search.
     * 
     * @return the search
     */
    public Search getSearch() {
        return search;
    }
    
    /**
     * Getter for firstHop.
     * 
     * @return the firstHop
     */
    public boolean isFirstHop() {
        return firstHop;
    }
    
    /**
     * Setter for firstHop.
     * 
     * @param firstHop
     *            the firstHop to set
     */
    public void setFirstHop(final boolean firstHop) {
        this.firstHop = firstHop;
    }
    
    /**
     * Getter for targetNodesSet.
     * 
     * @return the targetNodesSet
     */
    public Set<Node> getTargetNodesSet() {
        return targetNodesSet;
    }
    
    /**
     * Setter for targetNodesSet.
     * 
     * @param targetNodesSet
     *            the targetNodesSet to set
     */
    public void setTargetNodesSet(final Set<Node> targetNodesSet) {
        this.targetNodesSet = targetNodesSet;
    }
    
    /**
     * Getter for currentNodesPrevHopMap.
     * 
     * @return the currentNodesPrevHopMap
     */
    public HashMap<Integer, ArrayList<Integer>> getCurrentNodesPrevHopMap() {
        return currentNodesPrevHopMap;
    }
    
    /**
     * Setter for currentNodesPrevHopMap.
     * 
     * @param currentNodesPrevHopMap
     *            the currentNodesPrevHopMap to set
     */
    public void setCurrentNodesPrevHopMap(
            final HashMap<Integer, ArrayList<Integer>> currentNodesPrevHopMap) {
        this.currentNodesPrevHopMap = currentNodesPrevHopMap;
    }
    
    /**
     * Getter for currentNodesSet.
     * 
     * @return the currentNodesSet
     */
    public Set<Node> getCurrentNodesSet() {
        return currentNodesSet;
    }
    
    /**
     * Setter for currentNodesSet.
     * 
     * @param currentNodesSet
     *            the currentNodesSet to set
     */
    public void setCurrentNodesSet(final Set<Node> currentNodesSet) {
        this.currentNodesSet = currentNodesSet;
    }
    
    /**
     * Getter for visitedNodesSet.
     * 
     * @return the visitedNodesSet
     */
    public Set<Node> getVisitedNodesSet() {
        return visitedNodesSet;
    }
    
    /**
     * Setter for visitedNodesSet.
     * 
     * @param visitedNodesSet
     *            the visitedNodesSet to set
     */
    public void setVisitedNodesSet(final Set<Node> visitedNodesSet) {
        this.visitedNodesSet = visitedNodesSet;
    }
    
    /**
     * Getter for sourceNode.
     * 
     * @return the sourceNode
     */
    public Node getSourceNode() {
        return sourceNode;
    }
    
    /**
     * Setter for sourceNode.
     * 
     * @param sourceNode
     *            the sourceNode to set
     */
    public void setSourceNode(final Node sourceNode) {
        this.sourceNode = sourceNode;
    }
    
}
