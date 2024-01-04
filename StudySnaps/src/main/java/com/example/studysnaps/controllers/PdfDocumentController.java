package com.example.studysnaps.controllers;


import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.services.PdfDocumentService;

import com.example.studysnaps.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Pdf")
public class PdfDocumentController {

    @Autowired
    PdfDocumentService pdfDocumentService;
    @Autowired
    QuizRepository quizRepository;

    @Autowired
    QuizService quizService;

    @Autowired
    PDFDocumentRepository pdfDocumentRepository;



    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPDF(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication,
                                                         @RequestParam(value = "language", defaultValue = "English") String textLanguage,
                                                         @RequestParam(value = "tags", defaultValue = "") List<String> tags) {
        try {
            // Extract text from the PDF
            String pdfText = pdfDocumentService.extractTextFromPDF(file);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();

            // Generate quizzes and answers, which will also save them to the database and return a map with IDs
            Map<String, Object> quizzesAndAnswers = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage, userEmail, tags);

            // Retrieve the saved document ID and quiz ID from the map
            Integer pdfDocumentId = (Integer) quizzesAndAnswers.get("docId");
            Integer quizId = (Integer) quizzesAndAnswers.get("quizId");

            // Initialize the user's progress for the uploaded PDF and generated quiz
            if (pdfDocumentId != null && quizId != null) {
                quizService.initializeUserProgress(userEmail, quizId, pdfDocumentId);
            } else {
                throw new IllegalStateException("Quiz or Document ID not found after save operation.");
            }

            // Respond with the created quizzes and answers
            return ResponseEntity.ok(quizzesAndAnswers);

        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the PDF file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (IllegalStateException e) {
            // Handle IllegalStateException
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    @PostMapping("/check-answer/{quizId}/{questionIndex}")
    public ResponseEntity<String> checkAnswer(
            @PathVariable Integer quizId,
            @PathVariable Integer questionIndex,
            @RequestBody String userResponse,
            Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        String result = quizService.checkUserResponse(quizId, questionIndex, userResponse, userEmail);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/start/{quizId}")
    public String startQuiz(@PathVariable Integer quizId) {
        return quizService.startQuiz(quizId);
    }

    @GetMapping("/remainingTime/{quizId}")
    public long getRemainingTime(@PathVariable Integer quizId) {
        return quizService.getRemainingTime(quizId);
    }


}




