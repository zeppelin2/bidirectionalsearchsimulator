package bidirectionalSearchSimulator;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

/**
 * SearchCoordinator is a class that coordinates the bidirectional
 * search simulator. It reacts to events and delegates tasks to others.
 */
public class SearchCoordinator {
    
    private NetworkStructurer networkStructurer;
    private Network network;
    private Search search;
    private final NetworkPanel networkPanel;
    private final ControlPanel controlPanel;
    
    private String networkSettingsFilename = "networkInformation.ser";
    private String searchConditionsFilename = "searchConditions.ser";
    
    /*
     * This is a flag to prevent the "paintComponent" method in NetworkPanel from
     * drawing the network before it has been initialized. Once someone click the
     * "Start simulation" button in the control panel GUI, this flag switches
     * values and allows for the network to be drawn to the screen.
     *
     * A value of 0 means that it is not OK to draw the network, while a value of
     * 1 means that the network is ready to be drawn.
     */
    private int flagOKToDrawNetwork = 0;
    
    /*
     * This is a flag to signal that the search has finished.
     *
     * A value of 0 means that the search is not yet complete, while a value of
     * 1 means that the search has completed.
     */
    private int flagSearchComplete = 0;
    
    /**
     * Constructor.
     * 
     */
    public SearchCoordinator() {
        controlPanel = new ControlPanel(this);
        networkPanel = new NetworkPanel(this);
    }
    
    /**
     * Initializes a new network and new search strategy.
     */
    public void initializeNetworkAndSearch() {
        /* Gets start time for the timer. */
//        final long startTime = System.nanoTime();
        
        generateNetwork();
        
        /*
         * Checks if the network should be saved or restored to/from a
         * file.
         */
        if (controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateSave())) {
            saveNetworkToFile(networkSettingsFilename);
        }
        else if (controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateRestore())) {
            networkStructurer = reloadNetworkFromFile(networkSettingsFilename);
        }
        
        /* Updates the number of nodes in information panel. */
        controlPanel.getInformationNumberOfNodesLabel().setText(
                Integer.toString(networkStructurer.getnNodes()));
        
        /* Updates the number of links in information panel. */
        controlPanel.getInformationNumberOfLinksLabel().setText(
                Integer.toString(networkStructurer
                        .calculateTotalNumberOfLinks()));
        
        /* Resets the search is complete flag. */
        flagSearchComplete = 0;
        
        /* Ensures the GUI is up to date. */
        controlPanel.getSameNetworkDifferentSearchButton().setEnabled(true);
        controlPanel.getSameNetworkSameSearchButton().setEnabled(true);
        controlPanel.resetLabels();
        networkPanel.requestFocus();
        
        generateSearch();
        
        /*
         * Checks if the search should be saved or restored to/from a
         * file.
         */
        if (controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateRestore())) {
            reloadSearchFromFile(searchConditionsFilename);
        }
        else {
            search.chooseSourceAndTargets(); // this method updates
            // the search complete flag and one of the control panel labels,
            // hence those initializers being place before the call to
            // chooseSourceAndTargets
            
            if (controlPanel.getInputedInitialSearchState().equals(
                    controlPanel.getInitialSearchStateSave())) {
                saveSearchToFile(searchConditionsFilename);
            }
        }
        
        /* No need to draw the network if running in batch mode. */
        if (!controlPanel.getInputedSimulationType().equals(
                controlPanel.getSimulationBatchString())) {
            flagOKToDrawNetwork = 1;
            networkPanel.repaint();
        }
        
        /* Gets end time for the timer and calculates duration. */
