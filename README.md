bidirectionalsearchsimulator
============================

The Bidirectional Search Simulator created as part of my bachelor project on bidirectional network search algorithms

The following folders are included:

/src 

    The source code for the project. The main entry point into the program is the "main" method in "SearchCoordinator.java".

/jar 

    The executable jar file for the Bidirectional Search Simulator.

/report 

    The bachelor project report which discusses the results obtained from simulations run using the Bidirectional Search Simulator.

A quick overview of how to obtain some results is as follows:

1. Set the Network Parameters (choose a network type and enter its properties)

2. Set the Search Parameters (choose a search method and enter its properties)

3. Select the Simulation Type ("Interactive" awaits a keypress to perform the next search step; "Automated" runs the whole search in one step; "Batch mode" was used to produce all the results for my project in one go and should be avoided as it's a bit complicated and not required to play with the program itself)

4. Choose from one of the three available buttons: "Different network, different search" generates a new network based on the parameters set in step 1 and randomly chooses the Source and Destination nodes within this network. This is the only choice available when the program is first started. "Same network, different search" reuses the current network but randomly chooses new Source and Destination nodes. "Same network, same search" reuses both the current network and the current Source and Destination nodes.

Normal nodes show up as black dots, the Source node is green, the Destination node is red, and the node at which the search currently resides is pink. Links (i.e. connections) between nodes are shown as blue lines.

Information regarding the search status is shown in the Information section.
