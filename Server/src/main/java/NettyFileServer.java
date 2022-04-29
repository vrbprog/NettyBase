import dataBase.DataBaseHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import java.sql.SQLException;

public class NettyFileServer {
    //База с пользователями
    private final DataBaseHandler db;

    public NettyFileServer(int port) {
        db = new DataBaseHandler();
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline inbound = socketChannel.pipeline();
                            inbound.addLast(
                                    new StringEncoder(),
                                    new BasicHandler(db)
                            );
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            System.out.println("Netty FileServer started on port " + port);
            channelFuture.channel().closeFuture().sync();
            System.out.println("Netty FileServer finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            try {
                db.close();
            } catch (SQLException throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
