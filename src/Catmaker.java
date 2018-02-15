import java.util.ArrayList;
import java.util.Random;

public class Catmaker {
	static ArrayList<Leg> forelegs = new ArrayList<Leg>();
	static ArrayList<Leg> hindlegs = new ArrayList<Leg>(); 
	static int catCount = 0;
	static Random rnd = new Random();
	
	public static void main(String[] args) {
		
	}
	
	class LegAssembler implements Runnable{
		
		@Override
		public void run() {
			while(catCount < 250) {
				Leg l = LegBin.getLeg();
				int toes = 0;
				boolean isForeleg = rnd.nextBoolean();
				if(isForeleg) toes=4; else toes=5;
				for(int i=0; i<toes; i++) {
					l.addToe(ToeBin.getToe());
				}
				if(isForeleg) {
					synchronized(forelegs) {
						forelegs.add(l);
						forelegs.notify();
					}
				}else {
					synchronized(hindlegs) {
						hindlegs.add(l);
						hindlegs.notify();
					}
				}
				try {
					Thread.sleep(rnd.nextInt(10)+10);
				} catch (InterruptedException e) {}
			}
		}
	}

	class Assembler implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}

}





class Cat{
	
}

class Body{
	
}

class Tail{
	
}

class Eye{
	
}

class Leg{
	ArrayList<Toe> toes;
	
	public String getType() {
		if(toes.size() == 4) {
			return "HINDLEG";
		}else if(toes.size() == 5) {
			return "FORELEG";
		}else {
			return "INVALID";
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

