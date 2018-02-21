import java.util.LinkedList;
import java.util.Random;

/**
 * This class solve the problem but does not tackle any concurrency problem,
 * those should be handled by the class that extends SynchronizedBin
 * @author Anthony
 *
 */
public class Catmaker {
	static SynchronizedBin<Leg> forelegBin;
	static SynchronizedBin<Leg> hindlegBin;
	
	static SynchronizedBin<Body> bodyTailBin;
	static SynchronizedBin<Body> completeBodyBin;
	static SynchronizedBin<Body> bodyLegBin;
	
	static SynchronizedBin<Head> headWhiskerBin;
	static SynchronizedBin<Head> headEyeBin;
	static SynchronizedBin<Head> completeHeadBin;
		
	static int catCount = 0;
	static Random rnd = new Random();
	
	public static void runThreads() {
		Thread[] threads = new Thread[] {
				new Thread(new LegAssembler()),
				new Thread(new LegAssembler()),
				new Thread(new LegPlugger()),
				new Thread(new LegPlugger()),
				new Thread(new TailPlugger()),
				new Thread(new TailPlugger()),
				new Thread(new EyePlugger()),
				new Thread(new EyePlugger()),
				new Thread(new WhiskersPlugger()),
				new Thread(new WhiskersPlugger()),
				new Thread(new Assembler())
		};
		
		for(Thread t : threads) t.start();
		for(Thread t : threads)
			try {
				t.join();
			} catch (InterruptedException e) {}
	}
	
	
	//~~~~~~~~~~~~~~~~   THREAD DEFINITION ~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Get leg and add toes (4 or 5)
	 * @author Anthony
	 *
	 */
	static class LegAssembler implements Runnable{
		double idletime = 0;
		@Override
		public void run() {
			while(catCount < 250) {	//We stop at 250 total. We wont need mutex here or perfect accuracy
				Leg l = LegBin.getLeg();
				int toes = 0;
				boolean isForeleg = rnd.nextBoolean();
				if(isForeleg) toes=5; else toes=4;
				for(int i=0; i<toes; i++) {
					l.addToe(ToeBin.getToe());
				}
				if(isForeleg) {
					forelegBin.produceObject(l);
				}else {
					hindlegBin.produceObject(l);
				}
				sleepBetween(10, 20);
			}
			System.out.println("LegAssembler idletime = "+idletime+ "ms");
		}
	}
	
	/**
	 * Get a body with or without tail and add legs.
	 * @author Anthony
	 *
	 */
	static class LegPlugger implements Runnable{
		double idletime = 0;
		@Override
		public void run() {
			while(catCount < 250) {
				
				Body assembly = null; 
				long t1 = System.currentTimeMillis();
				assembly = bodyTailBin.tryGetObject();	//Get a body with tail if there is any
				if(assembly == null) assembly = BodyBin.getBody();	//Else get a new one
				
				//Get the legs
				LinkedList<Leg> forelegs = new LinkedList<Leg>();
				LinkedList<Leg> hindlegs = new LinkedList<Leg>();
				for(int i=0; i<2; i++) {
					forelegs.add(forelegBin.consumeObject());
					hindlegs.add(hindlegBin.consumeObject());
				}
				idletime += System.currentTimeMillis()-t1;
				
				//Assemble everything
				assembly.forelegs.addAll(forelegs);
				assembly.hindlegs.addAll(hindlegs);
				
				if(assembly.tail!= null) {
					completeBodyBin.produceObject(assembly);
				}else {
					bodyLegBin.produceObject(assembly);
				}
				sleepBetween(30, 50);
			}
			System.out.println("LegPlugger idletime = "+idletime+ "ms");
		}
	}
	
	/**
	 * Get body (with legs if possible) and add a tail
	 * @author Anthony
	 *
	 */
	static class TailPlugger implements Runnable{
		double idletime = 0;

		@Override
		public void run() {
			while(catCount < 250) {
				
				Body assembly = null; 
				long t1 = System.currentTimeMillis();
				assembly = bodyLegBin.tryGetObject();	//Get a body with legs if there is any
				if(assembly == null) assembly = BodyBin.getBody();	//Else get a new one
				assembly.tail = TailBin.getTail();
				idletime += System.currentTimeMillis()-t1;
				
				if(assembly.forelegs.size() != 0) {
					assert(assembly.forelegs.size() == 2 && assembly.hindlegs.size() == 2);
					completeBodyBin.produceObject(assembly);
				}else {
					assert(assembly.forelegs.size() == 0 && assembly.hindlegs.size() == 0);
					bodyTailBin.produceObject(assembly);
				}
				sleepBetween(10, 20);
			}
			System.out.println("TailPlugger idletime = "+idletime+" ms");
		}
	}
	
	/**
	 * Take a head with wiskers if it exists, otherwise take a new head
	 * @author Anthony
	 *
	 */
	static class EyePlugger implements Runnable{
		double idletime = 0;
		@Override
		public void run() {
			while(catCount < 250) {
				Head assembly = null; 
				
				long t1 = System.currentTimeMillis();
				assembly = headWhiskerBin.tryGetObject();	//Get a head with whiskers if there is any
				if(assembly == null) assembly = HeadBin.getHead();	//Else get a new one
				
				for(int i=0; i<2; i++) {
					assembly.eyes.add(EyeBin.getEye());
				}
				idletime += System.currentTimeMillis()-t1;
				
				if(assembly.whiskers.size() != 0) {
					assert(assembly.whiskers.size() == 6);
					completeHeadBin.produceObject(assembly);
				}else {
					assert(assembly.whiskers.size() == 0);
					headEyeBin.produceObject(assembly);
				}
				sleepBetween(10, 30);
			}
			System.out.println("EyePlugger idletime = "+idletime +" ms");
		}
	}
	
