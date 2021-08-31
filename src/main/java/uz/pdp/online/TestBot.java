package uz.pdp.online;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.pdp.online.helper.Generator;
import uz.pdp.online.helper.GsonHistoryHelper;
import uz.pdp.online.helper.GsonSubjectHelper;
import uz.pdp.online.helper.GsonUserHelper;
import uz.pdp.online.model.*;
import uz.pdp.online.model.history.ActiveHistory;
import uz.pdp.online.model.history.CheckQuestion;
import uz.pdp.online.model.history.History;
import uz.pdp.online.model.response.Response;
import uz.pdp.online.model.subject.Answer;
import uz.pdp.online.model.subject.Question;
import uz.pdp.online.model.subject.Subject;
import uz.pdp.online.sender.InlineKeyboardSender;
import uz.pdp.online.sender.ReplyKeyboardSender;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;

public class TestBot extends TelegramLongPollingBot {
    private final String API_TOKEN = "1896664287:AAEHp8iMOAmsOHwUHLJQL0_KfvU_Jk7ObiM";
    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    File subjectFile = new File("src/main/resources/base/subjects.txt");
    File userFile = new File("src/main/resources/base/users.txt");
    File historyFile = new File("src/main/resources/base/histories.txt");
    List<User> users = new ArrayList<>();
    List<Subject> subjects = new ArrayList<>();
    List<History> histories = new ArrayList<>();
    User admin = getUsersList().get(0);
    boolean createSubject = false;
    String subjectName = null;
    boolean createQuestion = false;
    String question = null;

