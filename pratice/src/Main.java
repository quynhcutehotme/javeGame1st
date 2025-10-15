import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.printf("Input the total number of students: ");
        int num = sc.nextInt();
        sc.nextLine();
        int i = 1;
        while (i<=num){

            System.out.printf("Input student's name: ");
            String a = sc.nextLine();
            System.out.printf("Input student's major: ");
            String b = sc.nextLine().toUpperCase();
            Student s1 = new Student(a);

            s1.setMajor(b);
            s1.printInfo();
            System.out.println("---------");
            i++;
        }
    }
}
class Student {
    private static int count = 0;
    private int id;
    private String name;
    private String major;

    public Student(String name) {
        count++;
        this.id = count;
        this.name = name;
    }
    public void setMajor(String major) {
        if (major.equals("CS") || major.equals("NE")) {
            this.major = major;
        } else {
            System.out.println("Error: Major must be either CS or NE.");
        }
    }
    public void printInfo() {
        String idStr = (major == null ? "?" : major) + id;
        System.out.println("ID: " + idStr + ", Name: " + name
                + ", Major: " + (major == null ? "Not assigned" : major));
    }
}

