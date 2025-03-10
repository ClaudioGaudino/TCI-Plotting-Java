package data;

import java.util.List;

public interface NMF {
    List<List<List<Double>>> factorize(double[][] matrix, int k);

    int num(int a);
}
