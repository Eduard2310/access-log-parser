import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int requestCounter = 0;
        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();
            File file = new File(path);
            boolean fileNotFound = file.exists();
            boolean isFolder = file.isDirectory();
            if (!fileNotFound || isFolder) {
                System.out.println("Введен ошибочный путь - файл не существует/путь приводит к папке");
                continue;
            } else {
                requestCounter++;
                System.out.println("Путь указан верно");
                System.out.println("Это файл номер " + requestCounter);
            }
        }
    }

}
