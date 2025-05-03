import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Введите первое число: ");
        int num1 = new Scanner(System.in).nextInt();
        System.out.println("Введите второе число: ");
        int num2 = new Scanner(System.in).nextInt();
        int sum = num1 + num2;
        int difference = num1 - num2;
        int product = num1 * num2;
        double quotient = (double) num1 / num2;
        System.out.println("Сумма: " + sum);
        System.out.println("Разница: " + difference);
        System.out.println("Произведение: " + product);
        System.out.println("Частное: " + quotient);
    }
}
