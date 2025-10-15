package OOP_LAB;

import java.util.ArrayList;
import java.util.Scanner;

public class Order {
    public int id;
    public ArrayList<Item> items ;

    public Order(int id ){
        this.id = id;
        this.items = new ArrayList<>();
    }

    public double calculateAverageCost(){
        double sum = 0;
        double avarage=0;
        for (Item item : items){
            sum += item.getPrice();
            avarage = sum/items.size();}
        return avarage;
    }

    public int getId(){
        return this.id;}

    public void addItem(Item item) {
        items.add(item);}

}

class Item{
    public int id;
    public String name;
    public double price;

    public Item(int id, String name, double price){
        this.id = id;
        this.name = name;
        this.price = price;}

    public String getName(){ return this.name;}
    public double getPrice(){return this.price;}
    public int getID(){return this.id;}
}


class OrderTest{
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.printf("Enter a new number ID for order: ");
        int ordID= sc.nextInt();
        System.out.printf("Enter the number of items in order: ",ordID,": ");
        int amount= sc.nextInt();

        Order ord = new Order(ordID);


        for (int i=1; i <= amount; i++){

            System.out.printf("Input the name of item "+ i +": ");
            String name = sc.nextLine();
            sc.nextLine();
            System.out.printf("Input the ID of item "+ i+": ");
            int id = sc.nextInt();
            sc.nextLine();
            System.out.printf("Input the price of item "+ i+": ");
            double price = sc.nextDouble();


            Item item = new Item(id,name,price);
            ord.addItem(item);
        }

        System.out.printf("-------");
        System.out.printf("Information of order "+ ord.getId() +":");
        System.out.printf(" You have "+amount+" in order.");
        System.out.printf("The average price in order is "+ ord.calculateAverageCost());

    }
}
