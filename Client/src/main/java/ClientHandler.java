
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import model.MetaData;

// Класс обработчика принимаемых от сервера ответов
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private static final char START_META_DATA = '<';
    private static final char END_META_DATA = '>';
    private MainApp mainApp;
    private MetaData metaData;
    private StateChannelRead stateChannelRead = StateChannelRead.WAIT_META_DATA;

    public ClientHandler(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx)  {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        ByteBuf buffer = (ByteBuf) msg;
        StringBuffer stringBuffer = new StringBuffer();
        char nextChar = 0;

        switch (stateChannelRead) {
            case WAIT_META_DATA:
                while (buffer.isReadable() && ((char) buffer.readByte() != START_META_DATA)) ;
                stateChannelRead = StateChannelRead.READING_META_DATA;
                metaData = new MetaData();

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
                break;

            default:
                stateChannelRead = StateChannelRead.WAIT_META_DATA;

        }
    }
}
