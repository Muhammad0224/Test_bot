package uz.pdp.online.sender;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class ReplyKeyboardSender {
    public ReplyKeyboardMarkup createKeyboard(String[] strings) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> rowList = new ArrayList<>();
        int k = 0;
        int cycle = (strings.length % 2 == 0) ? strings.length / 2 : strings.length / 2 + 1;
        for (int i = 0; i < cycle; i++) {
            KeyboardRow row = new KeyboardRow();

            for (int j = 0; j < 2; j++) {
                row.add(new KeyboardButton(strings[k]));
                if (k == strings.length - 1) {
                    break;
                }
                k++;
            }

            rowList.add(row);
        }
        replyKeyboardMarkup.setKeyboard(rowList);
        return replyKeyboardMarkup;
    }
}
