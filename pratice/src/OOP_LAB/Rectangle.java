package OOP_LAB;

import java.util.Scanner;

public class Rectangle {
    public int width;
    public int height;
    public Rectangle(int width, int height){
        if (width > 0) {
            this.width = width;
        }
        else {
            System.out.println("Error value. Width must be positive.Set to default value");
            this.width = 1;
        }
        if (height > 0) {
            this.height = height;
        }
        else {
            System.out.println("Error value. Height must be positive. Set to default value");
            this.height = 1;
        }

    }
    public void getValue(){
        System.out.println("height :"+ height);
        System.out.println("width :"+ width);
        for (int i=1;i<=height; i++){
            for (int j = 1; j<=width;j++){
                System.out.printf("*");
            }
            System.out.println();
        }
    }
}

class TestRectangle{
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.print("input width :" );
        int a = sc.nextInt();
        System.out.print("input height:");
        int b = sc.nextInt();

        Rectangle samp1 = new Rectangle(a,b);
        samp1.getValue();
    }
}







