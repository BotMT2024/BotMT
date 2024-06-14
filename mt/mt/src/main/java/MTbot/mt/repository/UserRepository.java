package MTbot.mt.repository;

import MTbot.mt.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByNickname(String nickname);

    List<User> findByIsPingTrue();

    User findUserByNickname(String nickname);
}
