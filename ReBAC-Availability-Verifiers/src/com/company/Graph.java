package com.company;

import com.google.common.collect.HashBiMap;

import java.util.Set;
import java.util.TreeSet;

public class Graph {

    protected static int labelSetSize;
    protected Set<Integer> vertices;
    protected Set<Integer> labels;
    protected Set<Integer>[][] outEdges;
    protected Set<Integer>[][] inEdges;
    //private HashBiMap<Integer, Integer> verticesTable;

    /*
    public void setVerticesTable(HashBiMap<Integer, Integer> verticesTable) {
        this.verticesTable = verticesTable;
    }

    public HashBiMap<Integer, Integer> getVerticesTable() {
        return verticesTable;
    }
    */

    public Graph() {}

    public Graph(Graph graph) {
        this.vertices = new TreeSet<Integer>();
        this.labels = new TreeSet<Integer>();

        this.vertices.addAll(graph.vertices);
        this.labels.addAll(graph.labels);

        outEdges = new Set[labelSetSize][vertices.size()];
        inEdges = new Set[labelSetSize][vertices.size()];

        for (int l = 0; l < labelSetSize; l++) {
            for (int v = 0; v < vertices.size(); v++) {
                outEdges[l][v] = new TreeSet<Integer>();
                inEdges[l][v] = new TreeSet<Integer>();
            }
        }

        for (int l = 0; l < graph.labels.size(); l++) {
            for (int i = 0; i < graph.vertices.size(); i++) {
                this.inEdges[l][i].addAll(graph.inEdges[l][i]);
                this.outEdges[l][i].addAll(graph.outEdges[l][i]);
            }
        }

        /*
        int counter;

        verticesTable = HashBiMap.create();
        counter = 0;
        for (int v : vertices) {
            verticesTable.put(v, counter);
            counter++;
        }
        */
    }

    public Graph(Set<Integer> vertices, Set<Integer> labels) {
        this.vertices = new TreeSet<Integer>();
        this.labels = new TreeSet<Integer>();
        this.vertices.addAll(vertices);
        this.labels.addAll(labels);
        outEdges = new Set[labelSetSize][vertices.size()];
        inEdges = new Set[labelSetSize][vertices.size()];

        for (int l = 0; l < labelSetSize; l++) {
            for (int v = 0; v < vertices.size(); v++) {
                outEdges[l][v] = new TreeSet<Integer>();
                inEdges[l][v] = new TreeSet<Integer>();
            }
        }

        /*
        int counter;

        verticesTable = HashBiMap.create();
        counter = 0;
        for (int v : vertices) {
            verticesTable.put(v, counter);
            counter++;
        }
        */
    }

    public void addEdge(int l, int v, int w) {
        //outEdges[l][verticesTable.get(v)].add(verticesTable.get(w));
        //inEdges[l][verticesTable.get(w)].add(verticesTable.get(v));
        outEdges[l][v].add(w);
        inEdges[l][w].add(v);
    }

    public void removeEdge(int l, int v, int w) {
        //outEdges[l][verticesTable.get(v)].add(verticesTable.get(w));
        //inEdges[l][verticesTable.get(w)].add(verticesTable.get(v));
        outEdges[l][v].remove(w);
        inEdges[l][w].remove(v);
    }

    public void addVertex(int v) {
        vertices.add(v);
        //verticesTable.put(v, vertices.size()-1);
        Set<Integer> [][] outTemp = new Set[labels.size()][vertices.size()];
        Set<Integer> [][] inTemp = new Set[labels.size()][vertices.size()];
        for (int l = 0; l < labels.size(); l++) {
            for (int i = 0; i < vertices.size()-1; v++) {
                outTemp[l][i] = outEdges[l][i];
                inTemp[l][i] = inEdges [l][i];
            }
        }
        for (int l = 0; l < labels.size(); l++) {
            outTemp[l][vertices.size()-1] = new TreeSet<Integer>();
            inTemp[l][vertices.size()-1] = new TreeSet<Integer>();
        }
        outEdges = outTemp;
        inEdges = inTemp;
    }

}

