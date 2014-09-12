package bidirectionalSearchSimulator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * ControlPanel is a class that acts as an interface between the user and the
 * system. It allows for the user to input network parameters, search
 * parameters, and simulation information. It also outputs some network
 * information back to the user.
 */
public class ControlPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private final SearchCoordinator searchCoordinator;
    
    private final int xDimControlPanel = 280;    // control panel width
    private final int yDimControlPanel = 700;    // control panel height
    
    private String inputedNetworkType;
    private int inputednNodes;
    private double inputedLinkDensity;
    private int inputedBAInitialNodes;
    private int inputedBALinksEachStep;
    
    private String inputedSearch;
    private int inputedTTL;
    
    private String inputedSimulationType;
    private int inputedBatchNumberOfSimulations;
    private String inputedBatchSimulationType;
    
    private String inputedInitialSearchState;
    
    private int inputedBatchSimulateAllStrategies;
    private int inputedBatchSimulateAllNetworkAndStrategies;
    
    private final JPanel networkParametersPanel;
    private final JLabel networkParametersLabel;
    private final JPanel networkParametersCardPanel;
    private final JPanel networkParametersERPanel;
    private final JPanel networkParametersBAPanel;
    private final JPanel networkParametersRGPanel;
    private final String networkERGraphString = "Erdos-Renyi graph";
    private final String networkBAGraphString = "Barabasi-Albert graph";
    private final String networkRGGraphString = "Random geometric graph";
    private final String networkParameterTypeList[] = {networkERGraphString,
        networkBAGraphString, networkRGGraphString};
    private final JComboBox<String> networkParameterTypeComboBox;
    private final JLabel networkParameterERSizeLabel;
    private JTextField networkParameterERSizeField;
    private final JLabel networkParameterERLinkDensityLabel;
    private JTextField networkParameterERLinkDensityField;
    private final JLabel networkParameterBASizeLabel;
    private JTextField networkParameterBASizeField;
    private final JLabel networkParameterBAInitialNodesLabel;
    private JTextField networkParameterBAInitialNodesField;
    private final JLabel networkParameterBALinksToAddLabel;
    private JTextField networkParameterBALinksToAddField;
    private final JLabel networkParameterRGSizeLabel;
    private JTextField networkParameterRGSizeField;
    private final JLabel networkParameterRGLinkDensityLabel;
    private JTextField networkParameterRGLinkDensityField;
    
    private final JPanel searchParameterPanel;
    private final JLabel searchParameterLabel;
    private final JPanel searchParameterCardPanel;
    private final JPanel searchParameterFloodPanel;
    private final JPanel searchParameterRWPanel;
    private final JPanel searchParameterRRRWPanel;
    private final JPanel searchParameterBDRWPanel;
    private final JPanel searchParameterBDRRRWPanel;
    private final JPanel searchParameterBDLPanel;
    private final String searchFloodString = "Flooding";
    private final String searchRWString = "Random walk";
    private final String searchRRRWString = "Randomly replicated random walk";
    private final String searchBidirectionalRWString =
            "Bidirectional random walk";
    private final String searchBidirectionalLinearString =
            "Bidirectional linear";
    private final String searchBidirectionalRRRWString = "Bidirectional RRRW";
    private final String searchParametersAlgorithmList[] = {searchFloodString,
        searchRWString, searchRRRWString, searchBidirectionalRWString,
        searchBidirectionalRRRWString, searchBidirectionalLinearString};
    private final JComboBox<String> searchParameterAlgorithmComboBox;
    private final JLabel searchParameterFLOODTTLLabel;
    private JTextField searchParameterFLOODTTLField;
    private final JLabel searchParameterRWTTLLabel;
    private JTextField searchParameterRWTTLField;
    private final JLabel searchParameterRRRWTTLLabel;
    private JTextField searchParameterRRRWTTLField;
    private final JLabel searchParameterBDRWTTLLabel;
    private JTextField searchParameterBDRWTTLField;
    private final JLabel searchParameterBDRRRWTTLLabel;
    private JTextField searchParameterBDRRRWTTLField;
    private final JLabel searchParameterBDLTTLLabel;
    private JTextField searchParameterBDLTTLField;
    
    private final JPanel simulationTypePanel;
    private final JLabel simulationTypeLabel;
    private final String simulationInteractiveString = "Interactive";
    private final String simulationAutomatedString = "Automated";
    private final String simulationBatchString = "Batch";
