import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Star {
	public static final int width = 1920;
	public static final int height = 1080;
	public static int m;
	public static int c;
	public static int n = 6;
	public static Vertex2D head;

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
         Vertex2D[] vertices = {
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
         
         
         //Draw the polygon
         Vertex2D prev = null;
         for(int i=0; i<=vertices.length; i++) {
    		 Vertex2D v = vertices[i%vertices.length];
        	 if(prev != null) {
        		 graphics.drawLine((int)prev.x*10+width/2, (int)prev.y*10+height/2, (int)v.x*10+width/2, (int)v.y*10+height/2);
        	 }
        	 prev = v;
         }

         File outputfile = new File("outputimage.png");
         try {
			ImageIO.write(img, "png", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
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
