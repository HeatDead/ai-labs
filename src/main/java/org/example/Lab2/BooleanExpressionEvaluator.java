package org.example.Lab2;

import java.util.*;

public class BooleanExpressionEvaluator {

    public static void main(String[] args) {
        // Пример использования
        Map<String, Boolean> variables = new HashMap<>();
        variables.put("A", true);
        variables.put("B", false);
        variables.put("C", true);
        variables.put("D", false);

        String expression1 = "A & B | (C ^ D)";
        String expression2 = "!A & (B | C)";
        String expression3 = "A & !B ^ (C | D)";

        System.out.println(expression1 + " = " + evaluate(expression1, variables));
        System.out.println(expression2 + " = " + evaluate(expression2, variables));
        System.out.println(expression3 + " = " + evaluate(expression3, variables));

        // Интерактивный пример
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n=== Интерактивный режим ===");
        System.out.println("Доступные переменные: A = true, B = false, C = true, D = false");
        System.out.println("Доступные операторы: & (AND), | (OR), ! (NOT), ^ (XOR)");
        System.out.println("Введите выражение (или 'exit' для выхода):");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                boolean result = evaluate(input, variables);
                System.out.println("Результат: " + result);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        scanner.close();
    }

    // Приоритеты операторов
    private static Map<Character, Integer> precedence = new HashMap<>();
    static {
        precedence.put('!', 4); // NOT - наивысший приоритет
        precedence.put('&', 3); // AND
        precedence.put('^', 2); // XOR
        precedence.put('|', 1); // OR - наименьший приоритет
        precedence.put('(', 0); // Скобка
    }

    public static boolean evaluate(String expression, Map<String, Boolean> variables) {
        // Удаляем пробелы и конвертируем в нижний регистр
        expression = expression.replaceAll("\\s+", "").toUpperCase();

        // Преобразуем инфиксное выражение в обратную польскую запись (RPN)
        List<Object> rpn = toRPN(expression);

        // Вычисляем RPN
        return evaluateRPN(rpn, variables);
    }

    private static List<Object> toRPN(String expression) {
        List<Object> output = new ArrayList<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isLetter(c)) {
                // Это переменная
                StringBuilder var = new StringBuilder();
                var.append(c);
                // Считываем всю переменную (может быть длиннее одного символа)
                while (i + 1 < expression.length() && Character.isLetterOrDigit(expression.charAt(i + 1))) {
                    var.append(expression.charAt(++i));
                }
                output.add(var.toString());
            }
            else if (c == '(') {
                operators.push(c);
            }
            else if (c == ')') {
                // Выталкиваем операторы до открывающей скобки
                while (!operators.isEmpty() && operators.peek() != '(') {
                    output.add(operators.pop());
                }
                operators.pop(); // Удаляем '('
            }
            else if (isOperator(c)) {
                // Это оператор
                while (!operators.isEmpty() &&
                        precedence.getOrDefault(operators.peek(), 0) >= precedence.get(c)) {
                    output.add(operators.pop());
                }
                operators.push(c);
            }
            else {
                throw new IllegalArgumentException("Недопустимый символ: " + c);
            }
        }

        // Выталкиваем оставшиеся операторы
        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    private static boolean evaluateRPN(List<Object> rpn, Map<String, Boolean> variables) {
        Stack<Boolean> stack = new Stack<>();

        for (Object token : rpn) {
            if (token instanceof String) {
                // Это переменная
                String varName = (String) token;
                if (!variables.containsKey(varName)) {
                    throw new IllegalArgumentException("Неизвестная переменная: " + varName);
                }
                stack.push(variables.get(varName));
            }
            else if (token instanceof Character) {
                char op = (Character) token;
                boolean result;

                if (op == '!') {
                    // Унарный оператор NOT
                    if (stack.isEmpty()) {
                        throw new IllegalArgumentException("Недостаточно операндов для оператора NOT");
                    }
                    boolean operand = stack.pop();
                    result = !operand;
                }
                else {
                    // Бинарные операторы
                    if (stack.size() < 2) {
                        throw new IllegalArgumentException("Недостаточно операндов для оператора " + op);
                    }
                    boolean b = stack.pop();
                    boolean a = stack.pop();

                    switch (op) {
                        case '&':
                            result = a && b;
                            break;
                        case '|':
                            result = a || b;
                            break;
                        case '^':
                            result = a ^ b;
                            break;
                        default:
                            throw new IllegalArgumentException("Неизвестный оператор: " + op);
                    }
                }
                stack.push(result);
            }
        }

        if (stack.size() != 1) {
            throw new IllegalArgumentException("Некорректное выражение");
        }

        return stack.pop();
    }

    private static boolean isOperator(char c) {
        return c == '&' || c == '|' || c == '^' || c == '!';
    }

    // Дополнительный метод для парсинга выражений с английскими словами
    public static boolean evaluateWithWords(String expression, Map<String, Boolean> variables) {
        // Заменяем английские операторы на символы
        expression = expression.replaceAll("\\s+", " ")
                .replaceAll("(?i)\\bAND\\b", "&")
                .replaceAll("(?i)\\bOR\\b", "|")
                .replaceAll("(?i)\\bNOT\\b", "!")
                .replaceAll("(?i)\\bXOR\\b", "^");

        return evaluate(expression, variables);
    }
}
