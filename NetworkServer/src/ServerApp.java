import chat.MyServer;

import java.io.IOException;

public class ServerApp {
	private static final int DEFAULT_PORT = 8189;

	public static void main(String[] args) {
		int port = DEFAULT_PORT;

		//считывание порта из аргументов мейна
		if(args.length != 0){
			port = Integer.parseInt(args[0]);
		}

		//создание сервера, передаем порт и запускаем сервер
		try {
			new MyServer(port).start();
		} catch (IOException e){
			System.out.println("Ошибка!");
			e.printStackTrace();
			System.exit(1);
		}

	}


}
