package resource;

/**
 * @author Team 3
 * 
 * Custom tuple class used to encapsulates two types
 * 
 * @param <T>
 * @param <P>
 */
public class Tuple<T, P> {
	public T first;
	public P second;

	public Tuple(T t, P p) {
		this.first = t;
		this.second = p;
	}
}
