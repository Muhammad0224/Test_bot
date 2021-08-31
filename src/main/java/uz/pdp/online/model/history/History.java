package uz.pdp.online.model.history;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class History {
    private String id;
    private String userName;
    private Integer userId;
    private String subjectName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int questionSize;
    private int trueAnswer;

    public double getRate(){
        return (questionSize != 0)? ((double) trueAnswer * 100 / questionSize):0;
    }
}

