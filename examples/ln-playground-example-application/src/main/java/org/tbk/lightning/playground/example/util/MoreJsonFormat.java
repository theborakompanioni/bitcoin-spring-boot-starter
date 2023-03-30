package org.tbk.lightning.playground.example.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

public final class MoreJsonFormat {
    private static final JsonFormat.Parser jsonParser = JsonFormat.parser().ignoringUnknownFields();
    private static final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .omittingInsignificantWhitespace()
            .sortingMapKeys();

    private MoreJsonFormat() {
        throw new UnsupportedOperationException();
    }

    public static <T extends Message.Builder> T jsonToProto(String json, T builder) {
        try {
            jsonParser.merge(json, builder);
            return builder;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends MessageOrBuilder> String protoToJson(T messageOrBuilder) {
        try {
            return jsonPrinter.print(messageOrBuilder);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}