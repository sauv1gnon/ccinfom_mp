package com.ccinfoms17grp2.ui;

import com.ccinfoms17grp2.models.BranchWithDoctors;
import com.gluonhq.maps.MapLayer;
import com.gluonhq.maps.MapPoint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Pair;

import java.util.List;

public class BranchMapLayer extends MapLayer {
    
    private final ObservableList<Pair<MapPoint, Node>> points = FXCollections.observableArrayList();
    private final List<BranchWithDoctors> branches;
    private final double patientLat;
    private final double patientLon;

    public BranchMapLayer(List<BranchWithDoctors> branches, double patientLat, double patientLon) {
        this.branches = branches;
        this.patientLat = patientLat;
        this.patientLon = patientLon;
        
        addBranchMarkers();
        addPatientMarker();
    }

    private void addBranchMarkers() {
        for (BranchWithDoctors branch : branches) {
            if (branch.getBranch().getLatitude() == null || branch.getBranch().getLongitude() == null) {
                continue;
            }
            
            MapPoint branchPoint = new MapPoint(
                branch.getBranch().getLatitude(),
                branch.getBranch().getLongitude()
            );
            
            Circle circle = new Circle(8);
            circle.setFill(Color.RED);
            circle.setOpacity(0.7);
            circle.setStroke(Color.DARKRED);
            circle.setStrokeWidth(1.0);
            
            points.add(new Pair<>(branchPoint, circle));
            this.getChildren().add(circle);
        }
    }

    private void addPatientMarker() {
        MapPoint patientPoint = new MapPoint(patientLat, patientLon);
        
        Circle circle = new Circle(8);
        circle.setFill(Color.BLUE);
        circle.setOpacity(0.8);
        circle.setStroke(Color.DARKBLUE);
        circle.setStrokeWidth(1.0);
        
        points.add(new Pair<>(patientPoint, circle));
        this.getChildren().add(circle);
    }

    @Override
    protected void layoutLayer() {
        for (Pair<MapPoint, Node> candidate : points) {
            MapPoint point = candidate.getKey();
            Node node = candidate.getValue();
            Point2D mapPoint = getMapPoint(point.getLatitude(), point.getLongitude());
            node.setVisible(true);
            node.setTranslateX(mapPoint.getX());
            node.setTranslateY(mapPoint.getY());
        }
    }
}
