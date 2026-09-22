package br.com.leandrobrum.api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Tag("api")
class DogApiTest {
    private RequestSpecification request() {
        return new RequestSpecBuilder()
                .setBaseUri(System.getProperty("api.baseUrl", "https://dog.ceo/api"))
                .setAccept(ContentType.JSON)
                .setConfig(io.restassured.config.RestAssuredConfig.config().httpClient(
                        io.restassured.config.HttpClientConfig.httpClientConfig()
                                .setParam("http.connection.timeout", 15000)
                                .setParam("http.socket.timeout", 15000)))
                .build();
    }

    private Response get(String path, int status) {
        return given().spec(request()).log().ifValidationFails()
                .when().get(path)
                .then().log().ifValidationFails().statusCode(status)
                .contentType(ContentType.JSON).extract().response();
    }

    private void success(Response response) {
        assertEquals("success", response.path("status"), response.asString());
    }

    private void imageUrl(Object value) {
        assertInstanceOf(String.class, value, "A imagem deve ser uma URL textual");
        URI uri = URI.create((String) value);
        assertAll("URL da imagem: " + value,
                () -> assertEquals("https", uri.getScheme()),
                () -> assertEquals("images.dog.ceo", uri.getHost()),
                () -> assertTrue(uri.getPath().startsWith("/breeds/")),
                () -> assertTrue(uri.getPath().matches("(?i).+\\.(jpg|jpeg|png|webp)$")));
    }

    @Test
    @DisplayName("Lista de raças contém mapa não vazio e listas de sub-raças")
    void listsBreedsAndSubBreeds() {
        Response response = get("/breeds/list/all", 200);
        success(response);
        Object message = response.path("message");
        assertInstanceOf(Map.class, message, response.asString());
        Map<?, ?> breeds = (Map<?, ?>) message;
        assertFalse(breeds.isEmpty());
        assertTrue(breeds.containsKey("hound"), "Raça usada no exemplo da documentação");
        breeds.forEach((breed, subBreeds) -> {
            assertInstanceOf(String.class, breed);
            assertFalse(((String) breed).isBlank());
            assertInstanceOf(List.class, subBreeds, "Sub-raças de " + breed);
            ((List<?>) subBreeds).forEach(subBreed -> {
                assertInstanceOf(String.class, subBreed);
                assertFalse(((String) subBreed).isBlank());
            });
        });
    }

    @ParameterizedTest(name = "Imagens da raça {0}")
    @ValueSource(strings = {"hound", "pug"})
    void listsImagesForBreed(String breed) {
        Response response = get("/breed/" + breed + "/images", 200);
        success(response);
        Object message = response.path("message");
        assertInstanceOf(List.class, message, response.asString());
        List<?> images = (List<?>) message;
        assertFalse(images.isEmpty());
        images.forEach(value -> {
            imageUrl(value);
            String path = URI.create((String) value).getPath();
            assertTrue(path.startsWith("/breeds/" + breed + "/")
                    || path.startsWith("/breeds/" + breed + "-"), "Imagem de outra raça: " + value);
        });
    }

    @Test
    @DisplayName("Imagem aleatória retorna uma URL válida sem exigir resultados distintos")
    void returnsRandomImage() {
        Response response = get("/breeds/image/random", 200);
        success(response);
        imageUrl(response.path("message"));
    }

    @Test
    @DisplayName("Raça inexistente retorna 404 e contrato de erro")
    void rejectsUnknownBreed() {
        Response response = get("/breed/qa-nonexistent-breed-987654321/images", 404);
        assertAll("Resposta: " + response.asString(),
                () -> assertEquals("error", response.path("status")),
                () -> assertEquals(404, ((Number) response.path("code")).intValue()),
                () -> assertInstanceOf(String.class, response.path("message")),
                () -> assertFalse(response.<String>path("message").isBlank()));
    }
}
