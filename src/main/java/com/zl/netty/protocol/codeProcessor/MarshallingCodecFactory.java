package com.zl.netty.protocol.codeProcessor;

import org.jboss.marshalling.*;

import java.io.IOException;

/**
 * Created by hl on 2018/6/4.
 */
public final class MarshallingCodecFactory {
    private static final MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
    private static final MarshallingConfiguration config = new MarshallingConfiguration();

    static {
        config.setVersion(5);
    }

    public static Marshaller buildMarshalling() throws IOException {
        return factory.createMarshaller(config);
    }

    public static Unmarshaller buildUnmarshalling() throws IOException {
        return factory.createUnmarshaller(config);
    }

}
