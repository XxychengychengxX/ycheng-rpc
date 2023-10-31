/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.encoder;

import com.ychengycheng.constant.YchengRpcRequestFormatConstant;
import com.ychengycheng.core.compress.Compressor;
import com.ychengycheng.core.compress.CompressorFactory;
import com.ychengycheng.core.serialize.Serializer;
import com.ychengycheng.core.serialize.SerializerFactory;
import com.ychengycheng.message.YchengRpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 4B magic（模数） 1B version（版本号） 2B header length（头部长度） 4B full length（总长度） 1B serialize 1B compress 1B requestType
 * 出站时第一个经过的处理器
 */
@Slf4j
public class YchengDefaultRequestRpcEncoder extends MessageToByteEncoder<YchengRpcRequest> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YchengRpcRequest ychengRpcRequest,
                          ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(YchengRpcRequestFormatConstant.MAGIC)
               .writeByte(YchengRpcRequestFormatConstant.VERSION)
               .writeShort(YchengRpcRequestFormatConstant.HEADER_LENGTH)
               .writerIndex(byteBuf.writerIndex() + YchengRpcRequestFormatConstant.FULL_FIELD_LENGTH)
               .writeByte(ychengRpcRequest.getRequestType())
               .writeByte(ychengRpcRequest.getSerializeType())
               .writeByte(ychengRpcRequest.getCompressType())
               .writeLong(ychengRpcRequest.getRequestId())
               .writeLong(ychengRpcRequest.getTimeStamp());


        //报文body封装的是请求报文里的payload
        Serializer serializer = SerializerFactory.getSerializer(ychengRpcRequest.getSerializeType()).getImpl();
        //获取压缩器
        Compressor compressor = CompressorFactory.getCompressor(ychengRpcRequest.getCompressType()).getImpl();
        //1.对请求做序列化
        byte[] payloadBytes = serializer.serialize(ychengRpcRequest.getRequestPayload());
        if (payloadBytes != null) {
            //2.对请求进行压缩
            payloadBytes = compressor.compress(payloadBytes);
            byteBuf.writeBytes(payloadBytes);
        }

        int payloadLength = payloadBytes == null ? 0 : payloadBytes.length;
        //重新封装报文的总长度
        //先保存当前写指针位置
        int nowWriteIndex = byteBuf.writerIndex();
        //写full length
        byteBuf.writerIndex(
                YchengRpcRequestFormatConstant.MAGIC.length + YchengRpcRequestFormatConstant.VERSION_LENGTH + YchengRpcRequestFormatConstant.HEADER_FIELD_LENGTH);
        byteBuf.writeInt(YchengRpcRequestFormatConstant.HEADER_LENGTH + payloadLength);
        //恢复写指针
        byteBuf.writerIndex(nowWriteIndex);

        if (log.isDebugEnabled()) {
            log.debug("请求【{}】已经完成报文编码", ychengRpcRequest.getRequestId());
        }
    }

}
