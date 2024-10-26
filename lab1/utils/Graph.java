package lab1.utils;
import java.util.ArrayList;
import java.util.LinkedList;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class Graph {
    private class Node {
        private int val;
        private ArrayList<Node> outwards;
        private ArrayList<Node> inwards;

        public Node(int val) {
            this.val = val;
            this.outwards = new ArrayList<Node>();
            this.inwards = new ArrayList<Node>();
        }

        public void addOutward(Node node) {
            this.outwards.add(node);
        }

        /**
        public void rmOutward(Node node) {
            this.outwards.remove(node);
        }
        */

        public void addInward(Node node) {
            this.inwards.add(node);
        }

        public void rmInward(Node node) {
            this.inwards.remove(node);
        }

        public int getVal() {
            return this.val;
        }

        public ArrayList<Node> getOutwards() {
            return this.outwards;
        }

        public ArrayList<Node> getInwards() {
            return this.inwards;
        }

        public String toString() {
            String res;
            res = "Node " + this.val + ":\n";
            res += "  Outwards: ";
            for(Node node : this.outwards) {
                res += node.getVal() + " ";
            }
            res += "\n";
            res += "  Inwards: ";
            for(Node node : this.inwards) {
                res += node.getVal() + " ";
            }
            res += "\n";
            return res;
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private Map<Integer, Node> nodes;

    private Graph() {
        this.nodes = new HashMap<Integer, Node>();
    }

    private void addNode(int val) {
        Node node = this.nodes.get(val);
        if(node == null) {
            node = new Node(val);
            this.nodes.put(val, node);
        }
    }

    private void addDependence(int from, int to) {
        Node fromNode = this.nodes.get(from);
        Node toNode = this.nodes.get(to);
        if(fromNode == null) {
            fromNode = new Node(from);
            this.nodes.put(from, fromNode);
        }
        if(toNode == null) {
            toNode = new Node(to);
            this.nodes.put(to, toNode);
        }
        fromNode.addOutward(toNode);
        toNode.addInward(fromNode);
    }

    /**
    private void rmDependence(int from, int to) {
        Node fromNode = this.nodes.get(from);
        Node toNode = this.nodes.get(to);
        if(fromNode == null || toNode == null) {
            return;
        }
        fromNode.rmOutward(toNode);
        toNode.rmInward(fromNode);
    }
    */

    private Graph copyGraph() {
        Graph graph = new Graph();
        for (Node node : this.nodes.values()) {
            graph.addNode(node.getVal());
        }

        for(Node node : this.nodes.values()) {
            for(Node n : node.getOutwards()) {
                graph.addDependence(node.getVal(), n.getVal());
            }
        }
        return graph;
    }

    ////////////////////////////////////////////////////////////////////////////

    public static Graph fromOpHist(ArrayList<Operation> opHist) {
        Graph graph = new Graph();
        // Integer: txIdx; Node: node
        // Initialize the graph with all the transactions
        for(Operation op : opHist) {
            graph.addNode(op.getFromTxIdx());
        }

        // Record the last operation of each transaction on certain row
        // Map<Integer, Map<Integer, Operation>>: <rowNumber, txIdx, lastOp>
        Map<Integer, Map<Integer, Operation>> lastSignificantOp = new HashMap<Integer, Map<Integer, Operation>>();
        for(Operation op : opHist) {
            int txIdx = op.getFromTxIdx();
            int rowNumber = op.getRowNumber();
            int type = op.getType();

            // Write > Read
            {
                Operation lastOp = lastSignificantOp.get(rowNumber) == null ? null : lastSignificantOp.get(rowNumber).get(txIdx);
                if(lastOp == null || lastOp.getType() == Operation.OP_READ) {
                    lastSignificantOp.putIfAbsent(rowNumber, new HashMap<Integer, Operation>());
                    lastSignificantOp.get(rowNumber).put(txIdx, op);
                }
            }

            // Build the graph
            // Read -> Write, Write -> Write, Write -> Read
            for (Map.Entry<Integer, Operation> entry : lastSignificantOp.get(rowNumber).entrySet()) {
                if (entry.getKey() == txIdx) {
                    continue;
                }

                Operation lastOp = entry.getValue();
                if (lastOp.getType() == Operation.OP_READ && type == Operation.OP_READ) {
                    continue;
                }

                graph.addDependence(lastOp.getFromTxIdx(), txIdx);
            }
        }

        return graph;
    }

    public static ArrayList<Integer> topologicalSort(Graph graph) {
        Graph g = graph.copyGraph();
        ArrayList<Integer> res = new ArrayList<Integer>();

        // Find all nodes with no inwards
        Queue<Node> queue = new LinkedList<Node>();
        for(Node node : g.nodes.values()) {
            if(node.getInwards().size() == 0) {
                queue.add(node);
            }
        }

        // Topological sort
        HashSet<Node> visited = new HashSet<Node>();
        while(!queue.isEmpty()) {
            Node node = queue.poll();
            res.add(node.getVal());
            visited.add(node);
            for(Node n : node.getOutwards()) {
                n.rmInward(node);
                if(n.getInwards().size() == 0) {
                    queue.add(n);
                }
            }
        }

        // If there is a cycle, return null
        for(Node node : g.nodes.values()) {
            if(node.getInwards().size() != 0) {
                return null;
            }
        }

        // If there is still some nodes not visited, give a random order
        for(Node node : g.nodes.values()) {
            if(!visited.contains(node)) {
                res.add(node.getVal());
            }
        }


        return res;
    }

    ////////////////////////////////////////////////////////////////////////////

    public String toString() {
        String res = "";
        for(Node node : this.nodes.values()) {
            res += node.toString();
        }
        return res;
    }
}
