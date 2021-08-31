package uz.pdp.online.model.subject;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Answer {
    private String body;
    private Boolean answer;
    private Character letter;
}
