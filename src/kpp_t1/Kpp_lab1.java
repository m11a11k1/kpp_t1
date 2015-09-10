/*
 * shitty 2048 with save and other fcking functions
 */
package kpp_t1;

import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.*;
import java.util.Random;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * основной класс для рассчетов и распределения действий
 * @author m11a11k1
 */
public class Kpp_lab1 extends Application {

    @Override
    /*
    *основной метод, реализация главного меню
    *@param primaryStage - окно игры
    *
    */
    public void start(Stage primaryStage) {                                     //основной метод, реальзиция главного меню
        Button btn = new Button();                                              //кнопка новой игры
        btn.setText("New game");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                diff(primaryStage);
            }
        });

        StackPane root = new StackPane();                                       //окно
        root.getChildren().add(btn);

        Scene mainscene = new Scene(root, 500, 500);

        primaryStage.setTitle("2048");
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setScene(mainscene);
        primaryStage.show();

    }

    /**
     * отрисовка окна сложности и ее выбор или же сбор статистики
     * @param secondaryStage - окно отрисовки
     */
    public void diff(Stage secondaryStage) {
        Button btn_easy = new Button();
        btn_easy.setLayoutY(200);
        btn_easy.setLayoutX(230);
        btn_easy.setText("Easy");
        Button btn_hard = new Button();
        btn_hard.setLayoutY(250);
        btn_hard.setLayoutX(230);
        btn_hard.setText("Hard");
        Button btn_sort = new Button();
        btn_sort.setLayoutY(300);
        btn_sort.setLayoutX(230);
        btn_sort.setText("Sort");
        Button btn_stat = new Button();
        btn_stat.setLayoutY(150);
        btn_stat.setLayoutX(230);
        btn_stat.setText("Stat");
        Group root = new Group();
        root.getChildren().add(btn_easy);
        root.getChildren().add(btn_hard);
        root.getChildren().add(btn_sort);
        root.getChildren().add(btn_stat);
        btn_easy.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                game_easy(secondaryStage, 0);
            }
        });
        btn_hard.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                game_hard(secondaryStage, 0);
            }
        });
        btn_sort.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                sort_changer(secondaryStage);
            }
        });
        btn_stat.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                statistic p = new statistic(secondaryStage);
                p.main();
                //stat(secondaryStage);
            }
        });
        Scene difficultyscene = new Scene(root, 500, 500);
        secondaryStage.setTitle("Difficulty");
        secondaryStage.setScene(difficultyscene);
        secondaryStage.show();
    }

    /**
     * игровое поле
     */
    static int[][] numbers = new int[4][4];                                       //игровое поле
    /**
     * статус бота
     */
    static boolean bot = false;                                                   //включен или выключен бот
    /**
     * остаток ходов бота
     */
    static int botsteps = 0;                                                      //количество ходов бота
    /**
     * статус загрузки из файла
     */
    static boolean loadbot = false;
    /**
     * количество ходов для подгрузки
     */
    static int stepstogo = 0;
    /**
     * файл записи последовательности ходов
     */
    static FileOutputStream fos_step;
    /**
     * поток записи последовательности ходов
     */
    static ObjectOutputStream outStream_step;
    /**
     * поток чтения последовательности ходов
     */
    static FileInputStream fis_step;
    /**
     * поток чтения последовательности ходов
     */
    static ObjectInputStream inStream_step;
    /**
     * файл записи последовательности ходов в аннотации понятной любому
     */
    static FileOutputStream fos_step_lr7;
    /**
     * поток записи последовательности ходов в аннотации понятной любому
     */
    static ObjectOutputStream outStream_step_lr7;
    /**
     * массив хранящий статистику каждого файла
     */
    static int[] files = new int[10000];
    /**
     * массив с индексами всех файлов для удобства сортировки
     */
    static int[] filesid = new int[10000];

    /**
     * метод инициализации игрового поля
     * @param tempStage - окно отрисовки
     */
    static public void init(Stage tempStage) //метод инициализации игрового поля
    {
        int i, j, x, y;
        Random rnd = new Random();

        for (i = 0; i < 4; i++) //заливка поля 0
        {
            for (j = 0; j < 4; j++) {
                numbers[i][j] = 0;
            }
        }

        for (i = 0; i < 2; i++) //и создание 2 клеток с 2
        {
            x = rnd.nextInt() % 4;
            if (x < 0) {
                x = (-1) * x;
            }
            y = rnd.nextInt() % 4;
            if (y < 0) {
                y = (-1) * y;
            }
            if (numbers[x][y] == 0) {
                numbers[x][y] = 2;
            } else {
                i--;
            }
        }
    }

    /**
     * функция загрузки действия из файла при необходимости
     * @param diff - выбранная сложность
     * @param viewStage - окно отрисовки
     */
    static public void load_step(int diff, Stage viewStage) {
        if (stepstogo > 0) {
            stepstogo--;
        } else {
            loadbot = false;
            if (diff == 0) {
                game_easy(viewStage, 228);
            } else {
                game_hard(viewStage, 228);
            }
        }
        try {
            if (diff == 0) {
                game_easy(viewStage, inStream_step.readInt());
            } else {
                game_hard(viewStage, inStream_step.readInt());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * проверка на проигрыш
     *
     * @return true - если игрок проиграл
     */
    static public boolean check_lost() //проверка на проигрыш
    {
        int i, j;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                if (numbers[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * проверка на выигрыш
     *
     * @return true - если игрок выиграл
     */
    static public boolean check_won() //проверка на выигрыш
    {
        int i, j;
        for (i = 0; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                if (numbers[i][j] == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * обработка нажатия кнопки вверх
     *
     * @param tempStage true - окно отрисовки
     */
    static public void btn_up_prsed(Stage tempStage) //обработка нажатия кнопки вверх
    {
        int i, j, jp;
        int x, y;
        Random rnd = new Random();
        for (i = 0; i < 4; i++) //для каждой клетки
        {
            for (j = 1; j < 4; j++) {
                if (numbers[i][j] != 0) //если в ней не 0
                {
                    jp = j - 1;                                                     //проверить можно ли сместить
                    while (numbers[i][jp] == 0 && jp > 0) {
                        jp--;                        //и на сколько
                    }
                    if (numbers[i][jp] == numbers[i][j]) //если есть возможность соеденить
                    {
                        numbers[i][j] = 0;
                        numbers[i][jp] *= 2;
                    } else {
                        if (numbers[i][jp] == 0) {
                            numbers[i][jp] = numbers[i][j];
                            numbers[i][j] = 0;
                        } else {
                            if (numbers[i][jp + 1] == 0) {
                                numbers[i][jp + 1] = numbers[i][j];
                                numbers[i][j] = 0;
                            }
                        }
                    }
                }
            }
        }
        if (check_lost()) {
            end_game_lose(tempStage);
            return;
        }                     //проверка на выигрыш или проигрыш
        if (check_won()) {
            end_game_won(tempStage);
            return;
        }
        for (i = 0; i < 1; i++) //добавить 1 2
        {
            x = rnd.nextInt() % 4;
            if (x < 0) {
                x = (-1) * x;
            }
            y = rnd.nextInt() % 4;
            if (y < 0) {
                y = (-1) * y;
            }
            if (numbers[x][y] == 0) {
                numbers[x][y] = 2;
            } else {
                i--;
            }
        }
    }

    /**
     *
     * аналогично обработка нажатия кнопки вниз
     *
     * @param tempStage - окно отрисовки
     */
    static public void btn_down_prsed(Stage tempStage) //аналогично обработка нажатия кнопки вниз
    {
        int i, j, jp;
        int x, y;
        Random rnd = new Random();

        for (i = 0; i < 4; i++) {
            for (j = 2; j >= 0; j--) {
                if (numbers[i][j] != 0) {
                    jp = j + 1;
                    while (numbers[i][jp] == 0 && jp < 3) {
                        jp++;
                    }
                    if (numbers[i][jp] == numbers[i][j]) {
                        numbers[i][j] = 0;
                        numbers[i][jp] *= 2;
                    } else {
                        if (numbers[i][jp] == 0) {
                            numbers[i][jp] = numbers[i][j];
                            numbers[i][j] = 0;
                        } else {
                            if (numbers[i][jp - 1] == 0) {
                                numbers[i][jp - 1] = numbers[i][j];
                                numbers[i][j] = 0;
                            }
                        }
                    }
                }
            }
        }
        if (check_lost()) {
            end_game_lose(tempStage);
            return;
        }
        if (check_won()) {
            end_game_won(tempStage);
            return;
        }
        for (i = 0; i < 1; i++) {
            x = rnd.nextInt() % 4;
            if (x < 0) {
                x = (-1) * x;
            }
            y = rnd.nextInt() % 4;
            if (y < 0) {
                y = (-1) * y;
            }
            if (numbers[x][y] == 0) {
                numbers[x][y] = 2;
            } else {
                i--;
            }
        }
    }

    /**
     *
     * аналогично обработка нажатия кнопки вниз
     *
     * @param tempStage - окно отрисовки
     */
    static public void btn_left_prsed(Stage tempStage) //аналогично обработка нажатия кнопки вниз
    {
        int i, j, jp;
        int x, y;
        Random rnd = new Random();

        for (i = 1; i < 4; i++) {
            for (j = 0; j < 4; j++) {
                if (numbers[i][j] != 0) {
                    jp = i - 1;
                    while (numbers[jp][j] == 0 && jp > 0) {
                        jp--;
                    }
                    if (numbers[jp][j] == numbers[i][j]) {
                        numbers[i][j] = 0;
                        numbers[jp][j] *= 2;
                    } else {
                        if (numbers[jp][j] == 0) {
                            numbers[jp][j] = numbers[i][j];
                            numbers[i][j] = 0;
                        } else {
                            if (numbers[jp + 1][j] == 0) {
                                numbers[jp + 1][j] = numbers[i][j];
                                numbers[i][j] = 0;
                            }
                        }
                    }
                }
            }
        }
        if (check_lost()) {
            end_game_lose(tempStage);
            return;
        }
        if (check_won()) {
            end_game_won(tempStage);
            return;
        }
        for (i = 0; i < 1; i++) {
            x = rnd.nextInt() % 4;
            if (x < 0) {
                x = (-1) * x;
            }
            y = rnd.nextInt() % 4;
            if (y < 0) {
                y = (-1) * y;
            }
            if (numbers[x][y] == 0) {
                numbers[x][y] = 2;
            } else {
                i--;
            }
        }
    }

    /**
     *
     * аналогично обработка нажатия кнопки вниз
     *
     * @param tempStage - окно отрисовки
     */
    static public void btn_right_prsed(Stage tempStage) //аналогично обработка нажатия кнопки вниз
    {
        int i, j, jp;
        int x, y;
        Random rnd = new Random();

        for (i = 2; i >= 0; i--) {
            for (j = 0; j < 4; j++) {
                if (numbers[i][j] != 0) {
                    jp = i + 1;
                    while (numbers[jp][j] == 0 && jp < 3) {
                        jp++;
                    }
                    if (numbers[jp][j] == numbers[i][j]) {
                        numbers[i][j] = 0;
                        numbers[jp][j] *= 2;
                    } else {
                        if (numbers[jp][j] == 0) {
                            numbers[jp][j] = numbers[i][j];
                            numbers[i][j] = 0;
                        } else {
                            if (numbers[jp - 1][j] == 0) {
                                numbers[jp - 1][j] = numbers[i][j];
                                numbers[i][j] = 0;
                            }
                        }
                    }
                }
            }
        }
        if (check_lost()) {
            end_game_lose(tempStage);
            return;
        }
        if (check_won()) {
            end_game_won(tempStage);
            return;
        }
        for (i = 0; i < 1; i++) {
            x = rnd.nextInt() % 4;
            if (x < 0) {
                x = (-1) * x;
            }
            y = rnd.nextInt() % 4;
            if (y < 0) {
                y = (-1) * y;
            }
            if (numbers[x][y] == 0) {
                numbers[x][y] = 2;
            } else {
                i--;
            }
        }
    }

    /**
     *
     * обработка для легкого уровня сложности
     *
     * @param thirdStage - окно отрисовки
     * @param btn - нажатая кнопка
     */
    static public void game_easy(Stage thirdStage, int btn) //обработка для легкого уровня сложности
    {

        Random rnd = new Random();
        if (bot) {
            btn = rnd.nextInt() % 4;
            if (btn < 0) {
                btn = (-1) * btn;
            }
            btn++;
        }         //если бот включен нажать любую кнопку
        try {
            outStream_step.writeInt(btn);
            if (btn == 0) {
                outStream_step_lr7.writeChars("init(easy) |");
            }
            if (btn == 1) {
                outStream_step_lr7.writeChars("   up  |");
            }
            if (btn == 2) {
                outStream_step_lr7.writeChars("  down |");
            }
            if (btn == 3) {
                outStream_step_lr7.writeChars("  left |");
            }
            if (btn == 4) {
                outStream_step_lr7.writeChars(" right |");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (btn == 0) {
            init(thirdStage);
        }
        if (btn == 1) {
            btn_up_prsed(thirdStage);
        }
        if (btn == 2) {
            btn_down_prsed(thirdStage);
        }
        if (btn == 3) {
            btn_left_prsed(thirdStage);
        }
        if (btn == 4) {
            btn_right_prsed(thirdStage);
        }

        if (check_lost()) {
            end_game_lose(thirdStage);
            return;
        }
        if (check_won()) {
            end_game_won(thirdStage);
            return;
        }
        thread_client thr = new thread_client(thirdStage, 0);                    //результат вывести на экран
        thr.run();
        try {
            thr.join();
        } catch (InterruptedException e) {
        };
    }

    /**
     *
     * обработка для сложного уровня сложности
     *
     * @param thirdStage - окно отрисовки
     * @param btn - нажатая кнопка
     */
    static public void game_hard(Stage thirdStage, int btn) //обработка для сложного уровня сложности
    {

        Random rnd = new Random();
        if (bot) {
            btn = rnd.nextInt() % 4;
            if (btn < 0) {
                btn = (-1) * btn;
            }
            btn++;
        }         //бот
        try {
            outStream_step.writeInt(btn);
            if (btn == 0) {
                outStream_step_lr7.writeChars("init(hard) |");
            }
            if (btn == 1) {
                outStream_step_lr7.writeChars("   up  |");
            }
            if (btn == 2) {
                outStream_step_lr7.writeChars("  down |");
            }
            if (btn == 3) {
                outStream_step_lr7.writeChars("  left |");
            }
            if (btn == 4) {
                outStream_step_lr7.writeChars(" right |");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int i, j = rnd.nextInt() % 100, x, y;
        if (btn == 0) {
            init(thirdStage);
        }
        if (btn == 1) {
            btn_up_prsed(thirdStage);
        }
        if (btn == 2) {
            btn_down_prsed(thirdStage);
        }
        if (btn == 3) {
            btn_left_prsed(thirdStage);
        }
        if (btn == 4) {
            btn_right_prsed(thirdStage);
        }

        if (check_lost()) {
            end_game_lose(thirdStage);
            return;
        }
        if (check_won()) {
            end_game_won(thirdStage);
            return;
        }
        if (j <= 10 && j >= -10) //с вероятностью в 10 процентов удалить любую не 0 клетку
        {
            for (i = 0; i < 1; i++) {
                x = rnd.nextInt() % 4;
                if (x < 0) {
                    x = (-1) * x;
                }
                y = rnd.nextInt() % 4;
                if (y < 0) {
                    y = (-1) * y;
                }
                if (numbers[x][y] != 0) {
                    numbers[x][y] = 0;
                } else {
                    i--;
                }
            }
        }
        j = rnd.nextInt() % 100;
        if (j <= 20 && j >= -20) //а с верятностью 20 добавить еще 1 2
        {
            for (i = 0; i < 1; i++) {
                x = rnd.nextInt() % 4;
                if (x < 0) {
                    x = (-1) * x;
                }
                y = rnd.nextInt() % 4;
                if (y < 0) {
                    y = (-1) * y;
                }
                if (numbers[x][y] == 0) {
                    numbers[x][y] = 2;
                } else {
                    i--;
                }
            }
        }

        thread_client thr = new thread_client(thirdStage, 0);                    //результат вывести на экран
        thr.run();
        try {
            thr.join();
        } catch (InterruptedException e) {
        };
    }

    /**
     *
     * вывод на экран сообщения о победе
     *
     * @param lastStage - окно отрисовки
     */
    static public void end_game_won(Stage lastStage) //вывод на экран сообщения о победе
    {
        Button btn = new Button();
        btn.setText("YOU WON!!!");
        btn.setOnAction(new EventHandler<ActionEvent>() {                       //вывод единственной кнопки нажатие на которую запускает главный экран

            @Override
            public void handle(ActionEvent event) {
                //start(lastStage);
                System.exit(1);
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene mainscene = new Scene(root, 500, 500);

        lastStage.setTitle("2048");
        lastStage.setScene(mainscene);
        lastStage.show();

        bot = false;
    }

    /**
     *
     * вывод на экран сообщения о проигрыше
     *
     * @param lastStage - окно отрисовки
     */
    static public void end_game_lose(Stage lastStage) //вывод на экран сообщения о проигрыше
    {
        Button btn = new Button();
        btn.setText("you lost, noob");
        btn.setOnAction(new EventHandler<ActionEvent>() {                       //вывод единственной кнопки нажатие на которую запускает главный экран

            @Override
            public void handle(ActionEvent event) {
                //start(lastStage);
                System.exit(0);
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        Scene mainscene = new Scene(root, 500, 500);

        lastStage.setTitle("2048");
        lastStage.setScene(mainscene);
        lastStage.show();
        bot = false;

    }

    /**открытие потоков для чтения/записи сохранения, 
     * создание нового потока в котором будет выполнятся приложение
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            fos_step = new FileOutputStream("savedsteps_temp.txt");
            outStream_step = new ObjectOutputStream(fos_step);
            fos_step_lr7 = new FileOutputStream("savedsteps_lr7.txt");
            outStream_step_lr7 = new ObjectOutputStream(fos_step_lr7);
            fis_step = new FileInputStream("savedsteps.txt");
            inStream_step = new ObjectInputStream(fis_step);
        } catch (IOException e) {
            e.printStackTrace();
        }
        launch(args);

        try {
            outStream_step.close();
            outStream_step_lr7.close();
            inStream_step.close();
            fos_step.close();
            fos_step_lr7.close();
            fis_step.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File source = new File("savedsteps_temp.txt");
            File dest = new File("savedsteps.txt");
            dest.delete();
            Files.copy(source.toPath(), dest.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * функция выбора языка для сортировки и поиска луучши игр
     * @param secondaryStage - окно отрисовки
     */
    public void sort_changer(Stage secondaryStage) {
        for (int i = 0; i < 10000; i++) {
            String filename = new String();
            filename = "savedsteps" + Integer.toString(i) + ".txt";
            try {
                fis_step = new FileInputStream(filename);
                inStream_step = new ObjectInputStream(fis_step);

                files[i] = inStream_step.available() / 4;
                inStream_step.close();
                fis_step.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Button btn_java = new Button();
        btn_java.setLayoutY(200);
        btn_java.setLayoutX(230);
        btn_java.setText("Java");
        Button btn_scala = new Button();
        btn_scala.setLayoutY(250);
        btn_scala.setLayoutX(230);
        btn_scala.setText("Scala");

        btn_java.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                long timeout = System.currentTimeMillis();
                for (int i = 0; i < 10000; i++) {
                    filesid[i] = i;
                }
                qsort(0, 9999);
                try {
                    fos_step = new FileOutputStream("savedarr_sorted.txt");
                    outStream_step = new ObjectOutputStream(fos_step);
                    for (int i = 0; i < 10000; i++) {
                        outStream_step.writeChars(Integer.toString(filesid[i]) + ", ");
                    }
                    outStream_step.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                timeout = System.currentTimeMillis() - timeout;
                System.out.print(" Java QS:");
                System.out.print(timeout);
            }

        });
        btn_scala.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                t11 qs_scala = new t11();
                qs_scala.main1(files);
            }
        });

        Group root = new Group();
        root.getChildren().add(btn_java);
        root.getChildren().add(btn_scala);

        Scene difficultyscene = new Scene(root, 500, 500);
        secondaryStage.setTitle("Sorts");
        secondaryStage.setScene(difficultyscene);
        secondaryStage.show();
    }

    /**
     * быстрая сортировка на java
     * @param low - начало обрабатываемого участка массива
     * @param high - конец обрабатываемого участка массива
     */
    public static void qsort(int low, int high) {
        int i = low, j = high;
        int pivot = files[low + (high - low) / 2];
        while (i <= j) {
            while (files[i] < pivot) {
                i++;
            }
            while (files[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = files[i];
                files[i] = files[j];
                files[j] = temp;
                temp = filesid[i];
                filesid[i] = filesid[j];
                filesid[j] = temp;
                i++;
                j--;
            }
        }
        if (low < j) {
            qsort(low, j);
        }
        if (i < high) {
            qsort(i, high);
        }
    }

    /**
     * функция сбора статистики. запускается из scala, 
     * параллельно актором запускается сбор статистики scal'ой
     * @param secondaryStage - окно отрисовки
     */
    public static void stat(Stage secondaryStage) {
        long timeout = System.currentTimeMillis();
        float[] st = new float[4];
        float sum = 0;
        for (int i = 0; i < 10000; i++) {
            String filename = new String();
            filename = "savedsteps" + Integer.toString(i) + ".txt";
            try {
                fis_step = new FileInputStream(filename);
                inStream_step = new ObjectInputStream(fis_step);

                files[i] = inStream_step.available() / 4;
                sum += files[i] / Integer.BYTES;
                inStream_step.readInt();
                for (int j = 0; j < files[i] / Integer.BYTES; j++) {
                    st[inStream_step.readInt() - 1]++;
                }
                inStream_step.close();
                fis_step.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        timeout = System.currentTimeMillis() - timeout;
        System.out.print(" Java STAT:");
        System.out.print(timeout);
        Label[] lb = new Label[4];
        lb[0] = new Label("Up " + Float.toString(st[0] / sum));
        lb[1] = new Label("Down " + Float.toString(st[1] / sum));
        lb[2] = new Label("Left " + Float.toString(st[2] / sum));
        lb[3] = new Label("Rigth " + Float.toString(st[3] / sum));
        lb[1].setLayoutX(50);
        lb[2].setLayoutX(250);
        lb[3].setLayoutX(50);
        lb[0].setLayoutX(250);
        lb[1].setLayoutY(50);
        lb[2].setLayoutY(50);
        lb[3].setLayoutY(250);
        lb[0].setLayoutY(250);

        Group root = new Group();
        root.getChildren().addAll(lb[0], lb[1], lb[2], lb[3]);

       // lr6 ltt = new lr6();
        // ltt.main();
        Scene difficultyscene = new Scene(root, 500, 500);
        secondaryStage.setTitle("Stats");
        secondaryStage.setScene(difficultyscene);
        secondaryStage.show();
    }
}
