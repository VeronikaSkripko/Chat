package client.controllers;

import client.NetworkClient;
import client.models.Network;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class AuthDialogController {

	@FXML public TextField loginField;
	@FXML public PasswordField passField;

	private Network network;
	private NetworkClient networkClient;

	@FXML
	public void checkAuth() {
		//принимаем поля логина и пароля
		String login = loginField.getText();
		String password = passField.getText();
		//проверка что поля не пустые
		if(login.isBlank() || password.isBlank()){
			NetworkClient.showErrorMessage("Поля не должны быть пустыми", "Ошибка ввода" );
			return;
		}
		//обращаемся к нетворку, он отпраавляет всё на сервер, откуда придет ответ или сообщение об ошибке
		String authErrMessage = network.sendAuthCommand(login, password);
		//если ошибки нет, то открываем чат
		if(authErrMessage == null){
			networkClient.openChat();
		}
		else {
			NetworkClient.showErrorMessage(authErrMessage, "Ошибка авторизации" );
		}
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public void setNetworkClient(NetworkClient networkClient) {
		this.networkClient = networkClient;
	}
}
