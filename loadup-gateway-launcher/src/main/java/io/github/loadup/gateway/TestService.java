package io.github.loadup.gateway;

import org.springframework.stereotype.Service;

@Service
public class TestService {

  public Response getData() {
    return new Response("hello world");
  }

  class Response {
    private String message;

    public Response(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }
  }
}
