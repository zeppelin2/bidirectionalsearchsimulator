package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * BidirectionalSearchRW is a class that models the algorithm of searching
 * using multiple random walks. It uses two random walk queries for searching
 * the network at the same time -- one originating from the source node and the
 * other from the target. In other words, both the source and target participate
 * in the search process.
 * 
 * In a bidirectional random walk (BDRW), two RW queries work together
 * simultaneously, one starting from the first query's source node and the other
 * starting from the same query's target node. Each walker propagates its query
 * in the same manner as a regular RW. A major difference, one that has a huge
 * impact on the performance of the search, is that the search can end if either
 * of the two walkers intersect paths (i.e. one query reaches a node
 * already visited by the other query). This is in addition to each query
 * discovering the target node, which would also result in a success. A failure
 * occurs when both queries reach their TTL. The TTL refers to the limit on the
 * number of time steps or messages permitted by each walker, independent of the
 * search working in the other direction.
 * 
 * It is possible to have the random walk choose its next neighbour at random,
 * excluding the node that sent it the query, by modifying the code below. Look
 * for the do/while loop that that needs to be uncommented for this change to be
 * applied.
 */
public class BidirectionalSearchRW extends BidirectionalSearch {
    
    private static int nQueries = 2;  // number of queries to use in this search
    private final HashMap<Integer, Integer> ttlMap;  // time-to-live values for each query
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            BidirectionalSearchRW
     * 
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pttl
     *            time-to-live value for each search query
     */
    public BidirectionalSearchRW(final SearchCoordinator pSearchCoordinator,
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
                
                while (itCurrentNodes.hasNext()) {
                    final Node aCurrentNode = itCurrentNodes.next();
                    
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
                    
//                    do {
                    aRandomNeighbour = aCurrentNode.pickRandomNeighbour();
//                    } while ((!currentQuery.getCurrentNodesPrevHopMap().get(-1)
//                        .isEmpty()) &&
//                        (currentQuery.getCurrentNodesPrevHopMap().get(-1)
//                            .get(0) == aRandomNeighbour.getNodeID()) &&
//                        (aCurrentNode.degree() > 1));
                    
                    /*
                     * Forwards the query (i.e. the selected neighbour becomes a
                     * current node).
                     */
                    currentNodesSetTemp.add(aRandomNeighbour);
                    
                    /* Adds the neighbour to the list of visited nodes. */
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
        
        /*
         * This next part may seem a bit complicated, but as the queries are grouped
         * into query groups, based on their parent source (i.e. the source node
         * that first sent out the queries) and their target node sets, the queries
         * within a query group must be compared with the queries in other query
         * groups to test for intersection of the queries. Only then can we know
         * if two queries (from different query groups) have met and thus we can
         * conclude the search as being a success.
         *
         * Also, I did not update the other bidirectional searches to use this
         * methodology as I didn't have time. That's why they're different.
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
        
        /* Checks if TTL has been reached for each query. */
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
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
