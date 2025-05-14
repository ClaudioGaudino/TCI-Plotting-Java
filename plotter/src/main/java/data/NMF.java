package data;

import java.util.List;

public interface NMF {
    List<List<List<Double>>> factorize(List<List<Double>> matrix, int k);

}
