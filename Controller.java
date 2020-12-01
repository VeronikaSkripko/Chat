package sample;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class Controller {

	private Network network;
	@FXML
	private TextField inputField;
	@FXML
	private TextArea chatHistory;
	@FXML
	private Button sendButton;
	@FXML
	private ListView<String> usersList;

	@FXML
	public void initialize() {
		usersList.setItems(FXCollections.observableArrayList(EchoClient.USERS));
		sendButton.setOnAction(event -> Controller.this.sendMessage());
		inputField.setOnAction(event -> Controller.this.sendMessage());
	}

	public void sendMessage(){

		//отправка сообщения в чат
		String message = inputField.getText();
		appendMessage(message);
		inputField.clear();

		// отправка сообщения на сервер
		try {
			network.getDataOutputStream().writeUTF(message); //связываемся с сервером и отправляем на сервер сообщение
		} catch (IOException e) {
			e.printStackTrace();
			String errorMessage = "Ошибка при отправке сообщения";
			EchoClient.showErrorMessage(e.getMessage(), errorMessage);
		}
	}
	public void appendMessage(String message){
		chatHistory.appendText(message);
		chatHistory.appendText(System.lineSeparator());
	}

	//приняли класс для socket клиента
	public void setNetwork(Network network) {
		this.network = network;
	}

	@FXML
	public void exit(){System.exit(0);}
}
