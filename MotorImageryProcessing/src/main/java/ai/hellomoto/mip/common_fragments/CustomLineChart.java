package ai.hellomoto.mip.common_fragments;

import javafx.beans.NamedArg;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;

public class CustomLineChart<X, Y> extends LineChart<X, Y> {
    public CustomLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        this(xAxis, yAxis, FXCollections.<Series<X, Y>>observableArrayList());
    }

    public CustomLineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis, @NamedArg("data") ObservableList<Series<X,Y>> data) {
        super(xAxis,yAxis);
        setData(data);
    }

    public void hideXAxis() {
        getChartChildren().remove(this.getXAxis());
    }
}
