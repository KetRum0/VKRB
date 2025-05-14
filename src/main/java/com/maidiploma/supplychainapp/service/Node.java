package com.maidiploma.supplychainapp.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Node {

    List<Long> children;
    List<Long> parents;
    public double cost;
    public double h;
    public int c;
    public int cumulativeCost;
    public int t;
    public int M;
    public double stdDev;
    public Long nodeId;

    public Node(Long nodeId) {
        this.children = new ArrayList<>();
        this.parents = new ArrayList<>();
        this.cost = 0;
        this.cumulativeCost = 0;
        this.h = 0;
        this.t = 0;
        this.M = 0;
        this.stdDev = 0;
        this.nodeId = nodeId;
    }

    public Node(List<Long> children, List<Long> parents, double cost, double h, int c, int cumulativeCost, int t, int m, double stdDev, Long nodeId) {
        this.children = children;
        this.parents = parents;
        this.cost = cost;
        this.h = h;
        this.c = c;
        this.cumulativeCost = cumulativeCost;
        this.t = t;
        M = m;
        this.stdDev = stdDev;
        this.nodeId = nodeId;
    }

    public Node(Node other) {
        this.children = new ArrayList<>(other.children);
        this.parents = new ArrayList<>(other.parents);
        this.cost = other.cost;
        this.h = other.h;
        this.c = other.c;
        this.cumulativeCost = other.cumulativeCost;
        this.t = other.t;
        this.M = other.M;
        this.stdDev = other.stdDev;
        this.nodeId = other.nodeId;
    }
    @Override
    public String toString() {
        return "Node{" +
                "children=" + children +
                ", parents=" + parents +
                ", cost=" + cost +
                ", h=" + h +
                ", c=" + c +
                ", cumulativeCost=" + cumulativeCost +
                ", t=" + t +
                ", M=" + M +
                ", stdDev=" + stdDev +
                ", nodeId=" + nodeId +
                '}';
    }
}
