package utility;

/**
 * Refers to an nth row or column
 */
public class TableVectorRange {
    private TableVectorOrientation orientation = null;
    private int from, to;

    /**
     * Makes a TableVectorRange using orientation and ith
     * @param orientation either ROW or COL
     * @param from integer bigger or equal to 0
     * @param to integer bigger than from
     */
    public TableVectorRange(TableVectorOrientation orientation, int from, int to) {
        if (from < 0) {
            throw new RuntimeException("A table vector's from index must be bigger than or equal to 0!");
        }
        if (to <= from) {
            throw new RuntimeException("A table vector range ending index must be bigger than from");

        }
        this.orientation = orientation;
        this.from = from;
        this.to = to;
    }
}
