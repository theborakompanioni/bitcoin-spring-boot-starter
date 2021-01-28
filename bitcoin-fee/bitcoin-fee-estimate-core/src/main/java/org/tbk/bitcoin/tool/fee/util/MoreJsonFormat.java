package org.tbk.bitcoin.tool.fee.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;

public final class MoreJsonFormat {
    private final static JsonFormat.Parser JSON_PARSER = JsonFormat.parser().ignoringUnknownFields();

    private MoreJsonFormat() {
        throw new UnsupportedOperationException();
    }

    public static <T extends Message.Builder> T jsonToProto(String json, T builder) {
        try {
            JSON_PARSER.merge(json, builder);
            return builder;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
