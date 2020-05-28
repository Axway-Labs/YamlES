package com.axway.gw.es.yaml.dto.entity;

import java.util.Objects;

public class RoutingDTO {

    public static final String SUCCESS_NODE = "successNode";
    public static final String FAILURE_NODE = "failureNode";

    private String successNode;
    private String failureNode;

    public String getSuccessNode() {
        return successNode;
    }

    public RoutingDTO setSuccessNode(String successNode) {
        this.successNode = successNode;
        return this;
    }

    public String getFailureNode() {
        return failureNode;
    }

    public RoutingDTO setFailureNode(String failureNode) {
        this.failureNode = failureNode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoutingDTO that = (RoutingDTO) o;
        return Objects.equals(successNode, that.successNode) &&
                Objects.equals(failureNode, that.failureNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successNode, failureNode);
    }

    @Override
    public String toString() {
        return "RoutingDTO{" +
                "successNode='" + successNode + '\'' +
                ", failureNode='" + failureNode + '\'' +
                '}';
    }
}
