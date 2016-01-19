package com.company;

public class BirootedGraph extends Graph {

    protected final Integer anchor;
    protected final Integer requester;

    public BirootedGraph(Graph graph, Integer anchor, Integer requester) {
        this.anchor = anchor;
        this.requester = requester;
        this.vertices = graph.vertices;
        this.outEdges = graph.outEdges;
        this.inEdges = graph.inEdges;
        //this.setVerticesTable(graph.getVerticesTable());
        this.labels = graph.labels;
    }

}
