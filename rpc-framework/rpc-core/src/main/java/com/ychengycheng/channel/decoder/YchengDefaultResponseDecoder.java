/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.decoder;

import com.ychengycheng.constant.YchengRpcRequestFormatConstant;
import com.ychengycheng.core.compress.Compressor;
import com.ychengycheng.core.compress.CompressorFactory;
import com.ychengycheng.core.serialize.Serializer;
import com.ychengycheng.core.serialize.SerializerFactory;
import com.ychengycheng.exception.CompressException;
import com.ychengycheng.message.YchengRpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YchengDefaultResponseDecoder extends LengthFieldBasedFrameDecoder {
    public YchengDefaultResponseDecoder() {
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
        //5.请求类型
        byte responseCode = byteBuf.readByte();
        //6.序列化类型
        byte serializeType = byteBuf.readByte();
        //7.压缩类型
        byte compressType = byteBuf.readByte();
        //8.请求id
        long requestId = byteBuf.readLong();
        //9.时间戳
        long timestamp = byteBuf.readLong();
        //9.请求体
        //todo:心跳请求没有负载，这里可以直接判断并返回
        int bodyLength = fullLength - headLength;
        //这里需要重新封装响应
        YchengRpcResponse ychengRpcResponse = new YchengRpcResponse();
        ychengRpcResponse.setCode(responseCode);
        ychengRpcResponse.setSerializeType(serializeType);
        ychengRpcResponse.setCompressType(compressType);
        ychengRpcResponse.setRequestId(requestId);
        ychengRpcResponse.setTimeStamp(timestamp);

        //todo:如果是心跳请求，直接返回
        /*if (responseCode == RequestType.HEART_BEAT.getId()) {
            return ychengRpcResponse;
        }
*/

        if (bodyLength == 0) {
            return ychengRpcResponse;
        }
        byte[] payload = new byte[bodyLength];
        byteBuf.readBytes(payload);
        log.info("最终的请求负载如下:{}", payload);
        Serializer serializer = (Serializer) SerializerFactory.getSerializer(serializeType).getImpl();
        Compressor compressor = CompressorFactory.getCompressor(compressType).getImpl();
        try {
            payload = compressor.decompress(payload);
        } catch (CompressException e) {
            log.error("对字节数组进行解压缩时发生异常:【{}】", e.getMessage());

        }
        Object requestPayload = serializer.deSerlialize(payload, Object.class);

        ychengRpcResponse.setBody(requestPayload);

        log.info("最终获取的相应如下：【{}】", ychengRpcResponse);


        return ychengRpcResponse;
    }
}
