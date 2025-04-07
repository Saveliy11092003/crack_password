package ru.trushkov.crack_manager.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.trushkov.crack_manager.model.entity.Request;
import ru.trushkov.crack_manager.model.entity.Response;
import ru.trushkov.crack_manager.model.enumeration.Status;
import ru.trushkov.crack_manager.repository.RequestRepository;
import ru.trushkov.crack_manager.repository.ResponseRepository;

import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.trushkov.crack_manager.model.enumeration.Status.*;

@Service
@RequiredArgsConstructor
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

    private final RequestRepository repository;

    private final ResponseRepository responseRepository;

    private final AmqpTemplate amqpTemplate;

    @Value("${exchange.name}")
    private String exchangeName;

/*
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
*/
    public String crackPassword(CrackPasswordDto crackPasswordDto) {
        String requestId = UUID.randomUUID().toString();
        crackPasswordDto.setRequestId(requestId);
        System.out.println("do");
        addRequestToBD(crackPasswordDto);
        addNewRequest(crackPasswordDto);
        System.out.println(getRequest(requestId));
        doRequests(crackPasswordDto);
        System.out.println("posle");
        return requestId;
    }

    private Request getRequest(String id) {
        return repository.findById(id).get();
    }

    private void addRequestToBD(CrackPasswordDto crackPasswordDto) {
        Request request = Request.builder().id(crackPasswordDto.getRequestId()).length(crackPasswordDto.getLength())
                .hash(crackPasswordDto.getHash()).build();
        repository.save(request);
    }

    public PasswordDto getPasswords(String requestId) {
    //    PasswordRequest passwordRequest = requests.get(requestId);
     //   PasswordDto passwordDto = PasswordDto.builder().data(passwordRequest.getData())
     //           .status(passwordRequest.getStatus()).build();

        List<Response> responses = responseRepository.findAllByRequestId(requestId);
        System.out.println("RESPONSES " + responses);
        PasswordDto passwordDto1 = getPasswordDto(responses);
        System.out.println("FINALLY RESPONSE " + responses);
        return passwordDto1;
    }

    private PasswordDto getPasswordDto(List<Response> responses) {
        PasswordDto passwordDto = new PasswordDto();
        if (responses.size() >= 3) {
            passwordDto.setStatus(READY);
        } else if (!responses.isEmpty()) {
            passwordDto.setStatus(PARTIAL_READY);
        } else {
            passwordDto.setStatus(IN_PROGRESS);
        }
        Set<String> data = new HashSet<>();
        responses.forEach((r) -> data.addAll(r.getAnswers()));
        passwordDto.setData(data);
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
            doRequest(crackPasswordDto, i, crackPasswordDto.getRequestId());
        }
    }

    private void doRequest(CrackPasswordDto crackPasswordDto, Integer number, String requestId) {
        CrackHashManagerRequest crackHashManagerRequest = createCrackHashManagerRequest(crackPasswordDto, number, requestId);
        System.out.println(crackHashManagerRequest.getAlphabet().getSymbols());
        System.out.println(crackHashManagerRequest.getRequestId());
        System.out.println(crackHashManagerRequest.getHash());
        System.out.println(crackHashManagerRequest.getAlphabet().getSymbols());
        System.out.println("do convert");
        amqpTemplate.convertAndSend(exchangeName, "task.worker1", crackHashManagerRequest,
                message -> {
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                });
        System.out.println("posle convert");
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

    @RabbitListener(queues = "response_queue")
     synchronized public void changeRequest(CrackHashWorkerResponse response) {
        String requestId = response.getRequestId();
     //   System.out.println("RESPONSE " + response.getAnswers().getWords().get(0) + " " + response.getPartNumber() + " " + response.getRequestId());
    //    requests.get(requestId).getData().addAll(response.getAnswers().getWords());
    //    requests.get(requestId).setSuccessWork(requests.get(requestId).getSuccessWork() + 1);
   //     if (requests.get(requestId).getSuccessWork() == 3) {
   //         requests.get(requestId).setStatus(READY);
   //         canWork.set(true);
   //         System.out.println("can work = true");
  //      }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        addResponseToBD(response);
    }

    private void addResponseToBD(CrackHashWorkerResponse crackHashWorkerResponse) {
        Response response = Response.builder().id(UUID.randomUUID().toString()).answers(crackHashWorkerResponse.getAnswers().getWords())
                .partNumber(crackHashWorkerResponse.getPartNumber()).requestId(crackHashWorkerResponse.getRequestId()).build();
        responseRepository.save(response);
    }

    synchronized public void updateRequestsAfterErrorHealthCheck() {
        for (Map.Entry<String, PasswordRequest> entry : requests.entrySet()) {
            System.out.println("success work" + entry.getValue().getSuccessWork());
            if (entry.getValue().getSuccessWork() > 0 && !entry.getValue().getSuccessWork().equals(workersCount)) {
                entry.getValue().setStatus(PARTIAL_READY);
            }
            //else if (entry.getValue().getSuccessWork() == 0) {
            //    entry.getValue().setStatus(ERROR);
            //}
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
