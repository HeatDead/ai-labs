package org.example.Lab1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Scanner;

public class SimpleAnimalGame {

    static class Node {
        String question;
        Node yes;
        Node no;
        boolean isAnswer;

        // Конструктор для Jackson с аннотациями
        @JsonCreator
        public Node(
                @JsonProperty("question") String question,
                @JsonProperty("isAnswer") boolean isAnswer,
                @JsonProperty("yes") Node yes,
                @JsonProperty("no") Node no) {
            this.question = question;
            this.isAnswer = isAnswer;
            this.yes = yes;
            this.no = no;
        }

        // Конструктор для создания простых узлов
        public Node(String question, boolean isAnswer) {
            this(question, isAnswer, null, null);
        }

        // Геттеры нужны для сериализации
        public String getQuestion() { return question; }
        public boolean isAnswer() { return isAnswer; }
        public Node getYes() { return yes; }
        public Node getNo() { return no; }

        // Сеттеры нужны для десериализации
        public void setQuestion(String question) { this.question = question; }
        public void setAnswer(boolean answer) { isAnswer = answer; }
        public void setYes(Node yes) { this.yes = yes; }
        public void setNo(Node no) { this.no = no; }
    }

    private Node root;
    private Scanner scanner = new Scanner(System.in);
    private ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        new SimpleAnimalGame().run();
    }

    void run() throws Exception {
        File file = new File("game.json");

        if (file.exists()) {
            System.out.println("Загружаю сохраненную игру...");
            root = mapper.readValue(file, Node.class);
        } else {
            System.out.println("Создаю новую игру...");
            root = new Node("кот", true);
        }

        System.out.println("\n=== Игра 'Угадай животное' ===");
        System.out.println("Отвечайте 'да' или 'нет'\n");

        boolean playAgain;
        do {
            System.out.println("Загадайте животное...");
            play(root);
            System.out.print("\nХотите сыграть еще раз? (да/нет): ");
            String response = scanner.nextLine().toLowerCase();
            playAgain = response.startsWith("д") || response.startsWith("y");
            System.out.println();
        } while (playAgain);

        // Сохраняем игру
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
        System.out.println("Игра сохранена в файл: " + file.getAbsolutePath());
        System.out.println("До свидания!");
    }

    void play(Node node) {
        if (node.isAnswer) {
            System.out.print("Это " + node.question + "? ");
            String response = scanner.nextLine().toLowerCase();

            if (response.startsWith("д") || response.startsWith("y")) {
                System.out.println("Ура! Я угадал!");
            } else {
                learn(node);
            }
        } else {
            System.out.print(node.question + " ");
            String response = scanner.nextLine().toLowerCase();
            if (response.startsWith("д") || response.startsWith("y")) {
                if (node.yes != null) {
                    play(node.yes);
                } else {
                    System.out.println("Ошибка: нет следующего вопроса");
                }
            } else {
                if (node.no != null) {
                    play(node.no);
                } else {
                    System.out.println("Ошибка: нет следующего вопроса");
                }
            }
        }
    }

    void learn(Node wrong) {
        System.out.print("Какое животное вы загадали?: ");
        String animal = scanner.nextLine().trim();

        System.out.print("Чем " + animal + " отличается от " + wrong.question + "?: ");
        String difference = scanner.nextLine().trim();

        // Создаем новый вопрос
        String oldAnimal = wrong.question;
        wrong.question = difference.endsWith("?") ? difference : difference + "?";
        wrong.isAnswer = false;

        // Создаем новые узлы для животных
        Node newAnimalNode = new Node(animal, true);
        Node oldAnimalNode = new Node(oldAnimal, true);

        System.out.print("Для " + animal + " это отличие верно? (да/нет): ");
        String response = scanner.nextLine().toLowerCase();

        if (response.startsWith("д") || response.startsWith("y")) {
            wrong.yes = newAnimalNode;
            wrong.no = oldAnimalNode;
        } else {
            wrong.yes = oldAnimalNode;
            wrong.no = newAnimalNode;
        }

        System.out.println("Спасибо! Я научился новому животному!");
    }
}
