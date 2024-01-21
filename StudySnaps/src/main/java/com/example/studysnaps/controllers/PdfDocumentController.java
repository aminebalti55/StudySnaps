package com.example.studysnaps.controllers;


import com.example.studysnaps.Repositories.FlashCardRepository;
import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.Repositories.RoomRepository;
import com.example.studysnaps.dto.RoomDTO;
import com.example.studysnaps.entities.FlashCard;
import com.example.studysnaps.entities.Room;
import com.example.studysnaps.services.PdfDocumentService;

import com.example.studysnaps.services.QuizService;
import com.example.studysnaps.services.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipal;
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
    RoomService roomService;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    PDFDocumentRepository pdfDocumentRepository;
@Autowired
    FlashCardRepository flashCardRepository;


    /*@PostMapping(value = "/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadPDF(@RequestParam("file") MultipartFile file,
                                                         Authentication authentication,
                                                         @RequestParam(value = "language", defaultValue = "English") String textLanguage,
                                                         @RequestParam(value = "tags", defaultValue = "") List<String> tags) {
        try {
            // Extract text from the PDF
            String pdfText = pdfDocumentService.extractTextFromPDF(file);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();

            Map<String, Object> quizzesAndAnswers = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage, userEmail, tags);

            Integer pdfDocumentId = (Integer) quizzesAndAnswers.get("docId");
            Integer quizId = (Integer) quizzesAndAnswers.get("quizId");

            if (pdfDocumentId != null && quizId != null) {
                quizService.initializeUserProgress(userEmail, quizId, pdfDocumentId);
            } else {
                throw new IllegalStateException("Quiz or Document ID not found after save operation.");
            }

            return ResponseEntity.ok(quizzesAndAnswers);

        } catch (IOException e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the PDF file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (IllegalStateException e) {

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
*/

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



    @PostMapping(value = "/process-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> processPDF(@RequestParam("file") MultipartFile file,
                                        Authentication authentication,
                                        @RequestParam(value = "language", defaultValue = "English") String textLanguage,
                                        @RequestParam(value = "tags", defaultValue = "") List<String> tags,
                                        @RequestParam(value = "action", required = false) String action) {
        try {

            String pdfText = pdfDocumentService.extractTextFromPDF(file);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userEmail = userDetails.getUsername();

            Integer pdfDocumentId = pdfDocumentService.savePDFDocument(pdfText, textLanguage, userEmail, tags);

            Map<String, Object> result = new HashMap<>();

            if ("summary".equals(action) || "both".equals(action)) {
                ByteArrayOutputStream summaryPdf = pdfDocumentService.summarizePdfAndGeneratePdf(file, textLanguage);
                byte[] pdfBytes = summaryPdf.toByteArray();
                result.put("summary", pdfBytes);

                if ("summary".equals(action)) {

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentDisposition(ContentDisposition.builder("attachment").filename("summary.pdf").build());
                    headers.setContentType(MediaType.APPLICATION_PDF); // Set the correct content type for PDF
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                }
            }


            if ("quiz".equalsIgnoreCase(action) || "both".equals(action)) {
                Map<String, Object> quizResult = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage, userEmail, tags, pdfDocumentId);
                Integer quizId = (Integer) quizResult.get("quizId");

                if (pdfDocumentId != null && quizId != null) {
                    quizService.initializeUserProgress(userEmail, quizId, pdfDocumentId);
                }

                result.putAll(quizResult);
            } else if ("flashcards".equalsIgnoreCase(action)) {
                Map<String, Object> flashcardResult = pdfDocumentService.generateFlashCards(pdfText, textLanguage, userEmail, pdfDocumentId);
                result.putAll(flashcardResult);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error processing the PDF file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



/*    @PostMapping("/create")
    public ResponseEntity<RoomDTO> createRoom(@RequestParam("opponentUsername") String opponentUsername) {
        try {
            RoomDTO roomDTO = roomService.createRoom(opponentUsername);
            return ResponseEntity.ok(roomDTO);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
*/


    @PostMapping(value = "/rooms/{roomId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPDFToRoom(
            @PathVariable Integer roomId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            @RequestParam(value = "language", defaultValue = "English") String textLanguage,
            @RequestParam(value = "tags", defaultValue = "") List<String> tags) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userEmail = userDetails.getUsername();

        Integer userId = roomService.getUserIDByEmail(userEmail);

        try {
            Room room = roomService.findRoomById(roomId);
            if (!room.getOwnerId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Error: You are not authorized to upload a PDF to this room.");
            }


            String pdfText = pdfDocumentService.extractTextFromPDF(file);
            Integer pdfDocumentId = pdfDocumentService.savePDFDocument(pdfText, textLanguage, userEmail, tags);

            Map<String, Object> quizzesAndAnswers = pdfDocumentService.generateQuizzesAndAnswers(pdfText, textLanguage, userEmail, tags, pdfDocumentId);

            Integer quizId = (Integer) quizzesAndAnswers.get("quizId");

            if (pdfDocumentId != null && quizId != null) {
                roomService.addQuizToRoom(roomId, quizId);
            } else {
                throw new IllegalStateException("Quiz or Document ID not found after save operation.");
            }

            return ResponseEntity.ok(quizzesAndAnswers);

        } catch (IOException e) {
            // Log error details here for debug purposes
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the PDF file: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Log error details here for debug purposes
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        }
    }


    @PostMapping("/quickmatch")
    public ResponseEntity<?> requestQuickMatch(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        roomService.addToQuickMatchQueue(username);

        return ResponseEntity.ok("Quick match requested");
    }



}