//    private final String simulationTypeList[] = {simulationInteractiveString,
//        simulationAutomatedString, simulationBatchString};
    private final String batchSimulationSameNetworkSameSearchString =
            "Same network and same search";
    private final String batchSimulationSameNetworkDifferentSearchString =
            "Same network and different search";
    private final String batchSimulationDifferentNetworkDifferentSearchString =
            "Different network and different search";
    private final String batchSimulationTypeList[] = {
        batchSimulationSameNetworkSameSearchString,
        batchSimulationSameNetworkDifferentSearchString,
        batchSimulationDifferentNetworkDifferentSearchString};
    
    private JTextField networkInformationField;
    private JTextField searchConditionsField;
    private JCheckBox simulationTypeInteractiveCheckBox;
    private JCheckBox simulationTypeAutomatedCheckBox;
    private JCheckBox simulationTypeBatchCheckBox;
    private JCheckBox simulationTypeBatchAllSearchCheckBox;
    private JCheckBox simulationTypeBatchAllSearchAndNetworksCheckBox;
    private JTextField simulationNumberofSimulationsField;
    private JCheckBox saveNetworkAndSearchCheckBox;
    private JCheckBox reloadNetworkAndSearchCheckBox;
    
    private final String initialSearchStateNeither = "Do neither";
    private final String initialSearchStateSave = "Save network";
    private final String initialSearchStateRestore = "Restore network";
    private final String initialSearchStateList[] = {initialSearchStateNeither,
        initialSearchStateSave, initialSearchStateRestore};
    
    private final JPanel informationPanel;
    private final JPanel informationGridBagPanel;
    private final JLabel informationLabel;
    private final JLabel informationNumberOfNodesCaptionLabel;
    private final JLabel informationNumberOfNodesLabel;
    private final JLabel informationNumberOfLinksCaptionLabel;
    private final JLabel informationNumberOfLinksLabel;
    private final JLabel informationNumberOfTimeCaptionLabel;
    private final JLabel informationNumberOfTimeLabel;
    private final JLabel informationNumberOfMessagesCaptionLabel;
    private final JLabel informationNumberOfMessagesLabel;
    
    private final JPanel simulationControlsPanel;
    private final JButton differentNetworkDifferentSearchButton;
    private final JButton sameNetworkDifferentSearchButton;
    private final JButton sameNetworkSameSearchButton;
    private final JButton runBatchModeButton;
    
    private final JLabel resultLabel;
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            ControlPanel
     */
    public ControlPanel(final SearchCoordinator pSearchCoordinator) {
        searchCoordinator = pSearchCoordinator;
        setPreferredSize(new Dimension(xDimControlPanel, yDimControlPanel));
//        setBackground(Color.cyan);
        
        // FOR TEST PURPOSES ===========================================
        // SETS ALL THE IMPORTANT PARAMETERS NEEDED TO RUN A SEARCH ====
//        inputedNetworkType = networkERGraphString;
//        inputednNodes = 2000;
//        inputedLinkDensity = 0.015;
//        inputedBAInitialNodes = 2;
//        inputedBALinksEachStep = 5;
//        inputedSearch = searchFloodString;
//        inputedTTL = 10000;
//        inputedSimulationType = simulationBatchString;
//        inputedBatchSimulateAllNetworkAndStrategies = 1;
//        inputedBatchSimulateAllStrategies = 1;
//        inputedBatchNumberOfSimulations = 40;
//        inputedBatchSimulationType =
//                batchSimulationDifferentNetworkDifferentSearchString;
//        inputedInitialSearchState = initialSearchStateNeither;
        // =============================================================
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        /*
         * =====================================================================
         * Network parameters.
         * =====================================================================
         */
        networkParametersPanel = new JPanel();
//        networkParametersPanel.setBackground(Color.green);
        //networkParametersPanel.setPreferredSize(new Dimension(xDimControlPanel, 100));
        networkParametersPanel.setLayout(new BorderLayout());
        this.add(networkParametersPanel);
        
        /* Network parameters label. */
        networkParametersLabel =
                new JLabel("NETWORK PARAMETERS", SwingConstants.CENTER);
        networkParametersPanel.add(networkParametersLabel, BorderLayout.NORTH);
        
        /* Network parameters combobox. */
        networkParameterTypeComboBox =
                new JComboBox<String>(networkParameterTypeList);
        networkParameterTypeComboBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(final ItemEvent evt) {
                final CardLayout cardLayout =
                        (CardLayout) (networkParametersCardPanel.getLayout());
                cardLayout.show(networkParametersCardPanel, (String) evt
                        .getItem());
            }
            
        });
//        networkParameterTypeComboBox.addActionListener(new ActionListener() {
//
//            @SuppressWarnings({"unchecked"})
//            public void actionPerformed(ActionEvent e) {
//                String selectedNetwork =
//                    (String) ((JComboBox<String>) e.getSource())
//                        .getSelectedItem();
//                setInputedNetworkType(selectedNetwork);
//            }
//        });
        networkParametersPanel.add(networkParameterTypeComboBox,
                BorderLayout.CENTER);
        
        /* Network parameters card layout. */
        networkParametersCardPanel = new JPanel(new CardLayout());
        networkParametersPanel.add(networkParametersCardPanel,
                BorderLayout.SOUTH);
        
        /* Network parameters card layout -- ER panel. */
        networkParametersERPanel = new JPanel(new GridLayout(2, 2));
        networkParametersCardPanel.add(networkParametersERPanel,
                networkERGraphString);
        
        networkParameterERSizeLabel =
                new JLabel("<html>Size (2-2000):</html>", SwingConstants.CENTER);
        networkParametersERPanel.add(networkParameterERSizeLabel);
        
        networkParameterERSizeField = new JTextField(4);
        networkParameterERSizeField.setText("1000");
        networkParametersERPanel.add(networkParameterERSizeField);
        
        networkParameterERLinkDensityLabel =
                new JLabel("<html>Link density (0.0-1.0):</html>",
                        SwingConstants.CENTER);
        networkParametersERPanel.add(networkParameterERLinkDensityLabel);
        
        networkParameterERLinkDensityField = new JTextField(4);
        networkParameterERLinkDensityField.setText("0.02");
        networkParametersERPanel.add(networkParameterERLinkDensityField);
        
        /* Network parameters card layout -- BA panel. */
        networkParametersBAPanel = new JPanel(new GridLayout(3, 2));
        networkParametersCardPanel.add(networkParametersBAPanel,
                networkBAGraphString);
        
        networkParameterBASizeLabel =
                new JLabel("<html>Size (2-2000):</html>", SwingConstants.CENTER);
        networkParametersBAPanel.add(networkParameterBASizeLabel);
        
        networkParameterBASizeField = new JTextField(4);
        networkParameterBASizeField.setText("1000");
        networkParametersBAPanel.add(networkParameterBASizeField);
        
        networkParameterBAInitialNodesLabel =
                new JLabel("<html>Initial nodes (2+):</html>",
                        SwingConstants.CENTER);
        networkParametersBAPanel.add(networkParameterBAInitialNodesLabel);
        
        networkParameterBAInitialNodesField = new JTextField(4);
        networkParameterBAInitialNodesField.setText("2");
        networkParametersBAPanel.add(networkParameterBAInitialNodesField);
        
        networkParameterBALinksToAddLabel =
                new JLabel("<html>Links to add (1+):</html>",
                        SwingConstants.CENTER);
        networkParametersBAPanel.add(networkParameterBALinksToAddLabel);
        
        networkParameterBALinksToAddField = new JTextField(4);
        networkParameterBALinksToAddField.setText("2");
        networkParametersBAPanel.add(networkParameterBALinksToAddField);
        
        /* Network parameters card layout -- RG panel. */
        networkParametersRGPanel = new JPanel(new GridLayout(2, 2));
        networkParametersCardPanel.add(networkParametersRGPanel,
                networkRGGraphString);
        
        networkParameterRGSizeLabel =
                new JLabel("<html>Size (2-2000):</html>", SwingConstants.CENTER);
        networkParametersRGPanel.add(networkParameterRGSizeLabel);
        
        networkParameterRGSizeField = new JTextField(4);
        networkParameterRGSizeField.setText("1000");
        networkParametersRGPanel.add(networkParameterRGSizeField);
        
        networkParameterRGLinkDensityLabel =
                new JLabel("<html>Link density (0.0-1.0):</html>",
                        SwingConstants.CENTER);
        networkParametersRGPanel.add(networkParameterRGLinkDensityLabel);
        
        networkParameterRGLinkDensityField = new JTextField(4);
        networkParameterRGLinkDensityField.setText("0.08");
        networkParametersRGPanel.add(networkParameterRGLinkDensityField);
        
        /*
         * =====================================================================
         * Search parameters.
         * =====================================================================
         */
        this.add(Box.createRigidArea(new Dimension(0, 15))); // filler
        
        searchParameterPanel = new JPanel();
