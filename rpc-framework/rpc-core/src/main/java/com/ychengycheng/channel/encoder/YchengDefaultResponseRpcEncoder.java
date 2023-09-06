/**
 * @author Valar Morghulis
 * @Date 2023/9/3
 */
package com.ychengycheng.channel.encoder;

import com.ychengycheng.YchengYchengRPCBootstrap;
import com.ychengycheng.constant.YchengRpcRequestFormatConstant;
import com.ychengycheng.message.YchengRpcResponse;
import com.ychengycheng.util.compress.Compressor;
import com.ychengycheng.util.compress.CompressorFactory;
import com.ychengycheng.util.serialize.SerializeFactory;
import com.ychengycheng.util.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * 4B magic（模数） 1B version（版本号） 2B header length（头部长度） 4B full length（总长度） 1B serialize 1B compress 1B requestType
 * 出站时第一个经过的处理器
 */
@Slf4j
public class YchengDefaultResponseRpcEncoder extends MessageToByteEncoder<YchengRpcResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YchengRpcResponse ychengRpcResponse,
                          ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(YchengRpcRequestFormatConstant.MAGIC)
               .writeByte(YchengRpcRequestFormatConstant.VERSION)
               .writeShort(YchengRpcRequestFormatConstant.HEADER_LENGTH)
               .writerIndex(byteBuf.writerIndex() + YchengRpcRequestFormatConstant.FULL_FIELD_LENGTH)
               .writeByte(ychengRpcResponse.getCode())
               .writeByte(ychengRpcResponse.getSerializeType())
               .writeByte(ychengRpcResponse.getCompressType())
               .writeLong(ychengRpcResponse.getRequestId())
               .writeLong(ychengRpcResponse.getTimeStamp());

        //报文body封装的是请求报文里的payload
        Serializer serializer = SerializeFactory.getSerializer(YchengYchengRPCBootstrap.serializeType).getSerializer();
        //获取压缩器
        Compressor compressor = CompressorFactory.getCompressor(ychengRpcResponse.getCompressType()).getImpl();
        //1.对响应做序列化
        byte[] payloadBytes = serializer.serialize(ychengRpcResponse.getBody());
        if (payloadBytes != null) {
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
    }

}
