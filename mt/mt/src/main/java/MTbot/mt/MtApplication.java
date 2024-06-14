package MTbot.mt;

import MTbot.mt.entity.Document;
import MTbot.mt.entity.User;
import MTbot.mt.service.impl.DocumentServiceImpl;
import MTbot.mt.service.impl.UserServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.mail.im.botapi.BotApiClient;
import ru.mail.im.botapi.BotApiClientController;
import ru.mail.im.botapi.api.entity.InlineKeyboardButton;
import ru.mail.im.botapi.api.entity.SendTextRequest;
import ru.mail.im.botapi.fetcher.event.CallbackQueryEvent;
import ru.mail.im.botapi.fetcher.event.NewMessageEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class MtApplication {

    private static final Logger logger = LoggerFactory.getLogger(MtApplication.class);

    private final UserServiceImpl userService;

    private final DocumentServiceImpl documentService;

    private static final String TOKEN = "001.2362470500.3852636618:1011788675";

    private static final BotApiClient CLIENT = new BotApiClient(TOKEN);

    private static final BotApiClientController CONTROLLER = BotApiClientController.startBot(CLIENT);

    public static void main(String[] args) {
        SpringApplication.run(MtApplication.class, args);
    }

    @PostConstruct
    public void connect() {
        CLIENT.addOnEventFetchListener(events -> {
            if (events.get(events.size() - 1).getType().equals("newMessage")) {
                NewMessageEvent newMessageEvent = (NewMessageEvent) events.get(events.size() - 1);
                if (newMessageEvent.getText() == null)
                    return;

                logger.info(newMessageEvent.getChat().getChatId() + " " + newMessageEvent.getText());

                try {
                    if (!userService.existByNickname(newMessageEvent.getChat().getChatId())) {
                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(newMessageEvent.getChat().getChatId())
                                        .setText("Приветствую тебя, " + newMessageEvent.getFrom().getFirstName() +
                                                "! Меня зовут MTbot, я буду помогать тебе погружаться в технологии. " +
                                                "Готов(а) начать?")
                                        .setKeyboard((Arrays.asList(Arrays.asList(InlineKeyboardButton.callbackButton("Да", "readyYes", null),
                                                InlineKeyboardButton.callbackButton("Нет", "readyNo", null)))))
                        );
                        userService.saveUser(
                                User.builder()
                                        .nickname(newMessageEvent.getChat().getChatId())
                                        .localDateTime(LocalDateTime.now())
                                        .isPing(false)
                                        .build()
                        );
                    } else if (newMessageEvent.getText().equalsIgnoreCase("Готово")) {
                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(newMessageEvent.getChat().getChatId())
                                        .setText("Молодец! Давай проверим, как ты усвоил(а) материал")
                        );
                    } else {
                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(newMessageEvent.getChat().getChatId())
                                        .setText("Я тебя не понимаю, " + newMessageEvent.getFrom().getFirstName() + ", выбери вариант ответа Да/Нет" +
                                                " или напиши “готово”")
                        );
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (events.get(events.size() - 1).getType().equals("callbackQuery")) {
                CallbackQueryEvent callbackQueryEvent = (CallbackQueryEvent) events.get(events.size() - 1);

                logger.info(callbackQueryEvent.getFrom().getFirstName() +
                        callbackQueryEvent.getFrom().getLastName() +
                        callbackQueryEvent.getCallbackData());

                if (callbackQueryEvent.getCallbackData().equals("readyYes")) {
                    try {
                        List<Document> documentList = documentService.findAllDocuments();

                        String concatenatedReferences = documentList.stream()
                                .map(Document::getReference)
                                .collect(Collectors.joining(", "));

                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(callbackQueryEvent.getFrom().getUserId())
                                        .setText("Первое задание: изучить регламенты нашей компании. " +
                                                "После того, как ты прочитаешь, напиши мне “готово”. Время на выполнение " +
                                                "60 минут. Вот ссылки: " + concatenatedReferences)
                        );

                        userService.setIsPingTrue(userService.findUserByNickname(callbackQueryEvent.getFrom().getUserId()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (callbackQueryEvent.getCallbackData().equals("readyNo")) {
                    try {
                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(callbackQueryEvent.getFrom().getUserId())
                                        .setText("Возникли сложности?")
                                        .setKeyboard((Arrays.asList(Arrays.asList(InlineKeyboardButton.callbackButton("Да", "difficultiesYes", null),
                                                InlineKeyboardButton.callbackButton("Нет", "difficultiesNo", null)))))
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (callbackQueryEvent.getCallbackData().equals("difficultiesYes")) {
                    try {
                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(callbackQueryEvent.getFrom().getUserId())
                                        .setText("Рекомендую обратиться к ментору!")
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (callbackQueryEvent.getCallbackData().equals("difficultiesNo")) {
                    try {
                        List<Document> documentList = documentService.findAllDocuments();

                        String concatenatedReferences = documentList.stream()
                                .map(Document::getReference)
                                .collect(Collectors.joining(", "));

                        CONTROLLER.sendTextMessage(
                                new SendTextRequest()
                                        .setChatId(callbackQueryEvent.getFrom().getUserId())
                                        .setText("Первое задание: изучить регламенты нашей компании. " +
                                                "После того, как ты прочитаешь, напиши мне “готово”. Время на выполнение " +
                                                "60 минут. Вот ссылки: " + concatenatedReferences)
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Scheduled(fixedRate = 36000)
    private void pingUser() throws IOException {
        List<User> users = userService.getUsersIsPing();

        for (User user : users) {
            if (Boolean.TRUE.equals(user.getIsPing()) &&
                    user.getLocalDateTime().plusHours(1).isBefore(LocalDateTime.now())) {
                CONTROLLER.sendTextMessage(
                        new SendTextRequest()
                                .setChatId(user.getNickname())
                                .setText("Возникли какие-то трудности?")
                                .setKeyboard((Arrays.asList(Arrays.asList(
                                        InlineKeyboardButton.callbackButton("Да",
                                                "difficultiesYes",
                                                null),
                                        InlineKeyboardButton.callbackButton("Нет",
                                                "difficultiesNo",
                                                null)
                                ))))
                );

                user.setLocalDateTime(LocalDateTime.now());
                userService.setIsPingFalse(user);
            }
        }
    }

}
