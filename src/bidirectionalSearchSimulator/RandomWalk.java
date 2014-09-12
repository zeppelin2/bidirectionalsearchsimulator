package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * RandomWalk is a class that models the algorithm of searching using
 * a random walk.
 * 
 * In a random walk (RW), the query propagation phase forwards the query
 * to a randomly chosen neighbour. There are no restrictions on choice of the
 * neighbour – the query is permitted to travel back to the neighbour that sent
 * it. A current node is not considered a neighbour of itself. The RW ends with
 * a success if it finds the target node within the predefined TTL, or a failure
 * if the TTL is reached. The TTL for a RW refers to either the limit on the
 * number of time steps or messages permitted, as both of these values are one
 * the same.
 * 
 * It is possible to have the random walk choose its next neighbour at random,
 * excluding the node that sent it the query, by modifying the code below. Look
 * for the do/while loop that that needs to be uncommented for this change to be
 * applied.
 */
public class RandomWalk extends Search {
    
    private static int nQueries = 1;  // number of queries to use in this search
    private final HashMap<Integer, Integer> ttlMap;  // time-to-live values for each query
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            RandomWalk
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pttl
     *            time-to-live value for each search query
     */
    public RandomWalk(final SearchCoordinator pSearchCoordinator,
            final NetworkStructurer pNetworkStructurer, final int pttl) {
        super(pSearchCoordinator, pNetworkStructurer, nQueries);
        
        /*
         * Sets the TTL for each query to the TTL specified in the control panel
         * by the user. This can be changed at a later date to permit varying
         * TTL values.
         */
        ttlMap = new HashMap<Integer, Integer>();
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            ttlMap.put(iQuery, pttl);
        }
        
    }
    
    /**
     * Propagates the search queries by one step.
     */
    @Override
    public void propagateQueries() {
        Set<Node> currentNodesSetTemp; // stores the new set of current nodes
        // while the "old" set of current nodes are being added to the
        // set of visited nodes.
        
        /* Does the following for each search query. */
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            final Query currentQuery = searchQueriesMap.get(iQuery);
            
            currentNodesSetTemp = new HashSet<Node>();
            
            /*
             * Checks to see if this is the first hop of the query. If so,
             * initialize the previous hop structure.
             */
            ArrayList<Integer> prevHopArrayList;
            
            if (currentQuery.isFirstHop()) {
                prevHopArrayList = new ArrayList<Integer>();
                
                currentQuery.getCurrentNodesPrevHopMap().put(-1,
                        prevHopArrayList);
            }
            
            /* Checks that the TTL for the current query has not expired. */
            if (ttlMap.get(currentQuery.getQueryID()) > 0) {
                
                /* Iterates over each node in the set of current nodes. */
                final Iterator<Node> itCurrentNodes =
                        currentQuery.getCurrentNodesSet().iterator();
                Node aCurrentNode;
                
                while (itCurrentNodes.hasNext()) {
                    aCurrentNode = itCurrentNodes.next();
                    
                    /*
                     * Picks a random neighbour of the current node.
                     *
                     * NOTE: To pick a random neighbour except for the
                     * node that sent it the message, uncomment the do/while loop
                     * in the code below. This essentially means that the ranom walk
                     * has a memory of one hop.
                     *
                     * An exception is made if the degree of the current node is one,
                     * in which case it is allowed to send the query back to the
                     * previous hop so as to avoid deadlock.
                     */
                    Node aRandomNeighbour;
                    
//                do {
                    aRandomNeighbour = aCurrentNode.pickRandomNeighbour();
//                } while ((!currentQuery.getCurrentNodesPrevHopMap().get(-1)
//                    .isEmpty()) &&
//                    (currentQuery.getCurrentNodesPrevHopMap().get(-1).get(0) == aRandomNeighbour
//                        .getNodeID()) && (aCurrentNode.degree() > 1));
                    
                    /*
                     * Forwards the query (i.e. the selected neighbour becomes a
                     * current node).
                     */
                    currentNodesSetTemp.add(aRandomNeighbour);
                    
                    /* Adds the neighbour to list of visited nodes. */
                    currentQuery.getVisitedNodesSet().add(aRandomNeighbour);
                    
                    /*
                     * Stores previous hop information.
                     *
                     * As the implementation of "currentNodesPrevHopMap"
                     * differs for each search type, the following search
                     * uses the following:
                     *
                     * Only the key "-1" is used to loop up the list of node IDs
                     * that make up the path from the current node right back to the
                     * source. The reason for using "-1" is to not confuse the
                     * implementation that flooding uses, which uses current node IDs
                     * to look up previous hop information. Newly visited nodes are
                     * added to the front of this list.
                     */
                    prevHopArrayList =
                            currentQuery.getCurrentNodesPrevHopMap().get(-1);
                    prevHopArrayList.add(0, aCurrentNode.getNodeID());
                    currentQuery.getCurrentNodesPrevHopMap().put(-1,
                            prevHopArrayList);
                    
                    /* Decrements TTL. */
                    ttlMap.put(iQuery, ttlMap.get(iQuery) - 1);
                    
                    /*
                     * Increments the total number of messages send since the
                     * beginning of the search.
                     */
                    nTotalMessages++;
                }
                
                /* Updates the query after it has made its first hop. */
                if (currentQuery.isFirstHop()) {
                    currentQuery.setFirstHop(false);
                }
            }
            
            /*
             * Updates the current node sets with the temporary sets created
             * locally.
             */
            currentQuery.setCurrentNodesSet(currentNodesSetTemp);
        }
        
        /*
         * Increments the number of elapsed time steps since
         * the beginning of the search.
         */
        nTotalTime++;
        
        /* Updates the network panel and information display. */
        searchCoordinator.getNetworkPanel().repaint();
        searchCoordinator.getControlPanel().getInformationNumberOfTimeLabel()
                .setText(Integer.toString(nTotalTime));
        searchCoordinator.getControlPanel()
                .getInformationNumberOfMessagesLabel().setText(
                        Integer.toString(nTotalMessages));
    }
    
    /**
     * Checks to see if the terminating conditions of the search have been
     * reached.
     * 
     * @return 1 if terminating conditions have been met, 0 otherwise
     */
    @Override
    public int checkTerminatingConditions() {
        int expiredTTLQueryCounter = 0;  // keeps count of how many queries
        // have had their TTL expire
        
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            final Query currentQuery = searchQueriesMap.get(iQuery);
            
            /* Checks if any of the current nodes belong to the set of target nodes. */
            final Iterator<Node> itCurrentNodes =
                    currentQuery.getCurrentNodesSet().iterator();
            Node aCurrentNode;
            
            while (itCurrentNodes.hasNext()) {
                aCurrentNode = itCurrentNodes.next();
                
                if (currentQuery.getTargetNodesSet().contains(aCurrentNode)) {
                    searchCoordinator.getControlPanel().getResultLabel()
                            .setText("SUCCESS! Found a target node.");
                    
                    searchResult = 1;
                    
                    return 1;
                }
            }
            
            /* Checks if TTL has been reached for each query. */
            if (ttlMap.get(iQuery) == 0) {
                expiredTTLQueryCounter++;
            }
        }
        
        /* Ends the search if the TTL for all the queries has expired. */
        if (expiredTTLQueryCounter == searchQueriesMap.size()) {
            searchCoordinator.getControlPanel().getResultLabel().setText(
                    "FAILURE. TTL expired.");
            
            return 1;
        }
        
        return 0;
    }
    
    /**
     * Getter for ttlMap.
     * 
     * @return the ttlMap
     */
    public HashMap<Integer, Integer> getTtlMap() {
        return ttlMap;
    }
}
