import java.util.*;
import src.GameSession;

 public final class Demo {
   public static void main(String[] args) {
     final Scanner input = new Scanner(System.in);
     System.out.print("Enter your nickname: ");
     final String nickname = input.nextLine();
     int balance;
     do {
       System.out.print("Enter your balance: ");
       try {
         balance = Integer.parseInt(input.nextLine());
       } catch (Exception e) {
         balance = 10000;
       }
     } while (balance < GameSession.MIN_BALANCE ||
         balance > GameSession.MAX_BALANCE);
    
     new GameSession().start(balance, nickname);
   }
 }