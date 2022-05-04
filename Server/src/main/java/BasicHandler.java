import commands.CommandExecutor;
import commands.StateChannelRead;
import dataBase.DataBaseHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.MetaData;
import model.User;

// Класс обработчика принимаемых данных
public class BasicHandler extends ChannelInboundHandlerAdapter {

    private static final char START_META_DATA = '<'; // Символ начала метаданных
    private static final char END_META_DATA = '>';   // Символ конца метаданных
    private MetaData metaData;
    // Статус канала приема
    private StateChannelRead stateChannelRead = StateChannelRead.WAIT_META_DATA;
    private final DataBaseHandler db; // База пользователей
    private final User user; // Параметры подключившегося клиента

    public BasicHandler(DataBaseHandler db) {
        this.db = db;
        user = new User();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        user.setAddress(ctx.channel().remoteAddress());
        System.out.println("Client connected: " + user.getAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buffer = (ByteBuf) msg;
        StringBuffer stringBuffer = new StringBuffer();
        char nextChar = 0;

        // Цикл обработки входных данных
        while((buffer.isReadable())) {
            switch (stateChannelRead) {
                // Ожидание приема начала метаданных '<'
                case WAIT_META_DATA:
                    while (buffer.isReadable() && ((char) buffer.readByte() != START_META_DATA));
                    stateChannelRead = StateChannelRead.READING_META_DATA;
                    metaData = new MetaData();
                    stringBuffer.setLength(0);

                // Прием тела метаданных
                case READING_META_DATA:
                    while (buffer.isReadable() && (nextChar = (char) buffer.readByte()) != END_META_DATA) {
                        stringBuffer.append(nextChar);
                    }

                    // Конец тела метаданных и вызов обработчика принятых метаданных
                    metaData.buildMetadata(stringBuffer.toString());
                    if (metaData.isMetadataLoaded()) {
                        stateChannelRead = CommandExecutor.Execute(metaData.getMetadataParams(), db, ctx, user);
                    } else {
                        stateChannelRead = StateChannelRead.WAIT_META_DATA;
                    }
                    break;

                // Ожидание приема файла
                case READING_FILE:

                    //TODO
                    //FileLoader.loadToServer(buffer, user.getName(), fileInfo);

                    stateChannelRead = StateChannelRead.WAIT_META_DATA;
                    break;

                default:
                    stateChannelRead = StateChannelRead.WAIT_META_DATA;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected: " + user.getAddress());
    }
}
