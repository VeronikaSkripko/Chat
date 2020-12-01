package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.List;

public class EchoClient extends Application {

    public static final Collection<? extends String> USERS = List.of("Ivan", "Maria", "Stephan");

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(); // loader подгружает sample.fxml
        loader.setLocation(EchoClient.class.getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        //настройка соединения с сервером
        Network network = new Network(); // не передаем никакие значения, т.к используются дефолтные
        if(!network.connect()){
            showErrorMessage("", "Ошибка подключения к серверу");
        }

        //чтобы передавать network надо сначала указать какой контроллер работает с, в моем случае, sample
        // для этого обращаемся к loader и теперь точно знаем, что этот controller обращается к sample
        Controller controller = loader.getController();

        // из контроллера обратиться к network, чтобы иметь доступ к input&output networkа,
        // чтобы получать и отправлять сообщения клиента на сервер
        controller.setNetwork(network);

        network.waitMessage(controller);
        primaryStage.setOnCloseRequest(windowEvent -> network.close());
    }
    public static void showErrorMessage(String message, String errorMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Проблемы с соединением");
        alert.setHeaderText(errorMessage);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) { launch(args);}
}
