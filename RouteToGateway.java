// imports
import java.util.ArrayList;
import java.util.Scanner;

public class RouteToGateway {

	// user input to generate AS topology
	private static int numVertices;
	private static int[][] adjMatrix;
	private static ArrayList<Integer> gatewayRouters;
	private static int numGatewayRouters;

	// list of vertices --> N
	private static ArrayList<Integer> totalNodes;

	public static void main(String[] args) {

		// initialize scanner object
		Scanner sc = new Scanner(System.in);

		// gather user input for AS properties
		System.out.println("Welcome to link-state intra-AS route to gateway generator!");
		System.out.println("Forwarding tables generated using Dijkstra's algorithm.");
		System.out.println("-----------------------------------------------------------------");
		System.out.print("Enter number of vertices (n): ");
		numVertices = sc.nextInt();

		// initialize total nodes array list
		totalNodes = new ArrayList<>(numVertices);

		// initialize size of adjacency matrix
		adjMatrix = new int[numVertices][numVertices];

		// build the matrix
		System.out.println("Enter " + numVertices + " lines representing each row of adjacency matrix with weights: ");
		for (int i = 0; i < numVertices; i++) {
			// add node to total nodes
			totalNodes.add(i);
			for (int j = 0; j < numVertices; j++) {
				adjMatrix[i][j] = sc.nextInt();
			}
		}
		System.out.println("-".repeat(numVertices*4));

		// get number of gateway routers
		System.out.print("Enter number of gateway routers: ");
		numGatewayRouters = sc.nextInt();

		// build list of gateway routers
		System.out.print("Enter " + numGatewayRouters + " vertices representing a gateway router: ");
		gatewayRouters = new ArrayList<>(numGatewayRouters);
		for (int i = 0; i < numGatewayRouters; i++) {
			gatewayRouters.add(sc.nextInt());
		}
		System.out.println("-----------------------------------------------------------------");

		// display inputed matrix
		System.out.println("Inputed Matrix: ");
		for (int i = 1; i <= numVertices; i++) {
			System.out.print(String.format("%4d", i));
		}

		System.out.println();
		System.out.println("-".repeat(numVertices*4));

		for(int row = 0; row < adjMatrix.length; row++) {
			System.out.print(row + 1 + "|");
			for (int col = 0; col < adjMatrix.length; col++) {
				if (col != 0)
					System.out.print(String.format("%4d", adjMatrix[row][col]));
				else
					System.out.print(String.format("%2d", adjMatrix[row][col]));
			}
			System.out.println();
		}
		// display gateway routers
		System.out.println();
		System.out.print("Gateway routers at vertices: ");
		for (int i = 0; i <gatewayRouters.size(); i++) {
			System.out.print(gatewayRouters.get(i));
			if (i != gatewayRouters.size()-1)
				System.out.print(", ");
		}
		System.out.println();
		System.out.println("-----------------------------------------------------------------");

		// routingAlgorithm -> calls computePath for all source nodes (calls generateTable)
		routingAlgorithm();

		// close scanner
		sc.close();
	}

	private static void routingAlgorithm() {

		// computes paths for all nodes if not a gateway router
		// prints forwarding table for each source node
		for (int sourceNode = 0; sourceNode < adjMatrix.length; sourceNode++) {
			if (!gatewayRouters.contains(sourceNode + 1)) {
				ArrayList<ArrayList<Integer>> destCost = computePath(sourceNode);
				generateTable(sourceNode, destCost);
			}
		}
	}

	private static ArrayList<ArrayList<Integer>> computePath(int sourceNode) {

		// initialize subset of nodes --> N'
		ArrayList<Integer> subsetNodes = new ArrayList<>(numVertices);
		subsetNodes.add(sourceNode);

		// 2-D list to store least-cost path and next hop for all v --> [D(v), p(v)]
		ArrayList<ArrayList<Integer> > destData = new ArrayList<ArrayList<Integer>>(numVertices);  
		for (int i = 0; i < numVertices; i++) {
			destData.add(new ArrayList<Integer>()); 
		}

		// for all nodes v
		for (int v = 0; v < numVertices; v++) {
			// if v is a neighbor of sourceNode
			if (adjMatrix[sourceNode][v] != -1 && adjMatrix[sourceNode][v] != 0) {
				destData.get(v).add(adjMatrix[sourceNode][v]);  
				destData.get(v).add(v + 1);
			} else {
				destData.get(v).add(-1); 
				destData.get(v).add(-1);  
			} 
		}
		// set source node distance to zero
		destData.get(sourceNode).set(0, 0);

		// iterate until subset nodes contains all nodes
		while (!subsetNodes.containsAll(totalNodes)) {

			int wNeighbor = -1;
			// current minimum initialization
			int currMin = -1;
			
			// find w not in subset nodes such that D(w) is a minimum --> then add w to N'
			for (int w = 0; w < numVertices; w++) {
				if (!subsetNodes.contains(w)) {
					if ((currMin == -1) || (destData.get(w).get(0) != -1 && destData.get(w).get(0) < currMin)) {
						currMin = destData.get(w).get(0);
						wNeighbor = w;
					}
				}
			}
			// add w to N'
			subsetNodes.add(wNeighbor);

			// update D(v) for each neighbor v of w and not in N'
			for (int v = 0; v < numVertices; v++) {
				// neighbor v of w not already in node subset and v is a neighbor of w
				if (!subsetNodes.contains(v) && adjMatrix[wNeighbor][v] != -1) {

					// compute new distance from source to destination v
					int newDist = destData.get(wNeighbor).get(0) + adjMatrix[wNeighbor][v];
					// update cost if new minimum found
					if (newDist < destData.get(v).get(0) || destData.get(v).get(0) == -1) {
						destData.get(v).set(0, newDist); 
						// drop all previous next hops
						destData.get(v).subList(1, destData.get(v).size()).clear();
						// update next hop router for destination v
						destData.get(v).add(destData.get(wNeighbor).get(1));  
						
					// add additional next hop router
					} else if (newDist == destData.get(v).get(0)) {
						destData.get(v).add(destData.get(wNeighbor).get(1)); 
					}
				}
			}
		}
		return destData; 
	}

	private static void generateTable(int sourceNode, ArrayList<ArrayList<Integer>> destCost) {

		// display table header
		System.out.println("Forwarding Table for Source Node " + (sourceNode + 1));
		System.out.println("-----------------------------------------------------------------");
		System.out.print(String.format("%10s", "To"));
		System.out.print(String.format("%10s", "Cost"));
		System.out.print(String.format("%10s", "Next Hop"));
		System.out.print(String.format("%20s", "Alt. Next Hop(s)"));
		System.out.println();

		// display data row for each gateway router
		for (Integer gatewayRouter : gatewayRouters) {
			System.out.print(String.format("%10d", gatewayRouter));
			System.out.print(String.format("%10d", destCost.get(gatewayRouter-1).get(0)));
			System.out.print(String.format("%10d", destCost.get(gatewayRouter-1).get(1)));
			// print all possible next hop routers, none if only one exists
			if (destCost.get(gatewayRouter-1).size() <= 2)
				System.out.print(String.format("%14s", "n/a"));
			for (int i = 2; i < destCost.get(gatewayRouter-1).size(); i++) {
				if (i == 2)
					System.out.print(String.format("%13d", destCost.get(gatewayRouter-1).get(i)));
				else
					System.out.print(destCost.get(gatewayRouter-1).get(i));
				if (i != destCost.get(gatewayRouter-1).size()-1)
					System.out.print(", ");
			}
			System.out.println();
		}
		System.out.println("-----------------------------------------------------------------");
	}
}

