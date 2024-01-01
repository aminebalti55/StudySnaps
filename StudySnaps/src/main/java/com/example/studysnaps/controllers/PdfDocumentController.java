package com.example.studysnaps.controllers;


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



    @PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPDF(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication,  @RequestParam(value = "language", defaultValue = "English") String textLanguage, @RequestParam(value = "tags", defaultValue = "") List<String> tags){
        try {
            String pdfText = pdfDocumentService.extractTextFromPDF(file);


            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();

            Map<String, Object> quizzesAndAnswers = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage,userEmail,tags);


            return ResponseEntity.ok(quizzesAndAnswers);
        } catch (IOException e) {
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the PDF file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/check-answer/{quizId}/{questionIndex}")
    public ResponseEntity<String> checkAnswer(
            @PathVariable Integer quizId,
            @PathVariable Integer questionIndex,
            @RequestBody String userResponse) {

        String result = quizService.checkUserResponse(quizId, questionIndex, userResponse);

        return ResponseEntity.ok(result);
    }



}




