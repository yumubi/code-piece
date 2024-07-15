package io.goji.exp.generics.typeErasure;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JacksonDemo {
    public static void main(String[] args) throws IOException {
        String json = "[{\"id\":1,\"name\":\"bigbyto\"},{\"id\":2,\"name\":\"bigbyto2\"}]";

        List<User> users = jsonToList(json);
        System.out.println(users);
    }

    private static List<User> jsonToList(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,new TypeReference<List<User>>(){});
    }
}
