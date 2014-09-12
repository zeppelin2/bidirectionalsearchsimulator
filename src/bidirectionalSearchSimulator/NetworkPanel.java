package bidirectionalSearchSimulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * NetworkPanel is a class that displays the state of the network
 * (i.e. nodes and links).
 */
public class NetworkPanel extends JPanel implements KeyListener, MouseListener {
    
    private static final long serialVersionUID = 1L;
    
    private final SearchCoordinator searchCoordinator;
    private NetworkStructurer networkStructurer;
    private Search search;
    private final int xDim = 700;    // network panel width
    private final int yDim = 700;    // network panel height
    private final int nodeSize = 6;  // diameter of a node in pixels
    private final int nodeSize2;     // another diameter of a node in pixels, used
    
    // for the source and destination to make them
    // stand out
    
    /**
     * Constructor.
     * 
     * @param pSearchCoordinator
     *            the search coordinator that created this instance of
     *            NetworkPanel
     */
    public NetworkPanel(final SearchCoordinator pSearchCoordinator) {
        searchCoordinator = pSearchCoordinator;
        setPreferredSize(new Dimension(xDim, yDim));
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        nodeSize2 = nodeSize + 3;
        setBorder(BorderFactory.createLineBorder(Color.black));
    }
    
    /**
     * Calls the UI's paint method.
     */
    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        
        search = searchCoordinator.getSearch();
        
