package OOP_LAB;

import java.util.Scanner;

public class Point {
    public double x;
    public double y;
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    public double Distance(Point other){
        double dx= this.x-other.x ;
        double dy =  this.x-other.y;
        return Math.sqrt(dx * dx + dy * dy);

    }
}

class DistanceTest {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.printf("Input first point x and y: ");
        double x = sc.nextDouble();
        double y = sc.nextDouble();

        System.out.printf("Input second point x and y: ");
        double a = sc.nextDouble();
        double b = sc.nextDouble();

        Point p1 = new Point(x,y);
        Point p2 = new Point(a,b);


        System.out.println("The distance between X and Y is: "+ String.format("%.2f",p1.Distance(p2)) );



    }
}