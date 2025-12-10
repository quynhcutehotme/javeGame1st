import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class test {
    BufferedImage img ;
    public test(){
        try {
            img = ImageIO.read(getClass().getResourceAsStream("/player/up1.png"));
            System.out.println("ok r á");
        }

        catch (IOException e){
            System.out.println("anh loi");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new test();
    }
}
