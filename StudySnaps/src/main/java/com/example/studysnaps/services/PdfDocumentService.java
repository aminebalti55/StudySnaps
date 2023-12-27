package com.example.studysnaps.services;



import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.entities.PDFDocument;
import com.example.studysnaps.entities.Quiz;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class PdfDocumentService {

@Autowired
PDFDocumentRepository pdfDocumentRepository;
@Autowired
QuizRepository quizRepository;

    public String extractTextFromPDF(MultipartFile file) {
        try (InputStream input = file.getInputStream();
             PDDocument document = PDDocument.load(input)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @Autowired
    RestTemplate restTemplate;

    @Value("${google.api.key}")

    private String apiKey;


    public Map<String, Object> generateQuizzesAndAnswers(String pdfText, String textLanguage) throws JsonProcessingException {
        // Create a prompt based on the PDF content
        String prompt = "Please read the following text and generate a list of 10 unique questions suitable for a quiz, each one designed to test comprehension of the material presented. Focus on key details and concepts introduced in the passages.\n\n"
                + "Language: " + textLanguage + "\n\n"
                + pdfText.substring(0, Math.min(pdfText.length(), 500)) // Include a significant snippet of the text for context
                + "\n\nQuestions should be clear, concise, and directly related to the content of the text.";

        // Generate questions
        List<String> questions = generateQuestions(prompt);

        // Generate answers for the questions
        List<String> answers = generateAnswers(pdfText, textLanguage, questions);

        // Create a map to store questions and answers
        Map<String, Object> quizzesAndAnswers = new HashMap<>();

        List<String> formattedQuestions = formatQuestions(questions);
        quizzesAndAnswers.put("questions", formattedQuestions);

        List<String> formattedAnswers = formatAnswers(answers);
        quizzesAndAnswers.put("answers", formattedAnswers);

        saveQuizzesAndAnswersToDatabase(quizzesAndAnswers, pdfText, textLanguage);

        return quizzesAndAnswers;
    }

    private List<String> formatQuestions(List<String> questions) {
        List<String> formattedQuestions = new ArrayList<>();

        for (String question : questions) {
            String[] questionLines = question.split("\n\n");
            for (String line : questionLines) {
                formattedQuestions.add(line.trim());
            }
        }

        return formattedQuestions;
    }

    private List<String> formatAnswers(List<String> answers) {
        List<String> formattedAnswers = new ArrayList<>();

        for (String answer : answers) {
            String[] answerLines = answer.split("\n\n");
            for (String line : answerLines) {
                formattedAnswers.add(line.trim());
            }
        }

        return formattedAnswers;
    }

    private void saveQuizzesAndAnswersToDatabase(Map<String, Object> quizzesAndAnswers, String pdfText, String textLanguage) {
        List<String> questions = (List<String>) quizzesAndAnswers.get("questions");
        List<String> answers = (List<String>) quizzesAndAnswers.get("answers");

        // Assuming "questions" and "answers" are both present in the response
        if (!questions.isEmpty() && !answers.isEmpty()) {
            // Create a new PDFDocument entity
            PDFDocument pdfDocument = new PDFDocument();
            pdfDocument.setTitle("Title Placeholder"); // Provide an appropriate title
            pdfDocument.setTags(Collections.emptyList()); // Add tags if available
            pdfDocument.setSummary(Collections.emptyList()); // Add summaries if available
            pdfDocument.setFlashCardSet(Collections.emptyList()); // Add flash card sets if available

            // Save the PDFDocument entity to get its ID
            pdfDocument = pdfDocumentRepository.save(pdfDocument);

            // Create a new Quiz entity
            Quiz quiz = new Quiz();
            quiz.setPdfDoc(pdfDocument);
            quiz.setTitle("Generated Quiz"); // Provide an appropriate title
            quiz.setQuestions(questions);
            quiz.setAnswers(answers);

            // Save the Quiz entity
            quizRepository.save(quiz);


        }
    }


    private List<String> generateQuestions(String prompt) throws JsonProcessingException {
        // Setup the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // The body of the request
        Map<String, Object> map = new HashMap<>();
        map.put("contents", Collections.singletonMap("parts", Collections.singletonList(Collections.singletonMap("text", prompt))));

        // Wrap your body and headers into an entity
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        // Include the API key in the query parameters
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        // Send the request using RestTemplate
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        String generatedQuestions = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(generatedQuestions, new TypeReference<Map<String, Object>>() {});

        List<String> questions = parseCandidatesResponse(responseMap);

        return questions;
    }

    private List<String> generateAnswers(String pdfText, String textLanguage, List<String> questions) throws JsonProcessingException {

        List<String> answers = new ArrayList<>();

        for (String question : questions) {
            String promptForAnswer = "Based on the following text, what is the answer to this question?\n\n"
                    + "Language: " + textLanguage + "\n\n"
                    + pdfText + "\n\n"
                    + "Question: " + question.trim() + "\n\n"
                    + "Answer:";

            String answer = generateSingleAnswer(promptForAnswer);
            answers.add(answer);
        }

        return answers;
    }


    private String generateSingleAnswer(String promptForAnswer) throws JsonProcessingException {
        // Setup the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // The body of the request
        Map<String, Object> map = new HashMap<>();
        map.put("contents", Collections.singletonMap("parts", Collections.singletonList(Collections.singletonMap("text", promptForAnswer))));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        String generatedContent = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> responseMap = objectMapper.readValue(generatedContent, new TypeReference<Map<String, Object>>() {});

        return extractAnswerFromResponse(responseMap);
    }

    private String extractAnswerFromResponse(Map<String, Object> responseMap) {

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return "";
        }


        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return "";
        }

        Map<String, Object> textPart = parts.get(0);
        return (String) textPart.get("text");
    }



  private List<String> parseCandidatesResponse(Map<String, Object> responseMap) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        List<String> questions = new ArrayList<>();

        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts != null && !parts.isEmpty()) {
                for (Map<String, Object> part : parts) {
                    String text = (String) part.get("text");
                    if (text != null && !text.isEmpty()) {
                        questions.add(text);
                    }
                }
            }
        }

        return questions;
    }
























}