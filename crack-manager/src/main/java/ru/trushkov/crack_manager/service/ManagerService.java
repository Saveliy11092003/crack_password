package ru.trushkov.crack_manager.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.nsu.ccfit.schema.crack_hash_request.CrackHashManagerRequest;
import ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse;
import ru.trushkov.crack_manager.model.CrackPasswordDto;
import ru.trushkov.crack_manager.model.PasswordDto;
import ru.trushkov.crack_manager.model.PasswordRequest;
import ru.trushkov.crack_manager.model.enumeration.Status;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.trushkov.crack_manager.model.enumeration.Status.*;

@Service
public class ManagerService {

    @Value("${alphabet}")
    private List<String> symbolsOfAlphabet;

    private final ConcurrentHashMap<String, PasswordRequest> requests = new ConcurrentHashMap<>();

    private BlockingQueue<CrackPasswordDto> requestQueue = new LinkedBlockingQueue<>();

    @Value("${workers.urls.crack.task}")
    private List<String> urlsCrackPassword;

    @Value("${workers.urls.index}")
    private List<String> urlsIndex;

    @Value("${workers.count}")
    private Integer workersCount;

    private AtomicBoolean canWork = new AtomicBoolean(true);

    public ManagerService() {
        startProcessingQueue();
    }

    public void startProcessingQueue() {
        Runnable processor = () -> {
            while (true) {
                if (canWork.get()) {
                    try {
                        canWork.set(false);
                        System.out.println("can work = false");
                        CrackPasswordDto passwordDto = requestQueue.take();
                        System.out.println("startProcessingQueue " + passwordDto.getRequestId());
                        if (passwordDto != null) {
                            doRequests(passwordDto);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        };
        Thread thread = new Thread(processor);
        thread.start();
    }

    public String crackPassword(CrackPasswordDto crackPasswordDto) {
        String requestId = UUID.randomUUID().toString();
        crackPasswordDto.setRequestId(requestId);
        addNewRequest(crackPasswordDto);
        //doRequests(crackPasswordDto);
        return requestId;
    }

    public PasswordDto getPasswords(String requestId) {
        PasswordRequest passwordRequest = requests.get(requestId);
        PasswordDto passwordDto = PasswordDto.builder().data(passwordRequest.getData())
                .status(passwordRequest.getStatus()).build();
        return passwordDto;
    }

    private void addNewRequest(CrackPasswordDto crackPasswordDto) {
        requests.put(crackPasswordDto.getRequestId(), getPasswordRequest(crackPasswordDto));
        requestQueue.add(crackPasswordDto);
    }

    private PasswordRequest getPasswordRequest(CrackPasswordDto crackPasswordDto) {
        return PasswordRequest.builder().status(IN_PROGRESS).data(new CopyOnWriteArrayList<>())
                .successWork(0).maxLength(crackPasswordDto.getLength()).build();
    }

    private void doRequests(CrackPasswordDto crackPasswordDto) {
        System.out.println("do request");
        for (int i = 0; i < workersCount; i++) {
            doRequest(crackPasswordDto, i, urlsCrackPassword.get(i), crackPasswordDto.getRequestId());
        }
    }

    private void doRequest(CrackPasswordDto crackPasswordDto, Integer number, String url, String requestId) {
        RestTemplate restTemplate = new RestTemplate();
        CrackHashManagerRequest crackHashManagerRequest = createCrackHashManagerRequest(crackPasswordDto, number, requestId);
        System.out.println(crackHashManagerRequest.getAlphabet().getSymbols());
        System.out.println(crackHashManagerRequest.getRequestId());
        System.out.println(crackHashManagerRequest.getHash());
        System.out.println(crackHashManagerRequest.getAlphabet().getSymbols());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<CrackHashManagerRequest> request = new HttpEntity<>(crackHashManagerRequest, headers);
        Runnable runnable = () -> {
            restTemplate.postForObject(url, request, CrackHashManagerRequest.class);
        };
        Thread requestThread = new Thread(runnable);
        requestThread.start();
    }

    private CrackHashManagerRequest createCrackHashManagerRequest(CrackPasswordDto crackPasswordDto, Integer number, String requestId) {
        CrackHashManagerRequest crackHashManagerRequest = new CrackHashManagerRequest();
        crackHashManagerRequest.setHash(crackPasswordDto.getHash());
        crackHashManagerRequest.setRequestId(requestId);
        CrackHashManagerRequest.Alphabet alphabet = new CrackHashManagerRequest.Alphabet();
        alphabet.getSymbols().addAll(symbolsOfAlphabet);
        crackHashManagerRequest.setAlphabet(alphabet);
        crackHashManagerRequest.setMaxLength(crackPasswordDto.getLength());
        crackHashManagerRequest.setPartNumber(number);
        crackHashManagerRequest.setPartCount(workersCount);
        return crackHashManagerRequest;
    }

     synchronized public void changeRequest(CrackHashWorkerResponse response) {
        String requestId = response.getRequestId();
        requests.get(requestId).getData().addAll(response.getAnswers().getWords());
        requests.get(requestId).setSuccessWork(requests.get(requestId).getSuccessWork() + 1);
        if (requests.get(requestId).getSuccessWork() == 3) {
            requests.get(requestId).setStatus(READY);
            canWork.set(true);
            System.out.println("can work = true");
        }
    }

    synchronized public void updateRequestsAfterErrorHealthCheck() {
        for (Map.Entry<String, PasswordRequest> entry : requests.entrySet()) {
            System.out.println("success work" + entry.getValue().getSuccessWork());
            if (entry.getValue().getSuccessWork() > 0 && !entry.getValue().getSuccessWork().equals(workersCount)) {
                entry.getValue().setStatus(PARTIAL_READY);
            } else if (entry.getValue().getSuccessWork() == 0) {
                entry.getValue().setStatus(ERROR);
            }
        }
    }

    public long getPercent(String requestId) {
        RestTemplate restTemplate = new RestTemplate();
        long currentCount = 0;
        for (String url : urlsIndex) {
            Integer part = null;
            try {
                part = restTemplate.postForObject(url, requestId, Integer.class);
            } catch (Exception e) {
                System.out.println("Error while getting index : " + e.getMessage());
            }
            if (part != null) {
                currentCount += part;
            }
            System.out.println("current in cycle " + currentCount);
        }
        System.out.println("current after cycle " + currentCount);
        long totalCount = (long) Math.pow(36, requests.get(requestId).getMaxLength());
        System.out.println("total " + totalCount);
        System.out.println("100 * total " + currentCount / totalCount * 100);
        return 100 * currentCount / totalCount;
    }
}
