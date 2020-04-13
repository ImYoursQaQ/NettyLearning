package lalalalisa.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class Server {
    public static void main(String[] args) throws Exception {

        Server server = new Server();
        server.listen();
    }

    public void listen() throws IOException, InterruptedException {
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(8888));

        serverChannel.accept(this, new AcceptHandler(serverChannel));

        while (true) {
            Thread.sleep(1000);
        }
    }

    private static class AcceptHandler implements CompletionHandler<AsynchronousSocketChannel, Server> {
        private final AsynchronousServerSocketChannel serverChannel;

        public AcceptHandler(AsynchronousServerSocketChannel serverChannel) {
            this.serverChannel = serverChannel;
        }

        @Override
        public void completed(AsynchronousSocketChannel client, Server attachment) {
            // 调用 accept 方法继续接收其他客户端的请求
            serverChannel.accept(attachment, this);
            try {
                System.out.println(client.getRemoteAddress());
                //1. 先分配好 Buffer，告诉内核，数据拷贝到哪里去
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                //2. 调用 read 函数读取数据，除了把 buf 作为参数传入，还传入读回调类
                client.read(buffer, buffer, new ReadHandler(client));


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void failed(Throwable exc, Server attachment) {
            exc.printStackTrace();
        }

        private static class ReadHandler implements CompletionHandler<Integer, ByteBuffer> {
            private final AsynchronousSocketChannel client;

            public ReadHandler(AsynchronousSocketChannel client) {
                this.client = client;
            }

            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                //attachment 就是数据，调用 flip 操作，其实就是把读的位置移动最前面
                attachment.flip();
                System.out.println(new String(attachment.array(), 0, result));
                client.write(ByteBuffer.wrap("HelloClient".getBytes()));
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        }
    }
}
