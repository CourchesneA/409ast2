import java.util.LinkedList;

/**
 * This abstract class is used to solve the problem.
 * Different techniques (Synchronized and monitors or semaphores) should be used to implement this class
 * @author Anthony
 *
 * @param <T>
 */
public abstract class SynchronizedBin<T> {
	LinkedList<T> bin = new LinkedList<T>();
	
	abstract public void produceObject(T assembly);
	
	abstract public T consumeObject();
	
	abstract public T tryGetObject();
}