    {
        if (!subjectFile.exists()) {
            try {
                subjectFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!userFile.exists()) {
            try {
                userFile.createNewFile();
                users.add(User.builder().name("Murtazayev").username(null).id(254632678).build());
                writeUser(users);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!historyFile.exists()) {
            try {
                historyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReplyKeyboardSender keyboardSender = new ReplyKeyboardSender();
                InlineKeyboardSender inlineKeyboardSender = new InlineKeyboardSender();

                if (update.hasCallbackQuery()) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
                    String callBack = update.getCallbackQuery().getData();
                    subjects = getSubjectsList();
                    users = getUsersList();

                    for (User user : users) {
                        if (user.getId().equals(update.getCallbackQuery().getFrom().getId())) {
                            boolean hasSubject = false;
                            Subject subject1 = null;
                            for (Subject subject : subjects) {
                                if (callBack.equals(subject.getSubjectName())) {
                                    user.getHistories().add(History.builder().userName(user.getName()).userId(user.getId())
                                            .subjectName(subject.getSubjectName())
                                            .id(user.getLastHistoryId())
                                            .startTime(LocalDateTime.now()).build());
                                    hasSubject = true;
                                    subject1 = subject;
                                    break;
                                }
                            }
                            if (hasSubject) {
                                sendMessage.setText("Eslatma!!!\nAgar javob belgilaganingizda bot ishlamay qolsa, bir oz kuting");
                                ReplyKeyboardRemove keyboardRemove = new ReplyKeyboardRemove();
                                sendMessage.setReplyMarkup(keyboardRemove);
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                user.setPoint(user.getPoint() - 1);
                                user.setActiveHistory(ActiveHistory.builder().questions(new Generator().generatorQuestion(subject1)).build());
                                Question question = user.getActiveHistory().getQuestions().get(user.getActiveHistory().getQuestions().size() -
                                        (user.getActiveHistory().getQuestions().size() - ((user.getActiveHistory().getCheckQuestions() == null) ? 0 : user.getActiveHistory().getCheckQuestions().size())));
                                sendMessage.setText(question.getBody());
                                sendMessage.setReplyMarkup(inlineKeyboardSender.createKeyboard(question));
                                if (user.getActiveHistory().getCheckQuestions() == null) {
                                    user.getActiveHistory().setCheckQuestions(Arrays.asList(CheckQuestion.builder().question(question).state(false).build()));
                                } else
                                    user.getActiveHistory().getCheckQuestions().add(CheckQuestion.builder().question(question).state(false).build());
                            } else if (user.getActiveHistory().getQuestions().size() == user.getActiveHistory().getCheckQuestions().size()) {
                                char variant = callBack.charAt(0);
                                if (variant == user.getActiveHistory().getLastQuestion().getTrueAnswer().getLetter()) {
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setState(true);
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setUserAnswer(
                                            user.getActiveHistory().getLastQuestion().getLetterAnswer(variant)
                                    );
                                } else {
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setUserAnswer(
                                            user.getActiveHistory().getLastQuestion().getLetterAnswer(variant)
                                    );
                                }

                                sendMessage.setText("Testni yakunladingi! Sizda " + user.getPoint() + " ball qoldi");
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                String result = "";

                                List<CheckQuestion> checkQuestions = user.getActiveHistory().getCheckQuestions();
                                for (int i = 0; i < checkQuestions.size(); i++) {
                                    result += (i + 1) + ") Your answer: " + checkQuestions.get(i).getUserAnswer() + "\nTrue answer: " +
                                            checkQuestions.get(i).getQuestion().getTrueAnswer().getBody() + "\nState: " +
                                            (checkQuestions.get(i).isState() ? "✅\n\n" : "❌\n\n");
                                }
                                user.getLastHistory().setEndTime(LocalDateTime.now());
                                result += "Start: " + user.getLastHistory().getStartTime().toLocalDate() + " " + user.getLastHistory().getStartTime().toLocalTime() + "\n";
                                result += "End: " + user.getLastHistory().getEndTime().toLocalDate() + " " + user.getLastHistory().getEndTime().toLocalTime() + "\n\n";

                                user.getLastHistory().setQuestionSize(user.getActiveHistory().getCheckQuestions().size());
                                user.getLastHistory().setTrueAnswer(user.getActiveHistory().trueAnswer());

                                result += "Your result: " + user.getActiveHistory().trueAnswer() + "/" + user.getActiveHistory().getCheckQuestions().size()
                                        + "\nRate: " + ((double) (user.getActiveHistory().trueAnswer() * 100 / user.getActiveHistory().getCheckQuestions().size())) + "%";
                                user.setActiveHistory(null);
                                sendMessage.setText(result);

                                if (historyFile.length() != 0) {
                                    histories = getHistoryList();
                                }
                                histories.add(user.getLastHistory());
                                writeHistory(histories);
                            } else {
                                char variant = callBack.charAt(0);
                                if (variant == user.getActiveHistory().getLastQuestion().getTrueAnswer().getLetter()) {
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setState(true);
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setUserAnswer(
                                            user.getActiveHistory().getLastQuestion().getLetterAnswer(variant)
                                    );
                                } else {
                                    user.getActiveHistory().getCheckQuestions().get(user.getActiveHistory().getCheckQuestions().size() - 1).setUserAnswer(
                                            user.getActiveHistory().getLastQuestion().getLetterAnswer(variant)
                                    );
                                }
                                Question question = user.getActiveHistory().getQuestions().get(user.getActiveHistory().getQuestions().size() -
                                        (user.getActiveHistory().getQuestions().size() - user.getActiveHistory().getCheckQuestions().size()));
                                sendMessage.setText(question.getBody());
                                sendMessage.setReplyMarkup(inlineKeyboardSender.createKeyboard(question));
                                user.getActiveHistory().getCheckQuestions().add(CheckQuestion.builder().question(question).state(false).build());
                            }
                            break;
                        }
                    }
                    writeUser(users);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else if (update.hasMessage()) {
                    if (update.getMessage().getDocument() != null) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(update.getMessage().getChatId());
                        sendMessage.setText("Savollar muvaffaqiyatli saqlandi!!!");
                        Document document = update.getMessage().getDocument();

                        try {
                            URL url = new URL("https://api.telegram.org/bot" + API_TOKEN + "/getFile?file_id="+document.getFileId());

                            URLConnection connection = url.openConnection();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                            Response response = gson.fromJson(reader, Response.class);

                            File file = downloadFile(response.getResult().getFilePath());

                            try (FileInputStream fis = new FileInputStream(file)) {
                                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                                subjects = new ArrayList<>();
                                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {

                                    XSSFSheet sheet = workbook.getSheetAt(i);
                                    List<Question> questions = new ArrayList<>();
                                    for (int i1 = 1; i1 <= sheet.getLastRowNum(); i1++) {
                                        List<Answer> answers = new ArrayList<>();
                                        answers.add(Answer.builder().answer(true).body(sheet.getRow(i1).getCell(2).getStringCellValue()).build());
                                        answers.add(Answer.builder().answer(false).body(sheet.getRow(i1).getCell(3).getStringCellValue()).build());
                                        answers.add(Answer.builder().answer(false).body(sheet.getRow(i1).getCell(4).getStringCellValue()).build());
                                        answers.add(Answer.builder().answer(false).body(sheet.getRow(i1).getCell(5).getStringCellValue()).build());
                                        questions.add(Question.builder().body(sheet.getRow(i1).getCell(1).getStringCellValue()).answers(answers).build());
                                    }
                                    Subject subject = Subject.builder().subjectName(workbook.getSheetName(i)).questions(questions).build();

                                    subjects.add(subject);
                                }
                                writeSubject(subjects);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            reader.close();
                        } catch (IOException | TelegramApiException e) {
                            e.printStackTrace();
                        }
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }else if (update.getMessage().getFrom().getId().equals(admin.getId())) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(update.getMessage().getChatId());
                        Message message = update.getMessage();
                        String input;
                        input = update.getMessage().getText();
                        if (input.equals("/start")) {
                            sendMessage.setText("Choose operation");
                            sendMessage.setReplyMarkup(keyboardSender.createKeyboard(new String[]{"Create subject", "Create question",
                                    "Create subject(from excel)", "Get users list", "Get subjects list", "Test", "My histories", "Show tops", "Last histories"}));
                        } else if (input.equals("Create subject") || createSubject) {
                            createSubject = true;
                            if (subjectName == null) {
                                sendMessage.setText("Enter the subject name (f.ex: Mother Tongue): ");

                                subjectName = "";
                            } else {
                                Subject newSubject = new Subject();
                                newSubject.setSubjectName(input);
                                if (historyFile.length() != 0) {
                                    subjects = getSubjectsList();
                                }
                                subjects.add(newSubject);
                                subjectName = null;
                                createSubject = false;
                                writeSubject(subjects);
                                sendMessage.setText("Subject successfully created!");
                            }

                        } else if (input.equals("Create question") || createQuestion) {
                            subjects = getSubjectsList();
                            createQuestion = true;
                            if (question == null) {
                                sendMessage.setText("Choose subject");
                                String[] subjectName = new String[subjects.size()];
                                for (int i = 0; i < subjects.size(); i++) {
                                    subjectName[i] = subjects.get(i).getSubjectName();
                                }
                                sendMessage.setReplyMarkup(keyboardSender.createKeyboard(subjectName));
                                question = "";
                            } else if (question.equals("")) {
                                question = input;
                                sendMessage.setText("Enter the question: " +
                                        "\n\nQuestion body" +
                                        "\nTrueAnswer\nAnswer1\nAnswer2\nAnswer3");
                            } else {
                                String[] strings = input.split("\n");
                                if (strings.length == 5) {
                                    for (Subject subject : subjects) {
                                        if (subject.getSubjectName().equals(question)) {
                                            subject.getQuestions().add(new Question(strings[0], Arrays.asList(
                                                    Answer.builder().body(strings[1]).answer(true).build(),
                                                    Answer.builder().body(strings[2]).answer(false).build(),
                                                    Answer.builder().body(strings[3]).answer(false).build(),
                                                    Answer.builder().body(strings[4]).answer(false).build()
                                            )));
                                            break;
                                        }
                                    }
                                    createQuestion = false;
                                    question = null;
                                    writeSubject(subjects);
                                    sendMessage.setText("Question is successfully added!");
                                    sendMessage.setReplyMarkup(keyboardSender.createKeyboard(new String[]{"Create subject", "Create question",
                                            "Get users list", "Get subjects list", "Test", "My histories"}));
                                } else sendMessage.setText("Incorrect form");
                            }
                        } else if (input.equals("Create subject(from excel)")) {
                            sendMessage.setText("Fill this form");
                            File form = new File("src/main/resources/excels/form.xlsx");
                            SendDocument document = new SendDocument().setDocument(form).setChatId(message.getChatId());
                            try {
                                execute(document);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }

                        } else if (input.equals("Get users list")) {
                            File userInfo = new File("src/main/resources/excels/userInfo.xlsx");
                            try (FileOutputStream outputStream = new FileOutputStream(userInfo)) {
                                XSSFWorkbook workbook = new XSSFWorkbook();
                                users = getUsersList();
                                XSSFSheet sheet = workbook.createSheet("Users");
                                XSSFCellStyle myStyle = workbook.createCellStyle();
                                XSSFFont myFontBold = workbook.createFont();
                                myFontBold.setBold(true);

                                myStyle.setFont(myFontBold);
                                myStyle.setBorderTop(BorderStyle.MEDIUM);
                                myStyle.setBorderRight(BorderStyle.MEDIUM);
                                myStyle.setBorderBottom(BorderStyle.MEDIUM);
                                myStyle.setBorderLeft(BorderStyle.MEDIUM);

                                XSSFCellStyle mySty = workbook.createCellStyle();
                                XSSFFont myFont = workbook.createFont();

                                mySty.setFont(myFont);
                                mySty.setBorderTop(BorderStyle.MEDIUM);
                                mySty.setBorderRight(BorderStyle.MEDIUM);
                                mySty.setBorderBottom(BorderStyle.MEDIUM);
                                mySty.setBorderLeft(BorderStyle.MEDIUM);

                                XSSFRow row1 = sheet.createRow(0);
                                Cell cell = row1.createCell(0);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("№");
                                cell = row1.createCell(1);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("User firstname");
                                cell = row1.createCell(2);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("Username");
                                cell = row1.createCell(3);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("User Id");
                                cell = row1.createCell(4);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("Points");
                                cell = row1.createCell(5);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("Rate");
                                cell = row1.createCell(6);
                                cell.setCellStyle(myStyle);
                                cell.setCellValue("Attempts");

                                for (int i = 0; i < users.size(); i++) {
                                    row1 = sheet.createRow(i + 1);

                                    cell = row1.createCell(0);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(i + 1);
                                    cell = row1.createCell(1);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getName());
                                    cell = row1.createCell(2);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getUsername());
                                    cell = row1.createCell(3);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getId());
                                    cell = row1.createCell(4);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getPoint());
                                    cell = row1.createCell(5);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getRate());
                                    cell = row1.createCell(6);
                                    cell.setCellStyle(mySty);
                                    cell.setCellValue(users.get(i).getHistories().size());
                                }
                                sheet.setColumnWidth(0, 1000);
                                for (int i = 1; i <= row1.getLastCellNum(); i++) {
                                    sheet.setColumnWidth(i, 5000);
                                }

                                workbook.write(outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            SendDocument document = new SendDocument().setDocument(userInfo).setChatId(message.getChatId());
                            sendMessage.setText("File tayyor!");

                            try {
                                execute(document);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }

                        } else if (input.equals("Get subjects list")) {
                            File subjectInfo = new File("src/main/resources/excels/questions.xlsx");
                            try (FileOutputStream outputStream = new FileOutputStream(subjectInfo)) {
                                XSSFWorkbook workbook = new XSSFWorkbook();
                                subjects = getSubjectsList();
                                for (Subject subject : subjects) {
                                    createSheet(workbook, subject);
                                }

                                workbook.write(outputStream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            SendDocument document = new SendDocument().setDocument(subjectInfo).setChatId(message.getChatId());
                            sendMessage.setText("File tayyor!");
                            try {
                                execute(document);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (input.equals("Test")) {
                            subjects = getSubjectsList();
                            List<String> strings = new ArrayList<>();
                            for (int i = 0; i < subjects.size(); i++) {
                                strings.add(subjects.get(i).getSubjectName());
                            }

                            sendMessage.setText("Choose subject: ");
                            String[] strings1 = new String[strings.size()];
                            strings.toArray(strings1);
                            sendMessage.setReplyMarkup(inlineKeyboardSender.createKeyboard(strings1));
                        } else if (update.getMessage().getText().equals("My histories")) {
                            sendMessage.setChatId(update.getMessage().getChatId());
                            String result = "";
                            users = getUsersList();
                            for (User user : users) {
                                if (user.getId().equals(message.getFrom().getId())) {
                                    for (History history : user.getHistories()) {
                                        result += history.getId() + ") Subject Name: " + history.getSubjectName() +
                                                "\nStart Time: " + history.getStartTime().toLocalDate() + " " + history.getStartTime().toLocalTime() +
                                                "\nEnd Time: " + history.getEndTime().toLocalDate() + " " + history.getEndTime().toLocalTime() +
                                                "\nResult: " + history.getTrueAnswer() + "/" + history.getQuestionSize() +
                                                "\nRate: " + history.getRate() + "%\n\n";
                                    }
                                    break;
                                }
                            }
                            sendMessage.setText(result);
                        } else if (update.getMessage().getText().equals("Show tops")) {
                            sendMessage.setChatId(update.getMessage().getChatId());
                            List<User> topUsers = getUsersList();
                            topUsers.sort((o1, o2) -> (int) (o2.getRate() - o1.getRate()));
                            int cycle = Math.min(topUsers.size(), 10);
                            String result = "";
                            for (int i = 0; i < cycle; i++) {
                                result += (i + 1) + ") Foydalanuvchi: " + topUsers.get(i).getName() +
                                        "\nUrinishlari soni: " + topUsers.get(i).getHistories().size() +
                                        "\nO'rtacha ko'rsatkichi: " + topUsers.get(i).getRate() + "\n\n";
                            }

                            sendMessage.setText(result);
                        } else if (update.getMessage().getText().equals("Last histories")) {
                            sendMessage.setChatId(update.getMessage().getChatId());
                            List<History> histories = getHistoryList();
                            String result = "";
                            for (int i = histories.size() - 1, j = 1; i > histories.size() - 11; i--, j++) {
                                result += (j) + ") User: " + histories.get(i).getUserName() +
                                        "\nSubject Name: " + histories.get(i).getSubjectName() +
                                        "\nStart Time: " + histories.get(i).getStartTime().toLocalDate() + " " + histories.get(i).getStartTime().toLocalTime() +
                                        "\nEnd Time: " + histories.get(i).getEndTime().toLocalDate() + " " + histories.get(i).getEndTime().toLocalTime() +
                                        "\nResult: " + histories.get(i).getTrueAnswer() + "/" + histories.get(i).getQuestionSize() +
                                        "\nRate: " + histories.get(i).getRate() + "%\n\n";
                            }
                            sendMessage.setText(result);
                        } else {
                            Document document = message.getDocument();
                            System.out.println(document);
//                    downloadFile()


                            sendMessage.setText("Choose operation");
                            sendMessage.setReplyMarkup(keyboardSender.createKeyboard(new String[]{"Create subject", "Create question",
                                    "Get users list", "Get subjects list", "Test", "My histories", "Show tops"}));
                        }
                        try {
                            execute(sendMessage);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }

                        users = getUsersList();
                        for (User user : users) {
                            if (user.getLastHistory().getQuestionSize() == 0 && LocalDateTime.now().toLocalDate().getDayOfMonth() - user.getLastHistory().getStartTime().toLocalDate().getDayOfMonth() >= 1) {
                                user.setActiveHistory(null);
                            }
                            if (user.getChatId() != null) {
                                if (user.getLastHistory().getQuestionSize() == 0 && LocalDateTime.now().toLocalTime().getHour() - user.getLastHistory().getStartTime().toLocalTime().getHour() >= 3) {
                                    sendMessage.setChatId(user.getChatId());
                                    sendMessage.setText("Eslatma!!!\nTestga start berilgandan keyin 1 kun ichida ishlanmasa, test avtomat o'chirib yuboriladi!!!");
                                    try {
                                        execute(sendMessage);
                                    } catch (TelegramApiException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        writeUser(users);
                    } else {
                        SendMessage sendMessage = new SendMessage();
                        if (update.getMessage().getText().equals("/start")) {
                            sendMessage.setChatId(update.getMessage().getChatId());
                            Message message = update.getMessage();

                            sendMessage.setText("**Assalomu alaykum " + message.getFrom().getFirstName() + "\nBotimizga xush kelibsiz");
                            users = getUsersList();
                            boolean hasUser = false;
                            for (User user1 : users) {
                                if (user1.getId().equals(message.getFrom().getId())) {
                                    hasUser = true;
                                    break;
                                }
                            }

                            if (!hasUser) {
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                                User user = User.builder().name(message.getFrom().getFirstName())
                                        .username(message.getFrom().getUserName())
                                        .id(message.getFrom().getId())
                                        .point(10)
                                        .chatId(String.valueOf(update.getMessage().getChatId()))
                                        .build();
                                users.add(user);
                                writeUser(users);
                                sendMessage.setChatId(update.getMessage().getChatId());
                                sendMessage.setText("Sizga 10 ball berildi.\nEslatib o'tamiz, har safar test ishlaganingizda balingiz 1 balldan kamayadi. \nOmad!!!");
                            }
                            sendMessage.setReplyMarkup(keyboardSender.createKeyboard(new String[]{"Test",
                                    "My histories", "Get points", "Show tops"}));

                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (update.getMessage().getText().equals("Test")) {
                            Message message = update.getMessage();
                            sendMessage.setChatId(update.getMessage().getChatId());
                            users = getUsersList();
                            for (User user : users) {
                                if (message.getFrom().getId().equals(user.getId())) {
                                    if (user.getPoint() > 0) {
                                        subjects = getSubjectsList();
                                        List<String> strings = new ArrayList<>();
                                        for (int i = 0; i < subjects.size(); i++) {
                                            strings.add(subjects.get(i).getSubjectName());
                                        }
                                        sendMessage.setText("Choose subject: ");
                                        String[] strings1 = new String[strings.size()];
                                        strings.toArray(strings1);
                                        sendMessage.setReplyMarkup(inlineKeyboardSender.createKeyboard(strings1));
                                    } else {
                                        sendMessage.setText("Sizda ballar qolmagan!!!");
                                        sendMessage.setReplyMarkup(keyboardSender.createKeyboard(new String[]{"Get points"}));
                                    }
                                    break;
                                }
                            }
                            writeUser(users);
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (update.getMessage().getText().equals("My histories")) {
                            Message message = update.getMessage();
                            sendMessage.setChatId(update.getMessage().getChatId());
                            String result = "";
                            users = getUsersList();
                            for (User user : users) {
                                if (user.getId().equals(message.getFrom().getId())) {
                                    for (History history : user.getHistories()) {
                                        result += history.getId() + ") Subject Name: " + history.getSubjectName() +
                                                "\nStart Time: " + history.getStartTime().toLocalDate() + " " + history.getStartTime().toLocalTime() +
                                                "\nEnd Time: " + history.getEndTime().toLocalDate() + " " + history.getEndTime().toLocalTime() +
                                                "\nResult: " + history.getTrueAnswer() + "/" + history.getQuestionSize() +
                                                "\nRate: " + history.getRate() + "%\n\n";
                                    }
                                    break;
                                }
                            }
                            sendMessage.setText(result);
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (update.getMessage().getText().equals("Get points")) {
                            Message message = update.getMessage();
                            sendMessage.setChatId(update.getMessage().getChatId());
                            sendMessage.setText("Bitta ball narxi 5000 so'm\n" +
                                    "10 ta ball -> 45 000 so'm\n20 ta ball -> 90 000 so'm\n 50 ta ball -> 200 000 so'm\n\n" +
                                    "Mablag'ni **** **** **** **** kartaga o'tkazganingizdan keyin @Murtazayev_M ga chekni rasmini va telegramdagi id raqamingizni jo'nating");
                            SendPhoto photo1 = new SendPhoto().setChatId(message.getChatId()).setPhoto(new File("src/main/resources/photos/photo1.jpg"));
                            SendPhoto photo2 = new SendPhoto().setChatId(message.getChatId()).setPhoto(new File("src/main/resources/photos/photo2.jpg"));
                            try {
                                execute(photo1);
                                execute(photo2);
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        } else if (update.getMessage().getText().equals("Show tops")) {
                            sendMessage.setChatId(update.getMessage().getChatId());
                            List<User> topUsers = getUsersList();
                            topUsers.sort((o1, o2) -> (int) (o2.getRate() - o1.getRate()));
                            int cycle = Math.min(topUsers.size(), 10);
                            String result = "";
                            for (int i = 0; i < cycle; i++) {
                                result += (i + 1) + ") Foydalanuvchi: " + topUsers.get(i).getName() +
                                        "\nUrinishlari soni: " + topUsers.get(i).getHistories().size() +
                                        "\nO'rtacha ko'rsatkichi: " + topUsers.get(i).getRate() + "\n\n";
                            }

                            sendMessage.setText(result);
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }).start();
    }

    public void createSheet(XSSFWorkbook workbook, Subject subject) {
        XSSFSheet sheet = workbook.createSheet(subject.getSubjectName());
        XSSFCellStyle myStyle = workbook.createCellStyle();
        XSSFFont myFontBold = workbook.createFont();
        myFontBold.setBold(true);

        myStyle.setFont(myFontBold);
        myStyle.setBorderTop(BorderStyle.MEDIUM);
        myStyle.setBorderRight(BorderStyle.MEDIUM);
        myStyle.setBorderBottom(BorderStyle.MEDIUM);
        myStyle.setBorderLeft(BorderStyle.MEDIUM);

        XSSFCellStyle mySty = workbook.createCellStyle();
        XSSFFont myFont = workbook.createFont();

        mySty.setFont(myFont);
        mySty.setBorderTop(BorderStyle.MEDIUM);
        mySty.setBorderRight(BorderStyle.MEDIUM);
        mySty.setBorderBottom(BorderStyle.MEDIUM);
        mySty.setBorderLeft(BorderStyle.MEDIUM);

        XSSFRow row1 = sheet.createRow(0);
        Cell cell = row1.createCell(0);
        cell.setCellStyle(myStyle);
        cell.setCellValue("№");
        cell = row1.createCell(1);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Question");
        cell = row1.createCell(2);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Correct answer");
        cell = row1.createCell(3);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Answer 1");
        cell = row1.createCell(4);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Answer 2");
        cell = row1.createCell(5);
        cell.setCellStyle(myStyle);
        cell.setCellValue("Answer 3");


        for (int i = 0; i < subject.getQuestions().size(); i++) {
            row1 = sheet.createRow(i + 1);

            cell = row1.createCell(0);
            cell.setCellStyle(mySty);
            cell.setCellValue(i + 1);
            cell = row1.createCell(1);
            cell.setCellStyle(mySty);
            cell.setCellValue(subject.getQuestions().get(i).getBody());
            cell = row1.createCell(2);
            cell.setCellStyle(mySty);
            cell.setCellValue(subject.getQuestions().get(i).getTrueAnswer().getBody());
            cell = row1.createCell(3);
            cell.setCellStyle(mySty);
            cell.setCellValue(subject.getQuestions().get(i).getAnswers().get(1).getBody());
            cell = row1.createCell(4);
            cell.setCellStyle(mySty);
            cell.setCellValue(subject.getQuestions().get(i).getAnswers().get(2).getBody());
            cell = row1.createCell(5);
            cell.setCellStyle(mySty);
            cell.setCellValue(subject.getQuestions().get(i).getAnswers().get(3).getBody());

        }
        sheet.setColumnWidth(0, 1000);
        sheet.setColumnWidth(1, 15000);
        for (int i = 2; i <= row1.getLastCellNum(); i++) {
            sheet.setColumnWidth(i, 3000);
        }
    }

    public List<User> getUsersList() {
        GsonUserHelper userHelper = new GsonUserHelper();
        return userHelper.converter(userFile);
    }

    public void writeUser(List<User> users) {
        try (Writer writer = new FileWriter(userFile)) {
            writer.write(gson.toJson(users));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Subject> getSubjectsList() {
        GsonSubjectHelper subjectHelper = new GsonSubjectHelper();
        return subjectHelper.converter(subjectFile);
    }

    public void writeSubject(List<Subject> subjects) {
        try (Writer writer = new FileWriter(subjectFile)) {
            writer.write(gson.toJson(subjects));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<History> getHistoryList() {
        GsonHistoryHelper historyHelper = new GsonHistoryHelper();
        return historyHelper.converter(historyFile);
    }

    public void writeHistory(List<History> histories) {
        try (Writer writer = new FileWriter(historyFile)) {
            writer.write(gson.toJson(histories));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "solvingtestbot";
    }

    @Override
    public String getBotToken() {
        return API_TOKEN;
    }
}
