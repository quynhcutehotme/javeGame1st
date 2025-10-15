package OOP_LAB;

import java.util.Scanner;

public class Triangle {
    public int x;
    public int y ;
    public int z;
    public Triangle(int x, int y, int z){
            this.x = x;
            this.y = y;
            this.z = z; }
    public void verify(){
        if ( x+y<z || x+z<y || y+x<z){
            System.out.println("This is not a triangle!");
        }
        else if ( (x ==y ) && (x ==z)){
            System.out.println("This is an equilateral triangle");
        }
        else if ((x==y ) ||(x==z ) ||(z==y) ){
            System.out.println("This is an isosceles  triangle");
        }
        else {
            System.out.println("This is a scalene triangle");
        }
    }
}

class TriangleVerification{
    public static void main(String[] args) {
        Scanner sc= new Scanner(System.in);
        System.out.printf("Input 3 number for side of a triangle: ");
        int x = sc.nextInt();
        int y = sc.nextInt();
        int z = sc.nextInt();

        Triangle sam1 = new Triangle(x,y,z);
        sam1.verify();

    }

}
