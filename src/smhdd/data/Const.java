package smhdd.data;

import java.util.Random;

public class Const {
    
    public static Random random;   
    public static final long[] SEEDS = {179424673, 125164703, 132011827, 124987441, 123979721 , 119777719, 117705823 , 112131119, 108626351, 107980007, 106368047, 99187427, 98976029, 97875523, 96763291, 95808337, 94847387, 87552823, 86842271 , 80650457, 78220001, 74585729, 73852469 , 68750849, 58160551 , 
        45320477, 31913771, 24096223, 16980937, 8261369};

    public static final byte TYPE_CATEGORICAL = 1;
    public static final byte TYPE_NUMERICAL = 2;

    public final static String METRIC_QG = "Qg";
    public final static String METRIC_WRACC = "WRAcc";
    public final static String METRIC_WRACC_NORMALIZED = "WRAccN";
    public static final String METRIC_WRACC_OVER_SIZE = "WRAccOverSize";
    public static final String METRIC_SUB = "Sub";

    public final static byte SIMILARIDADE_JACCARD = 1;

    public final static byte DEFAULT_NUM_BINS = 0;
    
    public final static byte DATASET_TYPE_CATEGORICAL = 1;
    public final static byte DATASET_TYPE_AUTO_DETECT = 2;


}