//        searchParameterPanel.setBackground(Color.orange);
        //searchParametersPanel.setPreferredSize(new Dimension(xDimControlPanel, 100));
        searchParameterPanel.setLayout(new BorderLayout());
        this.add(searchParameterPanel);
        
        /* Search parameters label. */
        searchParameterLabel =
                new JLabel("SEARCH PARAMETERS", SwingConstants.CENTER);
        searchParameterPanel.add(searchParameterLabel, BorderLayout.NORTH);
        
        /* Search parameters combobox. */
        searchParameterAlgorithmComboBox =
                new JComboBox<String>(searchParametersAlgorithmList);
        searchParameterAlgorithmComboBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(final ItemEvent evt) {
                final CardLayout cardLayout =
                        (CardLayout) (searchParameterCardPanel.getLayout());
                cardLayout.show(searchParameterCardPanel, (String) evt
                        .getItem());
            }
            
        });
        searchParameterPanel.add(searchParameterAlgorithmComboBox,
                BorderLayout.CENTER);
        
        /* Search parameters card layout. */
        searchParameterCardPanel = new JPanel(new CardLayout());
        searchParameterPanel.add(searchParameterCardPanel, BorderLayout.SOUTH);
        
        /* Search parameters card layout -- FLOOD panel. */
        searchParameterFloodPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel.add(searchParameterFloodPanel,
                searchFloodString);
        
        searchParameterFLOODTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterFloodPanel.add(searchParameterFLOODTTLLabel);
        
        searchParameterFLOODTTLField = new JTextField(4);
        searchParameterFLOODTTLField.setText("5");
        searchParameterFloodPanel.add(searchParameterFLOODTTLField);
        
        /* Search parameters card layout -- RW panel. */
        searchParameterRWPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel.add(searchParameterRWPanel, searchRWString);
        
        searchParameterRWTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterRWPanel.add(searchParameterRWTTLLabel);
        
        searchParameterRWTTLField = new JTextField(4);
        searchParameterRWTTLField.setText("500");
        searchParameterRWPanel.add(searchParameterRWTTLField);
        
        /* Search parameters card layout -- RRRW panel. */
        searchParameterRRRWPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel
                .add(searchParameterRRRWPanel, searchRRRWString);
        
        searchParameterRRRWTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterRRRWPanel.add(searchParameterRRRWTTLLabel);
        
        searchParameterRRRWTTLField = new JTextField(4);
        searchParameterRRRWTTLField.setText("500");
        searchParameterRRRWPanel.add(searchParameterRRRWTTLField);
        
        /* Search parameters card layout -- BDRW panel. */
        searchParameterBDRWPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel.add(searchParameterBDRWPanel,
                searchBidirectionalRWString);
        
        searchParameterBDRWTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterBDRWPanel.add(searchParameterBDRWTTLLabel);
        
        searchParameterBDRWTTLField = new JTextField(4);
        searchParameterBDRWTTLField.setText("500");
        searchParameterBDRWPanel.add(searchParameterBDRWTTLField);
        
        /* Search parameters card layout -- BDRRRW panel. */
        searchParameterBDRRRWPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel.add(searchParameterBDRRRWPanel,
                searchBidirectionalRRRWString);
        
        searchParameterBDRRRWTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterBDRRRWPanel.add(searchParameterBDRRRWTTLLabel);
        
        searchParameterBDRRRWTTLField = new JTextField(4);
        searchParameterBDRRRWTTLField.setText("500");
        searchParameterBDRRRWPanel.add(searchParameterBDRRRWTTLField);
        
        /* Search parameters card layout -- BDL panel. */
        searchParameterBDLPanel = new JPanel(new GridLayout(1, 2));
        searchParameterCardPanel.add(searchParameterBDLPanel,
                searchBidirectionalLinearString);
        
        searchParameterBDLTTLLabel =
                new JLabel("<html>TTL (1+):</html>", SwingConstants.CENTER);
        searchParameterBDLPanel.add(searchParameterBDLTTLLabel);
        
        searchParameterBDLTTLField = new JTextField(4);
        searchParameterBDLTTLField.setText("500");
        searchParameterBDLPanel.add(searchParameterBDLTTLField);
        
        /*
         * =====================================================================
         * Simulation parameters.
         * =====================================================================
         */
        this.add(Box.createRigidArea(new Dimension(0, 15))); // filler
        
        simulationTypePanel = new JPanel();
