package com.Aga.Agali.service;


import com.Aga.Agali.entity.User;
import com.Aga.Agali.enums.UserState;
import com.Aga.Agali.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getOrCreate(Long chatId, org.telegram.telegrambots.meta.api.objects.User telegramUser) {
        return userRepository.findByChatId(chatId)
                .orElseGet(() -> {
                    User user = User.builder()
                            .chatId(chatId)
                            .firstName(telegramUser.getFirstName())
                            .lastName(telegramUser.getLastName())
                            .username(telegramUser.getUserName())
                            .state(UserState.IDLE)
                            .build();
                    log.info("Yeni istifadəçi yaradıldı: {}", chatId);
                    return userRepository.save(user);
                });
    }

    public void updateState(User user, UserState state) {
        user.setState(state);
        userRepository.save(user);
        log.info("İstifadəçi {} vəziyyəti {} oldu", user.getChatId(), state);
    }
}