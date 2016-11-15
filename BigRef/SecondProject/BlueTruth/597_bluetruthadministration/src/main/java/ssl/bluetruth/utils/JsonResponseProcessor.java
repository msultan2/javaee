/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.utils;

import java.io.IOException;
import java.io.Writer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

/**
 *
 * @author etorbett
 */
public class JsonResponseProcessor extends AbstractResponseProcessor {
    private final ObjectMapper mapper;

    public JsonResponseProcessor() {
        mapper = new ObjectMapper();
        mapper.configure(Feature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(Feature.CAN_OVERRIDE_ACCESS_MODIFIERS, false);
        mapper.getSerializationConfig().addMixInAnnotations(Object.class, ObjectSorterMixin.class);
    }

    @Override
    public void createResponse(Object responseObject, Object output) throws IOException, NullPointerException {
        if (output == null) {
            throw new NullPointerException("Output must not be null");
        }
        
        if (output instanceof Writer) {
            Writer writer = (Writer)output;
            //Convert the response to JSON format
            String jsonFormatResponse = convertResponseToJSONFormat(responseObject);
            //Transmit the response
            transmitResponse(jsonFormatResponse, writer);
            writer.flush();
        } else {
            throw new NullPointerException("Output must be writer object");
        }
    }

    private String convertResponseToJSONFormat(Object responseObject) {
        String retVal = "";
        try {
            retVal = mapper.writeValueAsString(responseObject);
        } catch (IOException ex) {
        }

        return retVal;
    }

    private void transmitResponse(String jsonFormatResponse, Writer output) throws IOException {
        output.write(jsonFormatResponse);
    }
    
    @JsonPropertyOrder(alphabetic=true)
    abstract class ObjectSorterMixin {
    }
}

