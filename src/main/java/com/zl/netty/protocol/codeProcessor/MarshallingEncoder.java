package com.zl.netty.protocol.codeProcessor;

import com.zl.netty.protocol.entity.ChannelBufferByteOutput;
import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.Marshaller;

import java.io.IOException;

/**
 * Created by hl on 2018/6/4.
 */
public class MarshallingEncoder {
    
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    
    private final Marshaller marshaller;


    public MarshallingEncoder() throws IOException {
        marshaller = MarshallingCodecFactory.buildMarshalling();
    }

    public void encode(Object msg, ByteBuf out) throws IOException {
        try {
            int lengthPosition = out.writerIndex();
            out.writeBytes(LENGTH_PLACEHOLDER);
            ChannelBufferByteOutput output = new ChannelBufferByteOutput(out);
            marshaller.start(output);
            marshaller.writeObject(msg);
            marshaller.finish();
            out.setInt(lengthPosition, out.writerIndex() - lengthPosition - 4);
        }finally {
            marshaller.close();
        }



    }
}
