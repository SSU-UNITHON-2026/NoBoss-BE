package blank.noboss.unithon.repository.message;

import blank.noboss.unithon.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
