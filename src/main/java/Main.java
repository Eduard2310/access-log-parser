import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
            } else {
                requestCounter++;
                System.out.println("Путь указан верно");
                System.out.println("Это файл номер " + requestCounter);

                int totalLines = 0;
                int maxLength = Integer.MIN_VALUE;
                int minLength = Integer.MAX_VALUE;

                try (FileReader fileReader = new FileReader(path);
                BufferedReader reader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int length = line.length();
                        if (length > 1024) {
                            throw new TooLongLineException("В файле обнаружена строка длиной более 1024 символов: " + length);
                        }
                        totalLines++;
                        if (length > maxLength) maxLength = length;
                        if (length < minLength) minLength = length;
                    }
                    if (totalLines == 0) {
                        minLength = 0;
                        maxLength = 0;
                    }
                    System.out.println("Общее количество строк в файле: " + totalLines);
                    System.out.println("Длина самой длинной строки: " + maxLength);
                    System.out.println("Длина самой короткой строки: " + minLength);
                } catch (TooLongLineException ex) {
                    System.out.println("Ошибка: " + ex.getMessage());
                    break;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
