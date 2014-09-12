package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * BidirectionalSearchLinear is a class that models the algorithm of
 * searching using multiple random walks that attempt to propagate in a straight
 * line.
 * 
 * In a bidirectional linear (BDL) search, two RW queries work together
 * simultaneously, one starting from the first query's source node and
 * the other starting from the same query's target node. Each query picks a
 * neighbour at random and tries to maintain a straight line, based on the
 * direction of the line connecting these two nodes. In * order to keep the
 * direction of propagation in a straight line, a restriction was added such
 * that the next neighbour chosen at each step must increase the distance of the
 * line from the source to the current node. Otherwise, a query may backtrack
 * along the same line or, worse, result in deadlock as the query hops endlessly
 * between two nodes situated along the same line. The next hop is chosen to
 * minimize the difference in direction between the line connecting the
 * source and its first hop. As such, there is some liberty in which node is
 * chosen, as the next hop in the algorithm is not fixed by direction (e.g. by
 * setting a degree limit on the largest difference in directions allowed) and
 * will instead always choose the next neighbour the results in the
 * "straightest" line while continuing to propagate the query. If a query
 * reaches the end of the network or can no longer be propagated according to
 * the above conditions, it restarts from the source node and takes a one hop
 * penalty in its TTL. The direction of this new query is random and is
 * independent of the previous query. BDL ends with a success if
 * one of its walkers finds the target node or if a walker intersects with the
 * visited path of the other query, within the predefined TTL. A failure occurs
 * if the TTL is reached for both of the two queries. The TTL refers to the
 * limit on the number of messages for each query, independent of the search
 * working in the other direction.
 * 
 */
public class BidirectionalSearchLinear extends BidirectionalSearch {
    
    private static int nQueries = 2;  // number of queries to use in this search
    private final HashMap<Integer, Integer> ttlMap;  // time-to-live values for each query
    private final HashMap<Integer, double[]> sourceFirstHopDirectionMap; // direction
    // of the vector connecting the source node to its first hop for each query
    private final HashMap<Integer, Integer> flagWalkerDeadlockMap;  // signals deadlock
    
