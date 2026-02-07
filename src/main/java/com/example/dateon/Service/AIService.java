package com.example.dateon.Service;

import com.example.dateon.Dto.MatchRequestDTO;
import com.example.dateon.Dto.MatchResponseDTO;
import com.example.dateon.Dto.QuizAnswersDTO;
import com.example.dateon.Models.Users;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class AIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_service_URL = "http://127.0.0.1:8000/predict_compatibility";

    public double getCompatibilityScore(Users targetUser, List<Users> candidates) {
        if (candidates == null || candidates.isEmpty())
            return 0.0;

        try {
            MatchRequestDTO request = new MatchRequestDTO();
            request.setTarget_user(mapUserToQuizAnswers(targetUser));

            List<QuizAnswersDTO> candidateDTOs = candidates.stream()
                    .map(this::mapUserToQuizAnswers)
                    .toList();
            request.setCandidates(candidateDTOs);

            MatchResponseDTO response = restTemplate.postForObject(AI_service_URL, request, MatchResponseDTO.class);

            if (response != null && response.getResults() != null) {
                // Find max score in the batch results
                return response.getResults().stream()
                        .mapToDouble(r -> r.getCompatibility_score())
                        .max()
                        .orElse(0.0) * 100; // Convert 0-1 to 0-100 percentage
            }
        } catch (Exception e) {
            System.err.println("Error calling AI Service: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0; // Default fallback
    }

    private QuizAnswersDTO mapUserToQuizAnswers(Users user) {
        QuizAnswersDTO dto = new QuizAnswersDTO();
        List<com.example.dateon.Models.Question> questions = user.getQuestions();

        if (questions == null || questions.isEmpty()) {
            System.err.println("User " + user.getId() + " has no questions answering stored.");
            return dto; // Return empty DTO to avoid null pointer in loop
        }

        for (com.example.dateon.Models.Question q : questions) {
            String text = q.getQuestionText().toLowerCase();
            String answer = q.getAnswerText();

            if (text.contains("second chances"))
                dto.setSecond_chances(answer);
            else if (text.contains("secret"))
                dto.setSecret_trust(answer);
            else if (text.contains("personal issues"))
                dto.setDiscuss_issues(answer);
            else if (text.contains("favorite time"))
                dto.setFav_time(answer);
            else if (text.contains("handle fear"))
                dto.setHandle_fear(answer);
            else if (text.contains("value most"))
                dto.setValue_friend(answer);
            else if (text.contains("affection"))
                dto.setExpress_affection(answer);
            else if (text.contains("logical"))
                dto.setLogical_emotional(answer);
            else if (text.contains("conflict"))
                dto.setHandle_conflict(answer);
            else if (text.contains("introvert"))
                dto.setIntrovert_extrovert(answer);
            else if (text.contains("lose interest"))
                dto.setLose_interest(answer);
            else if (text.contains("passionate"))
                dto.setPassionate_about(answer);
            // Handling the renamed/replaced questions
            else if (text.contains("movie genre") || text.contains("communication style"))
                dto.setCommunication_style(answer);
            else if (text.contains("cooking") || text.contains("de-stress"))
                dto.setDe_stress(answer);
            else if (text.contains("soulmates"))
                dto.setSoulmates(answer);
        }

        return dto;
    }
}
