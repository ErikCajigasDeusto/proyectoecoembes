package es.deusto.sd.ecoembes.gateway;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import es.deusto.sd.ecoembes.dto.CapacidadPlasSBDTO;

@Component
public class PlasSBGateway {

    private final RestClient restClient;

    public PlasSBGateway(
            @Value("${plassb.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<CapacidadPlasSBDTO> consultarTodas() {

        List<CapacidadPlasSBDTO> resultado = restClient.get()
                .uri("/api/capacidades")
                .retrieve()
                .body(new ParameterizedTypeReference<
                        List<CapacidadPlasSBDTO>>() {});

        return resultado != null ? resultado : List.of();
    }
}