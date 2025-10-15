package OOP_LAB;

import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

public class DiceGame {
    public static void main(String[] args) {
        House house = new House(1000);
        Player player = new Player(100);

        house.player = player;
        player.house = house;

        Scanner sc = new Scanner(System.in);

        System.out.println("You have "+ player.getWallet() +"$");
        System.out.println("House have "+ house.getWallet() +"$");
        System.out.println("-----------------");


        int i = 1;
        boolean next = true;

        while (next){
            System.out.println("Round "+ i);
            System.out.printf("Input your bet: ");
            int bet = sc.nextInt();
            sc.nextLine();
            player.setBet(bet);

            System.out.printf("Input your choice (big/small): ");
            String choice = sc.nextLine();
            player.setChoice(choice);

            house.rollAllDices();

            System.out.println("The results of dice: "+ house.getDice());
            System.out.println("The sum of dice: "+ house.sum());

            if (house.getOutput()){
                System.out.println("You loose "+player.getBet()+"$, the result is "+ house.resultChecking()+"!");
                System.out.println("You have "+player.walletUpdate() +"$");
                System.out.println("House have "+house.walletUpdate() +"$");
            }
            else{
                System.out.println("You win "+player.getBet()+"$, the result is "+ house.resultChecking()+"!");
                System.out.println("You have "+player.walletUpdate() +"$");
                System.out.println("House have "+house.walletUpdate() +"$");
            }
            System.out.println("------");
            if (house.getWallet() <= 0){
                System.out.println("Game end. You win! ");
                break;
            }
            else if (player.getWallet() <= 0){
                System.out.println("Game end. You loose!");
                break;
            }

            System.out.print("Do you want to continue to play?(yes/no): ");
            String answer = sc.nextLine();

            if (answer.equals("no")){
                next = false;}
            i++;
        }
    }
}

class Dice{
    private int value;
    public Dice(){
        this.value = 1;
    }

    public void roll(){
        Random rand = new Random();
        this.value = rand.nextInt(6) + 1;
    }

    public int getValue(){
        return this.value;
    }


}

class House{
    private final byte minSmall = 4;
    private final byte maxSmall = 10;
    private final byte minBig = 11;
    private final byte maxBig = 17;
    private int wallet;
    private Dice[] dices;
    public Player player;
    public boolean output ;

    public House() {
        this.wallet = 1000;}

    public House(int wallet) {
        this.wallet = wallet;
        dices = new Dice[3];
        for (int i = 0; i < 3; i++) {
            dices[i] = new Dice();
        }
    }
    public void rollAllDices() {
        for (Dice d : dices) {
            d.roll();
        }
    }
    public String getDice(){
        return  (dices[0].getValue()+" " +dices[1].getValue() +" " + dices[2].getValue());
    }

    public int sum(){
        return dices[0].getValue() +dices[1].getValue() + dices[2].getValue();
    }

    public String resultChecking(){
        int a = sum();
        if (a>= minBig && a<= maxBig){
            return "small";
        }
        else if (a>= minSmall && a<= maxSmall){
            return  "big";
        }
        else {return "same";}}

    public int getWallet() {
        return wallet;
    }

    public int walletUpdate(){
        String result = resultChecking();
        String playerResult = player.getChoice();
        if (!Objects.equals(result, playerResult)){
            wallet += player.getBet();

        }
        else {wallet -=player.getBet(); }
        return this.wallet;
    }

    public boolean getOutput(){
        String result = resultChecking();
        String playerResult = player.getChoice();
        if (!Objects.equals(result, playerResult)){
            output =true;

        }
        else {output = false;}
        return output;
    }
}


class Player{
    private int wallet;
    private int bet;
    private String choice;
    public boolean checking = true;
    public House house;

    public Player(int wallet){
        this.wallet = wallet;
    }
    public Player(){
        this.wallet = 150;
    }

    public void setChoice(String choice){
        this.choice = choice;
    }

    public String getChoice(){
        return this.choice;
    }
    public void setBet(int bet){
        if (bet <= wallet) {
            this.bet = bet;
        }
        else {
            System.out.printf("Bet must be equal or less than wallet!");
            checking = false;
    }}
    public int getBet(){return this.bet; }

    public int getWallet(){return  this.wallet;}

    public int walletUpdate(){
        String result = house.resultChecking();
        if ( result.equals(choice) ){
            wallet +=bet;
        }
        else {wallet -=bet;}
        return this.wallet;
    }
}






