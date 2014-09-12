package bidirectionalSearchSimulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Search is an abstract class that models a general blind (i.e. uninformed)
 * search scheme. This type of search hops from node to node until a target is
 * found or terminating conditions have been reached.
 * 
 * By default, source and target nodes are chosen at random. It is entirely
 * possible that the source and target(s) are not in connected parts of the
 * network. This is no different than no targets existing and puts into the
 * spotlight the importance of having good search termination conditions.
 */
public abstract class Search {
    
    protected SearchCoordinator searchCoordinator;
    protected NetworkStructurer networkStructurer;
    protected int nQueries;  // default number of queries used by this search
    protected HashMap<Integer, Query> searchQueriesMap;  // search queries
    // belonging to this search, indexed by search query ID
    protected int nTotalMessages = 0; // total number messages (i.e. queries) passed during the search
    protected int nTotalTime = 0;     // total number of time steps required for the search to end
    protected int searchResult = 0;  // result of the search: a failure has a value of 0 while a
    
    // success has a value of 1
    
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
    public Search(final SearchCoordinator pSearchCoordinator,
            final NetworkStructurer pNetworkStructurer, final int pnQueries) {
        searchCoordinator = pSearchCoordinator;
        networkStructurer = pNetworkStructurer;
        nQueries = pnQueries;
        searchQueriesMap = new HashMap<Integer, Query>();
        
        /* Generates individual search queries. */
        for (int iQuery = 0; iQuery < pnQueries; iQuery++) {
            searchQueriesMap.put(iQuery, new Query(this, iQuery));
        }
    }
    
    /**
     * Chooses source and target nodes for each search query.
     */
    public void chooseSourceAndTargets() {
        /*
         * For simplicity, I am putting only one target per search query.
         * This can be expanded at a later point to permit multiple targets.
         */
        final int nTargets = 1;
        
        for (int iQuery = 0; iQuery < searchQueriesMap.size(); iQuery++) {
            final Query currentQuery = searchQueriesMap.get(iQuery);
            
            /* Generates source. */
            int randomNodeID =
                    (int) Math.floor(Math.random() *
                            networkStructurer.getNodeList().size());
            final Node sourceNode = networkStructurer.getNodeByID(randomNodeID);
            currentQuery.setSourceNode(sourceNode);
            
            /* Actions to take if a source has no neighbours. */
            if (sourceNode.getLinksSet().size() == 0) {
                final String noNeighboursQuery2String =
                        "FAILURE. Source node for one of the queries has no neighbours.";
                searchCoordinator.getControlPanel().getResultLabel().setText(
                        "<html>" + noNeighboursQuery2String + "</html>");
                searchCoordinator.setFlagSearchComplete(1);
                
//                double retryPercentage = 0.2;  // percentage of total nodes
//                // in the network acting as the upper limit of nodes to test to
//                // see if they have neighbours
//
//                int nRetries =
//                    (int) Math.floor(searchCoordinator.getNetworkStructurer()
//                        .getNodeSet().size() *
//                        retryPercentage);
//
//                /* Retries to find a source node with neighbours. */
//                while ((nRetries > 0) && (sourceNode.getLinksSet().size() == 0)) {
//                    randomNodeID =
//                        (int) Math.floor(Math.random() *
//                            networkStructurer.getNodeSet().size());
//                    sourceNode = networkStructurer.getNodeByID(randomNodeID);
//                    nRetries--;
//                }
//
//                /* Ends search if no suitable source node could be found. */
//                if (nRetries == 0) {
//                    System.out
//                        .println("Could not find a source node with neighbours. "
//                            + "Please reconsider your network parameters.");
//                    searchCoordinator.getControlPanel().getResultLabel().setText(
//                        "Search ended: could not find a source node with neighbours. "
//                            + "Please reconsider your network parameters.");
//
//                    searchCoordinator.setFlagSearchComplete(1);
//                }
            }
            
            /* Adds source to set of current nodes. */
            currentQuery.getCurrentNodesSet().add(sourceNode);
            
            /* Adds source to set of visited nodes. */
            currentQuery.getVisitedNodesSet().add(sourceNode);
            
            /* Generates targets. Does not allow target and source to be the same. */
            for (int iTarget = 0; iTarget < nTargets; iTarget++) {
                Node targetNode;
                do {
                    randomNodeID =
                            (int) Math.floor(Math.random() *
                                    networkStructurer.getNodeList().size());
                } while (randomNodeID == sourceNode.getNodeID());
                
                targetNode = networkStructurer.getNodeByID(randomNodeID);
                currentQuery.getTargetNodesSet().add(targetNode);
            }
        }
    }
    
    /**
     * Calculates the number of nodes visited during the search.
     * 
     * @return the number of nodes visited during the search
     */
    public int calculateNumberOfNodesVisited() {
        Set<Node> combinedVisitedNodesSet = new HashSet<Node>();
        
        for (int iQuery = 0; iQuery < nQueries; iQuery++) {
            Query currentQuery = searchQueriesMap.get(iQuery);
            combinedVisitedNodesSet.addAll(currentQuery.getVisitedNodesSet());
        }
        
        return combinedVisitedNodesSet.size();
    }
    
    /**
     * Propagates the search queries by one step.
     */
    public abstract void propagateQueries();
    
    /**
     * Checks to see if the terminating conditions of the search have been
     * reached. Terminating conditions may include discovering a target node,
     * crossing another search if the method is bidirectional, or having another
     * terminating condition (i.e. time-to-live) be reached.
     * 
     * @return 1 if terminating conditions have been met, 0 otherwise
     */
    public abstract int checkTerminatingConditions();
    
    /**
     * Getter for nQueries.
     * 
     * @return the nQueries
     */
    public int getnQueries() {
        return nQueries;
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
     * Setter for networkStructurer.
     * 
     * @param networkStructurer
     *            the networkStructurer to set
     */
    public void setNetworkStructurer(final NetworkStructurer networkStructurer) {
        this.networkStructurer = networkStructurer;
    }
    
    /**
     * Getter for searchQueriesMap.
     * 
     * @return the searchQueriesMap
     */
    public HashMap<Integer, Query> getSearchQueriesMap() {
        return searchQueriesMap;
    }
    
    /**
     * Getter for nTotalMessages.
     * 
     * @return the nTotalMessages
     */
    public int getnTotalMessages() {
        return nTotalMessages;
    }
    
    /**
     * Getter for nTotalTime.
     * 
     * @return the nTotalTime
     */
    public int getnTotalTime() {
        return nTotalTime;
    }
    
    /**
     * Getter for searchResult.
     * 
     * @return the searchResult
     */
    public int getSearchResult() {
        return searchResult;
    }
    
}
