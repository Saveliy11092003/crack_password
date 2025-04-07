package ru.trushkov.crack_manager.endpoint;

import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.trushkov.crack_manager.service.ManagerService;

@Endpoint
@RequiredArgsConstructor
public class ManagerEndpoint {
    private final ManagerService managerService;

    @PayloadRoot(namespace = "http://ccfit.nsu.ru/schema/crack-hash-response", localPart = "CrackHashWorkerResponse")
    public void getCountry(@RequestPayload CrackHashWorkerResponse response) {
        System.out.println("do obrabotchika");
        managerService.changeRequest(response);
    }

}
