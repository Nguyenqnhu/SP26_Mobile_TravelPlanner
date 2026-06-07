package com.example.weathertrip_sep490.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RouteOption {
    @SerializedName("routeId")
    private String routeId;

    @SerializedName("routeIndex")
    private int routeIndex;

    @SerializedName("totalDistanceKm")
    private double totalDistanceKm;

    @SerializedName("nodes")
    private List<String> nodes;

    @SerializedName("nodeIds")
    private List<String> nodeIds;

    @SerializedName("polyline")
    private List<RoutePolylinePointDto> polyline;

    public RouteOption() {}

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public int getRouteIndex() {
        return routeIndex;
    }

    public void setRouteIndex(int routeIndex) {
        this.routeIndex = routeIndex;
    }

    public double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public List<String> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(List<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

    public List<RoutePolylinePointDto> getPolyline() {
        return polyline;
    }

    public void setPolyline(List<RoutePolylinePointDto> polyline) {
        this.polyline = polyline;
    }
}
