package com.project.evgo.ocpp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.project.evgo.ocpp.internal.OcppMessageParser;
import com.project.evgo.ocpp.internal.OcppProtocolException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for OcppMessageParser.
 */
class OcppMessageParserTest {

    private OcppMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new OcppMessageParser();
    }

    // ==================== PARSE TESTS ====================

    @Nested
    @DisplayName("Parse Call Tests")
    class ParseCallTests {

        @Test
        @DisplayName("Should parse valid Call message")
        void parse_ValidCall_ReturnsOcppCall() {
            // Given
            String rawJson = "[2,\"msg-001\",\"BootNotification\",{\"chargePointVendor\":\"TestVendor\",\"chargePointModel\":\"TestModel\"}]";

            // When
            OcppMessage result = parser.parse(rawJson);

            // Then
            assertThat(result).isInstanceOf(OcppCall.class);
            OcppCall call = (OcppCall) result;
            assertThat(call.getMessageTypeId()).isEqualTo(2);
            assertThat(call.messageId()).isEqualTo("msg-001");
            assertThat(call.action()).isEqualTo("BootNotification");
            assertThat(call.payload().get("chargePointVendor").asText()).isEqualTo("TestVendor");
        }

        @Test
        @DisplayName("Should parse Call with empty payload")
        void parse_CallWithEmptyPayload_ReturnsOcppCall() {
            // Given
            String rawJson = "[2,\"msg-002\",\"Heartbeat\",{}]";

            // When
            OcppMessage result = parser.parse(rawJson);

            // Then
            assertThat(result).isInstanceOf(OcppCall.class);
            OcppCall call = (OcppCall) result;
            assertThat(call.action()).isEqualTo("Heartbeat");
            assertThat(call.payload().isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("Parse CallResult Tests")
    class ParseCallResultTests {

        @Test
        @DisplayName("Should parse valid CallResult message")
        void parse_ValidCallResult_ReturnsOcppCallResult() {
            // Given
            String rawJson = "[3,\"msg-001\",{\"status\":\"Accepted\",\"currentTime\":\"2026-03-08T00:00:00Z\",\"interval\":300}]";

            // When
            OcppMessage result = parser.parse(rawJson);

            // Then
            assertThat(result).isInstanceOf(OcppCallResult.class);
            OcppCallResult callResult = (OcppCallResult) result;
            assertThat(callResult.getMessageTypeId()).isEqualTo(3);
            assertThat(callResult.messageId()).isEqualTo("msg-001");
            assertThat(callResult.payload().get("status").asText()).isEqualTo("Accepted");
        }
    }

    @Nested
    @DisplayName("Parse CallError Tests")
    class ParseCallErrorTests {

        @Test
        @DisplayName("Should parse valid CallError message")
        void parse_ValidCallError_ReturnsOcppCallError() {
            // Given
            String rawJson = "[4,\"msg-001\",\"NotImplemented\",\"Requested action is not implemented\",{}]";

            // When
            OcppMessage result = parser.parse(rawJson);

            // Then
            assertThat(result).isInstanceOf(OcppCallError.class);
            OcppCallError callError = (OcppCallError) result;
            assertThat(callError.getMessageTypeId()).isEqualTo(4);
            assertThat(callError.messageId()).isEqualTo("msg-001");
            assertThat(callError.errorCode()).isEqualTo("NotImplemented");
            assertThat(callError.errorDescription()).isEqualTo("Requested action is not implemented");
        }
    }

    // ==================== PARSE ERROR TESTS ====================

    @Nested
    @DisplayName("Parse Error Tests")
    class ParseErrorTests {

        @Test
        @DisplayName("Should throw exception for invalid JSON")
        void parse_InvalidJson_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "not a json";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for empty array")
        void parse_EmptyArray_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "[]";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for invalid message type ID")
        void parse_InvalidMessageTypeId_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "[5,\"msg-001\",\"Heartbeat\",{}]";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for Call with too few elements")
        void parse_CallTooFewElements_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "[2,\"msg-001\",\"Heartbeat\"]";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for non-array JSON")
        void parse_NonArrayJson_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "{\"type\":2}";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for CallResult with too few elements")
        void parse_CallResultTooFewElements_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "[3,\"msg-001\"]";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }

        @Test
        @DisplayName("Should throw exception for CallError with too few elements")
        void parse_CallErrorTooFewElements_ThrowsOcppProtocolException() {
            // Given
            String rawJson = "[4,\"msg-001\",\"NotImplemented\"]";

            // When/Then
            assertThatThrownBy(() -> parser.parse(rawJson))
                    .isInstanceOf(OcppProtocolException.class);
        }
    }

    // ==================== SERIALIZE TESTS ====================

    @Nested
    @DisplayName("Serialize Tests")
    class SerializeTests {

        @Test
        @DisplayName("Should serialize OcppCall to JSON array")
        void serialize_OcppCall_ReturnsJsonArray() {
            // Given
            JsonNode payload = JsonNodeFactory.instance.objectNode().put("key", "value");
            OcppCall call = new OcppCall("msg-001", "Heartbeat", payload);

            // When
            String result = parser.serialize(call);

            // Then
            assertThat(result).isEqualTo("[2,\"msg-001\",\"Heartbeat\",{\"key\":\"value\"}]");
        }

        @Test
        @DisplayName("Should serialize OcppCallResult to JSON array")
        void serialize_OcppCallResult_ReturnsJsonArray() {
            // Given
            JsonNode payload = JsonNodeFactory.instance.objectNode().put("status", "Accepted");
            OcppCallResult callResult = new OcppCallResult("msg-001", payload);

            // When
            String result = parser.serialize(callResult);

            // Then
            assertThat(result).isEqualTo("[3,\"msg-001\",{\"status\":\"Accepted\"}]");
        }

        @Test
        @DisplayName("Should serialize OcppCallError to JSON array")
        void serialize_OcppCallError_ReturnsJsonArray() {
            // Given
            JsonNode errorDetails = JsonNodeFactory.instance.objectNode();
            OcppCallError callError = new OcppCallError("msg-001", "NotImplemented", "Not implemented", errorDetails);

            // When
            String result = parser.serialize(callError);

            // Then
            assertThat(result).isEqualTo("[4,\"msg-001\",\"NotImplemented\",\"Not implemented\",{}]");
        }
    }
}