//        simulationTypePanel.setBackground(Color.pink);
        //simulationTypePanel.setPreferredSize(new Dimension(xDimControlPanel, 100));
        simulationTypePanel.setLayout(new BorderLayout());
        this.add(simulationTypePanel);
        
        simulationTypeLabel =
                new JLabel("SIMULATION TYPE", SwingConstants.CENTER);
        simulationTypePanel.add(simulationTypeLabel, BorderLayout.NORTH);
        
        JPanel simulationTypeGridBagPanel = new JPanel(new GridBagLayout());
        this.add(simulationTypeGridBagPanel);
        
        final GridBagConstraints gridBagConstraintsSimulationType =
                new GridBagConstraints();
        
        /* Interactive checkbox. */
        simulationTypeInteractiveCheckBox =
                new JCheckBox(simulationInteractiveString, true);
        simulationTypeInteractiveCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (simulationTypeInteractiveCheckBox.isSelected()) {
                    simulationTypeAutomatedCheckBox.setSelected(false);
                    simulationTypeBatchCheckBox.setSelected(false);
                    simulationTypeBatchAllSearchCheckBox.setEnabled(false);
                    simulationTypeBatchAllSearchAndNetworksCheckBox
                            .setEnabled(false);
                    
                    simulationNumberofSimulationsField.setEnabled(false);
                }
                else {
                    if (!simulationTypeAutomatedCheckBox.isSelected() &&
                            !simulationTypeBatchCheckBox.isSelected()) {
                        simulationTypeInteractiveCheckBox.setSelected(true);
                    }
                }
            }
        });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        simulationTypeGridBagPanel.add(simulationTypeInteractiveCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Automated checkbox. */
        simulationTypeAutomatedCheckBox =
                new JCheckBox(simulationAutomatedString, false);
        simulationTypeAutomatedCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (simulationTypeAutomatedCheckBox.isSelected()) {
                    simulationTypeInteractiveCheckBox.setSelected(false);
                    simulationTypeBatchCheckBox.setSelected(false);
                    simulationTypeBatchAllSearchCheckBox.setEnabled(false);
                    simulationTypeBatchAllSearchAndNetworksCheckBox
                            .setEnabled(false);
                    
                    simulationNumberofSimulationsField.setEnabled(false);
                }
                else {
                    if (!simulationTypeInteractiveCheckBox.isSelected() &&
                            !simulationTypeBatchCheckBox.isSelected()) {
                        simulationTypeAutomatedCheckBox.setSelected(true);
                    }
                }
            }
        });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 1;
        gridBagConstraintsSimulationType.gridy = 0;
        simulationTypeGridBagPanel.add(simulationTypeAutomatedCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Batch checkbox. */
        simulationTypeBatchCheckBox =
                new JCheckBox(simulationBatchString, false);
        simulationTypeBatchCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (simulationTypeBatchCheckBox.isSelected()) {
                    simulationTypeInteractiveCheckBox.setSelected(false);
                    simulationTypeAutomatedCheckBox.setSelected(false);
                    simulationTypeBatchAllSearchCheckBox.setEnabled(true);
                    simulationTypeBatchAllSearchAndNetworksCheckBox
                            .setEnabled(true);
                    
                    simulationNumberofSimulationsField.setEnabled(true);
                    
                }
                else {
                    simulationTypeBatchAllSearchCheckBox.setEnabled(false);
                    simulationTypeBatchAllSearchAndNetworksCheckBox
                            .setEnabled(false);
                    
                    if (!simulationTypeInteractiveCheckBox.isSelected() &&
                            !simulationTypeAutomatedCheckBox.isSelected()) {
                        simulationTypeBatchCheckBox.setSelected(true);
                    }
                }
            }
        });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 2;
        gridBagConstraintsSimulationType.gridy = 0;
        simulationTypeGridBagPanel.add(simulationTypeBatchCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Batch all searches checkbox. */
        simulationTypeBatchAllSearchCheckBox =
                new JCheckBox("Batch (do each search)", false);
        simulationTypeBatchAllSearchCheckBox.setEnabled(false);
        simulationTypeBatchAllSearchCheckBox
                .addItemListener(new ItemListener() {
                    
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (simulationTypeBatchAllSearchCheckBox.isSelected()) {
                            simulationTypeBatchAllSearchAndNetworksCheckBox
                                    .setSelected(false);
                        }
                        else {
                            inputedBatchSimulateAllStrategies = 0;
                        }
                        
                    }
                });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 1;
        gridBagConstraintsSimulationType.gridwidth = 2;
        simulationTypeGridBagPanel.add(simulationTypeBatchAllSearchCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Number of batch simulations. */
        simulationNumberofSimulationsField = new JTextField(0);
        simulationNumberofSimulationsField.setText("3");
        simulationNumberofSimulationsField.setEnabled(false);
        simulationNumberofSimulationsField
                .setToolTipText("Number of simulations");
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 2;
        gridBagConstraintsSimulationType.gridy = 1;
        simulationTypeGridBagPanel.add(simulationNumberofSimulationsField,
                gridBagConstraintsSimulationType);
        
        /* Batch all search and networks checkbox. */
        simulationTypeBatchAllSearchAndNetworksCheckBox =
                new JCheckBox("Batch (do all networks and search)", false);
        simulationTypeBatchAllSearchAndNetworksCheckBox.setEnabled(false);
        simulationTypeBatchAllSearchAndNetworksCheckBox
                .setToolTipText("This option is buggy, only works under special circumstances. Use at your own risk!");
        simulationTypeBatchAllSearchAndNetworksCheckBox
                .addItemListener(new ItemListener() {
                    
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (simulationTypeBatchAllSearchAndNetworksCheckBox
                                .isSelected()) {
                            simulationTypeBatchAllSearchCheckBox
                                    .setSelected(false);
                            
                            inputedBatchSimulateAllNetworkAndStrategies = 1;
                        }
                        else {
                            inputedBatchSimulateAllNetworkAndStrategies = 0;
                        }
                    }
                });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 2;
        gridBagConstraintsSimulationType.gridwidth = 3;
        simulationTypeGridBagPanel.add(
                simulationTypeBatchAllSearchAndNetworksCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Line separator. */
        JLabel lineSeparator = new JLabel("_________________");
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 3;
        gridBagConstraintsSimulationType.gridwidth = 3;
        gridBagConstraintsSimulationType.insets = new Insets(0, 50, 13, 0);
        simulationTypeGridBagPanel.add(lineSeparator,
                gridBagConstraintsSimulationType);
        
        /* Load/Save network and search information. */
        networkInformationField = new JTextField(0);
        networkInformationField.setText("networkInformation.ser");
        networkInformationField.setEnabled(false);
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 4;
        gridBagConstraintsSimulationType.gridwidth = 3;
        gridBagConstraintsSimulationType.insets = new Insets(0, 0, 0, 0);
        simulationTypeGridBagPanel.add(networkInformationField,
                gridBagConstraintsSimulationType);
        
        searchConditionsField = new JTextField(0);
        searchConditionsField.setText("searchConditions.ser");
        searchConditionsField.setEnabled(false);
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 5;
        gridBagConstraintsSimulationType.gridwidth = 3;
        simulationTypeGridBagPanel.add(searchConditionsField,
                gridBagConstraintsSimulationType);
        
        /* Save network and search checkbox. */
        saveNetworkAndSearchCheckBox = new JCheckBox("Save to disk", false);
        saveNetworkAndSearchCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (saveNetworkAndSearchCheckBox.isSelected()) {
                    reloadNetworkAndSearchCheckBox.setSelected(false);
                    networkInformationField.setEnabled(true);
                    searchConditionsField.setEnabled(true);
                }
                else {
                    if (!reloadNetworkAndSearchCheckBox.isSelected()) {
                        networkInformationField.setEnabled(false);
                        searchConditionsField.setEnabled(false);
                    }
                }
            }
        });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 6;
        simulationTypeGridBagPanel.add(saveNetworkAndSearchCheckBox,
                gridBagConstraintsSimulationType);
        
        /* Reload network checkbox. */
        reloadNetworkAndSearchCheckBox =
                new JCheckBox("Reload from disk", false);
        reloadNetworkAndSearchCheckBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (reloadNetworkAndSearchCheckBox.isSelected()) {
                    saveNetworkAndSearchCheckBox.setSelected(false);
                    networkInformationField.setEnabled(true);
                    searchConditionsField.setEnabled(true);
                    
                    inputedInitialSearchState = initialSearchStateRestore;
                }
                else {
                    if (!saveNetworkAndSearchCheckBox.isSelected()) {
                        networkInformationField.setEnabled(false);
                        searchConditionsField.setEnabled(false);
                        
                        inputedInitialSearchState = initialSearchStateNeither;
                    }
                }
            }
        });
        gridBagConstraintsSimulationType.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraintsSimulationType.gridx = 0;
        gridBagConstraintsSimulationType.gridy = 7;
        simulationTypeGridBagPanel.add(reloadNetworkAndSearchCheckBox,
                gridBagConstraintsSimulationType);
        
        /*
         * =====================================================================
         * Simulation controls.
         * =====================================================================
         */
        this.add(Box.createRigidArea(new Dimension(0, 15))); // filler
        
        simulationControlsPanel = new JPanel(new GridBagLayout());
