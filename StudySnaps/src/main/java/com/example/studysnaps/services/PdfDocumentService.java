package com.example.studysnaps.services;



import com.example.studysnaps.Repositories.*;
import com.example.studysnaps.entities.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import jakarta.persistence.EntityNotFoundException;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

@Service
public class PdfDocumentService {

@Autowired
PDFDocumentRepository pdfDocumentRepository;
@Autowired
QuizRepository quizRepository;
@Autowired
TagService tagService;
@Autowired
    FlashCardRepository flashCardRepository;
@Autowired
    UserRepository userRepository ;

@Autowired
    FlashCardSetRepository flashCardSetRepository;
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


    public Map<String, Object> generateQuizzesAndAnswers(String pdfText, String textLanguage, String userEmail,List<String> tags) throws JsonProcessingException {

        String prompt = "Please read the following text and generate a list of 10 unique questions suitable for a quiz, each one designed to test comprehension of the material presented. Focus on key details and concepts introduced in the passages.\n\n"
                + "Language: " + textLanguage + "\n\n"
                + pdfText.substring(0, Math.min(pdfText.length(), 500))
                + "\n\nQuestions should be clear, concise, and directly related to the content of the text.";

        List<String> questions = generateQuestions(prompt);

        List<String> answers = generateAnswers(pdfText, textLanguage, questions);

        Map<String, Object> quizzesAndAnswers = new HashMap<>();

        List<String> formattedQuestions = formatQuestions(questions);
        quizzesAndAnswers.put("questions", formattedQuestions);

        List<String> formattedAnswers = formatAnswers(answers);
        quizzesAndAnswers.put("answers", formattedAnswers);

        Map<String, Integer> ids = saveQuizzesAndAnswersToDatabase(quizzesAndAnswers, pdfText, textLanguage, userEmail, tags);
        quizzesAndAnswers.putAll(ids);

        return quizzesAndAnswers;
    }

    private List<String> formatQuestions(List<String> questions) {
        List<String> formattedQuestions = new ArrayList<>();

        for (String question : questions) {
            String[] questionLines = question.split("\n(?=\\d+\\.)");
            for (String line : questionLines) {
                formattedQuestions.add(line.trim());
            }
        }

        return formattedQuestions;
    }

    private List<String> formatAnswers(List<String> answers) {
        List<String> formattedAnswers = new ArrayList<>();

        for (String answer : answers) {
            String[] splitAnswers = answer.trim().split("\\n");
            for (String splitAnswer : splitAnswers) {
                if (!splitAnswer.trim().isEmpty()) {
                    String formattedAnswer = splitAnswer.replaceAll("^\\d+\\.\\s+", "").trim();
                    formattedAnswers.add(formattedAnswer);
                }
            }
        }

        return formattedAnswers;
    }

    public Integer savePDFDocument(String pdfText, String textLanguage, String userEmail, List<String> tags) {
        PDFDocument pdfDocument = new PDFDocument();
        pdfDocument.setTitle("Title Placeholder");
        pdfDocument.setTags(tagService.getOrCreateTags(tags));
        pdfDocument.setFlashCardSet(Collections.emptyList());

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        pdfDocument.setUser(user);

        pdfDocument = pdfDocumentRepository.save(pdfDocument);

        return pdfDocument.getDocId();
    }

    public Map<String, Integer> saveQuizzesAndAnswersToDatabase(Map<String, Object> quizzesAndAnswers, String pdfText, String textLanguage, String userEmail, List<String> tags) {
        List<String> questions = (List<String>) quizzesAndAnswers.get("questions");
        List<String> answers = (List<String>) quizzesAndAnswers.get("answers");
        Map<String, Integer> ids = new HashMap<>();

        if (!questions.isEmpty() && !answers.isEmpty()) {
            Integer docId = savePDFDocument(pdfText, textLanguage, userEmail, tags);

            Quiz quiz = new Quiz();
            PDFDocument pdfDocument = pdfDocumentRepository.findById(docId)
                    .orElseThrow(() -> new EntityNotFoundException("PDF Document with id " + docId + " not found."));
            quiz.setPdfDoc(pdfDocument);
            quiz.setTitle("Generated Quiz");
            quiz.setQuestions(questions);
            quiz.setAnswers(answers);

            quiz = quizRepository.save(quiz);

            ids.put("docId", docId);
            ids.put("quizId", quiz.getQuizId());
        }

        return ids;
    }

