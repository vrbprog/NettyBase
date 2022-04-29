import io.netty.channel.ChannelHandlerContext;
import javafx.application.Platform;

import java.util.Map;

// Класс обработчика выполняемых команд после получения ответов от сервера
public class ClientExecutor {

    public static StateChannelRead Execute(Map<String, String> params, ChannelHandlerContext ctx, MainApp mainApp) {
        String res;
        switch (getCommand(params)) {
            case NONE:
                System.out.println("No command");
                return StateChannelRead.WAIT_META_DATA;

            case LOGIN:
                res = params.get("result");
                if("successful".equals(res)){
                    Platform.runLater(mainApp::showFileManager);
                } else if("user_not_fined".equals(res)){
                    Platform.runLater(() -> {
                        mainApp.getLoginController().errorAuth();
                    });
                }
                return StateChannelRead.WAIT_META_DATA;

            case SIGN_UP:
                res = params.get("result");
                if("successful".equals(res)){
                    Platform.runLater(mainApp::showFileManager);
                }
                return StateChannelRead.WAIT_META_DATA;
        }
        return StateChannelRead.WAIT_META_DATA;
    }

    // Определение типа принятой от сервера команды
    private static CommandType getCommand(Map<String, String> params) {
        String com = params.get("command");
        if ("login".equals(com)) return CommandType.LOGIN;
        else if ("signup".equals(com)) return CommandType.SIGN_UP;
        else return CommandType.NONE;
    }
}
