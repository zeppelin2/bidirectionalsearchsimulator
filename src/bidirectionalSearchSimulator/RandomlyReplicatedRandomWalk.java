package bidirectionalSearchSimulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * RandomlyReplicatedRandomWalk is a class that models the algorithm of
 * searching using randomly replicated random walkers.
 * 
 * In randomly replicated random walks (RRRW), a single random walker starts
 * from the source and chooses a random neighbour. At this point, the walker
 * must decide if it replicates itself or not. This decision is based on two
 * factors: the initial replication probability and the replication policy
 * function. The initial replication probability is simply a probability that is
 * used by the replication policy to calculate the final replication
 * probability. To better illustrate their use, I used an initial replication
 * probability of 0.1 with a replication strategy that exponentially decreased
 * this probability as the number of previous replications increased. This was
 * shown in [5] to be a good choice for allowing the walkers to explore at
 * reasonable depths of the network without depleting their budget early on. At
 * each replication, I chose one walker to be generated. The TTL of the parent
 * walker was split evenly between all walkers generated during the replication
 * (i.e in my case, the TTL got split in half). RRRW ends with a success if one
 * of its walkers finds the target node within the predefined TTL, or
 * with a failure if the TTL is reached for each of the walkers. The TTL for
 * RRRW refers to the limit on the combined number of messages sent by all the
 * walkers.
 * 
 * NOTE: This class was added last minute and is essentially a copy of the class
 * BidirectionalSearchHybrid. I also copied over the concept of "query groups"
 * so that this class would work without modification. It's messy but should do
 * the trick to obtain simulations for a single RRRW method.
 * 
 */
public class RandomlyReplicatedRandomWalk extends Search {
    
    private static int nQueries = 1;  // number of queries to use in this search
    private static int nChildren = 1;  // number of children to produce at each
    // replication
    private final HashMap<Integer, Double> firstReplicationProbabilityMap;  // first
    // replication probability values for each query group
    private final HashMap<Integer, Integer> nTotalReplicationsMap;  // total number
    // of replications that have occurred since the first walker was sent from
    // the source node, for each query group
    private final HashMap<Integer, Integer> ttlMap;  // time-to-live values for each
    // query
    private final HashMap<Integer, Set<Query>> queryGroupsMap;  // groups queries
    
    // based on integer values, with each group containing the set of queries belonging
    // to that group. In a bidirectional search such as this one, there would be two
    // "query groups," each group defined by their set of targets. E.g. query group 0
    // would contain query 0 and query group 1 would contain query 1. Adding new
    // queries to the search need to be added to a query group in order for their
    // terminating conditions to be properly measured. E.g. If query 0 ends in deadlock
    // and a new query is released as a successor to query 0, this query would need
    // to be added to the query group that contained query 0, namely query group 0.
    // All queries in the same group can be traced back to the same source node (it
    // is possible that a query may replicate along the way, but the newly replicated
    // query would still belong to the same query group).
    
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
    public RandomlyReplicatedRandomWalk(
            final SearchCoordinator pSearchCoordinator,
            final NetworkStructurer pNetworkStructurer, final int pttl) {
        super(pSearchCoordinator, pNetworkStructurer, nQueries);
        
        /*
         * Initializes the query groups and adds query 0 to query group 0 and
         * query 1 to query group 1.
         */
        queryGroupsMap = new HashMap<Integer, Set<Query>>();
        
        final HashSet<Query> queriesToAddSet = new HashSet<Query>();
        queriesToAddSet.add(searchQueriesMap.get(0));
        queryGroupsMap.put(0, queriesToAddSet);
        
        firstReplicationProbabilityMap = new HashMap<Integer, Double>();
        firstReplicationProbabilityMap.put(0, 0.1);
        
        nTotalReplicationsMap = new HashMap<Integer, Integer>();
        nTotalReplicationsMap.put(0, 0);
        
        ttlMap = new HashMap<Integer, Integer>();
        ttlMap.put(0, pttl);
    }
    
