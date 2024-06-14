package MTbot.mt.service;

import MTbot.mt.entity.User;

import java.util.List;

public interface UserService {
    boolean existByNickname(String nickname);

    User saveUser(User user);

    List<User> getUsersIsPing();

    void setIsPingFalse(User user);

    void setIsPingTrue(User user);

    User findUserByNickname(String nickname);
}