//        simulationControlsPanel.setBackground(Color.white);
        this.add(simulationControlsPanel);
        
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        
        differentNetworkDifferentSearchButton =
                new JButton("Different network, different search");
        differentNetworkDifferentSearchButton
                .addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        /* Save network and search. */
                        if (saveNetworkAndSearchCheckBox.isSelected()) {
                            inputedInitialSearchState = initialSearchStateSave;
                        }
                        else if (reloadNetworkAndSearchCheckBox.isSelected()) {
                            inputedInitialSearchState =
                                    initialSearchStateRestore;
                        }
                        else {
                            inputedInitialSearchState =
                                    initialSearchStateNeither;
                        }
                        
                        /* Network and search filenames. */
                        searchCoordinator
                                .setNetworkSettingsFilename(networkInformationField
                                        .getText());
                        searchCoordinator
                                .setSearchConditionsFilename(searchConditionsField
                                        .getText());
                        
                        /* Network type: ER/BA/RG
                         * and parameters. */
                        inputedNetworkType =
                                (String) networkParameterTypeComboBox
                                        .getSelectedItem();
                        
                        if (inputedNetworkType.equals(networkERGraphString)) {
                            inputednNodes =
                                    Integer.parseInt(networkParameterERSizeField
                                            .getText());
                            inputedLinkDensity =
                                    Double.parseDouble(networkParameterERLinkDensityField
                                            .getText());
                        }
                        else if (inputedNetworkType
                                .equals(networkBAGraphString)) {
                            inputednNodes =
                                    Integer.parseInt(networkParameterBASizeField
                                            .getText());
                            inputedBAInitialNodes =
                                    Integer.parseInt(networkParameterBAInitialNodesField
                                            .getText());
                            inputedBALinksEachStep =
                                    Integer.parseInt(networkParameterBALinksToAddField
                                            .getText());
                        }
                        else if (inputedNetworkType
                                .equals(networkRGGraphString)) {
                            inputednNodes =
                                    Integer.parseInt(networkParameterRGSizeField
                                            .getText());
                            inputedLinkDensity =
                                    Double.parseDouble(networkParameterRGLinkDensityField
                                            .getText());
                        }
                        
                        /* Search algorithm: flood/RW/RRRW/BDRW/BDRRRW/BDL
                         * and parameters. */
                        inputedSearch =
                                (String) searchParameterAlgorithmComboBox
                                        .getSelectedItem();
                        
                        if (inputedSearch.equals(searchFloodString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterFLOODTTLField
                                            .getText());
                        }
                        else if (inputedSearch.equals(searchRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch.equals(searchRRRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterRRRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalRRRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDRRRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalLinearString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDLTTLField
                                            .getText());
                        }
                        
                        /* Simulation type: interactive/automated/batch. */
                        if (simulationTypeInteractiveCheckBox.isSelected()) {
                            inputedSimulationType = simulationInteractiveString;
                        }
                        else if (simulationTypeAutomatedCheckBox.isSelected()) {
                            inputedSimulationType = simulationAutomatedString;
                        }
                        else if (simulationTypeBatchCheckBox.isSelected()) {
                            inputedSimulationType = simulationBatchString;
                            
                            inputedBatchNumberOfSimulations =
                                    Integer.parseInt(simulationNumberofSimulationsField
                                            .getText());
                            
                            inputedBatchSimulationType =
                                    batchSimulationDifferentNetworkDifferentSearchString;
                            
                            if (!simulationTypeBatchAllSearchAndNetworksCheckBox
                                    .isSelected() &&
                                    !simulationTypeBatchAllSearchCheckBox
                                            .isSelected()) {
                                searchCoordinator.doSearchBatch();
                            }
                            else if (simulationTypeBatchAllSearchCheckBox
                                    .isSelected()) {
                                searchCoordinator.doSearchBatchAllStrategies();
                            }
                            else if (simulationTypeBatchAllSearchAndNetworksCheckBox
                                    .isSelected()) {
                                searchCoordinator
                                        .doSearchBatchAllNetworksAndStrategies();
                            }
                        }
                        
                        /* Finally, initialize the network and search! */
                        searchCoordinator.initializeNetworkAndSearch();
                    }
                });
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        simulationControlsPanel.add(differentNetworkDifferentSearchButton,
                gridBagConstraints);
        
        sameNetworkDifferentSearchButton =
                new JButton("Same network, different search");
        sameNetworkDifferentSearchButton.setEnabled(false);  // stays deactivated until a network
        // is created at least once (as a search cannot proceed without a network)
        sameNetworkDifferentSearchButton
                .addActionListener(new ActionListener() {
                    
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        /* Search algorithm: flood/RW/RRRW/BDRW/BDRRRW/BDL
                         * and parameters. */
                        inputedSearch =
                                (String) searchParameterAlgorithmComboBox
                                        .getSelectedItem();
                        
                        if (inputedSearch.equals(searchFloodString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterFLOODTTLField
                                            .getText());
                        }
                        else if (inputedSearch.equals(searchRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch.equals(searchRRRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterRRRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalRRRWString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDRRRWTTLField
                                            .getText());
                        }
                        else if (inputedSearch
                                .equals(searchBidirectionalLinearString)) {
                            inputedTTL =
                                    Integer.parseInt(searchParameterBDLTTLField
                                            .getText());
                        }
                        
                        /* Simulation type: interactive/automated/batch. */
                        if (simulationTypeInteractiveCheckBox.isSelected()) {
                            inputedSimulationType = simulationInteractiveString;
                        }
                        else if (simulationTypeAutomatedCheckBox.isSelected()) {
                            inputedSimulationType = simulationAutomatedString;
                        }
                        else if (simulationTypeBatchCheckBox.isSelected()) {
                            inputedSimulationType = simulationBatchString;
                            
                            inputedBatchNumberOfSimulations =
                                    Integer.parseInt(simulationNumberofSimulationsField
                                            .getText());
                            
                            inputedBatchSimulationType =
                                    batchSimulationSameNetworkDifferentSearchString;
                            
                            if (!simulationTypeBatchAllSearchAndNetworksCheckBox
                                    .isSelected() &&
                                    !simulationTypeBatchAllSearchCheckBox
                                            .isSelected()) {
                                searchCoordinator.doSearchBatch();
                            }
                            else if (simulationTypeBatchAllSearchCheckBox
                                    .isSelected()) {
                                searchCoordinator.doSearchBatchAllStrategies();
                            }
                            else if (simulationTypeBatchAllSearchAndNetworksCheckBox
                                    .isSelected()) {
                                searchCoordinator
                                        .doSearchBatchAllNetworksAndStrategies();
                            }
                            
                        }
                        
                        /* Save network and search. */
                        if (saveNetworkAndSearchCheckBox.isSelected()) {
                            inputedInitialSearchState = initialSearchStateSave;
                        }
                        else if (reloadNetworkAndSearchCheckBox.isSelected()) {
                            inputedInitialSearchState =
                                    initialSearchStateRestore;
                        }
                        else {
                            inputedInitialSearchState =
                                    initialSearchStateNeither;
                        }
                        
                        /* Network and search filenames. */
                        searchCoordinator
                                .setNetworkSettingsFilename(networkInformationField
                                        .getText());
                        searchCoordinator
                                .setSearchConditionsFilename(searchConditionsField
                                        .getText());
                        
                        /* Finally, initialize the search! */
                        searchCoordinator.initializeSearch();
                    }
                });
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        simulationControlsPanel.add(sameNetworkDifferentSearchButton,
                gridBagConstraints);
        
        sameNetworkSameSearchButton = new JButton("Same network, same search");
        sameNetworkSameSearchButton.setEnabled(false);  // stays deactivated until
        // a network is created at least once (as a search cannot proceed
        // without a network)
        sameNetworkSameSearchButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                /* Search algorithm: flood/RW/RRRW/BDRW/BDRRRW/BDL
                 * and parameters. */
                inputedSearch =
                        (String) searchParameterAlgorithmComboBox
                                .getSelectedItem();
                
                if (inputedSearch.equals(searchFloodString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterFLOODTTLField
                                    .getText());
                }
                else if (inputedSearch.equals(searchRWString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterRWTTLField
                                    .getText());
                }
                else if (inputedSearch.equals(searchRRRWString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterRRRWTTLField
                                    .getText());
                }
                else if (inputedSearch.equals(searchBidirectionalRWString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterBDRWTTLField
                                    .getText());
                }
                else if (inputedSearch.equals(searchBidirectionalRRRWString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterBDRRRWTTLField
                                    .getText());
                }
                else if (inputedSearch.equals(searchBidirectionalLinearString)) {
                    inputedTTL =
                            Integer.parseInt(searchParameterBDLTTLField
                                    .getText());
                }
                
                /* Simulation type: interactive/automated/batch. */
                if (simulationTypeInteractiveCheckBox.isSelected()) {
                    inputedSimulationType = simulationInteractiveString;
                }
                else if (simulationTypeAutomatedCheckBox.isSelected()) {
                    inputedSimulationType = simulationAutomatedString;
                }
                else if (simulationTypeBatchCheckBox.isSelected()) {
                    inputedSimulationType = simulationBatchString;
                    
                    inputedBatchNumberOfSimulations =
                            Integer.parseInt(simulationNumberofSimulationsField
                                    .getText());
                    
                    inputedBatchSimulationType =
                            batchSimulationSameNetworkSameSearchString;
                    
                    if (!simulationTypeBatchAllSearchAndNetworksCheckBox
                            .isSelected() &&
                            !simulationTypeBatchAllSearchCheckBox.isSelected()) {
                        searchCoordinator.doSearchBatch();
                    }
                    else if (simulationTypeBatchAllSearchAndNetworksCheckBox
                            .isSelected()) {
                        searchCoordinator
                                .doSearchBatchAllNetworksAndStrategies();
                    }
                    else if (simulationTypeBatchAllSearchCheckBox.isSelected()) {
                        searchCoordinator.doSearchBatchAllStrategies();
                    }
                }
                
                /* Save network and search. */
                if (saveNetworkAndSearchCheckBox.isSelected()) {
                    inputedInitialSearchState = initialSearchStateSave;
                }
                else if (reloadNetworkAndSearchCheckBox.isSelected()) {
                    inputedInitialSearchState = initialSearchStateRestore;
                }
                else {
                    inputedInitialSearchState = initialSearchStateNeither;
                }
                
                /* Network and search filenames. */
                searchCoordinator
                        .setNetworkSettingsFilename(networkInformationField
                                .getText());
                searchCoordinator
                        .setSearchConditionsFilename(searchConditionsField
                                .getText());
                
                /* Finally, reset the search! */
                searchCoordinator.resetSearch();
            }
        });
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        simulationControlsPanel.add(sameNetworkSameSearchButton,
                gridBagConstraints);
        
        runBatchModeButton = new JButton("Run batch mode");
        runBatchModeButton.setEnabled(false);
        runBatchModeButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(final ActionEvent e) {
                
            }
        });
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        //simulationControlsPanel.add(runBatchModeButton, gridBagConstraints);
        
        /*
         * =====================================================================
         * General information.
         * =====================================================================
         */
        this.add(Box.createRigidArea(new Dimension(0, 15))); // filler
        
        informationPanel = new JPanel(new BorderLayout());
