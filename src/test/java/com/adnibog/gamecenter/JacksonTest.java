package com.adnibog.gamecenter;

import com.adnibog.gamecenter.dto.request.UpdateFolderRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

public class JacksonTest {

    @Test
    public void test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"test\"}";
        UpdateFolderRequest req = mapper.readValue(json, UpdateFolderRequest.class);
        System.out.println("Deserialized: " + req.getName());
    }
}