	static class WhiskersPlugger implements Runnable{
		double idletime = 0;
		@Override
		public void run() {
			while(catCount < 250) {
				Head assembly = null; 
				
				long t1 = System.currentTimeMillis();
				assembly = headEyeBin.tryGetObject();	//Get a head with eyes if there is any
				if(assembly == null) assembly = HeadBin.getHead();	//Else get a new one
				
				for(int i=0; i<6; i++) {
					assembly.whiskers.add(WhiskerBin.getWhisker());
				}
				idletime += System.currentTimeMillis()-t1;
				
				if(assembly.eyes.size() != 0) {
					assert(assembly.eyes.size() == 2);
					completeHeadBin.produceObject(assembly);
				}else {
					assert(assembly.eyes.size() == 0);
					headWhiskerBin.produceObject(assembly);
				}
				sleepBetween(20, 60);
			}
			System.out.println("WhiskerPlugger idle time = "+idletime + " ms");
		}
	}
	


	/**
	 * Assemble the complete Cat and increment catCount
	 * @author Anthony
	 *
	 */
	static class Assembler implements Runnable{
		double idletime = 0;
		@Override
		public void run() {
			while(catCount < 250) {
				long t1 = System.currentTimeMillis();
				Head head = completeHeadBin.consumeObject();
				Body body = completeBodyBin.consumeObject();
				idletime += System.currentTimeMillis()-t1;
				
				Cat masterpiece = new Cat(head,body);
				
				if(validateAndDispose(masterpiece))catCount++;	//We are not keeping track of finished to reduce mem usage
				sleepBetween(10, 20);
			}
			System.out.println("Assembler idletime = "+idletime+ " ms");
		}
		
		
		private boolean validateAndDispose(Cat masterpiece) {
			try {
				assert(masterpiece.body != null);
				assert(masterpiece.body.forelegs.size() == 2);
				assert(masterpiece.body.forelegs.get(0).getType() == LegType.FORELEG);
				assert(masterpiece.body.forelegs.get(1).getType() == LegType.FORELEG);
				assert(masterpiece.body.hindlegs.size() == 2);
				assert(masterpiece.body.hindlegs.get(0).getType() == LegType.HINDLEG);
				assert(masterpiece.body.hindlegs.get(1).getType() == LegType.HINDLEG);
				
				assert(masterpiece.head != null);
				assert(masterpiece.head.whiskers.size() == 6);
				assert(masterpiece.head.eyes.size() == 2);
			}catch(AssertionError e) {
				e.printStackTrace();
				return false;
			}
			
			//Tell garbage collector to get rid of this object
			masterpiece = null;
			return true;
		}
		
	}
	
	/**
	 * Helper to make code clearer -> remove try catch and math stuff
	 * @param a min time to wait
	 * @param b max time to wait
	 */
	static void sleepBetween(int a, int b) {
		try {
			Thread.sleep(rnd.nextInt(Math.abs(b-a)+a));
		} catch (InterruptedException e) {}
	}

}



//~~~~~~~~~~~~~~  OBJECT DEFINITION   ~~~~~~~~~~~~~~~~~~

class Cat{
	Head head = null;
	Body body = null;
	
	public Cat(Head head, Body body) {
		this.head = head;
		this.body = body;
	}
}

class Body{
	LinkedList<Leg> forelegs = new LinkedList<Leg>();
	LinkedList<Leg> hindlegs = new LinkedList<Leg>();
	Tail tail = null;
}

class Tail{
	
}

class Eye{
	
}

class Leg{
	LinkedList<Toe> toes = new LinkedList<Toe>();
	
	public LegType getType() {
		if(toes.size() == 5) {
			return LegType.FORELEG;
		}else if(toes.size() == 4) {
			return LegType.HINDLEG;
		}else {
			return LegType.INVALID;
		}
	}
	
	public void addToe(Toe t) {
		toes.add(t);
	}
}

class Whisker{
	
}

class Toe{
	
}

class Head{
	LinkedList<Eye> eyes = new LinkedList<Eye>();
	LinkedList<Whisker> whiskers = new LinkedList<Whisker>();
}

//~~~~~~~~~~~~~~~~~~~~~  Infinite bins  ~~~~~~~~~~~~~~~~~~~~~~

/**
 * Infinite amount of Toes, act as a factory.
 * Does not need mutex, everything is static
 * If we wanted to make it so only one can access the bin at a time, we would make the getToe synchronized
 * @author Anthony
 *
 */
class ToeBin{
	
	public static Toe getToe() {
		return new Toe();
	}
}

class LegBin{
	public static Leg getLeg() {
		return new Leg();
	}
}

class BodyBin{
	public static Body getBody() {
		return new Body();
	}
}

class HeadBin{
	public static Head getHead() {
		return new Head();
	}
}

class TailBin{
	public static Tail getTail() {
		return new Tail();
	}
}

class EyeBin{
	public static Eye getEye() {
		return new Eye();
	}
}

class WhiskerBin{
	public static Whisker getWhisker() {
		return new Whisker();
	}
}


//~~~~~~~~~~~~~~~~~~~~~ Helper Classes ~~~~~~~~~~~~~~~~

enum LegType {
	FORELEG,
	HINDLEG,
	INVALID
}