        /* Checks to make sure that it's okay to draw the network. */
        if (searchCoordinator.getFlagOKToDrawNetwork() == 1) {
            networkStructurer = searchCoordinator.getNetworkStructurer();
            
            /*
             * =============================================================
             * Draws all links.
             * =============================================================
             */
            g.setColor(new Color(100, 200, 250)); // blue
            
            /* Iterates over each node in the set of network nodes. */
            Iterator<Node> itNode = networkStructurer.getNodeList().iterator();
            Node aCurrentNode;
            
            while (itNode.hasNext()) {
                aCurrentNode = itNode.next();
                
                /* Iterates over each link in the set of links of the current node. */
                final Iterator<Link> itLink =
                        aCurrentNode.getLinksSet().iterator();
                Link aCurrentLink;
                
                while (itLink.hasNext()) {
                    aCurrentLink = itLink.next();
                    
                    g.drawLine(
                            (int) Math
                                    .round((networkStructurer
                                            .getNodeLocationMap().get(
                                                    aCurrentLink
                                                            .getSourceNodeID())[0] * xDim)),
                            (int) Math
                                    .round((networkStructurer
                                            .getNodeLocationMap().get(
                                                    aCurrentLink
                                                            .getSourceNodeID())[1] * yDim)),
                            (int) Math
                                    .round((networkStructurer
                                            .getNodeLocationMap()
                                            .get(aCurrentLink
                                                    .getDestinationNodeID())[0] * xDim)),
                            (int) Math
                                    .round((networkStructurer
                                            .getNodeLocationMap()
                                            .get(aCurrentLink
                                                    .getDestinationNodeID())[1]) *
                                            yDim));
                }
            }
            
            /*
             * =============================================================
             * Draws all network nodes.
             * =============================================================
             */
            g.setColor(Color.black);
            
            /* Iterates over each node in the set of network nodes. */
            itNode = networkStructurer.getNodeList().iterator();
            
            while (itNode.hasNext()) {
                aCurrentNode = itNode.next();
                
                g.fillOval(((int) Math
                        .round((networkStructurer.getNodeLocationMap().get(
                                aCurrentNode.getNodeID())[0] * xDim) -
                                (nodeSize / 2))), ((int) Math
                        .round((networkStructurer.getNodeLocationMap().get(
                                aCurrentNode.getNodeID())[1] * yDim) -
                                (nodeSize / 2))), nodeSize, nodeSize);
                
                /* Draws node IDs next to each node. */
//                g.drawString(Integer.toString(aCurrentNode.getNodeID()),
//                    (int) Math.round((networkStructurer.getNodeLocationMap()
//                        .get(aCurrentNode.getNodeID())[0] * xDim) -
//                        (nodeSize / 2)), (int) Math
//                        .round((networkStructurer.getNodeLocationMap().get(
//                            aCurrentNode.getNodeID())[1] * yDim) -
//                            (nodeSize / 2)));
            }
            
            /*
             * The process is split into two iterations so that the current nodes
             * positions are always drawn on top of the other node types (so that
             * they remain visible throughout the search and are thus easier to
             * follow). The same goes for source and target nodes on top of
             * regular nodes.
             */
            for (int iQuery = 0; iQuery < search.getSearchQueriesMap().size(); iQuery++) {
                final Query currentQuery =
                        search.getSearchQueriesMap().get(iQuery);
                
                /*
                 * =============================================================
                 * Draws visited nodes.
                 * =============================================================
                 */
                g.setColor(new Color(150, 150, 150)); // grey
                
                /* Iterates over each node in the set of visited nodes. */
                final Iterator<Node> itVisitedNodes =
                        currentQuery.getVisitedNodesSet().iterator();
                Node aVisitedNode;
                
                while (itVisitedNodes.hasNext()) {
                    aVisitedNode = itVisitedNodes.next();
                    
                    g.fillOval(((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    aVisitedNode.getNodeID())[0] * xDim) -
                                    (nodeSize / 2))), ((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    aVisitedNode.getNodeID())[1] * yDim) -
                                    (nodeSize / 2))), nodeSize, nodeSize);
                }
            }
            
            for (int iQuery = 0; iQuery < search.getSearchQueriesMap().size(); iQuery++) {
                final Query currentQuery =
                        search.getSearchQueriesMap().get(iQuery);
                
                /*
                 * Only draws source and target nodes for the first two queries.
                 * This is so that, in particular, the bidirectional hybrid
                 * search, which replicates random walk queries, does not end
                 * up drawing source and target nodes all over the map (as each
                 * replicated query considers its point of replication as its
                 * source).
                 */
                if (iQuery < search.getnQueries()) {
                    /*
                     * =========================================================
                     * Draws source node.
                     * =========================================================
                     */
                    g.setColor(new Color(0, 240, 0)); // green
                    
                    final Node sourceNode = currentQuery.getSourceNode();
                    
                    g.fillOval(((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    sourceNode.getNodeID())[0] * xDim) -
                                    (nodeSize2 / 2))), ((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    sourceNode.getNodeID())[1] * yDim) -
                                    (nodeSize2 / 2))), nodeSize2 +
                            Math.round(nodeSize2 / 2), nodeSize2 +
                            Math.round(nodeSize2 / 2));
                    
                    /*
                     * =========================================================
                     * Draws target nodes.
                     * =========================================================
                     */
                    g.setColor(new Color(215, 0, 0)); // red
                    
                    final Iterator<Node> itTargetNode =
                            currentQuery.getTargetNodesSet().iterator();
                    
                    while (itTargetNode.hasNext()) {
                        aCurrentNode = itTargetNode.next();
                        
                        g.fillOval(
                                ((int) Math
                                        .round((networkStructurer
                                                .getNodeLocationMap().get(
                                                        aCurrentNode
                                                                .getNodeID())[0] * xDim) -
                                                (nodeSize2 / 2))),
                                ((int) Math
                                        .round((networkStructurer
                                                .getNodeLocationMap().get(
                                                        aCurrentNode
                                                                .getNodeID())[1] * yDim) -
                                                (nodeSize2 / 2))), nodeSize2 +
                                        Math.round(nodeSize2 / 2), nodeSize2 +
                                        Math.round(nodeSize2 / 2));
                    }
                }
            }
            
            for (int iQuery = 0; iQuery < search.getSearchQueriesMap().size(); iQuery++) {
                final Query currentQuery =
                        search.getSearchQueriesMap().get(iQuery);
                /*
                 * =============================================================
                 * Draws current nodes.
                 * =============================================================
                 */
                g.setColor(new Color(255, 110, 160)); // pink
                
                final Iterator<Node> itCurrentNodes =
                        currentQuery.getCurrentNodesSet().iterator();
                
                while (itCurrentNodes.hasNext()) {
                    aCurrentNode = itCurrentNodes.next();
                    
                    g.fillOval(((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    aCurrentNode.getNodeID())[0] * xDim) -
                                    (nodeSize / 2))), ((int) Math
                            .round((networkStructurer.getNodeLocationMap().get(
                                    aCurrentNode.getNodeID())[1] * yDim) -
                                    (nodeSize / 2))), nodeSize2, nodeSize2);
                }
            }
        }
        
        g.dispose();
    }
    
    /**
     * KeyListener's key pressed event.
     */
    @Override
    public void keyPressed(final KeyEvent arg0) {
        /*
         * Only reacts to a key press once the network has been initiated. This
         * is to prevent errors while the control panel has been drawn but the
         * search classes have not yet been created.
         */
        if (searchCoordinator.getFlagOKToDrawNetwork() == 1) {
            searchCoordinator.doSearchInteractive();
        }
    }
    
    /**
     * KeyListener's key released event.
     */
    @Override
    public void keyReleased(final KeyEvent arg0) {
    }
    
    /**
     * KeyListener's key typed event.
     */
    @Override
    public void keyTyped(final KeyEvent arg0) {
    }
    
    /**
     * Captures mouse presses.
     */
    @Override
    public void mousePressed(final MouseEvent e) {
        /* Sets focus on the network panel so that it can respond to key presses. */
        requestFocus();
    }
    
    /* Other mouse capture functions. */
    @Override
    public void mouseReleased(final MouseEvent e) {
    }
    
    @Override
    public void mouseClicked(final MouseEvent e) {
    }
    
    @Override
    public void mouseEntered(final MouseEvent e) {
    }
    
    @Override
    public void mouseExited(final MouseEvent e) {
    }
    
    /**
     * Getter for xDim.
     * 
     * @return the xDim
     */
    public int getxDim() {
        return xDim;
    }
    
    /**
     * Getter for yDim.
     * 
     * @return the yDim
     */
    public int getyDim() {
        return yDim;
    }
    
}
