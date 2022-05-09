
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import model.MetaData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

// Класс обработчика принимаемых от сервера ответов
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final char START_META_DATA = '<';
    private static final char END_META_DATA = '>';
    private MainApp mainApp;
    private MetaData metaData;
    private StateChannelRead stateChannelRead = StateChannelRead.WAIT_META_DATA;
    private FileOutputStream toFile;
    private String filePath;
    private int sizeFile;
    private byte[] buf;

    public ClientHandler(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)  {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf buffer = (ByteBuf) msg;
        StringBuilder stringBuffer = new StringBuilder();
        char nextChar = 0;

        while((buffer.isReadable())) {
            switch (stateChannelRead) {
                case WAIT_META_DATA:
                    while (buffer.isReadable() && ((char) buffer.readByte() != START_META_DATA));
                    stateChannelRead = StateChannelRead.READING_META_DATA;
                    metaData = new MetaData();
                    stringBuffer.setLength(0);

                case READING_META_DATA:
                    while (buffer.isReadable() && (nextChar = (char) buffer.readByte()) != END_META_DATA) {
                        stringBuffer.append(nextChar);
                    }

                    metaData.buildMetadata(stringBuffer.toString());
                    if (metaData.isMetadataLoaded()) {
                        stateChannelRead = ClientExecutor.Execute(metaData.getMetadataParams(), ctx, mainApp);
                    } else {
                        stateChannelRead = StateChannelRead.WAIT_META_DATA;
                    }
                    stringBuffer.delete(0, stringBuffer.length());
                    break;

                // Ожидание приема файла
                case CREATE_FILE:

                    toFile = null;
                    String fileName = mainApp.getFileManagerController().getMyPath().toAbsolutePath().toString();

                    File file = new File(fileName + File.separator + metaData.getMetadataParams().get("path"));
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

                        Platform.runLater(() -> {
                                    mainApp.getFileManagerController().updateClientListFiles();
                                });
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
}