  /*  public Map<String, Integer> saveQuizzesAndAnswersToDatabase(Map<String, Object> quizzesAndAnswers, String pdfText, String textLanguage, String userEmail, List<String> tags) {
        List<String> questions = (List<String>) quizzesAndAnswers.get("questions");
        List<String> answers = (List<String>) quizzesAndAnswers.get("answers");
        Map<String, Integer> ids = new HashMap<>();

        if (!questions.isEmpty() && !answers.isEmpty()) {
            PDFDocument pdfDocument = new PDFDocument();
            pdfDocument.setTitle("Title Placeholder");
            pdfDocument.setTags(tagService.getOrCreateTags(tags));
            pdfDocument.setFlashCardSet(Collections.emptyList());

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            pdfDocument.setUser(user);

            pdfDocument = pdfDocumentRepository.save(pdfDocument);

            Quiz quiz = new Quiz();
            quiz.setPdfDoc(pdfDocument);
            quiz.setTitle("Generated Quiz");
            quiz.setQuestions(questions);
            quiz.setAnswers(answers);

            quiz = quizRepository.save(quiz);

            ids.put("docId", pdfDocument.getDocId());
            ids.put("quizId", quiz.getQuizId());
        }

        return ids;
    }*/

    private List<String> generateQuestions(String prompt) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> map = new HashMap<>();
        map.put("contents", Collections.singletonMap("parts", Collections.singletonList(Collections.singletonMap("text", prompt))));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;

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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

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




    private String createSummarizationPrompt(String pdfText, String textLanguage) {
        return String.format(
                "Summarize the essential information from the following %s language text into a concise, easily understandable format, while retaining all critical details and concepts:\n\n%s",
                textLanguage,
                pdfText
        );
    }


    public ByteArrayOutputStream summarizePdfAndGeneratePdf(MultipartFile file, String textLanguage) throws IOException, DocumentException {
        String pdfText = extractTextFromPDF(file);
        String prompt = createSummarizationPrompt(pdfText, textLanguage);
        String summary = generateSingleAnswer(prompt);

        return generatePdfFromSummary(summary);
    }

    public ByteArrayOutputStream generatePdfFromSummary(String summary) throws DocumentException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, byteArrayOutputStream);

        document.open();


        Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD, BaseColor.DARK_GRAY);
        Paragraph title = new Paragraph("Summary", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add a separator line
        LineSeparator separator = new LineSeparator();
        separator.setLineColor(BaseColor.DARK_GRAY);
        document.add(new Chunk(separator));

        // Add content
        Font contentFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.BLACK);
        Paragraph content = new Paragraph(summary, contentFont);
        content.setSpacingBefore(20);
        content.setSpacingAfter(20);
        document.add(content);

        document.close();

        return byteArrayOutputStream;
    }


    public Map<String, Object> generateFlashCards(String pdfText, String textLanguage, String userEmail,Integer docId) throws JsonProcessingException {
        String prompt = "Extract key concepts and definitions from the following text for creating educational flashcards. " +
                "Each card should present a concept or term on one side and its definition or explanation on the other side. " +
                "Focus on the most important and relevant information that would aid in studying the material presented.\n\n" +
                "Language: " + textLanguage + "\n\n" +
                pdfText.substring(0, Math.min(pdfText.length(), 500)) + "\n\n" +
                "Flashcards:";

        List<String> definitionsBlocks = generateQuestions(prompt);
        List<Map<String, String>> flashcards = new ArrayList<>();

        for (String definitionBlock : definitionsBlocks) {
            String[] parts = definitionBlock.split("\n\n");
            for (int i = 0; i < parts.length; i += 2) {
                if (i + 1 < parts.length) {
                    Map<String, String> card = new HashMap<>();
                    String frontPart = parts[i].replace("Front: ", "").trim();
                    String backPart = parts[i+1].replace("Back: ", "").trim();
                    card.put("concept", frontPart);
                    card.put("definition", backPart);
                    flashcards.add(card);
                }
            }
        }

        FlashCardSet set = saveFlashCardSetToDatabase(flashcards, pdfText, textLanguage, userEmail,docId);

        Map<String, Object> response = new HashMap<>();
        response.put("flashcards", flashcards);
        return response;
    }


    private FlashCardSet saveFlashCardSetToDatabase(List<Map<String, String>> flashcards, String pdfText, String textLanguage, String userEmail, Integer docId) {

        PDFDocument pdfDocument = pdfDocumentRepository.findById(docId)
                .orElseThrow(() -> new EntityNotFoundException("PDF Document with id " + docId + " not found."));

        FlashCardSet set = new FlashCardSet();
        set.setPdfDoc(pdfDocument);
        set.setTitle("Generated Flashcards for " + userEmail);
        set = flashCardSetRepository.save(set);

        for (Map<String, String> card : flashcards) {
            FlashCard f = new FlashCard();
            f.setConcept(card.get("concept"));
            f.setDefinitionText(card.get("definition"));
            f.setFlashCardSet(set);
            flashCardRepository.save(f);
        }

        return set;
    }






}