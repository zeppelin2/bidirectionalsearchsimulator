package bidirectionalSearchSimulator;

import java.util.HashMap;
import java.util.Set;

/**
 * BidirectionalSearch is an abstract class that models the general structure
 * of a network search algorithm.
 */
public abstract class BidirectionalSearch extends Search {
    
    protected HashMap<Integer, Set<Query>> queryGroupsMap;  // groups queries
    
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
     *            search coordinator that coordinates this search
     * @param pNetworkStructurer
     *            network structurer containing node information used by this
     *            search
     * @param pnQueries
     *            number of queries used in this search
     */
    public BidirectionalSearch(final SearchCoordinator pSearchCoordinator,
            final NetworkStructurer pNetworkStructurer, final int pnQueries) {
        super(pSearchCoordinator, pNetworkStructurer, pnQueries);
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
     * Getter for queryGroupsMap.
     * 
     * @return the queryGroupsMap
     */
    public HashMap<Integer, Set<Query>> getQueryGroupsMap() {
        return queryGroupsMap;
    }
    
    /**
     * Setter for queryGroupsMap.
     * 
     * @param queryGroupsMap
     *            the queryGroupsMap to set
     */
    public void setQueryGroupsMap(
            final HashMap<Integer, Set<Query>> queryGroupsMap) {
        this.queryGroupsMap = queryGroupsMap;
    }
}