//        informationPanel.setBackground(Color.yellow);
        //informationPanel.setPreferredSize(new Dimension(xDimControlPanel, 100));
        this.add(informationPanel);
        
        informationLabel = new JLabel("INFORMATION", SwingConstants.CENTER);
        informationPanel.add(informationLabel, BorderLayout.NORTH);
        
        informationGridBagPanel = new JPanel(new GridBagLayout());
        informationPanel.add(informationGridBagPanel);
        
        final GridBagConstraints gridBagConstraintsGeneralInformation =
                new GridBagConstraints();
        
        informationNumberOfNodesCaptionLabel =
                new JLabel("Number of nodes: ", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 0;
        gridBagConstraintsGeneralInformation.gridy = 0;
        gridBagConstraintsGeneralInformation.insets = new Insets(0, 5, 5, 30);
        informationGridBagPanel.add(informationNumberOfNodesCaptionLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfNodesLabel = new JLabel("0", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 1;
        gridBagConstraintsGeneralInformation.gridy = 0;
        informationGridBagPanel.add(informationNumberOfNodesLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfLinksCaptionLabel =
                new JLabel("Number of links: ", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 0;
        gridBagConstraintsGeneralInformation.gridy = 1;
        informationGridBagPanel.add(informationNumberOfLinksCaptionLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfLinksLabel = new JLabel("0", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 1;
        gridBagConstraintsGeneralInformation.gridy = 1;
        informationGridBagPanel.add(informationNumberOfLinksLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfTimeCaptionLabel =
                new JLabel("Total time steps: ", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 0;
        gridBagConstraintsGeneralInformation.gridy = 2;
        informationGridBagPanel.add(informationNumberOfTimeCaptionLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfTimeLabel = new JLabel("0", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 1;
        gridBagConstraintsGeneralInformation.gridy = 2;
        informationGridBagPanel.add(informationNumberOfTimeLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfMessagesCaptionLabel =
                new JLabel("Total messages: ", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 0;
        gridBagConstraintsGeneralInformation.gridy = 3;
        informationGridBagPanel.add(informationNumberOfMessagesCaptionLabel,
                gridBagConstraintsGeneralInformation);
        
        informationNumberOfMessagesLabel =
                new JLabel("0", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
        gridBagConstraintsGeneralInformation.gridx = 1;
        gridBagConstraintsGeneralInformation.gridy = 3;
        informationGridBagPanel.add(informationNumberOfMessagesLabel,
                gridBagConstraintsGeneralInformation);
        
        /* Search result message. */
        resultLabel = new JLabel("", SwingConstants.CENTER);
        gridBagConstraintsGeneralInformation.gridx = 0;
        gridBagConstraintsGeneralInformation.gridy = 4;
        gridBagConstraintsGeneralInformation.gridwidth = 2;
        gridBagConstraintsGeneralInformation.insets = new Insets(20, 0, 0, 0);
        informationGridBagPanel.add(resultLabel,
                gridBagConstraintsGeneralInformation);
        gridBagConstraintsGeneralInformation.fill =
                GridBagConstraints.HORIZONTAL;
    }
    
    /**
     * Resets the various labels to their initial states.
     */
    public void resetLabels() {
        informationNumberOfMessagesLabel.setText(Integer.toString(0));
        informationNumberOfTimeLabel.setText(Integer.toString(0));
        resultLabel.setText("");
    }
    
    /**
     * Getter for inputedNetworkType.
     * 
     * @return the inputedNetworkType
     */
    public String getInputedNetworkType() {
        return inputedNetworkType;
    }
    
    /**
     * Setter for inputedNetworkType.
     * 
     * @param inputedNetworkType
     *            the inputedNetworkType to set
     */
    public void setInputedNetworkType(String inputedNetworkType) {
        this.inputedNetworkType = inputedNetworkType;
    }
    
    /**
     * Getter for inputednNodes.
     * 
     * @return the inputednNodes
     */
    public int getInputednNodes() {
        return inputednNodes;
    }
    
    /**
     * Getter for inputedSearch.
     * 
     * @return the inputedSearch
     */
    public String getInputedSearch() {
        return inputedSearch;
    }
    
    /**
     * Setter for inputedSearch.
     * 
     * @param inputedSearch
     *            the inputedSearch to set
     */
    public void setInputedSearch(String inputedSearch) {
        this.inputedSearch = inputedSearch;
    }
    
    /**
     * Getter for inputtedTTL.
     * 
     * @return the inputtedTTL
     */
    public int getInputtedTTL() {
        return inputedTTL;
    }
    
    /**
     * Setter for inputtedTTL.
     * 
     * @param inputtedTTL
     *            the inputtedTTL to set
     */
    public void setInputtedTTL(int inputtedTTL) {
        inputedTTL = inputtedTTL;
    }
    
    /**
     * Getter for inputedLinkDensity.
     * 
     * @return the inputedLinkDensity
     */
    public double getInputedLinkDensity() {
        return inputedLinkDensity;
    }
    
    /**
     * Getter for inputedBAInitialNodes.
     * 
     * @return the inputedBAInitialNodes
     */
    public int getInputedBAInitialNodes() {
        return inputedBAInitialNodes;
    }
    
    /**
     * Getter for inputedBALinksEachStep.
     * 
     * @return the inputedBALinksEachStep
     */
    public int getInputedBALinksEachStep() {
        return inputedBALinksEachStep;
    }
    
    /**
     * Getter for inputedBatchNumberOfSimulations.
     * 
     * @return the inputedBatchNumberOfSimulations
     */
    public int getInputedBatchNumberOfSimulations() {
        return inputedBatchNumberOfSimulations;
    }
    
    /**
     * Getter for inputedBatchSimulationType.
     * 
     * @return the inputedBatchSimulationType
     */
    public String getInputedBatchSimulationType() {
        return inputedBatchSimulationType;
    }
    
    /**
     * Getter for inputedSimulationType.
     * 
     * @return the inputedSimulationType
     */
    public String getInputedSimulationType() {
        return inputedSimulationType;
    }
    
    /**
     * Getter for networkERGraphString.
     * 
     * @return the networkERGraphString
     */
    public String getNetworkERGraphString() {
        return networkERGraphString;
    }
    
    /**
     * Getter for networkBAGraphString.
     * 
     * @return the networkBAGraphString
     */
    public String getNetworkBAGraphString() {
        return networkBAGraphString;
    }
    
    /**
     * Getter for networkRGGraphString.
     * 
     * @return the networkRGGraphString
     */
    public String getNetworkRGGraphString() {
        return networkRGGraphString;
    }
    
    /**
     * Getter for searchFloodString.
     * 
     * @return the searchFloodString
     */
    public String getSearchFloodString() {
        return searchFloodString;
    }
    
    /**
     * Getter for searchRWString.
     * 
     * @return the searchRWString
     */
    public String getSearchRWString() {
        return searchRWString;
    }
    
    /**
     * Getter for searchRRRWString.
     * 
     * @return the searchRRRWString
     */
    public String getSearchRRRWString() {
        return searchRRRWString;
    }
    
    /**
     * Getter for searchBidirectionalRWString.
     * 
     * @return the searchBidirectionalRWString
     */
    public String getSearchBidirectionalRWString() {
        return searchBidirectionalRWString;
    }
    
    /**
     * Getter for searchBidirectionalLinearString.
     * 
     * @return the searchBidirectionalLinearString
     */
    public String getSearchBidirectionalLinearString() {
        return searchBidirectionalLinearString;
    }
    
    /**
     * Getter for searchBidirectionalRRRWString.
     * 
     * @return the searchBidirectionalRRRWString
     */
    public String getSearchBidirectionalHybridString() {
        return searchBidirectionalRRRWString;
    }
    
    /**
     * Getter for searchParametersAlgorithmList.
     * 
     * @return the searchParametersAlgorithmList
     */
    public String[] getSearchParametersAlgorithmList() {
        return searchParametersAlgorithmList;
    }
    
    /**
     * Getter for simulationBatchString.
     * 
     * @return the simulationBatchString
     */
    public String getSimulationBatchString() {
        return simulationBatchString;
    }
    
    /**
     * Getter for informationNumberOfNodesLabel.
     * 
     * @return the informationNumberOfNodesLabel
     */
    public JLabel getInformationNumberOfNodesLabel() {
        return informationNumberOfNodesLabel;
    }
    
    /**
     * Getter for informationNumberOfLinksLabel.
     * 
     * @return the informationNumberOfLinksLabel
     */
    public JLabel getInformationNumberOfLinksLabel() {
        return informationNumberOfLinksLabel;
    }
    
    /**
     * Getter for informationNumberOfTimeLabel.
     * 
     * @return the informationNumberOfTimeLabel
     */
    public JLabel getInformationNumberOfTimeLabel() {
        return informationNumberOfTimeLabel;
    }
    
    /**
     * Getter for informationNumberOfMessagesLabel.
     * 
     * @return the informationNumberOfMessagesLabel
     */
    public JLabel getInformationNumberOfMessagesLabel() {
        return informationNumberOfMessagesLabel;
    }
    
    /**
     * Getter for resultLabel.
     * 
     * @return the resultLabel
     */
    public JLabel getResultLabel() {
        return resultLabel;
    }
    
    /**
     * Getter for sameNetworkSameSearchButton.
     * 
     * @return the sameNetworkSameSearchButton
     */
    public JButton getSameNetworkSameSearchButton() {
        return sameNetworkSameSearchButton;
    }
    
    /**
     * Getter for sameNetworkDifferentSearchButton.
     * 
     * @return the sameNetworkDifferentSearchButton
     */
    public JButton getSameNetworkDifferentSearchButton() {
        return sameNetworkDifferentSearchButton;
    }
    
    /**
     * Getter for batchSimulationSameNetworkSameSearchString.
     * 
     * @return the batchSimulationSameNetworkSameSearchString
     */
    public String getBatchSimulationSameNetworkSameSearchString() {
        return batchSimulationSameNetworkSameSearchString;
    }
    
    /**
     * Getter for batchSimulationSameNetworkDifferentSearchString.
     * 
     * @return the batchSimulationSameNetworkDifferentSearchString
     */
    public String getBatchSimulationSameNetworkDifferentSearchString() {
        return batchSimulationSameNetworkDifferentSearchString;
    }
    
    /**
     * Getter for batchSimulationDifferentNetworkDifferentSearchString.
     * 
     * @return the batchSimulationDifferentNetworkDifferentSearchString
     */
    public String getBatchSimulationDifferentNetworkDifferentSearchString() {
        return batchSimulationDifferentNetworkDifferentSearchString;
    }
    
    public String[] getBatchSimulationTypeList() {
        return batchSimulationTypeList;
    }
    
    /**
     * Getter for inputedInitialSearchState.
     * 
     * @return the inputedInitialSearchState
     */
    public String getInputedInitialSearchState() {
        return inputedInitialSearchState;
    }
    
    /**
     * Getter for initialSearchStateNeither.
     * 
     * @return the initialSearchStateNeither
     */
    public String getInitialSearchStateNeither() {
        return initialSearchStateNeither;
    }
    
    /**
     * Getter for initialSearchStateSave.
     * 
     * @return the initialSearchStateSave
     */
    public String getInitialSearchStateSave() {
        return initialSearchStateSave;
    }
    
    /**
     * Getter for initialSearchStateRestore.
     * 
     * @return the initialSearchStateRestore
     */
    public String getInitialSearchStateRestore() {
        return initialSearchStateRestore;
    }
    
    /**
     * Getter for initialSearchStateList.
     * 
     * @return the initialSearchStateList
     */
    public String[] getInitialSearchStateList() {
        return initialSearchStateList;
    }
    
    /**
     * Getter for inputedBatchSimulateAllStrategies.
     * 
     * @return the inputedBatchSimulateAllStrategies
     */
    public int getInputedBatchSimulateAllStrategies() {
        return inputedBatchSimulateAllStrategies;
    }
    
    /**
     * Getter for inputedBatchSimulateAllNetworkAndStrategies.
     * 
     * @return the inputedBatchSimulateAllNetworkAndStrategies
     */
    public int getInputedBatchSimulateAllNetworkAndStrategies() {
        return inputedBatchSimulateAllNetworkAndStrategies;
    }
    
    /**
     * Getter for networkParameterTypeList.
     * 
     * @return the networkParameterTypeList
     */
    public String[] getNetworkParameterTypeList() {
        return networkParameterTypeList;
    }
    
    /**
     * Getter for simulationInteractiveString.
     * 
     * @return the simulationInteractiveString
     */
    public String getSimulationInteractiveString() {
        return simulationInteractiveString;
    }
    
    /**
     * Getter for simulationAutomatedString.
     * 
     * @return the simulationAutomatedString
     */
    public String getSimulationAutomatedString() {
        return simulationAutomatedString;
    }
    
    /**
     * Setter for inputednNodes.
     * 
     * @param inputednNodes
     *            the inputednNodes to set
     */
    public void setInputednNodes(int inputednNodes) {
        this.inputednNodes = inputednNodes;
    }
    
    /**
     * Setter for inputedLinkDensity.
     * 
     * @param inputedLinkDensity
     *            the inputedLinkDensity to set
     */
    public void setInputedLinkDensity(double inputedLinkDensity) {
        this.inputedLinkDensity = inputedLinkDensity;
    }
    
    /**
     * Setter for inputedBAInitialNodes.
     * 
     * @param inputedBAInitialNodes
     *            the inputedBAInitialNodes to set
     */
    public void setInputedBAInitialNodes(int inputedBAInitialNodes) {
        this.inputedBAInitialNodes = inputedBAInitialNodes;
    }
    
    /**
     * Setter for inputedBALinksEachStep.
     * 
     * @param inputedBALinksEachStep
     *            the inputedBALinksEachStep to set
     */
    public void setInputedBALinksEachStep(int inputedBALinksEachStep) {
        this.inputedBALinksEachStep = inputedBALinksEachStep;
    }
    
}
