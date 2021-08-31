package uz.pdp.online.model.history;

import lombok.*;
import uz.pdp.online.model.subject.Question;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class ActiveHistory {

    private List<Question> questions = new ArrayList<>();
    private List<CheckQuestion> checkQuestions = new ArrayList<>();


    public Question getLastQuestion() {
        return checkQuestions.get(checkQuestions.size() - 1).getQuestion();
    }

    public int trueAnswer(){
        int counter = 0;
        for (CheckQuestion checkQuestion : checkQuestions) {
            if (checkQuestion.isState()){
                counter++;
            }
        }

        return counter;
    }
}
