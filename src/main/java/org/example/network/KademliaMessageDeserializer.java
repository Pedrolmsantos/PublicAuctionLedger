package org.example.network;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class KademliaMessageDeserializer extends StdDeserializer<RPC.RPCMessage> {

    public KademliaMessageDeserializer() {
        this(null);
    }

    protected KademliaMessageDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public RPC.RPCMessage deserialize(JsonParser parser, DeserializationContext context) throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser);
        int messageType = node.get("messageType").asInt();
        JsonNode payloadNode = node.get("payload");
        JsonNode originNode = node.get("origin");
        ObjectMapper mapper = new ObjectMapper();
        RPC.RPCMessage message = switch (messageType) {
            case 1, 3, 5, 16 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, BigInteger.class));
            case 2, 11 -> {
                List<Contact> contacts = Arrays.asList(mapper.treeToValue(payloadNode, Contact[].class));
                yield new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), contacts);
            }
            case 4, 7 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, StorePayload.class));
            case 6, 8, 10 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, Boolean.class));
            case 9 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), null);
            case 12 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, PublishPayload.class));
            case 14, 15 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, PublishBidPayload.class));
            case 13 -> new RPC.RPCMessage(messageType, mapper.treeToValue(originNode, Contact.class), mapper.treeToValue(payloadNode, SubscribePayload.class));
            default -> throw new IllegalArgumentException("Unknown message type: " + messageType);
        };
        return message;
    }
}
