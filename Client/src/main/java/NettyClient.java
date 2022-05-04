import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

// Класс сетевого клиента на Netty
public class NettyClient implements Runnable{
    private MainApp mainApp;
    private Channel channel;
    private EventLoopGroup eventLoopGroup;

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

    public void sendCommand(String command){
        channel.writeAndFlush(command);
    }

    public void sendFile(String fileName){

        //TODO Write selected file
        byte[] mas = new byte[0];
        try {
            mas = Files.readAllBytes(Path.of(fileName));

        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuf outBuffer = channel.alloc().buffer();
        outBuffer.writeBytes(mas);
        channel.writeAndFlush(outBuffer);

    }


    public void clientClose(){
        channel.close();
        eventLoopGroup.shutdownGracefully();
    }
}


