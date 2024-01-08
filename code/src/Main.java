import business.PostoTrabalho;
import data.PostoTrabalhoDAO;
import ui.Controller;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller(new Scanner(System.in));
        controller.start();
    }
}