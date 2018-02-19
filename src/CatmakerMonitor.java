import java.util.LinkedList;

/**
 * 409 ast 2, q2 a)
 * This class use the monitor technique to solve the problem
 * The problem is solved in Catmaker.java using an abstract class SynchronizedBin
 * @author Anthony
 *
 */
public class CatmakerMonitor {

	public static void main(String[] args) {
		Catmaker.forelegBin = new MonitorBin<Leg>();
		Catmaker.hindlegBin = new MonitorBin<Leg>();
		
		Catmaker.bodyTailBin = new MonitorBin<Body>();
		Catmaker.completeBodyBin = new MonitorBin<Body>();
		Catmaker.bodyLegBin = new MonitorBin<Body>();
		
		Catmaker.headWhiskerBin = new MonitorBin<Head>();
		Catmaker.headEyeBin = new MonitorBin<Head>();
		Catmaker.completeHeadBin = new MonitorBin<Head>();
		
		Catmaker.runThreads();
	}
}

class MonitorBin<T> extends SynchronizedBin<T>{
	LinkedList<T> bin = new LinkedList<T>();
	
	synchronized public void produceObject(T assembly) {
		bin.add(assembly);
		notify();
	}
	
	@Override
	synchronized public T consumeObject() {
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
	synchronized public T tryGetObject() {
		if(bin.size() == 0) {
			return null;
		}else {
			return consumeObject();
		}
	}
}