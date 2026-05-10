package com.example.dateon;

import com.example.dateon.Models.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update")
@ActiveProfiles("test")
public class LoadTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private com.example.dateon.Service.AIService aiService;

    @Autowired
    private com.example.dateon.Service.UserServices userServices;

    @Autowired
    private com.example.dateon.Service.JwtService jwtService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.dateon.Kafka.KafkaProducer kafkaProducer;

    @Test
    public void testConcurrentUsersLoad() throws InterruptedException, ExecutionException {
        int numberOfThreads = 50;
        int requestsPerThread = 10;
        int totalRequests = numberOfThreads * requestsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Boolean>> tasks = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            tasks.add(() -> {
                try {
                    Users user = new Users();
                    String uniqueId = "user" + index + "_" + System.currentTimeMillis();
                    user.setName("LoadTestUser_" + index);
                    user.setMail(uniqueId + "@example.com");
                    user.setPassword("password");
                    user.setAge(25);
                    user.setGender("Male");
                    user.setLocation("New York");

                    ResponseEntity<Users> response = restTemplate.postForEntity("/user/create", user, Users.class);

                    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                        successCount.incrementAndGet();
                        return true;
                    } else {
                        failureCount.incrementAndGet();
                        System.err.println("Request failed: " + response.getStatusCode());
                        return false;
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    e.printStackTrace();
                    return false;
                }
            });
        }

        System.out.println("Starting load test with " + totalRequests + " requests...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Future<Boolean>> futures = executorService.invokeAll(tasks);

        stopWatch.stop();
        System.out.println("Load test finished in " + stopWatch.getTotalTimeSeconds() + " seconds.");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests: " + failureCount.get());

        executorService.shutdown();

        assertThat(failureCount.get()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(totalRequests);
    }

    @Autowired
    private com.example.dateon.Repositories.IceBreakerRepository iceBreakerRepository;

    @Test
    public void testIceBreakerRepo() {
        System.out.println("Testing IceBreakerRepository...");
        try {
            List<com.example.dateon.Models.IceBreaker> all = iceBreakerRepository.findAll();
            System.out.println("IceBreakerRepository findAll size: " + all.size());
        } catch (Exception e) {
            System.err.println("IceBreakerRepository failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testIceBreakerEndpoint() {
        try {
            Users dummyUser = new Users();
            String mail = "icebreaker_test_" + System.currentTimeMillis() + "@test.com";
            dummyUser.setMail(mail);
            dummyUser.setName("Ice Breaker User");
            dummyUser.setPassword("pass");
            userServices.createNewUser(dummyUser);

            String token = jwtService.generateToken(mail);
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "/api/icebreakers/random?limit=3",
                    HttpMethod.GET,
                    entity,
                    String.class);

            System.out.println("IceBreaker Response Status: " + response.getStatusCode());
            System.out.println("IceBreaker Response Body: " + response.getBody());
            assertThat(response.getStatusCode().is2xxSuccessful())
                    .as("Status should be 2xx. Body: " + response.getBody()).isTrue();
        } catch (Exception e) {
            System.err.println("IceBreaker Endpoint failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testAIIntegration() {
        // 1. Create User A
        Users userA = new Users();
        String mailA = "userA_" + System.currentTimeMillis() + "@test.com";
        userA.setMail(mailA);
        userA.setName("User A");
        userA.setPassword("pass");
        Users createdUserA = userServices.createNewUser(userA);
        assertThat(createdUserA).isNotNull();

        // 2. Create User B
        Users userB = new Users();
        String mailB = "userB_" + System.currentTimeMillis() + "@test.com";
        userB.setMail(mailB);
        userB.setName("User B");
        userB.setPassword("pass");
        Users createdUserB = userServices.createNewUser(userB);
        assertThat(createdUserB).isNotNull();

        // 3. Add Questions for User A
        String tokenA = jwtService.generateToken(mailA);
        addQuestionsToUser(createdUserA.getId(), tokenA);

        // 4. Add Questions for User B
        String tokenB = jwtService.generateToken(mailB);
        addQuestionsToUser(createdUserB.getId(), tokenB);

        // Refresh users to get questions
        createdUserA = userServices.getUserById(createdUserA.getId());
        createdUserB = userServices.getUserById(createdUserB.getId());

        // 5. Call AI Service
        try {
            double score = aiService.getCompatibilityScore(createdUserA, List.of(createdUserB));
            System.out.println("Compatibility Score: " + score);
            assertThat(score).isGreaterThanOrEqualTo(0.0);
            assertThat(score).isLessThanOrEqualTo(1.0);
        } catch (Exception e) {
            System.out.println("AI Service call failed or returned 0.0: " + e.getMessage());
        }
    }

    private void addQuestionsToUser(int userId, String token) {
        String[] questions = {
                "Do you believe in second chances?",
                "Can you keep a secret?",
                "Do you discuss personal issues?",
                "What is your favorite time of day?",
                "How do you handle fear?",
                "What do you value most in a friend?",
                "How do you express affection?",
                "Are you logical or emotional?",
                "How do you handle conflict?",
                "Are you an introvert or extrovert?",
                "What makes you lose interest?",
                "What are you passionate about?",
                "What is your communication style?",
                "How do you de-stress?",
                "Do you believe in soulmates?"
        };

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        for (String qText : questions) {
            com.example.dateon.Models.Question q = new com.example.dateon.Models.Question();
            q.setQuestionText(qText);
            q.setAnswerText("A");

            HttpEntity<com.example.dateon.Models.Question> entity = new HttpEntity<>(q, headers);
            restTemplate.exchange("/users/" + userId + "/questions", HttpMethod.POST, entity, String.class);
        }
    }
}
