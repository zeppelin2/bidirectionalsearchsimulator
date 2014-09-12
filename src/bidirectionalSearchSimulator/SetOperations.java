package bidirectionalSearchSimulator;

import java.util.HashSet;
import java.util.Set;

/**
 * SetOperations is a class that defines commonly used set operations.
 * 
 * source: http://www.java2s.com/Code/Java/Collections-Data-Structure/
 * Setoperationsunionintersectiondifferencesymmetricdifferenceissubsetissuperset
 * .htm
 */
public class SetOperations {
    
    public static <T> Set<T> union(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmp = new HashSet<T>(setA);
        tmp.addAll(setB);
        return tmp;
    }
    
    public static <T> Set<T> intersection(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmp = new HashSet<T>();
        for (final T x : setA) {
            if (setB.contains(x)) {
                tmp.add(x);
            }
        }
        return tmp;
    }
    
    public static <T> Set<T> difference(final Set<T> setA, final Set<T> setB) {
        final Set<T> tmp = new HashSet<T>(setA);
        tmp.removeAll(setB);
        return tmp;
    }
    
    public static <T> Set<T>
            symDifference(final Set<T> setA, final Set<T> setB) {
        Set<T> tmpA;
        Set<T> tmpB;
        
        tmpA = union(setA, setB);
        tmpB = intersection(setA, setB);
        return difference(tmpA, tmpB);
    }
    
    public static <T> boolean isSubset(final Set<T> setA, final Set<T> setB) {
        return setB.containsAll(setA);
    }
    
    public static <T> boolean isSuperset(final Set<T> setA, final Set<T> setB) {
        return setA.containsAll(setB);
    }
    
}
