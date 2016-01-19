package com.company;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Pooya on 14-12-10.
 */
public class GraphGenerator {

    public GraphGenerator() {
    }

    public Graph genErdosRenyi(int numVertices, int numLabels, double probability) {
        if (probability < 0.0 || probability > 1.0)
            throw new IllegalArgumentException("Probability must be between 0 and 1");

        Set<Integer> vertices = new TreeSet<Integer>();
        for (int i = 0; i < numVertices; i++) {
            vertices.add(i);
        }

        Set<Integer> labels = new TreeSet<Integer>();
        for (int i = 0; i < numLabels; i++) {
            labels.add(i);
        }


        Graph graph = new Graph(vertices, labels);

        for (int v = 0; v < numVertices; v++)
            for (int w = 0; w < numVertices; w++)
                if (RandomUtil.random.nextDouble() < probability){
                    int l = RandomUtil.random.nextInt(numLabels);
                    graph.addEdge(l, v, w);
                }
        return graph;

    }

    public boolean _validate(Graph graph, int anchor, int requester) {
        Set<Integer> anchorAccess = new TreeSet<Integer>();
        Set<Integer> requesterAccess = new TreeSet<Integer>();
        Set[] edges = new Set[graph.vertices.size()];
        for (int i = 0; i < graph.vertices.size(); i++) {
            edges[i] = new TreeSet<Integer>();
            for (int l = 0; l < Graph.labelSetSize; l++) {
                edges[i].addAll(graph.inEdges[l][i]);
                edges[i].addAll(graph.outEdges[l][i]);
            }
        }
        dfs(edges, anchorAccess, anchor);
        dfs(edges, requesterAccess, requester);
        anchorAccess.addAll(requesterAccess);
        return anchorAccess.containsAll(graph.vertices);
    }

    public boolean validate(Graph graph, int anchor, int requester) {
        Set<Integer> anchorAccess = new TreeSet<Integer>();
        Set<Integer> requesterAccess = new TreeSet<Integer>();
        Set[] edges = new Set[graph.vertices.size()];
        for (int i = 0; i < graph.vertices.size(); i++) {
            edges[i] = new TreeSet<Integer>();
            for (int l = 0; l < Graph.labelSetSize; l++) {
                edges[i].addAll(graph.inEdges[l][i]);
                edges[i].addAll(graph.outEdges[l][i]);
            }
        }
        dfs(edges, anchorAccess, anchor);
        dfs(edges, requesterAccess, requester);
        return (anchorAccess.containsAll(graph.vertices) && requesterAccess.containsAll(graph.vertices));
    }


    public void dfs(Set<Integer>[] edges, Set<Integer> visited, Integer v) {
        visited.add(v);
        if (v != null) {
            for (int w : edges[v]) {
                if (!visited.contains(w)) {
                    dfs(edges, visited, w);
                }
            }
        }
    }

    public void removeEdges(Graph graph, double probability) {
        if (probability < 0.0 || probability > 1.0)
            throw new IllegalArgumentException("Probability must be between 0 and 1");
        for (int l = 0; l < Graph.labelSetSize; l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                Set<Integer> vertices = new TreeSet<Integer>(graph.outEdges[l][i]);
                for (int v : vertices) {
                    if (RandomUtil.random.nextDouble() < probability) {
                        graph.outEdges[l][i].remove(v);
                        graph.inEdges[l][v].remove(i);
                    }
                }
            }
        }
    }

}
