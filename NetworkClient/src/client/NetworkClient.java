package client;

import client.controllers.AuthDialogController;
import client.models.Network;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class NetworkClient extends Application {

    public static final List<String> USERS_TEST_DATA = List.of("Борис_Николаевич", "Гендальф_Серый", "Мартин_Некотов");
    public Stage primaryStage;
    private Stage authStage;
    private Network network;
    private client.controllers.ChatController chatController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        //настройка соединения с сервером
        //не передаем никакие значения, т.к используются дефолтные
        network = new Network();
        if (!network.connect()) {
            showErrorMessage("", "Ошибка подключения к серверу");
            return;
        }
        openAuthDialog(primaryStage);
        createChatDialog(primaryStage);
    }

    private void createChatDialog(Stage primaryStage) throws IOException {
        FXMLLoader mainLoader = new FXMLLoader(); //loader подгружает view.fxml (окно чата)
        mainLoader.setLocation(NetworkClient.class.getResource("client/views/chat-view.fxml"));

        Parent root = mainLoader.load();

        primaryStage.setTitle("Messenger");
        primaryStage.setScene(new Scene(root, 600, 400));

        //чтобы передавать network надо сначала указать какой контроллер работает с, в моем случае, view
        // для этого обращаемся к mainloader и теперь точно знаем, что этот controller обращается к view
        chatController = mainLoader.getController();
        // из контроллера обратиться к network, чтобы иметь доступ к input&output networkа,
        // чтобы получать и отправлять сообщения клиента на сервер
        chatController.setNetwork(network);


        primaryStage.setOnCloseRequest(event -> network.close());
    }

    private void openAuthDialog(Stage primaryStage) throws IOException {
        FXMLLoader authLoader = new FXMLLoader();
        authLoader.setLocation(NetworkClient.class.getResource("views/auth-dialog.fxml"));
        Parent page = authLoader.load();
        authStage = new Stage();

        authStage.setTitle("Авторизация");
        authStage.initModality(Modality.WINDOW_MODAL);
        authStage.initOwner(primaryStage);
        Scene scene = new Scene(page);
        authStage.setScene(scene);
        authStage.show();

        //получаем контроллер окна авторизации
        AuthDialogController authDialogController = authLoader.getController();
        authDialogController.setNetwork(network);
        authDialogController.setNetworkClient(this);
    }

    public static void showErrorMessage(String message, String errorMessage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Проблемы с соединением");
        alert.setHeaderText(errorMessage);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public void openChat() {
        authStage.close();
        primaryStage.show();
        primaryStage.setTitle(network.getUsername());
        System.out.println(network.getUsername());
        chatController.setUsernameTitle(network.getUsername());
        network.waitMessage(chatController);
    }

    public static void main(String[] args) { launch(args);}
}
