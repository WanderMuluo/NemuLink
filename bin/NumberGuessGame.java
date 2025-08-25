import java.util.Scanner;
import java.util.Random;

public class NumberGuessGame {
    public static void start() {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        int secretNumber = random.nextInt(100) + 1;
        int attempts = 0;
        boolean hasWon = false;
        
        System.out.println("欢迎来到数字猜谜游戏！");
        System.out.println("我已经想了一个1到100之间的数字，猜猜是多少？");
        
        while (attempts < 10 && !hasWon) {
            System.out.print("你的猜测(还剩" + (10 - attempts) + "次机会): ");
            int guess;
            try {
                guess = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("请输入有效数字！");
                scanner.nextLine();
                continue;
            }
            
            attempts++;
            
            if (guess < secretNumber) {
                System.out.println("太小了！");
            } else if (guess > secretNumber) {
                System.out.println("太大了！");
            } else {
                hasWon = true;
                System.out.println("恭喜你！你在" + attempts + "次尝试中猜对了！");
            }
        }
        
        if (!hasWon) {
            System.out.println("游戏结束！正确答案是: " + secretNumber);
        }
    }
}