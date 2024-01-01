package com.example.studysnaps.services;


import com.example.studysnaps.Repositories.QuizRepository;
import com.example.studysnaps.entities.Quiz;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.SimpleTokenizer;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    public String checkUserResponse(int quizId, int questionIndex, String userResponse) {

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
}