//        final long endTime = System.nanoTime();
//        System.out.format("network created in %f seconds%n",
//                ((endTime - startTime) * Math.pow(10, -9)));
        
        /*
        * Executes the automated search if the user selected the automated mode
        * or the batch mode. Otherwise, the interactive search waits for a key
        * press to begin (see the key listener in the class NetworkPanel).
        */
        if ((controlPanel.getInputedSimulationType().equals(controlPanel
                .getSimulationAutomatedString())) ||
                (controlPanel.getInputedSimulationType().equals(controlPanel
                        .getSimulationBatchString()))) {
            doSearchAutomated();
        }
    }
    
    /**
     * Initializes new search conditions (i.e. new source and target nodes)
     * while keeping the same network.
     */
    public void initializeSearch() {
        /* Generates a new instance of the search. */
        generateSearch();
        search.chooseSourceAndTargets();
        
        /*
         * Checks if the search should be saved to a file.
         */
        if (controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateSave())) {
            saveSearchToFile(searchConditionsFilename);
        }
        
        /* Re-initializes certain parts of the program to allow for a fresh search. */
        flagSearchComplete = 0;
        networkPanel.repaint();
        
        /* Blanks the information display and results label readout in the control panel. */
        controlPanel.resetLabels();
        
        /* Sets focus on the network panel so that it can respond to key presses. */
        networkPanel.requestFocus();
        
        /*
         * Executes the automated search if the user selected the automated mode
         * or the batch mode. Otherwise, the interactive search waits for a key
         * press to begin (see the key listener in the class NetworkPanel).
         */
        if ((controlPanel.getInputedSimulationType().equals(controlPanel
                .getSimulationAutomatedString())) ||
                (controlPanel.getInputedSimulationType().equals(controlPanel
                        .getSimulationBatchString()))) {
            doSearchAutomated();
        }
    }
    
    /**
     * Resets the search strategy to its initial conditions (same search)
     * while keeping the same network.
     */
    public void resetSearch() {
        Node sourceNodeBackup;
        final HashMap<Integer, Node> querySourceBackupMap =
                new HashMap<Integer, Node>();
        
        Set<Node> targetNodesBackupSet;
        final HashMap<Integer, Set<Node>> queryTargetsBackupMap =
                new HashMap<Integer, Set<Node>>();
        
        final int nQueriesToBackup = search.getnQueries();
        
        // TODO Add a try/catch for the following backups in case the source
        // or targets can't be accessed
        
        /* Copies the source and target nodes for each query. */
        for (int iQuery = 0; iQuery < nQueriesToBackup; iQuery++) {
            sourceNodeBackup =
                    search.getSearchQueriesMap().get(iQuery).getSourceNode();
            querySourceBackupMap.put(iQuery, sourceNodeBackup);
            
            targetNodesBackupSet =
                    search.getSearchQueriesMap().get(iQuery)
                            .getTargetNodesSet();
            queryTargetsBackupMap.put(Integer.valueOf(iQuery),
                    targetNodesBackupSet);
        }
        
        /* Generates a new instance of the search. */
        generateSearch();
        
        for (int iQuery = 0; iQuery < search.getSearchQueriesMap().size(); iQuery++) {
            /* Restores the source nodes. */
            search.searchQueriesMap.get(iQuery).setSourceNode(
                    querySourceBackupMap.get(iQuery));
            
            /* Adds the source nodes to the set of current nodes. */
            search.searchQueriesMap.get(iQuery).getCurrentNodesSet().add(
                    querySourceBackupMap.get(iQuery));
            
            /* Restores the target nodes. */
            search.searchQueriesMap.get(iQuery).setTargetNodesSet(
                    queryTargetsBackupMap.get(iQuery));
        }
        
        /*
         * Checks if the search should be saved to a file.
         */
        if (controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateSave())) {
            saveSearchToFile(searchConditionsFilename);
        }
        
        /* Re-initializes certain parts of the program to allow for a fresh search. */
        flagSearchComplete = 0;
        networkPanel.repaint();
        
        /* Blanks the information display and results label readout in the control panel. */
        controlPanel.resetLabels();
        
        /* Sets focus on the network panel so that it can respond to key presses. */
        networkPanel.requestFocus();
        
        /*
         * Executes the automated search if the user selected the automated mode
         * or the batch mode. Otherwise, the interactive search waits for a key
         * press to begin (see the key listener in the class NetworkPanel).
         */
        if ((controlPanel.getInputedSimulationType().equals(controlPanel
                .getSimulationAutomatedString())) ||
                (controlPanel.getInputedSimulationType().equals(controlPanel
                        .getSimulationBatchString()))) {
            doSearchAutomated();
        }
    }
    
    /**
     * Generates the network using the parameters inputed by the user.
     */
    public void generateNetwork() {
        int nNodes = controlPanel.getInputednNodes();
        networkStructurer = new NetworkStructurer(this, nNodes);
        
        /* Erdos-Renyi graph. */
        if (controlPanel.getInputedNetworkType().equals(
                controlPanel.getNetworkERGraphString())) {
            network =
                    new ErdosRenyi(networkStructurer, nNodes, controlPanel
                            .getInputedLinkDensity());
        }
        /* Barbasi-Albert graph. */
        else if (controlPanel.getInputedNetworkType().equals(
                controlPanel.getNetworkBAGraphString())) {
            network =
                    new BarabasiAlbert(networkStructurer, nNodes, controlPanel
                            .getInputedBAInitialNodes(), controlPanel
                            .getInputedBALinksEachStep());
        }
        /* Wireless sensor network. */
        else if (controlPanel.getInputedNetworkType().equals(
                controlPanel.getNetworkRGGraphString())) {
            network =
                    new RandomGeometric(networkStructurer, nNodes, controlPanel
                            .getInputedLinkDensity());
        }
        
        /*
         * Only generates nodes and links if the program is not set to
         * restore the network information from a file.
         */
        if (!controlPanel.getInputedInitialSearchState().equals(
                controlPanel.getInitialSearchStateRestore())) {
            network.generateNodes();
            network.generateLinks();
            
            /*
             * Generates the Barabasi-Albert growth model.
             */
            if (controlPanel.getInputedNetworkType().equals(
                    controlPanel.getNetworkBAGraphString())) {
                ((BarabasiAlbert) network).generateBAModel();
            }
        }
    }
    
    /**
     * Generates the search scheme using the parameters inputed by the user.
     */
    public void generateSearch() {
        /* Flooding. */
        if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchFloodString())) {
            search =
                    new Flood(this, networkStructurer, controlPanel
                            .getInputtedTTL());
        }
        /* Random walk. */
        else if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchRWString())) {
            search =
                    new RandomWalk(this, networkStructurer, controlPanel
                            .getInputtedTTL());
        }
        /* Randomly replicated random walk. */
        else if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchRRRWString())) {
            search =
                    new RandomlyReplicatedRandomWalk(this, networkStructurer,
                            controlPanel.getInputtedTTL());
        }
        /* Bidirectional random walk. */
        else if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchBidirectionalRWString())) {
            search =
                    new BidirectionalSearchRW(this, networkStructurer,
                            controlPanel.getInputtedTTL());
        }
        /* Bidirectional linear. */
        else if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchBidirectionalLinearString())) {
            search =
                    new BidirectionalSearchLinear(this, networkStructurer,
                            controlPanel.getInputtedTTL());
        }
        /* Bidirectional hybrid. */
        else if (controlPanel.getInputedSearch().equals(
                controlPanel.getSearchBidirectionalHybridString())) {
            search =
                    new BidirectionalSearchRRRW(this, networkStructurer,
                            controlPanel.getInputtedTTL());
        }
    }
    
    /**
     * Executes the search in an interactive manner, meaning that the search
     * only propagates the queries by one step with each key press. This method
     * also checks to see if the terminating conditions have been met.
     * 
     * The search will only proceed as long as the "search complete" flag has
     * not been raised.
     */
    public void doSearchInteractive() {
        if (flagSearchComplete == 0) {
            search.propagateQueries();
            
            if (search.checkTerminatingConditions() == 1) {
                flagSearchComplete = 1;
            }
        }
    }
    
    /**
     * Executes the search in an automated manner. The search runs until the end
     * without any user interaction. This method also checks to see if the
     * terminating conditions have been met.
     * 
     * The search will only proceed as long as the "search complete" flag has
     * not been raised.
     * 
     * I tried turning off drawing the network to the screen during automated
     * searches but adding a println within the network panel's repaint method
     * revealed that it's still only drawn once even without this extra level
     * of control (repaint is asynchronous?).
     */
    public void doSearchAutomated() {
        while (flagSearchComplete == 0) {
            search.propagateQueries();
            
            if (search.checkTerminatingConditions() == 1) {
                flagSearchComplete = 1;
            }
        }
    }
    
    /**
     * Executes a batch search for each search strategy (instead of just the
     * search strategy specified in the control panel) using each of the network
     * types.
     * 
     * It runs through a bunch of saved networks and search conditions,
     * essentially a 1-click solution to generate all the results at once.
     * 
     * Example network name: networkInformationBA1.ser
     * Example search condition name: searchConditionsBA11.ser
     */
    public void doSearchBatchAllNetworksAndStrategies() {
        String inputedNetworkBackup = controlPanel.getInputedNetworkType(); // backs
        // up the inputed network before running the batch mode
        
        /* Executes the batch search for each network type. */
        for (int iNetworkType = 0; iNetworkType < controlPanel
                .getNetworkParameterTypeList().length; iNetworkType++) {
            String networkType =
                    controlPanel.getNetworkParameterTypeList()[iNetworkType];
            String networkTypePostfix = "";
            
            if (networkType.equals(controlPanel.getNetworkERGraphString())) {
                networkTypePostfix = "ER";
                controlPanel.setInputedNetworkType(controlPanel
                        .getNetworkERGraphString());
            }
            else if (networkType.equals(controlPanel.getNetworkBAGraphString())) {
                networkTypePostfix = "BA";
                controlPanel.setInputedNetworkType(controlPanel
                        .getNetworkBAGraphString());
            }
            else if (networkType.equals(controlPanel.getNetworkRGGraphString())) {
                networkTypePostfix = "RG";
                controlPanel.setInputedNetworkType(controlPanel
                        .getNetworkRGGraphString());
            }
            
            /* Executes the batch search for each instance of a given network type.*/
            for (int iNetworkInstance = 1; iNetworkInstance <= 3; iNetworkInstance++) {
                /*
                 * Executes the batch search for each search condition of the given
                 * instance of the network.
                 */
                for (int iSearchCondition = 1; iSearchCondition <= 3; iSearchCondition++) {
                    /*
                     * Updates the names of the network information and search conditions
                     * so that the batch search will load the appropriate network and
                     * search settings for the following simulation.
                     */
                    networkSettingsFilename =
                            "networkInformation" + networkTypePostfix +
                                    iNetworkInstance + ".ser";
                    searchConditionsFilename =
                            "searchConditions" + networkTypePostfix +
                                    iNetworkInstance + iSearchCondition +
                                    ".ser";
                    
                    System.out
                            .println("====================================================");
                    System.out.println(networkSettingsFilename + ", " +
                            searchConditionsFilename);
                    System.out
                            .println("====================================================");
                    System.out.println();
                    
                    doSearchBatchAllStrategies();
                    
                    System.out.println();
                    System.out.println();
                }
            }
            
        }
        
        System.out.println("Finished.");
        
        controlPanel.setInputedNetworkType(inputedNetworkBackup); // restores the
        // initial inputed network type after the batch mode has finished
    }
    
    /**
     * Executes a batch search for each search strategy (instead of just the
     * search strategy specified in the control panel) using the same network
     * type.
     * 
     * The TTLs are set here for each strategy as not all strategies use the
     * same TTL.
     */
    public void doSearchBatchAllStrategies() {
        String inputedSearchBackup = controlPanel.getInputedSearch(); // backs
        // up the inputed search type before running the batch mode
        
        /* Does a batch search for each search strategy. */
        for (int iSearchStrategy = 0; iSearchStrategy < controlPanel
                .getSearchParametersAlgorithmList().length; iSearchStrategy++) {
            controlPanel.setInputedSearch(controlPanel
                    .getSearchParametersAlgorithmList()[iSearchStrategy]);
            
            /* Flooding's TTL. */
            if (controlPanel.getInputedSearch().equals(
                    controlPanel.getSearchFloodString())) {
                /* ER graph. */
                if (controlPanel.getInputedNetworkType().equals(
                        controlPanel.getNetworkERGraphString())) {
                    controlPanel.setInputtedTTL(5);
                    
                }
                /* BA graph. */
                else if (controlPanel.getInputedNetworkType().equals(
                        controlPanel.getNetworkBAGraphString())) {
                    controlPanel.setInputtedTTL(4);
                }
                /* RG graph. */
                else if (controlPanel.getInputedNetworkType().equals(
                        controlPanel.getNetworkRGGraphString())) {
                    controlPanel.setInputtedTTL(40);
                }
            }
            /* Bidirectional linear's TTL. */
            else if (controlPanel.getInputedSearch().equals(
                    controlPanel.getSearchBidirectionalLinearString())) {
                /* ER graph. */
                if (controlPanel.getInputedNetworkType().equals(
                        controlPanel.getNetworkERGraphString())) {
                    controlPanel.setInputtedTTL(15);
                }
            }
            /* TTL for all other strategies. */
            else {
                controlPanel.setInputtedTTL(10000);
            }
            
            doSearchBatch();
        }
        
        controlPanel.setInputedSearch(inputedSearchBackup); // restores the
        // initial inputed search type after the batch mode has finished
    }
    
    /**
     * Executes the search in batch mode for running multiple
     * simulations automatically without outputting the network to the screen.
     * 
     * Results are printed to a file.
     */
    public void doSearchBatch() {
        final HashMap<Integer, int[]> batchResultsMap =
                new HashMap<Integer, int[]>(); // stores results from the
        // various simulations. Each simulation number is the key (e.g. the first
        // simulation gets a key of 0, second simulation a key of 1, etc.) and
        // the values are an array of the various parameters being measured
        
        int nSimulations;  // number of
        // simulations to do for each set of network and search parameters
        
        /* Send the printed system output to a file. */
        File file = new File("bidirectionalSearchSimulator_output.txt");
        FileOutputStream fis;
        try {
            fis = new FileOutputStream(file, true); // true allows append
            PrintStream out = new PrintStream(fis);
            System.setOut(out);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        }
        
        /*
         * Used during my final simulation phase since I will be doing 100
         * simulations for each search type. Flooding is deterministic as long
         * as the network topology remains constant and thus all 100 simulations
         * would be the same.
         */
//        if (controlPanel.getInputedSearch().equals(
//                controlPanel.getSearchFloodString())) {
//            nSimulations = 2;
//        }
//        else {
        nSimulations = controlPanel.getInputedBatchNumberOfSimulations();
//        }
        
//        System.out
//            .println("Welcome to Bidirectional Search Simulator BATCH MODE");
//        System.out
//            .println("====================================================");
//        System.out.println("");
        System.out.println("Network type: " +
                controlPanel.getInputedNetworkType());
        System.out.println("Network size: " + controlPanel.getInputednNodes());
        if ((controlPanel.getInputedNetworkType().equals(controlPanel
                .getNetworkERGraphString())) ||
                (controlPanel.getInputedNetworkType().equals(controlPanel
                        .getNetworkRGGraphString()))) {
            System.out.println("Link probability: " +
                    controlPanel.getInputedLinkDensity());
        }
        else if (controlPanel.getInputedNetworkType().equals(
                controlPanel.getNetworkBAGraphString())) {
            System.out.println("Initial nodes: " +
                    controlPanel.getInputedBAInitialNodes());
            System.out.println("Links to add at each step: " +
                    controlPanel.getInputedBALinksEachStep());
        }
        System.out.println("Search method: " + controlPanel.getInputedSearch());
        System.out.println("Time-to-live: " + controlPanel.getInputtedTTL());
//      System.out.println("Simulation type: " +
//            controlPanel.getInputedBatchSimulationType());
        System.out.println("");
        System.out
                .println("Simulation      Time  Msgs Links Nodes Visited Success");
        System.out
                .println("============== ===== ===== ===== ============= =======");
        
        for (int iSimulationNumber = 0; iSimulationNumber < nSimulations; iSimulationNumber++) {
            
            /*
             * Each simulation is done on a new network using new search
             * conditions.
             *
             * NOTE: THIS IS THE METHOD TO CHOOSE WHEN RESTORING A NETWORK
             * AND ITS SEARCH INFORMATION.
             */
            if (controlPanel
                    .getInputedBatchSimulationType()
                    .equals(controlPanel
                            .getBatchSimulationDifferentNetworkDifferentSearchString())) {
                initializeNetworkAndSearch();
            }
            /*
             * Each simulation uses the same network but different search
             * conditions.
             */
            else if (controlPanel
                    .getInputedBatchSimulationType()
                    .equals(controlPanel
                            .getBatchSimulationSameNetworkDifferentSearchString())) {
                initializeSearch();
                
            }
            /*
             * Each simulation reuses both the same network and the same search
             * conditions.
             */
            else if (controlPanel.getInputedBatchSimulationType().equals(
                    controlPanel
                            .getBatchSimulationSameNetworkSameSearchString())) {
                resetSearch();
                
            }
            
            /* Stores results. */
            final int[] resultsArray = new int[5];
            resultsArray[0] = search.getnTotalTime();
            resultsArray[1] = search.getnTotalMessages();
            resultsArray[2] = networkStructurer.calculateTotalNumberOfLinks();
            resultsArray[3] = search.calculateNumberOfNodesVisited();
            resultsArray[4] = search.getSearchResult();
            
            batchResultsMap.put(iSimulationNumber, resultsArray);
            
            /* Displays results. */
            System.out.format("Simulation %2d: %5d %5d %5d %13d %7d%n",
                    iSimulationNumber, resultsArray[0], resultsArray[1],
                    resultsArray[2], resultsArray[3], resultsArray[4]);
        }
        
        /* Generates summary. */
        final double[] summaryResultsTimeArray = new double[nSimulations];
        final double[] summaryResultsMessagesArray = new double[nSimulations];
        final double[] summaryResultsLinksArray = new double[nSimulations];
        final double[] summaryResultsNodesVisitedArray =
                new double[nSimulations];
        final double[] summaryResultsSuccessArray = new double[nSimulations];
        
        final Mean mean = new Mean();
        final Min min = new Min();
        final Max max = new Max();
        final StandardDeviation stddev = new StandardDeviation();
        final Sum sum = new Sum();
        
        /*
         * Transfers the values of each measurement for a given simulation into
         * their own array so that statistical calculations can be made for each
         * measurement.
         */
        for (int iSimulationNumber = 0; iSimulationNumber < nSimulations; iSimulationNumber++) {
            summaryResultsTimeArray[iSimulationNumber] =
                    batchResultsMap.get(iSimulationNumber)[0];
            summaryResultsMessagesArray[iSimulationNumber] =
                    batchResultsMap.get(iSimulationNumber)[1];
            summaryResultsLinksArray[iSimulationNumber] =
                    batchResultsMap.get(iSimulationNumber)[2];
            summaryResultsNodesVisitedArray[iSimulationNumber] =
                    batchResultsMap.get(iSimulationNumber)[3];
            summaryResultsSuccessArray[iSimulationNumber] =
                    batchResultsMap.get(iSimulationNumber)[4];
        }
        
        System.out.println("");
        System.out.println("Summary");
        System.out.println("=======");
        System.out.println("");
        
        System.out.print("Total time:           ");
        System.out.format("AVG: %7.1f   ", mean
                .evaluate(summaryResultsTimeArray));
        System.out.format("STDDEV: %7.1f   ", stddev
                .evaluate(summaryResultsTimeArray));
        System.out.format("MIN: %7.0f   ", min
                .evaluate(summaryResultsTimeArray));
        System.out
                .format("MAX: %7.0f%n", max.evaluate(summaryResultsTimeArray));
        
        System.out.print("Total messages:       ");
        System.out.format("AVG: %7.1f   ", mean
                .evaluate(summaryResultsMessagesArray));
        System.out.format("STDDEV: %7.1f   ", stddev
                .evaluate(summaryResultsMessagesArray));
        System.out.format("MIN: %7.0f   ", min
                .evaluate(summaryResultsMessagesArray));
        System.out.format("MAX: %7.0f%n", max
                .evaluate(summaryResultsMessagesArray));
        
        System.out.print("Total links:          ");
        System.out.format("AVG: %7.1f   ", mean
                .evaluate(summaryResultsLinksArray));
        System.out.format("STDDEV: %7.1f   ", stddev
                .evaluate(summaryResultsLinksArray));
        System.out.format("MIN: %7.0f   ", min
                .evaluate(summaryResultsLinksArray));
        System.out.format("MAX: %7.0f%n", max
                .evaluate(summaryResultsLinksArray));
        
        System.out.print("Total nodes visited:  ");
        System.out.format("AVG: %7.1f   ", mean
                .evaluate(summaryResultsNodesVisitedArray));
        System.out.format("STDDEV: %7.1f   ", stddev
                .evaluate(summaryResultsNodesVisitedArray));
        System.out.format("MIN: %7.0f   ", min
                .evaluate(summaryResultsNodesVisitedArray));
        System.out.format("MAX: %7.0f%n", max
                .evaluate(summaryResultsNodesVisitedArray));
        
        System.out.print("Success rate:         ");
        System.out
                .format("AVG: %7.1f%n",
                        (sum.evaluate(summaryResultsSuccessArray) / summaryResultsSuccessArray.length) * 100);
        
        /* 
         * Display the results in a format that allows me to copy and paste
         * directly into the report. 
         */
        System.out.println("");
        System.out.println("COPYPASTESTART");
        System.out
                .format("%.1f %.1f %.1f %.1f %.1f %.1f %.1f%n",
                        mean.evaluate(summaryResultsTimeArray),
                        stddev.evaluate(summaryResultsTimeArray),
                        mean.evaluate(summaryResultsMessagesArray),
                        stddev.evaluate(summaryResultsMessagesArray),
                        mean.evaluate(summaryResultsNodesVisitedArray),
                        stddev.evaluate(summaryResultsNodesVisitedArray),
                        (sum.evaluate(summaryResultsSuccessArray) / summaryResultsSuccessArray.length) * 100);
        System.out.println("COPYPASTEEND");
        System.out.println("");
        System.out.println("");
        
    }
    
    /**
     * Saves the instance of networkStructurer to a file on the hard disk. This
     * is to save the nodes and their locations for future reuse.
     */
    public void saveNetworkToFile(String networkSettingsFilename) {
        try {
            final File newFile = new File(networkSettingsFilename);
            newFile.createNewFile();
            final FileOutputStream fileOut = new FileOutputStream(newFile);
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(networkStructurer);
            out.close();
            fileOut.close();
            //System.out.println("saved network information to file");
        } catch (final IOException i) {
            i.printStackTrace();
        }
    }
    
    /**
     * Restores the instance of networkStructurer to a file on the hard disk.
     */
    public NetworkStructurer reloadNetworkFromFile(
            String networkSettingsFilename) {
        NetworkStructurer restoredNetworkStructurer = null;
        
        try {
            final FileInputStream fileIn =
                    new FileInputStream(networkSettingsFilename);
            final ObjectInputStream in = new ObjectInputStream(fileIn);
            restoredNetworkStructurer = (NetworkStructurer) in.readObject();
            in.close();
            fileIn.close();
            // System.out.println("restored network information from file");
        } catch (final IOException i) {
            //i.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Could not reload the network information. File not found",
                    "Bidirectional Search Simulator -- Warning",
                    JOptionPane.WARNING_MESSAGE);
            System.out
                    .println("Could not reload the network information. File not found");
            return null;
        } catch (final ClassNotFoundException c) {
            System.out
                    .println("Could not reload the network information. Class not found.");
            c.printStackTrace();
            return null;
        }
        
        return restoredNetworkStructurer;
    }
    
    /**
     * Saves an instance of the first search query to a file on the hard disk.
     * This is to save the first search query's source and target nodes for
     * future reuse.
     */
    public void saveSearchToFile(String seachConditionsFilename) {
        try {
            final File newFile = new File(seachConditionsFilename);
            newFile.createNewFile();
            final FileOutputStream fileOut = new FileOutputStream(newFile);
            final ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(search.getSearchQueriesMap().get(0));
            out.close();
            fileOut.close();
            //System.out.println("saved search information to file");
        } catch (final IOException i) {
            i.printStackTrace();
        }
    }
    
    /**
     * Saves an instance of a search query to a file on the hard disk.
     * 
     * NOTE: This method is a bit delicate as I always work with one target node
     * in all my simulations. However, I made it such that the target nodes are
     * actually a set and can thus contain more than one target node. I have not
     * added anything to manage this.
     */
    public void reloadSearchFromFile(String seachConditionsFilename) {
        Query restoredQuery = null;
        
        try {
            final FileInputStream fileIn =
                    new FileInputStream(seachConditionsFilename);
            final ObjectInputStream in = new ObjectInputStream(fileIn);
            restoredQuery = (Query) in.readObject();
            in.close();
            fileIn.close();
            // System.out.println("restored search information from file");
        } catch (final IOException i) {
            //i.printStackTrace();
            System.out
                    .println("Could not reload the search information. File not found");
            JOptionPane.showMessageDialog(null,
                    "Could not reload the search information. File not found",
                    "Bidirectional Search Simulator -- Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        } catch (final ClassNotFoundException c) {
            System.out
                    .println("Could not reload the search information. Class not found.");
            c.printStackTrace();
            
            return;
        }
        
        /*
         * Pulls out relevant information from the query and restores it. Keep
         * in mind that the source and targets for query 0 are inverted with
         * those of query 1 (e.g. the source of query 0 becomes a target of
         * query 1).
         */
        if (restoredQuery != null) {
            final String currentSearchStrategy =
                    controlPanel.getInputedSearch();
            
            /*
             * Both standard search strategies and bidirectional strategies
             * have a query with ID 0.
             */
            final Query query0 = search.getSearchQueriesMap().get(0);
            
            /* Restores the source node for query 0. */
            query0.setSourceNode(restoredQuery.getSourceNode());
            
            /*
             * Replaces the set of current nodes with a set containing the source
             * node for query 0.
             */
            Set<Node> newCurrentNodesSet = new HashSet<Node>();
            newCurrentNodesSet.add(restoredQuery.getSourceNode());
            query0.setCurrentNodesSet(newCurrentNodesSet);
            
            /*
             * Replaces the set of visited nodes with a set containing the source
             * node for query 0.
             */
            Set<Node> newVisitedNodesSet = new HashSet<Node>();
            newVisitedNodesSet.add(restoredQuery.getSourceNode());
            query0.setVisitedNodesSet(newVisitedNodesSet);
            
            /* Restores the target node for query 0. */
            query0.setTargetNodesSet(restoredQuery.getTargetNodesSet());
            
            /*
             * Bidirectional strategies have an additional query (ID 1).
             */
            if ((currentSearchStrategy.equals(controlPanel
                    .getSearchBidirectionalRWString())) ||
                    (currentSearchStrategy.equals(controlPanel
                            .getSearchBidirectionalLinearString())) ||
                    (currentSearchStrategy.equals(controlPanel
                            .getSearchBidirectionalHybridString()))) {
                final Query query1 = search.getSearchQueriesMap().get(1);
                
                /*
                 * Restores the source node for query 1, which is the target
                 * node of query 0.
                 */
                if (restoredQuery.getTargetNodesSet().size() == 1) {
                    final Set<Node> oldTargetNodesSet =
                            restoredQuery.getTargetNodesSet();
                    
                    /* Iterates over each node in the set of query 0's target nodes. */
                    final Iterator<Node> itTargetNodes =
                            oldTargetNodesSet.iterator();
                    
                    while (itTargetNodes.hasNext()) {
                        final Node aTargetNode = itTargetNodes.next();
                        query1.setSourceNode(aTargetNode);
                        
                        /*
                         * Replaces the set of current nodes of query 1 with a
                         * set containing its source node.
                         */
                        newCurrentNodesSet = new HashSet<Node>();
                        newCurrentNodesSet.add(aTargetNode);
                        query1.setCurrentNodesSet(newCurrentNodesSet);
                        
                        /*
                         * Replaces the set of visited nodes of query 1 with a 
                         * set containing its source node.
                         */
                        newVisitedNodesSet = new HashSet<Node>();
                        newVisitedNodesSet.add(aTargetNode);
                        query0.setVisitedNodesSet(newVisitedNodesSet);
                    }
                }
                else {
                    System.out
                            .println("Target nodes for query ID 1 could not"
                                    + "be restored as there was more than one target. This"
                                    + "method is not designed to handle multiple targets.");
                }
                
                /*
                 * Restores the target node for query 1, which is the source node
                 * of query 0.
                 */
                final Set<Node> newTargetNodesSet = new HashSet<Node>();
                newTargetNodesSet.add(restoredQuery.getSourceNode());
                query1.setTargetNodesSet(newTargetNodesSet);
            }
        }
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
     * Getter for network.
     * 
     * @return the network
     */
    public Network getNetwork() {
        return network;
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
     * Getter for networkPanel.
     * 
     * @return the networkPanel
     */
    public NetworkPanel getNetworkPanel() {
        return networkPanel;
    }
    
    /**
     * Getter for controlPanel.
     * 
     * @return the controlPanel
     */
    public ControlPanel getControlPanel() {
        return controlPanel;
    }
    
    /**
     * Getter for flagOKToDrawNetwork.
     * 
     * @return the flagOKToDrawNetwork
     */
    public int getFlagOKToDrawNetwork() {
        return flagOKToDrawNetwork;
    }
    
    /**
     * Setter for flagOKToDrawNetwork.
     * 
     * @param flagOKToDrawNetwork
     *            the flagOKToDrawNetwork to set
     */
    public void setFlagOKToDrawNetwork(final int flagOKToDrawNetwork) {
        this.flagOKToDrawNetwork = flagOKToDrawNetwork;
    }
    
    /**
     * Getter for flagSearchComplete.
     * 
     * @return the flagSearchComplete
     */
    public int getFlagSearchComplete() {
        return flagSearchComplete;
    }
    
    /**
     * Setter for flagSearchComplete.
     * 
     * @param flagSearchComplete
     *            the flagSearchComplete to set
     */
    public void setFlagSearchComplete(final int flagSearchComplete) {
        this.flagSearchComplete = flagSearchComplete;
    }
    
    /**
     * Getter for networkSettingsFilename.
     * 
     * @return the networkSettingsFilename
     */
    public String getNetworkSettingsFilename() {
        return networkSettingsFilename;
    }
    
    /**
     * Setter for networkSettingsFilename.
     * 
     * @param networkSettingsFilename
     *            the networkSettingsFilename to set
     */
    public void setNetworkSettingsFilename(String networkSettingsFilename) {
        this.networkSettingsFilename = networkSettingsFilename;
    }
    
    /**
     * Getter for searchConditionsFilename.
     * 
     * @return the searchConditionsFilename
     */
    public String getSearchConditionsFilename() {
        return searchConditionsFilename;
    }
    
    /**
     * Setter for searchConditionsFilename.
     * 
     * @param searchConditionsFilename
     *            the searchConditionsFilename to set
     */
    public void setSearchConditionsFilename(String searchConditionsFilename) {
        this.searchConditionsFilename = searchConditionsFilename;
    }
    
    /*
     * =========================================================================
     * Main entry point into the bidrectional search simulator.
     * =========================================================================
     *
     * A quick rundown of the classes:
     *
     * SearchCoordinator: the mother of all classes -- coordinates between the
     * various elements of this program
     *
     * Node: represents a single network node
     *
     * Link: represents a link joining two network nodes
     *
     * Network: abstract class that describes certain aspects of an unstructured
     * decentralized network
     * -- ErdosRenyi: represents the Erdos-Renyi random graph model
     * -- BarbasiAlbert: represents the Barbasi-Albert random graph model
     * -- RandomGeometric: represents the random geometric graph model
     *
     * NetworkStructurer: maintains information on the nodes and links of the
     * network
     *
     * Search: abstract class that describes the structure of a search algorithm
     * -- Flood: represents a flood search
     * -- RandomWalk: represents a random walk search
     * -- RandomlyReplicatedRandomWalk: represents a randomly replicated
     * random walk search where the query follows a predefined replication policy
     * -- BidirectionalSearchRW: represents a bidirectional random walk search
     * where one walk starts from the source, another from the destination
     * -- BidirectionalSearchRRRW: represents a bidirectional randomly replicated
     * random walk search where one walk starts from the source, another from
     * the destination, where each query follows a predefined replication policy
     * -- BidirectionalSearchLinear: represents a bidirectional linear search
     * which attempts to cross two more or less straight walkers
     *
     * NetworkPanel: displays the contents of the network to reflect search
     * progress
     *
     * ControlPanel: displays the controls used to initialize the network and
     * search parameters
     *
     *
     * @param args
     *            arguments passed to the program when launching
     */
    public static void main(final String args[]) {
        SearchCoordinator searchCoordinator;
        searchCoordinator = new SearchCoordinator();
        
        /* Creates the window frame. */
        final JFrame window = new JFrame("Bidirectional Search Simulator");
        
        /* Creates the content panel which will hold all other panels. */
        final JPanel content = new JPanel();
        content.setLayout(new FlowLayout());
        content.setBackground(new Color(70, 70, 70));
        content.add(searchCoordinator.getNetworkPanel());
        content.add(searchCoordinator.getControlPanel());
        
        /* Creates the menu items. */
        final JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setMnemonic('Q');
        quitItem.setAccelerator(KeyStroke.getKeyStroke("control Q"));
        
        final JMenuItem aboutItem = new JMenuItem("Information");
        quitItem.setMnemonic('I');
        quitItem.setAccelerator(KeyStroke.getKeyStroke("control I"));
        
        /* Creates the menu. */
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        final JMenu aboutMenu = new JMenu("About");
        fileMenu.setMnemonic('F');
        menuBar.add(fileMenu);
        menuBar.add(aboutMenu);
        fileMenu.add(quitItem);
        aboutMenu.add(aboutItem);
        
        /* Adds listeners to the menu items. */
        quitItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(final ActionEvent event) {
                System.exit(0);
            }
        });
        
        aboutItem.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(final ActionEvent event) {
                JOptionPane
                        .showMessageDialog(
                                null,
                                "<html>Bidirectional Search Simulator.<br>By Matthew Polan.<br><br> Projet de bachelor (en informatique), Sept. 2013. Université de Genève.<br>Supervisor: Prof. Pierre Leone</html>",
                                "Bidirectional Search Simulator -- About",
                                JOptionPane.WARNING_MESSAGE);
            }
        });
        
        /* Sets window properties. */
        window.setContentPane(content);
        window.setJMenuBar(menuBar);
        window.setVisible(true);
        window.pack();
        window.setResizable(false);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        /* Replaces the functionality of the removed batch button. */
        //searchCoordinator.doSearchBatchAllNetworksAndStrategies();
    }
    
}
