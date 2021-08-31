package uz.pdp.online.sender;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.pdp.online.model.subject.Answer;
import uz.pdp.online.model.subject.Question;

import java.util.ArrayList;
import java.util.List;

public class InlineKeyboardSender {
    public InlineKeyboardMarkup createKeyboard(Question question) {

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        for (Answer answer : question.getAnswers()) {
            if (answer.getLetter().equals('A')) {
                row1.add(new InlineKeyboardButton(answer.getLetter() + ") " + answer.getBody()).setCallbackData("A"));
            } else if (answer.getLetter().equals('B')) {
                row2.add(new InlineKeyboardButton(answer.getLetter() + ") " + answer.getBody()).setCallbackData("B"));
            }else if (answer.getLetter().equals('C')) {
                row3.add(new InlineKeyboardButton(answer.getLetter() + ") " + answer.getBody()).setCallbackData("C"));
            }else if (answer.getLetter().equals('D')) {
                row4.add(new InlineKeyboardButton(answer.getLetter() + ") " + answer.getBody()).setCallbackData("D"));
            }
        }


        rowList.add(row1);
        rowList.add(row2);
        rowList.add(row3);
        rowList.add(row4);

        keyboardMarkup.setKeyboard(rowList);
        return keyboardMarkup;
}

    public InlineKeyboardMarkup createKeyboard(String[] subjects) {

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();

        int k = 0;
        int cycle = (subjects.length % 2 == 0) ? subjects.length / 2 : subjects.length / 2 + 1;

        for (int i = 0; i < cycle; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            for (int j = 0; j < 2; j++) {
                row.add(new InlineKeyboardButton(subjects[k]).setCallbackData(subjects[k]));
                if (k == subjects.length - 1) {
                    break;
                }
                k++;
            }
            rowList.add(row);
        }

        keyboardMarkup.setKeyboard(rowList);
        return keyboardMarkup;
    }
}
