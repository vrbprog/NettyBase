import commands.CommandExecutor;
import commands.StateChannelRead;
import dataBase.DataBaseHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.MetaData;
import model.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

// Класс обработчика принимаемых данных
public class BasicHandler extends ChannelInboundHandlerAdapter {

    private static final char START_META_DATA = '<'; // Символ начала метаданных
    private static final char END_META_DATA = '>';   // Символ конца метаданных
    private MetaData metaData;
    // Статус канала приема
    private StateChannelRead stateChannelRead = StateChannelRead.WAIT_META_DATA;
    private final DataBaseHandler db; // База пользователей
    private final User user; // Параметры подключившегося клиента
    private FileOutputStream toFile;
    private String filePath;
    private int sizeFile;
    private byte[] buf;

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
        StringBuilder stringBuffer = new StringBuilder();
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
                    stringBuffer.delete(0, stringBuffer.length());
                    break;

                // Ожидание приема файла
                case CREATE_FILE:

                    toFile = null;
                    final String SERVER_DIR = "Server" + File.separator + "Repositories" + File.separator;

                    filePath = (SERVER_DIR + metaData.getMetadataParams().get("path"));

                    Path path = Path.of(filePath);
                    File file = new File(path.toAbsolutePath().toString());
                    try {
                        toFile = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    sizeFile = Integer.parseInt(metaData.getMetadataParams().get("size"));

                case READING_FILE:

                    int take = 0;
                    int ready = buffer.readableBytes();
                    if(sizeFile < ready) take = sizeFile;
                    else {
                        take = ready;
                    }
                    buf = new byte[take];
                    buffer.readBytes(buf);
                    try {
                        toFile.write(buf);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    sizeFile -= take;

                    if(sizeFile == 0) {
                        try {
                            toFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int endPath = filePath.lastIndexOf(File.separator);
                        String curDir = filePath.substring(0, endPath);
                        endPath = curDir.indexOf(File.separator, 10);
                        if (endPath > 0) {
                            CommandExecutor.sendAnswerToClient(ctx,
                                    CommandExecutor.createUserListRepository(curDir.substring(endPath + 1)));
                        }
                        stateChannelRead = StateChannelRead.WAIT_META_DATA;
                    }else{
                        stateChannelRead = StateChannelRead.READING_FILE;
                    }
                    break;

                default:
                    stateChannelRead = StateChannelRead.WAIT_META_DATA;
            }
        }
        buffer.release();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Client disconnected: " + user.getAddress());
    }
}
