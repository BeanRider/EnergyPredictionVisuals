package utility.chart;

import mvc.model.datasource.Tuple;

import java.util.function.DoubleFunction;

/**
 * This should have no idea of its dimensions, or positions
 */
public class AxisLabelSettings {
    public enum TickMode {
        ALL_DATA, REGULAR, FUNCTIONAL
    }

    // 3 tick modes on a linear number axis:
    // 1) As all current data
    // 2) As regular interval (0, 5, 10, 15, ...)
    // 3) As functional interval (f(0)=0, f(1)=1, f(2)=4, f(3)=9, f(4)=16, ...)

    // 2 tick modes on a enumerated axis:
    // 1) As all current data
    // 2) As specified/mapped to. (data -> displayedText)

    private boolean render = true;
    private Tuple<Double, Double> dataRange = new Tuple<>(0.0, 0.0);
    private boolean isEnumerated;
    private TickMode tickMode;
    // NOTE: interval represents labeling frequency
    private double interval = 1;
    private DoubleFunction<Double> map = (x) -> x;

    /**
     * Linear, Regular
     * Labeling every given interval units
     * @param render all labels active or not
     * @param interval interval to put labels (interval starts from 0)
     */
    public AxisLabelSettings(boolean render, double interval, Tuple<Double, Double> dataRange) {
        this.render = render;
        this.dataRange = dataRange;
        this.isEnumerated = false;
        this.tickMode = TickMode.REGULAR;
        this.interval = interval;
    }

    /**
     * Linear, All Data
     * @param render
     * @param dataRange
     */
    public AxisLabelSettings(boolean render, Tuple<Double, Double> dataRange) {
        this.render = render;
        this.dataRange = dataRange;
        this.tickMode = TickMode.ALL_DATA;
    }

    /**
     * Linear, All Data
     * @param render
     * @param dataRange
     */
    public AxisLabelSettings(boolean render, Tuple<Double, Double> dataRange, DoubleFunction<Double> map) {
        this.render = render;
        this.dataRange = dataRange;
        this.tickMode = TickMode.FUNCTIONAL;
        this.map = map;
    }

    public boolean labelOn() {
        return render;
    }

    public TickMode getTickMode() {
        return tickMode;
    }

    public double getLowerBound() {
        return map.apply(dataRange.x);
    }

    public double getUpperBound() {
        return map.apply(dataRange.y);
    }

    public DoubleFunction<Double> getMap() {
        return map;
    }

    public Tuple<Double, Double> getBounds() {
        return new Tuple<>(map.apply(dataRange.x), map.apply(dataRange.y));
    }

    public double getInterval() {
        if (tickMode != TickMode.REGULAR && tickMode != TickMode.FUNCTIONAL) {
            throw new RuntimeException("Not using a REGULAR OR FUNCTIONAL tick mode!");
        }
        return interval;
    }

    public int calculateNumTicks() {
        return (int) Math.round((getUpperBound() - getLowerBound()) / getInterval()) + 1;
    }

    public static class Builder {

        // Default values: render, 0 - 10, with interval of 1
        private boolean render = true;
        private Tuple<Double, Double> visualRange = new Tuple<>(0.0, 10.0);
        private float interval = 1;
        private DoubleFunction<Double> map;

        public Builder render(boolean render) {
            this.render = true;
            return this;
        }

        public Builder visualRange(Tuple<Double, Double> visualRange) {
            this.visualRange = visualRange;
            return this;
        }

        public Builder interval(float interval) {
            this.interval = interval;
            return this;
        }

        public Builder mapFunction(DoubleFunction<Double> map) {
            this.map = map;
            return this;
        }

        public AxisLabelSettings buildRegular() {
            return new AxisLabelSettings(render, interval, visualRange);
        }

        public AxisLabelSettings buildAllData() {
            return new AxisLabelSettings(render, visualRange);
        }

        // example: mapping unix time -> month
        public AxisLabelSettings buildFunction() {
            return new AxisLabelSettings(render, visualRange, map);
        }
    }
}


