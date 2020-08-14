package main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

public class Main extends Canvas implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7604959185792845431L;
	private boolean running;
	private Thread thread;
	private int COL;
	private int ROW;
	private float cellSizeX;
	private float cellSizeY;
	private double maxDistance;
	private Point[][] list;

	public static void main(String[] args) {
		new Main().start();
	}
	
	public synchronized void start() {
		if(this.running == true) {
			return;
		}
		this.thread = new Thread(this);
		this.thread.start();
		this.running = true;
	}
	
	public synchronized void stop() {
		this.running = false;
		//clean up
	}
	
	private void init() {
		JFrame frame = new JFrame("2D Game");
		frame.setSize(200, 200);
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setUndecorated(true);
		frame.setVisible(true);
		this.requestFocus();
		//
		this.COL = 5;
		this.ROW = 5;
		this.cellSizeX = this.getWidth() / this.COL;
		this.cellSizeY = this.getHeight() / this.ROW;
		this.maxDistance = Math.hypot(cellSizeX, cellSizeY);
		this.list = new Point[COL][ROW];
		for(int i = 0; i < this.COL; i++) {
			for(int j = 0; j < this.ROW; j++) {
				this.list[i][j] = new Point(i*this.cellSizeX, j*this.cellSizeY, (i+1)*this.cellSizeX, (j+1)*this.cellSizeY);
			}
		}
	}
	
	@Override
	public void run() {
		this.init();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int updates = 0;
		int frames = 0;
		while(this.running == true) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while(delta >= 1){
				this.tick();
				updates++;
				delta--;
			}
			this.render();
			frames++;
			
			if(System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println("FPS: " + frames + " TICKS: " + updates);
				frames = 0;
				updates = 0;
			}
		}
		System.exit(0);
	}
	
	private void tick() {
		for(Point[] points: this.list) {
			for(Point p: points) {
				p.tick();
			}
		}
	}
	
	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		Graphics2D g2d = (Graphics2D) g;
		AffineTransform af = g2d.getTransform();
		//start draw
			//bg
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
			//bgg
		for(int i = 0; i < this.getWidth(); i++) {
			for(int j = 0; j < this.getHeight(); j++) {
				g.setColor(this.getColor(i, j));
				g.drawRect(i, j, 1, 1);
			}
		}
			//points
//		for(Point[] points: this.list) {
//			for(Point p: points) {
//				p.render(g);
//			}
//		}
		//end draw
		g2d.setTransform(af);
		g.dispose();
		bs.show();
	}
	
	private Color getColor(int i, int j) {
		//find distance to closet point
		double shortestDistance = Double.MAX_VALUE;
		int ii = (int) ((i / this.cellSizeX)-1);
		int jj = (int) ((j / this.cellSizeY)-1);
		for(int a = 0; a < 3; a++) {
			for(int b = 0; b < 3; b++) {
				if((a+ii) >= 0 && (b+jj) >= 0 && (a+ii) < this.COL && (b+jj) < this.ROW) {
					double d = this.list[(a+ii)][(b+jj)].distanceTo(i, j);
					if(d < shortestDistance) {
						shortestDistance = d;
					}
				}
			}
		}
		shortestDistance /= this.maxDistance;
		return new Color((float) shortestDistance, (float) shortestDistance, (float) shortestDistance);
	}
	
	class Point {
		private float x;
		private float y;
		private float velX;
		private float velY;
		private float minX;
		private float minY;
		private float maxX;
		private float maxY;
		private final float K = 1.5f; 
		
		public Point(float minX, float minY, float maxX, float maxY) {
			this.minX = minX;
			this.minY = minY;
			this.maxX = maxX;
			this.maxY = maxY;
			double a = Math.random()*2*Math.PI;
			this.velX = (float) (K*Math.cos(a));
			this.velY = (float) (K*Math.sin(a));
			this.x = (float) (this.minX + (Math.random() * ((this.maxX - this.minX) + 1)));
			this.y = (float) (this.minY + (Math.random() * ((this.maxY - this.minY) + 1)));
		}
		
		public double distanceTo(int i, int j) {
			return Math.hypot(this.x - i, this.y - j);
		}
		
		public void tick() {
			if(this.x + this.velX < this.minX || this.x + this.velX > this.maxX) {
				this.velX *= -1;
			}
			if(this.y + this.velY < this.minY || this.y + this.velY > this.maxY) {
				this.velY *= -1;
			}
			this.x += velX;
			this.y += velY;
		}
		
//		public void render(Graphics g) {
//			g.setColor(Color.red);
//			g.drawRect((int) this.x, (int) this.y, 1, 1);
//		}
		
	}

}