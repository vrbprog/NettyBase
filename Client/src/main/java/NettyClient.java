import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

// Класс сетевого клиента на Netty
public class NettyClient implements Runnable{
    private MainApp mainApp;
    private Channel channel;
    private EventLoopGroup eventLoopGroup;
    private static final int FILE_BLOCK_SIZE = 1024 * 1024;

    public NettyClient(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void run() {
        eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.remoteAddress("localhost", 1234);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    socketChannel.pipeline().addLast(
                            new StringEncoder(),
                            new ClientHandler(mainApp)
                    );
                }
            });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channel = channelFuture.channel();

            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    // Передача команды на сервер
    public void sendCommand(String command){
        channel.writeAndFlush(command);
    }

    // Передача файла на сервер с разделением на блоки размера FILE_BLOCK_SIZE
    public void sendFile(String fileName) {

//        byte[] mas = new byte[0];
//        try {
//            mas = Files.readAllBytes(Path.of(fileName));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        ByteBuf outBuffer = channel.alloc().buffer();
//        outBuffer.writeBytes(mas);
//        channel.writeAndFlush(outBuffer);
//    }

        try (FileChannel inputChannel = FileChannel.open(Paths.get(fileName))) {
            ByteBuffer buf = ByteBuffer.allocate(FILE_BLOCK_SIZE);
            while (inputChannel.read(buf) > 0) {
                buf.flip();
                channel.writeAndFlush(Unpooled.wrappedBuffer(buf));
                buf.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientClose(){
        channel.close();
        eventLoopGroup.shutdownGracefully();
    }
}


