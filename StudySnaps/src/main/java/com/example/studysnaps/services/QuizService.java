package com.example.studysnaps.services;


import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.Repositories.UserProgressTrackingRepository;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.entities.Quiz;
import com.example.studysnaps.entities.User;
import com.example.studysnaps.entities.UserProgressTracking;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserProgressTrackingRepository userProgressTrackingRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PDFDocumentRepository pdfDocumentRepository;


    public String checkUserResponse(int quizId, int questionIndex, String userResponse, String userEmail) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);

        if (quiz == null) {
            return "Quiz not found.";
        }

        String correctAnswer = quiz.getAnswers().get(questionIndex);
        String question = quiz.getQuestions().get(questionIndex);
        userResponse = userResponse.toLowerCase();
        correctAnswer = correctAnswer.toLowerCase();

        double similarity = calculateJaroWinklerSimilarity(userResponse, correctAnswer);

        if (similarity >= 0.3) {
            // Update user progress if the answer is correct
            updateProgressOnQuestionAnswered(userEmail, 0.1);

            return "Success! Your answer is correct.";
        } else {
            String hint = generateHintBasedOnAnswer(correctAnswer);
            return "Incorrect answer. Here's a hint: " + hint;
        }
    }

    private double calculateJaroWinklerSimilarity(String userResponse, String correctAnswer) {
        JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
        return similarity.apply(userResponse, correctAnswer);
    }


    /*private boolean isAnswerCloseEnough(String userResponse, String correctAnswer) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

        // Tokenize the user response and the correct answer
        String[] userTokens = tokenizer.tokenize(userResponse.toLowerCase());
        String[] correctTokens = tokenizer.tokenize(correctAnswer.toLowerCase());

        PorterStemmer stemmer = new PorterStemmer();

        // Stem the tokens and add to sets
        Set<String> userSet = new HashSet<>();
        for (String token : userTokens) {
            userSet.add(stemmer.stem(token));
        }

        Set<String> correctSet = new HashSet<>();
        for (String token : correctTokens) {
            correctSet.add(stemmer.stem(token));
        }

        double similarity = calculateJaccardIndex(userSet, correctSet);

        final double THRESHOLD = 0.3;
        return similarity >= THRESHOLD;
    }

    private double calculateJaccardIndex(Set<String> userSet, Set<String> correctSet) {
        Set<String> intersection = new HashSet<>(userSet);
        intersection.retainAll(correctSet);

        Set<String> union = new HashSet<>(userSet);
        union.addAll(correctSet);

        return intersection.size() / (double) union.size(); // Jaccard index formula
    }*/

    private String generateHintBasedOnAnswer(String correctAnswer) {

        int hintLength = Math.min(correctAnswer.length() / 2, 10);
        return correctAnswer.substring(0, hintLength) + "...";
    }


    public String startQuiz(int quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);

        if (quiz == null) {
            return "Quiz not found.";
        }

        quiz.setQuizStartTime(LocalDateTime.now());
        quiz.setQuizDuration(Duration.ofMinutes(3));


        quizRepository.save(quiz);

        return "Quiz started!";
    }

    public long getRemainingTime(int quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);

        if (quiz == null || quiz.getQuizStartTime() == null || quiz.getQuizDuration() == null) {
            return 0;
        }

        LocalDateTime endTime = quiz.getQuizStartTime().plus(quiz.getQuizDuration());
        long remainingSeconds = Duration.between(LocalDateTime.now(), endTime).getSeconds();

        return Math.max(0, remainingSeconds);
    }

    public void initializeUserProgress(String userEmail, Integer quizId, Integer pdfDocumentId) {
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            UserProgressTracking userProgress = userProgressTrackingRepository.findByUserAndPdfDocumentIsNull(user);

            if (userProgress == null) {
                userProgress = new UserProgressTracking();
                userProgress.setUser(user);
                userProgress.setProgressPercentage(0.0);
                userProgress.setLastAccessed(LocalDateTime.now());

                userProgress.setQuiz(quizRepository.findById(quizId).orElse(null));
                userProgress.setPdfDocument(pdfDocumentRepository.findById(pdfDocumentId).orElse(null));

                userProgressTrackingRepository.save(userProgress);
            }
        } else {
            System.err.println("User not found with email: " + userEmail);
        }
    }


    private void updateProgressOnQuestionAnswered(String userEmail, double progressIncrement) {
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            UserProgressTracking userProgress = userProgressTrackingRepository.findByUserAndPdfDocumentIsNull(user);

            if (userProgress != null) {
                double newProgress = Math.min(userProgress.getProgressPercentage() + progressIncrement, 100.0);
                userProgress.setProgressPercentage(newProgress);
                userProgress.setLastAccessed(LocalDateTime.now());
                userProgressTrackingRepository.save(userProgress);
            } else {
                System.err.println("User progress not found for email: " + userEmail);
            }
        } else {
            System.err.println("User not found with email: " + userEmail);
        }
    }

}