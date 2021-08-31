package uz.pdp.online.model.history;

import lombok.*;
import uz.pdp.online.model.subject.Question;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class CheckQuestion {
    private Question question;
    private String userAnswer;
    private boolean state;
}