    /**
     * Propagates the search queries by one step. After the query has been
     * propagated, the query decides if it must replicate itself or not.
     */
    @Override
    public void propagateQueries() {
        Set<Node> currentNodesSetTemp; // stores the new set of current nodes
        // while the "old" set of current nodes are being added to the
        // set of visited nodes.
        
        final int nQueriesBeforeAnyReplicationOccurs = searchQueriesMap.size();
        
        /* Does the following for each search query. */
        for (int iQuery = 0; iQuery < nQueriesBeforeAnyReplicationOccurs; iQuery++) {
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
                     * Picks a random neighbour of the current node, excluding
                     * the node that sent the query. First, however, it checks
                     * that at least one hop has been made by the search, in
                     * order to avoid the situation where the source (which has
                     * no previous hop) tries to access its previous hop 
                     * information.
                     *
                     * An exception is made if the degree of the current node is one,
                     * in which case it is allowed to send the query back to the
                     * previous hop so as to avoid deadlock.
                     */
                    Node aRandomNeighbour;
                    
                    do {
                        aRandomNeighbour = aCurrentNode.pickRandomNeighbour();
                    } while ((!currentQuery.getCurrentNodesPrevHopMap().get(-1)
                            .isEmpty()) &&
                            (currentQuery.getCurrentNodesPrevHopMap().get(-1)
                                    .get(0) == aRandomNeighbour.getNodeID()) &&
                            (aCurrentNode.degree() > 1));
                    
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
                    
                    /*
                     * Checks to see if the query should replicate itself.
                     */
                    if (Math.random() < calculatesReplicationProbability(currentQuery)) {
                        replicateQuery(currentQuery, aRandomNeighbour);
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
     * Replicates the current query.
     * 
     * @param aQuery
     *            the query to replicate
     * @param aNode
     *            the node at which the replication is to occur
     */
    public void replicateQuery(final Query aQuery, final Node aNode) {
        for (int iChild = 0; iChild < nChildren; iChild++) {
            /* Creates the child query. */
            final int childQueryNumber = searchQueriesMap.size();
            final Query childQuery = new Query(this, childQueryNumber);
            
            /* Makes the child's source node the current node of the parent query. */
            childQuery.setSourceNode(aNode);
            
            /* Adds parent's current node to child's set of current nodes. */
            childQuery.getCurrentNodesSet().add(aNode);
            
            /* Copies targets from parent query. */
            final Set<Node> oldQueryTargetNodesSet = aQuery.getTargetNodesSet();
            childQuery.setTargetNodesSet(oldQueryTargetNodesSet);
            
            /* Copies visited nodes from parent query. */
            final Set<Node> oldQueryVisitedNodesSet =
                    aQuery.getVisitedNodesSet();
            childQuery.setVisitedNodesSet(oldQueryVisitedNodesSet);
            
            /* Copies previous hop information from parent query. */
            final HashMap<Integer, ArrayList<Integer>> parentQueryCurrentNodesPrevHopMap =
                    aQuery.getCurrentNodesPrevHopMap();
            childQuery
                    .setCurrentNodesPrevHopMap(parentQueryCurrentNodesPrevHopMap);
            
            /* Updates TTL of both the parent and child query. */
            final int parentQueryttl = ttlMap.get(aQuery.getQueryID());
            ttlMap.put(aQuery.getQueryID(), (int) Math
                    .ceil((double) parentQueryttl / 2));
            ttlMap.put(childQueryNumber, (int) Math
                    .floor((double) parentQueryttl / 2));
            
            /* Adds the child query to the map of search queries. */
            searchQueriesMap.put(childQueryNumber, childQuery);
            
            /*
             * Adds the child query to the map of query groups under
             * the same query group as the parent.
             */
            final int parentQueryGroupID = getQueryGroupID(aQuery);
            final Set<Query> oldQuerySet =
                    queryGroupsMap.get(parentQueryGroupID);
            oldQuerySet.add(childQuery);
            queryGroupsMap.put(parentQueryGroupID, oldQuerySet);
        }
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
     * Calculates the replication probability for the provided query. This
     * probability decreases exponentially with the total number of replications
     * done by the current query group.
     * 
     * @param aQuery
     *            the query to calculate the replication for
     * @return the replication probability for the provided query
     */
    public double calculatesReplicationProbability(final Query aQuery) {
        final int queryGroup = getQueryGroupID(aQuery);
        final double initialReplicationProbability =
                firstReplicationProbabilityMap.get(queryGroup);
        final int nReplicationsThusFar = nTotalReplicationsMap.get(queryGroup);
        
        return Math.pow(initialReplicationProbability, Math.pow(2,
                nReplicationsThusFar));
    }
    
    /**
     * Gets the query group ID of which a query belongs to.
     * 
     * @param aQuery
     *            the query whose group is requested
     * @return the query group ID of the query or -1 if the query does not have
     *         a group
     */
    public int getQueryGroupID(final Query aQuery) {
        /* Iterates over the set of query groups. */
        for (int iQueryGroup = 0; iQueryGroup < queryGroupsMap.size(); iQueryGroup++) {
            final Set<Query> currentQuerySet = queryGroupsMap.get(iQueryGroup);
            
            if (currentQuerySet.contains(aQuery)) {
                return iQueryGroup;
            }
        }
        
        return -1;
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
