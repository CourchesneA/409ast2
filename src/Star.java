import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Solve the dining philosopher problem by ordering resources: lock the lowest index vertex first
 * @author Anthony
 *
 */
public class Star {
	public static final int width = 1920;
	public static final int height = 1080;
	public static int m;
	public static int c;
	public static int n = 6;
	public static Vertex2D head;
	public static Random rnd = new Random();
	public static Vertex2D vertices[];

	public static void main(String[] args) {
		 if (args.length<2)
             new Exception("Missing arguments, only "+args.length+" were specified!").printStackTrace();
         // arg 0 is m
         m = Integer.parseInt(args[0]);
         // arg 1 is c
         c = Integer.parseInt(args[1]);
         
         assert(c >= 0);
         assert(m<=n);
         
         BufferedImage img = new BufferedImage(1920,1080,BufferedImage.TYPE_INT_ARGB);
         Graphics2D graphics = img.createGraphics();
         for (int i=0;i<width;i++) {
             for (int j=0;j<height;j++) {
                 img.setRGB(i,j,0xffffffff);
             }
         }
         graphics.setColor(Color.black);
         
         //Hard code the polygon
         vertices = new Vertex2D[]{
        		 new Vertex2D(-1.0, 5.0), 
        		 new Vertex2D(1.0, 2.0),
        		 new Vertex2D(5.0, 0.0),
        		 new Vertex2D(1.0, -2.0),
        		 new Vertex2D(-4.0, -4.0),
        		 new Vertex2D(-3.0, -1.0)
         };

         //Set previous and next for the vertices
         for(int i=0; i<vertices.length; i++) {
        	 vertices[i].next = vertices[(i+1)%vertices.length];
        	 vertices[i].previous = vertices[(i+(vertices.length-1))%vertices.length];
         }
         
         //Start the threads
         Thread[] threadPool = new Thread[m];
         for(int i=0; i<m; i++) {
        	 threadPool[i] = new Thread(new VertexMover());
        	 threadPool[i].start();
         }
         
         //Wait for them to finish
         for(int i=0; i<m; i++) {
        	 try {
				threadPool[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
         }
         
         //Centering - otherwise the polygon drifts off screen after some iterations
         double maxX = vertices[0].x;
         double minX = vertices[0].x;
         double maxY = vertices[0].y;
         double minY = vertices[0].y;
         for(int i=0; i< vertices.length; i++) {
        	 double x = vertices[i].x;
        	 double y = vertices[i].y;
        	 maxX = (x > maxX) ? x : maxX;
        	 minX = (x < minX) ? x : minX;
        	 maxY = (y > maxY) ? y : maxY;
        	 minY = (y < minY) ? y : minY;
         }
         double midX = minX + (maxX-minX)/2;
         double movX = 0 - midX;
         double midY = minY + (maxY-minY)/2;
         double movY = 0 - midY; 
         
         for(int i=0; i<vertices.length; i++) {
        	 vertices[i].x+=movX;
        	 vertices[i].y+=movY;
         }
         
         
         //Scaling
         //Find the coord that is the further away from center
         maxX = 0;
         maxY = 0;
         for(int i=0; i< vertices.length; i++) {
        	 double x = Math.abs(vertices[i].x);
        	 double y = Math.abs(vertices[i].y);
        	 maxX = (x > maxX) ? x : maxX;
        	 maxY = (y > maxY) ? y : maxY;
         }
         //Scaling
		 while(maxX > width/2 || maxY > height/2) {
			 maxX/=2;maxY/=2;
			 scaleDown();
		 }
		     
		 while(maxX < width/4 && maxY < height/4) {
			 maxX*=2;maxY*=2;
			 scaleUp();
		 }
         
         //Draw the polygon
         Vertex2D prev = null;
         for(int i=0; i<=vertices.length; i++) {
    		 Vertex2D v = vertices[i%vertices.length];
        	 if(prev != null) {
        		 graphics.drawLine((int)prev.x+width/2, (int)prev.y+height/2, (int)v.x+width/2, (int)v.y+height/2);
        	 }
        	 prev = v;
         }
         
         for(int i=0; i< vertices.length; i++) {
        	 graphics.drawOval((int)vertices[i].x-2+width/2, (int)vertices[i].y+2+height/2, 2, 2);
         }
         graphics.drawOval(-2 + width/2, 2 + height/2, 4, 4);	//See origin

         File outputfile = new File("outputimage.png");
         try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void scaleDown() {
		System.out.println("Scaled down polygon");
		for(int i=0; i<vertices.length; i++) {
			vertices[i].x/=2;
			vertices[i].y/=2;
		}
	}
	
	public static void scaleUp() {
		System.out.println("Scaled up polygon");
		for(int i=0; i<vertices.length; i++) {
			vertices[i].x*=2;
			vertices[i].y*=2;
		}
	}
	
	/**
	 * Move the vertex within the triangle made by it and its neighbors
	 * algorithm from https://math.stackexchange.com/questions/18686/uniform-random-point-in-triangle
	 * @param v
	 */
	public static void moveVertex(Vertex2D v) {
		double r1 = rnd.nextDouble();
		double r2 = rnd.nextDouble();
		double x = (1 - Math.sqrt(r1)) * v.previous.x + (Math.sqrt(r1) * (1 - r2)) * v.x + (Math.sqrt(r1) * r2) * v.next.x;
		double y = (1 - Math.sqrt(r1)) * v.previous.y + (Math.sqrt(r1) * (1 - r2)) * v.y + (Math.sqrt(r1) * r2) * v.next.y;
		v.x = x;
		v.y = y;
	}
	
	/**
	 * Use resource ordering to prevent deadlocks
	 * @author anthony
	 *
	 */
	static class VertexMover implements Runnable{

		@Override
		public void run() {
			
			for(int i=0; i<c; i++) {
				//Choose random vertex v
				int vertexIndex = rnd.nextInt(vertices.length);
				Vertex2D v = vertices[vertexIndex];
				
				moveVertex(v);
				System.out.println("Moved "+vertexIndex);
				//Change the position
				//TODO
				
				//Sleep
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					//We dont care if its interrupted
				}
			}
		}
		
	}
}

class Vertex2D{
	public double x;
	public double y;
	public Vertex2D previous;
	public Vertex2D next;
	
	public Vertex2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
}
