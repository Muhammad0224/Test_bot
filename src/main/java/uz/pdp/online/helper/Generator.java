package uz.pdp.online.helper;

import uz.pdp.online.model.subject.Answer;
import uz.pdp.online.model.subject.Question;
import uz.pdp.online.model.subject.Subject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Generator {
    public List<Question> generatorQuestion(Subject subject){
        HashSet<Question> questions1 = new HashSet<>();
        List<Question> questions = new ArrayList<>();
        int circle = Math.min(subject.getQuestions().size(), 10);
        for (int i = 0; i < circle; i++) {
            int index = (int) (Math.random() * subject.getQuestions().size());
            if (!questions1.add(subject.getQuestions().get(index))){
                i--;
            }
        }
        for (Question question : questions1) {
            questions.add(generationAnswer(question));
        }
        return questions;
    }

    public Question generationAnswer(Question question) {
        Character[] characters = {'A','B','C','D'};
        List<Answer> answers = question.getAnswers();
        HashSet<Character> characterHashSet = new HashSet<>();
        for (int i = 0; i < characters.length; i++) {
            int index = (int) (Math.random() * characters.length);
            if (characterHashSet.add(characters[index])){
                answers.get(i).setLetter(characters[index]);
            }else i--;
        }
        question.setAnswers(answers);
        return question;
    }
}
