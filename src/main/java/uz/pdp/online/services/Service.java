package uz.pdp.online.services;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Service {
    SendMessage send(Update update, String text);
}
