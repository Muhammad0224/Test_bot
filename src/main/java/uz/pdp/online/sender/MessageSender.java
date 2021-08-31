package uz.pdp.online.sender;

import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.pdp.online.services.Service;

public class MessageSender implements Service {
    @Override
    public SendMessage send(Update update, String text) {
        SendMessage sendMessage = new SendMessage()
                .setParseMode(ParseMode.MARKDOWN);

        sendMessage.setText(text);
        return sendMessage;
    }
}