    // by a random walker for a given query
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            BidirectionalSearchLinear
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pttl
     *            time-to-live value for each search query
     * 
     */
    public BidirectionalSearchLinear(
            final SearchCoordinator pSearchCoordinator,
            final NetworkStructurer pNetworkStructurer, final int pttl) {
        super(pSearchCoordinator, pNetworkStructurer, nQueries);
        
        /*
         * Initializes the query groups and adds query 0 to query group 0 and
         * query 1 to query group 1.
         */
        queryGroupsMap = new HashMap<Integer, Set<Query>>();
        
        HashSet<Query> queriesToAddSet = new HashSet<Query>();
        queriesToAddSet.add(searchQueriesMap.get(0));
        queryGroupsMap.put(0, queriesToAddSet);
        
        queriesToAddSet = new HashSet<Query>();
        queriesToAddSet.add(searchQueriesMap.get(1));
        queryGroupsMap.put(1, queriesToAddSet);
        
        ttlMap = new HashMap<Integer, Integer>();
        ttlMap.put(0, pttl);
        ttlMap.put(1, pttl);
        
        sourceFirstHopDirectionMap = new HashMap<Integer, double[]>();
        
        /* Sets the walker deadlock flags */
        flagWalkerDeadlockMap = new HashMap<Integer, Integer>();
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            flagWalkerDeadlockMap.put(iQuery, 0);
        }
    }
    
    /**
     * Chooses source and target nodes for each search query.
     */
    @Override
    public void chooseSourceAndTargets() {
        /*
         * Chooses the source and target for the first query.
         */
        final Query query1 = searchQueriesMap.get(0);
        
        /* Generates source. */
        int randomNodeID =
                (int) Math.floor(Math.random() *
                        networkStructurer.getNodeList().size());
        final Node sourceNode = networkStructurer.getNodeByID(randomNodeID);
        query1.setSourceNode(sourceNode);
        
        /* Checks to see if source has no neighbours. */
        if (sourceNode.getLinksSet().size() == 0) {
            final String noNeighboursQuery1String =
                    "FAILURE. Source node for one of the queries has no neighbours.";
            searchCoordinator.getControlPanel().getResultLabel().setText(
                    "<html>" + noNeighboursQuery1String + "</html>");
            searchCoordinator.setFlagSearchComplete(1);
        }
        
        /* Adds source to set of current nodes. */
        query1.getCurrentNodesSet().add(sourceNode);
        
        /* Adds source to set of visited nodes. */
        query1.getVisitedNodesSet().add(sourceNode);
        
        /* Generates target. Does not allow target and source to be the same. */
        Node targetNode;
        do {
            randomNodeID =
                    (int) Math.floor(Math.random() *
                            networkStructurer.getNodeList().size());
        } while (randomNodeID == sourceNode.getNodeID());
        
        targetNode = networkStructurer.getNodeByID(randomNodeID);
        query1.getTargetNodesSet().add(targetNode);
        
        /*
         * Chooses the source and target for the second query.
         *
         * The target of the first query becomes the source for the second,
         * while the source of the first query becomes the target for the second
         * (hence the reason for using "targetNode" in place of where one might
         * normally expect to see "sourceNode."
         */
        final Query query2 = searchQueriesMap.get(1);
        
        /* Generates source. */
        query2.setSourceNode(targetNode);
        
        /* Checks to see if source has no neighbours. */
        if (targetNode.getLinksSet().size() == 0) {
            final String noNeighboursQuery2String =
                    "FAILURE. Source node for one of the queries has no neighbours.";
            searchCoordinator.getControlPanel().getResultLabel().setText(
                    "<html>" + noNeighboursQuery2String + "</html>");
            searchCoordinator.setFlagSearchComplete(1);
        }
        
        /* Adds source to set of current nodes. */
        query2.getCurrentNodesSet().add(targetNode);
        
        /* Adds source to set of visited nodes. */
        query2.getVisitedNodesSet().add(targetNode);
        
        /* Generates target. */
        query2.getTargetNodesSet().add(sourceNode);
    }
    
    /**
     * Propagates the search queries by one step.
     */
    @Override
    public void propagateQueries() {
        Set<Node> currentNodesSetTemp; // stores the new set of current nodes
        // while the "old" set of current nodes are being added to the
        // set of visited nodes.
        
        Node aCurrentNode;  // the current node being processed for a given query
        Node querySourceNode;  // the source node for a given query
        Node aNeighbour;  // a neighbour of the current node for a given query to
        // which the query will be forwarded to
        double directionOfSourceAndCurrentNeighbour;  //  the diretion of the line
        // connecting the source node with the current neighbour
        double differenceBetweenDirections;  // the absolute value of the
        // difference between the direction of the line connecting the source node
        // with the current neighbour and the line connecting the source node and
        // its first hop
        double smallestDifferenceBetweenDirections; // the smallest direction
        // of the line connecting the source node and the current neighbour
        Node theNeighbourWithSmallestDifference;  // the neighbour having the smallest
        // difference in directions between itself and the source compared with the
        // source and the first hop
        
        /* Does the following for each search query. */
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            final Query currentQuery = searchQueriesMap.get(iQuery);
            
            currentNodesSetTemp = new HashSet<Node>();  // stores the set of
            // nodes that receive the query during this propagation step and
            // will become the current nodes for the next propagation step
            
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
                
                querySourceNode = searchQueriesMap.get(iQuery).getSourceNode();
                
                while (itCurrentNodes.hasNext()) {
                    aCurrentNode = itCurrentNodes.next();
                    
                    aNeighbour = null;  // reinitializes this to null, otherwise it
                    // could keep the value from a previous query
                    
                    /* Chooses a random neighbour for the first hop of the search
                     * and stores the direction of the link connecting the source
                     * and this neighbour.
                     */
                    if (currentQuery.isFirstHop()) {
                        aNeighbour = querySourceNode.pickRandomNeighbour();
                        
                        directionOfSourceAndCurrentNeighbour =
                                networkStructurer.calculateDirection(
                                        querySourceNode, aNeighbour);
                        
                        sourceFirstHopDirectionMap
                                .put(iQuery,
                                        new double[] {directionOfSourceAndCurrentNeighbour});
                        
                        currentQuery.setFirstHop(false);
                        
                    }
                    /*
                     * Chooses the neighbour which make the line connecting the source
                     * with this neighbour have the closest direction to the line
                     * connecting the source and its first hop.
                     */
                    else {
                        /*
                         * Iterates over the links of the current node, used to find
                         * all of the node's neighbours.
                         */
                        final Iterator<Link> itLinksSet =
                                aCurrentNode.getLinksSet().iterator();
                        Link aLink;
                        
                        smallestDifferenceBetweenDirections = 400;
                        theNeighbourWithSmallestDifference = null;  // reinitializes
                        // this to null, otherwise it could keep the value from
                        // a previous query
                        
                        while (itLinksSet.hasNext()) {
                            aLink = itLinksSet.next();
                            
                            /* Gets the neighbour connected by the link. */
                            aNeighbour = aLink.connectedBy(aCurrentNode);
                            
                            /*
                             * Calculates direction of the line connecting the source and
                             * this neighbour.
                             */
                            directionOfSourceAndCurrentNeighbour =
                                    networkStructurer.calculateDirection(
                                            querySourceNode, aNeighbour);
                            
                            /*
                             * Calculates the difference in directions of the line connecting
                             * the source and the neighbour with the line connecting the
                             * source and its first hop.
                             */
                            differenceBetweenDirections =
                                    Math.abs(directionOfSourceAndCurrentNeighbour -
                                            sourceFirstHopDirectionMap
                                                    .get(iQuery)[0]);
                            
                            /* Checks to see if the difference in directions between this
                             * neighbour and the source is smaller than all other previous
                             * neighbours.
                             *
                             * Also checks that the source is not the current neighbour
                             * as it will result in a direction of zero (which can skew
                             * the results for the differences). We wish to be moving
                             * away from the source node.
                             *
                             * Finally, it checks that the distance from the source
                             * node increases by choosing this neighbour, so as to
                             * avoid backtracking closer to the source node.
                             */
                            if ((differenceBetweenDirections < smallestDifferenceBetweenDirections) &&
                                    (aNeighbour.getNodeID() != querySourceNode
                                            .getNodeID()) &&
                                    (networkStructurer
                                            .calculateDistanceBetweenNodes(
                                                    querySourceNode, aNeighbour) > networkStructurer
                                            .calculateDistanceBetweenNodes(
                                                    querySourceNode,
                                                    aCurrentNode))) {
                                smallestDifferenceBetweenDirections =
                                        differenceBetweenDirections;
                                theNeighbourWithSmallestDifference = aNeighbour;
                            }
                        }
                        
                        /* Checks that a new neighbour was chosen to propagate the
                         * query to (after all the neighbours have been considered).
                         */
                        if (theNeighbourWithSmallestDifference != null) {
                            aNeighbour = theNeighbourWithSmallestDifference;
                        }
                        /* If no new neighbour was chosen, instead of countlessly
                         * resending the query to the same node, the query kills
                         * itself and raises a flag.
                         *
                         * I chose to simulate sending a message back to the source
                         * to have the terminating conditions pick up on the raised
                         * flag (notifying of the deadlock).
                         */
                        else {
                            flagWalkerDeadlockMap.put(iQuery, 1);
                        }
                    }
                    
                    /*
                     * Only propagates the query if the walker deadlock flag has not
                     * been raised.
                     */
                    if (flagWalkerDeadlockMap.get(iQuery) == 0) {
                        /*
                         * Forwards the query (i.e. the selected neighbour becomes a
                         * current node).
                         */
                        currentNodesSetTemp.add(aNeighbour);
                        
                        /* Adds the neighbour to the list of visited nodes. */
                        currentQuery.getVisitedNodesSet().add(aNeighbour);
                        
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
                                currentQuery.getCurrentNodesPrevHopMap()
                                        .get(-1);
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
        int flagWalkerDeadlockCounter = 0;  // keeps count of how many queries
        // are in deadlock
        int flagResendQueryUponDeadlock = 1;  // resends a search query from the
        // source if one ends in deadlock
        int expiredTTLQueryCounter = 0;  // keeps count of how many queries
        // have had their TTL expire
        
        /*
         * This next part may seem a bit complicated, but as the queries are grouped
         * into query groups, based on their parent source (i.e. the source node
         * that first sent out the queries) and their target node sets, the queries
         * within a query group must be compared with the queries in other query
         * groups to test for intersection of the queries. Only then can we know
         * if two queries (from different query groups) have met and thus we can
         * conclude the search as being a success.
         */
        
        /* Iterates over the set of query groups. */
        for (int iQueryGroup = 0; iQueryGroup < queryGroupsMap.size(); iQueryGroup++) {
            final Set<Query> currentQuerySet = queryGroupsMap.get(iQueryGroup);
            
            /* Iterates over the set of queries within the current query group. */
            final Iterator<Query> itCurrentQueries = currentQuerySet.iterator();
            Query aCurrentQuery;
            
            while (itCurrentQueries.hasNext()) {
                aCurrentQuery = itCurrentQueries.next();
                
                /*
                 * Checks if any of the current query's set of
                 * current nodes are shared amongst the set of this
                 * other query's target nodes (i.e. the current
                 * query found a target).
                 */
                if (SetOperations.intersection(
                        aCurrentQuery.getCurrentNodesSet(),
                        aCurrentQuery.getTargetNodesSet()).size() != 0) {
                    searchCoordinator.getControlPanel().getResultLabel()
                            .setText("SUCCESS! Found a target node.");
                    
                    searchResult = 1;
                    
                    return 1;
                }
                
                /* Iterates over the set of other query groups. */
                for (int iAnotherQueryGroup = 0; iAnotherQueryGroup < queryGroupsMap
                        .size(); iAnotherQueryGroup++) {
                    /*
                     * Checks that the other query group being considered is not
                     * the current query group.
                     */
                    if (iAnotherQueryGroup != iQueryGroup) {
                        final Set<Query> anotherQuerySet =
                                queryGroupsMap.get(iAnotherQueryGroup);
                        
                        /*
                         * Iterates over the set of queries within the other
                         * query group.
                         */
                        final Iterator<Query> itOtherQueries =
                                anotherQuerySet.iterator();
                        Query anotherQuery;
                        
                        while (itOtherQueries.hasNext()) {
                            anotherQuery = itOtherQueries.next();
                            
                            /*
                             * Checks if any of the current query's set of
                             * current nodes are shared amongst the set of this
                             * other query's visited nodes (i.e. the current
                             * query intersected with another query).
                             */
                            if (SetOperations.intersection(
                                    aCurrentQuery.getCurrentNodesSet(),
                                    anotherQuery.getVisitedNodesSet()).size() != 0) {
                                searchCoordinator
                                        .getControlPanel()
                                        .getResultLabel()
                                        .setText(
                                                "SUCCESS! The two queries have met.");
                                
                                searchResult = 1;
                                
                                return 1;
                            }
                        }
                    }
                }
            }
        }
        
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            /* Checks if TTL has been reached for each query. */
            if (ttlMap.get(iQuery) == 0) {
                expiredTTLQueryCounter++;
            }
            
            /*
             * Checks if a flag has been raised to signal that a walker is
             * in deadlock.
             */
            if (flagWalkerDeadlockMap.get(iQuery) == 1) {
                /*
                * Checks to see if the source should resend a search query if the
                * previous one ended in deadlock. The new query remembers the
                * progress of the old query.
                */
                if (flagResendQueryUponDeadlock == 1) {
                    // I suppose I could have just set the set of current nodes
                    // to include just the source and continue propagating the
                    // query, but this seemed more proper (albeit more involved)
                    
                    final Query oldQuery = searchQueriesMap.get(iQuery);
                    
                    /* Creates new query. */
                    final int newQueryNumber = searchQueriesMap.size();
                    final Query newQuery = new Query(this, newQueryNumber);
                    
                    /* Copies source node from old query. */
                    final Node oldQuerySourceNode = oldQuery.getSourceNode();
                    newQuery.setSourceNode(oldQuerySourceNode);
                    
                    /* Adds source node to set of current nodes. */
                    newQuery.getCurrentNodesSet().add(oldQuerySourceNode);
                    
                    /* Copies targets from old query. */
                    final Set<Node> oldQueryTargetNodesSet =
                            oldQuery.getTargetNodesSet();
                    newQuery.setTargetNodesSet(oldQueryTargetNodesSet);
                    
                    /* Copies previous hop information from old query. */
                    final HashMap<Integer, ArrayList<Integer>> oldQueryCurrentNodesPrevHopMap =
                            oldQuery.getCurrentNodesPrevHopMap();
                    newQuery.setCurrentNodesPrevHopMap(oldQueryCurrentNodesPrevHopMap);
                    
                    /* Updates TTL of new query (while giving it a 1 TTL penalty). */
                    final int oldQueryttl = ttlMap.get(iQuery);
                    ttlMap.put(newQueryNumber, oldQueryttl - 1);
                    
                    /* Updates the TTL of the old query. */
                    ttlMap.put(iQuery, 0);
                    
                    /* Updates deadlock flag of old and new queries. */
                    flagWalkerDeadlockMap.put(iQuery, 0);
                    flagWalkerDeadlockMap.put(newQueryNumber, 0);
                    
                    /* Adds the query to the map of search queries. */
                    searchQueriesMap.put(newQueryNumber, newQuery);
                    
                    /* Adds the query to the map of query groups. */
                    final int oldQueryGroupID = getQueryGroupID(oldQuery);
                    final Set<Query> oldQuerySet =
                            queryGroupsMap.get(oldQueryGroupID);
                    oldQuerySet.add(newQuery);
                    queryGroupsMap.put(oldQueryGroupID, oldQuerySet);
                }
                /*
                 * Raises the query deadlock counter to allow for terminating
                 * the search if all queries reached deadlock.
                 */
                else {
                    flagWalkerDeadlockCounter++;
                }
            }
        }
        
        /* If all queries have been killed due to deadlock, ends the search. */
        if (flagWalkerDeadlockCounter == flagWalkerDeadlockMap.size()) {
            searchCoordinator.getControlPanel().getResultLabel().setText(
                    "FAILURE. All queries ended in deadlock.");
            
            return 1;
        }
        
        /* If all queries have had their TTL expire, ends the search. */
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
