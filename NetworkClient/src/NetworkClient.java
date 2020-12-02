import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.List;

public class NetworkClient extends Application {

    public static final Collection<? extends String> USERS = List.of("Борис", "Тимофей", "Мартин");
    public Stage primaryStage;
    private Stage authStage;
    private Network network;
    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;

        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(NetworkClient.class.getResource("auth-dialog.fxml"));
        Parent page = authLoader.load();
        authStage = new Stage();

        authStage.setTitle("Авторизация");
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        authStage.setScene(scene);
        authStage.show();

        FXMLLoader loader = new FXMLLoader(); // loader подгружает sample.fxml
        loader.setLocation(NetworkClient.class.getResource("sample.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Chat");
        primaryStage.setScene(new Scene(root));
        //primaryStage.show();

        //настройка соединения с сервером
        // не передаем никакие значения, т.к используются дефолтные
        network = new Network();
        //получаем контроллер окна авторизации
        AuthDialogController authDialogController = authLoader.getController();
        authDialogController.setNetwork(network);
        authDialogController.setNetworkClient(this);

        if(!network.connect()){
            showErrorMessage("", "Ошибка подключения к серверу");
        }

        //чтобы передавать network надо сначала указать какой контроллер работает с, в моем случае, sample
        // для этого обращаемся к loader и теперь точно знаем, что этот controller обращается к sample
        controller = loader.getController();

        // из контроллера обратиться к network, чтобы иметь доступ к input&output networkа,
        // чтобы получать и отправлять сообщения клиента на сервер
        controller.setNetwork(network);

        //network.waitMessage(controller);
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

    public void openChat(){
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        network.waitMessage(controller);
    }
}
