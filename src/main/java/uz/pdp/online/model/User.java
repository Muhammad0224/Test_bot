package uz.pdp.online.model;

import lombok.*;
import uz.pdp.online.model.history.ActiveHistory;
import uz.pdp.online.model.history.History;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    private String name;
    private String username;
    private Integer id;
    private ActiveHistory activeHistory;
    private List<History> histories = new ArrayList<>();
    private int point;
    private String chatId;

    public String getLastHistoryId() {
        return String.valueOf(histories.size() + 1);
    }

    public History getLastHistory() {
        return histories.get(histories.size() - 1);
    }

    public Double getRate() {
        double rate = 0;
        for (History history : histories) {
            rate += history.getRate();
        }

        return histories.size() != 0 ? rate/histories.size(): 0;
    }
}
