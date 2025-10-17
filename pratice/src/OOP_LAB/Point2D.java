package OOP_LAB;

import java.util.Scanner;

public class Point2D {
    public int x ;
    public int y;
    public Point2D(){
        this.x = 0;
        this.y= 0;

    }
    public Point2D(int x, int y){ this.x=x; this.y = y;}
    public Point2D(Point2D p){
        this.x = p.x;
        this.y = p.y;
    }

    public void input(){
        Scanner sc = new Scanner(System.in);
        System.out.print("Input value of X:" );
        this.x = sc.nextInt();
        sc.nextLine();
        System.out.print("Input value of Y:" );
        this.y = sc.nextInt();
    }
    public void move(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public String toString(){
        return "(" + x + ", " + y + ")";
    }
    public boolean isOrigin() {
        return x == 0 && y == 0;
    }

    public double distance(Point2D p) {
        int dx = this.x - p.x;
        int dy = this.y - p.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    public static double distance(Point2D p1, Point2D p2) {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

class TestingPoint2D {
    public static void main(String[] args) {

        //Test my Point2D class
        Point2D p1 = new Point2D();
        System.out.printf("The initial value of p1: %s\n", p1);
        System.out.printf("Is p1 at the origin?: %s\n", p1.isOrigin());
        System.out.println("Asking user to change values for p1!");
        p1.input();
        System.out.printf("The new value of p1: %s\n", p1);
        Point2D p2 = new Point2D(4,7);
        System.out.printf("The value of p2: %s\n", p2);
        Point2D p3 = new Point2D(p2);
        System.out.printf("The value of p3: %s\n", p3);
        System.out.printf("First way to calculate distance between p1 and p2: %.2f\n",
                p1.distance(p2));
        System.out.printf("Second way to calculate distance between p1 and p2: %.2f\n",
                Point2D.distance(p1, p2));
        System.out.printf("First way to calculate distance between p2 and p3: %.2f\n",
                p2.distance(p3));
        System.out.printf("Second way to calculate distance between p2 and p3: %.2f\n",
                Point2D.distance(p2, p3));
    }
}
