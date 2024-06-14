package MTbot.mt.service.impl;

import MTbot.mt.entity.User;
import MTbot.mt.repository.UserRepository;
import MTbot.mt.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public boolean existByNickname(String nickname) {
        try{
            return userRepository.existsByNickname(nickname);
        }
        catch (Exception e){
            return false;
        }
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> getUsersIsPing() {
        return userRepository.findByIsPingTrue();
    }

    @Override
    public void setIsPingFalse(User user) {
        user.setIsPing(false);
        userRepository.save(user);
    }

    @Override
    public void setIsPingTrue(User user) {
        user.setIsPing(true);
        userRepository.save(user);
    }

    @Override
    public User findUserByNickname(String nickname) {
        return userRepository.findUserByNickname(nickname);
    }

}
