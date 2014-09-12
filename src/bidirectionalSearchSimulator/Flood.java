package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Flood is a class that models the flooding search algorithm.
 * 
 * In flooding, the query propagation phase sends the search query
 * to all of the current node's neighbours, except for the neighbour that
 * sent it the query. If a node receives the same query multiple times, it
 * neither responds nor propagates the query. The flood ends with a success if
 * it finds the target node within the predefined TTL, or a failure if the TTL
 * is reached. The TTL for a flood refers to the limit on the number of time
 * steps permitted.
 */
public class Flood extends Search {
    
    private static int nQueries = 1;  // number of queries to use in this search
    private HashMap<Integer, Integer> ttlMap;  // time-to-live values for each query
    private HashMap<Integer, Integer> hasPropagatedQuery;  // values indicating
    
    // whether or not a node has participated in the flood by propagating the
    // search query. The node ID is used to lookup the value; a value of 1 indicates 
    // that the node has already propagated the search query
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            Flood
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pttl
     *            time-to-live value for each search query
     */
    public Flood(final SearchCoordinator pSearchCoordinator,
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
        
        hasPropagatedQuery = new HashMap<Integer, Integer>();
    }
    
    /**
     * Propagates the search queries by one step.
     */
    @Override
    public void propagateQueries() {
        Set<Node> currentNodesSetTemp; // stores the new set of current nodes
        // while the "old" set of current nodes are being added to the
        // set of visited nodes
        
        Set<Node> visitedNodesSetTemp; // stores the set of current nodes
        // that become "visited" once they propagate the query to all their
        // neighbours
        
        /* Does the following for each search query. */
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            final Query currentQuery = searchQueriesMap.get(iQuery);
            
            currentNodesSetTemp = new HashSet<Node>();  // uses a temporary
            // set as changing a set while iterating over it can cause
            // problems
            visitedNodesSetTemp = new HashSet<Node>();  // uses a temporary
            // set so that all the neighbours can be marked as visited only 
            // after one time step has passed, instead of incrementally within
            // one time step, which would cause problems
            
            final HashMap<Integer, ArrayList<Integer>> currentNodesPrevHopMapTemp =
                    new HashMap<Integer, ArrayList<Integer>>();
            
            ArrayList<Integer> prevHopArrayListOfNeighbour;
            
            /* Iterates over each node in the set of current nodes. */
            final Iterator<Node> itCurrentNodes =
                    currentQuery.getCurrentNodesSet().iterator();
            Node aCurrentNode;
            
            while (itCurrentNodes.hasNext()) {
                aCurrentNode = itCurrentNodes.next();
                
                final ArrayList<Integer> prevHopArrayListOfCurrentNode =
                        currentQuery.getCurrentNodesPrevHopMap().get(
                                aCurrentNode.getNodeID());
                
                /* 
                 * Checks that the current node has not already propagated 
                 * the query.
                 */
                if (!hasPropagatedQuery.containsKey(aCurrentNode.getNodeID())) {
                    /*
                     * Iterates over the links of the current node to find
                     * all of the node's neighbours.
                     */
                    final Iterator<Link> itLinksSet =
                            aCurrentNode.getLinksSet().iterator();
                    Link aLink;
                    
                    while (itLinksSet.hasNext()) {
                        aLink = itLinksSet.next();
                        final Node aNeighbour = aLink.connectedBy(aCurrentNode);
                        
                        /*
                         * The reason for checking if the query is doing its
                         * first hop is that the source node will not have
                         * any previous hops and thus returns a "null" value
                         * for "prevHopArrayListOfCurrentNode."
                         *
                         * Also checks that the current neighbour has not been
                         * previously visited. This covers both the case where the
                         * neighbour was the node that sent the query during the
                         * previous hop, as well as the case where the neighbour was
                         * visited by the query during any of the other hops. The
                         * latter is the equivalent of marking a node as visited
                         * only once it sends the query and then only having
                         * the neighbour propagate the query once it has verified
                         * that it had not previously received the same query.
                         */
                        if ((currentQuery.isFirstHop()) ||
                                (!prevHopArrayListOfCurrentNode
                                        .contains(aNeighbour.getNodeID()))) {
                            /*
                             * Forwards the query (i.e. the selected neighbour
                             * becomes a current node).
                             */
                            currentNodesSetTemp.add(aNeighbour);
                            
                            /* Adds the current neighbour to the set of visited nodes. */
                            visitedNodesSetTemp.add(aNeighbour);
                            
                            /* Marks the current node as having propagated the query. */
                            hasPropagatedQuery.put(aCurrentNode.getNodeID(), 1);
                            
                            /*
                             * Stores previous hop information.
                             *
                             * As the implementation of "currentNodesPrevHopMap"
                             * differs for each search type, the following search
                             * uses the following:
                             *
                             * The node ID of the current node can be used to
                             * loop up the list of node IDs that make up the last
                             * hop of this current node.
                             *
                             * As a node can receive the same query multiple
                             * times in the same time step, it is possible
                             * there to be multiple "last hops" associated with
                             * a given node.
                             */
                            if (currentNodesPrevHopMapTemp
                                    .containsKey(aNeighbour.getNodeID())) {
                                prevHopArrayListOfNeighbour =
                                        currentNodesPrevHopMapTemp
                                                .get(aNeighbour.getNodeID());
                            }
                            else {
                                prevHopArrayListOfNeighbour =
                                        new ArrayList<Integer>();
                            }
                            
                            prevHopArrayListOfNeighbour.add(aCurrentNode
                                    .getNodeID());
                            currentNodesPrevHopMapTemp.put(aNeighbour
                                    .getNodeID(), prevHopArrayListOfNeighbour);
                            
                            /*
                             * Increments the total number of messages send since the
                             * beginning of the search.
                             */
                            nTotalMessages++;
                        }
                    }
                }
            }
            
            /*
             * Updates the query's structures with the temporary ones created
             * locally.
             */
            currentQuery.setCurrentNodesSet(currentNodesSetTemp);
            currentQuery.getVisitedNodesSet().addAll(visitedNodesSetTemp);
            currentQuery.getCurrentNodesPrevHopMap().putAll(
                    currentNodesPrevHopMapTemp);
            
            /*  Decrements TTL. */
            ttlMap.put(iQuery, ttlMap.get(iQuery) - 1);
            
            /* Updates the query after it has made its first hop. */
            if (currentQuery.isFirstHop()) {
                currentQuery.setFirstHop(false);
            }
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
