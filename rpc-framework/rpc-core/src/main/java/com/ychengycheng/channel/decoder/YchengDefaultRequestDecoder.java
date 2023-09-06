/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.decoder;

import com.ychengycheng.constant.YchengRpcRequestFormatConstant;
import com.ychengycheng.emun.RequestType;
import com.ychengycheng.exception.CompressException;
import com.ychengycheng.message.RequestPayload;
import com.ychengycheng.message.YchengRpcRequest;
import com.ychengycheng.util.compress.Compressor;
import com.ychengycheng.util.compress.CompressorFactory;
import com.ychengycheng.util.serialize.SerializeFactory;
import com.ychengycheng.util.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class YchengDefaultRequestDecoder extends LengthFieldBasedFrameDecoder {
    public YchengDefaultRequestDecoder() {
        super(YchengRpcRequestFormatConstant.MAX_FRAME_LENGTH,
              YchengRpcRequestFormatConstant.MAGIC.length + YchengRpcRequestFormatConstant.VERSION_LENGTH + YchengRpcRequestFormatConstant.HEADER_FIELD_LENGTH,
              YchengRpcRequestFormatConstant.FULL_FIELD_LENGTH,
              -(YchengRpcRequestFormatConstant.MAGIC.length + YchengRpcRequestFormatConstant.VERSION_LENGTH + YchengRpcRequestFormatConstant.HEADER_FIELD_LENGTH + YchengRpcRequestFormatConstant.FULL_FIELD_LENGTH),
              0);

        //找到当前报文的总长度截取报文
        //长度的字段的偏移量
        //最大帧长度

    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Thread.sleep(new Random().nextInt(50));
        Object decode = super.decode(ctx, in);
        if (decode != null) {
            return decodeFrame((ByteBuf) decode);
        }

        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf) {
        //1.解析魔数
        byte[] magic = new byte[YchengRpcRequestFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        //检查魔数是否匹配
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != YchengRpcRequestFormatConstant.MAGIC[i]) {
                throw new RuntimeException("获得的请求不合法！！");
            }
        }
        //2.解析版本
        byte version = byteBuf.readByte();
        if (version > YchengRpcRequestFormatConstant.VERSION) {
            throw new RuntimeException("获得的请求版本不被支持！！");
        }
        //3.解析头部长度
        short headLength = byteBuf.readShort();
        //4.解析总长度
        int fullLength = byteBuf.readInt();
        log.info("信息的总长度：【{}】", fullLength);

        //5.请求类型
        byte requestType = byteBuf.readByte();

        //6.序列化类型
        byte serializeType = byteBuf.readByte();
        //7.压缩类型
        byte compressType = byteBuf.readByte();
        //8.请求id
        long requestId = byteBuf.readLong();
        //9.时间戳
        long timeStamp = byteBuf.readLong();
        log.info("信息的请求id：【{}】", requestId);
        //9.请求体
        //todo:心跳请求没有负载，这里可以直接判断并返回
        int payloadLength = fullLength - headLength;
        //这里需要重新封装响应
        YchengRpcRequest ychengRpcRequest = new YchengRpcRequest();
        ychengRpcRequest.setRequestType(requestType);
        ychengRpcRequest.setSerializeType(serializeType);
        ychengRpcRequest.setCompressType(compressType);
        ychengRpcRequest.setRequestId(requestId);
        ychengRpcRequest.setTimeStamp(timeStamp);
        //如果是心跳请求，直接返回
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return ychengRpcRequest;
        }
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);


        //有字节数组后可以解压缩，反序列化
        //1.解压缩
        Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
        try {
            payload = compressor.decompress(payload);
        } catch (CompressException e) {
            throw new RuntimeException(e);
        }
        //2.反序列化
        log.info("最终的请求负载如下:{}", payload);
        Serializer serializer = SerializeFactory.getSerializer(serializeType).getSerializer();
        RequestPayload requestPayload = serializer.deSerlialize(payload, RequestPayload.class);
        ychengRpcRequest.setRequestPayload(requestPayload);


        log.info("收到的请求报文对象如下：【{}】", ychengRpcRequest);
        return ychengRpcRequest;
    }
}
