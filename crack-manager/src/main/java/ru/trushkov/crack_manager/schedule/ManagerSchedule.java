package ru.trushkov.crack_manager.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.trushkov.crack_manager.service.ManagerService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ManagerSchedule {
    private final ManagerService managerService;

    @Value("${workers.urls.health}")
    private List<String> urls = new ArrayList<>();

    @Scheduled(fixedRate = 10000)
    public void checkWorkerHealth() {
        managerService.sendUnsentRequest();
    }

}