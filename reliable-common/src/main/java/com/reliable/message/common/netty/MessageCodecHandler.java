package com.reliable.message.common.netty;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by 李雷 on 2019/4/30.
 */
public class MessageCodecHandler extends ByteToMessageCodec<Message> {

    public static final int HEAD_LENGTH = 4;

//    private Kryo kryo = new Kryo();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message message, ByteBuf out) throws Exception {
        byte[] body = convertToBytes(message);  //将对象转换为byte
        int dataLength = body.length;  //读取消息的长度
        out.writeInt(dataLength);  //先将消息长度写入，也就是消息头
        out.writeBytes(body);  //消息体中包含我们要发送的数据
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < HEAD_LENGTH) {  //这个HEAD_LENGTH是我们用于表示头长度的字节数。  由于Encoder中我们传的是一个int类型的值，所以这里HEAD_LENGTH的值为4.
            return;
        }
        in.markReaderIndex();                  //我们标记一下当前的readIndex的位置
        int dataLength = in.readInt();       // 读取传送过来的消息的长度。ByteBuf 的readInt()方法会让他的readIndex增加4
        if (dataLength < 0) { // 我们读到的消息体长度为0，这是不应该出现的情况，这里出现这情况，关闭连接。
            ctx.close();
        }

        if (in.readableBytes() < dataLength) { //读到的消息体长度如果小于我们传送过来的消息长度，则resetReaderIndex. 这个配合markReaderIndex使用的。把readIndex重置到mark的地方
            in.resetReaderIndex();
            return;
        }

        byte[] body = new byte[dataLength];  //传输正常
        in.readBytes(body);
        Object o = convertToObject(body);  //将byte数据转化为我们需要的对象
        out.add(o);
    }


    private Object convertToObject(byte[] body){
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        HessianInput hessianInput = new HessianInput(bis);
        Object object = null;
        try {
            object = hessianInput.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(hessianInput!=null){
                    hessianInput.close();
                }
                if (bis!=null){
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return object;
    }


    private byte[] convertToBytes(Message message){
        byte[] bytes = null;
        // 1、创建字节输出流
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        HessianOutput hessianOutput = new HessianOutput(bos);
        try {
            hessianOutput.writeObject(message);
            bytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(hessianOutput!=null){
                    hessianOutput.close();
                }
                if (bos!=null){
                    bos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bytes;
    }

}
