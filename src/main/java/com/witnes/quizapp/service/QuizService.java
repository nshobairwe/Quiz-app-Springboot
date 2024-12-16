package com.witnes.quizapp.service;

import com.witnes.quizapp.dao.QuestionDao;
import com.witnes.quizapp.dao.QuizDao;
import com.witnes.quizapp.model.Question;
import com.witnes.quizapp.model.QuestionWrapper;
import com.witnes.quizapp.model.Quiz;
import com.witnes.quizapp.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    QuizDao quizDao;

    @Autowired
    QuestionDao questionDao;

    public ResponseEntity<String> createQuiz(String category, int numQ, String title) {
        List<Question> questions = questionDao.findRandomQustionsByCategory(category, numQ);
        if(questions.isEmpty()) {
            return new ResponseEntity<>("No questions found for the given category.", HttpStatus.BAD_REQUEST);
        }

        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setQuestions(questions);
        quizDao.save(quiz);

        return new ResponseEntity<>("Success", HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(Integer id) {
        Optional<Quiz> optionalQuiz = quizDao.findById(id);
        if(optionalQuiz.isEmpty()) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.NOT_FOUND);
        }

        List<Question> questionsFromDB = optionalQuiz.get().getQuestions();
        List<QuestionWrapper> questionsForUser = new ArrayList<>();
        for(Question q : questionsFromDB) {
            questionsForUser.add(new QuestionWrapper(q.getId(), q.getQuestionTitle(), q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()));
        }

        return new ResponseEntity<>(questionsForUser, HttpStatus.OK);
    }

    public ResponseEntity<Integer> calculateResult(Integer id, List<Response> response) {
        Optional<Quiz> optionalQuiz = quizDao.findById(id);
        if(optionalQuiz.isEmpty()) {
            return new ResponseEntity<>(0, HttpStatus.NOT_FOUND);
        }

        Quiz quiz = optionalQuiz.get();
        List<Question> questions = quiz.getQuestions();
        int right = 0;

        for(int i = 0; i < response.size() && i < questions.size(); i++) {
            if(response.get(i).getResponse().equals(questions.get(i).getRightAnswer())) {
                right++;
            }
        }

        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}
