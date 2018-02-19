import java.util.LinkedList;
import java.util.concurrent.Semaphore;

/** 
 * 409 ast 2, q2 b)
 * This class use the semaphore technique to solve the problem
 * The problem is solved in Catmaker.java using an abstract class SynchronizedBin
 * @author Anthony
 *
 */
public class CatmakerSemaphore{
	static Semaphore foreLegMutex = new Semaphore(1);
	static Semaphore hindLegMutex = new Semaphore(1);
	static Semaphore bodyTailMutex = new Semaphore(1);
	static Semaphore completeBodyMutex = new Semaphore(1);
	static Semaphore bodyLegMutex = new Semaphore(1);
	static Semaphore headWhiskerMutex = new Semaphore(1);
	static Semaphore headEyeMutex = new Semaphore(1);
	static Semaphore completeHeadMutex = new Semaphore(1);


	public static void main(String[] args) {
		Catmaker.foreLegBin = new SemaphoreBin<Leg>();
		Catmaker.hindLegBin = new SemaphoreBin<Leg>();
		
		Catmaker.bodyTailBin = new SemaphoreBin<Body>();
		Catmaker.completeBodyBin = new SemaphoreBin<Body>();
		Catmaker.bodyLegBin = new SemaphoreBin<Body>();
		
		Catmaker.headWhiskerBin = new SemaphoreBin<Head>();
		Catmaker.headEyeBin = new SemaphoreBin<Head>();
		Catmaker.completeHeadBin = new SemaphoreBin<Head>();
		
		Catmaker.runThreads();
		
	}
}

class SemaphoreBin<T> extends SynchronizedBin<T>{
	LinkedList<T> bin = new LinkedList<T>();
	
	public void produceObject(T assembly) {
		bin.add(assembly);
		notify();
	}
	
	@Override
	public T consumeObject() {
		while(bin.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		T assembly = bin.pop();
		return assembly;
	}
	
	@Override
	public T tryGetObject() {
		if(bin.size() == 0) {
			return null;
		}else {
			return consumeObject();
		}
	}
}