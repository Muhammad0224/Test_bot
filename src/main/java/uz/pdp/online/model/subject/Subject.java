package uz.pdp.online.model.subject;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Subject {
    private String subjectName;
    private List<Question> questions = new ArrayList<>();
}
