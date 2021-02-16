package calc;

public class Begin {
    public static void main(String[] args) {
        //   System.out.println("Hello !");
        new Thread(() -> {
            Main.main(args);
        }, "fx"
        ).start();
    }
}
