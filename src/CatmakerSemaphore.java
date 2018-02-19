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

	public static void main(String[] args) {
		Catmaker.forelegBin = new SemaphoreBin<Leg>();
		Catmaker.hindlegBin = new SemaphoreBin<Leg>();
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
	Semaphore mutex = new Semaphore(1);
	Semaphore objectCount = new Semaphore(0);
	LinkedList<T> bin = new LinkedList<T>();
	
	public void produceObject(T assembly) {
		try {
			mutex.acquire();
			try {
				bin.add(assembly);
			}finally {
				mutex.release();
			}
			objectCount.release();
			
		} catch (InterruptedException e) {
			
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	@Override
	public T consumeObject() {
		T assembly = null;
		try {
			objectCount.acquire();
			mutex.acquire();
			try {
				assembly = bin.pop();
			}finally {
				mutex.release();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			System.exit(-1);
		}
		assert(assembly != null);
		return assembly;
	}
	
	@Override
	public T tryGetObject() {
		T assembly = null;
		if(objectCount.tryAcquire()) {
			try {
				mutex.acquire();
				try {
					assembly = bin.pop();
				}finally {
					mutex.release();
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
			assert(assembly != null);
		}
		return assembly;
	}
}