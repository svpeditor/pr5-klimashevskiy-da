package ru.edu.pr02;

import java.util.Locale;
import java.util.Scanner;

public class TimesheetAnalyzer {
    private static final int DAYS = 5;
    private static final int MAX_HOURS = 16;
    // вариант 4: к норме 8 часов в день применен модификатор +25 %, получается 10 часов
    private static final int BASE_DAILY_NORM = 8;
    private static final int NORM_MODIFIER_PERCENT = 25;
    private static final int DAILY_NORM = BASE_DAILY_NORM * (100 + NORM_MODIFIER_PERCENT) / 100;
    private static final int WEEKLY_NORM = 40;
    private static final Locale RU = Locale.forLanguageTag("ru-RU");

    // ввод с клавиатуры: каждая строка - табель за неделю, пустая строка - выход
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        System.out.println("Введите часы за 5 дней через ; (например 8;9;10;8;8), пустая строка - выход");
        while (in.hasNextLine()) {
            String line = in.nextLine();
            if (line.isEmpty()) {
                break;
            }
            System.out.println(buildReport(line));
        }
    }

    // отчет по одной строке табеля или текст ошибки
    public static String buildReport(String line) {
        int[] hours;
        try {
            hours = parseHours(line);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        return String.format(RU, "Всего часов: %d%nСреднее в день: %.1f%nДней с переработкой: %d%nНедельная переработка: %d",
                total(hours), average(hours), overtimeDays(hours), weeklyOvertime(hours));
    }

    // строгий разбор: ровно 5 значений через ;, каждое целое от 0 до 16.
    // при первой ошибке бросается исключение с номером позиции, весь табель некорректен
    public static int[] parseHours(String line) {
        if (line == null || line.isEmpty()) {
            throw new IllegalArgumentException("Ошибка: пустая строка, нужно " + DAYS + " значений через ;");
        }
        // -1, чтобы split не выбрасывал пустые значения в конце строки
        String[] parts = line.split(";", -1);
        if (parts.length < DAYS) {
            throw new IllegalArgumentException("Ошибка: значений " + parts.length + " из " + DAYS
                    + ", нет значения в позиции " + (parts.length + 1));
        }
        if (parts.length > DAYS) {
            throw new IllegalArgumentException("Ошибка: значений " + parts.length + ", а нужно " + DAYS
                    + ", лишнее значение в позиции " + (DAYS + 1));
        }
        int[] hours = new int[DAYS];
        for (int i = 0; i < parts.length; i++) {
            hours[i] = parseDay(parts[i], i + 1);
        }
        return hours;
    }

    // одно значение табеля, position - номер дня с 1
    private static int parseDay(String value, int position) {
        if (value.isEmpty()) {
            throw error(position, "значение пустое");
        }
        // только цифры (можно минус), пробелы, буквы и дроби сюда не проходят
        if (!value.matches("-?\\d+")) {
            throw error(position, "\"" + value + "\" не целое число");
        }
        int hours;
        try {
            hours = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // сюда попадают только очень длинные числа, которые не помещаются в int
            throw error(position, value + " вне диапазона от 0 до " + MAX_HOURS);
        }
        if (hours < 0 || hours > MAX_HOURS) {
            throw error(position, value + " вне диапазона от 0 до " + MAX_HOURS);
        }
        return hours;
    }

    private static IllegalArgumentException error(int position, String text) {
        return new IllegalArgumentException("Ошибка в позиции " + position + ": " + text);
    }

    public static int total(int[] hours) {
        int sum = 0;
        for (int i = 0; i < hours.length; i++) {
            sum += hours[i];
        }
        return sum;
    }

    public static double average(int[] hours) {
        return (double) total(hours) / hours.length;
    }

    // переработка - день, где часов больше дневной нормы
    public static int overtimeDays(int[] hours) {
        int count = 0;
        for (int h : hours) {
            if (h > DAILY_NORM) {
                count++;
            }
        }
        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        return Math.max(0, total(hours) - WEEKLY_NORM);
    }
}
