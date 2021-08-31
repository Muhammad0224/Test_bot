package uz.pdp.online.model.subject;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Question {
    private String body;
    private List<Answer> answers = new ArrayList<>();

    public Answer getTrueAnswer(){
        for (Answer answer : answers) {
            if (answer.getAnswer()){
                return answer;
            }
        }
        return null;
    }

    public String getLetterAnswer(char letter){
        for (Answer answer : answers) {
            if (answer.getLetter() == letter){
                return answer.getBody();
            }
        }
        return null;
    }
}
